package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.SnmpProfile;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;

@Title("TNPD - Snmp Profiles")
@Theme("runo")
public class SnmpProfileTab extends DashboardTab {

	public static final String SNMP_PROFILE_NAME  = "name";
	public static final String SNMP_COMMUNITY     = "community";
	public static final String SNMP_VERSION       = "version";
	public static final String SNMP_TIMEOUT       = "timeout";
	public static final String SNMP_RETRIES       = "retries";
	public static final String SNMP_MAXVARSPERPDU = "maxvarsperpdu";

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private SnmpProfileDao m_snmpContainer;
	private boolean loaded = false;
	private boolean add = false;
	private Table m_snmpTable   	= new Table();
	private Button m_addSnmpButton  = new Button("Nuovo Profilo Snmp");
	private Button m_saveSnmpButton  = new Button("Salva Modifiche");
	private Button m_removeSnmpButton  = new Button("Elimina Profilo Snmp");

	private Set<String> m_fastSnmpProfile = new HashSet<String>();

	private VerticalLayout m_editSnmpLayout  = new VerticalLayout();
	TextField m_snmp_name = new TextField("Nome");
	
	private BeanFieldGroup<SnmpProfile> m_editorFields     = new BeanFieldGroup<SnmpProfile>(SnmpProfile.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public SnmpProfileTab(LoginBox login,DashBoardSessionService service) {
		super(login,service);
	}

	@Override
	public void load() {
		updateTabHead();
		if (!loaded) {
			m_snmpContainer = getService().getSnmpProfileContainer();
			m_snmpTable.setContainerDataSource(m_snmpContainer);
			for (FastServiceDevice fsd: getService().getFastServiceDeviceContainer().getFastServiceDevices()) {
				m_fastSnmpProfile.add(fsd.getSnmpprofile());
			}
			layout();
			loaded=true;
		}
	}

	private void layout() {
		final ComboBox snmpSearchComboBox        = new ComboBox("Select Snmp Profile");
		TextField snmp_comm = new TextField("Community");
		ComboBox snmp_vers = new ComboBox("Version v1|v2c");
		ComboBox snmp_time = new ComboBox("Timeout (ms)");
		ComboBox snmp_retries = new ComboBox("Retries");
		ComboBox snmp_max = new ComboBox("MaxVarsPerPdu");

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		getCore().addComponent(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();

		snmpSearchComboBox.setWidth("80%");
		searchlayout.addComponent(snmpSearchComboBox);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);
		
		leftLayout.addComponent(new Panel("Search",searchlayout));

		m_snmpTable.setSizeFull();
		leftLayout.addComponent(m_snmpTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));
		leftLayout.addComponent(bottomLeftLayout);
		leftLayout.setSizeFull();

		rightLayout.addComponent(m_editSnmpLayout);

		HorizontalLayout bottomRightLayout = new HorizontalLayout();
		bottomRightLayout.addComponent(m_removeSnmpButton);
		bottomRightLayout.addComponent(m_saveSnmpButton);
		bottomRightLayout.addComponent(m_addSnmpButton);
		bottomRightLayout.setComponentAlignment(m_removeSnmpButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveSnmpButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_addSnmpButton, Alignment.MIDDLE_RIGHT);
		Panel buttonPanel = new Panel(bottomRightLayout);
		rightLayout.addComponent(buttonPanel);
		rightLayout.setExpandRatio(buttonPanel, 3);
		m_snmpTable.setVisibleColumns(new Object[] { "name" });
		m_snmpTable.setSelectable(true);
		m_snmpTable.setImmediate(true);

		m_snmpTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (Object snmpname: m_snmpContainer.getItemIds()) {
			snmpSearchComboBox.addItem(((RowId)snmpname).toString());
		}
		snmpSearchComboBox.setInvalidAllowed(false);
		snmpSearchComboBox.setNullSelectionAllowed(true);		
		snmpSearchComboBox.setImmediate(true);
		snmpSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_snmpContainer.removeAllContainerFilters();
				if (snmpSearchComboBox.getValue() != null)
					m_snmpContainer.addContainerFilter(new Compare.Equal("name",snmpSearchComboBox.getValue()));
			}
		});


		m_snmp_name.setSizeFull();
		m_snmp_name.setWidth(4, Unit.CM);
		m_snmp_name.setHeight(6, Unit.MM);
		m_snmp_name.setRequired(true);
		m_snmp_name.setRequiredError("il nome del profilo non deve essere vuoto");
		m_snmp_name.addValidator(new RegexpValidator("^[A-Za-z][A-Za-z\\-_0-9]*$", "il profilo snmp deve iniziare con un carattere alfabetico"));
		m_snmp_name.addValidator(new DuplicatedSnmpProfileValidator());
		m_snmp_name.setImmediate(true);

		snmp_comm.setRequired(true);
		snmp_comm.setRequiredError("E' necessario specificare una community");
		snmp_comm.setImmediate(true);

		snmp_vers.setRequired(true);
		snmp_vers.setRequiredError("E' necessario specificare una versione");
		snmp_vers.setImmediate(true);
		snmp_vers.setNullSelectionAllowed(false);
		snmp_vers.addItem("v1");
		snmp_vers.addItem("v2c");

		snmp_time.setRequired(true);
		snmp_time.setRequiredError("E' necessario specificare il timeout");
		snmp_time.setNullSelectionAllowed(false);
		snmp_time.setImmediate(true);
		snmp_time.addItem("1800");
		snmp_time.addItem("3000");
		snmp_time.addItem("3600");
		snmp_time.addItem("5000");
		snmp_time.addItem("10000");

		snmp_retries.setRequired(true);
		snmp_retries.setRequiredError("E' necessario specificare il numero di retry");
		snmp_retries.setImmediate(true);
		snmp_retries.setNullSelectionAllowed(false);
		snmp_retries.addItem("1");
		snmp_retries.addItem("3");
		snmp_retries.addItem("5");

		snmp_max.setRequired(true);
		snmp_max.setRequiredError("E' necessario specificare il numero di retry");
		snmp_max.setImmediate(true);
		snmp_max.setNullSelectionAllowed(false);
		snmp_max.addItem("1");
		snmp_max.addItem("5");
		snmp_max.addItem("10");

		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_snmp_name, SNMP_PROFILE_NAME);
		m_editorFields.bind(snmp_comm, SNMP_COMMUNITY);
		m_editorFields.bind(snmp_vers, SNMP_VERSION);
		m_editorFields.bind(snmp_time, SNMP_TIMEOUT);
		m_editorFields.bind(snmp_retries, SNMP_RETRIES);
		m_editorFields.bind(snmp_max, SNMP_MAXVARSPERPDU);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_snmp_name);
		leftGeneralInfo.addComponent(snmp_comm);
		leftGeneralInfo.addComponent(snmp_vers);
		leftGeneralInfo.addComponent(snmp_time);
		leftGeneralInfo.addComponent(snmp_retries);
		leftGeneralInfo.addComponent(snmp_max);
		
		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
						
		HorizontalLayout generalInfo = new HorizontalLayout();
		generalInfo.addComponent(leftGeneralInfo);
		generalInfo.setExpandRatio(leftGeneralInfo, 3);

				
		m_editSnmpLayout.setMargin(true);
		m_editSnmpLayout.setVisible(false);
		m_editSnmpLayout.addComponent(new Panel(generalInfo));

		m_saveSnmpButton.setEnabled(false);
		m_removeSnmpButton.setEnabled(false);				
		
		m_addSnmpButton.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5112348680238979359L;

			@Override
			public void buttonClick(ClickEvent event) {
				add = true;
				m_snmpTable.unselect(null);
				m_editorFields.setItemDataSource(new SnmpProfile());
				m_editSnmpLayout.setVisible(true);
				m_saveSnmpButton.setEnabled(true);
				m_removeSnmpButton.setEnabled(false);
				m_addSnmpButton.setEnabled(false);
			}
		});
		
		m_saveSnmpButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					m_editorFields.commit();
				} catch (CommitException ce) {
					ce.printStackTrace();
					logger.warning("Save Snmp Profile Failed: " + ce.getMessage());
					Notification.show("Save Snmp Profile Failed", ce.getMessage(), Type.ERROR_MESSAGE);
					add=false;
					return;
				}

				SnmpProfile snmp = m_editorFields.getItemDataSource().getBean();
				if (add) {
					logger.info("Adding Snmp Profile: " + snmp.getName());
					m_snmpContainer.add(snmp);
					snmpSearchComboBox.addItem(snmp.getName());
				} else {
					logger.info("Updating Snmp Profile: " + snmp.getName());
					m_snmpContainer.save(m_snmpTable.getValue(),snmp);
				}
				add=false;
				try {
					m_snmpContainer.commit();
					m_snmpTable.select(null);
					m_editSnmpLayout.setVisible(false);
					m_addSnmpButton.setEnabled(true);
					logger.info("Saved Snmp Profile: " + snmp.getName());
					Notification.show("Save", "Snmp Profile " + snmp.getName()+ " Saved", Type.HUMANIZED_MESSAGE);
				} catch (UnsupportedOperationException uoe) {
					uoe.printStackTrace();					
					logger.warning("Save Snmp Profile Failed: " + uoe.getLocalizedMessage());
					Notification.show("Save Snmp Profile Failed", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
					snmpSearchComboBox.removeItem(snmp.getName());
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					logger.warning("Save Snmp Profile Failed: " + sqle.getLocalizedMessage());
					Notification.show("Save Snmp Profile Failed", sqle.getLocalizedMessage(), Type.ERROR_MESSAGE);
					snmpSearchComboBox.removeItem(snmp.getName());
				}
			}
		});

		m_removeSnmpButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				String snmp = m_editorFields.getItemDataSource().getBean().getName();
				if (m_snmpContainer.removeItem(m_snmpTable.getValue())) {
					try {
						m_snmpContainer.commit();
					} catch (UnsupportedOperationException uoe) {
						uoe.printStackTrace();					
						logger.warning("Delete Snmp Profile Failed: '" + snmp + "'. Error message: "+ uoe.getLocalizedMessage());
						Notification.show("Delete Snmp Profile Failed: '" + snmp + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
						return;
					} catch (SQLException uoe) {
							uoe.printStackTrace();					
							logger.warning("Delete Snmp Profile Failed: '" + snmp + "'. Error message: "+ uoe.getLocalizedMessage());
							Notification.show("Delete Snmp Profile Failed: '" + snmp + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					} catch (NullPointerException npe) {
							npe.printStackTrace();
							logger.warning("Delete Snmp Profile Failed: '" + snmp + "'. Error message: "+ npe.getLocalizedMessage());
							Notification.show("Delete Snmp Profile Failed: '" + snmp + "'.", npe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					}
					logger.info("Delete Snmp : '" + snmp + "', deleted");
					Notification.show("Delete", "Snmp Profile: '" + snmp+ "', Deleted", Type.HUMANIZED_MESSAGE);
					m_editSnmpLayout.setVisible(false);
					m_saveSnmpButton.setEnabled(false);
					m_removeSnmpButton.setEnabled(false);
					m_snmpTable.select(null);
					snmpSearchComboBox.removeItem(snmp);
				}  else {
					logger.warning("Cannot Found Snmp Profile to Delete: '" + snmp + "'.");
					Notification.show("Cannot Found Snmp Profile to Delete: '" + snmp + "'.", "Snmp Profile not found onq SqlContainer", Type.ERROR_MESSAGE);
				}
		}
		});		
	}

	private void selectItem() {
		Object snmpId = m_snmpTable.getValue();

		if (snmpId == null)
			return;
		SnmpProfile snmp = m_snmpContainer.get(snmpId);
		m_editorFields.setItemDataSource(snmp);
		m_editSnmpLayout.setVisible(true);
		if (m_fastSnmpProfile.contains(snmp.getName())) {
			m_snmp_name.setEnabled(false);
			m_saveSnmpButton.setEnabled(true);
			m_removeSnmpButton.setEnabled(false);
			Notification.show("Delete and Modify Name not permitted", "Name is referenced in Fast", Type.WARNING_MESSAGE);
		} else {
			m_snmp_name.setEnabled(true);
			m_saveSnmpButton.setEnabled(true);
			m_removeSnmpButton.setEnabled(true);
		}
	}

	
		
	class DuplicatedSnmpProfileValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String snmp = (String)value;
			SnmpProfile data = m_editorFields.getItemDataSource().getBean();
			if (data.getName() != null)
				return;
			logger.info("DuplicatedSnmpProfileValidator: validating Snmp Profile: " + snmp);
	         if (m_snmpContainer.getSnmpProfileMap().containsKey(snmp))
	             throw new InvalidValueException("DuplicatedSnmpProfileValidator: trovato un duplicato della profilo snmp: " + snmp);
	       }
	}
	
	@Override
	public String getName() {
		return "SnmpProfileTab";
	}


}
