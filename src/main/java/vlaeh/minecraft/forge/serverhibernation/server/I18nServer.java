package vlaeh.minecraft.forge.serverhibernation.server;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import vlaeh.minecraft.forge.serverhibernation.I18nProxy;

public class I18nServer implements I18nProxy {
	
	private final Properties properties = new Properties();

	private final void loadProperties(final String name) {
		final String fileName = "/assets/serverhibernation/lang/" + name + ".lang";
		try {
			final InputStream in = getClass().getResourceAsStream(fileName);
			if (in != null) {
		        for (String s : IOUtils.readLines(in, Charsets.UTF_8)) {
		            if (!s.isEmpty() && s.charAt(0) != 35) {
		            	final int index = s.indexOf('=');
		                if (index < 0)
		                	continue;
		                final String value = index < s.length()-1 ? s.substring(index + 1) : "";
		                this.properties.put(s.substring(0, index), value);
		            }
		        }
			} else 
				System.err.println("ServerHibernation: resource file '" + fileName + "' not found");
		} catch (Exception e) {
			System.err.println("Error loading lang file " + fileName);
			e.printStackTrace();
		}
	}
	
	public void load(final String lang) {
		properties.clear();
		loadProperties("en_US");
		if (! lang.equalsIgnoreCase("en_us"))
			loadProperties(lang);
		if (properties.isEmpty()) {
			System.err.println("ServerHibernation .lang files not found");
		}
	}
	
	@Override
    public String format(final String key, final Object... parameters)
    {
    	final String value = (String)this.properties.get(key);
        try {
            return String.format(value != null ? value : key, parameters);
        } catch (final Exception e) {
            return "Format error: " + e;
        }
    }

}
