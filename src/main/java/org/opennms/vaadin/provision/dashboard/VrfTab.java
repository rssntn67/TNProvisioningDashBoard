package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.DnsSubDomainDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;
import org.opennms.vaadin.provision.model.Vrf;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
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
	private VrfDao m_vrfContainer;
	private DnsDomainDao m_domainContainer;
	private DnsSubDomainDao m_subdomainContainer;
	private BackupProfileDao m_backupprofilecontainer;
	private SnmpProfileDao m_snmpprofilecontainer;
	private boolean loaded=false;

	private Table m_vrfTable   	= new Table();
	private Button m_addVrfButton  = new Button("Nuova VRF");
	private Button m_saveVrfButton  = new Button("Salva Modifiche");
	private Button m_removeVrfButton  = new Button("Elimina VRF");

	private VerticalLayout m_editVrfLayout  = new VerticalLayout();
	
	private BeanFieldGroup<Vrf> m_editorFields;

	private ComboBox m_dnsAddSubdomainsComboBox = new ComboBox();

	public VrfTab(DashBoardSessionService service) {
		super(service);
	}

	@Override
	public void load() {
		if (!loaded) {
			m_domainContainer = getService().getDnsDomainContainer();
			m_subdomainContainer = getService().getDnsSubDomainContainer();
			m_vrfContainer = getService().getVrfContainer();
			m_vrfTable.setContainerDataSource(m_vrfContainer);
			m_backupprofilecontainer = getService().getBackupProfileContainer();
			m_snmpprofilecontainer = getService().getSnmpProfileContainer();
			loaded=true;
		}
		layout();
	}
		
	private void layout() {
		final ComboBox networkCatSearchComboBox = new ComboBox("Select Vrf Network Level");
		final ComboBox notifCatSearchComboBox   = new ComboBox("Select Vrf Notif Level");
		final ComboBox threshCatSearchComboBox  = new ComboBox("Select Vrf Threshold Level");
		final ComboBox vrfSearchComboBox        = new ComboBox("Select Vrf");

		TextField vrf = new TextField("Vrf");
		final ComboBox domainComboBox = new ComboBox("Dominio");
		ComboBox networkCatComboBox = new ComboBox("Network Level");
		ComboBox notifCatComboBox   = new ComboBox("Notification Level");
		ComboBox threshCatComboBox  = new ComboBox("Threshold Level");
		ComboBox snmpComboBox  = new ComboBox("SNMP Profile");
		ComboBox backupComboBox  = new ComboBox("Backup Profile");

		for (String snmpprofile: m_snmpprofilecontainer.getSnmpProfileMap().keySet()) {
			snmpComboBox.addItem(snmpprofile);
		}

		for (String backupprofile: m_backupprofilecontainer.getBackupProfileMap().keySet()) {
			backupComboBox.addItem(backupprofile);
		}

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setCompositionRoot(splitPanel);
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);

		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(networkCatSearchComboBox);
		searchlayout.addComponent(notifCatSearchComboBox);
		searchlayout.addComponent(threshCatSearchComboBox);

		vrfSearchComboBox.setWidth("80%");
		searchlayout.addComponent(vrfSearchComboBox);
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
		

		//Add Domain
		final TextField dnsDomainTextBox = new TextField();
		dnsDomainTextBox.setImmediate(true);
		dnsDomainTextBox.addValidator(new ValidDnsValidator());
		dnsDomainTextBox.addValidator(new DuplicatedDnsValidator());

		//Delete Domain
		final ComboBox dnsDomainsDeleteComboBox = new ComboBox();
		dnsDomainsDeleteComboBox.setNullSelectionAllowed(false);
		for (Object domain: m_domainContainer.getItemIds())
			dnsDomainsDeleteComboBox.addItem(domain);
		dnsDomainsDeleteComboBox.setImmediate(true);

		// Add subdomain
		final TextField dnsAddSubdomainTextBox = new TextField();
		dnsAddSubdomainTextBox.setImmediate(true);
		dnsAddSubdomainTextBox.addValidator(new ValidDnsValidator());
		dnsAddSubdomainTextBox.addValidator(new DuplicatedSubDomainDnsValidator());
		
		m_dnsAddSubdomainsComboBox.setNullSelectionAllowed(false);
		m_dnsAddSubdomainsComboBox.removeAllItems();
		for (Object domain: m_domainContainer.getItemIds())
			m_dnsAddSubdomainsComboBox.addItem(domain);
		m_dnsAddSubdomainsComboBox.setImmediate(true);

		// Del sub domains
		final ComboBox delDnsSubdomainComboBox = new ComboBox();
		delDnsSubdomainComboBox.setNullSelectionAllowed(false);
		for (Object sdomain: m_subdomainContainer.getItemIds())
			delDnsSubdomainComboBox.addItem(sdomain);

		Button addDomainButton = new Button("Add dns domain");
		addDomainButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 439126521516044933L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (dnsDomainTextBox.getValue() != null) {
					m_domainContainer.add(dnsDomainTextBox.getValue());
					try {
						m_domainContainer.commit();
						logger.info("Added Dns Domain: " + dnsDomainTextBox.getValue());
						Notification.show("Adding", "Dns Domain" + dnsDomainTextBox.getValue()+ " Saved", Type.HUMANIZED_MESSAGE);
						dnsDomainsDeleteComboBox.removeAllItems();
						m_dnsAddSubdomainsComboBox.removeAllItems();
						domainComboBox.removeAllItems();					
						for (Object domain: m_domainContainer.getItemIds()) {
							dnsDomainsDeleteComboBox.addItem(domain);
							m_dnsAddSubdomainsComboBox.addItem(domain);
							domainComboBox.addItem(((RowId) domain).toString());
						}
					} catch (UnsupportedOperationException e) {
						e.printStackTrace();
						logger.warning("Add Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Add Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					} catch (SQLException e) {
						logger.warning("Add Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Add Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});


		HorizontalLayout addDomainInfo  = new HorizontalLayout();
		addDomainInfo.addComponent(dnsDomainTextBox);
		addDomainInfo.addComponent(addDomainButton);

		FormLayout addDomainForm = new FormLayout(new Label("Aggiungi dominio Dns"));
		addDomainForm.addComponent(addDomainInfo);

		VerticalLayout addDomainLayout = new VerticalLayout();
		addDomainLayout.setMargin(true);
		addDomainLayout.setMargin(true);
		addDomainLayout.setVisible(true);
		addDomainLayout.addComponent(new Panel(addDomainForm));

		rightLayout.addComponent(addDomainLayout);

		Button delDomainButton = new Button("Delete dns domain");
		delDomainButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 439126521516044933L;

 			@Override
			public void buttonClick(ClickEvent event) {
				if (dnsDomainsDeleteComboBox.getValue() != null &&
					m_domainContainer.removeItem(dnsDomainsDeleteComboBox.getValue())) {
					try {
						m_domainContainer.commit();
						logger.info("Deleted Dns Domain: " + dnsDomainsDeleteComboBox.getValue());
						Notification.show("Delete", "Dns Domain" + dnsDomainsDeleteComboBox.getValue()+ " Deleted", Type.HUMANIZED_MESSAGE);
						dnsDomainsDeleteComboBox.removeAllItems();
						m_dnsAddSubdomainsComboBox.removeAllItems();
						domainComboBox.removeAllItems();
						for (Object domain: m_domainContainer.getItemIds()) {
							dnsDomainsDeleteComboBox.addItem(domain);
							m_dnsAddSubdomainsComboBox.addItem(domain);
							domainComboBox.addItem(((RowId) domain).toString());
						}
					} catch (UnsupportedOperationException e) {
						e.printStackTrace();
						logger.warning("Delete Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Delete Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					} catch (SQLException e) {
						logger.warning("Delete Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Delete Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});

		HorizontalLayout deldomainInfo = new HorizontalLayout();
		deldomainInfo.addComponent(dnsDomainsDeleteComboBox);
		deldomainInfo.addComponent(delDomainButton);

		FormLayout delDomainForm = new FormLayout(new Label("Cancella un dominio dns"));
		delDomainForm.addComponent(deldomainInfo);

		VerticalLayout delDomainLayout  = new VerticalLayout();
		delDomainLayout.setMargin(true);
		delDomainLayout.setVisible(true);
		delDomainLayout.addComponent(new Panel(delDomainForm));

		rightLayout.addComponent(delDomainLayout);
		
		Button addSubDomainButton = new Button("Add dns sub domain");
		addSubDomainButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 439126521516044933L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (dnsAddSubdomainTextBox.getValue() != null &&
						m_dnsAddSubdomainsComboBox.getValue() != null) {
					m_subdomainContainer.add(dnsAddSubdomainTextBox.getValue()+"."+m_dnsAddSubdomainsComboBox.getValue());
					try {
						m_subdomainContainer.commit();
						logger.info("Added Dns Sub Domain: " + dnsAddSubdomainTextBox.getValue()+"."+m_dnsAddSubdomainsComboBox.getValue());
						Notification.show("Adding", "Dns Sub Domain" + dnsAddSubdomainTextBox.getValue()+"."+m_dnsAddSubdomainsComboBox.getValue()+ " Saved", Type.HUMANIZED_MESSAGE);
						delDnsSubdomainComboBox.removeAllItems();
						for (Object sdomain: m_subdomainContainer.getItemIds())
							delDnsSubdomainComboBox.addItem(sdomain);
					} catch (UnsupportedOperationException e) {
						e.printStackTrace();
						logger.warning("Add Dns Sub Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Add Dns Sub Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					} catch (SQLException e) {
						logger.warning("Add Dns Sub Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Add Dns Sub Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});


		HorizontalLayout addSubDomainInfo  = new HorizontalLayout();
		addSubDomainInfo.addComponent(m_dnsAddSubdomainsComboBox);
		addSubDomainInfo.addComponent(dnsAddSubdomainTextBox);
		addSubDomainInfo.addComponent(addSubDomainButton);

		FormLayout addSubDomainForm = new FormLayout(new Label("Aggiungi sotto dominio Dns"));
		addSubDomainForm.addComponent(addSubDomainInfo);

		VerticalLayout addSubDomainLayout = new VerticalLayout();
		addSubDomainLayout.setMargin(true);
		addSubDomainLayout.setMargin(true);
		addSubDomainLayout.setVisible(true);
		addSubDomainLayout.addComponent(new Panel(addSubDomainForm));

		rightLayout.addComponent(addSubDomainLayout);

		Button delSubDomainButton = new Button("Delete dns sub domain");
		delSubDomainButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 439126521516044933L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (delDnsSubdomainComboBox.getValue() != null &&
					m_subdomainContainer.removeItem(delDnsSubdomainComboBox.getValue())) {
					try {
						m_subdomainContainer.commit();
						logger.info("Deleted Dns Sub Domain: " + delDnsSubdomainComboBox.getValue());
						Notification.show("Delete", "Dns Sub Domain" + delDnsSubdomainComboBox.getValue()+ " Deleted", Type.HUMANIZED_MESSAGE);
						delDnsSubdomainComboBox.removeAllItems();
						for (Object sdomain: m_subdomainContainer.getItemIds())
							delDnsSubdomainComboBox.addItem(sdomain);
					} catch (UnsupportedOperationException e) {
						e.printStackTrace();
						logger.warning("Delete Sub Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Delete Sub Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					} catch (SQLException e) {
						logger.warning("Delete Sub Dns Domain Failed: " + e.getLocalizedMessage());
						Notification.show("Delete Sub Dns Domain Failed", e.getLocalizedMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});

		HorizontalLayout delSubDomainInfo = new HorizontalLayout();
		delSubDomainInfo.addComponent(delDnsSubdomainComboBox);
		delSubDomainInfo.addComponent(delSubDomainButton);

		FormLayout delSubDomainForm = new FormLayout(new Label("Cancella un sotto dominio dns"));
		delSubDomainForm.addComponent(delSubDomainInfo);

		VerticalLayout delSubDomainLayout  = new VerticalLayout();
		delSubDomainLayout.setMargin(true);
		delSubDomainLayout.setVisible(true);
		delSubDomainLayout.addComponent(new Panel(delSubDomainForm));

		rightLayout.addComponent(delSubDomainLayout);

		m_vrfTable.setVisibleColumns(new String[] { "name" });
		m_vrfTable.setSelectable(true);
		m_vrfTable.setImmediate(true);

		m_vrfTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectItem();
			}
		});

		for (String nl: DashBoardUtils.m_network_levels) {
			networkCatSearchComboBox.addItem(nl);
		}
		networkCatSearchComboBox.setInvalidAllowed(false);
		networkCatSearchComboBox.setNullSelectionAllowed(true);		
		networkCatSearchComboBox.setImmediate(true);
		networkCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_vrfContainer.removeAllContainerFilters();
				if (networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",vrfSearchComboBox.getValue()));
			}
		});


		for (String category: DashBoardUtils.m_notify_levels) {
			notifCatSearchComboBox.addItem(category);
		}
		notifCatSearchComboBox.setInvalidAllowed(false);
		notifCatSearchComboBox.setNullSelectionAllowed(true);		
		notifCatSearchComboBox.setImmediate(true);
		notifCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_vrfContainer.removeAllContainerFilters();
				if (notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",vrfSearchComboBox.getValue()));
			}
		});
		
		for (String category: DashBoardUtils.m_threshold_levels) {
			threshCatSearchComboBox.addItem(category);
		}
		threshCatSearchComboBox.setInvalidAllowed(false);
		threshCatSearchComboBox.setNullSelectionAllowed(true);		
		threshCatSearchComboBox.setImmediate(true);
		threshCatSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_vrfContainer.removeAllContainerFilters();
				if (threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",vrfSearchComboBox.getValue()));
			}
		});

		for (String kvrf: m_vrfContainer.getVrfMap().keySet()) {
			vrfSearchComboBox.addItem(kvrf);
		}
		vrfSearchComboBox.setInvalidAllowed(false);
		vrfSearchComboBox.setNullSelectionAllowed(true);		
		vrfSearchComboBox.setImmediate(true);
		vrfSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_vrfContainer.removeAllContainerFilters();
				if (vrfSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("name",vrfSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_vrfContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
			}
		});

		for (String nl: DashBoardUtils.m_network_levels) {
			networkCatComboBox.addItem(nl);
		}

		for ( Object domain: m_domainContainer.getItemIds()) {
			domainComboBox.addItem(((RowId) domain).toString());
		}

		for (String notif: DashBoardUtils.m_notify_levels) {
			notifCatComboBox.addItem(notif);
		}

		for (String threshold: DashBoardUtils.m_threshold_levels) {
			threshCatComboBox.addItem(threshold);
		}

		vrf.setSizeFull();
		vrf.setWidth(4, Unit.CM);
		vrf.setHeight(6, Unit.MM);
		vrf.setRequired(true);
		vrf.setRequiredError("vrf non deve essere vuota");
		vrf.addValidator(new RegexpValidator("^[A-Z][A-Za-z\\-0-9]*[A-Za-z0-9]+$", "la vrf deve iniziare con una maiuscola e contenere codici alfanumerici"));
		vrf.addValidator(new DuplicatedVrfValidator());
		vrf.setImmediate(true);

		networkCatComboBox.setInvalidAllowed(false);
		networkCatComboBox.setNullSelectionAllowed(false);
		networkCatComboBox.setRequired(true);
		networkCatComboBox.setRequiredError("E' necessario scegliere un livello di rete");
		networkCatComboBox.setImmediate(true);

		domainComboBox.setInvalidAllowed(false);
		domainComboBox.setNullSelectionAllowed(false);
		domainComboBox.setRequired(true);
		domainComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		domainComboBox.setImmediate(true);
		
		notifCatComboBox.setInvalidAllowed(false);
		notifCatComboBox.setNullSelectionAllowed(false);
		notifCatComboBox.setRequired(true);
		notifCatComboBox.setRequiredError("E' necessario scegliere una categoria per le notifiche");

		threshCatComboBox.setInvalidAllowed(false);
		threshCatComboBox.setNullSelectionAllowed(false);
		threshCatComboBox.setRequired(true);
		threshCatComboBox.setRequiredError("E' necessario scegliere una categoria per le threshold");

		snmpComboBox.setInvalidAllowed(false);
		snmpComboBox.setNullSelectionAllowed(false);
		snmpComboBox.setRequired(true);
		snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");


        backupComboBox.setInvalidAllowed(false);
        backupComboBox.setNullSelectionAllowed(false);
        backupComboBox.setRequired(true);
        backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");

        m_editorFields = new BeanFieldGroup<Vrf>(Vrf.class);
		m_editorFields.setBuffered(true);
		m_editorFields.bind(vrf, VRF);
		m_editorFields.bind(networkCatComboBox, NETWORK_LEVEL);
		m_editorFields.bind(domainComboBox, DNS_DOMAIN);
		m_editorFields.bind(snmpComboBox, SNMP_PROFILE);
		m_editorFields.bind(backupComboBox, BACKUP_PROFILE);
		m_editorFields.bind(notifCatComboBox, NOTIF_LEVEL);
		m_editorFields.bind(threshCatComboBox, THRESH_LEVEL);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(vrf);
		leftGeneralInfo.addComponent(networkCatComboBox);
		leftGeneralInfo.addComponent(domainComboBox);
		
		VerticalLayout centerGeneralInfo = new VerticalLayout();
		centerGeneralInfo.setMargin(true);
		
		HorizontalLayout catLayout = new HorizontalLayout();
		catLayout.setSizeFull();
		catLayout.addComponent(notifCatComboBox);
		catLayout.addComponent(threshCatComboBox);
		
		HorizontalLayout profLayout = new HorizontalLayout();
		profLayout.setSizeFull();
		profLayout.addComponent(snmpComboBox);
		profLayout.addComponent(backupComboBox);
				
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
					m_vrfContainer.add(vrf);
				} else {
					logger.info("Updating Vrf: " + vrf.getName());
					m_vrfContainer.save(vrfId, vrf);
				}
				if (versionid != null) 
					vrfSearchComboBox.addItem(vrf.getName());
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
					vrfSearchComboBox.removeItem(vrf.getName());
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					logger.warning("Save Vrf Failed: " + sqle.getLocalizedMessage());
					Notification.show("Save Vrf Failed", sqle.getLocalizedMessage(), Type.ERROR_MESSAGE);
					vrfSearchComboBox.removeItem(vrf.getName());
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
					vrfSearchComboBox.removeItem(vrf);
				}  else {
					logger.warning("Cannot Found Vrf to Delete: '" + vrf + "'.");
					Notification.show("Cannot Found Vrf to Delete: '" + vrf + "'.", "Vrf not found onq SqlContainer", Type.ERROR_MESSAGE);
				}
		}
		});		
	}

	private void selectItem() {
		Object vrfId = m_vrfTable.getValue();

		if (vrfId == null)
			return;
		Vrf vrf =m_vrfContainer.get(vrfId);
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
	         if (m_vrfContainer.getVrfMap().containsKey(vrf))
	             throw new InvalidValueException("DuplicatedVrfValidator: trovato un duplicato della vrf: " + vrf);
	       }
	}

	class DuplicatedDnsValidator implements Validator {		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String dns = (String)value;
			logger.info("DuplicatedDnsValidator: validating dns: " + dns);
	         if (m_domainContainer.getDomains().contains(dns))
	             throw new InvalidValueException("DuplicatedDnsValidator: trovato un duplicato del domainio: " + dns);
	       }
	}

	class DuplicatedSubDomainDnsValidator implements Validator {
				
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			if (m_dnsAddSubdomainsComboBox.getValue() == null)
				return;
			String subdomain = m_dnsAddSubdomainsComboBox.getValue().toString();
			String dns = (String)value + "." + subdomain;
			logger.info("DuplicatedSubDomainDnsValidator: validating dns: " + dns);
	         if (m_subdomainContainer.getSubdomains().contains(dns))
	             throw new InvalidValueException("DuplicatedSubDomainDnsValidator: trovato un duplicato del dominio: " + dns);
	       }
	}

	class ValidDnsValidator implements Validator {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1164410348714595136L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			if (value == null)
				return;
			String dns = (String)value;
			logger.info("ValidDnsValidator: validating dns domain: " + dns);
	         if (DashBoardUtils.hasInvalidDnsBind9Label(dns))
	             throw new InvalidValueException("ValidDnsValidator: dominio non valido: " + dns);
	       }
	}
	
}
