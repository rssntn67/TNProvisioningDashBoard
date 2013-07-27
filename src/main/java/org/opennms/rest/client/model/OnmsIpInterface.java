
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.rest.client.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsIpInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "ipInterface")
@Entity
@Table(name="ipInterface")
public class OnmsIpInterface implements Serializable {
    
    @Embeddable
    public static class PrimaryType implements Comparable<PrimaryType>, Serializable {
        private static final long serialVersionUID = -647348487361201657L;
        private static final char[] s_order = { 'N', 'S', 'P' };
        private char m_collType;

        private PrimaryType() {
            this('N');
        }

        private PrimaryType(char collType) {
            m_collType = collType;
        }

        @Column(name="isSnmpPrimary")
        public char getCharCode() {
            return m_collType;
        }

        public void setCharCode(char collType) {
            m_collType = collType;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof PrimaryType) {
                return this.compareTo((PrimaryType)o) == 0;
            } else return false;
        }

        public int compareTo(PrimaryType collType) {
            return getIndex(m_collType) - getIndex(collType.m_collType);
        }

        private static int getIndex(char code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal collType code '"+code+"'");
        }

        @Override
        public String toString() {
            return String.valueOf(m_collType);
        }

        public boolean isLessThan(PrimaryType collType) {
            return compareTo(collType) < 0;
        }

        public boolean isGreaterThan(PrimaryType collType) {
            return compareTo(collType) > 0;
        }

        public PrimaryType max(PrimaryType collType) {
            return this.isLessThan(collType) ? collType : this;
        }

        public PrimaryType min(PrimaryType collType) {
            return this.isLessThan(collType) ? this : collType;
        }

        public static PrimaryType get(char code) {
            switch (code) {
            case 'P': return PRIMARY;
            case 'S': return SECONDARY;
            case 'N': return NOT_ELIGIBLE;
            default:
                throw new IllegalArgumentException("Cannot create collType from code "+code);
            }
        }

        public static PrimaryType get(String code) {
            if (code == null) {
                return NOT_ELIGIBLE;
            }
            code = code.trim();
            if (code.length() < 1) {
                return NOT_ELIGIBLE;
            } else if (code.length() > 1) {
                throw new IllegalArgumentException("Cannot convert string "+code+" to a collType");
            } else {
                return get(code.charAt(0));
            }
        }

        public static final PrimaryType PRIMARY = new PrimaryType('P');
        public static final PrimaryType SECONDARY = new PrimaryType('S');
        public static final PrimaryType NOT_ELIGIBLE = new PrimaryType('N');
    }

    private static final long serialVersionUID = 7750043250236397014L;

    private Integer m_id;

    private String m_ipAddress;

    private String m_ipHostName;

    private String m_isManaged;

    private PrimaryType m_isSnmpPrimary = PrimaryType.NOT_ELIGIBLE;

    private Date m_ipLastCapsdPoll;

    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_monitoredServices = new HashSet<OnmsMonitoredService>();

    private OnmsSnmpInterface m_snmpInterface;

    private Integer m_ifIndex;

    /**
     * <p>Constructor for OnmsIpInterface.</p>
     */
    public OnmsIpInterface() {
    }

    /**
     * minimal constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsIpInterface(String ipAddr, OnmsNode node) {
        m_ipAddress = ipAddr;
        m_node = node;
    }

    /**
     * Unique identifier for ipInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
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
        return getId().toString();
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
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipAddr", length=16)
    @XmlElement(name="ipAddress")
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipaddr) {
        m_ipAddress = ipaddr;
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
        if (m_snmpInterface != null) {
            return m_snmpInterface.getIfIndex();
        }
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifindex) {
        if (m_snmpInterface != null) {
            m_snmpInterface.setIfIndex(ifindex);
        }
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
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface.PrimaryType} object.
     */
    @Column(name="isSnmpPrimary", length=1)
    @XmlTransient
    public PrimaryType getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    /**
     * <p>setIsSnmpPrimary</p>
     *
     * @param issnmpprimary a {@link org.opennms.netmgt.model.OnmsIpInterface.PrimaryType} object.
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
    @XmlIDREF
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * The services on this node
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlTransient
    @OneToMany(mappedBy="ipInterface")
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
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
    public String toString() {
        return new ToStringCreator(this)
        .append("ipaddr", getIpAddress())
        .append("ifindex", getIfIndex())
        .append("iphostname", getIpHostName())
        .append("ismanaged", getIsManaged())
        .append("iplastcapsdpoll", getIpLastCapsdPoll())
        .append("issnmpprimary", getIsSnmpPrimary())
        .toString();
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Transient
    public InetAddress getInetAddress() {
        String ipAddr = getIpAddress();
        if (ipAddr == null) {
            return null;
        }

        InetAddress addr = null;
        try {
            String hostName = getIpHostName() == null ? ipAddr : getIpHostName();
            addr = InetAddress.getByName(ipAddr);
            addr = InetAddress.getByAddress(hostName, addr.getAddress());
        } catch (UnknownHostException e) {
            // this can't happen here
        }
        return addr;
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
    public OnmsMonitoredService getMonitoredServiceByServiceType(String svcName) {
        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            if (monSvc.getServiceType().getName().equals(svcName)) {
                return monSvc;
            }
        }
        return null;
    }

}
