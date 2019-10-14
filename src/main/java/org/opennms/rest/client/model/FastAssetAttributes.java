package org.opennms.rest.client.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FastAssetAttributes {

    @Override
    public String toString() {
        return "FastAssetAttributes [asset_id=" + asset_id + ", isMaster="
                + isMaster + ", hostName=" + hostName + ", backup=" + backup
                + ", indirizzoIP=" + indirizzoIP + ", profiloSNMP="
                + profiloSNMP + ", profiloBackup=" + profiloBackup
                + ", dominio=" + dominio + "]";
    }
    private Long asset_id;

    @JsonProperty("IsMaster")
    private Integer isMaster;
    
    @JsonProperty("HostName")
    private String hostName;
    
    @JsonProperty("Backup")
    private Integer backup;
    
    @JsonProperty("IndirizzoIP")
    private String indirizzoIP;
    
    @JsonProperty("ProfiloSNMP")
    private String profiloSNMP ;
    
    @JsonProperty("ProfiloBackup")
    private String profiloBackup;

    @JsonProperty("Dominio")
    private String dominio;    

    public Long getAsset_id() {
        return asset_id;
    }
    public void setAsset_id(Long asset_id) {
        this.asset_id = asset_id;
    }
    public Integer getIsMaster() {
        return isMaster;
    }
    public void setIsMaster(Integer isMaster) {
        this.isMaster = isMaster;
    }
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public Integer isBackup() {
        return backup;
    }
    public void setBackup(Integer backup) {
        this.backup = backup;
    }
    public String getIndirizzoIP() {
        return indirizzoIP;
    }
    public void setIndirizzoIP(String indirizzoIP) {
        this.indirizzoIP = indirizzoIP;
    }
    public String getProfiloSNMP() {
        return profiloSNMP;
    }
    public void setProfiloSNMP(String profiloSNMP) {
        this.profiloSNMP = profiloSNMP;
    }
    public String getProfiloBackup() {
        return profiloBackup;
    }
    public void setProfiloBackup(String profiloBackup) {
        this.profiloBackup = profiloBackup;
    }
    public String getDominio() {
        return dominio;
    }
    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    
    
}
