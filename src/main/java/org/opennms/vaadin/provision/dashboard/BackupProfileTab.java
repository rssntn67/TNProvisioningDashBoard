package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.model.BackupProfile;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Property.ValueChangeEvent;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;

@Title("TNPD - Backup Profiles")
@Theme("runo")
public class BackupProfileTab extends DashboardTab {

	public static final String BACKUP_PROFILE_NAME = "name";
	public static final String BACKUP_USERNAME     = "username";
	public static final String BACKUP_PASSWORD     = "password";
	public static final String BACKUP_ENABLE       = "enable";
	public static final String BACKUP_CONN         = "connection";
	public static final String BACKUP_AUTOENABLE   = "autoenable";

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private SQLContainer m_backupContainer;
	private boolean loaded = false;
	
	private ComboBox m_backupSearchComboBox        = new ComboBox("Select Backup Profile");
	private Table m_backupTable   	= new Table();
	private Button m_addBackupButton  = new Button("Nuovo Profilo Backup");
	private Button m_saveBackupButton  = new Button("Salva Modifiche");
	private Button m_removeBackupButton  = new Button("Elimina Profilo Backup");

	private TextField m_backup_name = new TextField("Nome");
	private TextField m_backup_user = new TextField("Username");
	private TextField m_backup_pass = new TextField("Password");
	private TextField m_backup_ena  = new TextField("Enable");
	private ComboBox  m_backup_conn = new ComboBox("Select Connection");
	private ComboBox  m_backup_auto = new ComboBox("Select Autoenable");

	private VerticalLayout m_editBackupLayout  = new VerticalLayout();
	
	private BeanFieldGroup<BackupProfile> m_editorFields     = new BeanFieldGroup<BackupProfile>(BackupProfile.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public BackupProfileTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		if (loaded)
			return;
		m_backupContainer = (SQLContainer) getService().getTnDao().getBackupProfileContainer();

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setCompositionRoot(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();

		m_backupSearchComboBox.setWidth("80%");
		searchlayout.addComponent(m_backupSearchComboBox);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);
		
		leftLayout.addComponent(new Panel("Search",searchlayout));

		m_backupTable.setSizeFull();
		leftLayout.addComponent(m_backupTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));
		leftLayout.addComponent(bottomLeftLayout);
		leftLayout.setSizeFull();

		rightLayout.addComponent(m_editBackupLayout);

		HorizontalLayout bottomRightLayout = new HorizontalLayout();
		bottomRightLayout.addComponent(m_removeBackupButton);
		bottomRightLayout.addComponent(m_saveBackupButton);
		bottomRightLayout.addComponent(m_addBackupButton);
		bottomRightLayout.setComponentAlignment(m_removeBackupButton, Alignment.MIDDLE_LEFT);
		bottomRightLayout.setComponentAlignment(m_saveBackupButton, Alignment.MIDDLE_CENTER);
		bottomRightLayout.setComponentAlignment(m_addBackupButton, Alignment.MIDDLE_RIGHT);
		Panel buttonPanel = new Panel(bottomRightLayout);
		rightLayout.addComponent(buttonPanel);
		rightLayout.setExpandRatio(buttonPanel, 3);
		m_backupTable.setContainerDataSource(m_backupContainer);
		m_backupTable.setVisibleColumns(new String[] { "name" });
		m_backupTable.setSelectable(true);
		m_backupTable.setImmediate(true);

