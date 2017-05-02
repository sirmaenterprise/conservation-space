package com.sirma.itt.seip.template.rest;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Rest service for templates.
 *
 * @author yasko
 */
@Path(TemplateResource.TEMPLATES_PATH)
@Produces(Versions.V2_JSON)
public class TemplateResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String TEMPLATES_PATH = "/templates";

	@Inject
	private SecurityContextManager securityManager;

	@Inject
	private TemplateService templateService;


	/**
	 * Retrieve template by it's identifier.
	 *
	 * @param id
	 *            Identifier for this template. It could be the primary id for the template or it's filename.
	 * @return The template matching the identifier.
	 */
	@GET
	@Path("/{id}")
	public TemplateInstance find(@PathParam("id") String id) {
		return templateService.getTemplate(convertIdentifier(id));
	}

	/**
	 * Retrieve the content of a template or the default template content if one is not found.
	 *
	 * @param id
	 *            Identifier for this template. It could be the primary id for the template or it's filename.
	 * @return The matched template content.
	 */
	@GET
	@Path("/{id}/content")
	@Produces(MediaType.TEXT_HTML)
	public String findContent(@PathParam("id") String id) {
		TemplateInstance template = templateService.getTemplateWithContent(convertIdentifier(id));
		if (template == null) {
			return templateService.getDefaultTemplateContent();
		}
		return template.getContent();
	}

	/**
	 * Deletes a template.
	 *
	 * @param id
	 *            Identifier for this template. It could be the primary id for the template or it's filename.
	 */
	@DELETE
	@Path("/{id}")
	@Transactional(TxType.REQUIRED)
	public void delete(@PathParam("id") String id) {
		templateService.delete(convertIdentifier(id));
	}

	/**
	 * Load templates for group identifiers. The group identifier corresponds to the definition id for which a template
	 * is for.
	 *
	 * @param groupIds
	 *            List of group identifiers.
	 * @param purpose
	 *            The template purpose. Possible purposes are defined within {@link TemplatePurposes}
	 * @return A list containing all templates for the provided identifiers.
	 */
	@GET
	public List<TemplateInstance> findAll(@QueryParam("group-id") List<String> groupIds,
			@DefaultValue(TemplatePurposes.CREATABLE) @QueryParam(TemplateProperties.PURPOSE) String purpose) {
		if (CollectionUtils.isEmpty(groupIds)) {
			// XXX: maybe return ~everything with some pagination info
			return Collections.emptyList();
		}

		BiFunction<String, String, List<TemplateInstance>> wrap = securityManager
				.wrap()
					.biFunction(templateService::getTemplates);
		List<TemplateInstance> resultTemplates = groupIds
				.stream()
					.map(groupId -> wrap.apply(groupId, purpose))
					.flatMap(templates -> templates.stream())
					.collect(Collectors.toCollection(LinkedList::new));
		resultTemplates.add(templateService.getDefaultTemplate());
		return resultTemplates;
	}

	/**
	 * Creates a new template as an instance in the system. In order to be applicable for its forType, the template has
	 * to be activated.
	 * 
	 * @see TemplateResource#activate(TemplateInstance)
	 * 
	 * @param template
	 *            is the template data to be used for creating
	 * @return the id of the newly created instance
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	@Transactional(TxType.REQUIRED)
	public String create(TemplateInstance template) {
		return templateService.create(template);
	}

	private static Serializable convertIdentifier(String id) {
		try {
			return Long.valueOf(id);
		} catch (NumberFormatException e) { // NOSONAR
			return id;
		}
	}
}
