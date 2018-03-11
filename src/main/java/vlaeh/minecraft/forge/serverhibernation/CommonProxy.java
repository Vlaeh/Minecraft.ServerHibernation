package vlaeh.minecraft.forge.serverhibernation;

import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy {

    public void onFMLServerStartedEvent(FMLServerStartedEvent event) {
    }

    public void onFMLServerStoppingEvent(FMLServerStoppingEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public boolean forceStartHibernation() {
        return false;
    }

    public boolean forceStopHibernation() {
        return false;
    }

    public String getSavedStatus() {
        return "";
    }

    public String getStatus() {
        return "";
    }
}
