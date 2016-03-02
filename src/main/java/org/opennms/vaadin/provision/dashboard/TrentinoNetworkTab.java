package org.opennms.vaadin.provision.dashboard;

import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.Vrf;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
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
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TNPD - Trentino Network Requisition")
@Theme("runo")
public class TrentinoNetworkTab extends DashboardTab {

	private class NodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String   needle="";
		private Vrf needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		
		public NodeFilter(Object o, Object c1, Object c2,Object c3) {
			if ( o != null)
				needle = (String) o;
			if ( c1 != null) 
				needle1 = (Vrf) c1;
			if ( c2 != null)
				needle2 = (String) c2;
			if ( c3 != null)
				needle3 = (String) c3;
		}

		@SuppressWarnings("unchecked")
		public boolean passesFilter(Object itemId, Item item) {
			TrentinoNetworkNode node = ((BeanItem<TrentinoNetworkNode>)item).getBean();			
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

	private static final long serialVersionUID = -5948892618258879832L;

	public static final String LABEL = "nodeLabel";

	public static final String SNMP_PROFILE    = "snmpProfile";
	public static final String BACKUP_PROFILE  = "backupProfile";
	

	public static final String NETWORK_CATEGORY = "networkCategory";
	public static final String NOTIF_CATEGORY   = "notifCategory";
	public static final String THRESH_CATEGORY  = "threshCategory";

	public static final String DESCR = "descr";
	public static final String HOST = "hostname";
	public static final String PRIMARY = "primary";
	public static final String VRF = "vrf";
	public static final String PARENT = "parent";
	public static final String VALID = "valid";

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private String m_searchText = null;
	private BeanContainer<String, TrentinoNetworkNode> m_requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
	private boolean loaded=false;

	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");
	private Table m_requisitionTable   	= new Table();

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();	
	private BeanFieldGroup<TrentinoNetworkNode> m_editorFields     = new BeanFieldGroup<TrentinoNetworkNode>(TrentinoNetworkNode.class);
	Integer newHost = 0;
	
	private ComboBox m_secondaryIpComboBox = new ComboBox("Seleziona indirizzo ip");
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private Table m_secondaryIpAddressTable = new Table();
	private ComboBox m_vrfsComboBox = new ComboBox("Dominio");

	public TrentinoNetworkTab(DashBoardSessionService service) {
		super(service);
	}

	@Override
	public void load() {
		if (!loaded) {
			try {
				m_requisitionContainer = getService().getRequisitionContainer(LABEL,DashBoardUtils.TN);
				m_requisitionTable.setContainerDataSource(m_requisitionContainer);
				layout();
				loaded=true;
			} catch (UniformInterfaceException e) {
				logger.info("Response Status:" + e.getResponse().getStatus() + " Reason: "+e.getResponse().getStatusInfo().getReasonPhrase());
				if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NO_CONTENT.getStatusCode()) {
					logger.info("No Requisition Found: "+e.getLocalizedMessage());
					getService().createRequisition(DashBoardUtils.TN);
					load();
					return;
				}
				logger.warning("Load from rest Failed: "+e.getLocalizedMessage());
				Notification.show("Load Node Requisition", "Load from rest Failed Failed: "+e.getLocalizedMessage(), Type.WARNING_MESSAGE);
				return;
			}			
		}
	}
	
