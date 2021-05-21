package org.opennms.vaadin.provision.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsState;
import org.opennms.vaadin.provision.model.BasicNode.OnmsSync;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.MediaGatewayNode;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/*
 * UI class is the starting point for your app. You may deploy it with
 * VaadinServlet or VaadinPortlet by giving your UI class name a parameter.
 * When you browse to your app a web page showing your UI is automatically
 * generated. Or you may choose to embed your UI to an existing web page.
 */
@Title("TNPD - Trentino Network Requisition: Media Gateway")
@Theme("runo")
public class MediaGatewayTab extends RequisitionTab {

    private static final long serialVersionUID = -5948892618258879832L;

    private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());
    private String m_searchText = null;
    private BeanContainer<String, MediaGatewayNode> m_requisitionContainer = new BeanContainer<String, MediaGatewayNode>(MediaGatewayNode.class);
    private BasicNode m_mg;
    private Map<String, BasicNode> m_updates = new HashMap<String, BasicNode>();

    private BeanFieldGroup<MediaGatewayNode> m_editorFields = new BeanFieldGroup<MediaGatewayNode>(MediaGatewayNode.class);
    Integer newHost = 0;

    final TextField m_searchField = new TextField("Type Label Text");

    private Button m_syncSIVNRequisButton = new Button("Sync Virtual Node");
    final ComboBox m_networkCatComboBox = new ComboBox("Network Category");
    final ComboBox m_backupComboBox = new ComboBox("Backup Profile");

    @Override
    public void load() {
        try {
            m_mg = getService().getMediaGateway();
        } catch (UniformInterfaceException e) {
            logger.info("Response Status:" + e.getResponse().getStatus()
                    + " Reason: "
                    + e.getResponse().getStatusInfo().getReasonPhrase());
            if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                logger.info("No Requisition Found: "
                        + e.getLocalizedMessage());
                getService().createRequisition(DashBoardUtils.SIVN_REQU_NAME);
                load();
                return;
            }
        }

        try {
            m_requisitionContainer = getService().getMediaGatewayContainer();
            getRequisitionTable().setContainerDataSource(m_requisitionContainer);
            getRequisitionTable().setVisibleColumns(new Object[] {
                    DashBoardUtils.LABEL, DashBoardUtils.VALID });
        } catch (UniformInterfaceException e) {
            logger.info("Response Status:" + e.getResponse().getStatus()
                    + " Reason: "
                    + e.getResponse().getStatusInfo().getReasonPhrase());
            if (e.getResponse().getStatusInfo().getStatusCode() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                logger.info("No Requisition Found: "
                        + e.getLocalizedMessage());
                getService().createRequisition(DashBoardUtils.TN_REQU_NAME);
                load();
                return;
            }
            logger.warning("Load from rest Failed: "
                    + e.getLocalizedMessage());
            Notification.show("Load Node Requisition",
                              "Load from rest Failed Failed: "
                                      + e.getLocalizedMessage(),
                              Type.WARNING_MESSAGE);
            return;
        }
        reconcilemgvn(true);

        super.load();
        Map<String, BackupProfile> bckupprofilemap = getService().getBackupProfileContainer().getBackupProfileMap();
        List<String> backupprofiles = new ArrayList<String>(bckupprofilemap.keySet());
        Collections.sort(backupprofiles);

        for (String backupprofile : backupprofiles) {
            m_backupComboBox.addItem(backupprofile);
            m_backupComboBox.setItemCaption(backupprofile, backupprofile
                    + ("(username:"
                            + bckupprofilemap.get(backupprofile).getUsername()
                            + ")"));
        }

    }

    public MediaGatewayTab() {
        super();

        m_syncSIVNRequisButton.addClickListener(this);
        m_syncSIVNRequisButton.setImmediate(true);

        m_searchField.setInputPrompt("Search nodes");
        m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
        m_searchField.addTextChangeListener(new TextChangeListener() {
            private static final long serialVersionUID = 1L;

            public void textChange(final TextChangeEvent event) {
                m_searchText = event.getText();
                m_requisitionContainer.removeAllContainerFilters();
                m_requisitionContainer.addContainerFilter(new RequisitionNodeFilter(m_searchText));
            }
        });

        m_networkCatComboBox.addItem(DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY);

        m_networkCatComboBox.setInvalidAllowed(false);
        m_networkCatComboBox.setNullSelectionAllowed(false);
        m_networkCatComboBox.setRequired(true);
        m_networkCatComboBox.setRequiredError("E' necessario selezionare la categoria di rete");
        m_networkCatComboBox.setImmediate(true);

        m_backupComboBox.setInvalidAllowed(false);
        m_backupComboBox.setNullSelectionAllowed(false);
        m_backupComboBox.setRequired(true);
        m_backupComboBox.setRequiredError("E' necessario scegliere una profilo di backup");
        
        m_editorFields.setBuffered(true);
        m_editorFields.bind(getDescrComboBox(), DashBoardUtils.DESCR);
        m_editorFields.bind(getHostNameTextField(), DashBoardUtils.HOST);
        m_editorFields.bind(m_networkCatComboBox,
                            DashBoardUtils.NETWORK_CATEGORY);
        m_editorFields.bind(getDomainComboBox(), DashBoardUtils.CAT);
        m_editorFields.bind(getPrimaryTextField(), DashBoardUtils.PRIMARY);
        m_editorFields.bind(getParentComboBox(), DashBoardUtils.PARENT);
        m_editorFields.bind(getSnmpComboBox(), DashBoardUtils.SNMP_PROFILE);
        m_editorFields.bind(m_backupComboBox, DashBoardUtils.BACKUP_PROFILE);
        m_editorFields.bind(getCityTextField(), DashBoardUtils.CITY);
        m_editorFields.bind(getAddressTextField(), DashBoardUtils.ADDRESS1);
        m_editorFields.bind(getBuildingTextField(), DashBoardUtils.BUILDING);

        getRightHead().addComponent(m_syncSIVNRequisButton);

        VerticalLayout searchlayout = new VerticalLayout();
        m_searchField.setWidth("80%");
        searchlayout.addComponent(m_searchField);
        searchlayout.setWidth("100%");
        searchlayout.setMargin(true);

        HorizontalLayout bottomLeftLayout = new HorizontalLayout();
        bottomLeftLayout.addComponent(new Label("----Select to Edit----"));

        getLeft().addComponent(new Panel("Search", searchlayout));
        getLeft().addComponent(new Panel(getRequisitionTable()));
        getLeft().addComponent(bottomLeftLayout);
        getLeft().setSizeFull();

        FormLayout leftGeneralInfo = new FormLayout(new Label("Informazioni Generali"));
        leftGeneralInfo.setMargin(true);
        leftGeneralInfo.addComponent(getDescrComboBox());
        leftGeneralInfo.addComponent(getHostNameTextField());
        leftGeneralInfo.addComponent(m_networkCatComboBox);
        leftGeneralInfo.addComponent(getDomainComboBox());
        leftGeneralInfo.addComponent(getPrimaryTextField());
        leftGeneralInfo.addComponent(getParentComboBox());
        leftGeneralInfo.addComponent(getCityTextField());
        leftGeneralInfo.addComponent(getAddressTextField());
        leftGeneralInfo.addComponent(getBuildingTextField());

        VerticalLayout centerGeneralInfo = new VerticalLayout();
        centerGeneralInfo.setMargin(true);

        HorizontalLayout bottomRightGeneralInfo = new HorizontalLayout();

        FormLayout rightGeneralInfo = new FormLayout();
        rightGeneralInfo.setMargin(true);
        rightGeneralInfo.addComponent(bottomRightGeneralInfo);

        HorizontalLayout catLayout = new HorizontalLayout();
        catLayout.setSizeFull();

        HorizontalLayout profLayout = new HorizontalLayout();
        profLayout.setSizeFull();
        profLayout.addComponent(getSnmpComboBox());
        profLayout.addComponent(m_backupComboBox);

        HorizontalLayout generalInfo = new HorizontalLayout();
        generalInfo.addComponent(leftGeneralInfo);
        generalInfo.addComponent(centerGeneralInfo);
        generalInfo.addComponent(rightGeneralInfo);
        generalInfo.setExpandRatio(leftGeneralInfo, 3);
        generalInfo.setExpandRatio(centerGeneralInfo, 1);
        generalInfo.setExpandRatio(rightGeneralInfo, 3);

        FormLayout profileInfo = new FormLayout();
        profileInfo.addComponent(new Label("Profili"));
        profileInfo.addComponent(profLayout);
        profileInfo.addComponent(catLayout);

        getRight().addComponent(new Panel(generalInfo));
        getRight().addComponent(new Panel(profileInfo));
        getRight().setVisible(false);

    }

    private void reconcilemgvn(boolean openwindow) {
        Map<BasicInterface, Set<String>> serviceMap = m_mg.getServiceMap();
        Set<BasicService> serviceToDel = new HashSet<BasicService>();
        Set<BasicService> serviceToAdd = new HashSet<BasicService>();
        Set<String> ips = new HashSet<String>();

        for (String nodelabel : m_requisitionContainer.getItemIds()) {
            MediaGatewayNode mgnode = m_requisitionContainer.getItem(nodelabel).getBean();
            if (mgnode.getPrimary() == null)
                continue;
            ips.add(mgnode.getPrimary());
        }

        logger.info("found media gateway ip: " + ips);
        for (BasicInterface bi : serviceMap.keySet()) {
            if (ips.contains(bi.getIp())) {
                for (String service : serviceMap.get(bi)) {
                    if (service.equals("PattonSIPCalls"))
                        continue;
                    BasicService bs = new BasicService(bi);
                    bs.setService(service);
                    serviceToDel.add(bs);
                }
            } else {
                for (String service : serviceMap.get(bi)) {
                    BasicService bs = new BasicService(bi);
                    bs.setService(service);
                    serviceToDel.add(bs);
                }
            }
        }

        for (String ip : ips) {
            BasicInterface bi = new BasicInterface();
            bi.setIp(ip);
            bi.setDescr(DashBoardUtils.DESCR_TNPD);
            bi.setOnmsprimary(OnmsPrimary.N);
            if (serviceMap.containsKey(bi)
                    && serviceMap.get(bi).contains("PattonSIPCalls"))
                continue;
            BasicService bs = new BasicService(bi);
            bs.setService("PattonSIPCalls");
            serviceToAdd.add(bs);
        }
        if (serviceToAdd.isEmpty() && serviceToDel.isEmpty()) {
            getService().reconcilemediagateway(m_mg);
            m_mg.setNoneState();
            return;
        }

        m_mg.setUpdateState();
        m_updates.put(m_mg.getNodeLabel(), m_mg);

        for (BasicService bs : serviceToDel)
            m_mg.delService(bs);
        for (BasicService bs : serviceToAdd)
            m_mg.addService(bs);
        m_mg.setOnmsSyncOperations(OnmsSync.TRUE);
        getService().reconcilemediagateway(m_mg);
        if (openwindow) {
            final Window reconcilemgvnwindow = new Window(getName()
                    + ": MediaGateway reconcile");
            VerticalLayout windowcontent = new VerticalLayout();
            windowcontent.setMargin(true);
            windowcontent.setSpacing(true);
            if (!serviceToDel.isEmpty()) {
                Table servicetoDelTable = new Table("Servizi rimossi da mediagateway");
                servicetoDelTable.setSizeFull();
                BeanItemContainer<BasicService> secondaryIpContainer = new BeanItemContainer<BasicService>(BasicService.class);
                for (BasicService bs : serviceToDel) {
                    secondaryIpContainer.addBean(bs);
                }
                servicetoDelTable.setContainerDataSource(secondaryIpContainer);
                windowcontent.addComponent(servicetoDelTable);
            }
            if (!serviceToAdd.isEmpty()) {
                Table servicetoAddTable = new Table("Servizi aggiunti a mediagateway");
                servicetoAddTable.setSizeFull();
                BeanItemContainer<BasicService> secondaryIpContainer = new BeanItemContainer<BasicService>(BasicService.class);
                for (BasicService bs : serviceToAdd) {
                    secondaryIpContainer.addBean(bs);
                }
                servicetoAddTable.setContainerDataSource(secondaryIpContainer);
                windowcontent.addComponent(servicetoAddTable);
            }

            reconcilemgvnwindow.setContent(windowcontent);
            reconcilemgvnwindow.setModal(false);
            reconcilemgvnwindow.setWidth("400px");
            UI.getCurrent().addWindow(reconcilemgvnwindow);
        }

    }

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == m_syncSIVNRequisButton) {
            sync(DashBoardUtils.SIVN_REQU_NAME);
        } else {
            super.buttonClick(event);
        }
    }

    @Override
    public void replace() {
        super.replace();
        reconcilemgvn(false);
    }

    @Override
    public void delete() {
        super.delete();
        reconcilemgvn(false);
    }

    @Override
    public void save() {
        super.save();
        reconcilemgvn(false);
    }

    @Override
    public Map<String, Collection<BasicNode>> getUpdatesMap() {
        Map<String, Collection<BasicNode>> updatesmap = super.getUpdatesMap();
        if (m_mg != null && m_mg.getOnmstate() != OnmsState.NONE) {
            List<BasicNode> sivnnodes = new ArrayList<BasicNode>();
            sivnnodes.add(m_mg);
            updatesmap.put(DashBoardUtils.SIVN_REQU_NAME, sivnnodes);
        }
        return updatesmap;
    }

    @Override
    public void selectItem(BasicNode node) {
        m_editorFields.setItemDataSource((MediaGatewayNode) node);
    }

    @Override
    public String getName() {
        return "MediaGatewayTab";
    }

    public String getRequisitionName() {
        return DashBoardUtils.TN_REQU_NAME;
    }

    @Override
    public void cleanSearchBox() {
        m_searchText = "";
        m_searchField.setValue(m_searchText);
    }

    @Override
    public MediaGatewayNode addBean() {
        return m_requisitionContainer.addBeanAt(0,
                                                new MediaGatewayNode("notSavedHost"
                                                        + newHost++, DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY,
                                                                     DashBoardUtils.TN_REQU_NAME)).getBean();
    }

    @Override
    public BeanContainer<String, ? extends BasicNode> getRequisitionContainer() {
        return m_requisitionContainer;
    }

    @Override
    public BeanFieldGroup<MediaGatewayNode> getBeanFieldGroup() {
        return m_editorFields;
    }
}
