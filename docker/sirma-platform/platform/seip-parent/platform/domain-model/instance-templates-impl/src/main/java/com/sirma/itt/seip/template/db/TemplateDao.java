package com.sirma.itt.seip.template.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;
import com.sirma.itt.seip.template.rules.TemplateRuleUtils;

/**
 * Provides functionality to retrieve, persist and manage templates in the relational DB. </br>
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class TemplateDao {

	@Inject
	private DbDao dbDao;

	@Inject
	private SecurityContext securityContext;

	/**
	 * Saves the template, or updates it if it already exists.
	 *
	 * @param template is the template to save or update
	 */
	public void saveOrUpdate(Template template) {
		Objects.requireNonNull(template, "No template is provided for saving");
		TemplateEntity entity = toEntity(template);

		// The system DB ID is needed for a correct update
		Optional<TemplateEntity> existingEntity = fetchTemplate(template.getId());
		existingEntity.ifPresent(existing -> entity.setId(existing.getId()));

		entity.setModifiedOn(new Date());
		entity.setModifiedBy(getCurrentUser());
		dbDao.saveOrUpdate(entity);
	}

	/**
	 * Deletes the given template.
	 *
	 * @param identifier is the business identifier (templateId)
	 */
	public void delete(String identifier) {
		Optional<TemplateEntity> existing = fetchTemplate(identifier);
		if (!existing.isPresent()) {
			throw new MissingTemplateException(
					"Tried to delete template with id " + identifier + " which does not exist");
		}
		dbDao.delete(TemplateEntity.class, existing.get().getId());
	}

	/**
	 * Gets the template by its business identifier (templateId), or by corresponding instance id (works with both).
	 * </br>
	 * The template <b>content is not returned</b>. To get a template with its content, use the API provided by
	 * {@link TemplateService}
	 *
	 * @param identifier is the template business identifier, or its corresponding instance id
	 * @return the {@link Template}, or null if it doesn't exist
	 */
	public Template getTemplate(String identifier) {
		if (StringUtils.isBlank(identifier)) {
			throw new IllegalArgumentException("Identifier is required to retrieve a template");
		}
		Optional<TemplateEntity> optional = fetchTemplate(identifier);
		if (optional.isPresent()) {
			return toTemplate(optional.get());
		}
		return null;
	}

	/**
	 * Gets all templates that match the given forType and purpose. </br>
	 * The template <b>content is not returned</b>.
	 *
	 * @param forType is the for type
	 * @param purpose is the template purpose. An optional parameter. If not provided, templates will be retrieved
	 *        according to the given forType
	 * @return the matching templates
	 */
	public List<Template> getTemplates(String forType, String purpose) {
		if (StringUtils.isBlank(forType)) {
			throw new IllegalArgumentException("forType is required to retrieve templates");
		}
		String query;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<>(TemplateProperties.GROUP_ID, forType));
		if (StringUtils.isNotBlank(purpose)) {
			query = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY;
			params.add(new Pair<>(TemplateProperties.PURPOSE, purpose));
		} else {
			query = TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_KEY;
		}

		List<TemplateEntity> fetched = dbDao.fetchWithNamed(query, params);
		return fetched
				.stream()
				.map(TemplateDao::toTemplate)
				.collect(Collectors.toList());
	}

	/**
	 * Gets all activated templates available in the relational DB.</br>
	 * Template content is <b>NOT</b> retrieved. Instead, some dummy content is set to prevent failing validations. If
	 * needed, it can be retrieved using {@link TemplateService#getContent(String)}.
	 *
	 * @return all templates available in the system
	 */
	public List<Template> getAllTemplates() {
		List<TemplateEntity> fetched = dbDao.fetchWithNamed(TemplateEntity.QUERY_ALL_TEMPLATES_KEY,
				Collections.emptyList());
		return fetched
				.stream()
				.map(TemplateDao::toTemplate)
				// dummy content is set to prevent failing logical validations. Since the templates have already
				// been persisted, their real contents are valid anyways.
				.peek(template -> template.setContent("dummy content"))
				.collect(Collectors.toList());
	}

	private Optional<TemplateEntity> fetchTemplate(String identifier) {
		String query = TemplateEntity.QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID_KEY;
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>(TemplateProperties.ID, identifier));
		List<TemplateEntity> fetched = dbDao.fetchWithNamed(query, params);

		if (!fetched.isEmpty()) {
			return Optional.of(fetched.get(0));
		}
		return Optional.empty();
	}

	/**
	 * Finds an existing primary, activated template matching the passed type, purpose and rule. Search is performed by
	 * looking up for records in the relational DB.
	 *
	 * @param forType
	 *            the forType to search for
	 * @param purpose
	 *            is the purpose to search for
	 * @param rule
	 *            is an <b>optional parameter</b> - the template rule to search for. If this parameter is passed as null
	 *            or empty, <b>only templates without a rule</b> , matching the rest of the criteria will be returned!
	 * @param correspondingInstance
	 *            is the corresponding instance ID of the new 'candidate' for primary template. Needed when the new
	 *            primary template has a record in the RDB (has been activated at some point). It will be filtered out
	 *            from the found results, so that it can't find itself and return itself as a result.
	 * @return an optional of the found template, if any
	 */
	public Optional<Template> findExistingPrimaryTemplate(String forType, String purpose, String rule,
			String correspondingInstance) {

		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<>(TemplateProperties.GROUP_ID, forType));
		params.add(new Pair<>(TemplateProperties.PURPOSE, purpose));
		String query = TemplateEntity.QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE_KEY;

		List<TemplateEntity> templates = dbDao.fetchWithNamed(query, params);

		// Filter out the new primary template. Otherwise it could "find" itself and demote itself to secondary
		if (StringUtils.isNotBlank(correspondingInstance)) {
			templates = templates.stream()
					.filter(template -> !correspondingInstance.equals(template.getCorrespondingInstance()))
					.collect(Collectors.toList());
		}

		if (StringUtils.isNotBlank(rule)) {
			return templates.stream()
					.map(TemplateDao::toTemplate)
					.filter(template -> StringUtils.isNotBlank(template.getRule()))
					.filter(template -> TemplateRuleUtils.equals(template.getRule(), rule))
					.findFirst();
		}
		// if rule is null or empty, ignore all templates having a rule. They shouldn't be touched if not
		// needed.
		return templates
				.stream()
				.map(TemplateDao::toTemplate)
				.filter(template -> StringUtils.isBlank(template.getRule())).findFirst();
	}

	private static Template toTemplate(TemplateEntity entity) {
		Template template = new Template();
		template.setId(entity.getTemplateId());
		template.setTitle(entity.getTitle());
		template.setPurpose(entity.getPurpose());
		template.setForType(entity.getGroupId());
		template.setCorrespondingInstance(entity.getCorrespondingInstance());
		template.setPrimary(entity.getPrimary());
		template.setContentDigest(entity.getContentDigest());
		template.setRule(entity.getRule());
		template.setPublishedInstanceVersion(entity.getPublishedInstanceVersion());
		template.setModifiedOn(entity.getModifiedOn());
		template.setModifiedBy(entity.getModifiedBy());
		return template;
	}

	private static TemplateEntity toEntity(Template template) {
		TemplateEntity entity = new TemplateEntity();
		entity.setTemplateId(template.getId());
		entity.setTitle(template.getTitle());
		entity.setPurpose(template.getPurpose());
		entity.setGroupId(template.getForType());
		entity.setCorrespondingInstance(template.getCorrespondingInstance());
		entity.setPrimary(template.getPrimary());
		entity.setContentDigest(template.getContentDigest());
		entity.setRule(template.getRule());
		entity.setPublishedInstanceVersion(template.getPublishedInstanceVersion());
		return entity;
	}

	/**
	 * Fetches template content from the db.
	 *
	 * @param templateId template for which to fetch the content.
	 * @return template content or null if such content is not found.
	 */
	public String getTemplateContent(String templateId) {
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>("templateId", templateId));

		List<TemplateContentEntity> result = dbDao.fetchWithNamed(TemplateContentEntity.QUERY_BY_TEMPLATE_ID_KEY, params);

		if (!result.isEmpty()) {
			return result.get(0).getContent();
		}

		return null;
	}

	/**
	 * Deletes existing template content.
	 *
	 * @param templateId id of the template which content to delete.
	 */
	public void deleteContent(String templateId) {
		dbDao.delete(TemplateContentEntity.class, templateId);
	}

	private String getCurrentUser() {
		if (securityContext.isActive()) {
			return securityContext.getAuthenticated().getIdentityId();
		}
		return null;
	}

	/**
	 * Checks if a template exists for the provided template or corresponding instance identifier.
	 *
	 * @param templateId - identifier of the template or its corresponding instance
	 * @return <code>true</code> if a template exist or <code>false</code> if not
	 */
	public boolean hasTemplate(String templateId) {
		return dbDao.fetchWithNamed(TemplateEntity.QUERY_HAS_TEMPLATE_KEY,
				Collections.singletonList(new Pair<>("id", templateId))).size() > 0;
	}
}
