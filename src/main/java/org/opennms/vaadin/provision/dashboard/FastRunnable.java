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

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.KettleDao;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsState;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.Categoria;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.Job.JobStatus;
import org.opennms.vaadin.provision.model.JobLogEntry;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;

public abstract class FastRunnable implements Runnable {

    private final static String FAST_NULL_ORDER_CODE ="FAST(error): Device Null Order Code";
    private final static String FAST_ASSET_ORPHAN_ORDER_CODE ="FAST(error): Asset Orphan Order Code";
    private final static String FAST_INVALID_ORDER_CODE ="FAST(error): Device with Invalid Order Code";
    private final static String FAST_MISTMATCH_ORDER_CODE ="FAST(error): Asset Order Code has different VRF";
    private final static String FAST_NULL_IP ="FAST(error): Device Null Ip Address";
    private final static String FAST_DUPLICATED_IP ="FAST(error): Device Duplicated Ip Address";
    private final static String FAST_NULL_HOSTNAME ="FAST(error): Device Null Hostname";
    private final static String FAST_INVALID_IP ="FAST(error): Device Invalid Ip Address";
    private final static String FAST_INVALID_HOSTNAME ="FAST(error): Device Invalid Hostname";
    private final static String FAST_NULL_NOTIFY ="FAST(error): Device Null Notify Category";
    private final static String FAST_INVALID_NOTIFY ="FAST(error): Device Invalid Notify Category";
    private final static String FAST_NULL_SNMP_PROFILE ="FAST(error): Device Null Snmp Profile";
    private final static String FAST_INVALID_SNMP_PROFILE ="FAST(error): Device Invalid Snmp Profile";
    private final static String FAST_NULL_BACKUP_PROFILE ="FAST(error): Device Null Backup Profile";
    private final static String FAST_INVALID_BACKUP_PROFILE ="FAST(error): Device Invalid Backup Profile";
    private final static String FAST_NULL_VRF ="FAST(error): Asset Null VRF";
    private final static String FAST_INVALID_VRF ="FAST(error): Asset Invalid VRF";
    private final static String ONMS_DUPLICATED_ID ="ONMS(error): Duplicated Foreign Id";
    private final static String ONMS_NULL_IP ="ONMS(error): Null Ip Address";
    private final static String ONMS_INVALID_IP ="ONMS(error): Invalid Ip Address";
    private final static String FAST_DUPLICATED_ONMS_ID = "FAST(warning): mapped to duplicated foreign id in requisition";
    private final static String FAST_DUPLICATED_ONMS_IP = "FAST(warning): mapped to duplicated ip address in requisition";
    private final static String FAST_MISHMATCH_ONMS = "FAST(error): mismatch hostname/foreignIds";
    private final static String FAST_NO_REF_DEVICE = "FAST(error): no valid ref device for order code";
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
    private final static String JOB_FAILS_FAST_DEVICES="FAST run: Failed Load Fast Service Devices";
    private final static String JOB_FAILS_FAST_LINKS="FAST run: Failed load Fast Service Links";
    private final static String JOB_FAILS_KETTLE="FAST run: Failed Kettle Run";
    
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
    
    private boolean isValid(FastServiceLink link) {
        boolean valid = true;
        if (link.getOrderCode() == null) {               
            log(link,FAST_ASSET_ORPHAN_ORDER_CODE);
            valid = false;
        }
        if (link.getVrf() == null) {
            log(link,FAST_NULL_VRF);
            valid = false;
        } else if (!m_vrf.containsKey(link.getVrf())) {
            log(link,FAST_INVALID_VRF+": " + link.getVrf());
            valid = false;
        }
        return valid;
    }
    
    
    private boolean isValid(FastServiceDevice device, Set<String> orderCodes) {
        boolean valid = true;
        if (device.getOrderCode() == null) {
            log(device,FAST_NULL_ORDER_CODE);
            valid = false;
        } else if (!orderCodes.contains(device.getOrderCode())){
            log(device,FAST_INVALID_ORDER_CODE);
            valid = false;
        }
        if (device.getIpaddr() == null) {
            log(device,FAST_NULL_IP);
            valid = false;
        }  else if (DashBoardUtils.hasInvalidIp(device.getIpaddr())) {
            log(device,FAST_INVALID_IP);
            valid = false;
        } 
        if (device.getHostname() == null) {
            log(device,FAST_NULL_HOSTNAME);                
            valid = false;
        } else if (DashBoardUtils.hasInvalidDnsBind9Label(device.getHostname())) {
            log(device,FAST_INVALID_HOSTNAME);
            valid = false;
        } 
        return valid;
    }
    
