/**
 * Copyright (c) 2014 24.01.2014 , Sirma ITT. /* /**
 */
package com.sirma.cmf.web.template;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import com.sirma.itt.cmf.exceptions.DuplicateIdentifierException;
import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.template.TemplateProperties;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Provides services for document template manipulation.
 * 
 * @author Adrian Mitev
 */
@Stateless
@Path("/document-template")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentTemplateRestService extends EmfRestService {

	@Inject
	private DocumentTemplateService templateService;

	/**
	 * Provides all templates for a given document definition.
	 * 
	 * @param definitionId
	 *            type of the document.
	 * @return template data.
	 */
	@GET
	@Path("/definitions/{definition-id}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getTemplatesForType(@PathParam("definition-id") String definitionId) {
		List<TemplateInstance> templates = templateService.getTemplates(definitionId);
		// find the primary template and put it in first position
		for (ListIterator<TemplateInstance> iterator = templates.listIterator(); iterator.hasNext();) {
			if (iterator.next().getPrimary()) {
				Collections.swap(templates, 0, iterator.previousIndex());
				break;
			}
		}

		JSONArray result = new JSONArray();

		for (TemplateInstance templateInstance : templates) {
			result.put(JsonUtil.transformInstance(templateInstance, DefaultProperties.TYPE,
					DefaultProperties.TITLE));
		}

		return result.toString();
	}

	/**
	 * Provides content and additional metatata of a template.
	 * 
	 * @param templateId
	 *            id of the template to load.
	 * @return json containing the template content.
	 */
	@GET
	@Path("/content/{template-id}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getTemplateContent(@PathParam("template-id") String templateId) {
		TemplateInstance template = null;
		try {
			Long id = Long.valueOf(templateId);
			template = templateService.getTemplateWithContent(id);
		} catch (NumberFormatException e) {
			template = templateService.getTemplateWithContent(templateId);
		}
		return JsonUtil.transformInstance(template, TemplateProperties.TYPE,
				TemplateProperties.TITLE, TemplateProperties.CONTENT).toString();
	}

	/**
	 * Saves a template.
	 * 
	 * @param definitionId
	 *            type of the document for which a template will be fetched.
	 * @param parameters
	 *            post parameters
	 * @return true if template is created or false if fail
	 */
	@POST
	@Path("/{definitionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean saveTemplate(@PathParam("definitionId") String definitionId,
			Map<String, String> parameters) {
		TemplateInstance instance = new TemplateInstance();

		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().putAll(parameters);
		instance.setGroupId(definitionId);
		// property set primary to boolean value
		String primary = parameters.get(TemplateProperties.PRIMARY);
		instance.setPrimary(Boolean.valueOf(primary));

		try {
			templateService.save(instance, true);
		} catch (DuplicateIdentifierException e) {
			log.warn("", e);
			return false;
		}

		return true;
	}
}
