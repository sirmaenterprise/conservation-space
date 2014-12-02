/**
 * Copyright (c) 2013 09.09.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.cmf.services;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.emf.template.TemplateInstance;

/**
 * Manages operations on document templates like saving and retrieving. The service classifies
 * templates in groups identified by unique id. The id in most cases will be the type of the
 * document for witch is the template for. The template could be or private. The public templates
 * are visible by all users for the given template group id. The private templates are visible only
 * to the current user for the given the group id. User could change the visibility of a template
 * from private to public.<br>
 * Each template group has a primary template that could be requested if nothing else is specified
 * for the group. User could assign any of the templates in the group as primary. The primary
 * template could be only one, if new one is added the old primary template is demoted to regular
 * template in the same group. When setting a template as primary if the template is private it will
 * be moved to automatically.
 * 
 * @author Adrian Mitev
 * @author BBonev
 */
public interface DocumentTemplateService {

	/**
	 * Loads a template instance and it's content by template id or db id.<br>
	 * NOTE: db id is Long and template id is of String type.
	 * 
	 * @param templateId
	 *            template identifier
	 * @return template instance with loaded content.
	 */
	TemplateInstance getTemplateWithContent(Serializable templateId);

	/**
	 * Loads the content for the given template instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return template instance with content.
	 */
	TemplateInstance loadContent(TemplateInstance instance);

	/**
	 * Gets the all available templates and the private templates for the given user if present. If
	 * the user is <code>null</code> then the method should return only public templates.
	 * 
	 * @param user
	 *            the user DB id or <code>null</code> to fetch only templates.
	 * @return all templates that are visible for the given user.
	 */
	List<TemplateInstance> getAllTemplates(String user);

	/**
	 * Gets the templates by document type that are available for the current user.
	 * 
	 * @param documentType
	 *            the document type
	 * @return the templates
	 */
	List<TemplateInstance> getTemplates(String documentType);

	/**
	 * Gets the primary template for the given document type
	 * 
	 * @param documentType
	 *            the document type
	 * @return the primary template
	 */
	TemplateInstance getPrimaryTemplate(String documentType);

	/**
	 * Sets the given template as primary template for the template group.
	 * 
	 * @param template
	 *            the template
	 * @return true, if successful
	 */
	boolean setAsPrimaryTemplate(TemplateInstance template);

	/**
	 * Gets the template by db id or template id.
	 * 
	 * @param templateId
	 *            the template id
	 * @return the template
	 */
	TemplateInstance getTemplate(Serializable templateId);

	/**
	 * Save template new template or updates existing one.
	 * <p>
	 * If the template does not have a template id one will be generated based on :
	 * <ul>
	 * <li>If the template has property {@link com.sirma.itt.emf.properties.DefaultProperties#TITLE}
	 * set then from the value will be used for new template id by removing all non word characters
	 * and white spaces and all remaining will be converted to lower case.
	 * <li>If title is not present then new template id will be generated based on the UUID
	 * algorithm without the dash character.
	 * </ul>
	 * <p>
	 * If the template instance is new <code>({@link TemplateInstance#getId()} == null)</code> the
	 * method will check for template id uniqueness. If the template id and the container is not
	 * unique an exception {@link com.sirma.itt.cmf.exceptions.DuplicateIdentifierException} will be
	 * thrown.
	 * <p>
	 * If the template instance has a content loaded then the template will be updated in DMS also.
	 * 
	 * @param template
	 *            the template to save/update
	 * @param asPublic
	 *            if <code>true</code> the given template will be saved as and visible to all users
	 *            for the template group.
	 * @return true, if saved successfully
	 */
	boolean save(TemplateInstance template, boolean asPublic);

	/**
	 * Delete template. The argument could be a {@link TemplateInstance} or template id.
	 * 
	 * @param data
	 *            the data could be a {@link TemplateInstance} or template id.
	 * @return true, if deleted successfully
	 */
	boolean delete(Serializable data);

	/**
	 * Starts a template reload sequence.
	 */
	void reload();

}
