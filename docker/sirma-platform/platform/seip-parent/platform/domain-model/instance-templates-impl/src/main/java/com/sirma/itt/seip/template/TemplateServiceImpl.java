package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.ObjectTypes.TEMPLATE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.template.TemplateActions.ACTIVATE_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.DEACTIVATE_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.RELOAD_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.SET_TEMPLATE_AS_PRIMARY;
import static com.sirma.itt.seip.template.TemplateProperties.CORRESPONDING_INSTANCE;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_PURPOSE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE_DESCRIPTION;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.template.db.TemplateContentEntity;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.db.TemplateEntity;
import com.sirma.itt.seip.template.exceptions.InvalidTemplateOperationException;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;
import com.sirma.itt.seip.template.rules.TemplateRuleTranslator;
import com.sirma.itt.seip.template.rules.TemplateRuleUtils;
import com.sirma.itt.seip.template.schedule.TemplateActivateScheduler;
import com.sirma.itt.seip.template.utils.TemplateUtils;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.DigestUtils;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;

/**
 * Implementation of {@link TemplateService}.
 *
 * @author Adrian Mitev
 * @author Vilizar Tsonev
 */
@Transactional
@ApplicationScoped
public class TemplateServiceImpl implements TemplateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateServiceImpl.class);

	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final String STATUS_INACTIVE = "INACTIVE";
	private static final String STATUS_DRAFT = "DRAFT";
	private static final String STATUS_UPDATED = "UPDATED";

	/**
	 * Constant for sql prepared statements.
	 */
	private static final String TEMPLATE_ID = "templateId";

	/**
	 * Used when a default template is not found so this is returned as the default template content.
	 */
	private static final String DEFAULT_TEMPLATE_CONTENT = "<div data-tabs-counter=\"2\">"
			+ "<section data-title=\"Tab1\"data-show-navigation=\"true\""
			+ "data-show-comments=\"true\"data-default=\"true\"></section></div>";

	private static final String TEMPLATES_DEFAULT_TITLE_ID = "templates.default.title";
	private static final String DETECTED_CHANGE_IN_TEMPLATE = "Detected change in template [{}] , field [{}] . Old value: [{}] , New value: [{}]";
	private static final String DETECTED_CHANGE_IN_TEMPLATE_SYNC = DETECTED_CHANGE_IN_TEMPLATE + ". Synchronizing its relational record...";

	@Inject
	private DbDao dbDao;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private ContextualReadWriteLock templatesLock;

	@Inject
	private TemplatePreProcessor preProcessor;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceService instanceService;

	private final TemplateDao templateDao;

	private final TemplateInstanceHelper templateInstanceHelper;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private DatabaseIdManager databaseIdManager;

	@Inject
	private TemplateRuleTranslator templateRuleTranslator;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	public TemplateServiceImpl() {
		this(null, null);
	}

	/**
	 * Constructs the template service with the supplied dependent services.
	 *
	 * @param templateInstanceHelper
	 *            is a {@link TemplateInstanceHelper} instance
	 * @param templateDao
	 *            is a {@link TemplateDao} instance
	 */
	@Inject
	public TemplateServiceImpl(TemplateInstanceHelper templateInstanceHelper, TemplateDao templateDao) {
		this.templateInstanceHelper = templateInstanceHelper;
		this.templateDao = templateDao;
	}

	@Override
	public String getContent(String id) {
		if (id == null) {
			return null;
		} else if (DEFAULT_TEMPLATE_ID.equals(id)) {
			return getDefaultTemplate().getContent();
		}

		Template template = templateDao.getTemplate(id);
		if (template == null) {
			return null;
		}

		String content = loadContent(template);

		if (content == null) {
			throw new EmfApplicationException("No content found for template " + id + "");
		}

		return content;
	}

	private String loadContent(Template template) {
		if (DEFAULT_TEMPLATE_ID.equals(template.getId())) {
			return getDefaultTemplate().getContent();
		}

		return templateDao.getTemplateContent(template.getId());
	}

	@Override
	public void setAsPrimaryTemplate(String templateInstanceId) {
		Objects.requireNonNull(templateInstanceId, "Instance Id is required when setting template as primary");

		InstanceReference templateReference = instanceTypeResolver
				.resolveReference(templateInstanceId)
					.orElseThrow(() -> new MissingTemplateException(
						"Tried to set as primary template non-existing instance [" + templateInstanceId + "]"));

		Instance templateInstance = templateReference.toInstance();
		if (!templateInstance.type().is(TEMPLATE)) {
			throw new InvalidTemplateOperationException(
					"Tried to set as primary instance [" + templateInstanceId + "] that is not of type template");
		}
		if (templateInstance.getBoolean(IS_PRIMARY_TEMPLATE)) {
			LOGGER.warn(
					"Trying to execute operation Set As Primary on a template [{}] that is already primary. Operation aborted.",
					templateInstanceId);
			return;
		}
		setAsPrimaryInternal(templateInstance);
		LOGGER.info("Template [{}] successfully set as primary.", templateInstanceId);
	}

	private void setAsPrimaryInternal(Instance templateInstance) {
		boolean active = STATUS_ACTIVE.equals(templateInstance.getString(STATUS));
		if (active) {
			Template template = templateDao.getTemplate(templateInstance.getId().toString());
			if (template == null) {
				throw new MissingTemplateException("Active template instance [" + templateInstance.getId()
						+ "] has no corresponding record in the relational DB. Set as Primary operation aborted");
			}
			// demote should be performed only when an *active* template is being set as primary (because there is no
			// problem to have more than one non-active primary). Also, it is performed
			// *before* we save the new one as primary, because if performed after it, the search (and filtering) will
			// return the newly set as primary template and will demote it (thus, reverting the action).
			demoteExistingPrimary(template);

			// set as primary in the RDB
			template.setPrimary(true);
			templateDao.saveOrUpdate(template);

			// synchronize the primary flag with the DMS
			String content = loadContent(template);
			template.setContent(content);
		}

		templateInstance.add(IS_PRIMARY_TEMPLATE, Boolean.TRUE);
		InstanceSaveContext saveContext = InstanceSaveContext.create(templateInstance,
				new Operation(SET_TEMPLATE_AS_PRIMARY));
		domainInstanceService.save(saveContext);
	}

	private Template getDefaultTemplate() {
		Template template = new Template();
		template.setPrimary(true);
		template.setForType("commonDocument");
		template.setId(DEFAULT_TEMPLATE_ID);
		template.setContent(DEFAULT_TEMPLATE_CONTENT);
		template.setTitle(labelProvider.getValue(TEMPLATES_DEFAULT_TITLE_ID));
		return template;
	}

	@Override
	public List<Template> getTemplates(TemplateSearchCriteria criteria) {
		List<Template> foundTemplates = templateDao.getTemplates(criteria.getGroup(), criteria.getPurpose());

		// Creating a new list, because the returned one by the DAO is not modifiable
		List<Template> results = new ArrayList<>(foundTemplates);

		if (criteria.getFilter() != null) {
			results = TemplateRuleUtils.filter(results, criteria.getFilter());
		}

		putPrimaryTemplateInFirstPlace(results);
		sortTemplatesDescendingRulesWeight(results);
		results.add(getDefaultTemplate());

		return results;
	}

	private static void putPrimaryTemplateInFirstPlace(List<Template> templates) {
		int primaryTemplatePosition = -1;
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).getPrimary()) {
				primaryTemplatePosition = i;
				break;
			}
		}

		// no need to move if already first
		if (primaryTemplatePosition > 0) {
			templates.add(0, templates.remove(primaryTemplatePosition));
		}
	}

	private static void sortTemplatesDescendingRulesWeight(List<Template> templates) {
		templates.sort((Comparator.comparing(Template::getRuleWeight).thenComparing(Template::getRuleWeight)).reversed());
	}

	@Override
	public Template getTemplate(TemplateSearchCriteria criteria) {
		List<Template> templates = getTemplates(criteria);
		if (templates.isEmpty()) {
			return null;
		}

		Template template = templates.get(0);

		String content = loadContent(template);
		template.setContent(content);

		return template;
	}

	private void persistActiveTemplateData(Template template) {
		Objects.requireNonNull(template, "template passed for activation must not be null");
		try {
			templatesLock.writeLock().lock();
			persistTemplateData(template);
		} finally {
			templatesLock.writeLock().unlock();
		}
	}

	@Override
	public Instance activate(String templateInstanceId) {
		return activateInternal(templateInstanceId, true);
	}

	@Override
	public Instance activate(String templateInstanceId, boolean controlPrimaryFlag) {
		return activateInternal(templateInstanceId, controlPrimaryFlag);
	}

	private Instance activateInternal(String templateInstanceId, boolean controlPrimaryFlag) {
		Objects.requireNonNull(templateInstanceId, "Instance Id is required when activating a template");

		InstanceReference templateReference = instanceTypeResolver
				.resolveReference(templateInstanceId)
					.orElseThrow(() -> new MissingTemplateException(
						"Tried to activate template instance [" + templateInstanceId + "] which does not exist."));

		Instance templateInstance = templateReference.toInstance();
		if (!templateInstance.type().is(TEMPLATE)) {
			throw new InvalidTemplateOperationException(
					"Tried to activate instance [" + templateInstanceId + "] which is not of type template");
		}
		boolean alreadyInStatusActive = STATUS_ACTIVE.equals(templateInstance.getString(DefaultProperties.STATUS));
		boolean enforcedPrimary = false;
		if (!templateInstance.getBoolean(IS_PRIMARY_TEMPLATE)
				&& StringUtils.isNotBlank(templateInstance.getString(TEMPLATE_RULE)) && controlPrimaryFlag) {
			enforcedPrimary = enforcePrimaryIfFirst(templateInstance);
		}
		Template templateData = convertInstanceToTemplateData(templateInstance);

		if (!alreadyInStatusActive) {
			InstanceSaveContext context = InstanceSaveContext.create(templateInstance,
					new Operation(ACTIVATE_TEMPLATE));
			// save the instance, so that the state transition to active state is performed
			templateInstance = domainInstanceService.save(context);
			LOGGER.info("Template instance [{}] for type {} successfully activated.", templateInstance.getId(),
					templateData.getForType());
		}

		templateData.setPublishedInstanceVersion(templateInstance.getString(DefaultProperties.VERSION));

		// demoting of existing primary template is executed before the newly activated is persisted in RDB, because
		// otherwise, the new one would be found here and demoted, leaving incorrect data
		if (!enforcedPrimary && templateData.getPrimary() && controlPrimaryFlag) {
			demoteExistingPrimary(templateData);
		}

		persistActiveTemplateData(templateData);
		return templateInstance;
	}

	/**
	 * Finds an existing primary template for the type/purpose/rule of the passed template and demotes its primary flag
	 * to false. </br>
	 * <b>important:</b> This method has to be invoked <b>before</b> executing the actual operation requiring the demote
	 * (like activate or set as primary). Otherwise, the new primary template can find and demote itslef.
	 */
	private void demoteExistingPrimary(Template newPrimaryTemplate) {
		Optional<Template> existingPrimaryOptional = templateDao.findExistingPrimaryTemplate(
				newPrimaryTemplate.getForType(), newPrimaryTemplate.getPurpose(), newPrimaryTemplate.getRule(),
				newPrimaryTemplate.getCorrespondingInstance());
		if (!existingPrimaryOptional.isPresent()) {
			return;
		}
		Template existingPrimary = existingPrimaryOptional.get();

		existingPrimary.setPrimary(false);
		LOGGER.debug("Demoting existing primary template {} to become secondary in RDB. The new primary is {}",
				existingPrimary.getId(), newPrimaryTemplate.getId());
		templateDao.saveOrUpdate(existingPrimary);

		Instance existingPrimaryInstance = instanceTypeResolver
				.resolveReference(existingPrimary.getCorrespondingInstance())
					.map(InstanceReference::toInstance)
					.orElse(null);

		if (existingPrimaryInstance != null) {
			// demote the template instance in the semantics
			templateInstanceHelper.demoteInstance(existingPrimaryInstance, newPrimaryTemplate.getId());
		}

		// Synchronize the primary flag in the DMS
		// Since the operation is atomic and the whole file is uploaded, its content must be retrieved first.
		String content = loadContent(existingPrimary);
		existingPrimary.setContent(content);
	}

	private boolean enforcePrimaryIfFirst(Instance template) {
		Optional<Template> existingPrimaryOptional = templateDao.findExistingPrimaryTemplate(
				template.getString(FOR_OBJECT_TYPE), template.getString(TEMPLATE_PURPOSE),
				template.getString(TEMPLATE_RULE), template.getString(CORRESPONDING_INSTANCE));
		if (!existingPrimaryOptional.isPresent()) {
			template.add(IS_PRIMARY_TEMPLATE, Boolean.TRUE);
			return true;
		}
		return false;
	}

	/**
	 * Converts an {@link Instance} extracted from the system to a {@link Template} that is used just as a data carrier
	 * and easier manipulation. Also, extracts its content via {@link InstanceContentService}.
	 */
	private Template convertInstanceToTemplateData(Instance instance) {
		Template templateData = new Template();
		templateData.setTitle(instance.getString(DefaultProperties.TITLE, ""));
		templateData.setForType(instance.getAsString(TemplateProperties.FOR_OBJECT_TYPE));
		templateData.setPurpose(instance.getAsString(TemplateProperties.TEMPLATE_PURPOSE));
		templateData.setPrimary(instance.getBoolean(TemplateProperties.IS_PRIMARY_TEMPLATE));
		templateData.setCorrespondingInstance(instance.getId().toString());
		templateData.setRule(instance.getAsString(TEMPLATE_RULE));

		try {
			ContentInfo contentInfo = instanceContentService.getContent(instance, Content.PRIMARY_VIEW);
			if (!contentInfo.exists()) {
				throw new IllegalStateException("No view found for instance [" + instance.getId() + "]");
			}
			String content = IOUtils.toString(contentInfo.getInputStream());
			templateData.setContent(content);
		} catch (IOException | IllegalStateException e) {
			LOGGER.error("Cannot load view for instance [{}]", instance.getId());
			throw new EmfApplicationException("Cannot extract content for '" + templateData.getForType() + "'", e);
		}
		return templateData;
	}

	private void persistTemplateData(Template template) {

		boolean mailTemplate = EMAIL_TEMPLATE_TYPE.equals(template.getForType());
		if (!mailTemplate) {
			preProcessor.process(new TemplateContext(template));
		}

		setDefaultTemplateProperties(template);

		templateDao.saveOrUpdate(template);

		TemplateContentEntity contentEntity = new TemplateContentEntity();
		contentEntity.setId(template.getId());
		contentEntity.setContent(template.getContent());
		contentEntity.setFileName(template.getId() + ".xml");
		dbDao.saveOrUpdate(contentEntity);
	}

	private static void setDefaultTemplateProperties(Template template) {
		// generate identifier if missing
		if (template.getId() == null) {
			String templateIdentifier = template.getTitle();
			if (StringUtils.isBlank(templateIdentifier)) {
				throw new EmfApplicationException("Template for type [" + template.getForType()
						+ "] has missing title and can not be activated. Title is used to build a unique identifier and is a required field.");
			}
			templateIdentifier = TemplateUtils.buildIdFromTitle(templateIdentifier);
			template.setId(templateIdentifier);
		}
		if (template.getForType() == null) {
			template.setForType(TemplateProperties.DEFAULT_GROUP);
		}
		if (StringUtils.isBlank(template.getPurpose())) {
			template.setPurpose(TemplatePurposes.CREATABLE);
		}
		String content = template.getContent();
		template.setContentDigest(calculateDigest(content));
	}

	@Override
	public void deactivate(String templateInstanceId) {
		Objects.requireNonNull(templateInstanceId, "Instance Id is required when deactivating a template");

		InstanceReference templateReference = instanceTypeResolver
				.resolveReference(templateInstanceId)
					.orElseThrow(() -> new MissingTemplateException(
						"Tried to deactivate non-existing template instance [" + templateInstanceId + "]"));

		Instance templateInstance = templateReference.toInstance();
		if (!templateInstance.type().is(TEMPLATE)) {
			throw new InvalidTemplateOperationException("Tried to perform deactivate on instance [" + templateInstanceId
					+ "] which is not of type template");
		}
		if (templateInstance.getBoolean(IS_PRIMARY_TEMPLATE)) {
			throw new InvalidTemplateOperationException(
					"Template [" + templateInstanceId + "] is a primary template and can not be deactivated");
		}

		Template template = templateDao.getTemplate(templateInstanceId);
		if (template != null) {
			// delete from sep_template table
			templateDao.delete(template.getId());
			// delete the associated record from sep_template_content as well

			templateDao.deleteContent(template.getId());
		}

		InstanceSaveContext saveContext = InstanceSaveContext.create(templateInstance,
				new Operation(DEACTIVATE_TEMPLATE));
		domainInstanceService.save(saveContext);

		LOGGER.info("Template [{}] successfully deactivated.", templateInstanceId);
	}

	@Override
	public String create(Template template, String view) {
		Instance createdInstance = createTemplateInstanceInternal(template, null, view);

		return (String) createdInstance.getId();
	}

	private Instance createTemplateInstanceInternal(Template templateData, String id, String content) {
		// the corresponding library will be set as a parent of the template
		String parent = getSemanticClass(templateData.getForType());

		DefinitionModel templateDefinition = definitionService.find(TemplateProperties.TEMPLATE_DEFINITION_ID);

		Instance parentInstance = new ObjectInstance();
		parentInstance.setId(parent);

		Instance templateInstance = domainInstanceService.createInstance(templateDefinition, parentInstance);

		// ensure the template instance is created with the same corresponding id as in the imported template
		if (StringUtils.isNotBlank(id)) {
			databaseIdManager.unregisterId(templateInstance.getId());
			databaseIdManager.registerId(id);

			templateInstance.setId(id);
		}

		populateTemplateProperties(templateData, templateInstance, content);

		InstanceSaveContext context = InstanceSaveContext.create(templateInstance,
				new Operation(ActionTypeConstants.CREATE));
		Instance savedInstance = domainInstanceService.save(context);
		LOGGER.info("Template instance for type [{}] successfully created.", templateData.getForType());
		return savedInstance;
	}

	private String getSemanticClass(String forType) {
		if (URIUtil.isValidURIReference(forType)) {
			// it is possible for a semantic URI to be passed as forType. In this case, no further processing is
			// needed.
			return forType;
		}
		DefinitionModel definitionModel = definitionService.find(forType);
		if (definitionModel == null) {
			throw new IllegalArgumentException("Failed to load definition model for [" + forType + "]");
		}
		Optional<PropertyDefinition> rdfType = definitionModel.getField(DefaultProperties.SEMANTIC_TYPE);
		if (!rdfType.isPresent()) {
			throw new IllegalArgumentException(
					DefaultProperties.SEMANTIC_TYPE + " field is not specified for definition ["
							+ definitionModel.getIdentifier() + "] Failed to retrieve library for type " + forType);
		}
		return rdfType.get().getDefaultValue();
	}

	private void populateTemplateProperties(Template source, Instance destination, String content) {
		destination.add(DefaultProperties.TEMP_CONTENT_VIEW, content);
		destination.add(DefaultProperties.TITLE, source.getTitle());
		destination.add(TemplateProperties.FOR_OBJECT_TYPE, source.getForType());
		destination.add(TemplateProperties.TEMPLATE_PURPOSE, source.getPurpose());
		destination.add(TemplateProperties.IS_PRIMARY_TEMPLATE, source.getPrimary());
		destination.add(TemplateProperties.TEMPLATE_RULE, source.getRule());
		if (StringUtils.isNotBlank(source.getRule())) {
			String ruleDescription = templateRuleTranslator.translate(source.getRule(), source.getForType());
			destination.add(TEMPLATE_RULE_DESCRIPTION, ruleDescription);
		}
		// restore template instance if previously deleted
		Boolean deleted = destination.getBoolean(DefaultProperties.IS_DELETED, fieldConverter);
		if (Boolean.TRUE.equals(deleted)) {
			destination.add(DefaultProperties.IS_DELETED, false, fieldConverter);
			destination.add(DefaultProperties.STATUS, STATUS_ACTIVE);
			destination.add(DefaultProperties.DELETED_ON, null, fieldConverter);
		}
	}

	private static Idoc parseViewContent(String content) {
		Idoc idoc = Idoc.parse(content);
		idoc.widgets().filter(widget -> StringUtils.isBlank(widget.getId())).forEach(
				widget -> widget.addProperty(ContentNode.ELEMENT_ID, RandomStringUtils.randomAlphanumeric(8)));
		idoc.getSections().stream().filter(section -> StringUtils.isBlank(section.getId())).forEach(
				sectionNode -> sectionNode.addProperty(SectionNode.SECTION_NODE_ID_KEY,
						RandomStringUtils.randomAlphanumeric(8)));
		return idoc;
	}

	@Override
	public Template getTemplate(String id) {
		return templateDao.getTemplate(id);
	}

	/**
	 * Persists the data for the passed newly imported template. Creates a corresponding instance for it, if it still
	 * hasn't. If changes were detected, updates its data in the system. Activates it. </br>
	 * To be used <b>only when importing templates (as definitions).</b></br>
	 * For all other needs and cases, use the API provided by {@link TemplateService}
	 *
	 * @param template the newly imported template
	 */
	public void saveOrUpdateImportedTemplate(Template template) {
		LOGGER.debug("Processing template {}", template.getId());

		// template identifiers are stored in the RDB only converted to lower case. In order for the
		// lookup by identifier to work, toLowerCase is needed here as well (in case someone manually
		// modified it in the definition).
		template.setId(template.getId().toLowerCase());

		Serializable content = template.getContent();

		// we currently don't support template idoc compilation for emails
		boolean needsInstanceCreated = !EMAIL_TEMPLATE_TYPE.equals(template.getForType());
		if (needsInstanceCreated) {
			// If not specified in the xml, try to retrieve the corresponding instance (for that template ID) from
			// the RDB. That's in order to avoid creation of a new instance when not needed.
			if (StringUtils.isBlank(template.getCorrespondingInstance())) {
				String correspondingInstanceId = fetchTemplateById(template.getId())
						.map(TemplateEntity::getCorrespondingInstance)
							.orElse(null);
				template.setCorrespondingInstance(correspondingInstanceId);
			}
			synchronizeRelationalRecord(template);

			// process the template before saving its content to an instance view to avoid difference between
			// the template stored in the document store and the view stored in the template instance
			preProcessor.process(new TemplateContext(template));
			// the template content may have been modified manually, so calculate the digest on reload
			template.setContentDigest(calculateDigest(content));

			// added separate transaction because when create instances the check for previous is not correct
			transactionSupport.invokeInNewTx(
					() -> createOrUpdateCorrespondingInstance(template, content.toString()));
		} else {
			template.setContentDigest(calculateDigest(content));
			persistActiveTemplateData(template);
		}
	}

	@Override
	public List<Template> getAllTemplates() {
		return templateDao.getAllTemplates();
	}

	@Override
	public boolean hasTemplate(String templateInstanceId) {
		return templateDao.hasTemplate(templateInstanceId);
	}

	private void synchronizeRelationalRecord(Template importedTemplate) {
		Optional<TemplateEntity> entityOptional = fetchTemplateById(importedTemplate.getId());
		if (!entityOptional.isPresent()) {
			return;
		}
		TemplateEntity existingTemplate = entityOptional.get();
		boolean changed = false;

		if (!EqualsHelper.nullSafeEquals(existingTemplate.getCorrespondingInstance(),
				importedTemplate.getCorrespondingInstance())) {
			LOGGER.debug(DETECTED_CHANGE_IN_TEMPLATE_SYNC,
					importedTemplate.getId(), TemplateProperties.CORRESPONDING_INSTANCE,
					existingTemplate.getCorrespondingInstance(), importedTemplate.getCorrespondingInstance());
			existingTemplate.setCorrespondingInstance(importedTemplate.getCorrespondingInstance());
			changed = true;
		}

		if (changed) {
			dbDao.saveOrUpdate(existingTemplate);
		}
	}

	private void scheduleTemplateForActivation(String id) {
		SchedulerContext schedulerContext = TemplateActivateScheduler.createExecutorContext(id);
		SchedulerConfiguration configuration = schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		configuration
				.setScheduleTime(new Date())
					.setRemoveOnSuccess(true)
					.setPersistent(true)
					.setMaxRetryCount(5)
					.setRetryDelay(60L)
					.setRunAs(RunAs.USER);
		LOGGER.debug("Scheduling template for activation: {}", id);
		schedulerService.schedule(TemplateActivateScheduler.BEAN_ID, configuration, schedulerContext);
	}

	private String createOrUpdateCorrespondingInstance(Template template, String content) {
		try {
			String correspondingInstanceId = template.getCorrespondingInstance();

			if (StringUtils.isBlank(correspondingInstanceId)) {
				LOGGER.trace(
						"No correspondingInstanceId specified for template {}. A new instance will be created and its ID stored in the xml.",
						template.getId());
				String newlyCreatedId = createInstance(template, null, content);
				scheduleTemplateForActivation(newlyCreatedId);
				return newlyCreatedId;
			}

			// The corresponding instance may exist, but to be marked as deleted. We have to load deleted as well,
			// because otherwise, the data for the existing URI will be saved as new, and duplicated properties will
			// appear in the semantics.
			Optional<Instance> existingInstance = instanceService.loadDeleted(correspondingInstanceId);
			if (!existingInstance.isPresent()) {
				LOGGER.trace("Corresponding instance {} does not exist. Creating a new one", correspondingInstanceId);
				String newlyCreatedId = createInstance(template, correspondingInstanceId, content);
				scheduleTemplateForActivation(newlyCreatedId);
				return newlyCreatedId;
			}

			Instance existingCorrespondingInstance = existingInstance.get();

			String instanceState = existingCorrespondingInstance.getString(DefaultProperties.STATUS).toUpperCase();
			boolean templateInstanceCurrentlyEdited = STATUS_DRAFT.equals(instanceState) || STATUS_UPDATED.equals(instanceState);

			if (!templateInstanceCurrentlyEdited && hasTemplateChanged(existingCorrespondingInstance, template)) {
				LOGGER.trace("Template {} is changed. Updating its corresponding instance {}", template.getId(),
						correspondingInstanceId);
				populateTemplateProperties(template, existingCorrespondingInstance, content);
				InstanceSaveContext context = InstanceSaveContext.create(existingCorrespondingInstance,
						new Operation(RELOAD_TEMPLATE));
				domainInstanceService.save(context);
				transactionSupport.invokeOnSuccessfulTransactionInTx(
						() -> scheduleTemplateForActivation(correspondingInstanceId));
			} else {
				LOGGER.trace(
						"Corresponding instance {} for template {} aldready exists and no changes were detected on it. Nothing will be done with it.",
						correspondingInstanceId, template.getId());
			}

			return correspondingInstanceId;
		} catch (IllegalArgumentException e) {
			// skip the templates that don't have a valid forType so that the import process in not
			// interrupted
			LOGGER.warn("Template [{}] has invalid forType [{}] and will be skipped. Reason: {} ",
					template.getId(), template.getForType(), e.getMessage());
			LOGGER.trace("Invalid template", e);
		}
		return null;
	}

	private boolean hasTemplateChanged(Instance correspondingInstance, Template newTemplate) {
		// if the existing corresponding instance in marked as deleted, we have to bring it back to life. Indicate
		// as changed so that the instance save do its job.
		if (correspondingInstance.isDeleted()) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), DefaultProperties.IS_DELETED,
					correspondingInstance.isDeleted(), Boolean.TRUE);
			return true;
		}
		// the inactive templates have to be re-activated as well. Indicate as changed so that the correspondingInstance
		// is scheduled for activation
		if (STATUS_INACTIVE.equals(correspondingInstance.getString(STATUS))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), STATUS,
					correspondingInstance.getString(STATUS), STATUS_ACTIVE);
			return true;
		}
		if (!Boolean.valueOf(newTemplate.getPrimary())
					.equals(correspondingInstance.getBoolean(TemplateProperties.IS_PRIMARY_TEMPLATE))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), IS_PRIMARY_TEMPLATE,
					correspondingInstance.getBoolean(IS_PRIMARY_TEMPLATE), newTemplate.getPrimary());
			return true;
		}
		if (!EqualsHelper.nullSafeEquals(newTemplate.getPurpose(),
				correspondingInstance.getString(TemplateProperties.TEMPLATE_PURPOSE))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), TEMPLATE_PURPOSE,
					correspondingInstance.getString(TEMPLATE_PURPOSE), newTemplate.getPurpose());
			return true;
		}
		if (!EqualsHelper.nullSafeEquals(newTemplate.getTitle(), correspondingInstance.getString(TITLE))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), TITLE,
					correspondingInstance.getString(TITLE), newTemplate.getTitle());
			return true;
		}
		if (!newTemplate.getForType().equals(correspondingInstance.getString(FOR_OBJECT_TYPE))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), FOR_OBJECT_TYPE,
					correspondingInstance.getString(FOR_OBJECT_TYPE), newTemplate.getForType());
			return true;
		}
		if (!EqualsHelper.nullSafeEquals(newTemplate.getRule(), correspondingInstance.getString(TEMPLATE_RULE))) {
			LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(), TEMPLATE_RULE,
					correspondingInstance.getString(TEMPLATE_RULE), newTemplate.getRule());
			return true;
		}
		return isTemplateContentChanged(newTemplate);
	}

	private boolean isTemplateContentChanged(Template newTemplate) {
		Template existingTemplate = templateDao.getTemplate(newTemplate.getId());

		if (existingTemplate != null) {
			boolean changed = !existingTemplate.getContentDigest().equals(newTemplate.getContentDigest());
			if (changed) {
				LOGGER.trace(DETECTED_CHANGE_IN_TEMPLATE, newTemplate.getId(),
						TemplateProperties.CONTENT_DIGEST, existingTemplate.getContentDigest(),
						newTemplate.getContentDigest());
			}
			return changed;
		}
		return false;
	}

	private Optional<TemplateEntity> fetchTemplateById(String id) {
		List<String> ids = Collections.singletonList(id);
		List<TemplateEntity> fetched = dbDao.fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY,
				Collections.singletonList(new Pair<>(TEMPLATE_ID, ids)));
		if (!fetched.isEmpty()) {
			return Optional.of(fetched.get(0));
		}
		return Optional.empty();
	}

	private String createInstance(Template instance, String correspondingInstanceId, String content) {
		String newContent = parseViewContent(content).asHtml();
		String persistedInstanceId = (String) createTemplateInstanceInternal(instance, correspondingInstanceId,
				newContent).getId();
		instance.setCorrespondingInstance(persistedInstanceId);
		instance.setContent(newContent);
		LOGGER.debug("Uploaded to DMS newly created corresponding instance [{}] for type {}", correspondingInstanceId,
				instance.getForType());
		return persistedInstanceId;
	}

	private static String calculateDigest(Serializable content) {
		if (content == null) {
			return null;
		}
		return DigestUtils.calculateDigest(content.toString());
	}
}
