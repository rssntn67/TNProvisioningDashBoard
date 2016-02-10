package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.Vrf;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TNPD - Gestione VRF")
@Theme("runo")
public class VrfTab extends DashboardTab {

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

	public static final String VRF = "name";
	public static final String NOTIF_LEVEL   = "notifylevel";
	public static final String NETWORK_LEVEL = "networklevel";
	public static final String DNS_DOMAIN = "dnsdomain";
	public static final String THRESH_LEVEL  = "thresholdlevel";
	public static final String BACKUP_PROFILE  = "backupprofile";
	public static final String SNMP_PROFILE    = "snmpprofile";
	
	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private SQLContainer m_requisitionContainer;
	private boolean loaded=false;

	private ComboBox m_networkCatSearchComboBox = new ComboBox("Select Vrf Network Level");
	private ComboBox m_notifCatSearchComboBox   = new ComboBox("Select Vrf Notif Level");
	private ComboBox m_threshCatSearchComboBox  = new ComboBox("Select Vrf Threshold Level");
	private ComboBox m_vrfSearchComboBox        = new ComboBox("Select Vrf");
	private Table m_requisitionTable   	= new Table();
	private Button m_addNewNodeButton  = new Button("Nuova VRF");
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina VRF");

	private TextField m_vrf = new TextField("Vrf");
	private ComboBox m_domainComboBox = new ComboBox("Dominio");
	private ComboBox m_networkCatComboBox = new ComboBox("Network Level");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Level");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Level");
	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");

	private VerticalLayout m_editRequisitionNodeLayout  = new VerticalLayout();
	
	private BeanFieldGroup<Vrf> m_editorFields     = new BeanFieldGroup<Vrf>(Vrf.class);
	Integer newHost = 0;
	
	public VrfTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		Map<String, BackupProfile> backupprofilemap;
		if (loaded)
			return;
		for (String snmpprofile: getService().getTnDao().getSnmpProfiles().keySet()) {
			m_snmpComboBox.addItem(snmpprofile);
		}

		backupprofilemap = getService().getTnDao().getBackupProfiles();
		for (String backupprofile: backupprofilemap.keySet()) {
			m_backupComboBox.addItem(backupprofile);
		}

		try {
			m_requisitionContainer = (SQLContainer) getService().getTnDao().getVrfContainer();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

		m_vrfSearchComboBox.setWidth("80%");
		searchlayout.addComponent(m_vrfSearchComboBox);
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
		m_requisitionTable.setVisibleColumns(new String[] { "name" });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (String nl: TNDao.m_network_categories) {
			m_networkCatSearchComboBox.addItem(nl);
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
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_vrfSearchComboBox, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
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
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_vrfSearchComboBox, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
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
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_vrfSearchComboBox, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});

		for (String vrf: getService().getTnDao().getVrfs().keySet()) {
			m_vrfSearchComboBox.addItem(vrf);
		}
		m_vrfSearchComboBox.setInvalidAllowed(false);
		m_vrfSearchComboBox.setNullSelectionAllowed(true);		
		m_vrfSearchComboBox.setImmediate(true);
		m_vrfSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_vrfSearchComboBox, m_networkCatSearchComboBox.getValue(),m_notifCatSearchComboBox.getValue(),m_threshCatSearchComboBox.getValue()));
			}
		});

		for (String nl: TNDao.m_network_categories) {
			m_networkCatComboBox.addItem(nl);
		}

		for (final String vrfs: getService().getTnDao().getDomains()) {
			m_domainComboBox.addItem(vrfs);
		}

		for (String notif: TNDao.m_notif_categories) {
			m_notifCatComboBox.addItem(notif);
		}

		for (String threshold: TNDao.m_thresh_categories) {
			m_threshCatComboBox.addItem(threshold);
		}

		m_vrf.setSizeFull();
		m_vrf.setWidth(4, Unit.CM);
		m_vrf.setHeight(6, Unit.MM);
		m_vrf.setRequired(true);
		m_vrf.setRequiredError("vrf non deve essere vuota");
		m_vrf.addValidator(new RegexpValidator("^[A-Z][A-Za-z0-9]*$", "la vrf deve iniziare con una maiuscola e contenere codici alfanumerici"));
		m_vrf.addValidator(new DuplicatedVrfValidator());
		m_vrf.setImmediate(true);

		m_networkCatComboBox.setInvalidAllowed(false);
		m_networkCatComboBox.setNullSelectionAllowed(false);
		m_networkCatComboBox.setRequired(true);
		m_networkCatComboBox.setRequiredError("E' necessario scegliere un livello di rete");
		m_networkCatComboBox.setImmediate(true);

		m_domainComboBox.setInvalidAllowed(false);
		m_domainComboBox.setNullSelectionAllowed(false);
		m_domainComboBox.setRequired(true);
		m_domainComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_domainComboBox.setImmediate(true);
		
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

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_vrf, VRF);
		m_editorFields.bind(m_networkCatComboBox, NETWORK_LEVEL);
		m_editorFields.bind(m_domainComboBox, DNS_DOMAIN);
		m_editorFields.bind(m_snmpComboBox, SNMP_PROFILE);
		m_editorFields.bind(m_backupComboBox, BACKUP_PROFILE);
		m_editorFields.bind(m_notifCatComboBox, NOTIF_LEVEL);
		m_editorFields.bind(m_threshCatComboBox, THRESH_LEVEL);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_vrf);
		leftGeneralInfo.addComponent(m_networkCatComboBox);
		leftGeneralInfo.addComponent(m_domainComboBox);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);
		
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
		generalInfo.setExpandRatio(leftGeneralInfo, 3);
		generalInfo.setExpandRatio(centerGeneralInfo, 1);

				
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
			private static final long serialVersionUID = -5112348680238979359L;

			@Override
			public void buttonClick(ClickEvent event) {
				Vrf vrf = new Vrf();
				m_editorFields.setItemDataSource(vrf);
				m_editRequisitionNodeLayout.setVisible(true);
				m_saveNodeButton.setEnabled(true);
				m_removeNodeButton.setEnabled(true);
				m_resetNodeButton.setEnabled(true);						

			}
		});
		/*
		m_saveNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					m_editorFields.commit();
					Vrf vrf = m_editorFields.getItemDataSource().getBean();
					if (node.getForeignId() == null) {
						node.setForeignId(node.getHostname());
						node.setValid(true);
						getService().addNode(TN,node);
						logger.info("Added: " + m_editorFields.getItemDataSource().getBean().getNodeLabel());
						Notification.show("Save", "Node " +m_editorFields.getItemDataSource().getBean().getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
					} else {
						getService().updateNode(TN,node);
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
		});*/
		
		loaded=true;


	}

	private void selectItem() {
		Object vrfId = m_requisitionTable.getValue();

		if (vrfId == null)
			return;
		Item vrftableRow = m_requisitionTable.getItem(vrfId);
			
		Vrf vrf =new Vrf(vrftableRow.getItemProperty("name").getValue().toString(),
				vrftableRow.getItemProperty("notifylevel").getValue().toString(),
				vrftableRow.getItemProperty("networklevel").getValue().toString(),
				vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
				vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
				vrftableRow.getItemProperty("backupprofile").getValue().toString(),
				vrftableRow.getItemProperty("snmpprofile").getValue().toString()
				);
		m_editorFields.setItemDataSource(vrf);
		m_editRequisitionNodeLayout.setVisible(true);
		m_saveNodeButton.setEnabled(true);
		m_removeNodeButton.setEnabled(true);
		m_resetNodeButton.setEnabled(true);						
	}

	
		
	class DuplicatedVrfValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String vrf = (String)value;
			Vrf data = m_editorFields.getItemDataSource().getBean();
			if (data.getName() != null)
				return;
			logger.info("DuplicatedVrfValidator: validating vrf: " + vrf);
	         if (getService().getTnDao().getVrfs().containsKey(vrf))
	             throw new InvalidValueException("DuplicatedPrimaryValidator: trovato un duplicato della vrf: " + vrf);
	       }
	}

	
}
