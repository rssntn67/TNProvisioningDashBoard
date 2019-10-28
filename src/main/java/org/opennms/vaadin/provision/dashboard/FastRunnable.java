package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastOrder;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsState;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.Categoria;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.Job.JobStatus;
import org.opennms.vaadin.provision.model.JobLogEntry;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;

public abstract class FastRunnable implements Runnable {

    private final static String FAST_NULL_ORDER_CODE ="FAST(error): Device Null Order Code";
    private final static String FAST_ASSET_ORPHAN_ORDER_CODE ="FAST(error): Asset Orphan Order Code";
    private final static String FAST_NULL_IP ="FAST(error): Device Null Ip Address";
    private final static String FAST_DUPLICATED_IP ="FAST(error): Device Duplicated Ip Address";
    private final static String FAST_NULL_HOSTNAME ="FAST(error): Device Null Hostname";
    private final static String FAST_INVALID_IP ="FAST(error): Device Invalid Ip Address";
    private final static String FAST_INVALID_HOSTNAME ="FAST(error): Device Invalid Hostname";
    private final static String FAST_NULL_VRF ="FAST(error): Device Null Vrf";
    private final static String FAST_INVALID_VRF ="FAST(error): Device Invalid Vrf";
    private final static String FAST_NULL_SNMP_PROFILE ="FAST(error): Device Null Snmp Profile";
    private final static String FAST_INVALID_SNMP_PROFILE ="FAST(error): Device Invalid Snmp Profile";
    private final static String FAST_NULL_BACKUP_PROFILE ="FAST(error): Device Null Backup Profile";
    private final static String FAST_INVALID_BACKUP_PROFILE ="FAST(error): Device Invalid Backup Profile";
    private final static String ONMS_DUPLICATED_ID ="ONMS(error): Duplicated Foreign Id";
    private final static String ONMS_NULL_IP ="ONMS(error): Null Ip Address";
    private final static String ONMS_INVALID_IP ="ONMS(error): Invalid Ip Address";
    private final static String FAST_DUPLICATED_ONMS_ID = "FAST(warning): mapped to duplicated foreign id in requisition";
    private final static String FAST_DUPLICATED_ONMS_IP = "FAST(warning): mapped to duplicated ip address in requisition";
    private final static String FAST_NO_REF_DEVICE = "FAST(error): no valid ref device"; 
    private final static String FAST_MISHMATCH_ONMS = "FAST(error): mismatch hostname/foreignIds";
    private final static String ONMS_ADDED_DEVICE = "ONMS(info): added device";
    private final static String ONMS_DELETED_DEVICE = "ONMS(info): deleted device";
    private final static String ONMS_UPDATED_NO_FAST_DEVICE = "ONMS(info): updated device not managed by FAST  ";
    private final static String ONMS_UPDATED_FAST_DEVICE = "ONMS(info): updated device managed by FAST  ";
    private final static String ONMS_DELETED_IP = "ONMS(info): deleted interface";
    private final static String ONMS_ADDED_IP = "ONMS(info): added interface";
    private final static String ONMS_UPDATE_SNMP_PROFILE = "ONMS(info): updated Snmp Profile";
    
    private final static String JOB_FAILS_COMMIT="FAST run: Failed Commit Job to Table";
    private final static String JOB_FAILS_INTERRUPTED="FAST run: Interrupted";
    private final static String JOB_FAILS_SYNC="FAST run: Requisition Updated - Sync Failed";
    private final static String JOB_FAILS_REQU="FAST run: Failed Requisition Update";
    private final static String JOB_FAILS_REQU_GET="FAST run: Failed Requisition Download";
    private final static String JOB_FAILS_FAST_API="FAST run: Failed Load Fast Assets";
    
    List<BasicNode> m_updates = new ArrayList<BasicNode>();

    private DashBoardSessionService m_session;
    private static final Logger logger = Logger.getLogger(FastRunnable.class.getName());

    private Job m_job;
    private BeanItemContainer<JobLogEntry> m_logcontainer;
    boolean m_syncRequisition = false;

    Map<String, Categoria> m_vrf;

    Map<String, BackupProfile> m_backup;

    Map<String, SnmpProfile> m_snmp;


    public synchronized Job getJob() {
        return m_job;
    }

