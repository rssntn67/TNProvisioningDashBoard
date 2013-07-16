package org.opennms.vaadin.provision.dashboard;


import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_snmp_profiles;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_backup_profiles;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_vrfs;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_network_categories;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_notif_categories;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.m_thresh_categories;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.DESCR;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.HOST;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.VRF;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.PRIMARY;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.PARENT;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.LABEL;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.NETWORK_CATEGORY;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.NOTIF_CATEGORY;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.THRESH_CATEGORY;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.SNMP_PROFILE;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.BACKUP_PROFILE;

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.CITY;
import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.ADDRESS;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TrentinoNetwork Provision Dashboard")
@Theme("runo")
public class TrentinoNetworkTab extends DashboardTab {

	private static final long serialVersionUID = -5948892618258879832L;

	
	private String m_searchText = null;
	private BeanContainer<String, TrentinoNetworkRequisitionNode> m_requisitionContainer = new BeanContainer<String, TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
	private boolean loaded=false;

	private ComboBox m_networkCatSearchComboBox = new ComboBox("Select Network Category");
	private ComboBox m_notifCatSearchComboBox   = new ComboBox("Select Notification Category");
	private ComboBox m_threshCatSearchComboBox  = new ComboBox("Select Threshold Category");
	private TextField m_searchField       = new TextField("Type Label Text");
	private Table m_requisitionTable   	= new Table();
	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	private Button m_updateNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");

