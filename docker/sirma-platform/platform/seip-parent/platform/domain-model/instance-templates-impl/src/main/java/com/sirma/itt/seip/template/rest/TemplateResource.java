package com.sirma.itt.seip.template.rest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.instance.template.InstanceTemplateService;

/**
 * Rest service for templates.
 *
 * @author yasko
 */
@Transactional
@Path(TemplateResource.TEMPLATES_PATH)
@Produces(Versions.V2_JSON)
public class TemplateResource {

	public static final String TEMPLATES_PATH = "/templates";

	@Inject
	private TemplateService templateService;

	@Inject
	private InstanceTemplateService instanceTemplateService;

	/**
	 * Retrieve the content of a template or the default template content if one is not found.
	 *
	 * @param templateId
	 *            is the identifier of the template, or the corresponding instance id. Works with both.
	 * @return The matched template content.
	 */
	@GET
	@Path("/{id}/content")
	@Produces(MediaType.TEXT_HTML)
	public String findContent(@PathParam("id") String templateId) {
		if (StringUtils.isNotBlank(templateId)) {
			return templateService.getContent(templateId);
		}
		return null;
	}

	/**
	 * Load templates for group identifiers. The group identifier corresponds to the definition id for which a template
	 * is for.
	 *
	 * @param group
	 *            id for which to load templates.
	 * @param purpose
	 *            The template purpose. Possible purposes are defined within {@link TemplatePurposes}
	 * @return A list containing all templates for the provided identifiers.
	 */
	@GET
	public List<Template> findAll(@QueryParam("group-id") String group,
			@DefaultValue(TemplatePurposes.CREATABLE) @QueryParam(TemplateProperties.PURPOSE) String purpose) {
		if (StringUtils.isEmpty(group)) {
			return Collections.emptyList();
		}

		return templateService.getTemplates(new TemplateSearchCriteria(group, purpose, null));
	}

	/**
	 * Creates a new template as an instance in the system. In order to be applicable for its forType, the template has
	 * to be activated.
	 *
	 * @param request
	 *            is the template data to be used for creating
	 * @return the id of the newly created instance
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	public String create(TemplateCreateRequest request) {
		Objects.requireNonNull(request.getForType(), "'forType' property is required.");
		Objects.requireNonNull(request.getTitle(), "'title' property is required.");
		Objects.requireNonNull(request.isPrimary(), "'Is Primary' property is required.");
		Objects.requireNonNull(request.getPurpose(), "'purpose' property is required.");
		Objects.requireNonNull(request.getSourceInstance(), "'sourceInstance' property is required.");

		Template template = new Template();
		template.setForType(request.getForType());
		template.setTitle(request.getTitle());
		template.setPrimary(request.isPrimary());
		template.setPurpose(request.getPurpose());

		return instanceTemplateService.createTemplate(template, request.getSourceInstance());
	}

	/**
	 * Searches for templates according to the passed criteria.
	 *
	 * @param searchCriteria
	 *            is the {@link TemplateSearchCriteria} containing the search arguments.
	 * @return the found and filtered templates according to the criteria
	 */
	@POST
	@Path("/search")
	@Consumes(Versions.V2_JSON)
	public List<Template> search(TemplateSearchCriteria searchCriteria) {
		return templateService.getTemplates(searchCriteria);
	}
}
