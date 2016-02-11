package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.Vrf;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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

	private static final long serialVersionUID = -5948892618258879832L;

	public static final String VRF = "name";
	public static final String NOTIF_LEVEL   = "notifylevel";
	public static final String NETWORK_LEVEL = "networklevel";
	public static final String DNS_DOMAIN = "dnsdomain";
	public static final String THRESH_LEVEL  = "thresholdlevel";
	public static final String BACKUP_PROFILE  = "backupprofile";
	public static final String SNMP_PROFILE    = "snmpprofile";
	
	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private SQLContainer m_vrfContainer;
	private boolean loaded=false;

	private ComboBox m_networkCatSearchComboBox = new ComboBox("Select Vrf Network Level");
	private ComboBox m_notifCatSearchComboBox   = new ComboBox("Select Vrf Notif Level");
	private ComboBox m_threshCatSearchComboBox  = new ComboBox("Select Vrf Threshold Level");
	private ComboBox m_vrfSearchComboBox        = new ComboBox("Select Vrf");
	private Table m_vrfTable   	= new Table();
	private Button m_addVrfButton  = new Button("Nuova VRF");
	private Button m_saveVrfButton  = new Button("Salva Modifiche");
	private Button m_removeVrfButton  = new Button("Elimina VRF");

	private TextField m_vrf = new TextField("Vrf");
	private ComboBox m_domainComboBox = new ComboBox("Dominio");
	private ComboBox m_networkCatComboBox = new ComboBox("Network Level");
	private ComboBox m_notifCatComboBox   = new ComboBox("Notification Level");
	private ComboBox m_threshCatComboBox  = new ComboBox("Threshold Level");
	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");
	private ComboBox m_backupComboBox  = new ComboBox("Backup Profile");

	private VerticalLayout m_editVrfLayout  = new VerticalLayout();
	
	private BeanFieldGroup<Vrf> m_editorFields     = new BeanFieldGroup<Vrf>(Vrf.class);
	
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

		m_vrfContainer = (SQLContainer) getService().getTnDao().getVrfContainer();

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

		m_vrfTable.setSizeFull();
		leftLayout.addComponent(m_vrfTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));
		leftLayout.addComponent(bottomLeftLayout);
		leftLayout.setSizeFull();

		rightLayout.addComponent(m_editVrfLayout);

		HorizontalLayout bottomRightLayout = new HorizontalLayout();
		bottomRightLayout.addComponent(m_removeVrfButton);
		bottomRightLayout.addComponent(m_saveVrfButton);
		bottomRightLayout.addComponent(m_addVrfButton);
		bottomRightLayout.setComponentAlignment(m_removeVrfButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveVrfButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_addVrfButton, Alignment.MIDDLE_RIGHT);
		rightLayout.addComponent(new Panel(bottomRightLayout));
		
		m_vrfTable.setContainerDataSource(m_vrfContainer);
		m_vrfTable.setVisibleColumns(new String[] { "name" });
		m_vrfTable.setSelectable(true);
		m_vrfTable.setImmediate(true);

		m_vrfTable.addValueChangeListener(new Property.ValueChangeListener() {
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
				m_vrfContainer.removeAllContainerFilters();
				if (m_networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", m_networkCatSearchComboBox.getValue()));
				if (m_notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", m_notifCatSearchComboBox.getValue()));
				if (m_threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", m_threshCatSearchComboBox.getValue()));
				if (m_vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",m_vrfSearchComboBox.getValue()));
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
				m_vrfContainer.removeAllContainerFilters();
				if (m_notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", m_notifCatSearchComboBox.getValue()));
				if (m_networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", m_networkCatSearchComboBox.getValue()));
				if (m_threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", m_threshCatSearchComboBox.getValue()));
				if (m_vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",m_vrfSearchComboBox.getValue()));
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
				m_vrfContainer.removeAllContainerFilters();
				if (m_threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", m_threshCatSearchComboBox.getValue()));
				if (m_notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", m_notifCatSearchComboBox.getValue()));
				if (m_networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", m_networkCatSearchComboBox.getValue()));
				if (m_vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",m_vrfSearchComboBox.getValue()));
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
				m_vrfContainer.removeAllContainerFilters();
				if (m_vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",m_vrfSearchComboBox.getValue()));
				if (m_threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", m_threshCatSearchComboBox.getValue()));
				if (m_notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", m_notifCatSearchComboBox.getValue()));
				if (m_networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", m_networkCatSearchComboBox.getValue()));
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
		m_vrf.addValidator(new RegexpValidator("^[A-Z][A-Za-z\\-0-9]*[A-Za-z0-9]+$", "la vrf deve iniziare con una maiuscola e contenere codici alfanumerici"));
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

		m_editVrfLayout.setMargin(true);
		m_editVrfLayout.setVisible(false);
		m_editVrfLayout.addComponent(new Panel(generalInfo));
		m_editVrfLayout.addComponent(new Panel(profileInfo));

		m_saveVrfButton.setEnabled(false);
		m_removeVrfButton.setEnabled(false);				
		
		m_addVrfButton.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5112348680238979359L;

			@Override
			public void buttonClick(ClickEvent event) {
				m_vrfTable.select(null);
				m_editorFields.setItemDataSource(new Vrf());
				m_editVrfLayout.setVisible(true);
				m_saveVrfButton.setEnabled(true);
				m_removeVrfButton.setEnabled(true);
			}
		});
		
		m_saveVrfButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				Object vrfId = m_vrfTable.getValue();
				try {
					m_editorFields.commit();
				} catch (CommitException ce) {
					logger.warning("Save Vrf Failed: " + ce.getLocalizedMessage());
					Notification.show("Save Vrf Failed", ce.getLocalizedMessage(), Type.ERROR_MESSAGE);
				}
				
				Vrf vrf = m_editorFields.getItemDataSource().getBean();
				Integer versionid = null;
				if (vrfId == null) {
					vrfId = m_vrfContainer.addItem();
					versionid = 0;
					logger.info("Adding Vrf: " + vrf.getName());
				} else {
					logger.info("Updating Vrf: " + vrf.getName());
				}
				m_vrfContainer.getContainerProperty(vrfId, "name").setValue(vrf.getName());
				m_vrfContainer.getContainerProperty(vrfId, "notifylevel").setValue(vrf.getNotifylevel());
				m_vrfContainer.getContainerProperty(vrfId, "networklevel").setValue(vrf.getNetworklevel());
				m_vrfContainer.getContainerProperty(vrfId, "dnsdomain").setValue(vrf.getDnsdomain());
				m_vrfContainer.getContainerProperty(vrfId, "thresholdlevel").setValue(vrf.getThresholdlevel());
				m_vrfContainer.getContainerProperty(vrfId, "backupprofile").setValue(vrf.getBackupprofile());
				m_vrfContainer.getContainerProperty(vrfId, "snmpprofile").setValue(vrf.getSnmpprofile());
				if (versionid != null) 
					m_vrfSearchComboBox.addItem(vrf.getName());
				try {
					m_vrfContainer.commit();
					m_vrfTable.select(null);
					m_editVrfLayout.setVisible(false);
					logger.info("Saved Vrf: " + vrf.getName());
					Notification.show("Save", "Vrf " + vrf.getName()+ " Saved", Type.HUMANIZED_MESSAGE);
				} catch (UnsupportedOperationException uoe) {
					uoe.printStackTrace();					
					logger.warning("Save Vrf Failed: " + uoe.getLocalizedMessage());
					Notification.show("Save Vrf Failed", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
					m_vrfSearchComboBox.removeItem(vrf.getName());
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					logger.warning("Save Vrf Failed: " + sqle.getLocalizedMessage());
					Notification.show("Save Vrf Failed", sqle.getLocalizedMessage(), Type.ERROR_MESSAGE);
					m_vrfSearchComboBox.removeItem(vrf.getName());
				}
			}
		});

		m_removeVrfButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				String vrf = m_editorFields.getItemDataSource().getBean().getName();
				if (m_vrfContainer.removeItem(m_vrfTable.getValue())) {
					try {
						m_vrfContainer.commit();
					} catch (UnsupportedOperationException uoe) {
						uoe.printStackTrace();					
						logger.warning("Delete Vrf Failed: '" + vrf + "'. Error message: "+ uoe.getLocalizedMessage());
						Notification.show("Delete Vrf Failed: '" + vrf + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
						return;
					} catch (SQLException uoe) {
							uoe.printStackTrace();					
							logger.warning("Delete Vrf Failed: '" + vrf + "'. Error message: "+ uoe.getLocalizedMessage());
							Notification.show("Delete Vrf Failed: '" + vrf + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					} catch (NullPointerException npe) {
							npe.printStackTrace();
							logger.warning("Delete Vrf Failed: '" + vrf + "'. Error message: "+ npe.getLocalizedMessage());
							Notification.show("Delete Vrf Failed: '" + vrf + "'.", npe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					}
					logger.info("Delete Vrf : '" + vrf + "', deleted");
					Notification.show("Delete", "Vrf: '" + vrf+ "', Deleted", Type.HUMANIZED_MESSAGE);
					m_editVrfLayout.setVisible(false);
					m_saveVrfButton.setEnabled(false);
					m_removeVrfButton.setEnabled(false);
					m_vrfTable.select(null);
					m_vrfSearchComboBox.removeItem(vrf);
				}  else {
					logger.warning("Cannot Found Vrf to Delete: '" + vrf + "'.");
					Notification.show("Cannot Found Vrf to Delete: '" + vrf + "'.", "Vrf not found onq SqlContainer", Type.ERROR_MESSAGE);
				}
		}
		});		
		loaded=true;


	}

	private void selectItem() {
		Object vrfId = m_vrfTable.getValue();

		if (vrfId == null)
			return;
		Item vrftableRow = m_vrfTable.getItem(vrfId);
			
		Vrf vrf =new Vrf(vrftableRow.getItemProperty("name").getValue().toString(),
				vrftableRow.getItemProperty("notifylevel").getValue().toString(),
				vrftableRow.getItemProperty("networklevel").getValue().toString(),
				vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
				vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
				vrftableRow.getItemProperty("backupprofile").getValue().toString(),
				vrftableRow.getItemProperty("snmpprofile").getValue().toString()
				);
		m_editorFields.setItemDataSource(vrf);
		m_editVrfLayout.setVisible(true);
		m_saveVrfButton.setEnabled(true);
		m_removeVrfButton.setEnabled(true);
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
	             throw new InvalidValueException("DuplicatedVrfValidator: trovato un duplicato della vrf: " + vrf);
	       }
	}

	
}
