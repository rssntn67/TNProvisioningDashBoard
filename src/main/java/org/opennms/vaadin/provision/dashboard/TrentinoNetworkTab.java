package org.opennms.vaadin.provision.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.Categoria;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;

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
import com.vaadin.ui.OptionGroup;
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
public class TrentinoNetworkTab extends RequisitionTab {

	private class NodeFilter extends RequisitionNodeFilter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Categoria needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		private String   needle4=null;
		
		public NodeFilter(Object o, Object c1, Object c2,Object c3, Object c4) {
			super(o);
			if ( c1 != null) 
				needle1 = (Categoria) c1;
			if ( c2 != null)
				needle2 = (String) c2;
			if ( c3 != null)
				needle3 = (String) c3;
			if ( c4 != null)
				needle4 = (String) c4;
		}

		@SuppressWarnings("unchecked")
		public boolean passesFilter(Object itemId, Item item) {
			TrentinoNetworkNode node = ((BeanItem<TrentinoNetworkNode>)item).getBean();
			return ( 	super.passesFilter(itemId, item) 
					&& ( needle1 == null || needle1.equals(node.getNetworkCategory()) ) 
					&& ( needle2 == null || needle2.equals(node.getNotifCategory()) )
		            && ( needle3 == null || needle3.equals(node.getThreshCategory()) ) 
		            && ( needle4 == null || needle4.equals(node.getSlaCategory()) ) 
					);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private static final long serialVersionUID = -5948892618258879832L;

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private String m_searchText = null;
	private boolean loaded=false;

	private BeanContainer<String, TrentinoNetworkNode> m_requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
	private BeanFieldGroup<TrentinoNetworkNode> m_editorFields     = new BeanFieldGroup<TrentinoNetworkNode>(TrentinoNetworkNode.class);
	Integer newHost = 0;
	
	private ComboBox m_networkCatSearchComboBox = new ComboBox("Select Network Category");
	private ComboBox m_notifCatSearchComboBox   = new ComboBox("Select Notification Category");
	private ComboBox m_threshCatSearchComboBox  = new ComboBox("Select Threshold Category");
	private ComboBox m_slaCatSearchComboBox  = new ComboBox("Select Service Report Category");
	private TextField m_searchField       = new TextField("Type Label Text or Ip");

	private ComboBox m_secondaryIpComboBox = new ComboBox("Seleziona indirizzo ip");
	private Table m_secondaryIpAddressTable = new Table();

	private ComboBox m_networkCatComboBox = new ComboBox("Network Category");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Category");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Category");
	private ComboBox m_slaCatComboBox  = new ComboBox("Service Level Report Category");
	
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");
	private OptionGroup m_optionalGroup  = new OptionGroup("Optional Category");
	TextField m_circuiId = new TextField("circuitId");
	Button m_addSecondaryIpButton = new Button("Aggiungi indirizzo ip");


	public TrentinoNetworkTab() {
		super();

		m_circuiId.setWidth(8, Unit.CM);
		m_circuiId.setHeight(6, Unit.MM);

		m_networkCatSearchComboBox.setInvalidAllowed(false);
		m_networkCatSearchComboBox.setNullSelectionAllowed(true);		
		m_networkCatSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_networkCatSearchComboBox);

