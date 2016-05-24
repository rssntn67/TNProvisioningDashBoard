package org.opennms.vaadin.provision.dashboard;

import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SistemiInformativiNode;
import org.opennms.vaadin.provision.model.SnmpProfile;

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
@Title("TNPD - Sistemi Informativi Requisition")
@Theme("runo")
public class SistemiInformativiTab extends DashboardTab {

	private class NodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String   needle="";
		private String   needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		private String   needle4=null;
		private String   needle5=null;
		private String   needle6=null;
		
		public NodeFilter(Object o, Object c1, Object c3,Object c4, Object c5, Object c6) {
			if ( o != null)
				needle = (String) o;
			if ( c1 != null) {
				String[] c12 = (String[]) c1;
				needle1 = (String) c12[0];
				needle2 = (String) c12[1];
			}
			if ( c3 != null)
				needle3 = (String) c3;
			if ( c4 != null)
				needle4 = (String) c4;
			if ( c5 != null)
				needle5 = (String) c5;
			if ( c6 != null)
				needle6 = (String) c6;
		}

		@SuppressWarnings("unchecked")
		public boolean passesFilter(Object itemId, Item item) {
			SistemiInformativiNode node = ((BeanItem<SistemiInformativiNode>)item).getBean();			
			return (    node.getNodeLabel().contains(needle) 
					&& ( needle1 == null || node.getServerLevelCategory()[0].equals(needle1) )
					&& ( needle2 == null || node.getServerLevelCategory()[1].equals(needle2) ) 
		            && ( needle3 == null || node.getManagedByCategory().equals(needle3) ) 
		            && ( needle4 == null || node.getNotifCategory().equals(needle4) ) 
		            && ( needle5 == null || node.getOptionalCategory().equals(needle5) ) 
		            && ( needle6 == null || node.getProdCategory().equals(needle6) ) 
		        	);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private static final long serialVersionUID = -5948892618258879832L;

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private String m_searchText = null;
	private BeanContainer<String, SistemiInformativiNode> m_requisitionContainer = new BeanContainer<String, SistemiInformativiNode>(SistemiInformativiNode.class);
	private boolean loaded=false;

	private Button m_syncRequisButton  = new Button("Sync");
	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");
	private Table m_requisitionTable   	= new Table();

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();	
	private BeanFieldGroup<SistemiInformativiNode> m_editorFields     = new BeanFieldGroup<SistemiInformativiNode>(SistemiInformativiNode.class);
	Integer newHost = 0;
	
	private TextField m_secondaryIpTextBox = new TextField("Aggiungi indirizzo ip");
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private Table m_secondaryIpAddressTable = new Table();
	private ComboBox m_serviceComboBox = new ComboBox("Servizi");
	private ComboBox m_domainComboBox = new ComboBox("Dominio");

	public SistemiInformativiTab(DashBoardSessionService service) {
		super(service);
	}

	@Override
	public void load() {
		updateTabHead();
		if (!loaded) {
			try {
				m_requisitionContainer = getService().getSIContainer();
				m_requisitionTable.setContainerDataSource(m_requisitionContainer);
				layout();
				loaded=true;
			} catch (UniformInterfaceException e) {
				logger.info("Response Status:" + e.getResponse().getStatus() + " Reason: "+e.getResponse().getStatusInfo().getReasonPhrase());
				if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
					logger.info("No Requisition Found: "+e.getLocalizedMessage());
					getService().createRequisition(DashBoardUtils.SI_REQU_NAME);
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
		final ComboBox serverLevelSearchComboBox = new ComboBox("Select Server Type");
		final ComboBox managedBySearchComboBox  = new ComboBox("Select Server Managed by Group");
		final ComboBox prodSearchComboBox      = new ComboBox("Select Production Category");
		final ComboBox notifSearchComboBox      = new ComboBox("Select Notification Category");
		final ComboBox optionalSearchComboBox   = new ComboBox("Select Optional Category");
		final TextField searchField       = new TextField("Type Label Text");

		final TextField hostname = new TextField("Hostname");
		TextField primary = new TextField(DashBoardUtils.PRIMARY);
		final ComboBox serverLevelComboBox = new ComboBox("Server Level Category");
		final ComboBox managedByComboBox  = new ComboBox("Managed by Category");
		final ComboBox prodComboBox  = new ComboBox("Production Category");
		final ComboBox notifComboBox  = new ComboBox("Notification Category");
		final ComboBox optionalComboBox  = new ComboBox("Optional Category");
		final ComboBox tnComboBox  = new ComboBox("TN Category");

		final ComboBox snmpComboBox  = new ComboBox("SNMP Profile");

		Map<String,SnmpProfile> snmpprofilemap = 
				getService().getSnmpProfileContainer().getSnmpProfileMap();
		List<String> snmpprofiles = new ArrayList<String>(snmpprofilemap.keySet());
		Collections.sort(snmpprofiles);
		for (String snmpprofile: snmpprofiles) {
			snmpComboBox.addItem(snmpprofile);
			snmpComboBox.setItemCaption(snmpprofile, 
					snmpprofile + 
					"(community:"+snmpprofilemap.get(snmpprofile).getCommunity()+")"
					+ "(version:"+ snmpprofilemap.get(snmpprofile).getVersion()+")");
		}

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		getCore().addComponent(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(serverLevelSearchComboBox);
		searchlayout.addComponent(managedBySearchComboBox);
		searchlayout.addComponent(prodSearchComboBox);
		searchlayout.addComponent(notifSearchComboBox);
		searchlayout.addComponent(optionalSearchComboBox);

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
		topRightLayout.addComponent(m_syncRequisButton);
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
		
		m_requisitionTable.setVisibleColumns(new Object[] { DashBoardUtils.LABEL,DashBoardUtils.VALID });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (String[] serverLevel: DashBoardUtils.m_server_levels) {
			serverLevelSearchComboBox.addItem(serverLevel);
			serverLevelSearchComboBox.setItemCaption(serverLevel, serverLevel[0] + " - " + serverLevel[1]);
		}
		serverLevelSearchComboBox.setInvalidAllowed(false);
		serverLevelSearchComboBox.setNullSelectionAllowed(true);		
		serverLevelSearchComboBox.setImmediate(true);
		serverLevelSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		for (String[] category: DashBoardUtils.m_server_managedby) {
			managedBySearchComboBox.addItem(category[0]);
			managedBySearchComboBox.setItemCaption(category[0], category[1]);
		}
		managedBySearchComboBox.setInvalidAllowed(false);
		managedBySearchComboBox.setNullSelectionAllowed(true);		
		managedBySearchComboBox.setImmediate(true);
		managedBySearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		for (String category: DashBoardUtils.m_server_prod) {
			prodSearchComboBox.addItem(category);
		}
		prodSearchComboBox.setInvalidAllowed(false);
		prodSearchComboBox.setNullSelectionAllowed(true);		
		prodSearchComboBox.setImmediate(true);
		prodSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		for (String category: DashBoardUtils.m_server_notif) {
			notifSearchComboBox.addItem(category);
		}
		notifSearchComboBox.setInvalidAllowed(false);
		notifSearchComboBox.setNullSelectionAllowed(true);		
		notifSearchComboBox.setImmediate(true);
		notifSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		for (String category: DashBoardUtils.m_server_optional) {
			optionalSearchComboBox.addItem(category);
		}
		optionalSearchComboBox.setInvalidAllowed(false);
		optionalSearchComboBox.setNullSelectionAllowed(true);		
		optionalSearchComboBox.setImmediate(true);
		optionalSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_editRequisitionNodeLayout.setVisible(false);
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		searchField.setInputPrompt("Search nodes");
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			public void textChange(final TextChangeEvent event) {
				m_searchText = event.getText();
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								serverLevelSearchComboBox.getValue(),
								managedBySearchComboBox.getValue(),
								notifSearchComboBox.getValue(),
								optionalSearchComboBox.getValue(),
								prodSearchComboBox.getValue()));
			}
		});

		m_domainComboBox.removeAllItems();
		for (final String domain: getService().getDnsDomainContainer().getDomains()) {
			m_domainComboBox.addItem(domain);
		}

		for (String[] categories: DashBoardUtils.m_server_levels) {
			serverLevelComboBox.addItem(categories);
			serverLevelComboBox.setItemCaption(categories, categories[0]+" - " + categories[1]);
		}

		for (String[] category: DashBoardUtils.m_server_managedby) {
			managedByComboBox.addItem(category[0]);
			managedByComboBox.setItemCaption(category[0], category[1]);
		}

		for (String prod: DashBoardUtils.m_server_prod) {
			prodComboBox.addItem(prod);
		}

		for (String notif: DashBoardUtils.m_server_notif) {
			notifComboBox.addItem(notif);
		}

		for (String option: DashBoardUtils.m_server_optional) {
			optionalComboBox.addItem(option);
		}

		tnComboBox.addItem("TrentinoNetwork");
		
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

		serverLevelComboBox.setInvalidAllowed(false);
		serverLevelComboBox.setNullSelectionAllowed(false);
		serverLevelComboBox.setRequired(true);
		serverLevelComboBox.setRequiredError("E' necessario scegliere una coppia di categorie");
		serverLevelComboBox.setImmediate(true);

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
	        	logger.info("vrf combo box value change:"+ m_domainComboBox.getValue());
		        try {
		            hostname.validate();
		            hostname.setComponentError(null); // MAGIC CODE HERE!!!
		        } catch (Exception e) {
		        	logger.info("not working validation");
		        }
		    }
		});
		m_domainComboBox.setImmediate(true);

				
		primary.setRequired(true);
		primary.setRequiredError("E' necessario specifica un indirizzo ip primario");
		primary.setImmediate(true);
		primary.addValidator(new IpValidator());

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
					String ip = (String)source.getContainerProperty(itemId, "ip").getValue();
					String service = (String)source.getContainerProperty(itemId, "service").getValue();
			        source.getContainerDataSource().removeItem(itemId);
			        m_editorFields.getItemDataSource().getBean().delService(ip,service);
					logger.info("Deleted Secondary ip/service: " + ip + "/" + service);
			      }
			    });
			 
			    return button;
			  }
		});

		m_secondaryIpTextBox.addValidator(new IpValidator());

		m_serviceComboBox.setNullSelectionAllowed(true);
		m_serviceComboBox.setInvalidAllowed(false);
		for (String service: DashBoardUtils.service_list)
			m_serviceComboBox.addItem(service);
		
		Button addSecondaryServiceButton = new Button("Aggiungi Servizio");
		addSecondaryServiceButton.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				if (m_secondaryIpTextBox.getValue() != null && m_serviceComboBox.getValue() != null) {
					String ip = m_secondaryIpTextBox.getValue().toString();
					String service = m_serviceComboBox.getValue().toString();
						IndexedContainer secondaryIpContainer = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
						Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
						ipItem.getItemProperty("ip").setValue(ip); 
						ipItem.getItemProperty("service").setValue(service); 
				        m_editorFields.getItemDataSource().getBean().addService(ip,service);
						logger.info("Added Secondary ip/service: " + ip + "/" + service);
				}
			}
		});

		managedByComboBox.setInvalidAllowed(false);
		managedByComboBox.setNullSelectionAllowed(false);
		managedByComboBox.setRequired(true);
		managedByComboBox.setRequiredError("E' necessario scegliere da chi e' gestito il server");

		prodComboBox.setInvalidAllowed(false);
		prodComboBox.setNullSelectionAllowed(false);
		prodComboBox.setRequired(true);
		prodComboBox.setRequiredError("E' necessario scegliere una categoria per la produzione");

		
		notifComboBox.setInvalidAllowed(false);
		notifComboBox.setNullSelectionAllowed(false);
		notifComboBox.setRequired(true);
		notifComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");

		optionalComboBox.setInvalidAllowed(false);
		optionalComboBox.setNullSelectionAllowed(false);

		tnComboBox.setInvalidAllowed(false);
		tnComboBox.setNullSelectionAllowed(false);
		tnComboBox.setRequired(true);
		tnComboBox.setRequiredError("E' necessario scegliere la categoria");

		snmpComboBox.setInvalidAllowed(false);
		snmpComboBox.setNullSelectionAllowed(false);
		snmpComboBox.setRequired(true);
		snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");

		TextField leaseexpires = new TextField("leaseExpires");
		leaseexpires.setWidth(8, Unit.CM);
		leaseexpires.setHeight(6, Unit.MM);

		TextField lease = new TextField("lease");
		lease.setWidth(8, Unit.CM);
		lease.setHeight(6, Unit.MM);

		TextField vendorPhone = new TextField("vendorPhone");
		vendorPhone.setWidth(8, Unit.CM);
		vendorPhone.setHeight(6, Unit.MM);

		TextField vendor = new TextField("vendor");
		vendor.setWidth(8, Unit.CM);
		vendor.setHeight(6, Unit.MM);

		TextField slot = new TextField("slot");
		slot.setWidth(8, Unit.CM);
		slot.setHeight(6, Unit.MM);

		TextField rack = new TextField("rack");
		rack.setWidth(8, Unit.CM);
		rack.setHeight(6, Unit.MM);

		TextField room = new TextField("room");
		room.setWidth(8, Unit.CM);
		room.setHeight(6, Unit.MM);
		TextField building = new TextField("Building");
		building.setWidth(8, Unit.CM);
		building.setHeight(6, Unit.MM);

		TextField city = new TextField("Citta'");
		city.setWidth(8, Unit.CM);
		city.setHeight(6, Unit.MM);
		
		TextField address = new TextField("Indirizzo");
		address.setWidth(8, Unit.CM);
		address.setHeight(6, Unit.MM);

		TextField operatingSystem = new TextField("operatingSystem");
		operatingSystem.setWidth(8, Unit.CM);
		operatingSystem.setHeight(6, Unit.MM);

		TextField dateInstalled = new TextField("dateInstalled");
		dateInstalled.setWidth(8, Unit.CM);
		dateInstalled.setHeight(6, Unit.MM);

		TextField assetNumber = new TextField("assetNumber");
		assetNumber.setWidth(8, Unit.CM);
		assetNumber.setHeight(6, Unit.MM);

		TextField serialNumber = new TextField("serialNumber");
		serialNumber.setWidth(8, Unit.CM);
		serialNumber.setHeight(6, Unit.MM);

		TextField category = new TextField("category");
		category.setWidth(8, Unit.CM);
		category.setHeight(6, Unit.MM);

		TextField modelNumber = new TextField("modelNumber");
		modelNumber.setWidth(8, Unit.CM);
		modelNumber.setHeight(6, Unit.MM);

		TextField manufacturer = new TextField("manufacturer");
		manufacturer.setWidth(8, Unit.CM);
		manufacturer.setHeight(6, Unit.MM);

		TextField description = new TextField("description");
		description.setWidth(8, Unit.CM);
		description.setHeight(6, Unit.MM);

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_descrComboBox, DashBoardUtils.DESCR);
		m_editorFields.bind(hostname, DashBoardUtils.HOST);
		m_editorFields.bind(serverLevelComboBox, DashBoardUtils.SERVER_LEVEL_CATEGORY);
		m_editorFields.bind(m_domainComboBox, DashBoardUtils.VRF);
		m_editorFields.bind(primary,DashBoardUtils.PRIMARY);
		m_editorFields.bind(snmpComboBox, DashBoardUtils.SNMP_PROFILE);
		m_editorFields.bind(prodComboBox, DashBoardUtils.SERVER_PROD_CATEGORY);
		m_editorFields.bind(notifComboBox, DashBoardUtils.NOTIF_CATEGORY);
		m_editorFields.bind(managedByComboBox, DashBoardUtils.SERVER_MANAGED_BY_CATEGORY);
		m_editorFields.bind(optionalComboBox, DashBoardUtils.SERVER_OPTIONAL_CATEGORY);
		m_editorFields.bind(tnComboBox, DashBoardUtils.SERVER_TN_CATEGORY);
		m_editorFields.bind(city,DashBoardUtils.CITY);
	    m_editorFields.bind(address, DashBoardUtils.ADDRESS1);
	    m_editorFields.bind(building, DashBoardUtils.BUILDING);
		m_editorFields.bind(leaseexpires,DashBoardUtils.LEASEEXPIRES);
		m_editorFields.bind(lease,DashBoardUtils.LEASE);
		m_editorFields.bind(vendorPhone,DashBoardUtils.VENDORPHONE);
		m_editorFields.bind(vendor,DashBoardUtils.VENDOR);
		m_editorFields.bind(slot,DashBoardUtils.SLOT);
		m_editorFields.bind(rack,DashBoardUtils.RACK);
		m_editorFields.bind(room,DashBoardUtils.ROOM);
		m_editorFields.bind(operatingSystem,DashBoardUtils.OPERATINGSYSTEM);
		m_editorFields.bind(dateInstalled,DashBoardUtils.DATEINSTALLED);
		m_editorFields.bind(assetNumber,DashBoardUtils.ASSETNUMBER);
		m_editorFields.bind(serialNumber,DashBoardUtils.SERIALNUMBER);
		m_editorFields.bind(category,DashBoardUtils.CATEGORY);
		m_editorFields.bind(modelNumber,DashBoardUtils.MODELNUMBER);
		m_editorFields.bind(manufacturer,DashBoardUtils.MANUFACTURER);
		m_editorFields.bind(description,DashBoardUtils.DESCRIPTION);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_descrComboBox);
		leftGeneralInfo.addComponent(hostname);
		leftGeneralInfo.addComponent(serverLevelComboBox);
		leftGeneralInfo.addComponent(m_domainComboBox);
		leftGeneralInfo.addComponent(primary);
		leftGeneralInfo.addComponent(dateInstalled);
		leftGeneralInfo.addComponent(assetNumber);
		leftGeneralInfo.addComponent(serialNumber);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);

		VerticalLayout bottomRightGeneralInfo = new VerticalLayout();
		HorizontalLayout bottomRightGeneralInfoTop = new HorizontalLayout();
		bottomRightGeneralInfoTop.addComponent(m_secondaryIpTextBox);
		bottomRightGeneralInfoTop.addComponent(m_serviceComboBox);
		bottomRightGeneralInfo.addComponent(bottomRightGeneralInfoTop);
		bottomRightGeneralInfo.addComponent(addSecondaryServiceButton);
		
		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.setMargin(true);
		rightGeneralInfo.addComponent(m_secondaryIpAddressTable);
		rightGeneralInfo.addComponent(bottomRightGeneralInfo);
				
		HorizontalLayout catLayout1 = new HorizontalLayout();
		catLayout1.setSizeFull();
		catLayout1.addComponent(snmpComboBox);
		HorizontalLayout catLayout2 = new HorizontalLayout();
		catLayout2.setSizeFull();
		catLayout2.addComponent(managedByComboBox);
		catLayout2.addComponent(prodComboBox);
		catLayout2.addComponent(notifComboBox);
		HorizontalLayout catLayout3 = new HorizontalLayout();
		catLayout3.setSizeFull();
		catLayout3.addComponent(tnComboBox);
		catLayout3.addComponent(optionalComboBox);
		
		
		HorizontalLayout assetLayout11 = new HorizontalLayout();
		assetLayout11.setSizeFull();
		assetLayout11.addComponent(leaseexpires);
		assetLayout11.addComponent(lease);
		HorizontalLayout assetLayout12 = new HorizontalLayout();
		assetLayout12.setSizeFull();
		assetLayout12.addComponent(vendorPhone);
		assetLayout12.addComponent(vendor);
		HorizontalLayout assetLayout21 = new HorizontalLayout();
		assetLayout21.setSizeFull();
		assetLayout21.addComponent(slot);
		assetLayout21.addComponent(rack);
		HorizontalLayout assetLayout22 = new HorizontalLayout();
		assetLayout22.setSizeFull();
		assetLayout22.addComponent(modelNumber);
		assetLayout22.addComponent(manufacturer);
		HorizontalLayout assetLayout31 = new HorizontalLayout();
		assetLayout31.setSizeFull();
		assetLayout31.addComponent(city);
		assetLayout31.addComponent(address);
		HorizontalLayout assetLayout32 = new HorizontalLayout();
		assetLayout32.setSizeFull();
		assetLayout32.addComponent(building);
		assetLayout32.addComponent(room);
		HorizontalLayout assetLayout4 = new HorizontalLayout();
		assetLayout4.setSizeFull();
		assetLayout4.addComponent(category);
		assetLayout4.addComponent(operatingSystem);
		HorizontalLayout assetLayout5 = new HorizontalLayout();
		assetLayout5.setSizeFull();
		assetLayout5.addComponent(description);
				
		HorizontalLayout generalInfo = new HorizontalLayout();
		generalInfo.addComponent(leftGeneralInfo);
		generalInfo.addComponent(centerGeneralInfo);
		generalInfo.addComponent(rightGeneralInfo);
		generalInfo.setExpandRatio(leftGeneralInfo, 3);
		generalInfo.setExpandRatio(centerGeneralInfo, 1);
		generalInfo.setExpandRatio(rightGeneralInfo, 3);

				
		FormLayout profileInfo = new FormLayout();
		profileInfo.addComponent(new Label("Dati Asset e Profili"));
		profileInfo.addComponent(catLayout1);
		profileInfo.addComponent(catLayout2);
		profileInfo.addComponent(catLayout3);
		profileInfo.addComponent(assetLayout11);
		profileInfo.addComponent(assetLayout12);
		profileInfo.addComponent(assetLayout21);
		profileInfo.addComponent(assetLayout22);
		profileInfo.addComponent(assetLayout31);
		profileInfo.addComponent(assetLayout32);
		profileInfo.addComponent(assetLayout4);
		profileInfo.addComponent(assetLayout5);

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
				BeanItem<SistemiInformativiNode> bean = 
						m_requisitionContainer.addBeanAt(0,
								new SistemiInformativiNode("notSavedHost"+newHost++));
				serverLevelSearchComboBox.select(null);
				serverLevelSearchComboBox.setValue(null);
				managedBySearchComboBox.select(null);
				managedBySearchComboBox.setValue(null);
				notifSearchComboBox.select(null);
				notifSearchComboBox.setValue(null);
				optionalSearchComboBox.select(null);
				optionalSearchComboBox.setValue(null);
				m_searchText="";
				searchField.setValue(m_searchText);
				m_requisitionTable.select(bean.getBean().getNodeLabel());
				selectItem();
				m_requisitionContainer.removeAllContainerFilters();
				hostname.focus();
			}
		});
		
		m_syncRequisButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					getService().sync(DashBoardUtils.SI_REQU_NAME);
					logger.info("Sync succeed foreing source: " + DashBoardUtils.SI_REQU_NAME);
					Notification.show("Sync " + DashBoardUtils.SI_REQU_NAME, "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
				} catch (Exception e) {
					logger.warning("Sync Failed foreign source: " + DashBoardUtils.SI_REQU_NAME + " " + e.getLocalizedMessage());
					Notification.show("Sync Failed foreign source" + DashBoardUtils.SI_REQU_NAME, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
				}
				
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
					SistemiInformativiNode node = m_editorFields.getItemDataSource().getBean();
					if (node.getForeignId() == null) {
						node.setForeignId(node.getHostname());
						node.setValid(true);
						getService().addSINode(node);
						logger.info("Added: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
						Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
					} else {
						getService().updateSINode(node);
						logger.info("Updated: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
						Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Updated", Type.HUMANIZED_MESSAGE);
					}
					m_requisitionContainer.addContainerFilter(new NodeFilter(node.getHostname(), null,null, null, null,null));
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
				BeanItem<SistemiInformativiNode> node = m_editorFields.getItemDataSource();
				logger.info("Deleting: " + node.getBean().getNodeLabel());
				if (node.getBean().getForeignId() !=  null) {
					try {
						getService().deleteNode(node.getBean());
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
			Notification.show("Found Duplicated NodeLabel",  Arrays.toString(duplicatednodeLabels.toArray()), Type.ERROR_MESSAGE);
		}

		Set<String> duplicatedForeignIds= getService().checkUniqueForeignId();
		if (!duplicatedForeignIds.isEmpty()) {
			logger.warning(" Found Duplicated ForeignId: " + Arrays.toString(duplicatedForeignIds.toArray()));
			Notification.show("Found Duplicated ForeignId",  Arrays.toString(duplicatedForeignIds.toArray()), Type.ERROR_MESSAGE);
		}

		Set<String> duplicatedPrimaries= getService().checkUniquePrimary();
		if (!duplicatedPrimaries.isEmpty()) {
			logger.warning(" Found Duplicated Primary IP: " + Arrays.toString(duplicatedPrimaries.toArray()));
			Notification.show("Found Duplicated Primary IP",  Arrays.toString(duplicatedPrimaries.toArray()), Type.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("unchecked")
	private void selectItem() {
		Object contactId = m_requisitionTable.getValue();

		if (contactId != null) {
			SistemiInformativiNode node = ((BeanItem<SistemiInformativiNode>)m_requisitionTable
				.getItem(contactId)).getBean();
			
			m_descrComboBox.removeAllItems();
			if (node.getDescr() != null)
				m_descrComboBox.addItem(node.getDescr());
						
			IndexedContainer secondaryIpContainer = new IndexedContainer();
			secondaryIpContainer.addContainerProperty("ip",      String.class, null);
			secondaryIpContainer.addContainerProperty("service", String.class, null);
			if (node.getServiceMap() != null) {
				for (String ip: node.getServiceMap().keySet()) {
					for (String service: node.getServiceMap().get(ip)) {
						Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
						ipItem.getItemProperty("ip").setValue(ip); 
						ipItem.getItemProperty("service").setValue(service); 
					}
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
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
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
			SistemiInformativiNode node = m_editorFields.getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = (String)value;
			logger.info("DuplicatedForeignIdValidator: validating foreignId: " + hostname);
	         if (getService().hasDuplicatedForeignId((hostname)))
	             throw new InvalidValueException("The hostname exists: cannot duplicate hostname: " + hostname);
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
			SistemiInformativiNode node = m_editorFields.getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
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
	
	@Override
	public String getName() {
		return "SITab";
	}


}
