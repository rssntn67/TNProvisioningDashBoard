package org.opennms.vaadin.provision.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class DashboardTabSheet extends CustomComponent implements SelectedTabChangeListener {

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -4835992723502900986L;
	private TabSheet m_tabsheet;
    private LoginBox m_loginbox;
	private DashBoardSessionService m_service;

    
    DashboardTabSheet(DashBoardSessionService service) {
    	m_service = service;
    	m_tabsheet = new TabSheet();
		m_tabsheet.addSelectedTabChangeListener(this);
		setCompositionRoot(m_tabsheet);
    	
		m_loginbox = new LoginBox(service);
		m_loginbox.setParent(this);
        m_tabsheet.addTab(m_loginbox, "Login Box", new ThemeResource("icons/16/user.png"));
    	
        TrentinoNetworkTab tab=new TrentinoNetworkTab(service);
    	tab.setParent(this);
    	m_tabsheet.addTab(tab, "Rete TN", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(tab).setEnabled(false);

        MediaGatewayTab mtab=new MediaGatewayTab(service);
    	mtab.setParent(this);
    	m_tabsheet.addTab(mtab, "Media Gw", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(mtab).setEnabled(false);

    	SistemiInformativiTab stab=new SistemiInformativiTab(service);
    	m_tabsheet.addTab(stab, "SI", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(stab).setEnabled(false);

        CategorieTab catTab=new CategorieTab(service);
    	catTab.setParent(this);
    	m_tabsheet.addTab(catTab, "Categorie", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(catTab).setEnabled(false);
    	
    	SnmpProfileTab snmpTab = new SnmpProfileTab(service);
    	snmpTab.setParent(this);
        m_tabsheet.addTab(snmpTab, "Profili Snmp",new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(snmpTab).setEnabled(false);
    	
    	BackupProfileTab backupTab = new BackupProfileTab(service);
    	backupTab.setParent(this);
        m_tabsheet.addTab(backupTab, "Profili Backup",new ThemeResource("icons/16/users.png"));
        m_tabsheet.getTab(backupTab).setEnabled(false);
        
        FastTab fastTab= new FastTab(service);
        fastTab.setParent(this);
        m_tabsheet.addTab(fastTab, "Fast",new ThemeResource("icons/16/users.png"));
        m_tabsheet.getTab(fastTab).setEnabled(false);
    	
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == m_tabsheet) {
				((DashboardTab)source.getSelectedTab()).load();
		}
	}

	public void login(String url, String username, String password) throws ClientHandlerException,UniformInterfaceException, Exception  {
		m_service.login(url,username,password);
	    Iterator<Component> ite = m_tabsheet.iterator();
	    while (ite.hasNext()) {
	    	try {
	    		DashboardTab dashboardTab = (DashboardTab) ite.next();
	    		if (m_service.getConfig().isTabDisabled(dashboardTab.getName(),username))
	    			continue;
	    		m_tabsheet.getTab(dashboardTab).setEnabled(true);
	    	} catch (Exception e) {
	    		logger.log(Level.WARNING,"Not a DashboardTab", e);
	    	}
	    }
	    m_loginbox.setEnabled(true);
	}
	
	public void onLogout() {
		if (m_service.isFastRunning()) {
			Notification.show("Cannot Logged Out", "Fast Sync is Running", Notification.Type.WARNING_MESSAGE);
			return;
		}
		Map<String,Collection<BasicNode>> updatemap = new HashMap<String,Collection<BasicNode>>();
		Iterator<Component> ite = m_tabsheet.iterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp instanceof RequisitionTab) {
	    		RequisitionTab tab = (RequisitionTab) comp;
	    		if (!tab.getUpdates().isEmpty())
	    			updatemap.put(tab.getRequisitionName(),tab.getUpdates());
	    	}
    	}

		if (!updatemap.isEmpty()) {
			createdialogwindown(updatemap);
			return;
		}
		reallylogout();	
	}
	
	public void reallylogout() {
		m_service.logout();
		Iterator<Component> ite = m_tabsheet.iterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp != m_loginbox) {
	    		m_tabsheet.getTab(comp).setEnabled(false);
	    	} else {
	    		m_tabsheet.setSelectedTab(comp);
	    	}
	    }
		m_loginbox.logout();
	    getUI().getSession().close();
	}

	
	private void createdialogwindown(Map<String,Collection<BasicNode>> updatemap) {
		final Window confirm = new Window("Modifiche effettuate e non sincronizzate");
		Button si = new Button("si");
		si.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				reallylogout();
				confirm.close();
			}
		});
		
		Button no = new Button("no");
		no.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				confirm.close();
			}
		});
				
		VerticalLayout windowcontent = new VerticalLayout();
		windowcontent.setMargin(true);
		windowcontent.setSpacing(true);
    
		for (String requisition: updatemap.keySet()) {
			Table updatetable = new Table("Operazioni di Sync sospese per Requisition: " + requisition);
			updatetable.setSelectable(false);
			updatetable.setContainerDataSource(DashBoardUtils.getUpdateContainer(updatemap.get(requisition)));
			updatetable.setSizeFull();
			updatetable.setPageLength(3);
			windowcontent.addComponent(updatetable);
		}

		windowcontent.addComponent(new Label("Alcune Modifiche che sono state effettuate "
				+ "alle Requisition richiedono le "
				+ "operazioni di sync sopra elencate. Confermi il logout?"));
		HorizontalLayout buttonbar = new HorizontalLayout();
		buttonbar.setMargin(true);
		buttonbar.setSpacing(true);
    	buttonbar.addComponent(si);
		buttonbar.addComponent(no);
		windowcontent.addComponent(buttonbar);
	    confirm.setContent(windowcontent);
        confirm.setModal(true);
        confirm.setWidth("600px");
        UI.getCurrent().addWindow(confirm);
		
	}

	public void onInfo() {
		final Window infowindow = new Window("Informazioni TNPD");
		VerticalLayout windowcontent = new VerticalLayout();
		windowcontent.setMargin(true);
		windowcontent.setSpacing(true);
		windowcontent.addComponent(new Label(m_service.getConfig().getAppName()));
		windowcontent.addComponent(new Label("Versione: " + m_service.getConfig().getAppVersion()));
		windowcontent.addComponent(new Label("Build: " + m_service.getConfig().getAppBuild()));
		infowindow.setContent(windowcontent);
		infowindow.setModal(true);
		infowindow.setWidth("400px");
        UI.getCurrent().addWindow(infowindow);
	}
}
