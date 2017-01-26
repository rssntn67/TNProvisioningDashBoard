package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.RequisitionNode;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.Categoria;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
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

	private class NodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String   needle="";
		private Categoria needle1=null;
		private String   needle2=null;
		private String   needle3=null;
		private String   needle4=null;
		
		public NodeFilter(Object o, Object c1, Object c2,Object c3, Object c4) {
			if ( o != null)
				needle = (String) o;
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
			return (    node.getNodeLabel().contains(needle) 
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
	private TextField m_searchField       = new TextField("Type Label Text");

	private ComboBox m_secondaryIpComboBox = new ComboBox("Seleziona indirizzo ip");
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private Table m_secondaryIpAddressTable = new Table();

	public TrentinoNetworkTab(LoginBox login,DashBoardSessionService service) {
		super(login,service);
	}

	@Override
	public void load() {
		super.load();
		if (!loaded) {
			try {
				m_requisitionContainer = getService().getTNContainer();
				getRequisitionTable().setContainerDataSource(m_requisitionContainer);
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
		}
	}
	
	protected void layout() { 
		final ComboBox networkCatComboBox = new ComboBox("Network Category");
		final ComboBox notifCatComboBox   = new ComboBox("Notification Category");
		final ComboBox threshCatComboBox  = new ComboBox("Threshold Category");
		final ComboBox slaCatComboBox  = new ComboBox("Service Level Report Category");
		final ComboBox snmpComboBox  = new ComboBox("SNMP Profile");
		final ComboBox backupComboBox  = new ComboBox("Backup Profile");
		ComboBox parentComboBox = new ComboBox("Dipende da");
		final OptionGroup optionalGroup  = new OptionGroup("Optional Category");

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

		Map<String,BackupProfile> bckupprofilemap = 
				getService().getBackupProfileContainer().getBackupProfileMap();
		List<String> backupprofiles = new ArrayList<String>(bckupprofilemap.keySet());
		Collections.sort(backupprofiles);
		for (String backupprofile: backupprofiles) {
			backupComboBox.addItem(backupprofile);
			backupComboBox.setItemCaption(backupprofile, 
					backupprofile +
					("(username:"+ bckupprofilemap.get(backupprofile).getUsername() +")"));
		}

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
		getLeft().addComponent(getRequisitionTable());
		getLeft().addComponent(bottomLeftLayout);
		getLeft().setSizeFull();

		List<Categoria> cats = new ArrayList<Categoria>(getService().getCatContainer().getCatMap().values()); 
		Collections.sort(cats);
		for (Categoria categories: cats) {
			m_networkCatSearchComboBox.addItem(categories);
			m_networkCatSearchComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
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
				getRight().setVisible(false);
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});


		for (String category: DashBoardUtils.m_notify_levels) {
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
				getRight().setVisible(false);
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});
		
		for (String category: DashBoardUtils.m_threshold_levels) {
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
				getRight().setVisible(false);
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});

		for (String sla: DashBoardUtils.m_sla_levels) {
			m_slaCatSearchComboBox.addItem(sla);
		}
		m_slaCatSearchComboBox.setInvalidAllowed(false);
		m_slaCatSearchComboBox.setNullSelectionAllowed(true);		
		m_slaCatSearchComboBox.setImmediate(true);
		m_slaCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				getRight().setVisible(false);
				getRequisitionContainer().removeAllContainerFilters();
				getRequisitionContainer().addContainerFilter(
						new NodeFilter(m_searchText, 
								m_networkCatSearchComboBox.getValue(),
								m_notifCatSearchComboBox.getValue(),
								m_threshCatSearchComboBox.getValue(),
								m_slaCatSearchComboBox.getValue()));
			}
		});

		m_searchField.setInputPrompt("Search nodes");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
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

		for (Categoria categories: cats) {
			networkCatComboBox.addItem(categories);
			networkCatComboBox.setItemCaption(categories, categories.getNetworklevel()+" - " + categories.getName());
		}

		for (String notif: DashBoardUtils.m_notify_levels) {
			notifCatComboBox.addItem(notif);
		}

		for (String threshold: DashBoardUtils.m_threshold_levels) {
			threshCatComboBox.addItem(threshold);
		}

		for (String sla: DashBoardUtils.m_sla_levels) {
			slaCatComboBox.addItem(sla);
		}
		
		for (String option: DashBoardUtils.m_server_optional) {
			optionalGroup.addItem(option);
		}

		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);

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
					getDomain().select(((Categoria)networkCatComboBox.getValue()).getDnsdomain());
					notifCatComboBox.select(((Categoria)networkCatComboBox.getValue()).getNotifylevel());
					threshCatComboBox.select(((Categoria)networkCatComboBox.getValue()).getThresholdlevel());
					backupComboBox.select(((Categoria)networkCatComboBox.getValue()).getBackupprofile());
					snmpComboBox.select(((Categoria)networkCatComboBox.getValue()).getSnmpprofile());
				}
			}
		});

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
						getService().deleteInterface(DashBoardUtils.TN_REQU_NAME, m_editorFields.getItemDataSource().getBean().getForeignId(), 							
								ip);
			        source.getContainerDataSource().removeItem(itemId);
			        Set<String> secondary = new HashSet<String>();
			        for (Object id: source.getContainerDataSource().getItemIds()) {
			        	secondary.add((String)source.getContainerProperty(id, "indirizzo ip").getValue());
			        }
			        m_secondaryIpComboBox.addItem(ip);
			        m_editorFields.getItemDataSource().getBean().setSecondary(secondary);
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
							getService().addSecondaryInterface(DashBoardUtils.TN_REQU_NAME,m_editorFields.getItemDataSource().getBean().getForeignId(),
									ip);
							IndexedContainer secondaryIpContainer = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
							Item ipItem = secondaryIpContainer.getItem(secondaryIpContainer.addItem());
							ipItem.getItemProperty("indirizzo ip").setValue(m_secondaryIpComboBox.getValue().toString()); 
							Set<String> secondary = new HashSet<String>();
					        for (Object id: m_secondaryIpAddressTable.getContainerDataSource().getItemIds()) {
					        	secondary.add((String)m_secondaryIpAddressTable.getContainerProperty(id, "indirizzo ip").getValue());
					        }
					        m_editorFields.getItemDataSource().getBean().setSecondary(secondary);
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

		slaCatComboBox.setInvalidAllowed(false);
		slaCatComboBox.setNullSelectionAllowed(true);
		slaCatComboBox.setRequired(false);

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

		TextField building = new TextField("Edificio");
		building.setWidth(8, Unit.CM);
		building.setHeight(6, Unit.MM);

		TextField circuiId = new TextField("circuitId");
		circuiId.setWidth(8, Unit.CM);
		circuiId.setHeight(6, Unit.MM);

		optionalGroup.setInvalidAllowed(false);
		optionalGroup.setNullSelectionAllowed(false);
		optionalGroup.setImmediate(true);
		optionalGroup.setMultiSelect(true);

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_descrComboBox, DashBoardUtils.DESCR);
		m_editorFields.bind(getHostName(), DashBoardUtils.HOST);
		m_editorFields.bind(networkCatComboBox, DashBoardUtils.NETWORK_CATEGORY);
		m_editorFields.bind(getDomain(), DashBoardUtils.CAT);
		m_editorFields.bind(getPrimary(),DashBoardUtils.PRIMARY);
		m_editorFields.bind(parentComboBox, DashBoardUtils.PARENT);
		m_editorFields.bind(snmpComboBox, DashBoardUtils.SNMP_PROFILE);
		m_editorFields.bind(backupComboBox, DashBoardUtils.BACKUP_PROFILE);
		m_editorFields.bind(notifCatComboBox, DashBoardUtils.NOTIF_CATEGORY);
		m_editorFields.bind(threshCatComboBox, DashBoardUtils.THRESH_CATEGORY);
		m_editorFields.bind(slaCatComboBox, DashBoardUtils.SLA_CATEGORY);
		m_editorFields.bind(optionalGroup, DashBoardUtils.SERVER_OPTIONAL_CATEGORY);
		m_editorFields.bind(city,DashBoardUtils.CITY);
	    m_editorFields.bind(address, DashBoardUtils.ADDRESS1);
		m_editorFields.bind(building,DashBoardUtils.BUILDING);
	    m_editorFields.bind(circuiId, DashBoardUtils.CIRCUITID);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_descrComboBox);
		leftGeneralInfo.addComponent(getHostName());
		leftGeneralInfo.addComponent(networkCatComboBox);
		leftGeneralInfo.addComponent(getDomain());
		leftGeneralInfo.addComponent(getPrimary());
		leftGeneralInfo.addComponent(parentComboBox);
		leftGeneralInfo.addComponent(city);
		leftGeneralInfo.addComponent(address);
		leftGeneralInfo.addComponent(building);
		leftGeneralInfo.addComponent(circuiId);
		
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
		catLayout.addComponent(slaCatComboBox);
		
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

		HorizontalLayout catLayout4 = new HorizontalLayout();
		catLayout4.setSizeFull();
		catLayout4.addComponent(optionalGroup);

		FormLayout optionCat = new FormLayout();
		optionCat.addComponent(new Label("Option Categories"));
		optionCat.addComponent(catLayout4);
		
		getRight().addComponent(new Panel(generalInfo));
		getRight().addComponent(new Panel(profileInfo));
		getRight().addComponent(new Panel(optionCat));
		
		
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
	public void selectItem() {
		Object contactId = getRequisitionTable().getValue();

		if (contactId != null) {
			TrentinoNetworkNode node = ((BeanItem<TrentinoNetworkNode>)getRequisitionTable()
				.getItem(contactId)).getBean();
			
			m_descrComboBox.removeAllItems();
			if (node.getDescr() != null)
				m_descrComboBox.addItem(node.getDescr());
			
			m_secondaryIpComboBox.removeAllItems();
			for (String ip: getService().getIpAddresses(DashBoardUtils.TN_REQU_NAME,node.getNodeLabel()) ) {
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
						getService().syncSnmpProfile(node.getPrimary(), snmpProfile);
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
			getRight().setVisible(true);
			enableNodeButtons();
		}

	}
	
	
	@Override
	public String getName() {
		return "TNTab";
	}

	@Override
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
	public TrentinoNetworkNode getBean() {
		return m_editorFields.getItemDataSource().getBean();
	}

	@Override
	public TrentinoNetworkNode addBean() {
		BeanItem<TrentinoNetworkNode> bean = m_requisitionContainer.addBeanAt(0,
				new TrentinoNetworkNode("notSavedHost"+newHost++,
                     getService().getCatContainer().getCatMap().values().iterator().next()));
		return bean.getBean();
	}

	@Override
	public BeanContainer<String, ? extends RequisitionNode> getRequisitionContainer() {
		return m_requisitionContainer;
	}
	
	@Override
	public void commit() throws CommitException, SQLException {
		m_editorFields.commit();
		TrentinoNetworkNode node = m_editorFields.getItemDataSource().getBean();
		if (node.getForeignId() == null) {
			node.setForeignId(node.getHostname());
			node.setValid(true);
			getService().addTNNode(node);
			logger.info("Added: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
			Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
		} else {
			getService().updateTNNode(node);
			node.setValid(getService().isValid(node));
			logger.info("Updated: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
			Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Updated", Type.HUMANIZED_MESSAGE);
		}
		m_requisitionContainer.addContainerFilter(
				new NodeFilter(node.getHostname(), null, null, null,null));
		m_requisitionContainer.removeAllContainerFilters();
	}

	@Override
	public void discard() {
		m_editorFields.discard();		
	}

	@Override
	public void delete() {
		BeanItem<TrentinoNetworkNode> node = m_editorFields.getItemDataSource();
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
}