    private FastServiceDevice getRefFastServiceDevice(List<FastServiceDevice> devices) {
        FastServiceDevice refdevice = null;
        for (FastServiceDevice device : devices) {
            if (refdevice == null) {
                refdevice = device;
            } else if (device.isMaster() && !refdevice.isMaster()) {
                refdevice = device;
            } else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
                refdevice = device;
            } else if (device.getIpAddrLan() != null
                    && refdevice.getIpAddrLan() == null) {
                refdevice = device;
            }
        }
        return refdevice;
    }
    
    private boolean isValidFastDevice(FastServiceDevice device) {
        boolean valid = true;
        if (device.getNotifyCategory() == null) {
            log(device,FAST_NULL_NOTIFY);
            valid = false;
        } else if (!DashBoardUtils.isValidNotifyLevel(device.getNotifyCategory())) {
            log(device,FAST_INVALID_NOTIFY+ " :" +device.getNotifyCategory());
            valid = false;
        } 
        if (device.getSnmpprofile() == null) {
            log(device,FAST_NULL_SNMP_PROFILE);
            valid = false;
        } else if (!m_snmp.containsKey(device.getSnmpprofile())) {
            log(device,FAST_INVALID_SNMP_PROFILE+ " :" +device.getSnmpprofile());
            valid = false;
        } 
        if (device.getBackupprofile() == null) {
            log(device,FAST_NULL_BACKUP_PROFILE);
            valid = false;
        } else if(!m_backup.containsKey(device.getBackupprofile())) {
            log(device,FAST_INVALID_BACKUP_PROFILE+ " :" +device.getBackupprofile());
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

        KettleDao kettleDao = new KettleDao(
                                        new JerseyClientImpl(
                                                     getService().getConfig().getKettleUrl(),
                                                     getService().getConfig().getKettleUsername(),
                                                     getService().getConfig().getKettlePassword()
                                         )
                                    );

        try {
            KettleRunJob kjob = kettleDao.runJob();
            KettleJobStatus status = kettleDao.jobStatus(kjob);
            logger.info("run: kettle status: " + status.getStatusDescr());
            while (kettleDao.isRunning(status)) {
                Thread.sleep(1000);
                status = kettleDao.jobStatus(kjob);
                logger.info("run: kettle status: " + status.getStatusDescr());
            }
            if (!kettleDao.isFinished(status)
                    || !kettleDao.isCompleted(status)) {
                fails(JOB_FAILS_KETTLE, new Exception(status.getErroDescr()));
                return;
            }
        } catch (Exception e) {
            fails(JOB_FAILS_KETTLE, e);
            return;
        }

        FastServiceDeviceDao m_fastservicedevicecontainer;
        try {
            logger.info("run: loading table fastservicedevice");
            m_fastservicedevicecontainer = new FastServiceDeviceDao(new FreeformQuery("select * from fastservicedevices",
                                                                                      m_session.getPool()));
            logger.info("run: loaded table fastservicedevice");
        } catch (SQLException e) {
            fails(JOB_FAILS_FAST_DEVICES, e);
            return;
        }

        FastServiceLinkDao m_fastservicelinkcontainer;
        try {
            logger.info("run: loading table fastservicelink");
            m_fastservicelinkcontainer = new FastServiceLinkDao(new FreeformQuery("select * from fastservicelink",
                                                                                  m_session.getPool()));
            logger.info("run: loaded table fastservicelink");
        } catch (SQLException e) {
            fails(JOB_FAILS_FAST_LINKS, e);
            return;
        }

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
        
        Map<String, String> fastOrderCodeVrfMap = new HashMap<>();
        Map<String, List<FastServiceLink>> fastOrderCodeAssetsMap = new HashMap<>();
        final Set<String> duplicatedOrderCode = new HashSet<String>();
        for (FastServiceLink link : m_fastservicelinkcontainer.getFastServiceLinks()) {
            if (isValid(link)) {
                String predecheckedVrf = fastOrderCodeVrfMap.get(link.getOrderCode());
                if ( predecheckedVrf != null && !predecheckedVrf.equals(link.getVrf())) {
                    duplicatedOrderCode.add(link.getOrderCode());   
                    fastOrderCodeAssetsMap.get(predecheckedVrf).add(link);
                } else {
                    fastOrderCodeVrfMap.put(link.getOrderCode(), link.getVrf());
                }
                if (!fastOrderCodeAssetsMap.containsKey(link.getOrderCode())) {
                    fastOrderCodeAssetsMap.put(link.getOrderCode(), new ArrayList<>());
                }
                fastOrderCodeAssetsMap.get(link.getOrderCode()).add(link);
            }
        }
        
        for (String duplioc : duplicatedOrderCode) {
            fastOrderCodeVrfMap.remove(duplioc);
            fastOrderCodeAssetsMap.get(duplioc).forEach(link -> log(link, FAST_MISTMATCH_ORDER_CODE));
        }

        Map<String, List<FastServiceDevice>> fastHostnameServiceDeviceMap = new HashMap<>();
        Map<String, Set<String>> fastIpHostnameMap = new HashMap<String, Set<String>>();
        for (FastServiceDevice device : m_fastservicedevicecontainer.getFastServiceDevices()) {
            if (device.isNotmonitoring()) {
                continue;
            }
            if (isValid(device, fastOrderCodeVrfMap.keySet()) ) {

                if (!fastHostnameServiceDeviceMap.containsKey(device.getHostname())) {
                    fastHostnameServiceDeviceMap.put(device.getHostname(),
                                                     new ArrayList<FastServiceDevice>());
                }
                fastHostnameServiceDeviceMap.get(device.getHostname()).add(device);

                if (!fastIpHostnameMap.containsKey(device.getIpaddr())) {
                    fastIpHostnameMap.put(device.getIpaddr(),
                                            new HashSet<String>());
                }
                fastIpHostnameMap.get(device.getIpaddr()).add(device.getHostname());
            }
        }        
        for (String ipaddr : fastIpHostnameMap.keySet()) {
            if (fastIpHostnameMap.get(ipaddr).size() == 1)
                continue;
            for (String hostname : fastIpHostnameMap.get(ipaddr)) {
                List<FastServiceDevice> survived = new ArrayList<FastServiceDevice>();
                for (FastServiceDevice device : fastHostnameServiceDeviceMap.remove(hostname)) {
                    if (device.getIpaddr().equals(ipaddr)) {
                        log(device,FAST_DUPLICATED_IP);
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
                    for (FastServiceDevice device : fastHostnameServiceDeviceMap.get(hostname)) {
                        log(device,FAST_DUPLICATED_ONMS_ID);
                    }
                    continue;
                }
                boolean duplicated=false;
                for (FastServiceDevice device : fastHostnameServiceDeviceMap.get(hostname)) {
                    if (onmsDuplicatedIpAddress.contains(device.getIpaddr())) {
                        log(device,FAST_DUPLICATED_ONMS_IP);
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
                    fastHostnameServiceDeviceMap.get(hostname).stream().forEach(device -> log(device,FAST_MISHMATCH_ONMS));
                    continue;                    
                }

                if (foreignIds.size() == 1 && !isManagedByFast(rnode)) {
                    updateNonFast(rnode,
                                  fastHostnameServiceDeviceMap.get(hostname));
                    continue;
                }
                List<FastServiceDevice> validFastServicesDevices = new ArrayList<>();
                for (FastServiceDevice device:fastHostnameServiceDeviceMap.get(hostname)) {
                    if (isValidFastDevice(device)) {
                        validFastServicesDevices.add(device);
                    }
                }
                FastServiceDevice refdevice = getRefFastServiceDevice(validFastServicesDevices);
                if (refdevice == null) {
                    for (FastServiceDevice device : validFastServicesDevices) {
                        log(device,FAST_NO_REF_DEVICE);
                    }
                    continue;
                }
                List<FastServiceLink> fastAssets = fastOrderCodeAssetsMap.get(refdevice.getOrderCode());
                FastServiceLink refLink = electFastLink(fastAssets);
                if (refLink == null) {
                    fastAssets.forEach( link -> log(link,FAST_INVALID_VRF));
                    continue;
                }
                Categoria categoria = m_vrf.get(refLink.getVrf());

                if (foreignIds.size() == 0) {
                    add(refdevice,categoria,refLink.getDeliveryCode(),refLink.getSiteCode(),validFastServicesDevices);
                } else  {
                    updateFastDevice(rnode, refdevice, categoria, refLink.getDeliveryCode(),refLink.getSiteCode(),
                                   validFastServicesDevices);
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

 
    private FastServiceLink electFastLink(List<FastServiceLink> fastAssets) {
        FastServiceLink fastLink = null;
        for (FastServiceLink fsl: fastAssets) {
            if (fastLink == null) {
                fastLink = fsl;
                continue;
            }
            if (fastLink.getDeliveryCode() == null && fsl.getDeliveryCode() != null) {
                fastLink = fsl;
            } else if (fastLink.getSiteCode() == null && fsl.getSiteCode() != null) {
                fastLink = fsl;
            }
            
                
        }
        
        return fastLink;
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

    private void log(FastServiceDevice device, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        if (device.getHostname() != null) {
            jloe.setHostname(device.getHostname());
        } else {
            jloe.setHostname("NA");
        }
        if (device.getIpaddr() != null) {
            jloe.setIpaddr(device.getIpaddr());
        } else {
            jloe.setIpaddr("NA");            
        }
        if (device.getOrderCode() != null) {
            jloe.setOrderCode(device.getOrderCode());
        } else {
            jloe.setOrderCode("NA");
        }
        jloe.setDescription(description);
        jloe.setNote(getNote(device));
        log(jloe);        
        logger.info(jloe.toString());
    }
    
    private String getNote(FastServiceDevice device) {
        StringBuffer deviceNote = new StringBuffer("");
        if (device.getDeviceType() != null) {
            deviceNote.append(device.getDeviceType());
            deviceNote.append(" ");
        }
        if (device.getCity() != null) {
            deviceNote.append(device.getCity());
            deviceNote.append(" ");
        }
        if (device.isNotmonitoring())
            deviceNote.append("not_monitored");
        else
            deviceNote.append("monitored");
        return deviceNote.toString();
    }

    private void log(FastServiceLink link, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        if (link.getDeliveryDeviceClientSide() != null) {
            jloe.setHostname(link.getDeliveryDeviceClientSide());
        } else {
            jloe.setHostname("NA");
        }
        jloe.setIpaddr("NA");
        if (link.getOrderCode() != null) {
            jloe.setOrderCode(link.getOrderCode());
        } else {
            jloe.setOrderCode("NA");
            
        }
        jloe.setDescription(description);
        jloe.setNote(getNote(link));
        log(jloe);
        logger.info(jloe.toString());
    }

    private String getNote(FastServiceLink link) {
        StringBuffer deviceNote = new StringBuffer("");
        if (link.getVrf() != null) {
            deviceNote.append(link.getVrf());
            deviceNote.append(" ");
        }
        if (link.getDeliveryDeviceNetworkSide() != null) {
            deviceNote.append(link.getDeliveryDeviceNetworkSide());
            deviceNote.append(" ");
        }
        if (link.getDeliveryCode() != null) {
            deviceNote.append(link.getDeliveryCode());
        }

        return deviceNote.toString();
    }

    private void add(FastServiceDevice refdevice, 
                Categoria vrf, 
                String deliveryCode,
                String siteCode,
                List<FastServiceDevice> devices) {

        TrentinoNetworkNode rnode = new TrentinoNetworkNode(refdevice.getHostname(),
                                                            vrf,
                                                            DashBoardUtils.TN_REQU_NAME);
        rnode.setHostname(refdevice.getHostname());
        rnode.setForeignId(refdevice.getHostname());
        rnode = getnode(refdevice, vrf, deliveryCode, siteCode, rnode, devices.stream().map(device -> device.getIpaddr()).collect(Collectors.toSet()));

        getService().add(rnode);
        m_updates.add(rnode);
        log(refdevice,ONMS_ADDED_DEVICE);
        updateSnmp(refdevice);
    }

    private void updateNonFast(TrentinoNetworkNode rnode,
            List<FastServiceDevice> devices) {
        Set<String> fastipaddressonnode = new HashSet<String>();
        for (FastServiceDevice device : devices) {
            fastipaddressonnode.add(device.getIpaddr());
            BasicInterface bi = rnode.getInterface(device.getIpaddr());
            if (bi == null) {
                bi = new BasicInterface();
                bi.setIp(device.getIpaddr());
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

        devices.forEach( device ->log(device,ONMS_UPDATED_NO_FAST_DEVICE));

    }

    private TrentinoNetworkNode getnode(FastServiceDevice refdevice,
            Categoria vrf , String deliveryCode, String siteCode,TrentinoNetworkNode rnode,
            Set<String> ipaddresses) {
        rnode.setDescr(DashBoardUtils.DESCR_FAST);
        rnode.setVrf(vrf.getDnsdomain());
        rnode.setPrimary(refdevice.getIpaddr());
        rnode.setBackupProfile(refdevice.getBackupprofile());
        rnode.setSnmpProfile(refdevice.getSnmpprofile());
        rnode.setNetworkCategory(vrf);
        if (refdevice.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify)) {
            rnode.setNotifCategory(vrf.getNotifylevel());
        } else {
            rnode.setNotifCategory(refdevice.getNotifyCategory());
        }
        rnode.setThreshCategory(vrf.getThresholdlevel());
        rnode.setCity(refdevice.getCity());

        StringBuffer address1 = new StringBuffer();
        if (refdevice.getAddressDescr() != null)
            address1.append(refdevice.getAddressDescr());
        if (refdevice.getAddressName() != null) {
            if (address1.length() > 0)
                address1.append(" ");
            address1.append(refdevice.getAddressName());
        }
        if (refdevice.getAddressNumber() != null) {
            if (address1.length() > 0)
                address1.append(" ");
            address1.append(refdevice.getAddressNumber());
        }
        rnode.setAddress1(address1.toString());
        rnode.setCircuitId(deliveryCode);
        rnode.setBuilding(refdevice.getIstat() + "-" + siteCode);

        for (String ip : ipaddresses) {
            if (ip.equals(refdevice.getIpaddr()))
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
                    FastServiceDevice refdevice, 
                    Categoria vrf,
                    String deliveryCode, 
                    String siteCode,
            List<FastServiceDevice> devices) {
        rnode = getnode(refdevice,
                        vrf, deliveryCode, siteCode,
                        rnode,devices.stream().map(device -> device.getIpaddr()).collect(Collectors.toSet()));

        if (rnode.getOnmstate() == OnmsState.NONE)
            return;

        getService().update(rnode);
        m_updates.add(rnode);
        log(refdevice,ONMS_UPDATED_FAST_DEVICE);
        updateSnmp(refdevice);
    }

    private void updateSnmp(FastServiceDevice refdevice) {
        String snmpprofile = refdevice.getSnmpprofile();
        try {
            if (getService().saveSnmpProfile(refdevice.getIpaddr(),
                                             snmpprofile)) {
                log(refdevice,ONMS_UPDATE_SNMP_PROFILE+ ": " + snmpprofile);
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
