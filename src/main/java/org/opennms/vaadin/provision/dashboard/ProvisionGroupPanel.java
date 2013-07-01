package org.opennms.vaadin.provision.dashboard;

import java.util.Set;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
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
import com.vaadin.ui.HorizontalSplitPanel;
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
public class ProvisionGroupPanel extends ProvisionGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	/* User interface components are stored in session. */
	private Table m_requisitionNodeList = new Table();
	private TextField m_searchField = new TextField();
	private Button m_addNewContactButton = new Button("New Requisition Node");
	private Button m_removeContactButton = new Button("Remove this Node");
	private FormLayout m_editorLayout = new FormLayout();
	private FieldGroup m_editorFields = new FieldGroup();
	private ComboBox m_catComboBox = new ComboBox("Select Categories");
		
	private String m_searchText = null;

	IndexedContainer contactContainer;
	
	private boolean loaded=false;


	public ProvisionGroupPanel(String foreignsource, DashBoardService service) {

		super(foreignsource, service);
	}

	public void load() {
		if (loaded)
			return;
		contactContainer = getProvisionNodeList();
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
		
		for (String categories: getCategorylist()) {
			m_catComboBox.addItem(categories);
		}
		m_catComboBox.setInvalidAllowed(false);
		m_catComboBox.setNullSelectionAllowed(true);		
		m_catComboBox.setImmediate(true);
		m_catComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			/**
			 * 
			 */
			private static final long serialVersionUID = -3559078865783782719L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new NodeFilter(m_searchText, m_catComboBox.getValue()));
			}
		});
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(m_editorLayout);
		splitPanel.setSplitPosition(25,Unit.PERCENTAGE);
		
		VerticalLayout topLeftLayout = new VerticalLayout();
		topLeftLayout.addComponent(m_catComboBox);
		topLeftLayout.addComponent(m_searchField);
		
		leftLayout.addComponent(topLeftLayout);

		leftLayout.addComponent(m_requisitionNodeList);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(m_addNewContactButton);
		
		leftLayout.addComponent(bottomLeftLayout);
		
		/* Set the contents in the left of the split panel to use all the space */
		leftLayout.setSizeFull();

		/*
		 * On the left side, expand the size of the contactList so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(m_requisitionNodeList, 1);
		m_requisitionNodeList.setSizeFull();

		topLeftLayout.setWidth("100%");
		m_searchField.setWidth("50%");
		//topLeftLayout.setExpandRatio(m_searchField, 1);

		/* Put a little margin around the fields in the right side editor */
		m_editorLayout.setMargin(true);
		m_editorLayout.setVisible(false);
	}

	private void initEditor() {

		m_editorLayout.addComponent(m_removeContactButton);

		/* User interface can be created dynamically to reflect underlying data. */
		for (String fieldName : DashBoardService.fieldNames) {
			TextField field = new TextField(fieldName);
			m_editorLayout.addComponent(field);
			field.setWidth("100%");

			/*
			 * We use a FieldGroup to connect multiple components to a data
			 * source at once.
			 */
			m_editorFields.bind(field, fieldName);
		}

		/*
		 * Data can be buffered in the user interface. When doing so, commit()
		 * writes the changes to the data source. Here we choose to write the
		 * changes automatically without calling commit().
		 */
		m_editorFields.setBuffered(false);
	}

	private void initSearch() {

		/*
		 * We want to show a subtle prompt in the search field. We could also
		 * set a caption that would be shown above the field or description to
		 * be shown in a tooltip.
		 */
		m_searchField.setInputPrompt("Search nodes");

		/*
		 * Granularity for sending events over the wire can be controlled. By
		 * default simple changes like writing a text in TextField are sent to
		 * server with the next Ajax call. You can set your component to be
		 * immediate to send the changes to server immediately after focus
		 * leaves the field. Here we choose to send the text over the wire as
		 * soon as user stops writing for a moment.
		 */
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);

		/*
		 * When the event happens, we handle it in the anonymous inner class.
		 * You may choose to use separate controllers (in MVC) or presenters (in
		 * MVP) instead. In the end, the preferred application architecture is
		 * up to you.
		 */
		m_searchField.addTextChangeListener(new TextChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event) {

				/* Reset the filter for the contactContainer. */
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new NodeFilter(event
						.getText(),m_catComboBox.getValue()));
			}
		});
	}

	/*
	 * A custom filter for searching node names in the
	 * contactContainer.
	 */
	private class NodeFilter implements Filter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String needle1="";
		private String needle2="";

		public NodeFilter(Object o, Object c) {
			if ( o != null)
				this.needle1 = (String) o;
			if ( c != null)
				this.needle2 = (String) c;
		}

		public boolean passesFilter(Object itemId, Item item) {
			String haystack1 = ("" + item.getItemProperty(DashBoardService.LABEL).getValue());
			String haystack2 = ("" + item.getItemProperty(DashBoardService.CATEGORIES).getValue());
			return (haystack1.contains(needle1) && haystack2.contains(needle2));
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private void initAddRemoveButtons() {
		m_addNewContactButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {

				/*
				 * Rows in the Container data model are called Item. Here we add
				 * a new row in the beginning of the list.
				 */
				contactContainer.removeAllContainerFilters();
				Object contactId = contactContainer.addItemAt(0);

				/*
				 * Each Item has a set of Properties that hold values. Here we
				 * set a couple of those.
				 */
				m_requisitionNodeList.getContainerProperty(contactId, DashBoardService.LABEL).setValue(
						"New");

				m_requisitionNodeList.getContainerProperty(contactId, DashBoardService.CATEGORIES).setValue(
						"Categories");

				/* Lets choose the newly created contact to edit it. */
				m_requisitionNodeList.select(contactId);
			}
		});

		m_removeContactButton.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				Object contactId = m_requisitionNodeList.getValue();
				m_requisitionNodeList.removeItem(contactId);
			}
		});
	}

	private void initContactList() {
		m_requisitionNodeList.setContainerDataSource(contactContainer);
		m_requisitionNodeList.setVisibleColumns(new String[] { DashBoardService.LABEL });
		m_requisitionNodeList.setSelectable(true);
		m_requisitionNodeList.setImmediate(true);

		m_requisitionNodeList.addValueChangeListener(new Property.ValueChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				Object contactId = m_requisitionNodeList.getValue();

				/*
				 * When a contact is selected from the list, we want to show
				 * that in our editor on the right. This is nicely done by the
				 * FieldGroup that binds all the fields to the corresponding
				 * Properties in our contact at once.
				 */
				if (contactId != null)
					m_editorFields.setItemDataSource(m_requisitionNodeList
							.getItem(contactId));
				
				m_editorLayout.setVisible(contactId != null);
			}
		});
	}
	
	protected IndexedContainer getProvisionNodeList() {
		return getService().getProvisionNodeList(getForeignSource());
	}
	
	protected Set<String> getCategorylist() {
		return getService().getCategorieslist();
	}
	
}
