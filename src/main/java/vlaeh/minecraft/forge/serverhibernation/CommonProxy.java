package vlaeh.minecraft.forge.serverhibernation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

public class CommonProxy {

    private int playerCount = 0;
    private boolean hibernating = false;
    private String doDaylightCycle = "true";
    private String randomTickSpeed = "3";

    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onFMLServerStartedEvent(final FMLServerStartedEvent event) {
        System.out.println("ServerHibernation " + (ServerHibernation.hibernationEnabled ? "enabled" : "disabled") + ": server started");
        try {
            startHibernation(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onFMLServerStoppingEvent(final FMLServerStoppingEvent event) {
        System.out.println("ServerHibernation " + (ServerHibernation.hibernationEnabled ? "enabled" : "disabled") + ": server stopping");
        stopHibernation(FMLServerHandler.instance().getServer(), false);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.getModID().equals(ServerHibernation.MODID))
            ServerHibernation.syncConfig();
    }

    public void startHibernation(final boolean force) {
        startHibernation(FMLServerHandler.instance().getServer(), force);
    }

    public void startHibernation(final MinecraftServer server, final boolean force) {
        if (ServerHibernation.hibernationEnabled || force) {
            System.out.println("ServerHibernation: hibernating server");
            if (hibernating)
                return;
            hibernating = true;
            doDaylightCycle = server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle");
            server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
            randomTickSpeed = server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed");
            server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("randomTickSpeed", "0");
        }
        System.out.println("ServerHibernation: " + getStatus(server));
    }

    public void stopHibernation(final boolean force) {
        stopHibernation(FMLServerHandler.instance().getServer(), force);
    }

    public void stopHibernation(final MinecraftServer server, final boolean force) {
        if (ServerHibernation.hibernationEnabled || force) {
            System.out.println("ServerHibernation: waking up server");
            hibernating = false;
            if ("false".equals(server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle"))) {
                if ("false".equals(doDaylightCycle))
                    doDaylightCycle = "true";
                server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("doDaylightCycle", doDaylightCycle);
            }
            if ("0".equals(server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed"))) {
                if ("0".equals(randomTickSpeed))
                    randomTickSpeed = "3";
                server.worldServerForDimension(0).getGameRules().setOrCreateGameRule("randomTickSpeed", randomTickSpeed);
            }
            System.out.println("ServerHibernation: " + getStatus(server));
        }
    }

    public String getStatus() {
        return getStatus(FMLServerHandler.instance().getServer());
    }

    public String getStatus(final MinecraftServer server) {
        return "[doDaylightCycle=" + TextFormatting.GREEN 
                + server.worldServerForDimension(0).getGameRules().getString("doDaylightCycle")
                + TextFormatting.RESET + ", randomTickSpeed=" + TextFormatting.GREEN
                + server.worldServerForDimension(0).getGameRules().getString("randomTickSpeed")
                + TextFormatting.RESET + "]";
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(final PlayerLoggedInEvent event) {
        ++playerCount;
        if (playerCount == 1) {
            System.out.println("ServerHibernation: first player joined server");
            stopHibernation(event.player.world.getMinecraftServer(), false);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(final PlayerLoggedOutEvent event) {
        --playerCount;
        if (playerCount == 0) {
            System.out.println("ServerHibernation: last player left server");
            startHibernation(event.player.world.getMinecraftServer(), false);
        }
    }

    public final void sendMessageToPlayers(final World world, final ITextComponent text) {
        for (EntityPlayer player : world.playerEntities) {
            player.sendMessage(text);
        }
    }

}
