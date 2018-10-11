package com.sirma.sep.instance.template;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.template.Template;

/**
 * Defines operations for working with templates of existing instances.
 *
 * @author Adrian Mitev
 */
public interface InstanceTemplateService {

	/**
	 * <pre>
	 * Starts the a batch jobs that updates the views of existing instances created using a particular template with the
	 * newest version of the template. The merge is performed according to the following rules:
	 * - Locked tab (either in the template or in the instance) is replaced with the same tab in the template
	 * - Non-locked tabs are not touched
	 * - New tabs in the instance are append after the tabs from the template
	 * - User-defined tabs are not touched
	 * </pre>
	 *
	 * @param templateInstanceId
	 *            id of the template used linked to instances (via using hasTemplate relationship) that will be updated.
	 */
	void updateInstanceViews(String templateInstanceId);

	/**
	 * <pre>
	 * Updates the view of existing instance created using a particular template with the newest version of the
	 * template. The merge is performed according to the following rules:
	 * - Locked tab (either in the template or in the instance) is replaced with the same tab in the template
	 * - Non-locked tabs are not touched
	 * - New tabs in the instance are append after the tabs from the template
	 * - User-defined tabs are not touched
	 * </pre>
	 *
	 * @param instanceId
	 *            id of the instance to update
	 * @return updated instance
	 */
	Instance updateInstanceView(String instanceId);

	/**
	 * Creates a template using another instance as a source.
	 *
	 * @param templateData
	 *            template properties
	 * @param sourceId
	 *            id of the source instance.
	 * @return id of the created template.
	 */
	String createTemplate(Template templateData, String sourceId);

	/**
	 * Gets the template version applied to the instance.
	 *
	 * @param instanceId
	 *            id of the instance
	 * @return version of template applied
	 */
	String getInstanceTemplateVersion(String instanceId);
}
