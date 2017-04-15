package org.opennms.vaadin.provision.dashboard;


import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.SistemiInformativiNode;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
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
public class SistemiInformativiTab extends RequisitionTab {

	private class NodeFilter extends RequisitionNodeFilter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String   needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		private String   needle4=null;
		private String   needle5=null;
		private String   needle6=null;
		
		public NodeFilter(Object o, Object c1, Object c3,Object c4, Object c5, Object c6) {
			super(o);
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
			return ( super.passesFilter(itemId, item) 
					&& ( needle1 == null || needle1.equals(node.getServerLevelCategory()[0]) )
					&& ( needle2 == null || needle2.equals(node.getServerLevelCategory()[1]) ) 
		            && ( needle3 == null || needle3.equals(node.getManagedByCategory()) ) 
		            && ( needle4 == null || needle4.equals(node.getNotifCategory()) ) 
		            && ( needle5 == null || needle5.equals(node.getOptionalCategory()) ) 
		            && ( needle6 == null || needle6.equals(node.getProdCategory()) ) 
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

	private BeanFieldGroup<SistemiInformativiNode> m_editorFields     = new BeanFieldGroup<SistemiInformativiNode>(SistemiInformativiNode.class);
	Integer newHost = 0;
	
	private ComboBox m_serverLevelSearchComboBox = new ComboBox("Select Server Type");
	private ComboBox m_managedBySearchComboBox  = new ComboBox("Select Server Managed by Group");
	private ComboBox m_prodSearchComboBox      = new ComboBox("Select Production Category");
	private ComboBox m_notifSearchComboBox      = new ComboBox("Select Notification Category");
	private ComboBox m_optionalSearchComboBox   = new ComboBox("Select Optional Category");
	private TextField m_searchField       = new TextField("Type Label Text");

	private ComboBox m_serverLevelComboBox = new ComboBox("Server Level Category");
	private ComboBox m_managedByComboBox  = new ComboBox("Managed by Category");
	private ComboBox m_prodComboBox  = new ComboBox("Production Category");
	private ComboBox m_notifComboBox  = new ComboBox("Notification Category");
	private OptionGroup m_optionalGroup  = new OptionGroup("Optional Category");
	private ComboBox m_tnComboBox  = new ComboBox("TN Category");
	private ComboBox m_serviceComboBox = new ComboBox("Servizi");
	
	private TextField m_leaseexpires = new TextField("leaseExpires");
	private TextField m_lease = new TextField("lease");
	private TextField m_vendorPhone = new TextField("vendorPhone");
	private TextField m_vendor = new TextField("vendor");
	private TextField m_slot = new TextField("slot");
	private TextField m_rack = new TextField("rack");
	private TextField m_room = new TextField("room");
	private TextField m_operatingSystem = new TextField("operatingSystem");
	private TextField m_dateInstalled = new TextField("dateInstalled");
	private TextField m_assetNumber = new TextField("assetNumber");
	private TextField m_serialNumber = new TextField("serialNumber");
	private TextField m_category = new TextField("category");
	private TextField m_modelNumber = new TextField("modelNumber");
	private TextField m_manufacturer = new TextField("manufacturer");
	private TextField m_description = new TextField("description");
	
	private TextField m_secondaryIpTextBox = new TextField("Aggiungi indirizzo ip");
	private Table m_secondaryIpAddressTable = new Table();
	Button m_addSecondaryServiceButton = new Button("Aggiungi Servizio");

	public SistemiInformativiTab() {
		super();
		
		for (String[] serverLevel: DashBoardUtils.m_server_levels) {
			m_serverLevelSearchComboBox.addItem(serverLevel);
			m_serverLevelSearchComboBox.setItemCaption(serverLevel, serverLevel[0] + " - " + serverLevel[1]);
		}
		m_serverLevelSearchComboBox.setInvalidAllowed(false);
		m_serverLevelSearchComboBox.setNullSelectionAllowed(true);		
		m_serverLevelSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_serverLevelSearchComboBox);
		

		for (String category: DashBoardUtils.m_server_managedby) {
			m_managedBySearchComboBox.addItem(category);
		}
		m_managedBySearchComboBox.setInvalidAllowed(false);
		m_managedBySearchComboBox.setNullSelectionAllowed(true);		
		m_managedBySearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_managedBySearchComboBox);

		for (String category: DashBoardUtils.m_server_prod) {
			m_prodSearchComboBox.addItem(category);
		}
		m_prodSearchComboBox.setInvalidAllowed(false);
		m_prodSearchComboBox.setNullSelectionAllowed(true);		
		m_prodSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_prodSearchComboBox);
		
