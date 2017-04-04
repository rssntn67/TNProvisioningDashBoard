package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.CategoriaDao;
import org.opennms.vaadin.provision.model.Categoria;

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
@Title("TNPD - Gestione Categorie di Rete")
@Theme("runo")
public class CategorieTab extends DashboardTab {

	private static final long serialVersionUID = -5948892618258879832L;

	public static final String CAT = "name";
	public static final String NOTIF_LEVEL   = "notifylevel";
	public static final String NETWORK_LEVEL = "networklevel";
	public static final String DNS_DOMAIN = "dnsdomain";
	public static final String THRESH_LEVEL  = "thresholdlevel";
	public static final String BACKUP_PROFILE  = "backupprofile";
	public static final String SNMP_PROFILE    = "snmpprofile";
	
	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private CategoriaDao m_catContainer;
	private DnsDomainDao m_domainContainer;
	private boolean loaded=false;
	private boolean add=false;

	private Table m_catTable   	= new Table();
	private Button m_addCatButton  = new Button("Nuova");
	private Button m_saveCatButton  = new Button("Salva");
	private Button m_removeCatButton  = new Button("Elimina");

	private VerticalLayout m_editCatLayout  = new VerticalLayout();
	TextField m_name_cat = new TextField("Categoria");
	
	private BeanFieldGroup<Categoria> m_editorFields;

	public CategorieTab() {
		super();
	}

	@Override
	public void load() {
		updateTabHead();
		if (!loaded) {
			m_domainContainer = getService().getDnsDomainContainer();
			m_catContainer = getService().getCatContainer();
			m_catTable.setContainerDataSource(m_catContainer);
			layout();
			loaded=true;
		}
	}
		
	private void layout() {
		final ComboBox networkCatSearchComboBox = new ComboBox("Select Network Level");
		final ComboBox notifCatSearchComboBox   = new ComboBox("Select Notif Level");
		final ComboBox threshCatSearchComboBox  = new ComboBox("Select Threshold Level");
		final ComboBox catSearchComboBox        = new ComboBox("Select");

		final ComboBox domainComboBox = new ComboBox("Dominio");
		ComboBox networkCatComboBox = new ComboBox("Network Level");
		ComboBox notifCatComboBox   = new ComboBox("Notification Level");
		ComboBox threshCatComboBox  = new ComboBox("Threshold Level");
		ComboBox snmpComboBox  = new ComboBox("SNMP Profile");
		ComboBox backupComboBox  = new ComboBox("Backup Profile");

		List<String> snmpprofiles = new ArrayList<String>(getService().getSnmpProfileContainer().getSnmpProfileMap().keySet());
		Collections.sort(snmpprofiles);
		for (String snmpprofile: snmpprofiles) {
			snmpComboBox.addItem(snmpprofile);
		}

		List<String> backupprofiles = new ArrayList<String>(getService().getBackupProfileContainer().getBackupProfileMap().keySet());
		Collections.sort(backupprofiles);
		for (String backupprofile: backupprofiles) {
			backupComboBox.addItem(backupprofile);
		}

		VerticalLayout searchlayout = new VerticalLayout();
		searchlayout.addComponent(networkCatSearchComboBox);
		searchlayout.addComponent(notifCatSearchComboBox);
		searchlayout.addComponent(threshCatSearchComboBox);

		catSearchComboBox.setWidth("80%");
		searchlayout.addComponent(catSearchComboBox);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);
		
		getLeft().addComponent(new Panel("Search",searchlayout));

