package com.sirma.itt.seip.instance.actions.clone;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Data object which is used to hold the information needed in order to execute the clone action. This information is
 * extracted from the http contents.
 * <p>
 * Created by Ivo Rusev on 9.12.2016 г.
 */
public class InstanceCloneRequest extends ActionRequest {

    private static final long serialVersionUID = 892180039899968913L;

    protected static final String OPERATION_NAME = "clone";

    private Instance clonedInstance;

    @Override
    public String getOperation() {
        return OPERATION_NAME;
    }

    public Instance getClonedInstance() {
        return clonedInstance;
    }

    public void setClonedInstance(Instance clonedInstance) {
        this.clonedInstance = clonedInstance;
    }
}
