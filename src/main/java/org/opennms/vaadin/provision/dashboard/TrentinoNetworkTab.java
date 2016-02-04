package org.opennms.vaadin.provision.dashboard;

import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;

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
import com.vaadin.data.validator.RegexpValidator;
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
@Title("TrentinoNetwork Provision Dashboard - Trentino Network Requisition")
@Theme("runo")
public class TrentinoNetworkTab extends DashboardTab {

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

	public static final String TN = "TrentinoNetwork";
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

	private ComboBox m_networkCatSearchComboBox = new ComboBox("Select Network Category");
	private ComboBox m_notifCatSearchComboBox   = new ComboBox("Select Notification Category");
	private ComboBox m_threshCatSearchComboBox  = new ComboBox("Select Threshold Category");
	private TextField m_searchField       = new TextField("Type Label Text");
	private Table m_requisitionTable   	= new Table();
	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");

	private TextField m_hostname = new TextField("Hostname");
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private ComboBox m_vrfsComboBox = new ComboBox("Dominio");
	private ComboBox m_parentComboBox = new ComboBox("Dipende da");
	private ComboBox m_networkCatComboBox = new ComboBox("Network Category");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Category");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Category");
	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");

	private Table m_secondaryIpAddressTable = new Table();
	private ComboBox m_secondaryIpComboBox = new ComboBox("Seleziona indirizzo ip");

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();
	
	private BeanFieldGroup<TrentinoNetworkNode> m_editorFields     = new BeanFieldGroup<TrentinoNetworkNode>(TrentinoNetworkNode.class);
	Integer newHost = 0;
	
	public TrentinoNetworkTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		Map<String, BackupProfile> backupprofilemap;
		if (loaded)
			return;
		try {
			for (String snmpprofile: getService().getTnDao().getSnmpProfiles().keySet()) {
				m_snmpComboBox.addItem(snmpprofile);
			}
		} catch (SQLException e) {
			logger.warning("Load of Snmp Profile from db Failed: "+e.getLocalizedMessage());
			Notification.show("Snmp Profile", "Load from db Failed: "+e.getLocalizedMessage(), Type.WARNING_MESSAGE);
			return;
		}

		try {
			backupprofilemap = getService().getTnDao().getBackupProfiles();
			for (String backupprofile: backupprofilemap.keySet()) {
				m_backupComboBox.addItem(backupprofile);
			}
		} catch (SQLException e) {
			logger.warning("Load of Backup Profile from db Failed: "+e.getLocalizedMessage());
			Notification.show("Backup Profile", "Load from db Failed: "+e.getLocalizedMessage(), Type.WARNING_MESSAGE);
			return;
		}

		try {
			m_requisitionContainer = getService().getRequisitionContainer(LABEL,TN,backupprofilemap);
		} catch (UniformInterfaceException e) {
			logger.info("Response Status:" + e.getResponse().getStatus() + " Reason: "+e.getResponse().getStatusInfo().getReasonPhrase());
			if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NO_CONTENT.getStatusCode()) {
				logger.info("No Requisition Found: "+e.getLocalizedMessage());
				getService().createRequisition(TN);
				load();
				return;
			}
			logger.warning("Load from rest Failed: "+e.getLocalizedMessage());
			Notification.show("Load Node Requisition", "Load from rest Failed Failed: "+e.getLocalizedMessage(), Type.WARNING_MESSAGE);
			return;
		}

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
		bottomRightLayout.addComponent(m_saveNodeButton);
		bottomRightLayout.addComponent(m_resetNodeButton);
		bottomRightLayout.setComponentAlignment(m_removeNodeButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveNodeButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_resetNodeButton,  Alignment.MIDDLE_RIGHT);
		rightLayout.addComponent(new Panel(bottomRightLayout));
		
		m_requisitionTable.setContainerDataSource(m_requisitionContainer);
		m_requisitionTable.setVisibleColumns(new String[] { LABEL,VALID });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (String[] categories: TNDao.m_network_categories) {
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


		for (String category: TNDao.m_notif_categories) {
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
		
		for (String category: TNDao.m_thresh_categories) {
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

		for (final String vrfs: TNDao.m_vrfs) {
			m_vrfsComboBox.addItem(vrfs);
		}

		for (String[] categories: TNDao.m_network_categories) {
			m_networkCatComboBox.addItem(categories);
			m_networkCatComboBox.setItemCaption(categories, categories[1]+" - " + categories[0]);
		}

		for (String notif: TNDao.m_notif_categories) {
			m_notifCatComboBox.addItem(notif);
		}

		for (String threshold: TNDao.m_thresh_categories) {
			m_threshCatComboBox.addItem(threshold);
		}

		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);

		m_hostname.setSizeFull();
		m_hostname.setWidth(4, Unit.CM);
		m_hostname.setHeight(6, Unit.MM);
		m_hostname.setRequired(true);
		m_hostname.setRequiredError("hostname must be defined");
		m_hostname.addValidator(new RegexpValidator("^(?![0-9]+$)(?!-)[a-zA-Z0-9-]{,63}(?<!-)$","The definitive descriptions of the rules for forming domain names appear in RFC 1035, RFC 1123, and RFC 2181." +
		" A domain name consists of one or more parts, technically called labels, that are conventionally concatenated, and delimited by dots, such asexample.com."
				+ " Each label may contain up to 63 characters." +
				" The full domain name may not exceed a total length of 253 characters in its external dotted-label specification." +
				" The characters allowed in a label are a subset of the ASCII character set, and includes the characters a through z, A through Z, digits 0 through 9, the hyphen." +
				" This rule is known as the LDH rule (letters, digits, hyphen). " +
				" Labels may not start or end with a hyphen." +
				" A hostname is a domain name that has at least one IP address associated."));
		m_hostname.addValidator(new DnsNodelabelValidator());
		m_hostname.addValidator(new DuplicatedNodelabelValidator());
		m_hostname.addValidator(new SubdomainValidator());
		m_hostname.addValidator(new DuplicatedForeignIdValidator());
		m_hostname.setImmediate(true);

		m_networkCatComboBox.setInvalidAllowed(false);
		m_networkCatComboBox.setNullSelectionAllowed(false);
		m_networkCatComboBox.setRequired(true);
		m_networkCatComboBox.setRequiredError("E' necessario scegliere una coppia di categorie di rete");
		m_networkCatComboBox.setImmediate(true);
		m_networkCatComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = -3559078865783782719L;
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
				if (node.getForeignId() == null) {
					m_vrfsComboBox.select(getService().getTnDao().getDefaultValuesFromNetworkCategory(m_networkCatComboBox.getValue())[2]);
					m_notifCatComboBox.select(getService().getTnDao().getDefaultValuesFromNetworkCategory(m_networkCatComboBox.getValue())[3]);
					m_threshCatComboBox.select(getService().getTnDao().getDefaultValuesFromNetworkCategory(m_networkCatComboBox.getValue())[4]);
					m_backupComboBox.select(getService().getTnDao().getDefaultValuesFromNetworkCategory(m_networkCatComboBox.getValue())[5]);
					m_snmpComboBox.select(getService().getTnDao().getDefaultValuesFromNetworkCategory(m_networkCatComboBox.getValue())[6]);
				}
			}
		});

		m_vrfsComboBox.setInvalidAllowed(false);
		m_vrfsComboBox.setNullSelectionAllowed(false);
		m_vrfsComboBox.setRequired(true);
		m_vrfsComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_vrfsComboBox.setImmediate(true);
		m_vrfsComboBox.addValidator(new DnsNodelabelValidator());
		m_vrfsComboBox.addValidator(new DuplicatedNodelabelValidator());
		m_vrfsComboBox.addValidator(new SubdomainValidator());
		
		
		TextField primary = new TextField(PRIMARY);
		primary.setRequired(true);
		primary.setRequiredError("E' necessario specifica un indirizzo ip primario");
		primary.setImmediate(true);
		primary.addValidator(new RegexpValidator(
		"^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$", 
		"Deve essere inserito un valido indirizzo ip"));
		primary.addValidator(new DuplicatedPrimaryValidator());

		m_parentComboBox.setInvalidAllowed(false);
		m_parentComboBox.setNullSelectionAllowed(true);

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
					removeSecondaryInteface(m_editorFields.getItemDataSource().getBean().getForeignId(),
							(String)source.getContainerProperty(itemId, "indirizzo ip").getValue());
			        source.getContainerDataSource().removeItem(itemId);
					logger.info("Delete ip: " + itemId);
					Notification.show("Delete ip", "Done", Type.HUMANIZED_MESSAGE);
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
					try {
						TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
						if (node.getForeignId() != null) {
							IndexedContainer secondary = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
							for (Object id: secondary.getItemIds()) {
								secondary.getContainerProperty(id, "indirizzo ip").getValue().equals(m_secondaryIpComboBox.getValue().toString());
								logger.info("Already added ip: " + m_secondaryIpComboBox.getValue().toString());
								Notification.show("Add ip", "Already added", Type.HUMANIZED_MESSAGE);
								return;
							}
							addSecondaryInterface(m_editorFields.getItemDataSource().getBean().getForeignId(),
									m_secondaryIpComboBox.getValue().toString());
							Item ipItem = secondary.getItem(secondary.addItem());
							ipItem.getItemProperty("indirizzo ip").setValue(m_secondaryIpComboBox.getValue().toString()); 
							logger.info("Add ip: " + m_secondaryIpComboBox.getValue().toString());
							Notification.show("Add ip", "Done", Type.HUMANIZED_MESSAGE);
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
		
		m_notifCatComboBox.setInvalidAllowed(false);
		m_notifCatComboBox.setNullSelectionAllowed(false);
		m_notifCatComboBox.setRequired(true);
		m_notifCatComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");

		m_threshCatComboBox.setInvalidAllowed(false);
		m_threshCatComboBox.setNullSelectionAllowed(false);
		m_threshCatComboBox.setRequired(true);
		m_threshCatComboBox.setRequiredError("E' necessario scegliere una categoria per le threshold");

		m_snmpComboBox.setInvalidAllowed(false);
		m_snmpComboBox.setNullSelectionAllowed(false);
		m_snmpComboBox.setRequired(true);
		m_snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");


        m_backupComboBox.setInvalidAllowed(false);
        m_backupComboBox.setNullSelectionAllowed(false);
        m_backupComboBox.setRequired(true);
        m_backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");
		TextField city = new TextField("Citta'");
		city.setWidth(8, Unit.CM);
		city.setHeight(6, Unit.MM);
		
		TextField address = new TextField("Indirizzo");
		address.setWidth(8, Unit.CM);
		address.setHeight(6, Unit.MM);

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_descrComboBox, DESCR);
		m_editorFields.bind(m_hostname, HOST);
		m_editorFields.bind(m_networkCatComboBox, NETWORK_CATEGORY);
		m_editorFields.bind(m_vrfsComboBox, VRF);
		m_editorFields.bind(primary,PRIMARY);
		m_editorFields.bind(m_parentComboBox, PARENT);
		m_editorFields.bind(m_snmpComboBox, SNMP_PROFILE);
		m_editorFields.bind(m_backupComboBox, BACKUP_PROFILE);
		m_editorFields.bind(m_notifCatComboBox, NOTIF_CATEGORY);
		m_editorFields.bind(m_threshCatComboBox, THRESH_CATEGORY);
		m_editorFields.bind(city,OnmsDao.CITY);
	    m_editorFields.bind(address, OnmsDao.ADDRESS);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_descrComboBox);
		leftGeneralInfo.addComponent(m_hostname);
		leftGeneralInfo.addComponent(m_networkCatComboBox);
		leftGeneralInfo.addComponent(m_vrfsComboBox);
		leftGeneralInfo.addComponent(primary);
		leftGeneralInfo.addComponent(m_parentComboBox);
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
		catLayout.addComponent(m_notifCatComboBox);
		catLayout.addComponent(m_threshCatComboBox);
		
		HorizontalLayout profLayout = new HorizontalLayout();
		profLayout.setSizeFull();
		profLayout.addComponent(m_snmpComboBox);
		profLayout.addComponent(m_backupComboBox);
				
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
				BeanItem<TrentinoNetworkNode> bean = m_requisitionContainer.addBeanAt(0,new TrentinoNetworkNode("notSavedHost"+newHost++));
				m_networkCatSearchComboBox.select(null);
				m_networkCatSearchComboBox.setValue(null);
				m_notifCatSearchComboBox.select(null);
				m_notifCatSearchComboBox.setValue(null);
				m_threshCatSearchComboBox.select(null);
				m_threshCatSearchComboBox.setValue(null);
				m_searchText="";
				m_searchField.setValue(m_searchText);
				m_requisitionTable.select(bean.getBean().getNodeLabel());
				selectItem();
				m_hostname.focus();
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
						getService().save(TN,node);
					} else {
						getService().update(TN,node);
					}
					m_requisitionContainer.addContainerFilter(new NodeFilter(m_editorFields.getItemDataSource().getBean().getNodeLabel(), null,null,null));
					m_requisitionContainer.removeAllContainerFilters();
					if (m_searchText == null)
						m_searchText="";
					m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
					logger.info("Saved: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
					Notification.show("Save", "Done", Type.HUMANIZED_MESSAGE);
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
						getService().deleteNode(TN,node.getBean());
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

		loaded=true;


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
			
			for (String nodelabel :getService().getNodeLabels())
				m_parentComboBox.addItem(nodelabel);
			
			m_secondaryIpComboBox.removeAllItems();
			for (String ip: getService().getIpAddresses(TN,node.getNodeLabel()) ) {
				if (ip.equals(node.getPrimary()))
					continue;
				m_secondaryIpComboBox.addItem(ip);
			}
			
			m_secondaryIpAddressTable.setContainerDataSource(node.getSecondary());

			if (node.getForeignId() != null) {
				String snmpProfile=null;
				if (node.getPrimary() != null) {
					try {
					snmpProfile = getService().getSnmpProfile(node.getPrimary());
					} catch (SQLException sqle) {
						
					}
				}
				if (snmpProfile == null)
					node.setValid(false);
				node.setSnmpProfile(snmpProfile);
			}
			m_editorFields.setItemDataSource(node);
			m_editRequisitionNodeLayout.setVisible(true);
			m_saveNodeButton.setEnabled(true);
			m_removeNodeButton.setEnabled(true);
			m_resetNodeButton.setEnabled(true);
			
			if (node.getDescr().contains("FAST")) {
				m_saveNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
				Notification.show("provided by FAST", "le modifiche ai nodi aggiunti da FAST non sono abilitate", Type.WARNING_MESSAGE);
			} 
			
			
		}

	}
	
	public void addSecondaryInterface(String foreignId,String ipaddress) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		ipsecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		ipsecondary.setDescr("Provided by Provision Dashboard");
		ipsecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		getService().add(TN,foreignId, ipsecondary);
	}
	
	public void removeSecondaryInteface(String foreignId,String ipaddress) {
		getService().deleteInterface(TN, foreignId, ipaddress);
	}
	
	class SubdomainValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4896238666294939805L;

		@Override
		public void validate(Object value) throws InvalidValueException {
			TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
			 if (hasUnSupportedDnsDomain(node.getHostname(), node.getNodeLabel(), TNDao.m_sub_domains))
	             throw new InvalidValueException("There is no dns domain defined for: " + node.getHostname());
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
	         if (node.getForeignId() == null  && getService().hasDuplicatedForeignId((node.getHostname())))
	             throw new InvalidValueException("The hostname exists: cannot duplicate hostname: " + node.getHostname());
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
	         if (getService().hasDuplicatedNodelabel(node.getNodeLabel()))
	             throw new InvalidValueException("The node label exist: cannot duplicate node label: " + node.getNodeLabel());
	       }
	}
	
	class DnsNodelabelValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3708035507162528130L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
	         if (node.getNodeLabel().length() > 253)
	             throw new InvalidValueException("The node label is too long (more then 253 valid chars): " + node.getNodeLabel());
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
	         if (getService().hasDuplicatedPrimary((node.getPrimary())))
	             throw new InvalidValueException("The node label exist: cannot duplicate primary ip: " + node.getPrimary());
	       }
	}


}
