package org.opennms.vaadin.provision.dashboard;

import java.util.Set;

import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TrentinoNetwork Provision Dashboard")
@Theme("runo")
public class ProvisionGroupPanelNew extends ProvisionGroup {

	private static final long serialVersionUID = -5948892618258879832L;

	private Table m_requisitionTable   	= new Table();
	private TextField m_searchField       = new TextField("Type Label");
	private Button m_addNewNodeButton  = new Button("New");
	private Button m_updateNodeButton  = new Button("Update");
	private Button m_removeNodeButton  = new Button("Delete");
	private FormLayout m_editorLayout     = new FormLayout();
	private FieldGroup m_editorFields     = new FieldGroup();
	private ComboBox m_networkCatComboBox = new ComboBox("Select Network Categories");
	private ComboBox m_notifCatComboBox   = new ComboBox("Select Notification Categories");
	private ComboBox m_threshCatComboBox  = new ComboBox("Select Threshold Categories");
	
	protected static final String[] m_notif_categories = {"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"};
	
	protected static final String[] m_thresh_categories = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};

	protected static final String[][] m_network_categories = {
		{"AccessPoint","Backbone"},
		{"Core","Backbone"},
		{"Fiemme2013","Backbone"},
		{"Ponte5p4","Backbone"},
		{"PontePDH","Backbone"},
		{"SwitchWiNet","Backbone"},
		{"Fiemme2013","Backbone"},
		{"AgLav","Accesso"},
		{"Apss","Accesso"},
		{"Biblio","Accesso"},
		{"CPE","Accesso"},
		{"CUE","Accesso"},
		{"ComuneTN","Accesso"},
		{"Comuni","Accesso"},
		{"ConsPro","Accesso"},
		{"GeoSis","Accesso"},
		{"Info","Accesso"},
		{"Internet","Accesso"},
		{"Internet-Esterni","Accesso"},
		{"LAN","Accesso"},
		{"Medici","Accesso"},
		{"Mitt","Accesso"},
		{"OperaUnitn","Accesso"},
		{"Pat","Accesso"},
		{"PatAcquePub","Accesso"},
		{"PatDighe","Accesso"},
		{"PatVoce","Accesso"},
		{"RSACivicaTN","Accesso"},
		{"RSASpes","Accesso"},
		{"ReperibiliTnet","Accesso"},
		{"Scuole","Accesso"},
		{"ScuoleMaterne","Accesso"},
		{"Telpat-Autonome","Accesso"},
		{"Unitn","Accesso"},
		{"VdsRovereto","Accesso"},
		{"Winwinet","Accesso"}
	};
	
	protected static final String[] m_vrfs = {
		"aglav.tnnet.it",
		"alv01.wl.tnnet.it",
		"alv02.wl.tnnet.it",
		"alv03.wl.tnnet.it",
		"alv04.wl.tnnet.it",
		"alv05.wl.tnnet.it",
		"alv06.wl.tnnet.it",
		"apss.tnnet.it",
		"asw01.wl.tnnet.it",
		"bb.tnnet.it",
		"bb.tnnet.it.bb.tnnet.it",
		"biblio.tnnet.it",
		"cavalese-l3.pat.tnnet.it",
		"comunetn.tnnet.it",
		"comuni.tnnet.it",
		"conspro.tnnet.it",
		"cpe01.biblio.tnnet.it",
		"cpe01.pat.tnnet.it",
		"cpe01.patacquepub.tnnet.it",
		"cpe01.scuole.tnnet.it",
		"cpe01.wl.tnnet.it",
		"cue.tnnet.it",
		"ess01.wl.tnnet.it",
		"ess02.wl.tnnet.it",
		"ess03.wl.tnnet.it",
		"ess04.wl.tnnet.it",
		"ess05.wl.tnnet.it",
		"ess06.wl.tnnet.it",
		"ess07.wl.tnnet.it",
		"ess08.wl.tnnet.it",
		"esterni.tnnet.it",
		"geosis.tnnet.it",
		"hq.tnnet.it",
		"iasma.tnnet.it",
		"info.tnnet.it",
		"internet-esterni.tnnet.it",
		"internet.tnnet.it",
		"medici.tnnet.it",
		"mitt.tnnet.it",
		"mktic.comuni.tnnet.it",
		"mtk01.reperibilitnet.tnnet.it",
		"mtr01.wl.tnnet.it",
		"operaunitn.tnnet.it",
		"pat.tnnet.it",
		"patacquepub.tnnet.it",
		"patdighe.tnnet.it",
		"patvoce.tnnet.it",
		"reperibilitnet.tnnet.it",
		"rsacivicatn.tnnet.it",
		"rsaspes.tnnet.it",
		"scuole.tnnet.it",
		"sw01.bb.tnnet.it",
		"sw02.bb.tnnet.it",
		"telpat-autonome.tnnet.it",
		"uby.wl.tnnet.it",
		"unitn.tnnet.it",
		"vdsrovereto.tnnet.it",
		"winwinet.tnnet.it",
		"wl.tnnet.it"
	};
	
	private String m_searchText = null;
	private BeanContainer<String, RequisitionNode> m_requisitionContainer = new BeanContainer<String, RequisitionNode>(RequisitionNode.class);
	private boolean loaded=false;


	public ProvisionGroupPanelNew(String foreignsource, DashBoardService service) {
		super(foreignsource, service);
	}

	public void load() {
		if (loaded)
			return;
		initLayout();
		initContactList();
		initEditor();
		initSearch();
		initAddRemoveButtons();
		loaded=true;
	}

	private void initLayout() {

		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setCompositionRoot(splitPanel);

		for (String[] categories: m_network_categories) {
			m_networkCatComboBox.addItem(categories);
			m_networkCatComboBox.setItemCaption(categories, categories[1]+" - " + categories[0]);
		}
		m_networkCatComboBox.setInvalidAllowed(false);
		m_networkCatComboBox.setNullSelectionAllowed(true);		
		m_networkCatComboBox.setImmediate(true);
		m_networkCatComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatComboBox.getValue(),m_notifCatComboBox.getValue(),m_threshCatComboBox.getValue()));
			}
		});


		for (String category: m_notif_categories) {
			m_notifCatComboBox.addItem(category);
		}
		m_notifCatComboBox.setInvalidAllowed(false);
		m_notifCatComboBox.setNullSelectionAllowed(true);		
		m_notifCatComboBox.setImmediate(true);
		m_notifCatComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatComboBox.getValue(),m_notifCatComboBox.getValue(),m_threshCatComboBox.getValue()));
			}
		});
		
		for (String category: m_thresh_categories) {
			m_threshCatComboBox.addItem(category);
		}
		m_threshCatComboBox.setInvalidAllowed(false);
		m_threshCatComboBox.setNullSelectionAllowed(true);		
		m_threshCatComboBox.setImmediate(true);
		m_threshCatComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(m_searchText, m_networkCatComboBox.getValue(),m_notifCatComboBox.getValue(),m_threshCatComboBox.getValue()));
			}
		});
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		
		VerticalLayout rightLayout = new VerticalLayout();
		splitPanel.addComponent(rightLayout);
		
		splitPanel.setSplitPosition(25,Unit.PERCENTAGE);
		
		HorizontalLayout topRightLayout = new HorizontalLayout();
		topRightLayout.addComponent(m_addNewNodeButton);
		topRightLayout.addComponent(m_updateNodeButton);
		topRightLayout.addComponent(m_removeNodeButton);
		rightLayout.addComponent(new Panel(topRightLayout));
		rightLayout.addComponent(new Panel(m_editorLayout));
		m_updateNodeButton.setEnabled(false);
		m_removeNodeButton.setEnabled(false);
		
		
		
		VerticalLayout topLeftLayout = new VerticalLayout();
		topLeftLayout.addComponent(m_networkCatComboBox);
		topLeftLayout.addComponent(m_notifCatComboBox);
		topLeftLayout.addComponent(m_threshCatComboBox);
		topLeftLayout.addComponent(m_searchField);
		topLeftLayout.setMargin(true);
		
		Panel searchPanel= new Panel("Search",topLeftLayout);
		leftLayout.addComponent(searchPanel);

		leftLayout.addComponent(m_requisitionTable);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(new Label("----Select a Proivision node to Edit----"));
		leftLayout.addComponent(bottomLeftLayout);
		
		leftLayout.setSizeFull();

		leftLayout.setExpandRatio(m_requisitionTable, 1);
		m_requisitionTable.setSizeFull();

		topLeftLayout.setWidth("100%");
		m_searchField.setWidth("50%");
		
		m_editorLayout.setMargin(true);
		m_editorLayout.setVisible(false);
	}

	private void initEditor() {

		TextField label = new TextField(DashBoardService.FOREIGNID);
		m_editorLayout.addComponent(label);
		label.setWidth("100%");
		
		ComboBox vrfs = new ComboBox("Select Domain");
		for (String domain:m_vrfs)
			vrfs.addItem(domain);

		m_editorLayout.addComponent(vrfs);

		m_editorFields.bind(label, DashBoardService.FOREIGNID);
		m_editorFields.setBuffered(false);
	}

	private void initSearch() {
		m_searchField.setInputPrompt("Search nodes");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		m_searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			public void textChange(final TextChangeEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				m_requisitionContainer.addContainerFilter(new NodeFilter(event
						.getText(), m_networkCatComboBox.getValue(),m_notifCatComboBox.getValue(),m_threshCatComboBox.getValue()));
			}
		});
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
			RequisitionNode node = ((BeanItem<RequisitionNode>)item).getBean();			
			return (
					    node.getNodeLabel().contains(needle) 
					&& ( needle1 == null || (node.getCategory(needle1[0]) != null && node.getCategory(needle1[1]) != null )) 
					&& ( needle2 == null || node.getCategory(needle2) != null)
		            && ( needle3 == null || node.getCategory(needle3) != null) 
					);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private void initAddRemoveButtons() {
		m_addNewNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				Notification.show("Add", "Add not yet supported", Type.WARNING_MESSAGE);
				m_editorLayout.setVisible(false);
			}
		});

		m_updateNodeButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				Notification.show("Update", "Update not yet supported", Type.WARNING_MESSAGE);
				m_editorLayout.setVisible(false);
				m_updateNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
			}
		});

		m_removeNodeButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				m_requisitionContainer.removeAllContainerFilters();
				Notification.show("Delete", "Delete not yet supported", Type.WARNING_MESSAGE);
				m_editorLayout.setVisible(false);
				m_updateNodeButton.setEnabled(false);
				m_removeNodeButton.setEnabled(false);
			}
		});
	}

	private void initContactList() {
		m_requisitionContainer = getProvisionNodeList();
		m_requisitionTable.setContainerDataSource(m_requisitionContainer);
		m_requisitionTable.setVisibleColumns(new String[] { DashBoardService.LABEL });
		m_requisitionTable.setSelectable(true);
		m_requisitionTable.setImmediate(true);

		m_requisitionTable.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				Object contactId = m_requisitionTable.getValue();

				if (contactId != null)
					m_editorFields.setItemDataSource(m_requisitionTable
							.getItem(contactId));
				m_editorLayout.setVisible(contactId != null);
				m_updateNodeButton.setEnabled(contactId != null);
				m_removeNodeButton.setEnabled(contactId != null);

			}
		});
	}
	
	protected BeanContainer<String, RequisitionNode> getProvisionNodeList() {
		return getService().getContainerRequisitionNodes(getForeignSource());
	}
	
	protected Set<String> getCategorylist() {
		return getService().getCategorieslist();
	}
	
}
