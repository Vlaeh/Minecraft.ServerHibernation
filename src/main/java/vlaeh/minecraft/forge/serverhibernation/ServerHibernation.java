package vlaeh.minecraft.forge.serverhibernation;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import vlaeh.minecraft.forge.serverhibernation.commands.ServerHibernationCommand;

@Mod(modid = ServerHibernation.MODID, 
     version = ServerHibernation.VERSION, 
     name = ServerHibernation.NAME, 
     acceptableRemoteVersions = "*", 
     acceptedMinecraftVersions = "[1.9,1.13)"
     )
public class ServerHibernation 
{
    public static final String MODID = "serverhibernation";
    public static final String VERSION = "1.1";
    public static final String NAME = "Server Hibernation";

    public static Configuration config;
    public static boolean hibernationEnabled = true;

    @SidedProxy(serverSide = "vlaeh.minecraft.forge.serverhibernation.server.ServerProxy", clientSide = "vlaeh.minecraft.forge.serverhibernation.CommonProxy")
    public static CommonProxy proxy;

    @SidedProxy(serverSide = "vlaeh.minecraft.forge.serverhibernation.server.I18nServer", clientSide = "vlaeh.minecraft.forge.serverhibernation.client.I18nClient")
    public static I18nProxy i18n;

    @Mod.Instance
    public static ServerHibernation instance;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ServerHibernationCommand());
    }

    @EventHandler
    public void onFMLServerStartedEvent(final FMLServerStartedEvent event) {
        proxy.onFMLServerStartedEvent(event);
    }

    @EventHandler
    public void onFMLServerStoppingEvent(final FMLServerStoppingEvent event) {
        proxy.onFMLServerStoppingEvent(event);
    }

    public static void syncConfig() {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER)
            i18n.load(config.getString("serverLang", Configuration.CATEGORY_GENERAL, "en_US", "Server language"));
        hibernationEnabled = config.getBoolean("1.enabled", Configuration.CATEGORY_GENERAL, hibernationEnabled,
                "serverhibernation.conf.enabled.tooltip", "serverhibernation.conf.enabled");
        if (config.hasChanged())
            config.save();
    }

}
