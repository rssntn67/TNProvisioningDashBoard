package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;

import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

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

import static org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode.TN;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TrentinoNetwork Provision Dashboard - Trentino Network Requisition")
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
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");

	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private ComboBox m_vrfsComboBox = new ComboBox("Dominio");
	private ComboBox m_parentComboBox = new ComboBox("Dipende da");
	private ComboBox m_networkCatComboBox = new ComboBox("Network Category");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Category");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Category");
	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");

	private Table m_secondaryIpAddressTable = new Table();

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();
	
	private BeanFieldGroup<TrentinoNetworkRequisitionNode> m_editorFields     = new BeanFieldGroup<TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
	Integer newHost = 0;
	
	public TrentinoNetworkTab(String foreignsource, DashboardService service) {
		super(foreignsource, service);
	}

	public void load() {
		if (loaded)
			return;
		try {
			getService().loadSnmpProfiles();
		} catch (SQLException e) {
			Notification.show("Snmp Profile", "Load from db Failed", Type.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}
		try {
			getService().loadBackupProfiles();
		} catch (SQLException e) {
			Notification.show("Backup Profile", "Load from db Failed", Type.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}
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
		bottomRightLayout.addComponent(m_saveNodeButton);
		bottomRightLayout.addComponent(m_resetNodeButton);
		bottomRightLayout.setComponentAlignment(m_removeNodeButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveNodeButton, Alignment.MIDDLE_CENTER);
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
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(new Label("Informazioni Generali"));
		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);
		m_editorFields.bind(m_descrComboBox, DESCR);
		leftGeneralInfo.addComponent(m_descrComboBox);
		
		TextField hostname = new TextField("Hostname");
		hostname.setSizeFull();
		hostname.setWidth(4, Unit.CM);
		hostname.setHeight(6, Unit.MM);
		hostname.setRequired(true);
		hostname.setRequiredError("The definitive descriptions of the rules for forming domain names appear in RFC 1035, RFC 1123, and RFC 2181." +
		" A domain name consists of one or more parts, technically called labels, that are conventionally concatenated, and delimited by dots, such asexample.com."
				+ " Each label may contain up to 63 characters." +
				" The full domain name may not exceed a total length of 253 characters in its external dotted-label specification." +
				" The characters allowed in a label are a subset of the ASCII character set, and includes the characters a through z, A through Z, digits 0 through 9, the hyphen." +
				" This rule is known as the LDH rule (letters, digits, hyphen). " +
				" Labels may not start or end with a hyphen." +
				" A hostname is a domain name that has at least one IP address associated.");
		m_editorFields.bind(hostname, HOST);
		leftGeneralInfo.addComponent(hostname);

		for (final String vrfs: m_vrfs) {
			m_vrfsComboBox.addItem(vrfs);
		}
		m_vrfsComboBox.setInvalidAllowed(false);
		m_vrfsComboBox.setNullSelectionAllowed(false);
		m_vrfsComboBox.setRequired(true);
		m_vrfsComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_editorFields.bind(m_vrfsComboBox, VRF);
		leftGeneralInfo.addComponent(m_vrfsComboBox);
		TextField primary = new TextField(PRIMARY);
		primary.setRequired(true);
		primary.setRequiredError("E' necessario specifica un indirizzo ip primario");
		leftGeneralInfo.addComponent(primary);
		m_editorFields.bind(primary,PRIMARY);
		
		m_parentComboBox.setInvalidAllowed(false);
		m_parentComboBox.setNullSelectionAllowed(false);
		leftGeneralInfo.addComponent(m_parentComboBox);
		m_editorFields.bind(m_parentComboBox, PARENT);
		generalInfo.addComponent(leftGeneralInfo);

		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);
		generalInfo.addComponent(centerGeneralInfo);

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
					m_editorFields.getItemDataSource().getBean().removeSecondaryInteface((String)source.getContainerProperty(itemId, "indirizzo ip").getValue());
			        source.getContainerDataSource().removeItem(itemId);
					Notification.show("Delete ip", "Done", Type.HUMANIZED_MESSAGE);
					} catch (Exception e) {
						Notification.show("Delete ip", "Failed", Type.ERROR_MESSAGE);
						e.printStackTrace();
					}
			      }
			    });
			 
			    return button;
			  }
		});

		FormLayout rightGeneralInfo = new FormLayout();
		rightGeneralInfo.setMargin(true);
		rightGeneralInfo.addComponent(m_secondaryIpAddressTable);
		final TextField secondaryipaddtextbox = new TextField("Aggiungi ip");
		Button addSecondaryIp = new Button("+");
		addSecondaryIp.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void buttonClick(ClickEvent event) {
				if (secondaryipaddtextbox.getValue() != null) {
					try {
						TrentinoNetworkRequisitionNode node = m_editorFields.getItemDataSource().getBean();
						if (node.getUpdate()) {
							node.addSecondaryInterface(secondaryipaddtextbox.getValue().toString());
							IndexedContainer secondary = (IndexedContainer)m_secondaryIpAddressTable.getContainerDataSource();
							Item ipItem = secondary.getItem(secondary.addItem());
							ipItem.getItemProperty("indirizzo ip").setValue(secondaryipaddtextbox.getValue().toString()); 
							Notification.show("Add ip", "Done", Type.HUMANIZED_MESSAGE);
						} else {
							Notification.show("Add ip", "Cannot add secondary to new node: save it and then add secondary", Type.WARNING_MESSAGE);
						}
					} catch (Exception e) {
						Notification.show("Add ip", "Failed", Type.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
		});

		HorizontalLayout bottomRightGeneralInfo = new HorizontalLayout();
		bottomRightGeneralInfo.addComponent(secondaryipaddtextbox);
		bottomRightGeneralInfo.addComponent(addSecondaryIp);
		rightGeneralInfo.addComponent(bottomRightGeneralInfo);
		
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
		m_networkCatComboBox.setRequired(true);
		m_networkCatComboBox.setRequiredError("E' necessario scegliere una coppia di categorie di rete");
		m_editorFields.bind(m_networkCatComboBox, NETWORK_CATEGORY);
		
		for (String notif: m_notif_categories) {
			m_notifCatComboBox.addItem(notif);
		}
		m_notifCatComboBox.setInvalidAllowed(false);
		m_notifCatComboBox.setNullSelectionAllowed(false);
		m_notifCatComboBox.setRequired(true);
		m_notifCatComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");
		m_editorFields.bind(m_notifCatComboBox, NOTIF_CATEGORY);

		for (String threshold: m_thresh_categories) {
			m_threshCatComboBox.addItem(threshold);
		}
		m_threshCatComboBox.setInvalidAllowed(false);
		m_threshCatComboBox.setNullSelectionAllowed(false);
		m_threshCatComboBox.setRequired(true);
		m_threshCatComboBox.setRequiredError("E' necessario scegliere una categoria per le threshold");
		m_editorFields.bind(m_threshCatComboBox, THRESH_CATEGORY);
		
		catLayout.addComponent(m_networkCatComboBox);
		catLayout.addComponent(m_notifCatComboBox);
		catLayout.addComponent(m_threshCatComboBox);
		categoryInfo.addComponent(catLayout);
		m_editRequisitionNodeLayout.addComponent(new Panel(categoryInfo));

		FormLayout snmpProfile = new FormLayout();
		snmpProfile.addComponent(new Label("Credenziali SNMP per collezione dati"));
		HorizontalLayout snmplayout = new HorizontalLayout();
		m_snmpComboBox.setContainerDataSource(getService().getSnmpProfiles());
		m_snmpComboBox.setInvalidAllowed(false);
		m_snmpComboBox.setNullSelectionAllowed(false);
		m_snmpComboBox.setRequired(true);
		m_snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");
		m_editorFields.bind(m_snmpComboBox, SNMP_PROFILE);
		snmplayout.addComponent(m_snmpComboBox);
		snmpProfile.addComponent(snmplayout);
        m_editRequisitionNodeLayout.addComponent(new Panel(snmpProfile));

		FormLayout backupProfile = new FormLayout();
		m_backupComboBox.setContainerDataSource(getService().getBackupProfiles());
        m_backupComboBox.setInvalidAllowed(false);
        m_backupComboBox.setNullSelectionAllowed(false);
        m_backupComboBox.setRequired(true);
        m_backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");
		m_editorFields.bind(m_backupComboBox, BACKUP_PROFILE);
		backupProfile.addComponent(new Label("Credenziali accesso per backup configurazione"));
		HorizontalLayout backuplayout = new HorizontalLayout();
		backuplayout.addComponent(m_backupComboBox);
		backupProfile.addComponent(backuplayout);
		m_editRequisitionNodeLayout.addComponent(new Panel(backupProfile));

		HorizontalLayout localizationInfo = new HorizontalLayout();
		FormLayout localization = new FormLayout();
		localization.addComponent(new Label("Localizzazione"));
		
		TextField city = new TextField("Citta'");
		city.setWidth(8, Unit.CM);
		city.setHeight(6, Unit.MM);
		city.setRequired(true);
		city.setRequiredError("E' necessario specificare la citta'");
		m_editorFields.bind(city,CITY);
		localization.addComponent(city);
		
		TextField address = new TextField("Indirizzo");
		address.setWidth(8, Unit.CM);
		address.setHeight(6, Unit.MM);
		address.setRequired(true);
		address.setRequiredError("E' necessario specificare l'indirizzo");
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
		m_saveNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);				
		m_resetNodeButton.setEnabled(false);
		
		m_addNewNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				BeanItem<TrentinoNetworkRequisitionNode> bean = m_requisitionContainer.addBeanAt(0,new TrentinoNetworkRequisitionNode("notSavedHost"+newHost++,getService()));
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionTable.select(bean.getBean().getNodeLabel());
				selectItem();
			}
		});

		m_saveNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				try {
					m_editorFields.commit();
					m_editorFields.getItemDataSource().getBean().commit();
					m_requisitionContainer.addContainerFilter(new NodeFilter(m_editorFields.getItemDataSource().getBean().getNodeLabel(), null,null,null));
					m_requisitionContainer.removeAllContainerFilters();
					Notification.show("Save", "Done", Type.HUMANIZED_MESSAGE);
				} catch (CommitException e) {
					Notification.show("Save", "Failed", Type.ERROR_MESSAGE);
					e.printStackTrace();
				}
				selectItem();
			}
		});

		m_removeNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				try {
					m_editRequisitionNodeLayout.setVisible(false);
					m_saveNodeButton.setEnabled(false);
					m_removeNodeButton.setEnabled(false);
					m_resetNodeButton.setEnabled(false);
					BeanItem<TrentinoNetworkRequisitionNode> node = m_editorFields.getItemDataSource();
					if (node.getBean().getUpdate())
						getService().delete(TN,node.getBean().getRequisitionNode());
					if ( ! m_requisitionContainer.removeItem(node.getBean().getNodeLabel()))
						m_requisitionContainer.removeItem(m_requisitionContainer.getIdByIndex(0));
					Notification.show("Delete", "Done", Type.HUMANIZED_MESSAGE);
				} catch (Exception e) {
					Notification.show("Delete", "Failed", Type.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
		
		m_resetNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_editorFields.discard();
				m_requisitionContainer.removeAllContainerFilters();
				m_editRequisitionNodeLayout.setVisible(false);
				m_saveNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
			}
		});

	}

	private void initProvisionNodeList() {
		m_requisitionContainer = getProvisionNodeList();
		m_requisitionTable.setContainerDataSource(m_requisitionContainer);
		m_requisitionTable.setVisibleColumns(new String[] { LABEL });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void selectItem() {
		Object contactId = m_requisitionTable.getValue();

		if (contactId != null) {
			TrentinoNetworkRequisitionNode node = ((BeanItem<TrentinoNetworkRequisitionNode>)m_requisitionTable
				.getItem(contactId)).getBean();
			if (node.getPrimary() != null)
				node.updateSnmpProfile(getService().getSnmpInfo(node.getPrimary()));
			m_snmpComboBox.select(node.getSnmpProfile());

			m_descrComboBox.removeAllItems();
			m_descrComboBox.addItem(node.getDescr());
			
			m_editorFields.setItemDataSource(node);
			
			m_secondaryIpAddressTable.setContainerDataSource(node.getSecondary());
			
			m_editRequisitionNodeLayout.setVisible(true);
			if (node.getDescr().contains("FAST")) {
				m_saveNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
				m_resetNodeButton.setEnabled(false);
				Notification.show("provided by FAST", "le modifiche ai nodi aggiunti da FAST non sono abilitate", Type.WARNING_MESSAGE);
			} else {
				m_saveNodeButton.setEnabled(true);
				m_removeNodeButton.setEnabled(true);
				m_resetNodeButton.setEnabled(true);
			}
		}

	}
	private BeanContainer<String, TrentinoNetworkRequisitionNode> getProvisionNodeList() {
		BeanContainer<String,TrentinoNetworkRequisitionNode> nodes = new BeanContainer<String,TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
		nodes.setBeanIdProperty(LABEL);
		for (RequisitionNode node : getService().getRequisitionNodes(getForeignSource()).getNodes()) {
			nodes.addBean(new TrentinoNetworkRequisitionNode(node,getService()));
			m_parentComboBox.addItem(node.getForeignId());
		}
		return nodes;
	}
	
}
