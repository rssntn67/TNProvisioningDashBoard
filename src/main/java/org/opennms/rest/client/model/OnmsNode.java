//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 10: Organized imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.rest.client.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.core.style.ToStringCreator;


/**
 * Contains information on nodes discovered and potentially managed by OpenNMS.
 * sys* properties map to SNMP MIB 2 system table information.
 *
 * @hibernate.class table="node"
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="node")
@Entity()
@Table(name="node")
@SecondaryTable(name="pathOutage")
public class OnmsNode implements Serializable,
        Comparable<OnmsNode> {

    private static final long serialVersionUID = -5736397583719151493L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private Date m_createTime = new Date();

    /** nullable persistent field */
    private OnmsNode m_parent;

    /** nullable persistent field */
    private String m_type;

    /** nullable persistent field */
    private String m_sysObjectId;

    /** nullable persistent field */
    private String m_sysName;

    /** nullable persistent field */
    private String m_sysDescription;

    /** nullable persistent field */
    private String m_sysLocation;

    /** nullable persistent field */
    private String m_sysContact;

    /** nullable persistent field */
    private String m_label;

    /** nullable persistent field */
    private String m_labelSource;

    /** nullable persistent field */
    private String m_netBiosName;

    /** nullable persistent field */
    private String m_netBiosDomain;

    /** nullable persistent field */
    private String m_operatingSystem;

    /** nullable persistent field */
    private Date m_lastCapsdPoll;
    
    private String m_foreignSource;
    
    private String m_foreignId;

    /** persistent field */
    private OnmsDistPoller m_distPoller;

    /** persistent field */
    private OnmsAssetRecord m_assetRecord;

    private Set<OnmsCategory> m_categories = new LinkedHashSet<OnmsCategory>();

    /**
     * <p>Constructor for OnmsNode.</p>
     */
    public OnmsNode() {
        this(null);
    }

    /**
     * <p>Constructor for OnmsNode.</p>
     *
     * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    public OnmsNode(OnmsDistPoller distPoller) {
        m_distPoller = distPoller;
        m_assetRecord = new OnmsAssetRecord();
        m_assetRecord.setNode(this);
    }

    /**
     * Unique identifier for node.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="nodeId", nullable=false)
    @SequenceGenerator(name="nodeSequence", sequenceName="nodeNxtId")
    @GeneratedValue(generator="nodeSequence")
    @XmlTransient
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlID
    @XmlAttribute(name="id", required=true)
    @Transient
    public String getNodeId() {
    	if (getId() != null) {
    		return getId().toString();
    	}
    	return null;
    }

    /**
     * <p>setId</p>
     *
     * @param nodeid a {@link java.lang.Integer} object.
     */
    public void setId(Integer nodeid) {
        m_id = nodeid;
    }

    /**
     * <p>setNodeId</p>
     *
     * @param nodeid a {@link java.lang.String} object.
     */
    public void setNodeId(String nodeid) {
        setId(Integer.valueOf(nodeid));
    }

    /**
     * Time node was added to the database.
     *
     * @hibernate.property column="nodecreatetime" length="8" not-null="true"
     * @return a {@link java.util.Date} object.
     */
    @XmlElement(name="createTime")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="nodeCreateTime", nullable=false)
    public Date getCreateTime() {
        return m_createTime;
    }

    /**
     * <p>setCreateTime</p>
     *
     * @param nodecreatetime a {@link java.util.Date} object.
     */
    public void setCreateTime(Date nodecreatetime) {
        m_createTime = nodecreatetime;
    }

    /**
     * In the case that the node is virtual or an independent device in a chassis
     * that should be reflected as a subcomponent or "child", this field reflects
     * the nodeID of the chassis/physical node/"parent" device.
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeParentID")
    public OnmsNode getParent() {
        return m_parent;
    }

    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setParent(OnmsNode parent) {
        m_parent = parent;
    }

    /**
     * Flag indicating status of node
     * - 'A' - active
     * - 'D' - deleted
     *
     * TODO: Eventually this will be deprecated and deleted nodes will actually be deleted.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="type")
    @Column(name="nodeType", length=1)
    public String getType() {
        return m_type;
    }

    /**
     * <p>setType</p>
     *
     * @param nodetype a {@link java.lang.String} object.
     */
    public void setType(String nodetype) {
        m_type = nodetype;
    }

    /**
     * SNMP MIB-2 system.sysObjectID.0
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="sysObjectId")
    @Column(name="nodeSysOID", length=256)
    public String getSysObjectId() {
        return m_sysObjectId;
    }

    /**
     * <p>setSysObjectId</p>
     *
     * @param nodesysoid a {@link java.lang.String} object.
     */
    public void setSysObjectId(String nodesysoid) {
        m_sysObjectId = nodesysoid;
    }

    /**
     * SNMP MIB-2 system.sysName.0
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="sysName")
    @Column(name="nodeSysName", length=256)
    public String getSysName() {
        return m_sysName;
    }

    /**
     * <p>setSysName</p>
     *
     * @param nodesysname a {@link java.lang.String} object.
     */
    public void setSysName(String nodesysname) {
        m_sysName = nodesysname;
    }

    /**
     * SNMP MIB-2 system.sysDescr.0
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="sysDescription")
    @Column(name="nodeSysDescription", length=256)
    public String getSysDescription() {
        return m_sysDescription;
    }

    /**
     * <p>setSysDescription</p>
     *
     * @param nodesysdescription a {@link java.lang.String} object.
     */
    public void setSysDescription(String nodesysdescription) {
        m_sysDescription = nodesysdescription;
    }

    /**
     * SNMP MIB-2 system.sysLocation.0
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="sysLocation")
    @Column(name="nodeSysLocation", length=256)
    public String getSysLocation() {
        return m_sysLocation;
    }

    /**
     * <p>setSysLocation</p>
     *
     * @param nodesyslocation a {@link java.lang.String} object.
     */
    public void setSysLocation(String nodesyslocation) {
        m_sysLocation = nodesyslocation;
    }

    /**
     * SNMP MIB-2 system.sysContact.0
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="sysContact")
    @Column(name="nodeSysContact", length=256)
    public String getSysContact() {
        return m_sysContact;
    }

    /**
     * <p>setSysContact</p>
     *
     * @param nodesyscontact a {@link java.lang.String} object.
     */
    public void setSysContact(String nodesyscontact) {
        m_sysContact = nodesyscontact;
    }

    /**
     * User-friendly name associated with the node.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="label")
    @Column(name="nodeLabel", length=256)
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setLabel</p>
     *
     * @param nodelabel a {@link java.lang.String} object.
     */
    public void setLabel(String nodelabel) {
        m_label = nodelabel;
    }

    /**
     * Flag indicating source of nodeLabel
     * - 'U' = user defined
     * - 'H' = IP hostname
     * - 'S' = sysName
     * - 'A' = IP address
     *
     * TODO: change this to an enum
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="labelSource")
    @Column(name="nodeLabelSource", length=1)
    public String getLabelSource() {
        return m_labelSource;
    }

    /**
     * <p>setLabelSource</p>
     *
     * @param nodelabelsource a {@link java.lang.String} object.
     */
    public void setLabelSource(String nodelabelsource) {
        m_labelSource = nodelabelsource;
    }

    /**
     * NetBIOS workstation name associated with the node.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="netBIOSName")
    @Column(name="nodeNetBIOSName", length=16)
    public String getNetBiosName() {
        return m_netBiosName;
    }

    /**
     * <p>setNetBiosName</p>
     *
     * @param nodenetbiosname a {@link java.lang.String} object.
     */
    public void setNetBiosName(String nodenetbiosname) {
        m_netBiosName = nodenetbiosname;
    }

    /**
     * NetBIOS domain name associated with the node.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="netBIOSDomainName")
    @Column(name="nodeDomainName", length=16)
    public String getNetBiosDomain() {
        return m_netBiosDomain;
    }

    /**
     * <p>setNetBiosDomain</p>
     *
     * @param nodedomainname a {@link java.lang.String} object.
     */
    public void setNetBiosDomain(String nodedomainname) {
        m_netBiosDomain = nodedomainname;
    }

    /**
     * Operating system running on the node.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="operatingSystem")
    @Column(name="operatingSystem", length=64)
    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    /**
     * <p>setOperatingSystem</p>
     *
     * @param operatingsystem a {@link java.lang.String} object.
     */
    public void setOperatingSystem(String operatingsystem) {
        m_operatingSystem = operatingsystem;
    }

    /**
     * Date and time of last Capsd scan.
     *
     * @return a {@link java.util.Date} object.
     */
    @XmlElement(name="lastCapsdPoll")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastCapsdPoll")
    public Date getLastCapsdPoll() {
        return m_lastCapsdPoll;
    }

    /**
     * <p>setLastCapsdPoll</p>
     *
     * @param lastcapsdpoll a {@link java.util.Date} object.
     */
    public void setLastCapsdPoll(Date lastcapsdpoll) {
        m_lastCapsdPoll = lastcapsdpoll;
    }
    
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="foreignId")
    @Column(name="foreignId")
    public String getForeignId() {
        return m_foreignId;
    }

    /**
     * <p>setForeignId</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     */
    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="foreignSource")
    @Column(name="foreignSource")
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * <p>setForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }
    
    /**
     * Distributed Poller responsible for this node
     *
     * @return a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="dpName")
    public OnmsDistPoller getDistPoller() {
        return m_distPoller;
    }

    /**
     * <p>setDistPoller</p>
     *
     * @param distpoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    public void setDistPoller(org.opennms.netmgt.model.OnmsDistPoller distpoller) {
        m_distPoller = distpoller;
    }
    
    /**
     * The assert record associated with this node
     *
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @OneToOne(mappedBy="node", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    public OnmsAssetRecord getAssetRecord() {
        return m_assetRecord;
    }

    /**
     * <p>setAssetRecord</p>
     *
     * @param asset a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    public void setAssetRecord(OnmsAssetRecord asset) {
        m_assetRecord = asset;
    }
    
    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlElement(name="categories")
    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
    		name="category_node",
    		joinColumns={@JoinColumn(name="nodeId")},
    		inverseJoinColumns={@JoinColumn(name="categoryId")}
    )
    public Set<OnmsCategory> getCategories() {
        return m_categories;
    }
    
    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.Set} object.
     */
    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }
    
    /**
     * <p>addCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a boolean.
     */
    public boolean addCategory(OnmsCategory category) {
        return getCategories().add(category);
    }
    
    /**
     * <p>removeCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a boolean.
     */
    public boolean removeCategory(OnmsCategory category) {
        return getCategories().remove(category);
    }
    
    /**
     * <p>hasCategory</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasCategory(String categoryName) {
        for(OnmsCategory category : getCategories()) {
            if (category.getName().equals(categoryName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("label", getLabel())
            .toString();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a int.
     */
    public int compareTo(OnmsNode o) {
        return getLabel().compareToIgnoreCase(o.getLabel());
    }
}
