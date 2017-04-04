package org.opennms.vaadin.provision.dashboard;


import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;

@Theme("runo")
public class FastUI extends DashboardAbstractUI {

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
		
		String username = request.getParameter("username");
		if (username == null) {
			username = "admin";
		}
		
		String password = request.getParameter("password");
		if (password == null) {
			password = "admin";
		}

		super.init(request);
		
       FastTab fastTab= new FastTab();
	   fastTab.load();
	   String url = getSessionService().getConfig().getUrl(name);
	   getSessionService().login(url, username, password);
	   fastTab.runFast();

	   setContent(new Label("OK"));
	}

}
