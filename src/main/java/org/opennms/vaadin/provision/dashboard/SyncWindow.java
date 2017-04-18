package org.opennms.vaadin.provision.dashboard;

import java.util.Collection;
import java.util.Map;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.SyncOperationNode;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

@Title("TNPD - Sync")
@Theme("runo")
public class SyncWindow extends Window implements ClickListener{
	
	private static final long serialVersionUID = -5567143641324108682L;

	private Button m_synctrue = new Button("Sync True");
	private Button m_syncfalse = new Button("Sync False");
	private Button m_syncdbonly = new Button("Sync DbOnly");
	private String m_requisition;
	public SyncWindow(String requisition) {
		m_requisition=requisition;
		m_synctrue.addClickListener(this);
		m_syncfalse.addClickListener(this);
		m_syncdbonly.addClickListener(this);
		
		m_synctrue.setImmediate(true);
		m_syncfalse.setImmediate(true);
		m_syncfalse.setImmediate(true);
		m_synctrue.setEnabled(false);
		m_syncfalse.setEnabled(false);
		m_syncdbonly.setEnabled(false);			
		
		VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subContent.setSpacing(true);
        setContent(subContent);
        setModal(true);
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setMargin(true);
        buttons.setSpacing(true);
        buttons.addComponent(m_synctrue);
        buttons.addComponent(m_syncfalse);
        buttons.addComponent(m_syncdbonly);
        subContent.addComponent(buttons);        
        subContent.addComponent(new HorizontalLayout());
        boolean noupdates = true;
        Map<String,Map<String,Collection<BasicNode>>> updatemap = 
        		((DashboardUI)getParent()).
        		getUpdatesMapforTabs();
        for (String tabname: updatemap.keySet()) {
        	Map<String,Collection<BasicNode>> updatesontab= updatemap.get(tabname);
        	if (updatemap.isEmpty())
        		continue;
        	if (!updatesontab.containsKey(requisition))
        		continue;
        	noupdates = true;
        	BeanItemContainer<SyncOperationNode> container = 
            		DashBoardUtils.
            		getUpdateContainer(updatesontab.get(requisition));
			Table updatetable = new Table("Nodi da Sincronizzare (modificati da: " + tabname +")");
			updatetable.setSelectable(false);
			updatetable.setContainerDataSource(container);
			updatetable.setSizeFull();
			updatetable.setPageLength(3);
			subContent.addComponent(updatetable);
			for (SyncOperationNode upn: container.getItemIds()) {
				if (upn.isSYNCDBONLY())
					m_syncdbonly.setEnabled(true);
				if (upn.isSYNCTRUE())
					m_synctrue.setEnabled(true);
				if (upn.isSYNCFALSE())
					m_syncfalse.setEnabled(true);
			}
		}
        if (noupdates) {
        	subContent.addComponent
        	(new Label("No pending operation on Requisition"));
        	buttons.setVisible(false);
        }

	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_synctrue)
    		((DashboardUI)getParent()).
    		synctrue(m_requisition);
		else if (event.getButton() == m_syncfalse) 
    		((DashboardUI)getParent()).
    		syncfalse(m_requisition);
		else if (event.getButton() == m_syncdbonly) 
    		((DashboardUI)getParent()).
    		syncdbonly(m_requisition);
		close();
	}

}
