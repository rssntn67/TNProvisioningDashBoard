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

    private final static String FAST_NULL_ORDER_CODE ="FAST(error): Null Order Code";
    private final static String FAST_DUPLICATED_ORDER_CODE ="FAST(error): Duplicated Order Code";
    private final static String FAST_NULL_IP ="FAST(error): Null Ip Address";
    private final static String FAST_DUPLICATED_IP ="FAST(error): Duplicated Ip Address";
    private final static String FAST_NULL_HOSTNAME ="FAST(error): Null Hostname";
    private final static String FAST_INVALID_IP ="FAST(error): Invalid Ip Address";
    private final static String FAST_INVALID_HOSTNAME ="FAST(error): Invalid Hostname";
    private final static String FAST_NULL_NOTIFY ="FAST(error): Null Notify Category";
    private final static String FAST_INVALID_NOTIFY ="FAST(error): Invalid Notify Category";
    private final static String FAST_NULL_SNMP_PROFILE ="FAST(error): Null Snmp Profile";
    private final static String FAST_INVALID_SNMP_PROFILE ="FAST(error): Invalid Snmp Profile";
    private final static String FAST_NULL_BACKUP_PROFILE ="FAST(error): Null Backup Profile";
    private final static String FAST_INVALID_BACKUP_PROFILE ="FAST(error): Invalid Backup Profile";
    private final static String FAST_NULL_VRF ="FAST(error): Null VRF";
    private final static String FAST_INVALID_VRF ="FAST(error): Invalid VRF";
    private final static String ONMS_DUPLICATED_ID ="ONMS(error): Duplicated Foreign Id";
    private final static String ONMS_NULL_IP ="ONMS(error): Null Ip Address";
    private final static String ONMS_INVALID_IP ="ONMS(error): Invalid Ip Address";
    private final static String FAST_DUPLICATED_ONMS_ID = "FAST(warning): mapped to duplicated foreign id in requisition";
    private final static String FAST_DUPLICATED_ONMS_IP = "FAST(warning): mapped to duplicated ip address in requisition";
    private final static String FAST_MISHMATCH_ONMS = "FAST(error): mismatch hostname/foreignIds";
    private final static String FAST_NO_REF_DEVICE = "FAST(error): no valid ref device for order code";
    private final static String FAST_NO_DEVICE = "FAST(error): no device find";
    private final static String ONMS_ADDED_DEVICE = "ONMS(info): added device";
    private final static String ONMS_DELETED_DEVICE = "ONMS(info): deleted device";
    private final static String ONMS_UPDATED_NO_FAST_DEVICE = "ONMS(info): updated device not maneged by FAST  ";
    private final static String ONMS_UPDATED_FAST_DEVICE = "ONMS(info): updated device maneged by FAST  ";
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
    
    private void sync() {
        KettleDao kettleDao = new KettleDao(new JerseyClientImpl(getService().getConfig().getKettleUrl(),
                                                                 getService().getConfig().getKettleUsername(),
                                                                 getService().getConfig().getKettlePassword()));

        try {
            KettleRunJob kjob = kettleDao.runJob();
            KettleJobStatus status = kettleDao.jobStatus(kjob);
            while (kettleDao.isRunning(status)) {
                Thread.sleep(1000);
                status = kettleDao.jobStatus(kjob);
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
        
        logger.info("run: loading table vrf");
        Map<String, Categoria> m_vrf = getService().getCatContainer().getCatMap();
        logger.info("run: loaded table vrf");

        logger.info("run: loading table backupprofile");
        Map<String, BackupProfile> m_backup = getService().getBackupProfileContainer().getBackupProfileMap();
        logger.info("run: loaded table backupprofile");

        logger.info("run: loading table snmpprofile");
        Map<String, SnmpProfile> m_snmp = getService().getSnmpProfileContainer().getSnmpProfileMap();
        logger.info("run: loaded table snmpprofile");

        Map<String, List<FastServiceDevice>> m_fastHostnameServiceDeviceMap = new HashMap<>();
        Map<String, Set<String>> m_fastIpHostnameMap = new HashMap<String, Set<String>>();

        for (FastServiceDevice device : m_fastservicedevicecontainer.getFastServiceDevices()) {
            boolean valid = true;
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
            if (device.getNotifyCategory() == null) {
                log(device,FAST_NULL_NOTIFY);
            } else if (!DashBoardUtils.isValidNotifyLevel(device.getNotifyCategory())) {
                log(device,FAST_INVALID_NOTIFY+ " :" +device.getNotifyCategory());
            } 
            if (device.getSnmpprofile() == null) {
                log(device,FAST_NULL_SNMP_PROFILE);
            } else if (!m_snmp.containsKey(device.getSnmpprofile())) {
                log(device,FAST_INVALID_SNMP_PROFILE+ " :" +device.getSnmpprofile());
            } 
            if (device.getBackupprofile() == null) {
                log(device,FAST_NULL_BACKUP_PROFILE);
            } else if(!m_backup.containsKey(device.getBackupprofile())) {
                log(device,FAST_INVALID_BACKUP_PROFILE+ " :" +device.getBackupprofile());
            } 

            if (device.getOrderCode() == null) {
                log(device,FAST_NULL_ORDER_CODE);
                valid = false;
            } 

            if (valid && !device.isNotmonitoring()) {

                if (!m_fastHostnameServiceDeviceMap.containsKey(device.getHostname().toLowerCase())) {
                    m_fastHostnameServiceDeviceMap.put(device.getHostname().toLowerCase(),
                                                     new ArrayList<FastServiceDevice>());
                }
                m_fastHostnameServiceDeviceMap.get(device.getHostname().toLowerCase()).add(device);

                if (!m_fastIpHostnameMap.containsKey(device.getIpaddr())) {
                    m_fastIpHostnameMap.put(device.getIpaddr(),
                                            new HashSet<String>());
                }
                m_fastIpHostnameMap.get(device.getIpaddr()).add(device.getHostname().toLowerCase());
            }
        }
        
        for (String ipaddr : m_fastIpHostnameMap.keySet()) {
            if (m_fastIpHostnameMap.get(ipaddr).size() == 1)
                continue;
            for (String hostname : m_fastIpHostnameMap.get(ipaddr)) {
                List<FastServiceDevice> survived = new ArrayList<FastServiceDevice>();
                for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.remove(hostname)) {
                    if (device.getIpaddr().equals(ipaddr)) {
                        log(device,FAST_DUPLICATED_IP);
                    } else {
                        survived.add(device);
                    }
                }
                if (!survived.isEmpty())
                    m_fastHostnameServiceDeviceMap.put(hostname, survived);

            }
        }


        Map<String, FastServiceLink> m_fastOrderCodeServiceLinkMap = new HashMap<>();
        final Set<String> duplicatedEntry = new HashSet<String>();
        boolean valid = true;
        for (FastServiceLink link : m_fastservicelinkcontainer.getFastServiceLinks()) {
            if (link.getOrderCode() == null) {               
                log(link,FAST_NULL_ORDER_CODE);
                valid = false;
            }
            if (link.getVrf() == null) {
                log(link,FAST_NULL_VRF);
                valid = false;
            } else if (!m_vrf.containsKey(link.getVrf())) {
                log(link,FAST_INVALID_VRF+": " + link.getVrf());
                valid = false;
            }

            if (m_fastOrderCodeServiceLinkMap.containsKey(link.getOrderCode())) {
                log(link, FAST_DUPLICATED_ORDER_CODE);
                duplicatedEntry.add(link.getOrderCode());                
                continue;

            }
            if (valid) {
                m_fastOrderCodeServiceLinkMap.put(link.getOrderCode(), link);
            }
        }
        for (String duplioc : duplicatedEntry) {
            FastServiceLink link = m_fastOrderCodeServiceLinkMap.remove(duplioc);
            log(link, FAST_DUPLICATED_ORDER_CODE);
            duplicatedEntry.add(link.getOrderCode());                
        }


        Map<String, TrentinoNetworkNode> m_onmsForeignIdRequisitionNodeMap = new HashMap<>();
        Set<String> m_onmsDuplicatedForeignId = new HashSet<String>();
        Set<String> m_onmsDuplicatedIpAddress = new HashSet<String>();
        for (String id : requisition.getItemIds()) {
            TrentinoNetworkNode rnode = requisition.getItem(id).getBean();
            if (m_onmsForeignIdRequisitionNodeMap.containsKey(rnode.getForeignId())) {
                m_onmsDuplicatedForeignId.add(rnode.getForeignId());
                log(rnode,ONMS_DUPLICATED_ID);
            } else {
                m_onmsForeignIdRequisitionNodeMap.put(rnode.getForeignId(),
                                                    rnode);
            }
        }

        for (String dupforeignid : m_onmsDuplicatedForeignId) {
            TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.remove(dupforeignid);
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
            m_onmsDuplicatedIpAddress.add(ipaddr);
            for (String foreignid : onmsIpForeignIdMap.get(ipaddr)) {
                TrentinoNetworkNode duplicatedipnode = m_onmsForeignIdRequisitionNodeMap.remove(foreignid);
                if (duplicatedipnode == null)
                    continue;
                BasicInterface duplicatedinterface = duplicatedipnode.getInterface(ipaddr);
                if (duplicatedinterface == null)
                    continue;
                log(duplicatedipnode,duplicatedinterface,ONMS_DUPLICATED_ID);
            }

        }
                
        logger.info("run: sync Fast devices with Requisition");
        double current = 0.0;
        updateProgress(new Float(current));

        int i = 0;
        int step = (m_fastHostnameServiceDeviceMap.size()
                + m_onmsForeignIdRequisitionNodeMap.size()) / 100;
        logger.info("run: step: " + step);
        logger.info("run: size: " + m_fastHostnameServiceDeviceMap.size());
        int barrier = step;

        try {

            for (String hostname : m_fastHostnameServiceDeviceMap.keySet()) {
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

                if (!checkduplicatedhostname(hostname)
                        && !checkduplicatedipaddress(hostname)) {
                    Set<String> foreignIds = new HashSet<String>();
                    if (m_onmsForeignIdRequisitionNodeMap.containsKey(hostname)) {
                        foreignIds.add(hostname);
                    }

                    for (TrentinoNetworkNode rnode : m_onmsForeignIdRequisitionNodeMap.values()) {
                        if (rnode.getNodeLabel().startsWith(hostname)) {
                            foreignIds.add(rnode.getForeignId());
                        }
                    }

                    if (foreignIds.size() == 0) {
                        add(hostname);
                    } else if (foreignIds.size() == 1) {
                        String foreignId = foreignIds.iterator().next();
                        TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.get(foreignId);
                        if (isManagedByFast(rnode)) {
                            updateFastDevice(rnode,
                                       m_fastHostnameServiceDeviceMap.get(hostname));
                        } else {
                            updateNonFast(rnode,
                                          m_fastHostnameServiceDeviceMap.get(hostname));
                        }
                    } else {
                        mismatch(hostname, foreignIds);
                    }
                }
            }

            for (String foreignId : m_onmsForeignIdRequisitionNodeMap.keySet()) {
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

                if (isDeviceInFast(foreignId))
                    continue;
                TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.get(foreignId);
                if (isManagedByFast(rnode)) {
                    delete(rnode);
                    continue;
                }
                Set<BasicInterface> inttoDelete = new HashSet<BasicInterface>();
                for (BasicInterface riface : rnode.getServiceMap().keySet()) {

                    if (m_fastIpHostnameMap.containsKey(riface.getIp())) {
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
        log(jloe);        
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
    }
    
    private String getNote(FastServiceDevice device) {
        StringBuffer deviceNote = new StringBuffer("Notes:");
        if (device.getDeviceType() != null) {
            deviceNote.append(" deviceType: ");
            deviceNote.append(device.getDeviceType());
        }
        if (device.getCity() != null) {
            deviceNote.append(" city: ");
            deviceNote.append(device.getCity());
        }
        if (device.getIpAddrLan() != null) {
            deviceNote.append(" ip lan");
            deviceNote.append(device.getIpAddrLan());
        }
        if (device.isNotmonitoring())
            deviceNote.append(" not_monitored");
        else
            deviceNote.append(" monitored");
        return deviceNote.toString();
    }

    private void log(FastServiceLink link, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        jloe.setHostname(link.getDeliveryDeviceClientSide());
        jloe.setIpaddr("NA");
        jloe.setOrderCode("NA");
        jloe.setDescription(description);
        jloe.setNote(getNote(link));
    }

    private String getNote(FastServiceLink link) {
        StringBuffer deviceNote = new StringBuffer("Notes:");
        if (link.getVrf() != null) {
            deviceNote.append(" vrf: ");
            deviceNote.append(link.getVrf());
        }
        if (link.getDeliveryDeviceClientSide() != null) {
            deviceNote.append(" client device: ");
            deviceNote.append(link.getDeliveryDeviceClientSide());
        }
        if (link.getDeliveryDeviceNetworkSide() != null) {
            deviceNote.append(" network device: ");
            deviceNote.append(link.getDeliveryDeviceNetworkSide());
        }

        if (link.getDeliveryCode() != null) {
            deviceNote.append(" delivery Code: ");
            deviceNote.append(link.getDeliveryCode());
        }

        if (link.getOrderCode() != null) {
            deviceNote.append(" order Code: ");
            deviceNote.append(link.getOrderCode());
        }

        return deviceNote.toString();
    }


    private void log(String hostname, String description) {
        final JobLogEntry jloe = new JobLogEntry();
        jloe.setHostname(hostname);
        jloe.setIpaddr("NA");
        jloe.setOrderCode("NA");
        jloe.setDescription(description);
    }

    private boolean checkduplicatedhostname(String hostname) {
        if (m_onmsDuplicatedForeignId.contains(hostname)) {
            for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.get(hostname)) {
                log(device,FAST_DUPLICATED_ONMS_ID);
            }
            return true;
        }
        return false;
    }

    private boolean checkduplicatedipaddress(String hostname) {
        boolean duplicated=false;
        for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.get(hostname)) {
            if (m_onmsDuplicatedIpAddress.contains(device.getIpaddr())) {
                log(device,FAST_DUPLICATED_ONMS_IP);
                duplicated = true;
            }
        }
        return duplicated;
    }

    private void mismatch(String hostname, Set<String> foreignIds) {
        for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.get(hostname)) {
            log(device,FAST_MISHMATCH_ONMS);
        }


    }

    private void norefdevice(String hostname) {
        if (m_fastHostnameServiceDeviceMap.containsKey(hostname)) {
            for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.get(hostname)) {
                log(device,FAST_NO_REF_DEVICE);
            }
        } else {
            log(hostname,FAST_NO_DEVICE);
        }
    }

    private void add(String hostname) {
        Set<String> secondary = new HashSet<String>();
        FastServiceDevice refdevice = null;
        for (FastServiceDevice device : m_fastHostnameServiceDeviceMap.get(hostname)) {
            secondary.add(device.getIpaddr());
            if (!m_fastOrderCodeServiceLinkMap.containsKey(device.getOrderCode()))
                continue;
            if (refdevice == null) {
                refdevice = device;
            } else if (device.isMaster() && !refdevice.isMaster()) {
                refdevice = device;
            } else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
                refdevice = device;
            } else if (device.getIpAddrLan() != null
                    && device.getIpAddrLan() == null) {
                refdevice = device;
            }
            log(device,ONMS_ADDED_DEVICE);
        }

        if (refdevice == null) {
            norefdevice(hostname);
            return;
        }

        FastServiceLink reflink = m_fastOrderCodeServiceLinkMap.get(refdevice.getOrderCode());
        TrentinoNetworkNode rnode = new TrentinoNetworkNode(hostname,
                                                            m_vrf.get(reflink.getVrf()),
                                                            DashBoardUtils.TN_REQU_NAME);
        rnode.setHostname(hostname);
        rnode.setForeignId(hostname);
        rnode = getnode(refdevice, reflink, rnode, secondary);

        getService().add(rnode);
        m_updates.add(rnode);
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

        log(rnode,ONMS_UPDATED_NO_FAST_DEVICE);

    }

    private TrentinoNetworkNode getnode(FastServiceDevice refdevice,
            FastServiceLink reflink, TrentinoNetworkNode rnode,
            Set<String> ipaddresses) {
        rnode.setDescr(DashBoardUtils.DESCR_FAST);
        rnode.setVrf(m_vrf.get(reflink.getVrf()).getDnsdomain());
        rnode.setPrimary(refdevice.getIpaddr());
        rnode.setBackupProfile(refdevice.getBackupprofile());
        rnode.setSnmpProfile(refdevice.getSnmpprofile());
        rnode.setNetworkCategory(m_vrf.get(reflink.getVrf()));
        if (refdevice.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify)) {
            rnode.setNotifCategory(m_vrf.get(reflink.getVrf()).getNotifylevel());
        } else {
            rnode.setNotifCategory(refdevice.getNotifyCategory());
        }
        rnode.setThreshCategory(m_vrf.get(reflink.getVrf()).getThresholdlevel());
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
        rnode.setCircuitId(reflink.getDeliveryCode());
        rnode.setBuilding(refdevice.getIstat() + "-" + reflink.getSiteCode());

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
            List<FastServiceDevice> fastdevices) {
        Set<String> ipaddresses = new HashSet<String>();
        FastServiceDevice refdevice = null;
        for (FastServiceDevice device : fastdevices) {
            ipaddresses.add(device.getIpaddr());
            if (!m_fastOrderCodeServiceLinkMap.containsKey(device.getOrderCode()))
                continue;
            if (refdevice == null) {
                refdevice = device;
            } else if (rnode.getInterface(device.getIpaddr()) != null
                    && rnode.getInterface(device.getIpaddr()).getOnmsprimary() == OnmsPrimary.P) {
                refdevice = device;
            } else if (device.isMaster() && !refdevice.isMaster()) {
                refdevice = device;
            } else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
                refdevice = device;
            } else if (device.getIpAddrLan() != null
                    && device.getIpAddrLan() == null) {
                refdevice = device;
            }
        }

        if (refdevice == null) {
            norefdevice(rnode.getHostname());
            return;
        }
        rnode = getnode(refdevice,
                        m_fastOrderCodeServiceLinkMap.get(refdevice.getOrderCode()),
                        rnode, ipaddresses);

        if (rnode.getOnmstate() == OnmsState.NONE)
            return;

        getService().update(rnode);
        m_updates.add(rnode);
        updateSnmp(refdevice);
        log(refdevice,ONMS_UPDATED_FAST_DEVICE);

    }

    private boolean updateSnmp(FastServiceDevice refdevice) {
        String snmpprofile = refdevice.getSnmpprofile();
        try {
            if (getService().saveSnmpProfile(refdevice.getIpaddr(),
                                             snmpprofile)) {
                log(refdevice,ONMS_UPDATE_SNMP_PROFILE+ ": " + snmpprofile);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING,
                       "FAST sync: cannot set snmp profile: " + snmpprofile,
                       e);
        }
        return false;

    }

    private boolean isDeviceInFast(String foreignId) {
        if (m_fastHostnameServiceDeviceMap.containsKey(foreignId)) {
            return true;
        }
        for (String hostname : m_fastHostnameServiceDeviceMap.keySet()) {
            if (m_onmsForeignIdRequisitionNodeMap.get(foreignId).getNodeLabel().startsWith(hostname)) {
                return true;
            }
        }
        return false;
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
