/**
 * Copyright (c) 2013 09.09.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.template;

import java.util.List;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Manages operations on templates. The service classifies templates in groups, as each group is a composite of the
 * template's forType, purpose and the template rule (if any). </br>
 * In order for a template to become applicable (available) for its group, it has to be activated first. </br>
 * Each template group has a primary template. User could set any of the templates in the group as primary. The active
 * primary template could be only one and if a new one is activated, the old primary is demoted to secondary template in
 * the same group.
 *
 * @author Adrian Mitev
 * @author Vilizar Tsonev
 * @author BBonev
 */
public interface TemplateService {

	/**
	 * Used as id of the default(blank) template. It should be with prefix so that it comply with URI standard, because
	 * it is used as marker that the blank template was selected. It is send from the client as instance property and it
	 * is validated as URI as it is defined as object property.
	 */
	String DEFAULT_TEMPLATE_ID = "emf:defaultTemplate";

	/**
	 * Gets templates according to the passed type, purpose and criteria. The retrieval is executed on two steps: first,
	 * all templates matching the documentType and purpose are retrieved, and then filtering is applied on them,
	 * according to the filter map passed in the {@link TemplateSearchCriteria}. </br>
	 * The primary template is returned first in the resulting list.</br>
	 * The default template is automatically added at the bottom of the list.
	 *
	 * @param criteria
	 *            the {@link TemplateSearchCriteria} containing the arguments to search and filter by
	 * @return the found, filtered templates
	 */
	List<Template> getTemplates(TemplateSearchCriteria criteria);

	/**
	 * Returns the first template matching the provided {@link TemplateSearchCriteria} including its content.
	 *
	 * @param criteria
	 *            the {@link TemplateSearchCriteria} containing the arguments to search and filter by.
	 * @return the found template including its content.
	 */
	Template getTemplate(TemplateSearchCriteria criteria);

	/**
	 * Returns the template by given template id, or by corresponding instance id. </br>
	 * The template <b>content is not returned</b>. If needed, it can be retrieved via</br>
	 * {@link TemplateService#getContent(String)}
	 *
	 * @param instanceId template id or corresponding instance id
	 * @return the found template
	 */
	Template getTemplate(String instanceId);

	/**
	 * Loads the content for the given template instance.
	 *
	 * @param id
	 *            is the identifier of the template, or the corresponding instance id. Works with both.
	 * @return template content, or null if not found.
	 */
	String getContent(String id);

	/**
	 * Gets all activated templates. Their contents are not included and have to be retrieved separately.
	 * 
	 * @return all activated templates available in the system
	 */
	List<Template> getAllTemplates();

	/**
	 * Sets the given template as primary. If there is an existing active primary template for that group, it is
	 * automatically demoted (reset) to secondary.
	 *
	 * @param templateInstanceId
	 *            the ID (URI) of the template instance
	 */
	void setAsPrimaryTemplate(String templateInstanceId);

	/**
	 * Activates the template. When activated, the template becomes available for its forType, and it can be applied.
	 * </br>
	 * If there is an existing active primary template for that group, it is automatically demoted (reset) to secondary.
	 *
	 * @param templateInstanceId
	 *            is the ID of the template instance that will be activated
	 * @return the activated template instance
	 */
	Instance activate(String templateInstanceId);

	/**
	 * Deactivates the template. Its status becomes Inactive and it is not available to be applied to its group anymore.
	 *
	 * @param templateInstanceId
	 *            is the template id to deactivate
	 */
	void deactivate(String templateInstanceId);

	/**
	 * Activates the template. When activated, the template becomes available for its forType, and it can be applied.
	 *
	 * @param templateInstanceId
	 *            is the ID of the template instance that will be activated
	 * @param controlPrimaryFlag
	 *            indicates if the system should automatically manage the primary flag(s) when activating the template.
	 *            This means that if there are no templates for that type/purpose/rule the newly activated will be
	 *            automatically set as primary. Also, if there is existing primary template for the type/purpose/rule,
	 *            it will be demoted (reset) to secondary.
	 * @return the activated template instance
	 */
	Instance activate(String templateInstanceId, boolean controlPrimaryFlag);

	/**
	 * Creates a new template instance. In order to be applicable for its forType, the template has to be activated via
	 * {@link TemplateService#activate(Template, boolean)}
	 *
	 * @param template
	 *            the template to create
	 * @param view
	 *            view of the template
	 * @return the ID of the newly created template instance
	 */
	String create(Template template, String view);

}
