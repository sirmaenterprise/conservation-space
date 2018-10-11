package com.sirma.itt.seip.instance.actions.relations;

import java.io.Serializable;
import java.util.Set;

/**
 * Contains information about link id and instances which links will be update with a instance.
 *
 * @author Boyan Tonchev.
 */
public class UpdateRelationData implements Serializable {

    private static final long serialVersionUID = -6846625938741359319L;
    private String linkId;

    private Set<String> instances;

    /**
     * Initializes a {@link UpdateRelationData} object.
     *
     * @param linkId
     *         - link id.
     * @param instances
     *         - instance ids.
     */
    public UpdateRelationData(String linkId, Set<String> instances) {
        this.linkId = linkId;
        this.instances = instances;
    }

    /**
     * Getter for link id.
     *
     * @return the link id.
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * Getter for list with instance ids.
     *
     * @return the list with instance ids.
     */
    public Set<String> getInstances() {
        return instances;
    }
}
