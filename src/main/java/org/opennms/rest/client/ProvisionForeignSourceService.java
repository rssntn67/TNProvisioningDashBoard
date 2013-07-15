package org.opennms.rest.client;


import org.opennms.netmgt.provision.persist.foreignsource.DetectorCollection;
import org.opennms.netmgt.provision.persist.foreignsource.DetectorWrapper;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;

public interface ProvisionForeignSourceService extends RestFilterService{


    public ForeignSourceCollection getAll();

    public int count();

    public ForeignSourceCollection getAllDefault();

    public int countDeployed();

    public ForeignSourceCollection getAllDeployed();


    public ForeignSource get(String foreignSource);    

    public DetectorCollection getDetectors(String name);
    
    public DetectorWrapper getDetector(String name, String detectorname);
    
    public PolicyCollection getpolicies(String name);

    public PolicyWrapper getPolicy(String name, String policyname);
        
    public void add(ForeignSource foreignSource);
    
    public void addOrReplace(String name, DetectorWrapper detector);
    
    public void addOrReplace(String name, PolicyWrapper policy);
    
    public void update(ForeignSource foreignSource);
        
    public void deleteForeignSource(String foreignSource);
    
    public void deleteDetector(String name, String detectorname);
    
    public void deletePolicy(String name, String policyname);
       
}
