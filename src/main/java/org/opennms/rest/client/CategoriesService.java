package org.opennms.rest.client;


import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;

public interface CategoriesService extends RestFilterService{
    
    public OnmsCategoryCollection getAll();
    
    public OnmsCategory getCategory(String categoryName);
    
    public void delete(String categoryName);
}