		for (String category: DashBoardUtils.m_notify_levels) {
			m_notifCatSearchComboBox.addItem(category);
		}
		m_notifCatSearchComboBox.setInvalidAllowed(false);
		m_notifCatSearchComboBox.setNullSelectionAllowed(true);		
		m_notifCatSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_notifCatSearchComboBox);

		for (String category: DashBoardUtils.m_threshold_levels) {
			m_threshCatSearchComboBox.addItem(category);
		}
		m_threshCatSearchComboBox.setInvalidAllowed(false);
		m_threshCatSearchComboBox.setNullSelectionAllowed(true);		
		m_threshCatSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_threshCatSearchComboBox);

		for (String sla: DashBoardUtils.m_sla_levels) {
			m_slaCatSearchComboBox.addItem(sla);
		}
		m_slaCatSearchComboBox.setInvalidAllowed(false);
		m_slaCatSearchComboBox.setNullSelectionAllowed(true);		
		m_slaCatSearchComboBox.setImmediate(true);
		addSearchValueChangeListener(m_slaCatSearchComboBox);

		m_searchField.setInputPrompt("Search nodes");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		addSearchValueChangeListener(m_searchField);

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
					getDomainComboBox().select(((Categoria)m_networkCatComboBox.getValue()).getDnsdomain());
					m_notifCatComboBox.select(((Categoria)m_networkCatComboBox.getValue()).getNotifylevel());
					m_threshCatComboBox.select(((Categoria)m_networkCatComboBox.getValue()).getThresholdlevel());
					m_backupComboBox.select(((Categoria)m_networkCatComboBox.getValue()).getBackupprofile());
					getSnmpComboBox().select(((Categoria)m_networkCatComboBox.getValue()).getSnmpprofile());
				}
			}
		});

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
					BasicService ip = (BasicService) source.getValue();
			        source.getContainerDataSource().removeItem(itemId);
					if (ip == null) {
						logger.info("Null Secondary ip: " + ip);
						return;
					}
			        m_editorFields.getItemDataSource().getBean().delService(ip);
			        m_secondaryIpComboBox.addItem(ip.getInterface().getIp());
					logger.info("Deleted Secondary Service: " + ip);
			      }
			    });
			 
			    return button;
			  }
		});

		m_secondaryIpComboBox.setNullSelectionAllowed(false);
		m_secondaryIpComboBox.setInvalidAllowed(false);
		m_addSecondaryIpButton.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				if (m_secondaryIpComboBox.getValue() != null) {
					String ip = m_secondaryIpComboBox.getValue().toString();
					BeanItemContainer<BasicService> secondaryIpContainer = (BeanItemContainer<BasicService>)m_secondaryIpAddressTable.getContainerDataSource();
					BasicInterface bip = new BasicInterface();
					bip.setDescr(DashBoardUtils.DESCR_TNPD);
					bip.setIp(ip);
					bip.setOnmsprimary(OnmsPrimary.N);
					BasicService bs = new BasicService(bip);
					bs.setService("ICMP");
					secondaryIpContainer.addBean(bs);
			        m_editorFields.getItemDataSource().getBean().addService(bs);
					logger.info("Added Secondary ip address: " + ip);
			        m_secondaryIpComboBox.removeItem(ip);
				}
			}
		});

		for (String notif: DashBoardUtils.m_notify_levels) {
			m_notifCatComboBox.addItem(notif);
		}
		m_notifCatComboBox.setInvalidAllowed(false);
		m_notifCatComboBox.setNullSelectionAllowed(false);
		m_notifCatComboBox.setRequired(true);
		m_notifCatComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");

		for (String threshold: DashBoardUtils.m_threshold_levels) {
			m_threshCatComboBox.addItem(threshold);
		}
		m_threshCatComboBox.setInvalidAllowed(false);
		m_threshCatComboBox.setNullSelectionAllowed(false);
		m_threshCatComboBox.setRequired(true);
		m_threshCatComboBox.setRequiredError("E' necessario scegliere una categoria per le threshold");

		for (String sla: DashBoardUtils.m_sla_levels) {
			m_slaCatComboBox.addItem(sla);
		}		
		m_slaCatComboBox.setInvalidAllowed(false);
		m_slaCatComboBox.setNullSelectionAllowed(true);
		m_slaCatComboBox.setRequired(false);

        m_backupComboBox.setInvalidAllowed(false);
        m_backupComboBox.setNullSelectionAllowed(false);
        m_backupComboBox.setRequired(true);
        m_backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");

		for (String option: DashBoardUtils.m_server_optional) {
			m_optionalGroup.addItem(option);
		}
		m_optionalGroup.setInvalidAllowed(false);
		m_optionalGroup.setNullSelectionAllowed(true);
		m_optionalGroup.setImmediate(true);
		m_optionalGroup.setMultiSelect(true);
	}

	@Override
	public void load() {
		if (!loaded) {
			try {
				m_requisitionContainer = getService().getTNContainer();
				getRequisitionTable().setContainerDataSource(m_requisitionContainer);
				getRequisitionTable().setVisibleColumns(new Object[] { DashBoardUtils.LABEL,DashBoardUtils.VALID });
				layout();
				loaded=true;
			} catch (UniformInterfaceException e) {
				logger.info("Response Status:" + e.getResponse().getStatus() + " Reason: "+e.getResponse().getStatusInfo().getReasonPhrase());
				if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
					logger.info("No Requisition Found: "+e.getLocalizedMessage());
					getService().createRequisition(DashBoardUtils.TN_REQU_NAME);
					load();
					return;
				}
				logger.warning("Load from rest Failed: "+e.getLocalizedMessage());
				Notification.show("Load Node Requisition", "Load from rest Failed Failed: "+e.getLocalizedMessage(), Type.WARNING_MESSAGE);
				return;
			}
			
			m_editorFields.setBuffered(true);
			m_editorFields.bind(getHostNameTextField(), DashBoardUtils.HOST);
			m_editorFields.bind(getDomainComboBox(), DashBoardUtils.CAT);
			m_editorFields.bind(getPrimaryTextField(),DashBoardUtils.PRIMARY);
			m_editorFields.bind(getDescrComboBox(), DashBoardUtils.DESCR);
			m_editorFields.bind(getParentComboBox(), DashBoardUtils.PARENT);
			m_editorFields.bind(getSnmpComboBox(), DashBoardUtils.SNMP_PROFILE);
			m_editorFields.bind(getCityTextField(),DashBoardUtils.CITY);
			m_editorFields.bind(getAddressTextField(), DashBoardUtils.ADDRESS1);
			m_editorFields.bind(getBuildingTextField(),DashBoardUtils.BUILDING);
			m_editorFields.bind(m_backupComboBox, DashBoardUtils.BACKUP_PROFILE);
			m_editorFields.bind(m_networkCatComboBox, DashBoardUtils.NETWORK_CATEGORY);
			m_editorFields.bind(m_notifCatComboBox, DashBoardUtils.NOTIF_CATEGORY);
			m_editorFields.bind(m_threshCatComboBox, DashBoardUtils.THRESH_CATEGORY);
			m_editorFields.bind(m_slaCatComboBox, DashBoardUtils.SLA_CATEGORY);
			m_editorFields.bind(m_optionalGroup, DashBoardUtils.SERVER_OPTIONAL_CATEGORY);
		    m_editorFields.bind(m_circuiId, DashBoardUtils.CIRCUITID);

		}
		
		super.load();
		Map<String,BackupProfile> bckupprofilemap = 
				getService().getBackupProfileContainer().getBackupProfileMap();
		List<String> backupprofiles = new ArrayList<String>(bckupprofilemap.keySet());
		Collections.sort(backupprofiles);
		for (String backupprofile: backupprofiles) {
			m_backupComboBox.addItem(backupprofile);
			m_backupComboBox.setItemCaption(backupprofile, 
					backupprofile +
					("(username:"+ bckupprofilemap.get(backupprofile).getUsername() +")"));
		}

		List<Categoria> cats = new ArrayList<Categoria>(getService().getCatContainer().getCatMap().values()); 
		Collections.sort(cats);
		for (Categoria categories: cats) {
			m_networkCatSearchComboBox.addItem(categories);
			m_networkCatSearchComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
		}

		for (Categoria categories: cats) {
			m_networkCatComboBox.addItem(categories);
			m_networkCatComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
		}

	}
	
	private void layout() { 

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(m_networkCatSearchComboBox);
		searchlayout.addComponent(m_notifCatSearchComboBox);
		searchlayout.addComponent(m_threshCatSearchComboBox);
		searchlayout.addComponent(m_slaCatSearchComboBox);

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
		leftGeneralInfo.addComponent(m_networkCatComboBox);
		leftGeneralInfo.addComponent(getDomainComboBox());
		leftGeneralInfo.addComponent(getPrimaryTextField());
		leftGeneralInfo.addComponent(getParentComboBox());
		leftGeneralInfo.addComponent(getCityTextField());
		leftGeneralInfo.addComponent(getAddressTextField());
		leftGeneralInfo.addComponent(getBuildingTextField());
		leftGeneralInfo.addComponent(m_circuiId);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);

		HorizontalLayout bottomRightGeneralInfo = new HorizontalLayout();
		bottomRightGeneralInfo.addComponent(m_secondaryIpComboBox);
		bottomRightGeneralInfo.addComponent(m_addSecondaryIpButton);
		
		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.setMargin(true);
		rightGeneralInfo.addComponent(m_secondaryIpAddressTable);
		rightGeneralInfo.addComponent(bottomRightGeneralInfo);
				
		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
		catLayout.addComponent(m_notifCatComboBox);
		catLayout.addComponent(m_threshCatComboBox);
		catLayout.addComponent(m_slaCatComboBox);
		
		HorizontalLayout profLayout = new HorizontalLayout();
		profLayout.setSizeFull();
		profLayout.addComponent(getSnmpComboBox());
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

		HorizontalLayout catLayout4 = new HorizontalLayout();
		catLayout4.setSizeFull();
		catLayout4.addComponent(m_optionalGroup);

		FormLayout optionCat = new FormLayout();
		optionCat.addComponent(new Label("Option Categories"));
		optionCat.addComponent(catLayout4);
		
		getRight().addComponent(new Panel(generalInfo));
		getRight().addComponent(new Panel(profileInfo));
		getRight().addComponent(new Panel(optionCat));
		getRight().setVisible(false);		
	}

	public void selectItem(BasicNode node) {		
		m_secondaryIpComboBox.removeAllItems();
		for (String ip: getService().getIpAddresses(DashBoardUtils.TN_REQU_NAME,node.getNodeLabel()) ) {
			if (ip.equals(node.getPrimary()))
				continue;
			boolean add=true;
			if (node.getServiceMap() != null) {
				for (BasicInterface secip: node.getServiceMap().keySet()) {
					if (ip.equals(secip.getIp())) { 
						add=false;
						break;
					}
				}
			}
			if (add)
				m_secondaryIpComboBox.addItem(ip);
		}
		
		BeanItemContainer<BasicService> secondaryIpContainer = new BeanItemContainer<BasicService>(BasicService.class);
		if (node.getServiceMap() != null) {
			for (BasicInterface ip: node.getServiceMap().keySet()) {
				for (String service: node.getServiceMap().get(ip)) {
					if (ip.getIp().equals(node.getPrimary()) && service.equals("ICMP"))
						continue;
					if (ip.getIp().equals(node.getPrimary()) && service.equals("SNMP"))
						continue;
					BasicService bs = new BasicService(ip);
					bs.setService(service);
					secondaryIpContainer.addBean(bs);
				}
			}
		}
		m_secondaryIpAddressTable.setContainerDataSource(secondaryIpContainer);

		m_editorFields.setItemDataSource((TrentinoNetworkNode)node);
	}
	
	
	@Override
	public String getName() {
		return "TNTab";
	}

	public String getRequisitionName() {
		return DashBoardUtils.TN_REQU_NAME;
	}
	
	@Override
	public void cleanSearchBox() {
		m_networkCatSearchComboBox.select(null);
		m_networkCatSearchComboBox.setValue(null);
		m_notifCatSearchComboBox.select(null);
		m_notifCatSearchComboBox.setValue(null);
		m_threshCatSearchComboBox.select(null);
		m_threshCatSearchComboBox.setValue(null);
		m_searchText="";
		m_searchField.setValue(m_searchText);
	}
	
	@Override
	public TrentinoNetworkNode addBean() {
		return m_requisitionContainer.addBeanAt(0,
				new TrentinoNetworkNode("notSavedHost"+newHost++,
                     getService().getCatContainer().getCatMap().values().iterator().next(),
                     DashBoardUtils.TN_REQU_NAME)).getBean();
	}

	@Override
	public BeanContainer<String, ? extends BasicNode> getRequisitionContainer() {
		return m_requisitionContainer;
	}
	
	@Override
	public BeanFieldGroup<TrentinoNetworkNode> getBeanFieldGroup() {
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
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});
	}
	
	private void addSearchValueChangeListener(TextField box) {
		m_searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			public void textChange(final TextChangeEvent event) {
				m_searchText = event.getText();
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});
	}
}
