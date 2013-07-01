package org.opennms.vaadin.provision.dashboard;


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
@Title("Opennms Demo Provision Dashboard")
@Theme("runo")
public class ProvisiondashboardUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	protected final static String[] FOREIGN_SOURCE_LIST = new String[] {"Latency",
		"OpenNMS Servers","Minimal Detectors", "theBOX"};

	protected final static String[] URL_LIST = new String[] {
		"http://demo.opennms.org/opennms/rest"
	};
	
	DashBoardService m_service = new DashBoardService();
	/* User interface components are stored in session. */
//	private Table m_requisitionNodeList = new Table();
		
//    private JerseyClientImpl m_jerseyClient = new JerseyClientImpl(
//            "http://demo.opennms.org/opennms/rest/","demo","demo");

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	protected void init(VaadinRequest request) {
		setContent(new DashboardTabSheet(FOREIGN_SOURCE_LIST, URL_LIST,m_service));
	}

}
