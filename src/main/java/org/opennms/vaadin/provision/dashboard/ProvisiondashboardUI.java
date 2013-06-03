package org.opennms.vaadin.provision.dashboard;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;

import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("TrentinoNetwork Provision Dashboard")
public class ProvisiondashboardUI extends UI {

	/* User interface components are stored in session. */
	private Table m_contactList = new Table();
	private TextField m_searchField = new TextField();
	private Button m_addNewContactButton = new Button("New Requisition Node");
	private Button m_removeContactButton = new Button("Remove this Node");
	private FormLayout m_editorLayout = new FormLayout();
	private FieldGroup m_editorFields = new FieldGroup();
	private ComboBox m_primaryComboBox = new ComboBox("Select Categories");
		
	private static final String LABEL = "label";
	private static final String FOREIGNSOURCE = "foreign source";
	private static final String CATEGORIES = "categories";
	private static final String[] fieldNames = new String[] { LABEL, FOREIGNSOURCE, CATEGORIES};
	private static final Collection<String> categorieslist = new HashSet();

	private static JerseyNodesService m_nodeService;
	/*
	 * Any component can be bound to an external data source. This example uses
	 * just a dummy in-memory list, but there are many more practical
	 * implementations.
	 */
	IndexedContainer contactContainer = getNodeList();

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	protected void init(VaadinRequest request) {
		initLayout();
		initContactList();
		initEditor();
		initSearch();
		initAddRemoveButtons();
	}

	/*
	 * In this example layouts are programmed in Java. You may choose use a
	 * visual editor, CSS or HTML templates for layout instead.
	 */
	private void initLayout() {

		/* Root of the user interface component tree is set */
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setContent(splitPanel);

		/* Build the component tree */
		for (String categories: categorieslist) {
			m_primaryComboBox.addItem(categories);
		}
		m_primaryComboBox.setInvalidAllowed(false);
		m_primaryComboBox.setNullSelectionAllowed(false);		
		m_primaryComboBox.setImmediate(true);
		m_primaryComboBox.addValueChangeListener(new Property.ValueChangeListener() {
		
			@Override
			public void valueChange(ValueChangeEvent event) {
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new CategoriesFilter(m_primaryComboBox.getValue()));
			}
		});
		
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(m_editorLayout);
		leftLayout.addComponent(m_primaryComboBox);

		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		leftLayout.addComponent(bottomLeftLayout);
		bottomLeftLayout.addComponent(m_searchField);
		bottomLeftLayout.addComponent(m_addNewContactButton);
		
		leftLayout.addComponent(m_contactList);

		/* Set the contents in the left of the split panel to use all the space */
		leftLayout.setSizeFull();

		/*
		 * On the left side, expand the size of the contactList so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(m_contactList, 1);
		m_contactList.setSizeFull();

		/*
		 * In the bottomLeftLayout, searchField takes all the width there is
		 * after adding addNewContactButton. The height of the layout is defined
		 * by the tallest component.
		 */
		bottomLeftLayout.setWidth("100%");
		m_searchField.setWidth("100%");
		bottomLeftLayout.setExpandRatio(m_searchField, 1);

		/* Put a little margin around the fields in the right side editor */
		m_editorLayout.setMargin(true);
		m_editorLayout.setVisible(false);
	}

	private void initEditor() {

		m_editorLayout.addComponent(m_removeContactButton);

		/* User interface can be created dynamically to reflect underlying data. */
		for (String fieldName : fieldNames) {
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
			public void textChange(final TextChangeEvent event) {

				/* Reset the filter for the contactContainer. */
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new NodeFilter(event
						.getText()));
			}
		});
	}

	/*
	 * A custom filter for searching node names in the
	 * contactContainer.
	 */
	private class NodeFilter implements Filter {
		private String needle;

		public NodeFilter(Object o) {
			this.needle = (String) o;
		}

		public boolean passesFilter(Object itemId, Item item) {
			String haystack = ("" + item.getItemProperty(LABEL).getValue());
			return haystack.contains(needle);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	/*
	 * A custom filter for searching categories in the
	 * contactContainer.
	 */
	private class CategoriesFilter implements Filter {
		private String needle;

		public CategoriesFilter(Object o) {
			this.needle = (String)o;
		}

		public boolean passesFilter(Object itemId, Item item) {
			String haystack = ("" + item.getItemProperty(CATEGORIES).getValue());
			return haystack.contains(needle);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private void initAddRemoveButtons() {
		m_addNewContactButton.addClickListener(new ClickListener() {
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
				m_contactList.getContainerProperty(contactId, LABEL).setValue(
						"New");
				m_contactList.getContainerProperty(contactId, FOREIGNSOURCE).setValue(
						"Contact");

				m_contactList.getContainerProperty(contactId, CATEGORIES).setValue(
						"Categories");

				/* Lets choose the newly created contact to edit it. */
				m_contactList.select(contactId);
			}
		});

		m_removeContactButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				Object contactId = m_contactList.getValue();
				m_contactList.removeItem(contactId);
			}
		});
	}

	private void initContactList() {
		m_contactList.setContainerDataSource(contactContainer);
		m_contactList.setVisibleColumns(new String[] { LABEL, FOREIGNSOURCE,CATEGORIES });
		m_contactList.setSelectable(true);
		m_contactList.setImmediate(true);

		m_contactList.addValueChangeListener(new Property.ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				Object contactId = m_contactList.getValue();

				/*
				 * When a contact is selected from the list, we want to show
				 * that in our editor on the right. This is nicely done by the
				 * FieldGroup that binds all the fields to the corresponding
				 * Properties in our contact at once.
				 */
				if (contactId != null)
					m_editorFields.setItemDataSource(m_contactList
							.getItem(contactId));
				
				m_editorLayout.setVisible(contactId != null);
			}
		});
	}

	/*
	 * Generate some in-memory example data to play with. In a real application
	 * we could be using SQLContainer, JPAContainer or some other to persist the
	 * data.
	 */
	private static IndexedContainer getNodeList() {
	    m_nodeService = new JerseyNodesService();
	    JerseyClientImpl jerseyClient = new JerseyClientImpl(
	                                                         "http://devopennms.noc.tnnet.it:8980/opennms/rest/","admin","admin2001");
	    m_nodeService.setJerseyClient(jerseyClient);
	 	IndexedContainer ic = new IndexedContainer();

		for (String p : fieldNames) {
			ic.addContainerProperty(p, String.class, "");
		}

		int i=0;
		for (OnmsNode node : m_nodeService.getAll() ) {
			Object id = ic.addItem();
			ic.getContainerProperty(id, LABEL).setValue(node.getLabel());
			ic.getContainerProperty(id, FOREIGNSOURCE).setValue(node.getForeignSource());
			String categories = "";
			for (OnmsCategory category: node.getCategories()) {
				categories += category.getName()+"-";
			}
			ic.getContainerProperty(id, CATEGORIES).setValue(categories);
			categorieslist.add(categories);
		}

		return ic;
	}

}
