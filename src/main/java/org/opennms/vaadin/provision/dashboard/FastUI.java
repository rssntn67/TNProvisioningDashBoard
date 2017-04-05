package org.opennms.vaadin.provision.dashboard;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.vaadin.provision.config.DashBoardConfig;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;

public class FastUI extends HttpServlet {

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -7128455812303263101L;
	DashBoardConfig m_config;
	JDBCConnectionPool m_pool;
	public void init() throws ServletException {
		m_config = new DashBoardConfig();
		m_config.reload();
		
		try {
			m_pool = (new SimpleJDBCConnectionPool(
					"org.postgresql.Driver", m_config.getDbUrl(), m_config
							.getDbUsername(), m_config.getDbPassword()));
			logger.info("created connection to database: " + m_config.getDbUrl());
		} catch (SQLException e) {
			logger.log(Level.SEVERE,
					"cannot create collection", e);
			throw new ServletException(e);
		}
		
	}
	
	public void destroy()
	  {
		m_pool.destroy();
	  }
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,IOException
	{
		String name = request.getParameter("name");
		if (name == null) {
			name = "main";
		}
		logger.info("name: " +  name);
		String username = request.getParameter("username");
		if (username == null) {
			username = "admin";
		}
		logger.info("username: " +  username);
		
		String password = request.getParameter("password");
		if (password == null) {
			password = "admin";
		}
		logger.info("password: " +  password);
		
		response.setContentType("text/html");
		
		DashBoardSessionService sessionservice = new DashBoardSessionService(null);
		sessionservice.setConfig(m_config);
		sessionservice.setPool(m_pool);
		PrintWriter out = response.getWriter();
		try {
			sessionservice.init();
		} catch (SQLException e) {
			out.println("KO: cannot connect to database");
			return;
		}
		String url = sessionservice.getConfig().getUrl(name);
		logger.info("url: " +  url);
		
       FastTab fastTab= new FastTab();
       fastTab.setSessionService(sessionservice);
	   fastTab.load();
	   try {
			sessionservice.login(url, username, password);
		} catch (Exception e) {
			out.println("KO: cannot connect to opennms rest interface");
			return;
		}
		if (!sessionservice.isFastRunning()) {
			if (!fastTab.runFast()) {
				out.println("KO: FAST integration error");
				return;
			}
		} else {
			out.println("KO: FAST is running");
			return;			
		}
		out.println("OK: FAST integration started");

	}

}
