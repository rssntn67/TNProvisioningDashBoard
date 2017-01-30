package org.opennms.vaadin.provision.dashboard;



import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.SnmpProfile;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public abstract class RequisitionTab extends DashboardTab {

	/**
	 * 
	 */
	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
	private static final long serialVersionUID = 4694567853140078034L;
	
	private VerticalLayout m_left = new VerticalLayout();;
	private VerticalLayout m_right = new VerticalLayout();;
	
	private Button m_syncRequisButton  = new Button("Sync");
	private Button m_addNewNodeButton  = new Button("Nuovo Nodo");
	
	private Button m_saveNodeButton  = new Button("Salva Modifiche");
	private Button m_resetNodeButton   = new Button("Annulla Modifiche");
	private Button m_removeNodeButton  = new Button("Elimina Nodo");
	
	private Table m_requisitionTable   	= new Table();
	
	private ComboBox m_descrComboBox = new ComboBox("Descrizione");
	private ComboBox m_parentComboBox = new ComboBox("Dipende da");
	private TextField m_hostname = new TextField("Hostname");
	private TextField m_primary = new TextField(DashBoardUtils.PRIMARY);
	private ComboBox m_domainComboBox = new ComboBox("Dominio");

	private ComboBox m_snmpComboBox  = new ComboBox("SNMP Profile");

	private TextField m_city = new TextField("Citta'");
	private TextField m_address = new TextField("Indirizzo");
	private TextField m_building = new TextField("Edificio");



	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	RequisitionTab(LoginBox login, DashBoardSessionService service) {
		super(login,service);
    	m_syncRequisButton.addClickListener(this);
    	m_syncRequisButton.setImmediate(true);
    	
       	m_addNewNodeButton.addClickListener(this);
    	m_addNewNodeButton.setImmediate(true);
    	
		getHead().addComponent(m_syncRequisButton);
		getHead().addComponent(m_addNewNodeButton);

		m_saveNodeButton.addClickListener(this);
		m_saveNodeButton.setImmediate(true);		
		m_saveNodeButton.setEnabled(false);

		m_removeNodeButton.addClickListener(this);
		m_removeNodeButton.setImmediate(true);		
		m_removeNodeButton.setEnabled(false);				
		
		m_resetNodeButton.addClickListener(this);
		m_resetNodeButton.setImmediate(true);				
		m_resetNodeButton.setEnabled(false);
		
		HorizontalLayout editNodeButtons = new HorizontalLayout();
		editNodeButtons.addComponent(m_removeNodeButton);
		editNodeButtons.addComponent(m_saveNodeButton);
		editNodeButtons.addComponent(m_resetNodeButton);
		editNodeButtons.setComponentAlignment(m_removeNodeButton, Alignment.MIDDLE_LEFT);
		editNodeButtons.setComponentAlignment(m_saveNodeButton, Alignment.MIDDLE_CENTER);
		editNodeButtons.setComponentAlignment(m_resetNodeButton,  Alignment.MIDDLE_RIGHT);


		m_right.addComponent(new Panel(editNodeButtons));
		
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.addComponent(m_left);
		splitPanel.addComponent(m_right);
		splitPanel.setSplitPosition(29,Unit.PERCENTAGE);

		getCore().addComponent(splitPanel);
		
		m_hostname.setSizeFull();
		m_hostname.setWidth(4, Unit.CM);
		m_hostname.setHeight(6, Unit.MM);
		m_hostname.setRequired(true);
		m_hostname.setRequiredError("hostname must be defined");
		m_hostname.addValidator(new DnsNodeLabelValidator());
		m_hostname.addValidator(new SubdomainValidator());
		m_hostname.addValidator(new DuplicatedForeignIdValidator());
		m_hostname.addValidator(new DuplicatedNodelabelValidator());
		m_hostname.setImmediate(true);

		m_primary.setRequired(true);
		m_primary.setRequiredError("E' necessario specificare un indirizzo ip primario");
		m_primary.setImmediate(true);
		m_primary.addValidator(new IpValidator());
		m_primary.addValidator(new DuplicatedPrimaryValidator());

		m_descrComboBox.setInvalidAllowed(false);
		m_descrComboBox.setNullSelectionAllowed(false);
		m_descrComboBox.setWidth(8, Unit.CM);

		m_parentComboBox.setInvalidAllowed(false);
		m_parentComboBox.setNullSelectionAllowed(true);
		for (String nodelabel :getService().getNodeLabels())
			m_parentComboBox.addItem(nodelabel);
		
		m_domainComboBox.setInvalidAllowed(false);
		m_domainComboBox.setNullSelectionAllowed(false);
		m_domainComboBox.setRequired(true);
		m_domainComboBox.setRequiredError("Bisogna scegliere un dominio valido");
		m_domainComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = -2209110948375990804L;

			@Override
		    public void valueChange(ValueChangeEvent event) {
	        	logger.info("domain combo box value change:"+ m_domainComboBox.getValue());
		        try {
		            m_hostname.validate();
		            m_hostname.setComponentError(null); // MAGIC CODE HERE!!!
		        } catch (Exception e) {
		        	logger.info("not working validation");
		        }
		    }
		});
		m_domainComboBox.setImmediate(true);
		
		m_domainComboBox.removeAllItems();
		for (final String domain: getService().getDnsDomainContainer().getDomains()) {
			m_domainComboBox.addItem(domain);
		}
		m_requisitionTable.setSizeFull();
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue() == null) {
					m_right.setVisible(false);
					return;
				}
				Object contactId = getRequisitionTable().getValue();
				if (contactId == null) 
					return;
				@SuppressWarnings("unchecked")
				BasicNode node = ((BeanItem<? extends BasicNode>)m_requisitionTable
					.getItem(contactId)).getBean();
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
				m_descrComboBox.removeAllItems();
				if (node.getDescr() != null)
					m_descrComboBox.addItem(node.getDescr());

				selectItem(node);
				getRight().setVisible(true);
				enableNodeButtons();

				
			}
		});
		
		m_city.setWidth(8, Unit.CM);
		m_city.setHeight(6, Unit.MM);
		
		m_address.setWidth(8, Unit.CM);
		m_address.setHeight(6, Unit.MM);

		m_building.setWidth(8, Unit.CM);
		m_building.setHeight(6, Unit.MM);
		
		m_snmpComboBox.setInvalidAllowed(false);
		m_snmpComboBox.setNullSelectionAllowed(false);
		m_snmpComboBox.setRequired(true);
		m_snmpComboBox.setRequiredError("E' necessario scegliere un profilo snmp");

		Map<String,SnmpProfile> snmpprofilemap = 
				getService().getSnmpProfileContainer().getSnmpProfileMap();
		List<String> snmpprofiles = new ArrayList<String>(snmpprofilemap.keySet());
		Collections.sort(snmpprofiles);
		for (String snmpprofile: snmpprofiles) {
			m_snmpComboBox.addItem(snmpprofile);
			m_snmpComboBox.setItemCaption(snmpprofile, 
					snmpprofile + 
					"(community:"+snmpprofilemap.get(snmpprofile).getCommunity()+")"
					+ "(version:"+ snmpprofilemap.get(snmpprofile).getVersion()+")");
		}
	}
		
	public abstract void selectItem(BasicNode node);
	public abstract String getRequisitionName();
	public abstract void applyFilter(String filter);
	public abstract void cleanSearchBox();
	public abstract BeanContainer<String,? extends BasicNode> getRequisitionContainer();
	public abstract BasicNode addBean();
	public abstract BeanFieldGroup<? extends BasicNode> getBeanFieldGroup();
	

		
	@Override
	public void buttonClick(ClickEvent event) {
		super.buttonClick(event);
		if (event.getButton() == m_syncRequisButton) {
	    	sync();
	    } else if (event.getButton() == m_addNewNodeButton) {
	    	newNode();
	    } else if (event.getButton() == m_saveNodeButton) {
	    	save();
	    } else if (event.getButton() == m_removeNodeButton) {
	    	remove();
	    } else if (event.getButton() == m_resetNodeButton) {
	    	reset();
	    }
	}
	
	public void enableNodeButtons() {
		m_saveNodeButton.setEnabled(true);
		m_removeNodeButton.setEnabled(true);
		m_resetNodeButton.setEnabled(true);
	}

	public void disableNodeButtons() {
		m_saveNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);
		m_resetNodeButton.setEnabled(false);
	}

	private void sync() {
		try {
			getService().sync(getRequisitionName());
			logger.info("Sync succeed foreign source: " +getRequisitionName());
			Notification.show("Sync " + getRequisitionName(), "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync Failed foreign source: " +getRequisitionName() + " " + e.getLocalizedMessage());
			Notification.show("Sync Failed foreign source" + getRequisitionName(), e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}		
	}

	private void  newNode() {
		cleanSearchBox();
		BasicNode bean = addBean();
		m_requisitionTable.select(bean.getNodeLabel());
		m_descrComboBox.removeAllItems();
		if (bean.getDescr() != null)
			m_descrComboBox.addItem(bean.getDescr());
		selectItem(bean);
		getRequisitionContainer().removeAllContainerFilters();
		m_hostname.focus();
	}

	public void save() {
		try {
			getBeanFieldGroup().commit();
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() == null) {
				node.setForeignId(node.getHostname());
				node.setValid(true);
				getService().add(node);
				logger.info("Added: " + getBeanFieldGroup().getItemDataSource().getBean().getNodeLabel());
				Notification.show("Save", "Node " +getBeanFieldGroup().getItemDataSource().getBean().getNodeLabel() + " Added", Type.HUMANIZED_MESSAGE);
			} else {
				getService().update(node);
				node.setValid(getService().isValid(node));
				logger.info("Updated: " + getBeanFieldGroup().getItemDataSource().getBean().getNodeLabel());
				Notification.show("Save", "Node " +getBeanFieldGroup().getItemDataSource().getBean().getNodeLabel() + " Updated", Type.HUMANIZED_MESSAGE);
			}
			applyFilter(node.getHostname());
			getRequisitionContainer().removeAllContainerFilters();
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

	public void remove() {
		m_right.setVisible(false);
		m_saveNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);
		m_resetNodeButton.setEnabled(false);
		BeanItem<? extends BasicNode> node = getBeanFieldGroup().getItemDataSource();
		logger.info("Deleting: " + node.getBean().getNodeLabel());
		if (node.getBean().getForeignId() !=  null) {
			try {
				getService().delete(node.getBean());
				Notification.show("Delete Node From Requisition", "Done", Type.HUMANIZED_MESSAGE);
			} catch (UniformInterfaceException e) {
				logger.warning(e.getLocalizedMessage()+" Reason: " + e.getResponse().getStatusInfo().getReasonPhrase());
				Notification.show("Delete Node From Requisition", "Failed: "+e.getLocalizedMessage()+ " Reason: " +
				e.getResponse().getStatusInfo().getReasonPhrase(), Type.ERROR_MESSAGE);
				return;
			}
		}
		if ( ! getRequisitionContainer().removeItem(node.getBean().getNodeLabel()))
			getRequisitionContainer().removeItem(getRequisitionContainer().getIdByIndex(0));
		logger.info("Node Deleted");
		Notification.show("Delete", "Done", Type.HUMANIZED_MESSAGE);
	}
	
	public void reset() {
			getBeanFieldGroup().discard();
			m_right.setVisible(false);
			m_requisitionTable.unselect(m_requisitionTable.getValue());
			m_saveNodeButton.setEnabled(false);
			m_removeNodeButton.setEnabled(false);
			m_resetNodeButton.setEnabled(false);
		}

	class SubdomainValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4896238666294939805L;
		
		@Override
		public void validate(Object value) throws InvalidValueException {
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
			logger.info("SubdomainValidator: validating hostname: " + hostname);
			 if (hasUnSupportedDnsDomain(hostname, nodelabel, getService().getDnsSubDomainContainer().getSubdomains()))
	             throw new InvalidValueException("There is no dns domain defined for: " + hostname);
	       }
	}

	class DuplicatedForeignIdValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = (String)value;
			logger.info("DuplicatedForeignIdValidator: validating foreignId: " + hostname);
	         if (getService().hasDuplicatedForeignId((hostname)))
	             throw new InvalidValueException("The hostname exists: cannot duplicate hostname: " + hostname);
	       }
	}
	
	class DuplicatedPrimaryValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String ip = (String)value;
			logger.info("DuplicatedPrimaryValidator: validating ip: " + ip);
	         if (getService().hasDuplicatedPrimary((ip)))
	             throw new InvalidValueException("DuplicatedPrimaryValidator: trovato un duplicato del primary ip: " + ip);
	       }
	}

	class IpValidator implements Validator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6243101356369643407L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			String ip = (String)value;
			logger.info("IpValidator: validating ip: " + ip);
	         if (DashBoardUtils.hasInvalidIp((ip)))
	             throw new InvalidValueException("Deve essere inserito un valido indirizzo ip (0.0.0.0 e 127.0.0.0 non sono validi): indirizzo sbagliato:" + ip);
	       }
	}
	
	class DuplicatedNodelabelValidator implements Validator {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5690578176254609879L;

		@Override
		public void validate( Object value) throws InvalidValueException {
			BasicNode node = getBeanFieldGroup().getItemDataSource().getBean();
			if (node.getForeignId() != null)
				return;
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
			logger.info("DuplicatedNodelabelValidator: validating label: " + nodelabel);
	        if (getService().hasDuplicatedNodelabel(nodelabel))
	             throw new InvalidValueException("DuplicatedNodelabelValidator: trovato un duplicato della node label: " + nodelabel);
	       }
	}

	class DnsNodeLabelValidator implements Validator {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1802798646587971819L;
		@Override
		public void validate( Object value) throws InvalidValueException {
			String hostname = ((String)value).toLowerCase();
			String nodelabel = hostname+"."+m_domainComboBox.getValue();
			logger.info("DnsNodeLabelValidator: validating nodelabel: " + nodelabel);
	         if (DashBoardUtils.hasInvalidDnsBind9Label(nodelabel))
	             throw new InvalidValueException("DnsNodeLabelValidator: Dns name: '" + nodelabel + "' is not well formed. The definitive descriptions of the rules for forming domain names appear in RFC 1035, RFC 1123, and RFC 2181." +
               " A domain name consists of one or more parts, technically called labels, that are conventionally concatenated, and delimited by dots, such asexample.com."
                               + " Each label may contain up to 63 characters." +
                               " The full domain name may not exceed a total length of 253 characters in its external dotted-label specification." +
                               " The characters allowed in a label are a subset of the ASCII character set, and includes the characters a through z, A through Z, digits 0 through 9, the hyphen." +
                               " This rule is known as the LDH rule (letters, digits, hyphen). " +
                               " Labels may not start or end with a hyphen.");
		}
	}

	public Table getRequisitionTable() {
		return m_requisitionTable;
	}
	
	public VerticalLayout getLeft() {
		return m_left;
	}
	
	public VerticalLayout getRight() {
		return m_right;
	}

	public TextField getHostNameTextField() {
		return m_hostname;
	}
	
	public TextField getPrimaryTextField() {
		return m_primary;
	}
	
	public ComboBox getDomainComboBox() {
		return m_domainComboBox;
	}

	public TextField getCityTextField() {
		return m_city;
	}

	public TextField getAddressTextField() {
		return m_address;
	}

	public TextField getBuildingTextField() {
		return m_building;
	}

	public ComboBox getSnmpComboBox() {
		return m_snmpComboBox;
	}

	public ComboBox getDescrComboBox() {
		return m_descrComboBox;
	}

	public ComboBox getParentComboBox() {
		return m_parentComboBox;
	}

}
