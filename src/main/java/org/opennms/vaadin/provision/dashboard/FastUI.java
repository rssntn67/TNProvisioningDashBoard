package org.opennms.vaadin.provision.dashboard;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.model.JobLogEntry;

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
	FastUIRunnable m_runnable;
	
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
		String action = request.getParameter("action"); 
		if (action == null || !action.equals("run")) {
			action="status";
		}
		logger.info("action: " +  action);
		
		String name = request.getParameter("name");
		if (name == null) {
			name = "main";
		}
		logger.info("name: " +  name);
		String url = m_config.getUrl(name);
		logger.info("url: " +  url);

		String username = request.getParameter("username");
		if (username == null) {
			username = "admin";
		}
		logger.info("username: " +  username);
		
		String password = request.getParameter("password");
		if (password == null) {
			password = "admin";
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (action.equals("status")) {
			if (m_runnable != null) {
				if (m_runnable.running()) {
					out.println("OK: Job Status: Running");
					return;			
				}
				
				if (m_runnable.failed()) {
					out.println("OK: Job Status: Failed");
					return;			
				}
				
				if (m_runnable.success()) {
					out.println("OK: Job Status: Success");
					return;			
				}
			} else {
				out.println("OK: Job Status: Session Expired");
				return;			
			}
		}

		if ( m_runnable == null ) {
			DashBoardSessionService sessionservice = new DashBoardSessionService(null);
			sessionservice.setConfig(m_config);
			sessionservice.setPool(m_pool);
			try {
				sessionservice.init();
				logger.info("session service inited");
			} catch (SQLException e) {
				logger.log(Level.SEVERE,"cannot init session service", e);
				out.println("KO: cannot connect to database");
				return;
			}
					
	 	   try {
				sessionservice.login(url, username, password);
			} catch (Exception e) {
				logger.log(Level.SEVERE,"cannot login", e);
				out.println("KO: cannot connect to opennms rest interface");
				return;
			}
			
	 	   if (sessionservice.isFastRunning()) {
				out.println("KO: FAST integration is already running");
				return;			
			}
	        m_runnable = new FastUIRunnable(sessionservice);
		} else {
			if (m_runnable.running()) {
				out.println("KO: FAST integration is already running");
				return;			
			}
		}
		
        m_runnable.syncRequisition();
        Thread thread = new Thread(m_runnable);
        thread.start();

		out.println("OK: FAST integration started");

	}
			
	private class FastUIRunnable extends FastRunnable {

		public FastUIRunnable(DashBoardSessionService session) {
			super(session);
		}

		@Override
		public void updateProgress(Float progress) {
			
		}

		@Override
		public void log(List<JobLogEntry> logs) {
			for (JobLogEntry log: logs)
				log(log);
		}

		@Override
		public void beforeStartJob() {			
		}

		@Override
		public void afterEndJob() {
		}
		
	}


}