	private ComboBox m_vrfsComboBox = new ComboBox("Dominio");
	private ComboBox m_parentComboBox = new ComboBox("Dipende da");
	private ComboBox m_networkCatComboBox = new ComboBox("Network Category");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Category");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Category");
	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");

	private Table m_secondaryTable = new Table();

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();
	
	private BeanFieldGroup<TrentinoNetworkRequisitionNode> m_editorFields     = new BeanFieldGroup<TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
	
	
	public TrentinoNetworkTab(String foreignsource, DashboardService service) {
		super(foreignsource, service);
	}

	public void load() {
		if (loaded)
			return;
		initLayout();
		initProvisionNodeList();
		initEditor();
		initSearch();
		initActionButtons();
		loaded=true;
	}

	private void initLayout() {

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setCompositionRoot(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(m_networkCatSearchComboBox);
		searchlayout.addComponent(m_notifCatSearchComboBox);
		searchlayout.addComponent(m_threshCatSearchComboBox);

		m_searchField.setWidth("80%");
		searchlayout.addComponent(m_searchField);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);
		
		leftLayout.addComponent(new Panel("Search",searchlayout));

		m_requisitionTable.setSizeFull();
		leftLayout.addComponent(m_requisitionTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));
		leftLayout.addComponent(bottomLeftLayout);
		leftLayout.setSizeFull();


		HorizontalLayout topRightLayout = new HorizontalLayout();
		topRightLayout.addComponent(m_addNewNodeButton);
		rightLayout.addComponent(new Panel(topRightLayout));

		rightLayout.addComponent(m_editRequisitionNodeLayout);

		HorizontalLayout bottomRightLayout = new HorizontalLayout();
		bottomRightLayout.addComponent(m_removeNodeButton);
		bottomRightLayout.addComponent(m_updateNodeButton);
		bottomRightLayout.addComponent(m_resetNodeButton);
		bottomRightLayout.setComponentAlignment(m_removeNodeButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_updateNodeButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_resetNodeButton,  Alignment.MIDDLE_RIGHT);
		rightLayout.addComponent(new Panel(bottomRightLayout));
		
	}
	
	private void initSearch() {

		for (String[] categories: m_network_categories) {
			m_networkCatSearchComboBox.addItem(categories);
			m_networkCatSearchComboBox.setItemCaption(categories, categories[1]+" - " + categories[0]);
		}
		m_networkCatSearchComboBox.setInvalidAllowed(false);
		m_networkCatSearchComboBox.setNullSelectionAllowed(true);		
		m_networkCatSearchComboBox.setImmediate(true);
		m_networkCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});


		for (String category: m_notif_categories) {
			m_notifCatSearchComboBox.addItem(category);
		}
		m_notifCatSearchComboBox.setInvalidAllowed(false);
		m_notifCatSearchComboBox.setNullSelectionAllowed(true);		
		m_notifCatSearchComboBox.setImmediate(true);
		m_notifCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});
		
		for (String category: m_thresh_categories) {
			m_threshCatSearchComboBox.addItem(category);
		}
		m_threshCatSearchComboBox.setInvalidAllowed(false);
		m_threshCatSearchComboBox.setNullSelectionAllowed(true);		
		m_threshCatSearchComboBox.setImmediate(true);
		m_threshCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});

		m_searchField.setInputPrompt("Search nodes");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		m_searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			public void textChange(final TextChangeEvent event) {
				m_searchText = event.getText();
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});

	}

	private void initEditor() {

		m_editRequisitionNodeLayout.setMargin(true);
		m_editRequisitionNodeLayout.setVisible(false);
		m_editorFields.setBuffered(true);

		HorizontalLayout generalInfo = new HorizontalLayout();
		FormLayout leftGeneralInfo = new FormLayout();
		leftGeneralInfo.addComponent(new Label("Informazioni Generali"));
		TextField descr = new TextField("Descrizione");
		descr.setSizeFull();
		descr.setWidth(8, Unit.CM);
		descr.setHeight(6, Unit.MM);
		m_editorFields.bind(descr, DESCR);
		leftGeneralInfo.addComponent(descr);
		
		TextField hostname = new TextField("Hostname");
		hostname.setSizeFull();
		hostname.setWidth(4, Unit.CM);
		hostname.setHeight(6, Unit.MM);
		hostname.setRequired(true);
		hostname.setRequiredError("Deve essere un nome valido per bind9: non contiene '_' vedi RFCxxxx");
		m_editorFields.bind(hostname, HOST);
		leftGeneralInfo.addComponent(hostname);

		for (final String vrfs: m_vrfs) {
			m_vrfsComboBox.addItem(vrfs);
		}
		m_vrfsComboBox.setInvalidAllowed(false);
		m_vrfsComboBox.setNullSelectionAllowed(false);
		m_editorFields.bind(m_vrfsComboBox, VRF);
		leftGeneralInfo.addComponent(m_vrfsComboBox);
		leftGeneralInfo.addComponent(m_editorFields.buildAndBind(PRIMARY));
		m_parentComboBox.setInvalidAllowed(false);
		m_parentComboBox.setNullSelectionAllowed(false);
		leftGeneralInfo.addComponent(m_parentComboBox);
		m_editorFields.bind(m_parentComboBox, PARENT);
		generalInfo.addComponent(leftGeneralInfo);

		VerticalLayout centerGeneralInfo = new VerticalLayout();
		generalInfo.addComponent(centerGeneralInfo);

		m_secondaryTable.setCaption("Altri ip da monitorare");
		m_secondaryTable.setEditable(true);
		m_secondaryTable.setSelectable(true);
		m_secondaryTable.setHeight(180,Unit.PIXELS);
		m_secondaryTable.setWidth(130,Unit.PIXELS);
		
		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.addComponent(m_secondaryTable);
		generalInfo.addComponent(rightGeneralInfo);

		generalInfo.setExpandRatio(leftGeneralInfo, 3);
		generalInfo.setExpandRatio(centerGeneralInfo, 1);
		generalInfo.setExpandRatio(rightGeneralInfo, 3);
		m_editRequisitionNodeLayout.addComponent(new Panel(generalInfo));

		FormLayout categoryInfo = new FormLayout();
		categoryInfo.addComponent(new Label("Categorie"));
		
		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
		for (String[] categories: m_network_categories) {
			m_networkCatComboBox.addItem(categories);
			m_networkCatComboBox.setItemCaption(categories, categories[1]+" - " + categories[0]);
		}
		m_networkCatComboBox.setInvalidAllowed(false);
		m_networkCatComboBox.setNullSelectionAllowed(false);		
		m_editorFields.bind(m_networkCatComboBox, NETWORK_CATEGORY);
		
		for (String notif: m_notif_categories) {
			m_notifCatComboBox.addItem(notif);
		}
		m_notifCatComboBox.setInvalidAllowed(false);
		m_notifCatComboBox.setNullSelectionAllowed(false);
		m_editorFields.bind(m_notifCatComboBox, NOTIF_CATEGORY);

		for (String threshold: m_thresh_categories) {
			m_threshCatComboBox.addItem(threshold);
		}
		m_threshCatComboBox.setInvalidAllowed(false);
		m_threshCatComboBox.setNullSelectionAllowed(false);
		m_editorFields.bind(m_threshCatComboBox, THRESH_CATEGORY);
		
		catLayout.addComponent(m_networkCatComboBox);
		catLayout.addComponent(m_notifCatComboBox);
		catLayout.addComponent(m_threshCatComboBox);
		categoryInfo.addComponent(catLayout);
		m_editRequisitionNodeLayout.addComponent(new Panel(categoryInfo));

		FormLayout snmpProfile = new FormLayout();
		snmpProfile.addComponent(new Label("Credenziali SNMP per collezione dati"));
		HorizontalLayout snmplayout = new HorizontalLayout();
		for (String[] snmp: m_snmp_profiles) {
			m_snmpComboBox.addItem(snmp[0]);
		}
		m_snmpComboBox.setInvalidAllowed(false);
		m_snmpComboBox.setNullSelectionAllowed(false);
		m_editorFields.bind(m_snmpComboBox, SNMP_PROFILE);
		snmplayout.addComponent(m_snmpComboBox);
		snmpProfile.addComponent(snmplayout);
        m_editRequisitionNodeLayout.addComponent(new Panel(snmpProfile));

		FormLayout backupProfile = new FormLayout();
        for (String[] backup: m_backup_profiles) {
        	m_backupComboBox.addItem(backup[0]);
        }
        m_backupComboBox.setInvalidAllowed(false);
        m_backupComboBox.setNullSelectionAllowed(false);
		m_editorFields.bind(m_backupComboBox, BACKUP_PROFILE);
		backupProfile.addComponent(new Label("Credenziali accesso per backup configurazione"));
		HorizontalLayout backuplayout = new HorizontalLayout();
		backuplayout.addComponent(m_backupComboBox);
		backupProfile.addComponent(backuplayout);
		m_editRequisitionNodeLayout.addComponent(new Panel(backupProfile));

		HorizontalLayout localizationInfo = new HorizontalLayout();
		FormLayout localization = new FormLayout();
		localization.addComponent(new Label("Localizzazione"));
		localization.addComponent(m_editorFields.buildAndBind(CITY));
		TextField address = new TextField("Indirizzo");
		address.setWidth(8, Unit.CM);
		address.setHeight(6, Unit.MM);
		m_editorFields.bind(address, ADDRESS);
		localization.addComponent(address);
		localizationInfo.addComponent(localization);
		m_editRequisitionNodeLayout.addComponent(new Panel(localizationInfo));

	}

	private class NodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String   needle="";
		private String[] needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		
		public NodeFilter(Object o, Object c1, Object c2,Object c3) {
			if ( o != null)
				needle = (String) o;
			if ( c1 != null) 
				needle1 = (String[]) c1;
			if ( c2 != null)
				needle2 = (String) c2;
			if ( c3 != null)
				needle3 = (String) c3;
		}

		@SuppressWarnings("unchecked")
		public boolean passesFilter(Object itemId, Item item) {
			TrentinoNetworkRequisitionNode node = ((BeanItem<TrentinoNetworkRequisitionNode>)item).getBean();			
			return (    node.getNodeLabel().contains(needle) 
					&& ( needle1 == null || node.getNetworkCategory() == needle1 ) 
					&& ( needle2 == null || node.getNotifCategory()   == needle2 )
		            && ( needle3 == null || node.getThreshCategory()  == needle3 ) 
					);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private void initActionButtons() {
		m_updateNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);				
		m_resetNodeButton.setEnabled(false);
		
		//FIXME add new operation
		m_addNewNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				/*
				RequisitionNode node = new RequisitionNode();
				node.setForeignId("prova");
				node.setNodeLabel("prova.arsinfo.it");
				node.setCity("Positano");
				RequisitionInterface primary = new RequisitionInterface();
				primary.setIpAddr("10.10.10.10");
				primary.setDescr("Added by Provisioning Dashboard");
				primary.setSnmpPrimary("P");
				node.putInterface(primary);
				node.putCategory(new RequisitionCategory("categoria1"));
				node.putCategory(new RequisitionCategory("categoria2"));
				node.putCategory(new RequisitionCategory("categoria3"));
				node.putCategory(new RequisitionCategory("categoria4"));
				
				node.putAsset(new RequisitionAsset("enable", "notused"));
				node.putAsset(new RequisitionAsset("connection", "notused"));
				node.putAsset(new RequisitionAsset("autoenable", "notused"));
				node.putAsset(new RequisitionAsset("username", "notused"));
				node.putAsset(new RequisitionAsset("password", "notused"));
				node.putAsset(new RequisitionAsset("description", "notused"));
				node.putAsset(new RequisitionAsset("address1", "notused"));

				try {
					add(node);
				} catch (UnmarshalException e) {
					System.err.println(e.getMessage());
				}*/
				m_editRequisitionNodeLayout.setVisible(false);
				Notification.show("New", "Operation not yet supported", Type.WARNING_MESSAGE);
			}
		});

		//FIXME add update operation
		m_updateNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				Notification.show("Update", "Operation not yet supported", Type.WARNING_MESSAGE);
				/*
				try {
					m_editorFields.commit();
				} catch (CommitException e) {
					e.printStackTrace();
				}
				*/
				m_editRequisitionNodeLayout.setVisible(false);
				m_updateNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
			}
		});

		//FIXME add delete operation
		m_removeNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				Notification.show("Delete", "Operation not yet supported", Type.WARNING_MESSAGE);
				m_editRequisitionNodeLayout.setVisible(false);
				m_updateNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
			}
		});
		
		m_resetNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_editorFields.discard();
				m_requisitionContainer.removeAllContainerFilters();
				m_editRequisitionNodeLayout.setVisible(false);
				m_updateNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
			}
		});

	}

	private void initProvisionNodeList() {
		// FIXME disable changes when provided by FAST
		m_requisitionContainer = getProvisionNodeList();
		m_requisitionTable.setContainerDataSource(m_requisitionContainer);
		m_requisitionTable.setVisibleColumns(new String[] { LABEL });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void valueChange(ValueChangeEvent event) {
				m_secondaryTable.removeAllItems();
				Object contactId = m_requisitionTable.getValue();

				if (contactId != null) {
					TrentinoNetworkRequisitionNode node = ((BeanItem<TrentinoNetworkRequisitionNode>)m_requisitionTable
						.getItem(contactId)).getBean();
					node.updateSnmpProfile(getService().getSnmpInfo(node.getPrimary()));
					m_snmpComboBox.select(node.getSnmpProfile());

					m_editorFields.setItemDataSource(node);
					m_secondaryTable.setContainerDataSource(node.getSecondary());
					m_editRequisitionNodeLayout.setVisible(true);
					m_updateNodeButton.setEnabled(true);
					m_removeNodeButton.setEnabled(true);
					m_resetNodeButton.setEnabled(true);
				}
			}
		});
	}
	
	private BeanContainer<String, TrentinoNetworkRequisitionNode> getProvisionNodeList() {
		BeanContainer<String,TrentinoNetworkRequisitionNode> nodes = new BeanContainer<String,TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
		nodes.setBeanIdProperty(HOST);
		for (RequisitionNode node : getService().getRequisitionNodes(getForeignSource()).getNodes()) {
			nodes.addBean(new TrentinoNetworkRequisitionNode(node,getService()));
			m_parentComboBox.addItem(node.getForeignId());
		}
		return nodes;
	}
	
}
