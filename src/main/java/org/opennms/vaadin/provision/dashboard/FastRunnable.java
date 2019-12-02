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

    private final static String FAST_NULL_ORDER_CODE ="FAST(error): Asset Null Order Code";
    private final static String FAST_ASSET_ORPHAN_ORDER_CODE ="FAST(error): Asset Orphan Order Code";
    private final static String FAST_NULL_IP ="FAST(error): Asset Null Ip Address";
    private final static String FAST_DUPLICATED_IP ="FAST(error): Asset Duplicated Ip Address";
    private final static String FAST_NULL_HOSTNAME ="FAST(error): Asset Null Hostname";
    private final static String FAST_INVALID_IP ="FAST(error): Asset Invalid Ip Address";
    private final static String FAST_INVALID_HOSTNAME ="FAST(error): Asset Invalid Hostname";
    private final static String FAST_NULL_VRF ="FAST(error): Asset Null Vrf";
    private final static String FAST_INVALID_VRF ="FAST(error): Asset Invalid Vrf";
    private final static String FAST_INVALID_DOMAIN ="FAST(error): Asset Invalid Domain";
    private final static String FAST_NULL_SNMP_PROFILE ="FAST(error): Asset Null Snmp Profile";
    private final static String FAST_INVALID_SNMP_PROFILE ="FAST(error): Asset Invalid Snmp Profile";
    private final static String FAST_NULL_BACKUP_PROFILE ="FAST(error): Asset Null Backup Profile";
    private final static String FAST_INVALID_BACKUP_PROFILE ="FAST(error): Asset Invalid Backup Profile";
    private final static String ONMS_DUPLICATED_ID ="ONMS(error): Duplicated Foreign Id";
    private final static String ONMS_NULL_IP ="ONMS(error): Null Ip Address";
    private final static String ONMS_INVALID_IP ="ONMS(error): Invalid Ip Address";
    private final static String FAST_DUPLICATED_ONMS_ID = "FAST(warning): mapped to duplicated foreign id in requisition";
    private final static String FAST_DUPLICATED_ONMS_IP = "FAST(warning): mapped to duplicated ip address in requisition";
    private final static String FAST_NO_REF_Asset = "FAST(error): no valid ref Asset"; 
    private final static String FAST_MISHMATCH_ONMS = "FAST(error): mismatch hostname/foreignIds";
    private final static String ONMS_ADDED_ASSET = "ONMS(info): added Asset";
    private final static String ONMS_DELETED_ASSET = "ONMS(info): deleted Asset";
    private final static String ONMS_UPDATED_NO_FAST_ASSET = "ONMS(info): updated Asset not managed by FAST  ";
    private final static String ONMS_UPDATED_FAST_ASSET = "ONMS(info): updated Asset managed by FAST  ";
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

    private Set<String> m_domains = new HashSet<>();

    private Map<String, Categoria> m_vrf;

    private Map<String, BackupProfile> m_backup;

    private Map<String, SnmpProfile> m_snmp;


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

        if (asset.getAttributes().getDominio() 
                != null 
            && !DashBoardUtils.isSupportedDnsDomain(asset.getAttributes().getDominio(), m_domains)) {
            log(asset,order,FAST_INVALID_DOMAIN+ ": " + asset.getAttributes().getDominio());
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

        logger.info("run: loading table dnsdomain");
        m_domains.clear();
        for (String domain: getService().getDnsDomainContainer().getDomains()) {
            m_domains.add(domain);
        }
        logger.info("run: loaded table dnsdomain");

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

        Map<String, List<FastAsset>> fastHostnameServiceAssetMap = new HashMap<>();
        Map<String, Set<String>> fastIpHostnameMap = new HashMap<String, Set<String>>();
        for (FastAsset asset : assets) {
            if (!asset.getAttributes().monitorato()) {
                continue;
            }
            if (asset.getOrder_id() == null) {
                log(asset,null,FAST_NULL_ORDER_CODE);
                continue;
            } else  if (!fastOrderMap.containsKey(asset.getOrder_id())) {
                log(asset,null,FAST_ASSET_ORPHAN_ORDER_CODE);
                continue;
            }
            FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
            if (!order.produzione()) {
                continue;
            }

            if (!isValidAsset(asset,order)) {
                continue;
            }
 
            if (!fastHostnameServiceAssetMap.containsKey(asset.getAttributes().getHostName())) {
                fastHostnameServiceAssetMap.put(asset.getAttributes().getHostName(),
                                                 new ArrayList<FastAsset>());
            }
            fastHostnameServiceAssetMap.get(asset.getAttributes().getHostName()).add(asset);

            if (!fastIpHostnameMap.containsKey(asset.getAttributes().getIndirizzoIP())) {
                fastIpHostnameMap.put(asset.getAttributes().getIndirizzoIP(),
                                        new HashSet<String>());
            }
            fastIpHostnameMap.get(asset.getAttributes().getIndirizzoIP()).add(asset.getAttributes().getHostName());
        }
        for (String ipaddr : fastIpHostnameMap.keySet()) {
            if (fastIpHostnameMap.get(ipaddr).size() == 1)
                continue;
            for (String hostname : fastIpHostnameMap.get(ipaddr)) {
                List<FastAsset> survived = new ArrayList<FastAsset>();
                for (FastAsset Asset : fastHostnameServiceAssetMap.remove(hostname)) {
                    if (Asset.getAttributes().getIndirizzoIP().equals(ipaddr)) {
                        FastOrder order = fastOrderMap.get(Asset.getOrder_id()); 
                        log(Asset,order,FAST_DUPLICATED_IP);
                    } else {
                        survived.add(Asset);
                    }
                }
                if (!survived.isEmpty())
                    fastHostnameServiceAssetMap.put(hostname, survived);
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
        int step = (fastHostnameServiceAssetMap.size()
                + onmsForeignIdRequisitionNodeMap.size()) / 100;
        logger.info("run: step: " + step);
        logger.info("run: size: " + fastHostnameServiceAssetMap.size());
        int barrier = step;

        try {

            for (String hostname : fastHostnameServiceAssetMap.keySet()) {
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
                    for (FastAsset asset : fastHostnameServiceAssetMap.get(hostname)) {
                        FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
                        log(asset,order,FAST_DUPLICATED_ONMS_ID);
                    }
                    continue;
                }
                boolean duplicated=false;
                for (FastAsset asset : fastHostnameServiceAssetMap.get(hostname)) {
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
                    if (tnnode.getNodeLabel().toLowerCase().startsWith(hostname)) {
                        foreignIds.add(tnnode.getForeignId());
                        rnode=tnnode;
                        logger.info("found foreignid="+ tnnode.getForeignId()+" for: " + hostname);
                    }
                }

                if (foreignIds.size() > 1) {
                    fastHostnameServiceAssetMap
                    .get(hostname)
                    .forEach(asset -> {
                        FastOrder order = fastOrderMap.get(asset.getOrder_id()); 
                        log(asset,order,FAST_MISHMATCH_ONMS);   
                    });
                    continue;                    
                }

                if (foreignIds.size() == 1 && !isManagedByFast(rnode)) {
                    updateNonFast(rnode, fastOrderMap,
                                  fastHostnameServiceAssetMap.get(hostname));
                    continue;
                }

                FastAsset refAsset = null; 
                for (FastAsset Asset: fastHostnameServiceAssetMap.get(hostname)) {
                    if (refAsset == null) {
                        refAsset = Asset;
                        continue;
                    }
                    if (refAsset.getAttributes().getProfiloSNMP() == null && Asset.getAttributes().getProfiloSNMP() != null) {
                        refAsset = Asset;
                        continue;
                    }
                    if (refAsset.getAttributes().getProfiloBackup() == null && Asset.getAttributes().getProfiloBackup() != null) {
                        refAsset = Asset;
                        continue;
                    }
                    if (
                            refAsset.getAttributes().getBackup() == null 
                         && Asset.getAttributes().getBackup() != null 
                            ) {
                        refAsset = Asset;
                        continue;
                    }
                    if (
                               refAsset.getAttributes().getBackup() != null 
                            && refAsset.getAttributes().getBackup() == 0 
                            && Asset.getAttributes().getBackup() != null 
                            && Asset.getAttributes().getBackup() == 1 ) {
                        refAsset = Asset;
                        continue;
                    }

                }
                FastOrder order = fastOrderMap.get(refAsset.getOrder_id());
                if (!checkManagedByFastAsset(refAsset,order)) {
                    log(refAsset,order,FAST_NO_REF_Asset);
                    continue;
                }
                
                Categoria vrf = m_vrf.get(refAsset.getAttributes().getVrf());
                
                if (foreignIds.size() == 0) {
                    add(refAsset,order,vrf,fastHostnameServiceAssetMap.get(hostname));
                } else  {
                    updateFastAsset(rnode, refAsset, order,vrf,
                                     fastHostnameServiceAssetMap.get(hostname));
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

                if (fastHostnameServiceAssetMap.containsKey(foreignId)) {
                    continue;
                }
                for (String hostname : fastHostnameServiceAssetMap.keySet()) {
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
        StringBuffer AssetNote = new StringBuffer("Notes:");
        if (rnode.getForeignId() != null) {
            AssetNote.append(" ForeignId: ");
            AssetNote.append(rnode.getForeignId());
        }

        if (rnode.getNodeLabel() != null) {
            AssetNote.append(" NodeLabel: ");
            AssetNote.append(rnode.getNodeLabel());
        }

        return AssetNote.toString();
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
        StringBuffer AssetNote = new StringBuffer("Notes:");
        if (riface.getIp() != null) {
            AssetNote.append(" ipaddr: ");
            AssetNote.append(riface.getIp());
        }

        if (riface.getDescr() != null) {
            AssetNote.append(" description: ");
            AssetNote.append(riface.getDescr());
        }

        if (riface.getOnmsprimary() != null) {
            AssetNote.append(" snmpPrimary: ");
            AssetNote.append(riface.getOnmsprimary());

        }
        if (riface.getDescr() != null) {
            AssetNote.append(" descr: ");
            AssetNote.append(riface.getDescr());

        }
        return AssetNote.toString();

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
    
    private String getNote(FastAsset Asset, FastOrder order) {
        StringBuffer AssetNote = new StringBuffer("");
        AssetNote.append(Asset.getMeta());
        AssetNote.append(" ");
        
        if (Asset.getAttributes().monitorato())
            AssetNote.append("monitored");
        else
            AssetNote.append("not_monitored");
        
        if (order != null) {
            AssetNote.append(" ");
            AssetNote.append(order.getOrder_customer());            
        }
        
        return AssetNote.toString();
    }

    private void add(FastAsset refAsset,FastOrder order, Categoria vrf, List<FastAsset> Assets) {

        TrentinoNetworkNode rnode = new TrentinoNetworkNode(refAsset.getAttributes().getHostName(),
                                                            vrf,
                                                            DashBoardUtils.TN_REQU_NAME);
        
        rnode.setHostname(refAsset.getAttributes().getHostName());
        rnode.setForeignId(refAsset.getAttributes().getHostName());
        rnode = getnode(refAsset, order,vrf,rnode, Assets.stream().map(Asset -> Asset.getAttributes().getIndirizzoIP()).collect(Collectors.toSet()));

        getService().add(rnode);
        m_updates.add(rnode);
        log(refAsset,order,ONMS_ADDED_ASSET);
        updateSnmp(refAsset,order);
    }

    private void updateNonFast(TrentinoNetworkNode rnode,Map<Long,FastOrder> fastOrderMap,
            List<FastAsset> Assets) {
        Set<String> fastipaddressonnode = new HashSet<String>();
        for (FastAsset Asset : Assets) {
            fastipaddressonnode.add(Asset.getAttributes().getIndirizzoIP());
            BasicInterface bi = rnode.getInterface(Asset.getAttributes().getIndirizzoIP());
            if (bi == null) {
                bi = new BasicInterface();
                bi.setIp(Asset.getAttributes().getIndirizzoIP());
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

        Assets.forEach( Asset ->log(Asset,fastOrderMap.get(Asset.getOrder_id()),ONMS_UPDATED_NO_FAST_ASSET));

    }

    private TrentinoNetworkNode getnode(FastAsset refAsset, FastOrder order,
            Categoria vrf , TrentinoNetworkNode rnode,
            Set<String> ipaddresses) {
        rnode.setDescr(DashBoardUtils.DESCR_FAST);
        if (refAsset.getAttributes().getDominio() == null 
                || DashBoardUtils.hasInvalidDnsBind9Label(refAsset.getAttributes().getDominio())) {
            rnode.setVrf(vrf.getDnsdomain());
        } else {
            rnode.setVrf(refAsset.getAttributes().getDominio());
        }
        rnode.setPrimary(refAsset.getAttributes().getIndirizzoIP());
        rnode.setBackupProfile(refAsset.getAttributes().getProfiloBackup());
        rnode.setSnmpProfile(refAsset.getAttributes().getProfiloSNMP());
        rnode.setNetworkCategory(vrf);
        rnode.setNotifCategory(vrf.getNotifylevel());
        rnode.setThreshCategory(vrf.getThresholdlevel());
        rnode.setCity(order.getCity());
        rnode.setAddress1(order.getAddress());
        rnode.setCircuitId(order.getOrder_code() + "-"+ order.getOrder_tariff());
        rnode.setBuilding(refAsset.getMeta().name() + ": " + order.getOrder_customer()+ "-" +order.getOrder_billing());
        
        for (String ip : ipaddresses) {
            if (ip.equals(refAsset.getAttributes().getIndirizzoIP()))
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

    private void updateFastAsset(TrentinoNetworkNode rnode, 
                    FastAsset refAsset, FastOrder order,
                    Categoria vrf,
            List<FastAsset> Assets) {
        rnode = getnode(refAsset, order,
                        vrf, 
                        rnode,Assets.stream().map(Asset -> Asset.getAttributes().getIndirizzoIP()).collect(Collectors.toSet()));

        if (rnode.getOnmstate() == OnmsState.NONE)
            return;

        getService().update(rnode);
        m_updates.add(rnode);
        log(refAsset,order,ONMS_UPDATED_FAST_ASSET);
        updateSnmp(refAsset,order);
    }

    private void updateSnmp(FastAsset refAsset,FastOrder order) {
        String snmpprofile = refAsset.getAttributes().getProfiloSNMP();
        try {
            if (getService().saveSnmpProfile(refAsset.getAttributes().getIndirizzoIP(),
                                             snmpprofile)) {
                log(refAsset,order,ONMS_UPDATE_SNMP_PROFILE+ ": " + snmpprofile);
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
        log(rnode,ONMS_DELETED_ASSET);

    }
}
