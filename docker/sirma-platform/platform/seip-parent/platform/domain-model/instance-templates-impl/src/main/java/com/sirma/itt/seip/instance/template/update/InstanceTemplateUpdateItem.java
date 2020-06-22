package com.sirma.itt.seip.instance.template.update;

/**
 * Represents the result of merging and instance view with a template view.
 *
 * @author Adrian Mitev
 */
public class InstanceTemplateUpdateItem {

	private final String instanceId;

	private final String mergedContent;

	/**
	 * Initializes class properties.
	 *
	 * @param instanceId
	 *            id of the instance that is merged.
	 * @param mergedContent
	 *            merged content.
	 */
	public InstanceTemplateUpdateItem(String instanceId, String mergedContent) {
		this.instanceId = instanceId;
		this.mergedContent = mergedContent;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getMergedContent() {
		return mergedContent;
	}

}
