package vlaeh.minecraft.forge.serverhibernation.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
import vlaeh.minecraft.forge.serverhibernation.CommonProxy;
import vlaeh.minecraft.forge.serverhibernation.ServerHibernation;
import vlaeh.minecraft.forge.serverhibernation.commands.ServerHibernationCommand;

public class ServerProxy extends CommonProxy {

    private int playerCount = 0;
    private String doDaylightCycle = "true";
    private String randomTickSpeed = "3";
    private String doFireTick = "true";

    public static final Logger LOGGER = LogManager.getLogger(ServerHibernation.MODID);

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onFMLServerStartedEvent(final FMLServerStartedEvent event) {
        final MinecraftServer server = FMLServerHandler.instance().getServer();
        if (server == null)
            return;
        LOGGER.info((ServerHibernation.hibernationEnabled ? "enabled" : "disabled") + ": server started "
                + getStatus(server));
        startHibernation(server);
    }

    @Override
    public void onFMLServerStoppingEvent(final FMLServerStoppingEvent event) {
        final MinecraftServer server = FMLServerHandler.instance().getServer();
        LOGGER.info((ServerHibernation.hibernationEnabled ? "enabled" : "disabled") + ": server stopping "
                + getStatus(server));
        stopHibernation(server);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.getModID().equals(ServerHibernation.MODID)) {
            LOGGER.info((ServerHibernation.hibernationEnabled ? "enabled" : "disabled"));
            ServerHibernation.syncConfig();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(final PlayerLoggedInEvent event) {
        ++playerCount;
        if (playerCount == 1) {
            LOGGER.info("first player joined server");
            final MinecraftServer server = event.player.world.getMinecraftServer();
            if (stopHibernation(server))
                event.player
                        .sendMessage(new TextComponentString(TextFormatting.YELLOW + ServerHibernationCommand.COMMAND
                                + ": server has just been awaken " + TextFormatting.RESET + getStatus(server)));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(final PlayerLoggedOutEvent event) {
        --playerCount;
        if (playerCount == 0) {
            LOGGER.info("last player left server");
            startHibernation(event.player.world.getMinecraftServer());
        }
    }

    public void startHibernation(final MinecraftServer server) {
        if (ServerHibernation.hibernationEnabled)
            forceStartHibernation(server);
        LOGGER.info(getStatus(server));
    }

    @Override
    public boolean forceStartHibernation() {
        return forceStartHibernation(FMLServerHandler.instance().getServer());
    }

    public boolean forceStartHibernation(final MinecraftServer server) {
        final boolean isDayLightCycleOff = "false"
                .equals(server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle"));
        final boolean isTickSpeedOff = "0"
                .equals(server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed"));
        final boolean isFireTickOff = "false"
                .equals(server.worldServerForDimension(0).getGameRules().getString("doFireTick"));
        if ((!isDayLightCycleOff) || (!isTickSpeedOff)) {
            LOGGER.info("hibernating server");
            doDaylightCycle = server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle");
            randomTickSpeed = server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed");
            doFireTick = server.worldServerForDimension(0).getGameRules().getString("doFireTick");
            if (!isDayLightCycleOff)
                server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
            if (!isTickSpeedOff)
                server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("randomTickSpeed", "0");
            if (!isFireTickOff)
                server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doFireTick", "false");
            return true;
        }
        LOGGER.info("server already hibernating");
        return false;
    }

    public boolean stopHibernation(final MinecraftServer server) {
        boolean ret = false;
        if (ServerHibernation.hibernationEnabled)
            ret = forceStoptHibernation(server);
        LOGGER.info(getStatus(server));
        return ret;
    }

    @Override
    public boolean forceStopHibernation() {
        return forceStoptHibernation(FMLServerHandler.instance().getServer());
    }

    public boolean forceStoptHibernation(final MinecraftServer server) {
        final boolean isDayLightCycleOff = "false"
                .equals(server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle"));
        final boolean isTickSpeedOff = "0"
                .equals(server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed"));
        final boolean isFireTickOff = "false"
                .equals(server.worldServerForDimension(0).getGameRules().getString("doFireTick"));
        if (isDayLightCycleOff && isTickSpeedOff && isFireTickOff) {
            LOGGER.info("waking up server");
            if ("false".equals(doDaylightCycle) && "0".equals(randomTickSpeed) && "false".equals(doFireTick)) {
                doDaylightCycle = "true";
                randomTickSpeed = "3";
                doFireTick = "true";
            }
            server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doDaylightCycle", doDaylightCycle);
            server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("randomTickSpeed", randomTickSpeed);
            server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doFireTick", doFireTick);
            return true;
        }
        LOGGER.info("server already awaken");
        return false;
    }

    public String getStatus() {
        return getStatus(FMLServerHandler.instance().getServer());
    }

    public String getStatus(final MinecraftServer server) {
        return "[doDaylightCycle=" + TextFormatting.GREEN
                + server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle") + TextFormatting.RESET
                + ", randomTickSpeed=" + TextFormatting.GREEN
                + server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed") + TextFormatting.RESET
                + ", doFireTick=" + TextFormatting.GREEN
                + server.worldServerForDimension(0).getGameRules().getString("doFireTick") + TextFormatting.RESET + "]";
    }

    public String getSavedStatus() {
        return "[doDaylightCycle=" + TextFormatting.GREEN + doDaylightCycle + TextFormatting.RESET
                + ", randomTickSpeed=" + TextFormatting.GREEN + randomTickSpeed + TextFormatting.RESET //
                + ", doFireTick=" + TextFormatting.GREEN + doFireTick + TextFormatting.RESET //
                + "]";
    }

}
