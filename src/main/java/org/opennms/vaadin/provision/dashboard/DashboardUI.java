package org.opennms.vaadin.provision.dashboard;


import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Trentino Network Opennms Provision Dashboard")
@Theme("runo")
public class DashboardUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	protected void init(VaadinRequest request) {
		DashBoardConfig config = new DashBoardConfig();
		TNDao tnDao= new TNDao();
		OnmsDao onmsDao = new OnmsDao();
		
	    DashBoardService service = new DashBoardService();
	    service.setConfig(config);
	    service.setTnDao(tnDao);
	    service.setOnmsDao(onmsDao);
		
	    setContent(new DashboardTabSheet(service));	    
	}

}
