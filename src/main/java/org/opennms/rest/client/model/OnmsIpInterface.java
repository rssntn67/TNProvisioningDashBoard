/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.rest.client.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.NodeIdAdapter;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsIpInterface class.</p>
 */
@XmlRootElement(name = "ipInterface")
@Entity
@Table(name="ipInterface")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsIpInterface implements Serializable {
    private static final long serialVersionUID = 5202941338689399917L;

    private Integer m_id;

    private InetAddress m_ipAddress;

    private Integer m_ifIndex;
    
    private String m_ipHostName;

    private String m_isManaged;

    private PrimaryType m_isSnmpPrimary = PrimaryType.NOT_ELIGIBLE;

    private Date m_ipLastCapsdPoll;

    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_monitoredServices = new LinkedHashSet<OnmsMonitoredService>();

    private OnmsSnmpInterface m_snmpInterface;

    /**
     * <p>Constructor for OnmsIpInterface.</p>
     */
    public OnmsIpInterface() {
    }

    /**
     * Unique identifier for ipInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getInterfaceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getInterfaceId() {
        return getId() == null? null : getId().toString();
    }

    public void setInterfaceId(final String id) {
        setId(Integer.valueOf(id));
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @deprecated
     */
    @Transient
    @XmlTransient
    public String getIpAddressAsString() {
        return InetAddressUtils.toIpAddrString(m_ipAddress);
    }

    //@Column(name="ifIndex")
    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    @XmlAttribute(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifindex) {
        m_ifIndex = ifindex;
    }

    /**
     * <p>getIpHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipHostName", length=256)
    @XmlElement(name="hostName")
    public String getIpHostName() {
        return m_ipHostName;
    }

    /**
     * <p>setIpHostName</p>
     *
     * @param iphostname a {@link java.lang.String} object.
     */
    public void setIpHostName(String iphostname) {
        m_ipHostName = iphostname;
    }

    /**
     * <p>getIsManaged</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="isManaged", length=1)
    @XmlAttribute(name="isManaged")
    public String getIsManaged() {
        return m_isManaged;
    }

    /**
     * <p>setIsManaged</p>
     *
     * @param ismanaged a {@link java.lang.String} object.
     */
    public void setIsManaged(String ismanaged) {
        m_isManaged = ismanaged;
    }

    /**
     * <p>isManaged</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlTransient
    public boolean isManaged() {
        return "M".equals(getIsManaged());
    }

    /**
     * <p>getIpLastCapsdPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ipLastCapsdPoll")
    @XmlElement(name="lastCapsdPoll")
    public Date getIpLastCapsdPoll() {
        return m_ipLastCapsdPoll;
    }

    /**
     * <p>setIpLastCapsdPoll</p>
     *
     * @param iplastcapsdpoll a {@link java.util.Date} object.
     */
    public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
        m_ipLastCapsdPoll = iplastcapsdpoll;
    }

    /**
     * <p>getPrimaryString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlAttribute(name="snmpPrimary")
    public String getPrimaryString() {
        return m_isSnmpPrimary == null? null : m_isSnmpPrimary.toString();
    }
    /**
     * <p>setPrimaryString</p>
     *
     * @param primaryType a {@link java.lang.String} object.
     */
    public void setPrimaryString(String primaryType) {
        m_isSnmpPrimary = new PrimaryType(primaryType.charAt(0));
    }
    
    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    @Column(name="isSnmpPrimary", length=1)
    @XmlTransient
    public PrimaryType getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    /**
     * <p>setIsSnmpPrimary</p>
     *
     * @param issnmpprimary a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    public void setIsSnmpPrimary(PrimaryType issnmpprimary) {
        m_isSnmpPrimary = issnmpprimary;
    }
    
    /**
     * <p>isPrimary</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlTransient
    public boolean isPrimary(){
        return m_isSnmpPrimary.equals(PrimaryType.PRIMARY);
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlElement(name="nodeId")
    //@XmlIDREF
    @XmlJavaTypeAdapter(NodeIdAdapter.class)
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        m_node = node;
    }

    @XmlTransient
    @Transient
    @JsonIgnore
    public Integer getNodeId() {
        if (m_node != null) {
            return m_node.getId();
        }
        return null;
    }

    /**
     * The services on this interface
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlTransient
    @OneToMany(mappedBy="ipInterface",orphanRemoval=true)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    public Set<OnmsMonitoredService> getMonitoredServices() {
        return m_monitoredServices ;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param ifServices a {@link java.util.Set} object.
     */
    public void setMonitoredServices(Set<OnmsMonitoredService> ifServices) {
        m_monitoredServices = ifServices;
    }

    @Transient
    @JsonIgnore
    public void addMonitoredService(final OnmsMonitoredService svc) {
        m_monitoredServices.add(svc);
    }

    public void removeMonitoredService(final OnmsMonitoredService svc) {
        m_monitoredServices.remove(svc);
    }

    /**
     * The SnmpInterface associated with this interface if any
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    @XmlElement(name = "snmpInterface")
    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="snmpInterfaceId")
    public OnmsSnmpInterface getSnmpInterface() {
        return m_snmpInterface;
    }


    /**
     * <p>setSnmpInterface</p>
     *
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void setSnmpInterface(OnmsSnmpInterface snmpInterface) {
        m_snmpInterface = snmpInterface;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("id", m_id)
        .append("ipAddr", InetAddressUtils.str(m_ipAddress))
        .append("ipHostName", m_ipHostName)
        .append("isManaged", m_isManaged)
        .append("isSnmpPrimary", m_isSnmpPrimary)
        .append("ipLastCapsdPoll", m_ipLastCapsdPoll)
        .append("nodeId", getNodeId())
        .toString();
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Column(name="ipAddr")
    @XmlElement(name="ipAddress")
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setInetAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddress(InetAddress ipaddr) {
        m_ipAddress = ipaddr;
    }

    /**
     * <p>isDown</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlAttribute(name="isDown")
    public boolean isDown() {
        boolean down = true;
        for (OnmsMonitoredService svc : m_monitoredServices) {
            if (!svc.isDown()) {
                return !down;
            }
        }
        return down;
    }

    
    @Transient 
    @XmlAttribute
    public int getMonitoredServiceCount () {
    	return m_monitoredServices.size();
    }
    
    /**
     * <p>getMonitoredServiceByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @JsonIgnore
    public OnmsMonitoredService getMonitoredServiceByServiceType(String svcName) {
        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            if (monSvc.getServiceType().getName().equals(svcName)) {
                return monSvc;
            }
        }
        return null;
    }
    
    /**
     * <p>hasNewCollectionTypeValue</p>
     *
     * @param newVal a {@link org.opennms.netmgt.model.PrimaryType} object.
     * @param existingVal a {@link org.opennms.netmgt.model.PrimaryType} object.
     * @return a boolean.
     */
    protected static boolean hasNewCollectionTypeValue(PrimaryType newVal, PrimaryType existingVal) {
        return newVal != null && !newVal.equals(existingVal) && newVal != PrimaryType.NOT_ELIGIBLE;
    }



    @Transient
    @XmlTransient
    @JsonIgnore
    public String getForeignSource() {
        if (getNode() != null) {
            return getNode().getForeignSource();
        }
        return null;
    }

    @Transient
    @XmlTransient
    @JsonIgnore
    public String getForeignId() {
        if (getNode() != null) {
            return getNode().getForeignId();
        }
        return null;
    }

}