		m_catTable.setSizeFull();
		getLeft().addComponent(m_catTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select to Edit----"));
		getLeft().addComponent(bottomLeftLayout);
		getLeft().setSizeFull();

		getRight().addComponent(m_editCatLayout);

		getRightHead().addComponent(m_removeCatButton);
		getRightHead().addComponent(m_saveCatButton);
		getRightHead().addComponent(m_addCatButton);
		getRightHead().setComponentAlignment(m_removeCatButton, Alignment.MIDDLE_LEFT);
		getRightHead().setComponentAlignment(m_saveCatButton, Alignment.MIDDLE_CENTER);
		getRightHead().setComponentAlignment(m_addCatButton, Alignment.MIDDLE_RIGHT);
		

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
						domainComboBox.removeAllItems();					
						for (Object domain: m_domainContainer.getItemIds()) {
							dnsDomainsDeleteComboBox.addItem(domain);
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

		getRight().addComponent(addDomainLayout);

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
						domainComboBox.removeAllItems();
						for (Object domain: m_domainContainer.getItemIds()) {
							dnsDomainsDeleteComboBox.addItem(domain);
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

		getRight().addComponent(delDomainLayout);
		
		m_catTable.setVisibleColumns(new Object[] { "name" });
		m_catTable.setSelectable(true);
		m_catTable.setImmediate(true);

		m_catTable.addValueChangeListener(new Property.ValueChangeListener() {
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
				m_catContainer.removeAllContainerFilters();
				if (networkCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (catSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("name",catSearchComboBox.getValue()));
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
				m_catContainer.removeAllContainerFilters();
				if (notifCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (catSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("name",catSearchComboBox.getValue()));
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
				m_catContainer.removeAllContainerFilters();
				if (threshCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
				if (catSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("name",catSearchComboBox.getValue()));
			}
		});

		List<String> catnames = new ArrayList<String>(m_catContainer.getCatMap().keySet());
		Collections.sort(catnames);
		for (String kcat: catnames) {
			catSearchComboBox.addItem(kcat);
		}
		catSearchComboBox.setInvalidAllowed(false);
		catSearchComboBox.setNullSelectionAllowed(true);		
		catSearchComboBox.setImmediate(true);
		catSearchComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_catContainer.removeAllContainerFilters();
				if (catSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("name",catSearchComboBox.getValue()));
				if (threshCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("thresholdlevel", threshCatSearchComboBox.getValue()));
				if (notifCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("notifylevel", notifCatSearchComboBox.getValue()));
				if (networkCatSearchComboBox.getValue() != null)
					m_catContainer.addContainerFilter(new Compare.Equal("networklevel", networkCatSearchComboBox.getValue()));
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

		m_name_cat.setSizeFull();
		m_name_cat.setWidth(4, Unit.CM);
		m_name_cat.setHeight(6, Unit.MM);
		m_name_cat.setRequired(true);
		m_name_cat.setRequiredError("la categoria non deve essere vuota");
		m_name_cat.addValidator(new RegexpValidator("^[A-Z][A-Za-z\\-0-9]*[A-Za-z0-9]+$", "la categoria deve iniziare con una maiuscola e contenere codici alfanumerici"));
		m_name_cat.addValidator(new DuplicateCatValidator());
		m_name_cat.setEnabled(false);
		m_name_cat.setImmediate(true);

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

        m_editorFields = new BeanFieldGroup<Categoria>(Categoria.class);
		m_editorFields.setBuffered(true);
		m_editorFields.bind(m_name_cat, CAT);
		m_editorFields.bind(networkCatComboBox, NETWORK_LEVEL);
		m_editorFields.bind(domainComboBox, DNS_DOMAIN);
		m_editorFields.bind(snmpComboBox, SNMP_PROFILE);
		m_editorFields.bind(backupComboBox, BACKUP_PROFILE);
		m_editorFields.bind(notifCatComboBox, NOTIF_LEVEL);
		m_editorFields.bind(threshCatComboBox, THRESH_LEVEL);

		FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
		leftGeneralInfo.setMargin(true);
		leftGeneralInfo.addComponent(m_name_cat);
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

		m_editCatLayout.setMargin(true);
		m_editCatLayout.setVisible(false);
		m_editCatLayout.addComponent(new Panel(generalInfo));
		m_editCatLayout.addComponent(new Panel(profileInfo));

		m_saveCatButton.setEnabled(false);
		m_removeCatButton.setEnabled(false);				
		
		m_addCatButton.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5112348680238979359L;

			@Override
			public void buttonClick(ClickEvent event) {
				m_name_cat.setEnabled(true);
				m_catTable.select(null);
				m_editorFields.setItemDataSource(new Categoria());
				m_editCatLayout.setVisible(true);
				m_saveCatButton.setEnabled(true);
				m_removeCatButton.setEnabled(false);
				m_addCatButton.setEnabled(false);
				add=true;
			}
		});
		
		m_saveCatButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					m_editorFields.commit();
				} catch (CommitException ce) {
					ce.printStackTrace();
					logger.warning("Save Categoria Failed: " + ce.getMessage());
					Notification.show("Save Categoria Failed", ce.getMessage(), Type.ERROR_MESSAGE);
					add=false;
					return;
				}
				
				Categoria cat = m_editorFields.getItemDataSource().getBean();
				if (add) {
					logger.info("Adding Categoria: " + cat.getName());
					m_catContainer.add(cat);
					catSearchComboBox.addItem(cat.getName());
				} else {
					logger.info("Updating Categoria: " + cat.getName());
					m_catContainer.save(m_catTable.getValue(), cat);
				}
				add=false;
				try {
					m_catContainer.commit();
					m_catTable.select(null);
					m_editCatLayout.setVisible(false);
					m_addCatButton.setEnabled(true);
					logger.info("Saved Categoria: " + cat.getName());
					Notification.show("Save", "Categoria " + cat.getName()+ " Saved", Type.HUMANIZED_MESSAGE);
				} catch (UnsupportedOperationException uoe) {
					uoe.printStackTrace();					
					logger.warning("Save Categoria Failed: " + uoe.getLocalizedMessage());
					Notification.show("Save Categoria Failed", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
					catSearchComboBox.removeItem(cat.getName());
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					logger.warning("Save Categoria Failed: " + sqle.getLocalizedMessage());
					Notification.show("Save Categoria Failed", sqle.getLocalizedMessage(), Type.ERROR_MESSAGE);
					catSearchComboBox.removeItem(cat.getName());
				}
			}
		});

