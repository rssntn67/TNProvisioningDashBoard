package org.opennms.vaadin.provision.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashBoardConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5792020022013510381L;
	protected static final String PROPERTIES_FILE_PATH = "provision-dashboard.properties";
	protected static final String[] PROPERTIES_FILE_PATHS = {
		"/opt/opennms/etc/provision-dashboard.properties",
		"/etc/opennms/provision-dashboard.properties",
		"/etc/tomcat7/provision-dashboard.properties",
		"/usr/share/tomcat/conf/provision-dashboard.properties"
	};
	protected static final String PROPERTIES_URLS_KEY = "urls";
	protected static final String PROPERTIES_URL_ROOT_KEY = "url.";
	protected static final String PROPERTIES_DB_URL_KEY = "db_url";
	protected static final String PROPERTIES_DB_USER_KEY = "db_username";
	protected static final String PROPERTIES_DB_PASS_KEY = "db_password";

	protected static final String PROPERTIES_KETTLE_URL_KEY = "kettle_url";
	protected static final String PROPERTIES_KETTLE_USER_KEY = "kettle_username";
	protected static final String PROPERTIES_KETTLE_PASS_KEY = "kettle_password";

	protected static final String VERSION_FILE_PATH = "provision-version.properties";
	protected static final String PROPERTIES_APP_NAME = "projectName";
	protected static final String PROPERTIES_APP_VERSION = "versionName";
	protected static final String PROPERTIES_APP_BUILD = "buildTimestamp";

	protected String[] URL_LIST = new String[] {
			"http://demo.arsinfo.it/opennms/rest",
			"http://localhost:8980/opennms/rest"
		};

	private Properties m_configuration = new Properties();

	private final static Logger logger = Logger.getLogger(DashBoardConfig.class.getName());	

	public void reload() {
		
		try {
            m_configuration.load(this.getClass().getClassLoader().getResourceAsStream(VERSION_FILE_PATH));
			logger.info("Loaded Classpath Version file: " + VERSION_FILE_PATH);
		} catch (IOException ex) {
			logger.log(Level.INFO, "Cannot load version file: ", ex );
		}

		for (String name: PROPERTIES_FILE_PATHS) {
			File file = new File(name);
    		if (file.exists() && file.isFile()) {
    			try {
    				m_configuration.load(new FileInputStream(file));
    				logger.info("Loaded Configuration file: " + file.getPath());
    				return;
    			} catch (IOException ex) {
    				logger.log(Level.INFO, "Cannot load configuration file: ", ex );
    			}
    		}
		}
		try {
			m_configuration.load(
					this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH));
			logger.info("Loaded Classpath DefaultConfiguration file: " + PROPERTIES_FILE_PATH);
		} catch (IOException ex) {
			logger.log(Level.INFO, "Cannot load configuration file: ", ex );
		}



		
	}

	public DashBoardConfig() {
		super();
	}

    public String getAppName() {
    	return m_configuration.getProperty(PROPERTIES_APP_NAME);
    }

    public String getAppVersion() {
    	return m_configuration.getProperty(PROPERTIES_APP_VERSION);
    }

    public String getAppBuild() {
    	return m_configuration.getProperty(PROPERTIES_APP_BUILD);
    }

    public String getUrl(String urlKey) {
    	String key = PROPERTIES_URL_ROOT_KEY+urlKey;
    	return m_configuration.getProperty(key,"http://localhost:8980/opennms/rest");
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
			return "jdbc:postgresql://localhost:5432/tnpd";
	}
	
	public String getDbUsername() {
		if (m_configuration.getProperty(PROPERTIES_DB_USER_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_USER_KEY);
		else 
			return "tnpd";
	}
	
	public String getDbPassword() {
		if (m_configuration.getProperty(PROPERTIES_DB_PASS_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_PASS_KEY);
		else 
			return "tnpd";
		
	}

	public String getKettleUrl() {
		if (m_configuration.getProperty(PROPERTIES_KETTLE_URL_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_KETTLE_URL_KEY);
		else 
			return "http://localhost:8080/kettle/";
	}
	
	public String getKettleUsername() {
		if (m_configuration.getProperty(PROPERTIES_KETTLE_USER_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_KETTLE_USER_KEY);
		else 
			return "admin";
	}
	
	public String getKettlePassword() {
		if (m_configuration.getProperty(PROPERTIES_KETTLE_PASS_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_KETTLE_PASS_KEY);
		else 
			return "admin";
	}

	public boolean isTabDisabled(String tabName, String username) {
		logger.info("Checking Authorization: Tab: " + tabName + " User: " + username);
		if (m_configuration.getProperty(tabName) == null) {
			logger.info("Null property, Authorization Denied: Tab: " + tabName + " User: " + username);
				return true;
		}
		if ("enabled".equals(m_configuration.getProperty(tabName))) {
			logger.info("enabled found, Authorization Granted: Tab: " + tabName + " User: " + username);
			return false;
		}
		if ("disabled".equals(m_configuration.getProperty(tabName))) {
			logger.info("disabled found, Authorization Denied: Tab: " + tabName + " User: " + username);
			return true;
		}
		for (String autorizeduser: m_configuration.getProperty(tabName).split(",")) {
			logger.info("checking user:" + autorizeduser + " Tab: " + tabName + " User: " + username);
			if (autorizeduser.equals(username)) {
				logger.info("user found, Authorization Granted: Tab: " + tabName + " User: " + username);
				return false;
			}
		}
		logger.info("no user found, Authorization Denied: Tab: " + tabName + " User: " + username);
		return true;
	}
}
