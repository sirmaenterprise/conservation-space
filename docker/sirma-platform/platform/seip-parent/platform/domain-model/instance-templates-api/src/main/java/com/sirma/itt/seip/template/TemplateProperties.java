package com.sirma.itt.seip.template;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Default properties for template instances.
 *
 * @author BBonev
 */
public interface TemplateProperties extends DefaultProperties {

	String TEMPLATE_DEFINITION_ID = "template";

	String TEMPLATE_CLASS_ID = "emf:Template";

	String ID = "id";

	/** The template. */
	String TEMPLATE_TYPE = "template";

	/** The group id to witch a template belongs. */
	String GROUP_ID = "groupId";

	/** The flag if the template is primary. */
	String PRIMARY = "primary";

	/** The flag if the template is public. */
	String PUBLIC = "public";

	String TYPE_ATTRIBUTE = "type";

	/** The default group name. */
	String DEFAULT_GROUP = "default_group";

	/**
	 * The instance which content will be used for the template. Usually that's the instance from which 'save as
	 * template' was invoked
	 */
	String SOURCE_INSTANCE = "sourceInstance";

	/**
	 * The content digest. Hash of the content used to detect changes in the content without loading one of the files
	 */
	String CONTENT_DIGEST = "contentDigest";

	/**
	 * The template purpose (for example: creatable or uploadable). It is different than the purpose defined in
	 * {@link DefaultProperties}, used in solr.
	 **/
	String PURPOSE = "purpose";

	String CORRESPONDING_INSTANCE = "correspondinginstance";

	/**
	 * Version of the corresponding instance when publishing the template.
	 */
	String PUBLISHED_INSTANCE_VERSION = "publishedInstanceVersion";

	String TEMPLATE_RULE = "templateRule";

	String TEMPLATE_RULE_DESCRIPTION = "templateRuleDescription";

	/** The object type for which the template is applicable. */
	String FOR_TYPE = "forType";

	/** The is content loaded. */
	String IS_CONTENT_LOADED = "isContentLoaded";

	/** The definition property indicating if the template is primary **/
	String IS_PRIMARY_TEMPLATE = "isPrimaryTemplate";

	/** The definition property indicating the object type for which the template is applicable. */
	String FOR_OBJECT_TYPE = "forObjectType";

	// Materializes the label of forObjectType
	String FOR_OBJECT_TYPE_LABEL = "forObjectTypeLabel";

	/** The definition property indicating the template purpose (creatable/uploadable) **/
	String TEMPLATE_PURPOSE = "templatePurpose";

	/** Semantic properties for the template **/
	String EMF_FOR_OBJECT_TYPE = "emf:forObjectType";

	String EMF_TEMPLATE_PURPOSE = "emf:templatePurpose";

	String EMF_PRIMARY_TEMPLATE = "emf:isPrimaryTemplate";
}
