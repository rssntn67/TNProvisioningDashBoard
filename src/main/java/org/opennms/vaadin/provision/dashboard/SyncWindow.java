package org.opennms.vaadin.provision.dashboard;

import org.opennms.vaadin.provision.core.DashBoardUtils;
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
	private RequisitionTab m_tab;

	public SyncWindow(RequisitionTab tab) {
		m_tab=tab;
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
        BeanItemContainer<SyncOperationNode> container = 
        		DashBoardUtils.
        		getUpdateContainer(
        				m_tab.getUpdates());
        if (container.size() == 0) {
			subContent.addComponent
			(new Label("No pending operation on Requisition"));
			buttons.setVisible(false);
		}
		else {
			Table updatetable = new Table("Nodi da Sincronizzare");
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
	}

	@Override
	public void buttonClick(ClickEvent event) {
		
		if (event.getButton() == m_synctrue)
			m_tab.synctrue();
		else if (event.getButton() == m_syncfalse) 
			m_tab.syncfalse();
		else if (event.getButton() == m_syncdbonly) 
			m_tab.syncdbonly();
		close();
	}

	public RequisitionTab getTab() {
		return m_tab;
	}
}
