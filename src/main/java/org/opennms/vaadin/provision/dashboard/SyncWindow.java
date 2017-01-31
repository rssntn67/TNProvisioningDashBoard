package org.opennms.vaadin.provision.dashboard;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

public class SyncWindow extends Window implements ClickListener{
	
	private static final long serialVersionUID = -5567143641324108682L;

	private Button m_synctrue = new Button("Sync True");
	private Button m_syncfalse = new Button("Sync False");
	private Button m_syncdbonly = new Button("Sync DbOnly");
	private RequisitionTab m_tab;

	public SyncWindow(RequisitionTab tab) {
		m_tab=tab;
		VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        setContent(subContent);
        setModal(true);
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(m_synctrue);
        buttons.addComponent(m_syncfalse);
        buttons.addComponent(m_syncdbonly);
        subContent.addComponent(new Panel(buttons));        
		Table updatetable = new Table();
		updatetable.setSelectable(false);
		updatetable.setContainerDataSource(m_tab.getUpdates());
		updatetable.setVisible(true);
		subContent.addComponent(new Panel(updatetable));
	}

	@Override
	public void buttonClick(ClickEvent event) {
		
		if (event.getButton() == m_synctrue) 
			m_tab.synctrue();
		else if (event.getButton() == m_syncfalse) 
			m_tab.syncfalse();
		else if (event.getButton() == m_syncdbonly) 
			m_tab.syncdbonly();
	}

	public RequisitionTab getTab() {
		return m_tab;
	}
}
