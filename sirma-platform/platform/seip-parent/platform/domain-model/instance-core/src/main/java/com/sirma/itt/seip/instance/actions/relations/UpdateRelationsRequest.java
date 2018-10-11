package com.sirma.itt.seip.instance.actions.relations;

import com.sirma.itt.seip.instance.actions.ActionRequest;

import java.util.Collections;
import java.util.Set;

/**
 * Request object for updating (add/remove) relations from an instance.
 *
 * @author Boyan Tonchev.
 */
public class UpdateRelationsRequest extends ActionRequest {

    private static final long serialVersionUID = -8294876237268019644L;

    protected static final String OPERATION_NAME = "updateRelations";

    private Set<UpdateRelationData> linksToBeAdded = Collections.emptySet();

    private Set<UpdateRelationData> linksToBeRemoved = Collections.emptySet();

    /**
     * Initializes a {@link UpdateRelationsRequest} object.
     *
     * @param instanceId
     *         - id of instance which relations will be updated.
     */
    public UpdateRelationsRequest(String instanceId) {
        super.setTargetId(instanceId);
    }

    @Override
    public String getOperation() {
        return OPERATION_NAME;
    }

    /**
     * Getter for list with {@link UpdateRelationData} objects that holds needed information
     * about links which have to be added to {@link UpdateRelationsRequest#targetId}.
     *
     * @return list with {@link UpdateRelationData} objects.
     */
    public Set<UpdateRelationData> getLinksToBeAdded() {
        return linksToBeAdded;
    }

    /**
     * Setter for list with {@link UpdateRelationData} objects that holds needed information
     * about links which have to be added to {@link UpdateRelationsRequest#targetId}.
     *
     * @param linksToBeAdded
     *         - the list with {@link UpdateRelationData} objects.
     */
    public void setLinksToBeAdded(Set<UpdateRelationData> linksToBeAdded) {
        this.linksToBeAdded = linksToBeAdded;
    }

    /**
     * Getter for list with {@link UpdateRelationData} objects that holds needed information
     * about links which have to be remove from {@link UpdateRelationsRequest#targetId}.
     *
     * @return list with {@link UpdateRelationData} objects.
     */
    public Set<UpdateRelationData> getLinksToBeRemoved() {
        return linksToBeRemoved;
    }

    /**
     * Setter for list with {@link UpdateRelationData} objects that holds needed information
     * about links which have to be removed from {@link UpdateRelationsRequest#targetId}.
     *
     * @param linksToBeRemoved
     *         - the list with {@link UpdateRelationData} objects.
     */
    public void setLinksToBeRemoved(Set<UpdateRelationData> linksToBeRemoved) {
        this.linksToBeRemoved = linksToBeRemoved;
    }
}