		m_removeCatButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				String cat = m_editorFields.getItemDataSource().getBean().getName();
				if (m_catContainer.removeItem(m_catTable.getValue())) {
					try {
						m_catContainer.commit();
					} catch (UnsupportedOperationException uoe) {
						uoe.printStackTrace();					
						logger.warning("Delete Categoria Failed: '" + cat + "'. Error message: "+ uoe.getLocalizedMessage());
						Notification.show("Delete Categoria Failed: '" + cat + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
						return;
					} catch (SQLException uoe) {
							uoe.printStackTrace();					
							logger.warning("Delete Categoria Failed: '" + cat + "'. Error message: "+ uoe.getLocalizedMessage());
							Notification.show("Delete Categoria Failed: '" + cat + "'.", uoe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					} catch (NullPointerException npe) {
							npe.printStackTrace();
							logger.warning("Delete Categoria Failed: '" + cat + "'. Error message: "+ npe.getLocalizedMessage());
							Notification.show("Delete Categoria Failed: '" + cat + "'.", npe.getLocalizedMessage(), Type.ERROR_MESSAGE);
							return;
					}
					logger.info("Delete Categoria : '" + cat + "', deleted");
					Notification.show("Delete", "Categoria: '" + cat+ "', Deleted", Type.HUMANIZED_MESSAGE);
					m_editCatLayout.setVisible(false);
					m_saveCatButton.setEnabled(false);
					m_removeCatButton.setEnabled(false);
					m_catTable.select(null);
					catSearchComboBox.removeItem(cat);
				}  else {
					logger.warning("Cannot Found Categoria to Delete: '" + cat + "'.");
					Notification.show("Cannot Found Categoria to Delete: '" + cat + "'.", "Categoria not found onq SqlContainer", Type.ERROR_MESSAGE);
				}
		}
		});		
	}

	private void selectItem() {
		Object catId = m_catTable.getValue();

		if (catId == null)
			return;
		Categoria cat =m_catContainer.get(catId);
		m_editorFields.setItemDataSource(cat);
		m_editCatLayout.setVisible(true);
		m_name_cat.setEnabled(false);
		m_saveCatButton.setEnabled(true);
		m_removeCatButton.setEnabled(true);
	}

	
		
	class DuplicateCatValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String cat = (String)value;
			Categoria data = m_editorFields.getItemDataSource().getBean();
			if (data.getName() != null)
				return;
			logger.info("DuplicatedCatValidator: validating categoria: " + cat);
	         if (m_catContainer.getCatMap().containsKey(cat))
	             throw new InvalidValueException("DuplicatedCatValidator: trovato un duplicato della categoria: " + cat);
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
	
	@Override
	public String getName() {
		return "CatTab";
	}

	
}
