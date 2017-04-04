package org.opennms.vaadin.provision.dashboard;


import java.util.logging.Logger;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;

@Theme("runo")
public class FastUI extends DashboardAbstractUI {

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -7128455812303263101L;

	@Override
	protected void init(VaadinRequest request) {
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

		super.init(request);
		
       FastTab fastTab= new FastTab();
	   fastTab.load();
	   String url = getSessionService().getConfig().getUrl(name);
		logger.info("url: " +  password);
		try {
			getSessionService().login(url, username, password);
		} catch (Exception e) {
			setContent(new Label("KO"));
			getUI().getSession().close();
			return;
		}
		if (!getSessionService().isFastRunning()) {
			fastTab.runFast();
		} else {
			setContent(new Label("KO"));
			getUI().getSession().close();
			return;			
		}
		setContent(new Label("OK"));

	}

}