	private void layout() { 
		final ComboBox networkCatSearchComboBox = new ComboBox("Select Network Category");
		final ComboBox notifCatSearchComboBox   = new ComboBox("Select Notification Category");
		final ComboBox threshCatSearchComboBox  = new ComboBox("Select Threshold Category");
		final TextField searchField       = new TextField("Type Label Text");

		final TextField hostname = new TextField("Hostname");
		TextField primary = new TextField(PRIMARY);
		final ComboBox networkCatComboBox = new ComboBox("Network Category");
		final ComboBox notifCatComboBox   = new ComboBox("Notification Category");
		final ComboBox threshCatComboBox  = new ComboBox("Threshold Category");
		final ComboBox snmpComboBox  = new ComboBox("SNMP Profile");
		final ComboBox backupComboBox  = new ComboBox("Backup Profile");
		ComboBox parentComboBox = new ComboBox("Dipende da");

		for (String snmpprofile: getService().getSnmpProfileContainer().getSnmpProfileMap().keySet()) {
			snmpComboBox.addItem(snmpprofile);
		}

		for (String backupprofile: getService().getBackupProfileContainer().getBackupProfileMap().keySet()) {
			backupComboBox.addItem(backupprofile);
		}

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setCompositionRoot(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(networkCatSearchComboBox);
		searchlayout.addComponent(notifCatSearchComboBox);
		searchlayout.addComponent(threshCatSearchComboBox);

		searchField.setWidth("80%");
		searchlayout.addComponent(searchField);
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
		bottomRightLayout.addComponent(m_saveNodeButton);
		bottomRightLayout.addComponent(m_resetNodeButton);
		bottomRightLayout.setComponentAlignment(m_removeNodeButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveNodeButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_resetNodeButton,  Alignment.MIDDLE_RIGHT);
		rightLayout.addComponent(new Panel(bottomRightLayout));
		
		m_requisitionTable.setVisibleColumns(new Object[] { LABEL,VALID });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (Vrf categories: getService().getVrfContainer().getVrfMap().values()) {
			networkCatSearchComboBox.addItem(categories);
			networkCatSearchComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
		}
		networkCatSearchComboBox.setInvalidAllowed(false);
		networkCatSearchComboBox.setNullSelectionAllowed(true);		
		networkCatSearchComboBox.setImmediate(true);
		networkCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, networkCatSearchComboBox.getValue(),notifCatSearchComboBox.getValue(),threshCatSearchComboBox.getValue()));
			}
		});


		for (String category: DashBoardUtils.m_notify_levels) {
			notifCatSearchComboBox.addItem(category);
		}
		notifCatSearchComboBox.setInvalidAllowed(false);
		notifCatSearchComboBox.setNullSelectionAllowed(true);		
		notifCatSearchComboBox.setImmediate(true);
		notifCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, networkCatSearchComboBox.getValue(),notifCatSearchComboBox.getValue(),threshCatSearchComboBox.getValue()));
			}
		});
		
		for (String category: DashBoardUtils.m_threshold_levels) {
			threshCatSearchComboBox.addItem(category);
		}
		threshCatSearchComboBox.setInvalidAllowed(false);
		threshCatSearchComboBox.setNullSelectionAllowed(true);		
		threshCatSearchComboBox.setImmediate(true);
		threshCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, networkCatSearchComboBox.getValue(),notifCatSearchComboBox.getValue(),threshCatSearchComboBox.getValue()));
			}
		});

		searchField.setInputPrompt("Search nodes");
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			public void textChange(final TextChangeEvent event) {
				m_searchText = event.getText();
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, networkCatSearchComboBox.getValue(),notifCatSearchComboBox.getValue(),threshCatSearchComboBox.getValue()));
			}
		});

		m_vrfsComboBox.removeAllItems();
		for (final String vrfs: getService().getDnsDomainContainer().getDomains()) {
			m_vrfsComboBox.addItem(vrfs);
		}

		for (Vrf categories: getService().getVrfContainer().getVrfMap().values()) {
			networkCatComboBox.addItem(categories);
			networkCatComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
		}

		for (String notif: DashBoardUtils.m_notify_levels) {
			notifCatComboBox.addItem(notif);
		}

		for (String threshold: DashBoardUtils.m_threshold_levels) {
			threshCatComboBox.addItem(threshold);
		}

		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);

		hostname.setSizeFull();
		hostname.setWidth(4, Unit.CM);
		hostname.setHeight(6, Unit.MM);
		hostname.setRequired(true);
		hostname.setRequiredError("hostname must be defined");
		hostname.addValidator(new DnsNodeLabelValidator());
		hostname.addValidator(new SubdomainValidator());
		hostname.addValidator(new DuplicatedForeignIdValidator());
		hostname.addValidator(new DuplicatedNodelabelValidator());
		hostname.setImmediate(true);

		networkCatComboBox.setInvalidAllowed(false);
		networkCatComboBox.setNullSelectionAllowed(false);
		networkCatComboBox.setRequired(true);
		networkCatComboBox.setRequiredError("E' necessario scegliere una coppia di categorie di rete");
		networkCatComboBox.setImmediate(true);
		networkCatComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = -3559078865783782719L;
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
				if (node.getForeignId() == null) {
					m_vrfsComboBox.select(((Vrf)networkCatComboBox.getValue()).getDnsdomain());
					notifCatComboBox.select(((Vrf)networkCatComboBox.getValue()).getNotifylevel());
					threshCatComboBox.select(((Vrf)networkCatComboBox.getValue()).getThresholdlevel());
					backupComboBox.select(((Vrf)networkCatComboBox.getValue()).getBackupprofile());
					snmpComboBox.select(((Vrf)networkCatComboBox.getValue()).getSnmpprofile());
				}
			}
		});

		m_vrfsComboBox.setInvalidAllowed(false);
		m_vrfsComboBox.setNullSelectionAllowed(false);
		m_vrfsComboBox.setRequired(true);
		m_vrfsComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_vrfsComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = -2209110948375990804L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
	        	logger.info("vrf combo box value change:"+ m_vrfsComboBox.getValue());
		        try {
		            hostname.validate();
		            hostname.setComponentError(null); // MAGIC CODE HERE!!!
		        } catch (Exception e) {
		        	logger.info("not working validation");
		        }
		    }
		});
		m_vrfsComboBox.setImmediate(true);

				
		primary.setRequired(true);
		primary.setRequiredError("E' necessario specifica un indirizzo ip primario");
		primary.setImmediate(true);
		primary.addValidator(new IpValidator());
		primary.addValidator(new DuplicatedPrimaryValidator());

		parentComboBox.setInvalidAllowed(false);
		parentComboBox.setNullSelectionAllowed(true);

		for (String nodelabel :getService().getNodeLabels())
			parentComboBox.addItem(nodelabel);
		

		m_secondaryIpAddressTable.setCaption("Altri ip da monitorare");
		m_secondaryIpAddressTable.setHeight(180,Unit.PIXELS);
		m_secondaryIpAddressTable.setWidth(300,Unit.PIXELS);
		m_secondaryIpAddressTable.setSelectable(true);
		m_secondaryIpAddressTable.addGeneratedColumn("Delete", new ColumnGenerator() {
			 
			  /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override 
			  public Object generateCell(final Table source, final Object itemId, Object columnId) {
			 
			    Button button = new Button("Delete");
			 
			    button.addClickListener(new ClickListener() {
			 
			      /**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				@Override public void buttonClick(ClickEvent event) {
					try {
						String ip = (String)source.getContainerProperty(itemId, "indirizzo ip").getValue();
						getService().deleteInterface(DashBoardUtils.TN, m_editorFields.getItemDataSource().getBean().getForeignId(), 							
								ip);
			        source.getContainerDataSource().removeItem(itemId);
			        Set<String> secondary = new HashSet<String>();
			        for (Object id: source.getContainerDataSource().getItemIds()) {
			        	secondary.add((String)source.getContainerProperty(id, "indirizzo ip").getValue());
			        }
			        m_secondaryIpComboBox.addItem(ip);
			        m_editorFields.getItemDataSource().getBean().setSecondary(secondary.toArray(new String[secondary.size()]));
					logger.info("Deleted Secondary ip: " + itemId);
					Notification.show("Delete Secondary ip", "Ip: " + itemId + " deleted.", Type.WARNING_MESSAGE);
					} catch (Exception e) {
						logger.warning("Delete ip failed: " + itemId +" " + e.getLocalizedMessage());
						Notification.show("Delete ip", "Failed: "+e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
			      }
			    });
			 
			    return button;
			  }
		});

		m_secondaryIpComboBox.setNullSelectionAllowed(false);
		m_secondaryIpComboBox.setInvalidAllowed(false);
		Button addSecondaryIpButton = new Button("Aggiungi");
		addSecondaryIpButton.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				if (m_secondaryIpComboBox.getValue() != null) {
					String ip = m_secondaryIpComboBox.getValue().toString();
					try {
						TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
						if (node.getForeignId() != null) {
							getService().addSecondaryInterface(DashBoardUtils.TN,m_editorFields.getItemDataSource().getBean().getForeignId(),
									ip);
							IndexedContainer secondaryIpContainer = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
							Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
							ipItem.getItemProperty("indirizzo ip").setValue(m_secondaryIpComboBox.getValue().toString()); 
							Set<String> secondary = new HashSet<String>();
					        for (Object id: m_secondaryIpAddressTable.getContainerDataSource().getItemIds()) {
					        	secondary.add((String)m_secondaryIpAddressTable.getContainerProperty(id, "indirizzo ip").getValue());
					        }
					        m_editorFields.getItemDataSource().getBean().setSecondary(secondary.toArray(new String[secondary.size()]));
							logger.info("Added Secondary ip address: " + ip);
							Notification.show("Add Secondary ip", "Added ip: "+ip+ " to node in repository", Type.WARNING_MESSAGE);
					        m_secondaryIpComboBox.removeItem(ip);
						} else {
							Notification.show("Add ip", "Cannot add secondary to new node: save it and then add secondary", Type.WARNING_MESSAGE);
						}
					} catch (Exception e) {
						logger.warning("Add ip failed: " + m_secondaryIpComboBox.getValue().toString() + " :" +e.getLocalizedMessage());
						Notification.show("Add ip", "Failed: "+e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});
		
		notifCatComboBox.setInvalidAllowed(false);
		notifCatComboBox.setNullSelectionAllowed(false);
		notifCatComboBox.setRequired(true);
		notifCatComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");

		threshCatComboBox.setInvalidAllowed(false);
		threshCatComboBox.setNullSelectionAllowed(false);
		threshCatComboBox.setRequired(true);
		threshCatComboBox.setRequiredError("E' necessario scegliere una categoria per le threshold");

		snmpComboBox.setInvalidAllowed(false);
		snmpComboBox.setNullSelectionAllowed(false);
		snmpComboBox.setRequired(true);
		snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");


        backupComboBox.setInvalidAllowed(false);
        backupComboBox.setNullSelectionAllowed(false);
        backupComboBox.setRequired(true);
        backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");
		TextField city = new TextField("Citta'");
		city.setWidth(8, Unit.CM);
		city.setHeight(6, Unit.MM);
		
		TextField address = new TextField("Indirizzo");
		address.setWidth(8, Unit.CM);
		address.setHeight(6, Unit.MM);

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_descrComboBox, DESCR);
		m_editorFields.bind(hostname, HOST);
		m_editorFields.bind(networkCatComboBox, NETWORK_CATEGORY);
		m_editorFields.bind(m_vrfsComboBox, VRF);
		m_editorFields.bind(primary,PRIMARY);
		m_editorFields.bind(parentComboBox, PARENT);
		m_editorFields.bind(snmpComboBox, SNMP_PROFILE);
		m_editorFields.bind(backupComboBox, BACKUP_PROFILE);
		m_editorFields.bind(notifCatComboBox, NOTIF_CATEGORY);
		m_editorFields.bind(threshCatComboBox, THRESH_CATEGORY);
		m_editorFields.bind(city,DashBoardUtils.CITY);
	    m_editorFields.bind(address, DashBoardUtils.ADDRESS1);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_descrComboBox);
		leftGeneralInfo.addComponent(hostname);
		leftGeneralInfo.addComponent(networkCatComboBox);
		leftGeneralInfo.addComponent(m_vrfsComboBox);
		leftGeneralInfo.addComponent(primary);
		leftGeneralInfo.addComponent(parentComboBox);
		leftGeneralInfo.addComponent(city);
		leftGeneralInfo.addComponent(address);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);

		HorizontalLayout bottomRightGeneralInfo = new HorizontalLayout();
		bottomRightGeneralInfo.addComponent(m_secondaryIpComboBox);
		bottomRightGeneralInfo.addComponent(addSecondaryIpButton);
		
		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.setMargin(true);
		rightGeneralInfo.addComponent(m_secondaryIpAddressTable);
		rightGeneralInfo.addComponent(bottomRightGeneralInfo);
				
		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
		catLayout.addComponent(notifCatComboBox);
		catLayout.addComponent(threshCatComboBox);
		
		HorizontalLayout profLayout = new HorizontalLayout();
		profLayout.setSizeFull();
		profLayout.addComponent(snmpComboBox);
		profLayout.addComponent(backupComboBox);
				
		HorizontalLayout generalInfo = new HorizontalLayout();
		generalInfo.addComponent(leftGeneralInfo);
		generalInfo.addComponent(centerGeneralInfo);
		generalInfo.addComponent(rightGeneralInfo);
		generalInfo.setExpandRatio(leftGeneralInfo, 3);
		generalInfo.setExpandRatio(centerGeneralInfo, 1);
		generalInfo.setExpandRatio(rightGeneralInfo, 3);

				
		FormLayout profileInfo = new FormLayout();
		profileInfo.addComponent(new Label("Profili"));
		profileInfo.addComponent(profLayout);
		profileInfo.addComponent(catLayout);

		m_editRequisitionNodeLayout.setMargin(true);
		m_editRequisitionNodeLayout.setVisible(false);
		m_editRequisitionNodeLayout.addComponent(new Panel(generalInfo));
		m_editRequisitionNodeLayout.addComponent(new Panel(profileInfo));

		m_saveNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);				
		m_resetNodeButton.setEnabled(false);
		
		m_addNewNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				BeanItem<TrentinoNetworkNode> bean = m_requisitionContainer.addBeanAt(0,new TrentinoNetworkNode("notSavedHost"+newHost++,
						getService().getVrfContainer().getVrfMap().values().iterator().next()));
				networkCatSearchComboBox.select(null);
				networkCatSearchComboBox.setValue(null);
				notifCatSearchComboBox.select(null);
				notifCatSearchComboBox.setValue(null);
				threshCatSearchComboBox.select(null);
				threshCatSearchComboBox.setValue(null);
				m_searchText="";
				searchField.setValue(m_searchText);
				m_requisitionTable.select(bean.getBean().getNodeLabel());
				selectItem();
				m_requisitionContainer.removeAllContainerFilters();
				hostname.focus();
			}
		});

		m_saveNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					m_editorFields.commit();
					TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
					if (node.getForeignId() == null) {
						node.setForeignId(node.getHostname());
						node.setValid(true);
						getService().addNode(DashBoardUtils.TN,node);
						logger.info("Added: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
						Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
					} else {
						getService().updateNode(DashBoardUtils.TN,node);
						logger.info("Updated: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
						Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Updated", Type.HUMANIZED_MESSAGE);
					}
					m_requisitionContainer.addContainerFilter(new NodeFilter(node.getHostname(), null, null, null));
					m_requisitionContainer.removeAllContainerFilters();
				} catch (Exception e) {
					e.printStackTrace();
					String localizedMessage = e.getLocalizedMessage();
					Throwable t = e.getCause();
					while ( t != null) {
						if (t.getLocalizedMessage() != null)
							localizedMessage+= ": " + t.getLocalizedMessage();
						t = t.getCause();
					}
					logger.warning("Save Failed: " + localizedMessage);
					Notification.show("Save Failed", localizedMessage, Type.ERROR_MESSAGE);
				}
				m_requisitionTable.unselect(m_requisitionTable.getValue());
			}
		});

		m_removeNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_saveNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
				BeanItem<TrentinoNetworkNode> node = m_editorFields.getItemDataSource();
				logger.info("Deleting: " + node.getBean().getNodeLabel());
				if (node.getBean().getForeignId() !=  null) {
					try {
						getService().deleteNode(DashBoardUtils.TN,node.getBean());
						Notification.show("Delete Node From Requisition", "Done", Type.HUMANIZED_MESSAGE);
					} catch (UniformInterfaceException e) {
						logger.warning(e.getLocalizedMessage()+" Reason: " + e.getResponse().getStatusInfo().getReasonPhrase());
						Notification.show("Delete Node From Requisition", "Failed: "+e.getLocalizedMessage()+ " Reason: " +
						e.getResponse().getStatusInfo().getReasonPhrase(), Type.ERROR_MESSAGE);
						return;
					}
				}
				if ( ! m_requisitionContainer.removeItem(node.getBean().getNodeLabel()))
					m_requisitionContainer.removeItem(m_requisitionContainer.getIdByIndex(0));
				logger.info("Node Deleted");
				Notification.show("Delete", "Done", Type.HUMANIZED_MESSAGE);
		}
		});
		
		m_resetNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_editorFields.discard();
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionTable.unselect(m_requisitionTable.getValue());
				m_saveNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
			}
		});
		
		Set<String> duplicatednodeLabels = getService().checkUniqueNodeLabel();
		if (!duplicatednodeLabels.isEmpty()) {
			logger.warning(" Found Duplicated NodeLabel: " + Arrays.toString(duplicatednodeLabels.toArray()));
			Notification.show("Found Duplicated NodeLabel",  Arrays.toString(duplicatednodeLabels.toArray()), Type.WARNING_MESSAGE);
		}

		Set<String> duplicatedForeignIds= getService().checkUniqueForeignId();
		if (!duplicatedForeignIds.isEmpty()) {
			logger.warning(" Found Duplicated ForeignId: " + Arrays.toString(duplicatedForeignIds.toArray()));
			Notification.show("Found Duplicated ForeignId",  Arrays.toString(duplicatedForeignIds.toArray()), Type.WARNING_MESSAGE);
		}

		Set<String> duplicatedPrimaries= getService().checkUniquePrimary();
		if (!duplicatedPrimaries.isEmpty()) {
			logger.warning(" Found Duplicated Primary IP: " + Arrays.toString(duplicatedPrimaries.toArray()));
			Notification.show("Found Duplicated Primary IP",  Arrays.toString(duplicatedPrimaries.toArray()), Type.WARNING_MESSAGE);
		}
	}

	@SuppressWarnings("unchecked")
	private void selectItem() {
		Object contactId = m_requisitionTable.getValue();

		if (contactId != null) {
			TrentinoNetworkNode node = ((BeanItem<TrentinoNetworkNode>)m_requisitionTable
				.getItem(contactId)).getBean();
			
			m_descrComboBox.removeAllItems();
			if (node.getDescr() != null)
				m_descrComboBox.addItem(node.getDescr());
			
			m_secondaryIpComboBox.removeAllItems();
			for (String ip: getService().getIpAddresses(DashBoardUtils.TN,node.getNodeLabel()) ) {
				if (ip.equals(node.getPrimary()))
					continue;
				boolean add=true;
				if (node.getSecondary() != null) {
					for (String secip: node.getSecondary()) {
						if (ip.equals(secip)) { 
							add=false;
							break;
						}
					}
				}
				if (add)
					m_secondaryIpComboBox.addItem(ip);
			}
			
			IndexedContainer secondaryIpContainer = new IndexedContainer();
			secondaryIpContainer.addContainerProperty("indirizzo ip", String.class, null);
			if (node.getSecondary() != null) {
				for (String ip: node.getSecondary()) {
					Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
					ipItem.getItemProperty("indirizzo ip").setValue(ip); 
				}
			}
			m_secondaryIpAddressTable.setContainerDataSource(secondaryIpContainer);

			if (node.getForeignId() != null) {
				String snmpProfile=null;
				if (node.getPrimary() != null) {
					try {
					snmpProfile = getService().getSnmpProfileName(node.getPrimary());
					} catch (SQLException sqle) {
						logger.warning("Errore nel richiesta del profilo snmp al database: " + sqle.getLocalizedMessage());
						Notification.show("Errore nel richiesta del profilo snmp al database", sqle.getMessage(), Type.WARNING_MESSAGE);
					} catch (UniformInterfaceException uie) {
						
					}
				}
				if (snmpProfile == null)
					node.setValid(false);
				node.setSnmpProfileWithOutUpdating(snmpProfile);
			}
			m_editorFields.setItemDataSource(node);
			m_editRequisitionNodeLayout.setVisible(true);
			m_saveNodeButton.setEnabled(true);
			m_removeNodeButton.setEnabled(true);
			m_resetNodeButton.setEnabled(true);
		}

	}
	
	class SubdomainValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4896238666294939805L;
		
		@Override
		public void validate(Object value) throws InvalidValueException {
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_vrfsComboBox.getValue();
			logger.info("SubdomainValidator: validating hostname: " + hostname);
			 if (hasUnSupportedDnsDomain(hostname, nodelabel, getService().getDnsSubDomainContainer().getSubdomains()))
	             throw new InvalidValueException("There is no dns domain defined for: " + hostname);
	       }
	}

	class DuplicatedForeignIdValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = (String)value;
			logger.info("DuplicatedForeignIdValidator: validating foreignId: " + hostname);
	         if (getService().hasDuplicatedForeignId((hostname)))
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
			TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String ip = (String)value;
			logger.info("DuplicatedPrimaryValidator: validating ip: " + ip);
	         if (getService().hasDuplicatedPrimary((ip)))
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
			TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_vrfsComboBox.getValue();
			logger.info("DuplicatedNodelabelValidator: validating label: " + nodelabel);
	        if (getService().hasDuplicatedNodelabel(nodelabel))
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
			String nodelabel = hostname+"."+m_vrfsComboBox.getValue();
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

}