		for (String category: DashBoardUtils.m_server_notif) {
			m_notifSearchComboBox.addItem(category);
		}
		m_notifSearchComboBox.setInvalidAllowed(false);
		m_notifSearchComboBox.setNullSelectionAllowed(true);		
		m_notifSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_notifSearchComboBox);
		
		for (String category: DashBoardUtils.m_server_optional) {
			m_optionalSearchComboBox.addItem(category);
		}
		m_optionalSearchComboBox.setInvalidAllowed(false);
		m_optionalSearchComboBox.setNullSelectionAllowed(true);		
		m_optionalSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_optionalSearchComboBox);

		m_searchField.setInputPrompt("Search nodes");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		addSearchValueChangeListener(m_searchField);
		
		for (String[] categories: DashBoardUtils.m_server_levels) {
			m_serverLevelComboBox.addItem(categories);
			m_serverLevelComboBox.setItemCaption(categories, categories[0]+" - " + categories[1]);
		}
		m_serverLevelComboBox.setInvalidAllowed(false);
		m_serverLevelComboBox.setNullSelectionAllowed(false);
		m_serverLevelComboBox.setRequired(true);
		m_serverLevelComboBox.setRequiredError("E' necessario scegliere una coppia di categorie");
		m_serverLevelComboBox.setImmediate(true);

		for (String category: DashBoardUtils.m_server_managedby) {
			m_managedByComboBox.addItem(category);
		}
		m_managedByComboBox.setInvalidAllowed(false);
		m_managedByComboBox.setNullSelectionAllowed(false);
		m_managedByComboBox.setRequired(true);
		m_managedByComboBox.setRequiredError("E' necessario scegliere da chi e' gestito il server");

		for (String prod: DashBoardUtils.m_server_prod) {
			m_prodComboBox.addItem(prod);
		}
		m_prodComboBox.setInvalidAllowed(false);
		m_prodComboBox.setNullSelectionAllowed(false);
		m_prodComboBox.setRequired(true);
		m_prodComboBox.setRequiredError("E' necessario scegliere una categoria per la produzione");

		for (String notif: DashBoardUtils.m_server_notif) {
			m_notifComboBox.addItem(notif);
		}
		m_notifComboBox.setInvalidAllowed(false);
		m_notifComboBox.setNullSelectionAllowed(true);