    public synchronized void setJobId(RowId jobid) {
        if (m_job != null) {
            m_job.setJobid(Integer.parseInt(jobid.toString()));
        }
    }

    private void fails(String description, Exception e) {
        m_job.setJobstatus(JobStatus.FAILED);
        m_job.setJobdescr(description+": " + e.getMessage());
        logger.log(Level.SEVERE, description, e);
    }

    public void syncRequisition() {
        m_syncRequisition = true;
    }

    public Collection<BasicNode> getUpdates() {
        return m_updates;
    }

    public void resetUpdateMap() {
        List<BasicNode> updates = new ArrayList<BasicNode>();
        for (BasicNode node : m_updates) {
            if (node.getOnmstate() == OnmsState.NONE)
                continue;
            updates.add(node);
        }
        m_updates = updates;
    }

    public FastRunnable(DashBoardSessionService session) {
        m_session = session;
        m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);
    }

    public DashBoardSessionService getService() {
        return m_session;
    }

    public BeanItemContainer<JobLogEntry> getJobLogContainer() {
        return m_logcontainer;
    }

    public abstract void updateProgress(Float progress);

    public abstract void log(JobLogEntry log);

    public abstract void beforeJob();

    public abstract void afterJob();

    public void persist(JobLogEntry jLogE) {
        jLogE.setJobid(m_job.getJobid());
        m_logcontainer.addBean(jLogE);
    }


    public synchronized boolean success() {
        return m_job.getJobstatus() == JobStatus.SUCCESS;
    }

    public synchronized boolean running() {
        return m_job.getJobstatus() == JobStatus.RUNNING;
    }

    public synchronized boolean failed() {
        return m_job.getJobstatus() == JobStatus.FAILED;
    }

    @SuppressWarnings("deprecation")
    public void startJob() {
        getService().getJobContainer().add(m_job);

        getService().getJobContainer().addListener(new QueryDelegate.RowIdChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void rowIdChange(RowIdChangeEvent event) {
                logger.info("new job id: " + event.getNewRowId());
                setJobId(event.getNewRowId());
            }
        });

        try {
            logger.info("run: adding job in jobs table");
            getService().getJobContainer().commit();
            logger.info("run: added job in jobs table");
        } catch (SQLException e) {
            fails(JOB_FAILS_COMMIT, e);
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fails(JOB_FAILS_INTERRUPTED, e);
        }

    }

    public void endJob() {
        RowId jobId = new RowId(new Object[] { m_job.getJobid() });
        getService().getJobContainer().save(jobId, m_job);

        try {
            getService().getJobContainer().commit();
            logger.info("run: commit job in jobs table");
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Cannot commit job in job table", e);
            return;
        }

        for (JobLogEntry joblog : m_logcontainer.getItemIds()) {
            getService().getJobLogContainer().add(joblog);
        }

        try {
            getService().getJobLogContainer().commit();
            logger.info("run: commit logs in joblog table");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Exception commit logs ", e);
            return;
        }

        try {
            getService().getIpSnmpProfileContainer().commit();
            logger.info("run: commit profiles in ipsnmpprofile table");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Exception commit ipsnmpprofile", e);
        }

    }

    @Override
    public void run() {

        m_job = new Job();
        m_job.setUsername(getService().getUser());
        m_job.setJobdescr("FAST sync: started");
        m_job.setJobstatus(JobStatus.RUNNING);
        m_job.setJobstart(new Date());

        beforeJob();
        
        if (running()) {
            sync();
        }

        m_job.setJobend(new Date());
        
        afterJob();
        
    }
    
    private boolean checkManagedByFastAsset(FastAsset asset, FastOrder order) {
        boolean valid = true;
        if (asset.getAttributes().getVrf() == null) {
            log(asset,order,FAST_NULL_VRF);
            valid = false;
        } else if (!m_vrf.containsKey(asset.getAttributes().getVrf())) {
            log(asset,order,FAST_INVALID_VRF+" :" + asset.getAttributes().getVrf());
            valid = false;            
        }

        if (asset.getAttributes().getProfiloSNMP() == null) {
            log(asset,order,FAST_NULL_SNMP_PROFILE);
            valid = false;
        } else if (!m_snmp.containsKey(asset.getAttributes().getProfiloSNMP())) {
            log(asset,order,FAST_INVALID_SNMP_PROFILE+ " :" +asset.getAttributes().getProfiloSNMP());
            valid = false;
        } 
        if (asset.getAttributes().getProfiloBackup() == null) {
            log(asset,order,FAST_NULL_BACKUP_PROFILE);
            valid = false;
        } else if(!m_backup.containsKey(asset.getAttributes().getProfiloBackup())) {
            log(asset,order,FAST_INVALID_BACKUP_PROFILE+ " :" +asset.getAttributes().getProfiloBackup());
            valid = false;
        }
        return valid;
        
    }
            
    private boolean isValidAsset(FastAsset asset, FastOrder order) {
        boolean valid = true;
        if (asset.getAttributes().getIndirizzoIP() == null) {
            log(asset,order,FAST_NULL_IP);
            valid = false;
        }  else if (DashBoardUtils.hasInvalidIp(asset.getAttributes().getIndirizzoIP())) {
            log(asset,order,FAST_INVALID_IP);
            valid = false;
        } 
        if (asset.getAttributes().getHostName() == null) {
            log(asset,order,FAST_NULL_HOSTNAME);                
            valid = false;
        } else if (DashBoardUtils.hasInvalidDnsBind9Label(asset.getAttributes().getHostName())) {
            log(asset,order,FAST_INVALID_HOSTNAME);
            valid = false;
        } 

        return valid;
    }

    private void sync() {
        
        logger.info("run: loading table vrf");
        m_vrf = getService().getCatContainer().getCatMap();
        logger.info("run: loaded table vrf");

        logger.info("run: loading table backupprofile");
        m_backup = getService().getBackupProfileContainer().getBackupProfileMap();
        logger.info("run: loaded table backupprofile");

        logger.info("run: loading table snmpprofile");
        m_snmp = getService().getSnmpProfileContainer().getSnmpProfileMap();
        logger.info("run: loaded table snmpprofile");



        BeanContainer<String, TrentinoNetworkNode> requisition;
        try {
            logger.info("run: loading requisition: "
                    + DashBoardUtils.TN_REQU_NAME);
            requisition = getService().getTNContainer();
            logger.info("run: loaded requisition: "
                    + DashBoardUtils.TN_REQU_NAME);
        } catch (final UniformInterfaceException e) {
            fails(JOB_FAILS_REQU_GET, e);
            return;
        } catch (final Exception e) {
            fails(JOB_FAILS_REQU_GET, e);
            return;
        }
        
        Map<Long,FastOrder> fastOrderMap = new HashMap<>();
        List<FastAsset> assets = new ArrayList<>();
        
        try {

            logger.info("run: loading fast Order");
            for (FastOrder order: getService().getFastApiDao().getOrders()) {
                fastOrderMap.put(order.getOrder_id(), order);
            }
            logger.info("run: loaded fast Order");

            logger.info("run: loading fast Asset: " + Meta.Router);
            assets.addAll(getService().getFastApiDao().getAssetsByMeta(Meta.Router));
            logger.info("run: loaded requisition: " + Meta.Router);

            logger.info("run: loading fast Asset: " + Meta.Switch);
            assets.addAll(getService().getFastApiDao().getAssetsByMeta(Meta.Switch));
            logger.info("run: loaded requisition: " + Meta.Switch);
                        
            logger.info("run: loading fast Asset: " + Meta.Radio);
            assets.addAll(getService().getFastApiDao().getAssetsByMeta(Meta.Radio));
            logger.info("run: loaded requisition: " + Meta.Radio);
                                    
            logger.info("run: loading fast Asset: " + Meta.Modem);
            assets.addAll(getService().getFastApiDao().getAssetsByMeta(Meta.Modem));
            logger.info("run: loaded requisition: " + Meta.Modem);
            
        } catch (final UniformInterfaceException e) {
            fails(JOB_FAILS_FAST_API, e);
            return;
        } catch (final Exception e) {
            fails(JOB_FAILS_FAST_API, e);
            return;
        }

        Map<String, List<FastAsset>> fastHostnameServiceDeviceMap = new HashMap<>();
        Map<String, Set<String>> fastIpHostnameMap = new HashMap<String, Set<String>>();
        for (FastAsset device : assets) {
            if (!device.getAttributes().monitorato()) {
                continue;
            }
            if (device.getOrder_id() == null) {
                log(device,null,FAST_NULL_ORDER_CODE);
                continue;
            } else  if (!fastOrderMap.containsKey(device.getOrder_id())) {
                log(device,null,FAST_ASSET_ORPHAN_ORDER_CODE);
                continue;
            }
            FastOrder order = fastOrderMap.get(device.getOrder_id()); 
            if (!order.produzione()) {
                continue;
            }

            if (!isValidAsset(device,order)) {
                continue;
            }
 
            if (!fastHostnameServiceDeviceMap.containsKey(device.getAttributes().getHostName())) {
                fastHostnameServiceDeviceMap.put(device.getAttributes().getHostName(),
                                                 new ArrayList<FastAsset>());
            }
            fastHostnameServiceDeviceMap.get(device.getAttributes().getHostName()).add(device);

            if (!fastIpHostnameMap.containsKey(device.getAttributes().getIndirizzoIP())) {
                fastIpHostnameMap.put(device.getAttributes().getIndirizzoIP(),
                                        new HashSet<String>());
            }
            fastIpHostnameMap.get(device.getAttributes().getIndirizzoIP()).add(device.getAttributes().getHostName());
        }
        for (String ipaddr : fastIpHostnameMap.keySet()) {
            if (fastIpHostnameMap.get(ipaddr).size() == 1)
                continue;
            for (String hostname : fastIpHostnameMap.get(ipaddr)) {
                List<FastAsset> survived = new ArrayList<FastAsset>();
                for (FastAsset device : fastHostnameServiceDeviceMap.remove(hostname)) {
                    if (device.getAttributes().getIndirizzoIP().equals(ipaddr)) {
                        FastOrder order = fastOrderMap.get(device.getOrder_id()); 
                        log(device,order,FAST_DUPLICATED_IP);
                    } else {
                        survived.add(device);
                    }
                }
                if (!survived.isEmpty())
                    fastHostnameServiceDeviceMap.put(hostname, survived);
            }
        }

        Map<String, TrentinoNetworkNode> onmsForeignIdRequisitionNodeMap = new HashMap<>();
        Set<String> onmsDuplicatedForeignId = new HashSet<String>();
        Set<String> onmsDuplicatedIpAddress = new HashSet<String>();
        for (String id : requisition.getItemIds()) {
            TrentinoNetworkNode rnode = requisition.getItem(id).getBean();
            if (onmsForeignIdRequisitionNodeMap.containsKey(rnode.getForeignId())) {
                onmsDuplicatedForeignId.add(rnode.getForeignId());
                log(rnode,ONMS_DUPLICATED_ID);
            } else {
                onmsForeignIdRequisitionNodeMap.put(rnode.getForeignId(),
                                                    rnode);
            }
        }

        for (String dupforeignid : onmsDuplicatedForeignId) {
            TrentinoNetworkNode rnode = onmsForeignIdRequisitionNodeMap.remove(dupforeignid);
            log(rnode,ONMS_DUPLICATED_ID);
        }

        Map<String, Set<String>> onmsIpForeignIdMap = new HashMap<String, Set<String>>();
        for (String id : requisition.getItemIds()) {
            TrentinoNetworkNode rnode = requisition.getItem(id).getBean();
            for (BasicInterface riface : rnode.getServiceMap().keySet()) {
                if (riface.getIp() == null) {
                    log(rnode,riface,ONMS_NULL_IP);
                    continue;
                } else if (DashBoardUtils.hasInvalidIp(riface.getIp())) {
                    log(rnode,riface,ONMS_INVALID_IP);
                    continue;
                }
                if (!onmsIpForeignIdMap.containsKey(riface.getIp())) {
                    onmsIpForeignIdMap.put(riface.getIp(),
                                           new HashSet<String>());
                    onmsIpForeignIdMap.get(riface.getIp()).add(rnode.getForeignId());
                }
            }
        }

        for (String ipaddr : onmsIpForeignIdMap.keySet()) {
            if (onmsIpForeignIdMap.get(ipaddr).size() == 1)
                continue;
            onmsDuplicatedIpAddress.add(ipaddr);
            for (String foreignid : onmsIpForeignIdMap.get(ipaddr)) {
                TrentinoNetworkNode duplicatedipnode = onmsForeignIdRequisitionNodeMap.remove(foreignid);
                if (duplicatedipnode == null)
                    continue;
                BasicInterface duplicatedinterface = duplicatedipnode.getInterface(ipaddr);
                if (duplicatedinterface == null)
                    continue;
                log(duplicatedipnode,duplicatedinterface,ONMS_DUPLICATED_ID);
            }
        }
                
        double current = 0.0;
        updateProgress(new Float(current));

        int i = 0;
        int step = (fastHostnameServiceDeviceMap.size()
                + onmsForeignIdRequisitionNodeMap.size()) / 100;
        logger.info("run: step: " + step);
        logger.info("run: size: " + fastHostnameServiceDeviceMap.size());
        int barrier = step;

        try {

            for (String hostname : fastHostnameServiceDeviceMap.keySet()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    fails(JOB_FAILS_INTERRUPTED, e);
                    return;
                }

                i++;
                if (i >= barrier) {
                    if (current < 0.99)
                        current += 0.01;
                    updateProgress(new Float(current));
                    barrier += step;
                }

                if (onmsDuplicatedForeignId.contains(hostname)) {
                    for (FastAsset asset : fastHostnameServiceDeviceMap.get(hostname)) {
                        FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
                        log(asset,order,FAST_DUPLICATED_ONMS_ID);
                    }
                    continue;
                }
                boolean duplicated=false;
                for (FastAsset asset : fastHostnameServiceDeviceMap.get(hostname)) {
                    if (onmsDuplicatedIpAddress.contains(asset.getAttributes().getIndirizzoIP())) {
                        FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
                        log(asset,order,FAST_DUPLICATED_ONMS_IP);
                        duplicated = true;
                    }
                }
                if (duplicated) {
                    continue;
                }

                Set<String> foreignIds = new HashSet<String>();
                TrentinoNetworkNode rnode= null;
                if (onmsForeignIdRequisitionNodeMap.containsKey(hostname)) {
                    logger.info("found foreignid=hostname for: " + hostname);
                    foreignIds.add(hostname);
                    rnode=onmsForeignIdRequisitionNodeMap.get(hostname);
                } 
                
                for (TrentinoNetworkNode tnnode : onmsForeignIdRequisitionNodeMap.values()) {
                    if (tnnode.getNodeLabel().startsWith(hostname)) {
                        foreignIds.add(tnnode.getForeignId());
                        rnode=tnnode;
                        logger.info("found foreignid="+ tnnode.getForeignId()+" for: " + hostname);
                    }
                }

                if (foreignIds.size() > 1) {
                    fastHostnameServiceDeviceMap
                    .get(hostname)
                    .forEach(asset -> {
                        FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
                        log(asset,order,FAST_MISHMATCH_ONMS);   
                    });
                    continue;                    
                }

                if (foreignIds.size() == 1 && !isManagedByFast(rnode)) {
                    updateNonFast(rnode, fastOrderMap,
                                  fastHostnameServiceDeviceMap.get(hostname));
                    continue;
                }

                FastAsset refdevice = null; 
                for (FastAsset device: fastHostnameServiceDeviceMap.get(hostname)) {
                    if (refdevice == null) {
                        refdevice = device;
                        continue;
                    }
                    if (refdevice.getAttributes().getProfiloSNMP() == null && device.getAttributes().getProfiloSNMP() != null) {
                        refdevice = device;
                        continue;
                    }
                    if (refdevice.getAttributes().getProfiloBackup() == null && device.getAttributes().getProfiloBackup() != null) {
                        refdevice = device;
                        continue;
                    }
                    if (
                            refdevice.getAttributes().getBackup() == null 
                         && device.getAttributes().getBackup() != null 
                            ) {
                        refdevice = device;
                        continue;
                    }
                    if (
                               refdevice.getAttributes().getBackup() != null 
                            && refdevice.getAttributes().getBackup() == 0 
                            && device.getAttributes().getBackup() != null 
                            && device.getAttributes().getBackup() == 1 ) {
                        refdevice = device;
                        continue;
                    }

                }
                FastOrder order = fastOrderMap.get(refdevice.getOrder_id());
                if (!checkManagedByFastAsset(refdevice,order)) {
                    log(refdevice,order,FAST_NO_REF_DEVICE);
                    continue;
                }
                
                Categoria vrf = m_vrf.get(refdevice.getAttributes().getVrf());
                
                if (foreignIds.size() == 0) {
                    add(refdevice,order,vrf,fastHostnameServiceDeviceMap.get(hostname));
                } else  {
                    updateFastDevice(rnode, refdevice, order,vrf,
                                     fastHostnameServiceDeviceMap.get(hostname));
                }
            }

FID:            for (String foreignId : onmsForeignIdRequisitionNodeMap.keySet()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    fails(JOB_FAILS_INTERRUPTED, e);
                    return;
                }
                i++;
                if (i == barrier) {
                    if (current < 0.99)
                        current += 0.01;
                    updateProgress(new Float(current));
                    barrier += step;
                }

                if (fastHostnameServiceDeviceMap.containsKey(foreignId)) {
                    continue;
                }
                for (String hostname : fastHostnameServiceDeviceMap.keySet()) {
                    if (onmsForeignIdRequisitionNodeMap.get(foreignId).getNodeLabel().startsWith(hostname)) {
                        continue FID;
                    }
                }
                TrentinoNetworkNode rnode = onmsForeignIdRequisitionNodeMap.get(foreignId);
                if (isManagedByFast(rnode)) {
                    delete(rnode);
                    continue;
                }
                Set<BasicInterface> inttoDelete = new HashSet<BasicInterface>();
                for (BasicInterface riface : rnode.getServiceMap().keySet()) {

                    if (fastIpHostnameMap.containsKey(riface.getIp())) {
                        continue;
                    }
                    if (riface.getOnmsprimary() == OnmsPrimary.P) {
                        continue;
                    }
                    if (riface.getDescr() != null
                            && riface.getDescr().contains("FAST")) {
                        inttoDelete.add(riface);
                    }
                }
                for (BasicInterface bi : inttoDelete) {
                    BasicService bs = new BasicService(bi);
                    bs.setService("ICMP");
                    rnode.delService(bs);
                    log(rnode,bi,ONMS_DELETED_IP);
                }
                getService().update(rnode);

            }

        } catch (final UniformInterfaceException e) {
            fails(JOB_FAILS_REQU, e);
            return;
        }

        m_job.setJobstatus(JobStatus.SUCCESS);
        m_job.setJobdescr("FAST run: Done without sync");

       if (m_syncRequisition) {
           try {
               getService().synctrue(DashBoardUtils.TN_REQU_NAME);
               logger.info("run: sync requisition"
                       + DashBoardUtils.TN_REQU_NAME);
               m_job.setJobstatus(JobStatus.SUCCESS);
               m_job.setJobdescr("FAST run: Done with sync");
               m_updates.clear();
           } catch (Exception e) {
               fails(JOB_FAILS_SYNC, e);
           }
       }


       
    }

    private void log(TrentinoNetworkNode rnode, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        jloe.setHostname(rnode.getForeignId());
        jloe.setIpaddr("NA");
        jloe.setDescription(description);
        jloe.setNote(getNote(rnode));
        jloe.setOrderCode("NA");
        log(jloe);        
        logger.info(jloe.toString());

    }

    private String getNote(TrentinoNetworkNode rnode) {
        StringBuffer deviceNote = new StringBuffer("Notes:");
        if (rnode.getForeignId() != null) {
            deviceNote.append(" ForeignId: ");
            deviceNote.append(rnode.getForeignId());
        }

        if (rnode.getNodeLabel() != null) {
            deviceNote.append(" NodeLabel: ");
            deviceNote.append(rnode.getNodeLabel());
        }

        return deviceNote.toString();
    }

    private void log(TrentinoNetworkNode rnode, BasicInterface riface,String description) {
        final JobLogEntry jloe = new JobLogEntry();
        jloe.setHostname(rnode.getForeignId());
        if (riface.getIp() != null) {
            jloe.setIpaddr(riface.getIp());
        } else {
            jloe.setIpaddr("NA");
        }
        jloe.setDescription(description);
        jloe.setNote(getNote(riface));
        log(jloe);        
        logger.info(jloe.toString());
    }


    private String getNote(BasicInterface riface) {
        StringBuffer deviceNote = new StringBuffer("Notes:");
        if (riface.getIp() != null) {
            deviceNote.append(" ipaddr: ");
            deviceNote.append(riface.getIp());
        }

        if (riface.getDescr() != null) {
            deviceNote.append(" description: ");
            deviceNote.append(riface.getDescr());
        }

        if (riface.getOnmsprimary() != null) {
            deviceNote.append(" snmpPrimary: ");
            deviceNote.append(riface.getOnmsprimary());

        }
        if (riface.getDescr() != null) {
            deviceNote.append(" descr: ");
            deviceNote.append(riface.getDescr());

        }
        return deviceNote.toString();

    }

    private void log(FastAsset asset, FastOrder order, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        if (asset.getAttributes().getHostName() != null) {
            jloe.setHostname(asset.getAttributes().getHostName());
        } else {
            jloe.setHostname("NA");
        }
        if (asset.getAttributes().getIndirizzoIP() != null) {
            jloe.setIpaddr(asset.getAttributes().getIndirizzoIP());
        } else {
            jloe.setIpaddr("NA");            
        }
        if (order != null ) {
            jloe.setOrderCode(order.getOrder_code());
        } else {
            jloe.setOrderCode("NA");
        }
        jloe.setDescription(description);
        jloe.setNote(getNote(asset,order));
        log(jloe);        
        logger.info(jloe.toString());
    }
    
    private String getNote(FastAsset device, FastOrder order) {
        StringBuffer deviceNote = new StringBuffer("");
        deviceNote.append(device.getMeta());
        deviceNote.append(" ");
        
        if (device.getAttributes().monitorato())
            deviceNote.append("monitored");
        else
            deviceNote.append("not_monitored");
        
        if (order != null) {
            deviceNote.append(" ");
            deviceNote.append(order.getOrder_customer());            
        }
        
        return deviceNote.toString();
    }

    private void add(FastAsset refdevice,FastOrder order, Categoria vrf, List<FastAsset> devices) {

        TrentinoNetworkNode rnode = new TrentinoNetworkNode(refdevice.getAttributes().getHostName(),
                                                            vrf,
                                                            DashBoardUtils.TN_REQU_NAME);
        
        rnode.setHostname(refdevice.getAttributes().getHostName());
        rnode.setForeignId(refdevice.getAttributes().getHostName());
        rnode = getnode(refdevice, order,vrf,rnode, devices.stream().map(device -> device.getAttributes().getIndirizzoIP()).collect(Collectors.toSet()));

        getService().add(rnode);
        m_updates.add(rnode);
        log(refdevice,order,ONMS_ADDED_DEVICE);
        updateSnmp(refdevice,order);
    }

    private void updateNonFast(TrentinoNetworkNode rnode,Map<Long,FastOrder> fastOrderMap,
            List<FastAsset> devices) {
        Set<String> fastipaddressonnode = new HashSet<String>();
        for (FastAsset device : devices) {
            fastipaddressonnode.add(device.getAttributes().getIndirizzoIP());
            BasicInterface bi = rnode.getInterface(device.getAttributes().getIndirizzoIP());
            if (bi == null) {
                bi = new BasicInterface();
                bi.setIp(device.getAttributes().getIndirizzoIP());
                bi.setDescr(DashBoardUtils.DESCR_FAST);
                bi.setOnmsprimary(OnmsPrimary.N);
                BasicService bs = new BasicService(bi);
                bs.setService("ICMP");
                rnode.addService(bs);
                log(rnode, bi, ONMS_ADDED_IP);
            }
        }

        for (BasicInterface bi : rnode.getServiceMap().keySet()) {
            if (bi.getDescr().contains("FAST")
                    && !fastipaddressonnode.contains(bi.getIp())) {
                BasicService bs = new BasicService(bi);
                bs.setService("ICMP");
                rnode.delService(bs);
                log(rnode, bi, ONMS_DELETED_IP);
            }
        }

        if (rnode.getOnmstate() == OnmsState.NONE)
            return;
        getService().update(rnode);
        m_updates.add(rnode);

        devices.forEach( device ->log(device,fastOrderMap.get(device.getOrder_id()),ONMS_UPDATED_NO_FAST_DEVICE));

    }

    private TrentinoNetworkNode getnode(FastAsset refdevice, FastOrder order,
            Categoria vrf , TrentinoNetworkNode rnode,
            Set<String> ipaddresses) {
        rnode.setDescr(DashBoardUtils.DESCR_FAST);
        if (refdevice.getAttributes().getDominio() == null 
                || DashBoardUtils.hasInvalidDnsBind9Label(refdevice.getAttributes().getDominio())) {
            rnode.setVrf(vrf.getDnsdomain());
        } else {
            rnode.setVrf(refdevice.getAttributes().getDominio());
        }
        rnode.setPrimary(refdevice.getAttributes().getIndirizzoIP());
        rnode.setBackupProfile(refdevice.getAttributes().getProfiloBackup());
        rnode.setSnmpProfile(refdevice.getAttributes().getProfiloSNMP());
        rnode.setNetworkCategory(vrf);
        rnode.setNotifCategory(vrf.getNotifylevel());
        rnode.setThreshCategory(vrf.getThresholdlevel());
        rnode.setCity(order.getCity());
        rnode.setAddress1(order.getAddress());
        rnode.setCircuitId(order.getOrder_code() + "-"+ order.getOrder_tariff());
        rnode.setBuilding(refdevice.getMeta().name() + ": " + order.getOrder_customer()+ "-" +order.getOrder_billing());
        
        for (String ip : ipaddresses) {
            if (ip.equals(refdevice.getAttributes().getIndirizzoIP()))
                continue;
            BasicInterface bi = rnode.getInterface(ip);
            if (bi == null) {
                bi = new BasicInterface();
                bi.setIp(ip);
                bi.setDescr(DashBoardUtils.DESCR_FAST);
                bi.setOnmsprimary(OnmsPrimary.N);
            }
            BasicService bs = new BasicService(bi);
            bs.setService("ICMP");
            rnode.addService(bs);
        }
        Set<BasicInterface> intefacetodelonnode = new HashSet<BasicInterface>();
        for (BasicInterface riface : rnode.getServiceMap().keySet()) {
            if (!ipaddresses.contains(riface.getIp())) {
                intefacetodelonnode.add(riface);
            }
        }
        for (BasicInterface bi : intefacetodelonnode) {
            BasicService bs = new BasicService(bi);
            bs.setService("ICMP");
            rnode.delService(bs);
        }
        return rnode;
    }

    private void updateFastDevice(TrentinoNetworkNode rnode, 
                    FastAsset refdevice, FastOrder order,
                    Categoria vrf,
            List<FastAsset> devices) {
        rnode = getnode(refdevice, order,
                        vrf, 
                        rnode,devices.stream().map(device -> device.getAttributes().getIndirizzoIP()).collect(Collectors.toSet()));

        if (rnode.getOnmstate() == OnmsState.NONE)
            return;

        getService().update(rnode);
        m_updates.add(rnode);
        log(refdevice,order,ONMS_UPDATED_FAST_DEVICE);
        updateSnmp(refdevice,order);
    }

    private void updateSnmp(FastAsset refdevice,FastOrder order) {
        String snmpprofile = refdevice.getAttributes().getProfiloSNMP();
        try {
            if (getService().saveSnmpProfile(refdevice.getAttributes().getIndirizzoIP(),
                                             snmpprofile)) {
                log(refdevice,order,ONMS_UPDATE_SNMP_PROFILE+ ": " + snmpprofile);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING,
                       "FAST sync: cannot set snmp profile: " + snmpprofile,
                       e);
        }
    }

    private boolean isManagedByFast(TrentinoNetworkNode rnode) {
        if (rnode.getNetworkCategory() != null
                && DashBoardUtils.m_network_levels[2].equals(rnode.getNetworkCategory().getNetworklevel())) {
            return true;
        }
        return false;
    }

    private void delete(TrentinoNetworkNode rnode) {
        rnode.setDeleteState();
        m_updates.add(rnode);
        getService().delete(rnode);
        log(rnode,ONMS_DELETED_DEVICE);

    }
}