		m_backupTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (String backupname: getService().getTnDao().getBackupProfiles().keySet()) {
			m_backupSearchComboBox.addItem(backupname);
		}
		m_backupSearchComboBox.setInvalidAllowed(false);
		m_backupSearchComboBox.setNullSelectionAllowed(true);		
		m_backupSearchComboBox.setImmediate(true);
		m_backupSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_backupContainer.removeAllContainerFilters();
				if (m_backupSearchComboBox.getValue() != null)
					m_backupContainer.addContainerFilter(new Compare.Equal("name",m_backupSearchComboBox.getValue()));
			}
		});


		m_backup_name.setSizeFull();
		m_backup_name.setWidth(4, Unit.CM);
		m_backup_name.setHeight(6, Unit.MM);
		m_backup_name.setRequired(true);
		m_backup_name.setRequiredError("il nome del profilo non deve essere vuoto");
		m_backup_name.addValidator(new RegexpValidator("^[A-Za-z][A-Za-z\\-_0-9]*$", "il profilo backup deve iniziare con un carattere alfabetico"));
		m_backup_name.addValidator(new DuplicatedBackupProfileValidator());
		m_backup_name.setImmediate(true);

		m_backup_user.setRequired(true);
		m_backup_user.setRequiredError("E' necessario specificare un Username");
		m_backup_user.setImmediate(true);

		m_backup_pass.setRequired(true);
		m_backup_pass.setRequiredError("E' necessario specificare un Password");
		m_backup_pass.setImmediate(true);

		m_backup_auto.setNullSelectionAllowed(true);
		m_backup_auto.setImmediate(true);
		m_backup_auto.addItem("A");
		m_backup_auto.addValueChangeListener(new Property.ValueChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (m_backup_auto.getValue() != null) {
					m_backup_ena.setValue("notused");
					m_backup_ena.setEnabled(false);
				}
			}
		});

		m_backup_ena.setRequired(true);
		m_backup_ena.setRequiredError("E' necessario specificare un Enable Password | notused");
		m_backup_ena.setImmediate(true);

		m_backup_conn.setRequired(true);
		m_backup_conn.setNullSelectionAllowed(false);
		m_backup_conn.setRequiredError("E' necessario specificare una connessione");
		m_backup_conn.setImmediate(true);
		m_backup_conn.addItem("tftp");
		m_backup_conn.addItem("http");
		m_backup_conn.addItem("ssh");
		m_backup_conn.addItem("telnet");
		
		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_backup_name, BACKUP_PROFILE_NAME);
		m_editorFields.bind(m_backup_user, BACKUP_USERNAME);
		m_editorFields.bind(m_backup_pass, BACKUP_PASSWORD);
		m_editorFields.bind(m_backup_ena,  BACKUP_ENABLE);
		m_editorFields.bind(m_backup_auto, BACKUP_AUTOENABLE);
		m_editorFields.bind(m_backup_conn, BACKUP_CONN);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_backup_name);
		leftGeneralInfo.addComponent(m_backup_user);
		leftGeneralInfo.addComponent(m_backup_pass);
		leftGeneralInfo.addComponent(m_backup_auto);
		leftGeneralInfo.addComponent(m_backup_ena);
		leftGeneralInfo.addComponent(m_backup_conn);

		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
						
		HorizontalLayout generalInfo = new HorizontalLayout();
		generalInfo.addComponent(leftGeneralInfo);
		generalInfo.setExpandRatio(leftGeneralInfo, 3);

				
		m_editBackupLayout.setMargin(true);
		m_editBackupLayout.setVisible(false);
		m_editBackupLayout.addComponent(new Panel(generalInfo));

		m_saveBackupButton.setEnabled(false);
		m_removeBackupButton.setEnabled(false);				
		
		m_addBackupButton.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5112348680238979359L;

			@Override
			public void buttonClick(ClickEvent event) {
				m_backupTable.unselect(null);
				m_editorFields.setItemDataSource(new BackupProfile());
				m_editBackupLayout.setVisible(true);
				m_saveBackupButton.setEnabled(true);
				m_removeBackupButton.setEnabled(true);
			}
		});
		
		m_saveBackupButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				Object backupId = m_backupTable.getValue();
				try {
					m_editorFields.commit();
				} catch (CommitException ce) {
					logger.warning("Save Backup Profile Failed: " + ce.getLocalizedMessage());
					Notification.show("Save Backup Profile Failed", ce.getLocalizedMessage(), Type.ERROR_MESSAGE);
				}
				
				BackupProfile backup = m_editorFields.getItemDataSource().getBean();
				Integer versionid = null;
				if (backupId == null) {
					backupId = m_backupContainer.addItem();
					versionid = 0;
					logger.info("Adding Backup Profile: " + backup.getName());
				} else {
					logger.info("Updating Backup Profile: " + backup.getName());
				}
				m_backupContainer.getContainerProperty(backupId, "name").setValue(backup.getName());
				m_backupContainer.getContainerProperty(backupId, "username").setValue(backup.getUsername());
				m_backupContainer.getContainerProperty(backupId, "password").setValue(backup.getPassword());
				m_backupContainer.getContainerProperty(backupId, "enable").setValue(backup.getEnable());
				m_backupContainer.getContainerProperty(backupId, "connection").setValue(backup.getConnection());
				m_backupContainer.getContainerProperty(backupId, "auto_enable").setValue(backup.getAutoenable());
				if (versionid != null) 
					m_backupSearchComboBox.addItem(backup.getName());
				try {
					m_backupContainer.commit();
					m_backupTable.select(null);
					m_editBackupLayout.setVisible(false);
					logger.info("Saved Backup Profile: " + backup.getName());
					Notification.show("Save", "Backup Profile " + backup.getName()+ " Saved", Type.HUMANIZED_MESSAGE);
				} catch (UnsupportedOperationException uoe) {
					uoe.printStackTrace();					
					logger.warning("Save Backup Profile Failed: " + uoe.getLocalizedMessage());
					Notification.show("Save Backup Profile Failed", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
					m_backupSearchComboBox.removeItem(backup.getName());
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					logger.warning("Save Backup Profile Failed: " + sqle.getLocalizedMessage());
					Notification.show("Save Backup Profile Failed", sqle.getLocalizedMessage(), Type.ERROR_MESSAGE);
					m_backupSearchComboBox.removeItem(backup.getName());
				}
			}
		});

		m_removeBackupButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				String backup = m_editorFields.getItemDataSource().getBean().getName();
				if (m_backupContainer.removeItem(m_backupTable.getValue())) {
					try {
						m_backupContainer.commit();
					} catch (UnsupportedOperationException uoe) {
						uoe.printStackTrace();					
						logger.warning("Delete Backup Profile Failed: '" + backup + "'. Error message: "+ uoe.getLocalizedMessage());
						Notification.show("Delete Backup Profile Failed: '" + backup + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
						return;
					} catch (SQLException uoe) {
							uoe.printStackTrace();					
							logger.warning("Delete Backup Profile Failed: '" + backup + "'. Error message: "+ uoe.getLocalizedMessage());
							Notification.show("Delete Backup Profile Failed: '" + backup + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					} catch (NullPointerException npe) {
							npe.printStackTrace();
							logger.warning("Delete Backup Profile Failed: '" + backup + "'. Error message: "+ npe.getLocalizedMessage());
							Notification.show("Delete Backup Profile Failed: '" + backup + "'.", npe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					}
					logger.info("Delete Backup : '" + backup + "', deleted");
					Notification.show("Delete", "Backup Profile: '" + backup+ "', Deleted", Type.HUMANIZED_MESSAGE);
					m_editBackupLayout.setVisible(false);
					m_saveBackupButton.setEnabled(false);
					m_removeBackupButton.setEnabled(false);
					m_backupTable.select(null);
					m_backupSearchComboBox.removeItem(backup);
				}  else {
					logger.warning("Cannot Found Backup Profile to Delete: '" + backup + "'.");
					Notification.show("Cannot Found Backup Profile to Delete: '" + backup + "'.", "Backup Profile not found onq SqlContainer", Type.ERROR_MESSAGE);
				}
		}
		});		
		loaded=true;


	}

	@SuppressWarnings("unchecked")
	private void selectItem() {
		Object backupId = m_backupTable.getValue();

		if (backupId == null)
			return;
		Item backuptableRow = m_backupTable.getItem(backupId);
			
		BackupProfile backup = new BackupProfile(backuptableRow.getItemProperty("name"),
				backuptableRow.getItemProperty("username"),
				backuptableRow.getItemProperty("password"),
				backuptableRow.getItemProperty("enable"),
				backuptableRow.getItemProperty("connection"),
				backuptableRow.getItemProperty("auto_enable")
				);
		m_editorFields.setItemDataSource(backup);
		m_editBackupLayout.setVisible(true);
		m_saveBackupButton.setEnabled(true);
		m_removeBackupButton.setEnabled(true);
	}

	
		
	class DuplicatedBackupProfileValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String backup = (String)value;
			BackupProfile data = m_editorFields.getItemDataSource().getBean();
			if (data.getName() != null)
				return;
			logger.info("DuplicatedBackupProfileValidator: validating Backup Profile: " + backup);
	         if (getService().getTnDao().getBackupProfiles().containsKey(backup))
	             throw new InvalidValueException("DuplicatedBackupProfileValidator: trovato un duplicato della profilo backup: " + backup);
	       }
	}

}
