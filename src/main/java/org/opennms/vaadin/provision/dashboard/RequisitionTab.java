package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsState;
import org.opennms.vaadin.provision.model.BasicNode.OnmsSync;
import org.opennms.vaadin.provision.model.SnmpProfile;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public abstract class RequisitionTab extends DashboardTab {

	public class RequisitionNodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7648836269899715294L;
		private String   needle="";

		public RequisitionNodeFilter(Object o) {
			if ( o != null)
				needle = (String) o;
		}
		
		@SuppressWarnings("unchecked")
		public boolean passesFilter(Object itemId, Item item) {
			BasicNode node = ((BeanItem<BasicNode>)item).getBean();
			if (node.getPrimary() == null && node.getNodeLabel() == null)
				return false;
			if (node.getNodeLabel() == null && node.getPrimary() != null)
				return node.getPrimary().contains(needle);			
			if (node.getPrimary() == null && node.getNodeLabel() != null)
				return node.getNodeLabel().contains(needle);			
			return ((node.getPrimary().contains(needle) 
					|| node.getNodeLabel().contains(needle)));
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}


	}

	/**
	 * 
	 */
	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private static final long serialVersionUID = 4694567853140078034L;
		
	private Button m_populateSnmpButton  = new Button("Sync Snmp Data");
	private Button m_syncRequisButton  = new Button("Sync");
	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	private Button m_replaceNodeButton  = new Button("Sostituisci Nodo");
	
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_deleteNodeButton  = new Button("Elimina Nodo");
	
	private Table m_requisitionTable   	= new Table();
	
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private ComboBox m_parentComboBox = new ComboBox("Dipende da");
	private TextField m_hostname = new TextField("Hostname");
	private TextField m_primary = new TextField(DashBoardUtils.PRIMARY);
	private ComboBox m_domainComboBox = new ComboBox("Dominio");

	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");

	private TextField m_city = new TextField("Citta'");
	private TextField m_address = new TextField("Indirizzo");
	private TextField m_building = new TextField("Edificio");

	private Map<String,Set<String>> m_foreignIdNodeLabelMap = new HashMap<String, Set<String>>();
	private Map<String,Set<String>> m_nodeLabelForeignIdMap = new HashMap<String, Set<String>>();
	private Map<String,Set<String>> m_primaryipforeignidmap = new HashMap<String, Set<String>>();
	protected Map<String,BasicNode> m_updates = new HashMap<String,BasicNode>();

	private boolean m_loaded = false;

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	RequisitionTab(LoginBox login, DashBoardSessionService service) {
		super(login,service);
		
    	m_populateSnmpButton.addClickListener(this);
    	m_populateSnmpButton.setImmediate(true);

    	m_syncRequisButton.addClickListener(this);
    	m_syncRequisButton.setImmediate(true);
    	
       	m_addNewNodeButton.addClickListener(this);
    	m_addNewNodeButton.setImmediate(true);
    	
		m_saveNodeButton.addClickListener(this);
		m_saveNodeButton.setImmediate(true);		
		m_saveNodeButton.setEnabled(false);

		m_deleteNodeButton.addClickListener(this);
		m_deleteNodeButton.setImmediate(true);		
		m_deleteNodeButton.setEnabled(false);				
		
		m_resetNodeButton.addClickListener(this);
		m_resetNodeButton.setImmediate(true);				
		m_resetNodeButton.setEnabled(false);

    	m_replaceNodeButton.addClickListener(this);
    	m_replaceNodeButton.setImmediate(true);
    	m_replaceNodeButton.setEnabled(false);
    	
		m_hostname.setSizeFull();
		m_hostname.setWidth(4, Unit.CM);
		m_hostname.setHeight(6, Unit.MM);
		m_hostname.setRequired(true);
		m_hostname.setRequiredError("hostname must be defined");
		m_hostname.addValidator(new DnsNodeLabelValidator());
		m_hostname.addValidator(new DuplicatedForeignIdValidator());
		m_hostname.addValidator(new DuplicatedNodelabelValidator());
		m_hostname.setImmediate(true);

		m_primary.setRequired(true);
		m_primary.setRequiredError("E' necessario specificare un indirizzo ip primario");
		m_primary.setImmediate(true);
		m_primary.addValidator(new IpValidator());
		m_primary.addValidator(new DuplicatedPrimaryValidator());

		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);

		m_parentComboBox.setInvalidAllowed(false);
		m_parentComboBox.setNullSelectionAllowed(true);
		
		m_domainComboBox.setInvalidAllowed(false);
		m_domainComboBox.setNullSelectionAllowed(false);
		m_domainComboBox.setRequired(true);
		m_domainComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_domainComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = -2209110948375990804L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
	        	logger.info("domain combo box value change:"+ m_domainComboBox.getValue());
		        try {
		            m_hostname.validate();
		            m_hostname.setComponentError(null); // MAGIC CODE HERE!!!
		        } catch (Exception e) {
		        	logger.info("not working validation");
		        }
		    }
		});
		m_domainComboBox.setImmediate(true);
		
		m_requisitionTable.setSizeFull();
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue() == null) {
					getRight().setVisible(false);
					return;
				}
				Object contactId = getRequisitionTable().getValue();
				if (contactId == null) 
					return;
				@SuppressWarnings("unchecked")
				BasicNode node = ((BeanItem<? extends BasicNode>)m_requisitionTable
					.getItem(contactId)).getBean();
				if (node.getForeignId() != null) {
					String snmpProfile=null;
					if (node.getPrimary() != null) {
						try {
							snmpProfile = getService().getSnmpProfileName(node.getPrimary());
							if (snmpProfile == null) {
								node.setValid(false);
							}
							node.setSnmpProfileWithOutUpdating(snmpProfile);
						} catch (UniformInterfaceException uie) {
							logger.warning("Errore nel richiesta del profilo snmp alla interfaccia rest: " + uie.getLocalizedMessage());
							Notification.show("Errore nel richiesta del profilo snmp alla interfaccia rest", uie.getMessage(), Type.WARNING_MESSAGE);							
						} catch (SQLException uie) {
							logger.warning("Errore nel richiesta del profilo snmp al database: " + uie.getLocalizedMessage());
							Notification.show("Errore nel richiesta del profilo snmp aldatabase", uie.getMessage(), Type.WARNING_MESSAGE);							
						}
						
						try {
							getService().saveSnmpProfile(node.getPrimary(), snmpProfile);
						} catch (SQLException sqle) {
							logger.warning("Errore nel salvare il profilo snmp al database: " + sqle.getLocalizedMessage());
							Notification.show("Errore nel nel salvare il profilo snmp al database", sqle.getMessage(), Type.WARNING_MESSAGE);
						}

					}
				}
				m_descrComboBox.removeAllItems();
				if (node.getDescr() != null)
					m_descrComboBox.addItem(node.getDescr());

				selectItem(node);
				getRight().setVisible(true);
				enableNodeButtons();
				if (node.getOnmstate() ==  OnmsState.NEW)
					m_replaceNodeButton.setEnabled(false);
				else
					m_replaceNodeButton.setEnabled(true);
			}
		});
		
		m_city.setWidth(8, Unit.CM);
		m_city.setHeight(6, Unit.MM);
		
		m_address.setWidth(8, Unit.CM);
		m_address.setHeight(6, Unit.MM);

		m_building.setWidth(8, Unit.CM);
		m_building.setHeight(6, Unit.MM);
		
		m_snmpComboBox.setInvalidAllowed(false);
		m_snmpComboBox.setNullSelectionAllowed(false);
		m_snmpComboBox.setRequired(true);
		m_snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");
		
		if (service.getUser().equals("admin"))
			getHead().addComponent(m_populateSnmpButton);
	    getHead().addComponent(m_syncRequisButton);
    	getHead().addComponent(m_addNewNodeButton);
		getHead().addComponent(m_deleteNodeButton);
		getHead().addComponent(m_saveNodeButton);
		getHead().addComponent(m_replaceNodeButton);
		getHead().addComponent(m_resetNodeButton);
		
		getHead().setComponentAlignment(m_syncRequisButton, Alignment.MIDDLE_CENTER);
		getHead().setComponentAlignment(m_addNewNodeButton,  Alignment.MIDDLE_CENTER);
		getHead().setComponentAlignment(m_deleteNodeButton, Alignment.MIDDLE_RIGHT);
		getHead().setComponentAlignment(m_saveNodeButton, Alignment.MIDDLE_RIGHT);
		getHead().setComponentAlignment(m_replaceNodeButton, Alignment.MIDDLE_RIGHT);
		getHead().setComponentAlignment(m_resetNodeButton,  Alignment.MIDDLE_RIGHT);

							


	}
		
	public void load() {
		updateTabHead();
		if (!m_loaded) {
			for (String itemId: getRequisitionContainer().getItemIds()) {
				BasicNode node = getRequisitionContainer().getItem(itemId).getBean();
				if (!m_foreignIdNodeLabelMap.containsKey(node.getForeignId()))
					m_foreignIdNodeLabelMap.put(node.getForeignId(), new HashSet<String>());
				m_foreignIdNodeLabelMap.get(node.getForeignId()).add(node.getNodeLabel());
				
				if (!m_nodeLabelForeignIdMap.containsKey(node.getNodeLabel()))
					m_nodeLabelForeignIdMap.put(node.getNodeLabel(), new HashSet<String>());
				m_nodeLabelForeignIdMap.get(node.getNodeLabel()).add(node.getForeignId());
				
				if (!m_primaryipforeignidmap.containsKey(node.getPrimary()))
					m_primaryipforeignidmap.put(node.getPrimary(), new HashSet<String>());
				m_primaryipforeignidmap.get(node.getPrimary()).add(node.getForeignId());
			}
			
			
			Set<String> duplicatednodeLabels = new HashSet<String>();
			for (String nodelabel: m_nodeLabelForeignIdMap.keySet()) {
				if (m_nodeLabelForeignIdMap.get(nodelabel).size() > 1)
					duplicatednodeLabels.add(nodelabel);
			}
			if (!duplicatednodeLabels.isEmpty()) {
				final Window duplicatednodelabelwindow = new Window(getRequisitionName() + ":Duplicated Node Label");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String duplicatedlabel: duplicatednodeLabels) {
					windowcontent.addComponent(new Label(duplicatedlabel));
				}
				duplicatednodelabelwindow.setContent(windowcontent);
				duplicatednodelabelwindow.setModal(false);
				duplicatednodelabelwindow.setWidth("400px");
		        UI.getCurrent().addWindow(duplicatednodelabelwindow);
				logger.warning(getRequisitionName() + ": Found Duplicated NodeLabel: " + Arrays.toString(duplicatednodeLabels.toArray()));
			}

			Set<String> duplicatedForeignIds= new HashSet<String>();
			for (String foreinId: m_foreignIdNodeLabelMap.keySet()) {
				if (m_foreignIdNodeLabelMap.get(foreinId).size() > 1)
					duplicatedForeignIds.add(foreinId);
			}
				
			if (!duplicatedForeignIds.isEmpty()) {
				final Window duplicatedIdwindow = new Window(getRequisitionName() + ":Duplicated Foreign Id");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String duplicatedIdp: duplicatedForeignIds) {
					windowcontent.addComponent(new Label(duplicatedIdp));
				}
				duplicatedIdwindow.setContent(windowcontent);
				duplicatedIdwindow.setModal(false);
				duplicatedIdwindow.setWidth("400px");
		        UI.getCurrent().addWindow(duplicatedIdwindow);
				logger.warning(getRequisitionName() + ": Found Duplicated ForeignId" +  Arrays.toString(duplicatedForeignIds.toArray()));
			}

			Set<String> duplicatedPrimaries = new HashSet<String>();
			for (String primary: m_primaryipforeignidmap.keySet()) {
				if (m_primaryipforeignidmap.get(primary).size() > 1)
					duplicatedPrimaries.add(primary);
			}

			if (!duplicatedPrimaries.isEmpty()) {
				final Window duplicatedipwindow = new Window(getRequisitionName() + ":Duplicated Primary Ip");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String duplicatedip: duplicatedPrimaries) {
					windowcontent.addComponent(new Label(duplicatedip));
				}
				duplicatedipwindow.setContent(windowcontent);
				duplicatedipwindow.setModal(false);
				duplicatedipwindow.setWidth("400px");
		        UI.getCurrent().addWindow(duplicatedipwindow);
				logger.warning(getRequisitionName() + ": Found Duplicated Primary IP: " + Arrays.toString(duplicatedPrimaries.toArray()));
			}
			Set<String> nulllbls = m_nodeLabelForeignIdMap.get(null);
			if (nulllbls != null && !nulllbls.isEmpty()) {
				final Window nulllblswindow = new Window(getRequisitionName() + ":Null Node Labels");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String nullprimid: nulllbls) {
					windowcontent.addComponent(new Label(nullprimid));
				}
				nulllblswindow.setContent(windowcontent);
				nulllblswindow.setModal(false);
				nulllblswindow.setWidth("400px");
		        UI.getCurrent().addWindow(nulllblswindow);
				logger.warning(getRequisitionName() + ": Found Null nodeLabels: " + Arrays.toString(nulllbls.toArray()));
			}

			
			Set<String> nullfids = m_foreignIdNodeLabelMap.get(null);
			if (nullfids != null && !nullfids.isEmpty()) {
				final Window nullidswindow = new Window(getRequisitionName() + ":Null Foreign Id");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String nullprimid: nullfids) {
					windowcontent.addComponent(new Label(nullprimid));
				}
				nullidswindow.setContent(windowcontent);
				nullidswindow.setModal(false);
				nullidswindow.setWidth("400px");
		        UI.getCurrent().addWindow(nullidswindow);
				logger.warning(getRequisitionName() + ": Found Null foreign Id: " + Arrays.toString(nullfids.toArray()));
			}

			Set<String> nullprims = m_primaryipforeignidmap.get(null);
			if (nullprims != null && !nullprims.isEmpty()) {
				final Window nullipwindow = new Window(getRequisitionName() + ":Null Primary");
				VerticalLayout windowcontent = new VerticalLayout();
				windowcontent.setMargin(true);
				windowcontent.setSpacing(true);
				for (String nullprimid: nullprims) {
					windowcontent.addComponent(new Label(nullprimid));
				}
				nullipwindow.setContent(windowcontent);
				nullipwindow.setModal(false);
				nullipwindow.setWidth("400px");
		        UI.getCurrent().addWindow(nullipwindow);
				logger.warning(getRequisitionName() + ": Found Null Primary IP: " + Arrays.toString(nullprims.toArray()));
			}

		}
		
		m_parentComboBox.removeAllItems();
		for (String nodelabel :m_nodeLabelForeignIdMap.keySet())
			m_parentComboBox.addItem(nodelabel);

		m_domainComboBox.removeAllItems();
		for (final String domain: getService().getDnsDomainContainer().getDomains()) {
			m_domainComboBox.addItem(domain);
		}

		m_snmpComboBox.removeAllItems();
		Map<String,SnmpProfile> snmpprofilemap = 
				getService().getSnmpProfileContainer().getSnmpProfileMap();
		List<String> snmpprofiles = new ArrayList<String>(snmpprofilemap.keySet());
		Collections.sort(snmpprofiles);
		for (String snmpprofile: snmpprofiles) {
			m_snmpComboBox.addItem(snmpprofile);
			m_snmpComboBox.setItemCaption(snmpprofile, 
					snmpprofile + 
					"(community:"+snmpprofilemap.get(snmpprofile).getCommunity()+")"
					+ "(version:"+ snmpprofilemap.get(snmpprofile).getVersion()+")");
		}


		m_loaded=true;
		
	}
	
	public Collection<BasicNode> getUpdates() {
		return m_updates.values();
	}

	public abstract void selectItem(BasicNode node);
	public abstract void cleanSearchBox();
	public abstract BeanContainer<String,? extends BasicNode> getRequisitionContainer();
	public abstract BasicNode addBean();
	public abstract BeanFieldGroup<? extends BasicNode> getBeanFieldGroup();
	public abstract String getRequisitionName();
		
	@Override
	public void buttonClick(ClickEvent event) {
		super.buttonClick(event);
		if (event.getButton() == m_syncRequisButton) {
	    	sync(getRequisitionName());
	    } else if (event.getButton() == m_addNewNodeButton) {
	    	newNode();
	    } else if (event.getButton() == m_saveNodeButton) {
	    	save();
	    } else if (event.getButton() == m_deleteNodeButton) {
	    	delete();
	    } else if (event.getButton() == m_replaceNodeButton) {
	    	replace();
	    } else if (event.getButton() == m_resetNodeButton) {
	    	reset();
	    } else if (event.getButton() == m_populateSnmpButton) {
	    	populateSnmp();
		}
	}
	
	public void enableNodeButtons() {
		m_saveNodeButton.setEnabled(true);
		m_deleteNodeButton.setEnabled(true);
		m_replaceNodeButton.setEnabled(true);
		m_resetNodeButton.setEnabled(true);
	}

	public void disableNodeButtons() {
		m_saveNodeButton.setEnabled(false);
		m_deleteNodeButton.setEnabled(false);
		m_replaceNodeButton.setEnabled(false);
		m_resetNodeButton.setEnabled(false);
	}
	
	public void populateSnmp() {
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
		logger.info("Sync db with snmp profiles for Requisition: " + getRequisitionName());
		try {
			BeanContainer<String, ? extends BasicNode> container = getRequisitionContainer();
			Set<String> primaries = new HashSet<String>();
			for (String itemid: container.getItemIds()) 
				primaries.add(container.getItem(itemid).getBean().getPrimary());
			
			getService().syncSnmpProfile(primaries);
			Notification.show("Sync Snmp profile: " + getRequisitionName(), " Done ", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync Snmp profile Failed: " + getRequisitionName() + " " + e.getLocalizedMessage());
			Notification.show("Sync Snmp profile Failed: " + getRequisitionName(), e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}
			}
		});

	}
	
	public void replace() {
		disableNodeButtons();
		BasicNode node = null;
		try {
			getBeanFieldGroup().commit();
			node = getBeanFieldGroup().getItemDataSource().getBean();
		} catch (CommitException e) {
			logger.warning("Replaced Failed: " + e.getMessage());
			Notification.show("Replaced Failed", e.getMessage(), Type.ERROR_MESSAGE);
			return;
		}
		if (node == null ) {
			logger.warning("Replace failed. Cannot replace null node");
			Notification.show("Replace", " Failed, cannot replace null node", Type.ERROR_MESSAGE);
			return;
		} 

		if (node.getForeignId() == null) {
			logger.warning("Replace failed: " + node.getNodeLabel() + ". Cannot replace new node");
			Notification.show("Replace", "Node " +node.getNodeLabel() + " Failed, cannot replace new node", Type.ERROR_MESSAGE);
			return;
		} 

		try {
			getService().delete(node);
			logger.info("Replace:delete done: " + node.getNodeLabel());
		} catch (Exception e) {
			String localizedMessage = e.getLocalizedMessage();
			Throwable t = e.getCause();
			while ( t != null) {
				if (t.getLocalizedMessage() != null)
					localizedMessage+= ": " + t.getLocalizedMessage();
				t = t.getCause();
			}
			logger.warning("Replace: delete Failed: " + localizedMessage);
			Notification.show("Replace: delete Failed", localizedMessage, Type.ERROR_MESSAGE);
			return;
		}
		
		node.setDeleteState();
		m_updates.put(node.getNodeLabel(), node);



		Set<String> nodelabels = m_foreignIdNodeLabelMap.remove(node.getForeignId());
		nodelabels.remove(node.getNodeLabel());
		if (!nodelabels.isEmpty())
			m_foreignIdNodeLabelMap.put(node.getForeignId(), nodelabels);
		
		Set<String> foreignids = m_nodeLabelForeignIdMap.remove(node.getNodeLabel());
		foreignids.remove(node.getForeignId());
		if (!foreignids.isEmpty())
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(), foreignids);
		Set<String> primaries = m_primaryipforeignidmap.remove(node.getPrimary());
		primaries.remove(node.getForeignId());
		if(!primaries.isEmpty())
			m_primaryipforeignidmap.put(node.getPrimary(), primaries);

		m_parentComboBox.removeAllItems();
		for (String nodelabel :m_nodeLabelForeignIdMap.keySet())
			m_parentComboBox.addItem(nodelabel);

		if (node.getParent() != null)
			node.setParentId(m_nodeLabelForeignIdMap.get(node.getParent()).iterator().next());
		try {
			node.setForeignId(node.getForeignId()+"rR");
			getService().add(node);
			node.setValid(getService().isValid(node));
			m_foreignIdNodeLabelMap.put(node.getForeignId(), new HashSet<String>());
			m_foreignIdNodeLabelMap.get(node.getForeignId()).add(node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(), new HashSet<String>());
			m_nodeLabelForeignIdMap.get(node.getNodeLabel()).add(node.getForeignId());
			m_primaryipforeignidmap.put(node.getPrimary(), new HashSet<String>());
			m_primaryipforeignidmap.get(node.getPrimary()).add(node.getForeignId());

			logger.info("Replace:new done: " + node.getNodeLabel());
			Notification.show("Replace", "Node " +node.getNodeLabel() + " Done", Type.HUMANIZED_MESSAGE);
			applyFilter(node.getHostname());
			getRequisitionContainer().removeAllContainerFilters();
		} catch (Exception e) {
			String localizedMessage = e.getLocalizedMessage();
			Throwable t = e.getCause();
			while ( t != null) {
				if (t.getLocalizedMessage() != null)
					localizedMessage+= ": " + t.getLocalizedMessage();
				t = t.getCause();
			}
			logger.warning("Replaced:new Failed: " + localizedMessage);
			Notification.show("Replaced:new Failed", localizedMessage, Type.ERROR_MESSAGE);
		}
		m_parentComboBox.removeAllItems();
		for (String nodelabel :m_nodeLabelForeignIdMap.keySet())
			m_parentComboBox.addItem(nodelabel);

		enableNodeButtons();

		m_requisitionTable.unselect(m_requisitionTable.getValue());

	}
	
	public void sync(String requisitionName) {
		SyncWindow subWindow = new SyncWindow(this,requisitionName);
        subWindow.center();
        subWindow.setWidth("600px");
        subWindow.setCaption("Sincronizzazione dei nodi " + requisitionName);
        UI.getCurrent().addWindow(subWindow);
	}

	public void  newNode() {
		cleanSearchBox();
		m_replaceNodeButton.setEnabled(false);
		BasicNode bean = addBean();
		m_requisitionTable.select(bean.getNodeLabel());
		m_descrComboBox.removeAllItems();
		if (bean.getDescr() != null)
			m_descrComboBox.addItem(bean.getDescr());
		selectItem(bean);
		getRequisitionContainer().removeAllContainerFilters();
		m_hostname.focus();
	}

	public void save() {
		try {
			getBeanFieldGroup().commit();
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getParent() != null)
				node.setParentId(m_nodeLabelForeignIdMap.get(node.getParent()).iterator().next());
			if (node.getForeignId() == null) {
				node.setForeignId(node.getHostname());
				node.setValid(true);
				getService().add(node);
				logger.info("Added: " + node.getNodeLabel()+ " Valid: " + node.isValid());
				Notification.show("Save", "Node " +node.getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
			} else {
				node.setValid(getService().isValid(node));
				getService().update(node);
				logger.info("Updated: " + node.getNodeLabel() + " Valid: " + node.isValid());
				Notification.show("Save", "Node " +node.getNodeLabel() + " Updated", Type.HUMANIZED_MESSAGE);
			}
			//FIXME
			node.setNoneState();

			m_updates.put(node.getNodeLabel(), node );		
			m_foreignIdNodeLabelMap.put(node.getForeignId(), new HashSet<String>());
			m_foreignIdNodeLabelMap.get(node.getForeignId()).add(node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(), new HashSet<String>());
			m_nodeLabelForeignIdMap.get(node.getNodeLabel()).add(node.getForeignId());
			m_primaryipforeignidmap.put(node.getPrimary(), new HashSet<String>());
			m_primaryipforeignidmap.get(node.getPrimary()).add(node.getForeignId());
			applyFilter(node.getHostname());
			cleanSearchBox();
			getRequisitionContainer().removeAllContainerFilters();
		} catch (Exception e) {
			String localizedMessage = e.getLocalizedMessage();
			Throwable t = e.getCause();
			while ( t != null) {
				if (t.getLocalizedMessage() != null)
					localizedMessage+= ": " + t.getLocalizedMessage();
				t = t.getCause();
			}
			m_parentComboBox.removeAllItems();
			for (String nodelabel :m_nodeLabelForeignIdMap.keySet())
				m_parentComboBox.addItem(nodelabel);

			logger.warning("Save Failed: " + localizedMessage);
			Notification.show("Save Failed", localizedMessage, Type.ERROR_MESSAGE);
		}
		m_requisitionTable.unselect(m_requisitionTable.getValue());
	}

	public void delete() {
		getRight().setVisible(false);
		m_saveNodeButton.setEnabled(false);
		m_deleteNodeButton.setEnabled(false);
		m_replaceNodeButton.setEnabled(false);
		m_resetNodeButton.setEnabled(false);
		BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
		logger.info("Deleting: " + node.getNodeLabel());
		if (node.getForeignId() !=  null) {
			try {
				getService().delete(node);
				Notification.show("Delete Node From Requisition", "Done", Type.HUMANIZED_MESSAGE);
			} catch (UniformInterfaceException e) {
				logger.warning(e.getLocalizedMessage()+" Reason: " + e.getResponse().getStatusInfo().getReasonPhrase());
				Notification.show("Delete Node From Requisition", "Failed: "+e.getLocalizedMessage()+ " Reason: " +
				e.getResponse().getStatusInfo().getReasonPhrase(), Type.ERROR_MESSAGE);
				return;
			}
		}

		if (node.getOnmstate() == BasicNode.OnmsState.NEW) {
			m_updates.remove(node.getNodeLabel());
		} else {
			node.setDeleteState();
			m_updates.put(node.getNodeLabel(),node);			
		}
		/* FIXME MediaGateway
		m_updates.put(DashBoardUtils.SIVN_REQU_NAME, new HashMap<String, BasicNode>());
		BasicNode mg = new BasicNode(mediagateway.getNodeLabel());
		mg.setUpdateState();
		mg.setOnmsSyncOperations(OnmsSync.FALSE);
		m_updates.get(DashBoardUtils.SIVN_REQU_NAME).put(mediagateway.getNodeLabel(),mg);
		 */

		if ( ! getRequisitionContainer().removeItem(node.getNodeLabel()))
			getRequisitionContainer().removeItem(getRequisitionContainer().getIdByIndex(0));
		
		Set<String> nodelabels = m_foreignIdNodeLabelMap.remove(node.getForeignId());
		nodelabels.remove(node.getNodeLabel());
		
		if (!nodelabels.isEmpty())
			m_foreignIdNodeLabelMap.put(node.getForeignId(), nodelabels);
		
		Set<String> foreignids = m_nodeLabelForeignIdMap.remove(node.getNodeLabel());
		foreignids.remove(node.getForeignId());
		if (!foreignids.isEmpty())
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(), foreignids);
		Set<String> primaries = m_primaryipforeignidmap.remove(node.getPrimary());
		primaries.remove(node.getForeignId());
		if(!primaries.isEmpty())
			m_primaryipforeignidmap.put(node.getPrimary(), primaries);

		m_parentComboBox.removeAllItems();
		for (String nodelabel :m_nodeLabelForeignIdMap.keySet())
			m_parentComboBox.addItem(nodelabel);

		logger.info("Node Deleted");
		Notification.show("Delete", "Done", Type.HUMANIZED_MESSAGE);
	}
	
	public void reset() {
			getBeanFieldGroup().discard();
			getRight().setVisible(false);
			m_requisitionTable.unselect(m_requisitionTable.getValue());
			m_saveNodeButton.setEnabled(false);
			m_deleteNodeButton.setEnabled(false);
			m_replaceNodeButton.setEnabled(false);
			m_resetNodeButton.setEnabled(false);
		}

	class DuplicatedForeignIdValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = (String)value;
			
			logger.info("DuplicatedForeignIdValidator: validating foreignId: " + hostname);
	         if (m_foreignIdNodeLabelMap.containsKey(hostname))
	             throw new InvalidValueException("The hostname exists: cannot duplicate hostname: " + hostname);
	       }
	}
	
	class DuplicatedPrimaryValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String ip = (String)value;
			logger.info("DuplicatedPrimaryValidator: validating ip: " + ip);
	         if (m_primaryipforeignidmap.containsKey(ip))
	             throw new InvalidValueException("DuplicatedPrimaryValidator: trovato un duplicato del primary ip: " + ip);
	       }
	}

	class IpValidator implements Validator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6243101356369643407L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String ip = (String)value;
			logger.info("IpValidator: validating ip: " + ip);
	         if (DashBoardUtils.hasInvalidIp((ip)))
	             throw new InvalidValueException("Deve essere inserito un valido indirizzo ip (0.0.0.0 e 127.0.0.0 non sono validi): indirizzo sbagliato:" + ip);
	       }
	}
	
	class DuplicatedNodelabelValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
			logger.info("DuplicatedNodelabelValidator: validating label: " + nodelabel);
	        if (m_nodeLabelForeignIdMap.containsKey(nodelabel))
	             throw new InvalidValueException("DuplicatedNodelabelValidator: trovato un duplicato della node label: " + nodelabel);
	       }
	}

	class DnsNodeLabelValidator implements Validator {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1802798646587971819L;
		@Override
		public void validate( Object value) throws InvalidValueException {
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
			logger.info("DnsNodeLabelValidator: validating nodelabel: " + nodelabel);
	         if (DashBoardUtils.hasInvalidDnsBind9Label(nodelabel))
	             throw new InvalidValueException("DnsNodeLabelValidator: Dns name: '" + nodelabel + "' is not well formed. The definitive descriptions of the rules for forming domain names appear in RFC 1035, RFC 1123, and RFC 2181." +
               " A domain name consists of one or more parts, technically called labels, that are conventionally concatenated, and delimited by dots, such asexample.com."
                               + " Each label may contain up to 63 characters." +
                               " The full domain name may not exceed a total length of 253 characters in its external dotted-label specification." +
                               " The characters allowed in a label are a subset of the ASCII character set, and includes the characters a through z, A through Z, digits 0 through 9, the hyphen." +
                               " This rule is known as the LDH rule (letters, digits, hyphen). " +
                               " Labels may not start or end with a hyphen.");
		}
	}
	
	public Table getRequisitionTable() {
		return m_requisitionTable;
	}
	
	public TextField getHostNameTextField() {
		return m_hostname;
	}
	
	public TextField getPrimaryTextField() {
		return m_primary;
	}
	
	public ComboBox getDomainComboBox() {
		return m_domainComboBox;
	}

	public TextField getCityTextField() {
		return m_city;
	}

	public TextField getAddressTextField() {
		return m_address;
	}

	public TextField getBuildingTextField() {
		return m_building;
	}

	public ComboBox getSnmpComboBox() {
		return m_snmpComboBox;
	}

	public ComboBox getDescrComboBox() {
		return m_descrComboBox;
	}

	public ComboBox getParentComboBox() {
		return m_parentComboBox;
	}
	
	public void synctrue(String requisition) {
		try {
			getService().synctrue(requisition);
	    	clearUpdateMap(requisition,OnmsSync.TRUE);
			logger.info("Sync succeed foreign source: " +getRequisitionName());
			Notification.show("Sync " + getRequisitionName(), "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync Failed foreign source: " +getRequisitionName() + " " + e.getLocalizedMessage());
			Notification.show("Sync Failed foreign source" + getRequisitionName(), e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}
	}

	public void syncfalse(String requisition) {
		try {
			getService().syncfalse(requisition);
	    	clearUpdateMap(requisition,OnmsSync.FALSE);
			logger.info("Sync rescanExisting=false succeed foreign source: " +getRequisitionName());
			Notification.show("Sync rescanExisting=false " + getRequisitionName(), "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync rescanExisting=false Failed foreign source: " +getRequisitionName() + " " + e.getLocalizedMessage());
			Notification.show("Sync rescanExisting=false Failed foreign source" + getRequisitionName(), e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}				
	}

	public void syncdbonly(String requisition) {
		try {
			getService().syncdbonly(requisition);
	    	clearUpdateMap(requisition,OnmsSync.DBONLY);
			logger.info("Sync rescanExisting=dbonly succeed foreign source: " +getRequisitionName());
			Notification.show("Sync rescanExisting=dbonly " + getRequisitionName(), "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync rescanExisting=dbonly Failed foreign source: " +getRequisitionName() + " " + e.getLocalizedMessage());
			Notification.show("Sync rescanExisting=dbonly Failed foreign source" + getRequisitionName(), e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}						
	}
	
	public void applyFilter(String hostname) {
		getRequisitionContainer().addContainerFilter(
				new RequisitionNodeFilter(hostname));

	}
	
	public void clearUpdateMap(String requisition,BasicNode.OnmsSync sync) {
		Map<String, BasicNode> after = new HashMap<String, BasicNode>();		
		switch (sync) {
			case TRUE:
				for (BasicNode node: m_updates.values()) {
					if (!requisition.equals(node.getForeignSource()))
						continue;
					node.setNoneState();
				}
				break;
			case FALSE:
				for (BasicNode node: m_updates.values()) {
					if (!requisition.equals(node.getForeignSource()))
						continue;
					node.deleteOnmsSyncOperation(OnmsSync.FALSE);
					if (node.getSyncOperations().isEmpty())
						node.setNoneState();
					else
						after.put(node.getNodeLabel(), node);
				}
				break;
			case DBONLY:
				for (BasicNode node: m_updates.values()) {
					if (!requisition.equals(node.getForeignSource()))
						continue;
					node.deleteOnmsSyncOperation(OnmsSync.DBONLY);
					if (node.getSyncOperations().isEmpty())
						node.setNoneState();
					else
						after.put(node.getNodeLabel(), node);
				}
				break;
		}
		m_updates = after;

	}



}
