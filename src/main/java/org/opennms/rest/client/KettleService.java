package org.opennms.rest.client;

import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;



public interface KettleService {


    public KettleRunJob runJob();

    public KettleJobStatus jobStatus(KettleRunJob runjob);

}
