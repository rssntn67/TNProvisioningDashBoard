package org.opennms.vaadin.provision.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashBoardConfig {

	protected static final String PROPERTIES_FILE_PATH = "/etc/tomcat7/provision-dashboard.properties";
	protected static final String PROPERTIES_URLS_KEY = "urls";
	protected static final String PROPERTIES_URL_ROOT_KEY = "url.";
	protected static final String PROPERTIES_DB_URL_KEY = "db_url";
	protected static final String PROPERTIES_DB_USER_KEY = "db_username";
	protected static final String PROPERTIES_DB_PASS_KEY = "db_password";

	protected String[] URL_LIST = new String[] {
			"http://demo.arsinfo.it/opennms/rest",
			"http://localhost:8980/opennms/rest"
		};

	private Properties m_configuration = new Properties();

	private final static Logger logger = Logger.getLogger(DashBoardConfig.class.getName());	

	public void reload() {
    	File file = new File(PROPERTIES_FILE_PATH);
    	if (file.exists() && file.isFile()) {
    		try {
    			m_configuration.load(new FileInputStream(file));
    			logger.info("Loaded Configuration file: " + PROPERTIES_FILE_PATH );
    		} catch (IOException ex) {
    			logger.log(Level.INFO, "Cannot load configuration file: ", ex );
    		}
  
    	}
	}

	public DashBoardConfig() {
		super();
		reload();
	}

	public String[] getUrls() {
		List<String> urls = new ArrayList<String>();
		if (m_configuration.getProperty(PROPERTIES_URLS_KEY) != null ) {
			for (String urlKey: m_configuration.getProperty(PROPERTIES_URLS_KEY).split(",")) {
				String key = PROPERTIES_URL_ROOT_KEY+urlKey;
				if (m_configuration.getProperty(key) != null)
					urls.add(m_configuration.getProperty(key));
			}
		}
		if (urls.size() > 0)
			return urls.toArray(new String[urls.size()]);
		else
			return URL_LIST;
	}

	public String getDbUrl() {
		if (m_configuration.getProperty(PROPERTIES_DB_URL_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_URL_KEY);
		else 
			return "jdbc:postgresql://172.25.200.36:5432/tnnet";
	}
	
	public String getDbUsername() {
		if (m_configuration.getProperty(PROPERTIES_DB_USER_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_USER_KEY);
		else 
			return "isi_writer";
	}
	
	public String getDbPassword() {
		if (m_configuration.getProperty(PROPERTIES_DB_PASS_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_PASS_KEY);
		else 
			return "Oof6Eezu";
		
	}
}
