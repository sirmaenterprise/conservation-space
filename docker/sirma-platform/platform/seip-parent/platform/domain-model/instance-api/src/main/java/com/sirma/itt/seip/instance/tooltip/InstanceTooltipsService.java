package com.sirma.itt.seip.instance.tooltip;

/**
 * Handles instance tooltip retrieval.
 *
 * @author nvelkov
 */
public interface InstanceTooltipsService {

	/**
	 * Retrieve the tooltip for the instance with the given id.
	 *
	 * @param instanceId the id of the instance
	 * @return the instance tooltip
	 */
	String getTooltip(String instanceId);

}
