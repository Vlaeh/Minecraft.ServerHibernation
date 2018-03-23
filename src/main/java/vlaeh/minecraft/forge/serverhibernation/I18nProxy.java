package vlaeh.minecraft.forge.serverhibernation;

public interface I18nProxy {

    public String format(String key, Object... parameters);

    public void load(final String lang);

}