//		notifComboBox.setRequired(true);
//		notifComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");


		for (String option: DashBoardUtils.m_server_optional) {
			m_optionalGroup.addItem(option);
		}
		m_optionalGroup.setInvalidAllowed(false);
		m_optionalGroup.setNullSelectionAllowed(true);
		m_optionalGroup.setImmediate(true);
		m_optionalGroup.setMultiSelect(true);
		

		m_tnComboBox.addItem("TrentinoNetwork");		
		m_tnComboBox.setInvalidAllowed(false);
		m_tnComboBox.setNullSelectionAllowed(false);
		m_tnComboBox.setRequired(true);
		m_tnComboBox.setRequiredError("E' necessario scegliere la categoria");

		m_leaseexpires.setWidth(8, Unit.CM);
		m_leaseexpires.setHeight(6, Unit.MM);

		m_lease.setWidth(8, Unit.CM);
		m_lease.setHeight(6, Unit.MM);

		m_vendorPhone.setWidth(8, Unit.CM);
		m_vendorPhone.setHeight(6, Unit.MM);

		m_vendor.setWidth(8, Unit.CM);
		m_vendor.setHeight(6, Unit.MM);

		m_slot.setWidth(8, Unit.CM);
		m_slot.setHeight(6, Unit.MM);

		m_rack.setWidth(8, Unit.CM);
		m_rack.setHeight(6, Unit.MM);

		m_room.setWidth(8, Unit.CM);
		m_room.setHeight(6, Unit.MM);

		m_operatingSystem.setWidth(8, Unit.CM);
		m_operatingSystem.setHeight(6, Unit.MM);

		m_dateInstalled.setWidth(8, Unit.CM);
		m_dateInstalled.setHeight(6, Unit.MM);

		m_assetNumber.setWidth(8, Unit.CM);
		m_assetNumber.setHeight(6, Unit.MM);

		m_serialNumber.setWidth(8, Unit.CM);
		m_serialNumber.setHeight(6, Unit.MM);

		m_category.setWidth(8, Unit.CM);
		m_category.setHeight(6, Unit.MM);

		m_modelNumber.setWidth(8, Unit.CM);
		m_modelNumber.setHeight(6, Unit.MM);

		m_manufacturer.setWidth(8, Unit.CM);
		m_manufacturer.setHeight(6, Unit.MM);

		m_description.setWidth(8, Unit.CM);
		m_description.setHeight(6, Unit.MM);

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
					BasicService service = (BasicService)source.getValue();
			        source.getContainerDataSource().removeItem(itemId);
			        m_editorFields.getItemDataSource().getBean().delService(service);
					logger.info("Deleted Secondary ip/service: " + service.getIp() + "/" + service.getService());
			      }
			    });
			 
			    return button;
			  }
		});
		m_serviceComboBox.setNullSelectionAllowed(true);
		m_serviceComboBox.setInvalidAllowed(false);
		for (String onmsservice: DashBoardUtils.service_list)
			m_serviceComboBox.addItem(onmsservice);
		
		m_addSecondaryServiceButton.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				if (m_secondaryIpTextBox.getValue() != null && m_serviceComboBox.getValue() != null) {
					String ip = m_secondaryIpTextBox.getValue();
					if (DashBoardUtils.hasInvalidIp(ip)) {
						Notification.show("Add secondary ip/service, Failed", "Invalid Ip address: "+ip, Type.WARNING_MESSAGE);
						logger.warning("Added Secondary ip/service Failed: Invalid ip address" + ip);
						return;
					}
					String service = m_serviceComboBox.getValue().toString();
					IndexedContainer secondaryIpContainer = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
					Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
					ipItem.getItemProperty("ip").setValue(ip); 
					ipItem.getItemProperty("service").setValue(service); 
					BasicInterface bip = new BasicInterface();
					bip.setDescr(DashBoardUtils.DESCR_TNPD);
					bip.setIp(ip);
					bip.setOnmsprimary(OnmsPrimary.N);
					BasicService bs = new BasicService(bip);
					bs.setService(service);
			        m_editorFields.getItemDataSource().getBean().addService(bs);
					logger.info("Added Secondary ip/service: " + ip + "/" + service);
				}
			}
		});


	}

	@Override
	public void load() {
		if (!loaded) {
			try {
				m_requisitionContainer = getService().getSIContainer();
				getRequisitionTable().setContainerDataSource(m_requisitionContainer);
				getRequisitionTable().setVisibleColumns(new Object[] { DashBoardUtils.LABEL,DashBoardUtils.VALID });
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
			m_editorFields.setBuffered(true);
			m_editorFields.bind(getDescrComboBox(), DashBoardUtils.DESCR);
			m_editorFields.bind(getHostNameTextField(), DashBoardUtils.HOST);
			m_editorFields.bind(m_serverLevelComboBox, DashBoardUtils.SERVER_LEVEL_CATEGORY);
			m_editorFields.bind(getDomainComboBox(), DashBoardUtils.CAT);
			m_editorFields.bind(getPrimaryTextField(),DashBoardUtils.PRIMARY);
			m_editorFields.bind(getSnmpComboBox(), DashBoardUtils.SNMP_PROFILE);
			m_editorFields.bind(m_prodComboBox, DashBoardUtils.SERVER_PROD_CATEGORY);
			m_editorFields.bind(m_notifComboBox, DashBoardUtils.NOTIF_CATEGORY);
			m_editorFields.bind(m_managedByComboBox, DashBoardUtils.SERVER_MANAGED_BY_CATEGORY);
			m_editorFields.bind(m_optionalGroup, DashBoardUtils.SERVER_OPTIONAL_CATEGORY);
			m_editorFields.bind(m_tnComboBox, DashBoardUtils.SERVER_TN_CATEGORY);
			m_editorFields.bind(getCityTextField(),DashBoardUtils.CITY);
		    m_editorFields.bind(getAddressTextField(), DashBoardUtils.ADDRESS1);
		    m_editorFields.bind(getBuildingTextField(), DashBoardUtils.BUILDING);
			m_editorFields.bind(m_leaseexpires,DashBoardUtils.LEASEEXPIRES);
			m_editorFields.bind(m_lease,DashBoardUtils.LEASE);
			m_editorFields.bind(m_vendorPhone,DashBoardUtils.VENDORPHONE);
			m_editorFields.bind(m_vendor,DashBoardUtils.VENDOR);
			m_editorFields.bind(m_slot,DashBoardUtils.SLOT);
			m_editorFields.bind(m_rack,DashBoardUtils.RACK);
			m_editorFields.bind(m_room,DashBoardUtils.ROOM);
			m_editorFields.bind(m_operatingSystem,DashBoardUtils.OPERATINGSYSTEM);
			m_editorFields.bind(m_dateInstalled,DashBoardUtils.DATEINSTALLED);
			m_editorFields.bind(m_assetNumber,DashBoardUtils.ASSETNUMBER);
			m_editorFields.bind(m_serialNumber,DashBoardUtils.SERIALNUMBER);
			m_editorFields.bind(m_category,DashBoardUtils.CATEGORY);
			m_editorFields.bind(m_modelNumber,DashBoardUtils.MODELNUMBER);
			m_editorFields.bind(m_manufacturer,DashBoardUtils.MANUFACTURER);
			m_editorFields.bind(m_description,DashBoardUtils.DESCRIPTION);
		}
		super.load();
	}
	
	private void layout() {

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(m_serverLevelSearchComboBox);
		searchlayout.addComponent(m_managedBySearchComboBox);
		searchlayout.addComponent(m_prodSearchComboBox);
		searchlayout.addComponent(m_notifSearchComboBox);
		searchlayout.addComponent(m_optionalSearchComboBox);

		m_searchField.setWidth("80%");
		searchlayout.addComponent(m_searchField);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));

		getLeft().addComponent(new Panel("Search",searchlayout));
		getLeft().addComponent(new Panel(getRequisitionTable()));
		getLeft().addComponent(bottomLeftLayout);
		getLeft().setSizeFull();

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(getDescrComboBox());
		leftGeneralInfo.addComponent(getHostNameTextField());
		leftGeneralInfo.addComponent(m_serverLevelComboBox);
		leftGeneralInfo.addComponent(getDomainComboBox());
		leftGeneralInfo.addComponent(getPrimaryTextField());
		leftGeneralInfo.addComponent(m_dateInstalled);
		leftGeneralInfo.addComponent(m_assetNumber);
		leftGeneralInfo.addComponent(m_serialNumber);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);

		VerticalLayout bottomRightGeneralInfo = new VerticalLayout();
		HorizontalLayout bottomRightGeneralInfoTop = new HorizontalLayout();
		bottomRightGeneralInfoTop.addComponent(m_secondaryIpTextBox);
		bottomRightGeneralInfoTop.addComponent(m_serviceComboBox);
		bottomRightGeneralInfo.addComponent(bottomRightGeneralInfoTop);
		bottomRightGeneralInfo.addComponent(m_addSecondaryServiceButton);
		
		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.setMargin(true);
		rightGeneralInfo.addComponent(m_secondaryIpAddressTable);
		rightGeneralInfo.addComponent(bottomRightGeneralInfo);
				
		HorizontalLayout catLayout1 = new HorizontalLayout();
		catLayout1.setSizeFull();
		catLayout1.addComponent(getSnmpComboBox());
		HorizontalLayout catLayout2 = new HorizontalLayout();
		catLayout2.setSizeFull();
		catLayout2.addComponent(m_managedByComboBox);
		catLayout2.addComponent(m_prodComboBox);
		HorizontalLayout catLayout3 = new HorizontalLayout();
		catLayout3.setSizeFull();
		catLayout3.addComponent(m_tnComboBox);
		catLayout3.addComponent(m_notifComboBox);
		
		HorizontalLayout catLayout4 = new HorizontalLayout();
		catLayout4.setSizeFull();
		catLayout4.addComponent(m_optionalGroup);
		
		
		HorizontalLayout assetLayout11 = new HorizontalLayout();
		assetLayout11.setSizeFull();
		assetLayout11.addComponent(m_leaseexpires);
		assetLayout11.addComponent(m_lease);
		
		HorizontalLayout assetLayout12 = new HorizontalLayout();
		assetLayout12.setSizeFull();
		assetLayout12.addComponent(m_vendorPhone);
		assetLayout12.addComponent(m_vendor);
		
		HorizontalLayout assetLayout21 = new HorizontalLayout();
		assetLayout21.setSizeFull();
		assetLayout21.addComponent(m_slot);
		assetLayout21.addComponent(m_rack);
		
		HorizontalLayout assetLayout22 = new HorizontalLayout();
		assetLayout22.setSizeFull();
		assetLayout22.addComponent(m_modelNumber);
		assetLayout22.addComponent(m_manufacturer);
		
		HorizontalLayout assetLayout31 = new HorizontalLayout();
		assetLayout31.setSizeFull();
		assetLayout31.addComponent(getCityTextField());
		assetLayout31.addComponent(getAddressTextField());
		
		HorizontalLayout assetLayout32 = new HorizontalLayout();
		assetLayout32.setSizeFull();
		assetLayout32.addComponent(getBuildingTextField());
		assetLayout32.addComponent(m_room);
		
		HorizontalLayout assetLayout4 = new HorizontalLayout();
		assetLayout4.setSizeFull();
		assetLayout4.addComponent(m_category);
		assetLayout4.addComponent(m_operatingSystem);
		HorizontalLayout assetLayout5 = new HorizontalLayout();
		assetLayout5.setSizeFull();
		assetLayout5.addComponent(m_description);
				
		HorizontalLayout generalInfo = new HorizontalLayout();
		generalInfo.addComponent(leftGeneralInfo);
		generalInfo.addComponent(centerGeneralInfo);
		generalInfo.addComponent(rightGeneralInfo);
		generalInfo.setExpandRatio(leftGeneralInfo, 3);
		generalInfo.setExpandRatio(centerGeneralInfo, 1);
		generalInfo.setExpandRatio(rightGeneralInfo, 3);

				
		FormLayout profileInfo = new FormLayout();
		profileInfo.addComponent(new Label("Profili"));
		profileInfo.addComponent(catLayout1);
		profileInfo.addComponent(catLayout2);
		profileInfo.addComponent(catLayout3);
		
		FormLayout optionCat = new FormLayout();
		optionCat.addComponent(new Label("Option Categories"));
		optionCat.addComponent(catLayout4);
		
		FormLayout assetInfo = new FormLayout();
		assetInfo.addComponent(new Label("Asset Data"));

		assetInfo.addComponent(assetLayout11);
		assetInfo.addComponent(assetLayout12);
		assetInfo.addComponent(assetLayout21);
		assetInfo.addComponent(assetLayout22);
		assetInfo.addComponent(assetLayout31);
		assetInfo.addComponent(assetLayout32);
		assetInfo.addComponent(assetLayout4);
		assetInfo.addComponent(assetLayout5);

		getRight().setMargin(true);
		getRight().setVisible(false);
		getRight().addComponent(new Panel(generalInfo));
		getRight().addComponent(new Panel(profileInfo));
		getRight().addComponent(new Panel(optionCat));
		getRight().addComponent(new Panel(assetInfo));
						
	}
		
	@Override
	public String getName() {
		return "SITab";
	}

	@Override
	public void selectItem(BasicNode node) {
		BeanItemContainer<BasicService> secondaryIpContainer = new BeanItemContainer<BasicService>(BasicService.class);
		if (node.getServiceMap() != null) {
			for (BasicInterface ip: node.getServiceMap().keySet()) {
				for (String service: node.getServiceMap().get(ip)) {
					if (ip.getIp().equals(node.getPrimary()) && service.equals("ICMP"))
						continue;
					BasicService bs = new BasicService(ip);
					bs.setService(service);
					secondaryIpContainer.addBean(bs);
				}
			}
		}
		m_secondaryIpAddressTable.setContainerDataSource(secondaryIpContainer);
		m_editorFields.setItemDataSource((SistemiInformativiNode)node);

	}

	public String getRequisitionName() {
		return DashBoardUtils.SI_REQU_NAME;
	}

	@Override
	public void cleanSearchBox() {
		m_serverLevelSearchComboBox.select(null);
		m_serverLevelSearchComboBox.setValue(null);
		m_managedBySearchComboBox.select(null);
		m_managedBySearchComboBox.setValue(null);
		m_notifSearchComboBox.select(null);
		m_notifSearchComboBox.setValue(null);
		m_optionalSearchComboBox.select(null);
		m_optionalSearchComboBox.setValue(null);
		m_searchText="";
		m_searchField.setValue(m_searchText);		
	}

	@Override
	public BeanContainer<String, ? extends BasicNode> getRequisitionContainer() {
		return m_requisitionContainer;
	}

	@Override
	public SistemiInformativiNode addBean() {
		return 
				m_requisitionContainer.addBeanAt(0,
						new SistemiInformativiNode("notSavedHost"+newHost++,DashBoardUtils.SI_REQU_NAME)).getBean();
	}

	@Override
	public BeanFieldGroup<SistemiInformativiNode> getBeanFieldGroup() {
		return m_editorFields;
	}

	private void addSearchValueChangeListener(ComboBox box) {
		box.addValueChangeListener(new Property.ValueChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(
						new NodeFilter(m_searchText,
								m_serverLevelSearchComboBox.getValue(),
								m_managedBySearchComboBox.getValue(),
								m_notifSearchComboBox.getValue(),
								m_optionalSearchComboBox.getValue(),
								m_prodSearchComboBox.getValue()));
			}
		});
		
	}
	
	private void addSearchValueChangeListener(TextField box) {
		m_searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event) {
				m_searchText = event.getText();
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(
						m_searchText, m_serverLevelSearchComboBox.getValue(),
						m_managedBySearchComboBox.getValue(),
						m_notifSearchComboBox.getValue(),
						m_optionalSearchComboBox.getValue(),
						m_prodSearchComboBox.getValue()));

			}
		});
	}
	
}
