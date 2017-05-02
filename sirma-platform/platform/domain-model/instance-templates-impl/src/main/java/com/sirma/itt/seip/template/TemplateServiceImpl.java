package com.sirma.itt.seip.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.openrdf.model.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.descriptor.ByteArrayAndPropertiesDescriptor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.compile.DefinitionCompiler;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.compile.DefinitionType;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.template.event.BeforeTemplatePersistEvent;
import com.sirma.itt.seip.template.event.TemplateOpenEvent;
import com.sirma.itt.seip.template.event.TemplatePersistedEvent;
import com.sirma.itt.seip.template.schedule.TemplateActivateScheduler;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.DigestUtils;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Idoc;
import com.sirmaenterprise.sep.content.idoc.SectionNode;

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

	/** The forbidden characters for template identifier */
	private static final Pattern FORBIDDEN_NAME_CHARACTERS = Pattern.compile("\\s|\\W");

	/**
	 * Used when a template for a given instance is not found so the default template is used instead.
	 */
	private static final String DEFAULT_TEMPLATE_ID = "defaultTemplate";

	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private static final String STATUS_ACTIVE = "ACTIVE";

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
	private static final String ALL_TEMPLATES_IDS = "*";

	@Inject
	@DefinitionType(TemplateProperties.TEMPLATE_TYPE)
	private DefinitionCompilerCallback<? extends TemplateDefinition> definitionCompilerCallback;

	@Inject
	private ContentAdapterService contentAdapterService;

	@Inject
	private DefintionAdapterService defintionAdapterService;

	@Inject
	private DefinitionCompiler compiler;

	@Inject
	private DefinitionCompilerHelper compilerHelper;

	@Inject
	private DbDao dbDao;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	@InstanceType(type = TemplateProperties.TEMPLATE_TYPE)
	private InstanceDao templateInstanceDao;

	@Inject
	private ObjectMapper mapper;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private EventService eventService;

	@Inject
	private TaskExecutor executor;

	@Inject
	private ContextualReadWriteLock templatesLock;

	@Inject
	private TemplatePreProcessor preProcessor;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private Statistics statistics;

	@Inject
	private SearchService searchService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Property to enable/disable parallel template saving. Disabled by default due to some unknown race condition in
	 * DMS when document upload is made.<b>Default value is: false</b>
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "templates.useParallelSave", type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "Property to enable/disable parallel template saving. Disabled by default due to some unknown race condition in DMS when document upload is made.")
	private ConfigurationProperty<Boolean> enableParallelTemplateSave;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public TemplateInstance getTemplateWithContent(Serializable templateId) {
		TemplateInstance instance = getTemplate(templateId);
		if (instance == null) {
			return null;
		}
		return loadContent(instance);
	}

	@Override
	public TemplateInstance loadContent(TemplateInstance instance) {
		if (instance == null) {
			return null;
		}
		// load the file from DMS
		try (InputStream inputStream = downloadContentFromDms(instance)) {
			String content = null;
			if (inputStream != null) {
				content = IOUtils.toString(inputStream);
			}
			if (content == null) {
				content = DEFAULT_TEMPLATE_CONTENT;
				// if we couldn't manage to find the template in the DMS then it's probably deleted
				// so we should remove the local information about it.
				// on second thought if the remote service is down or there is a problem with the network this may
				// trigger massive templates wipe
			}

			// set loaded content
			instance.getProperties().put(DefaultProperties.CONTENT, content);
			instance.getProperties().put(TemplateProperties.IS_CONTENT_LOADED, Boolean.TRUE);

			// notify that a template is loaded and is going to be returned to the user.
			eventService.fire(new TemplateOpenEvent(instance));
		} catch (IOException e) {
			LOGGER.warn("Failed to read template content due to: {}", e.getMessage());
			LOGGER.debug("", e);
		}
		return instance;
	}

	@Override
	public String getDefaultTemplateContent() {
		return getDefaultTemplate().getContent();
	}

	@Override
	public List<TemplateInstance> getAllTemplates(String user) {
		String query = TemplateEntity.QUERY_TEMPLATES_FOR_USER_KEY;
		List<Pair<String, Object>> params = new ArrayList<>(1);
		if (user == null) {
			params.add(new Pair<>("users", Arrays.asList("system", "<no_user>")));
		} else {
			params.add(new Pair<>("users", user));
		}
		List<Long> list = dbDao.fetchWithNamed(query, params);
		return templateInstanceDao.loadInstancesByDbKey(list);
	}

	@Override
	public List<TemplateInstance> getTemplates(String documentType) {
		return getTemplatesInternal(documentType, null);
	}

	@Override
	public List<TemplateInstance> getTemplates(String documentType, String purpose) {
		return getTemplatesInternal(documentType, purpose);
	}

	@Override
	public TemplateInstance getPrimaryTemplate(String documentType) {
		return getPrimaryTemplateInternal(documentType, null);
	}

	@Override
	public TemplateInstance getPrimaryTemplate(String documentType, String purpose) {
		return getPrimaryTemplateInternal(documentType, purpose);
	}

	@Override
	public boolean setAsPrimaryTemplate(TemplateInstance template) {
		if (template == null) {
			return false;
		}
		// added parallel save and update of the old and new primary
		TemplateInstance oldPrimary = getPrimaryTemplate(template.getForType());
		List<GenericAsyncTask> tasks = new ArrayList<>(2);
		if (oldPrimary != null) {
			if (EqualsHelper.nullSafeEquals(template.getId(), oldPrimary.getId())
					&& EqualsHelper.nullSafeEquals(template.getIdentifier(), oldPrimary.getIdentifier())) {
				// no need to update the same template it's already the primary one
				return true;
			}
			oldPrimary.setPrimary(Boolean.FALSE);
			tasks.add(new TemplateSaveTask(oldPrimary));
			// should load the template content before DMS update
		}
		template.setPrimary(Boolean.TRUE);
		// change visibility to public
		template.setVisibleTo(null);

		tasks.add(new TemplateSaveTask(template));

		if (enableParallelTemplateSave.get().booleanValue()) {
			executor.execute(tasks);
		} else {
			for (GenericAsyncTask task : tasks) {
				try {
					task.call();
				} catch (Exception e) {
					throw new EmfRuntimeException("Failed to update template", e);
				}
			}
		}

		return true;
	}

	private void saveTemplateAndContent(TemplateInstance template) {
		if (!template.isContentLoaded()) {
			loadContent(template);
		}
		activateInternal(template);
	}

	@Override
	public TemplateInstance getTemplate(Serializable templateId) {
		try {
			templatesLock.readLock().lock();
			if (templateId instanceof Long) {
				return templateInstanceDao.loadInstance(templateId, null, true);
			} else if (templateId instanceof String) {
				return templateInstanceDao.loadInstance(null, templateId, true);
			}
		} finally {
			templatesLock.readLock().unlock();
		}
		// template id not supported or null
		return null;
	}

	@Override
	public TemplateInstance getDefaultTemplate() {
		TemplateInstance template = getTemplateWithContent(DEFAULT_TEMPLATE_ID);
		if (template == null) {
			template = new TemplateInstance();
			template.setPrimary(Boolean.FALSE);
			template.setForType("commonDocument");
			template.setId(Long.MAX_VALUE);
			template.add(DefaultProperties.CONTENT, DEFAULT_TEMPLATE_CONTENT);
			template.add(DefaultProperties.TITLE, labelProvider.getValue(TEMPLATES_DEFAULT_TITLE_ID));
		}
		return template;
	}

	@Override
	@RunAsSystem
	public void activate(TemplateInstance template) {
		Objects.requireNonNull(template, "template passed for activation must not be null");
		try {
			templatesLock.writeLock().lock();
			activateInternal(template);
		} finally {
			templatesLock.writeLock().unlock();
		}
	}

	@Override
	public Instance activate(String instanceId) {
		Objects.requireNonNull(instanceId, "Instance Id is required when activating a template");

		TimeTracker timeTracker = statistics.createTimeStatistics(getClass(), "activateTemplate").begin();
		Optional<InstanceReference> templateReference = instanceTypeResolver.resolveReference(instanceId);
		if (templateReference.isPresent()) {
			Instance templateInstance = templateReference.get().toInstance();
			boolean alreadyInStatusActive = STATUS_ACTIVE.equals(templateInstance.getString(DefaultProperties.STATUS));
			TemplateInstance templateData = convertInstanceToTemplateData(templateInstance);
			activate(templateData);
			// when performing activate on already active template instance, the resetExistingPrimary is not needed,
			// because it will find the template already loaded here (templateInstance) and will reset its primary flag
			if (templateData.getPrimary().booleanValue() && !alreadyInStatusActive) {
				Optional<Instance> existingPrimaryActive = findExistingPrimaryTemplate(templateData.getForType(),
						templateData.getPurpose(), STATUS_ACTIVE);
				if (existingPrimaryActive.isPresent()) {
					resetExistingPrimary(existingPrimaryActive.get());
				}
			}
			InstanceSaveContext context = InstanceSaveContext.create(templateInstance,
					new Operation(ActionTypeConstants.ACTIVATE_TEMPLATE));
			// save the instance, so that the state transition to active state is performed
			templateInstance = domainInstanceService.save(context);
			LOGGER.info("Template [{}] for type {} successfully activated for {} ms", templateInstance.getId(),
					templateData.getForType(), Long.valueOf(timeTracker.stop()));
			return templateInstance;
		}
		return null;
	}

	/**
	 * Converts an {@link Instance} extracted from the system to a {@link TemplateInstance} that is used just as a data
	 * carrier and easier manipulation. Also, extracts its content via {@link InstanceContentService}.
	 */
	private TemplateInstance convertInstanceToTemplateData(Instance instance) {
		TemplateInstance templateData = new TemplateInstance();
		templateData.add(DefaultProperties.TITLE, instance.get(DefaultProperties.TITLE));
		templateData.setForType(instance.getAsString(TemplateProperties.FOR_OBJECT_TYPE));
		templateData.setPurpose(instance.getAsString(TemplateProperties.TEMPLATE_PURPOSE));
		templateData.setPrimary(Boolean.valueOf(instance.getBoolean(TemplateProperties.IS_PRIMARY_TEMPLATE)));
		templateData.setCorrespondingInstance(instance.getId().toString());

		try {
			ContentInfo contentInfo = instanceContentService.getContent(instance, Content.PRIMARY_VIEW);
			if (!contentInfo.exists()) {
				throw new IllegalStateException("No view found for instance [" + instance.getId() + "]");
			}
			String content = IOUtils.toString(contentInfo.getInputStream());
			templateData.setContent(content);
			templateData.setDmsId(contentInfo.getRemoteId());
		} catch (IOException | IllegalStateException e) {
			LOGGER.error("Cannot load view for instance [{}]", instance.getId());
			throw new EmfApplicationException("Cannot extract content for '" + templateData.getForType() + "'", e);
		}
		return templateData;
	}

	private void activateInternal(TemplateInstance template) {

		setDefaultTemplateProperties(template);

		demoteOldPrimaryTemplate(template);

		preProcessor.process(new TemplateContext(template));

		BeforeTemplatePersistEvent event = null;
		if (template.getId() == null) {
			event = new BeforeTemplatePersistEvent(template);
			eventService.fire(event);
		}

		String dmsId = uploadToDms(template);
		if (dmsId != null) {
			template.setDmsId(dmsId);
		}

		templateInstanceDao.instanceUpdated(template, true);

		if (event != null) {
			eventService.fireNextPhase(event);
		}
		eventService.fire(new TemplatePersistedEvent(template, null));
		eventService.fire(new AuditableEvent(template.getOwningInstance(), ActionTypeConstants.SAVE_AS_TEMPLATE));
	}

	private void setDefaultTemplateProperties(TemplateInstance template) {
		// generate identifier if missing
		if (template.getIdentifier() == null) {
			Serializable serializable = template.getProperties().get(DefaultProperties.TITLE);
			if (serializable == null) {
				// last resort
				// we should remove the '-' because are not valid char in the XSD for id
				serializable = buildIdFromTitle(UUID.randomUUID().toString());
			} else {
				serializable = buildIdFromTitle(serializable.toString());
			}
			template.setIdentifier(serializable.toString());
		}
		if (template.getForType() == null) {
			template.setForType(TemplateProperties.DEFAULT_GROUP);
		}
		if (StringUtils.isNullOrEmpty(template.getPurpose())) {
			template.setPurpose(TemplatePurposes.CREATABLE);
		}
		String content = template.getContent();
		if (content != null && template.getTemplateDigest() == null) {
			template.setTemplateDigest(calculateDigest(content));
		}
		template.setPublicTemplate(Boolean.TRUE);
		if (template.getVisibleTo() == null && Boolean.FALSE.equals(template.getPrimary())) {
			template.setVisibleTo(getCurrentUser());
		}
	}

	private void demoteOldPrimaryTemplate(TemplateInstance template) {
		// Get the primary template only for the same purpose (creatable/uploadable). Otherwise, template for another
		// purpose may get overwritten
		TemplateInstance oldPrimary = getPrimaryTemplate(template.getForType(), template.getPurpose());
		if (oldPrimary != null && !EqualsHelper.nullSafeEquals(template.getIdentifier(), oldPrimary.getIdentifier())
				&& Boolean.TRUE.equals(template.getPrimary())) {
			oldPrimary.setPrimary(Boolean.FALSE);
			// make it public
			template.setVisibleTo(null);
			templateInstanceDao.persistChanges(oldPrimary);
		}
	}

	private List<TemplateInstance> getTemplatesInternal(String documentType, String purpose) {
		String query;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<>(TemplateProperties.GROUP_ID, documentType));
		if (StringUtils.isNotNullOrEmpty(purpose)) {
			query = TemplateEntity.QUERY_TEMPLATES_FOR_USER_GROUP_ID_PURPOSE_KEY;
			params.add(new Pair<>(TemplateProperties.PURPOSE, purpose));
		} else {
			query = TemplateEntity.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY;
		}
		List<Long> list = dbDao.fetchWithNamed(query, params);
		return templateInstanceDao.loadInstancesByDbKey(list);
	}

	private TemplateInstance getPrimaryTemplateInternal(String documentType, String purpose) {
		String query;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<>(TemplateProperties.GROUP_ID, documentType));
		if (StringUtils.isNotNullOrEmpty(purpose)) {
			query = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE_KEY;
			params.add(new Pair<>(TemplateProperties.PURPOSE, purpose));
		} else {
			query = TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY;
		}
		List<Long> list = dbDao.fetchWithNamed(query, params);
		List<TemplateInstance> instances = templateInstanceDao.loadInstancesByDbKey(list);
		if (instances.isEmpty()) {
			return null;
		}
		return instances.get(0);
	}

	private static String buildIdFromTitle(String string) {
		return FORBIDDEN_NAME_CHARACTERS.matcher(string).replaceAll("").toLowerCase();
	}

	private String getCurrentUser() {
		if (securityContext.isActive()) {
			return securityContext.getAuthenticated().getIdentityId();
		}
		return null;
	}

	/**
	 * Upload the given template instance to DMS. The method first generates a definition from the instance, converts it
	 * to JAXB compatible class and transforms it XML. The XML is uploaded to DMS. The method does not upload template
	 * that does not have a content.
	 *
	 * @param template
	 *            the template
	 * @return the new DMS id or update one, If <code>null</code> the file was not uploaded/updated in the DMS
	 */
	private String uploadToDms(TemplateInstance template) {
		if (template == null || template.getContent() == null) {
			// invalid template or missing content
			LOGGER.warn("Template not uploaded: no content!");
			return null;
		}

		TemplateDefinition definition = buildDefinition(template);

		com.sirma.itt.seip.template.jaxb.TemplateDefinition templateDefinition = mapper.map(definition,
				com.sirma.itt.seip.template.jaxb.TemplateDefinition.class);

		String xml = convertToXML(templateDefinition);

		try {
			Map<String, Serializable> properties = new LinkedHashMap<>(1, 1f);
			String fileName = templateDefinition.getId() + ".xml";
			final ByteArrayAndPropertiesDescriptor descriptor = new ByteArrayAndPropertiesDescriptor(fileName,
					xml.getBytes("utf-8"), template.getContainer(), properties);

			return defintionAdapterService.uploadDefinition(TemplateDefinition.class, descriptor);
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("", e);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to upload template {} to DMS due to {}", template.getIdentifier(), e.getMessage());
			LOGGER.debug("", e);
		}
		return null;
	}

	/**
	 * Convert to xml using JAXB.
	 *
	 * @param data
	 *            the data
	 * @return the string
	 */
	private static String convertToXML(Object data) {
		JAXBContext context;
		try {
			StringWriter writer = new StringWriter();
			context = JAXBContext.newInstance(com.sirma.itt.seip.template.jaxb.TemplateDefinition.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(data, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new DefinitionValidationException("Cannot generate template due to " + e.getMessage(), e);
		}
	}

	private TemplateDefinition buildDefinition(TemplateInstance template) {
		TemplateDefinitionImpl impl = new TemplateDefinitionImpl();
		impl.setContainer(template.getContainer());
		impl.setIdentifier(template.getIdentifier());
		List<PropertyDefinition> fields = impl.getFields();
		for (Entry<String, Serializable> entry : template.getProperties().entrySet()) {
			fields.add(createField(entry.getKey(), entry.getValue()));
		}
		return impl;
	}

	private PropertyDefinition createField(String key, Serializable value) {
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setIdentifier(key);
		// set proper type depending on the value
		if (value instanceof String) {
			if (value.toString().length() < 1024) {
				impl.setType("an.." + value.toString().length());
			} else {
				impl.setType("ANY");
			}
		} else if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				impl.setType("n.." + value.toString().length() + ",5");
			} else {
				impl.setType("n..10");
			}
		} else if (value instanceof Boolean) {
			impl.setType("boolean");
		} else if (value instanceof Date) {
			impl.setType("datetime");
		} else if (value instanceof Resource) {
			impl.setType("an..50");
			ControlDefinition controlDefinition = new ControlDefinitionImpl();
			controlDefinition.setIdentifier("USER");
			impl.setControlDefinition(controlDefinition);
		} else {
			// this is set not to miss the definition type and later to become an invalid xml
			LOGGER.warn("Unrecongnized type when creating definition. Setting it to ANY");
			impl.setType("ANY");
		}

		impl.setDisplayType(DisplayType.SYSTEM);
		impl.setMandatory(Boolean.FALSE);
		// convert values to string depending on the type
		impl.setValue(typeConverter.convert(String.class, value));
		// fill the remaining default properties if any
		impl.setDefaultProperties();
		return impl;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean delete(Serializable data) {
		Entity<Serializable> instance = null;
		if (data instanceof String || data instanceof Long) {
			instance = getTemplate(data);
		} else if (data instanceof TemplateInstance || data instanceof TemplateEntity) {
			instance = (Entity<Serializable>) data;
		}
		if (instance != null) {
			templateInstanceDao.delete(instance);
			return true;
		}
		return false;
	}

	@Override
	public String create(TemplateInstance template) {
		Serializable sourceId = template.getOwningInstance().getId();
		Objects.requireNonNull(sourceId, "Source instance Id is required when creating a new template");
		Objects.requireNonNull(template.getForType(), "forType is required when creating a new template");

		String sourceInstanceContent = getParsedContent(sourceId.toString());
		return createTemplateInstanceInternal(template, sourceInstanceContent);
	}

	private String createTemplateInstanceInternal(TemplateInstance templateData, String content) {
		TimeTracker timeTracker = statistics.createTimeStatistics(getClass(), "createNewTemplateInstance").begin();

		// the corresponding library will be set as a parent of the template
		String parent = getSemanticClass(templateData.getForType());
		Instance templateInstance = domainInstanceService.createInstance(TemplateProperties.TEMPLATE_DEFINITION_ID,
				parent);

		boolean enforcePrimaryTemplate = false;
		Optional<Instance> existingPrimaryTemplate = findExistingPrimaryTemplate(templateData.getForType(),
				templateData.getPurpose(), "");
		if (!existingPrimaryTemplate.isPresent() && Boolean.FALSE.equals(templateData.getPrimary())) {
			LOGGER.debug(
					"No primary template exists for [{}] - {} yet. Newly created [{}] will be forcibly set as primary",
					templateData.getForType(), templateData.getPurpose(), templateInstance.getId());
			enforcePrimaryTemplate = true;
		}
		populateTemplateProperties(templateData, templateInstance, content, enforcePrimaryTemplate);

		InstanceSaveContext context = InstanceSaveContext.create(templateInstance,
				new Operation(ActionTypeConstants.CREATE));
		Instance savedInstance = domainInstanceService.save(context);
		LOGGER.info("Template instance for type [{}] successfully created for {} ms", templateData.getForType(),
				Long.valueOf(timeTracker.stop()));
		return savedInstance.getId().toString();
	}

	private String getSemanticClass(String forType) {
		if (URIUtil.isValidURIReference(forType)) {
			// it is possible for a semantic URI to be passed as forType. In this case, no further processing is
			// needed.
			return forType;
		}
		DefinitionModel definitionModel = dictionaryService.find(forType);
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

	private void resetExistingPrimary(Instance template) {
		LOGGER.debug(
				"Setting template [{}] to be secondary (alternative). New primary template was created for that type and purpose",
				template.getId());
		template.add(TemplateProperties.IS_PRIMARY_TEMPLATE, Boolean.FALSE);
		domainInstanceService
				.save(InstanceSaveContext.create(template, Operation.NO_OPERATION));
	}

	private Optional<Instance> findExistingPrimaryTemplate(String forType, String purpose, String status) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setFilterOutLatestRevisions(false);
		arguments.setFaceted(false);
		// there can be only one primary template per type & purpose
		arguments.setPageSize(1);
		arguments.setSkipCount(0);
		Map<String, Serializable> properties = CollectionUtils.createHashMap(4);
		properties.put(DefaultProperties.SEMANTIC_TYPE, TemplateProperties.TEMPLATE_CLASS_ID);
		properties.put(TemplateProperties.EMF_FOR_OBJECT_TYPE, forType);
		properties.put(TemplateProperties.EMF_PRIMARY_TEMPLATE, Boolean.TRUE);
		properties.put(TemplateProperties.EMF_TEMPLATE_PURPOSE, purpose);
		if (StringUtils.isNotNullOrEmpty(status)) {
			properties.put(DefaultProperties.EMF_STATUS, status);
		}
		arguments.setArguments(properties);
		// we search and load the instance, because version is needed later when saving it
		searchService.searchAndLoad(Instance.class, arguments);
		return arguments.getResult().stream().findFirst();
	}

	private static void populateTemplateProperties(TemplateInstance source, Instance destination, String content,
			boolean enforcePrimary) {
		destination.add(DefaultProperties.TEMP_CONTENT_VIEW, content);
		destination.add(DefaultProperties.TITLE, source.get(DefaultProperties.TITLE));
		destination.add(TemplateProperties.EMF_FOR_OBJECT_TYPE, source.getForType());
		destination.add(TemplateProperties.EMF_TEMPLATE_PURPOSE, source.getPurpose());
		if (enforcePrimary) {
			destination.add(TemplateProperties.IS_PRIMARY_TEMPLATE, Boolean.TRUE);
		} else {
			destination.add(TemplateProperties.IS_PRIMARY_TEMPLATE, source.getPrimary());
		}
	}

	private String getParsedContent(String instanceId) {
		try {
			String rawContent = extractViewContent(instanceId);
			Idoc parsedContent = parseViewContent(rawContent);
			return parsedContent.asHtml();
		} catch (IllegalStateException | IOException e) {
			LOGGER.error("Cannot load view for instance '{}'", instanceId);
			throw new EmfApplicationException("Could not extract content for source instance '" + instanceId + "'", e);
		}
	}

	private static Idoc parseViewContent(String content) {
		Idoc idoc = Idoc.parse(content);
		idoc.widgets()
				.filter(widget -> StringUtils.isNullOrEmpty(widget.getId()))
				.forEach(widget -> widget.addProperty(ContentNode.ELEMENT_ID, RandomStringUtils.randomAlphanumeric(8)));
		idoc.getSections()
				.stream()
				.filter(section -> StringUtils.isNullOrEmpty(section.getId()))
				.forEach(sectionNode -> sectionNode.addProperty(SectionNode.SECTION_NODE_ID_KEY, RandomStringUtils.randomAlphanumeric(8)));
		return idoc;
	}

	private String extractViewContent(String sourceId) throws IOException {
		ContentInfo contentInfo = instanceContentService.getContent(sourceId, Content.PRIMARY_VIEW);
		if (!contentInfo.exists()) {
			throw new IllegalStateException("No view found for instance '" + sourceId + "'");
		}
		return IOUtils.toString(contentInfo.getInputStream());
	}

	@Override
	public void reload() {
		try {
			templatesLock.writeLock().lock();
			// clear cache before reload
			templateInstanceDao.clearInternalCache();
			reloadInternal(true);
			eventService.fire(new TemplatesSynchronizedEvent());
		} finally {
			templatesLock.writeLock().unlock();
		}
	}

	/**
	 * Reload all templates from the server. The method could also force file synchronization with local repository. The
	 * method removes all invalid templates that are not found after the full synchronization.
	 * 
	 * @param activateLoadedTemplates
	 *            if the loaded templates have to be activated.
	 */
	private void reloadInternal(boolean activateLoadedTemplates) {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<? extends TemplateDefinition> templates = compiler.compileDefinitions(definitionCompilerCallback, false);

		Set<String> dmsIds = CollectionUtils.createLinkedHashSet(templates.size());
		Map<String, Serializable> map = processNewTemplates(tracker, templates, dmsIds, false, activateLoadedTemplates);

		deleteNotUsedTemplates(dmsIds, map.keySet());

		// not implemented, yet
		// fixMissingPrimaryTemplate

		templateInstanceDao.clearInternalCache();
	}

	/**
	 * Deletes all templates that not match any of the given DMS or template ids.
	 *
	 * @param dmsIds
	 *            the dms ids not to remove
	 * @param templateIds
	 *            the template ids not to remove
	 */
	private void deleteNotUsedTemplates(Set<String> dmsIds, Set<String> templateIds) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		List<TemplateEntity> listByDmsId = Collections.emptyList();
		if (!dmsIds.isEmpty()) {
			args.add(new Pair<>("dmsId", dmsIds));
			listByDmsId = dbDao.fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY, args);

			args.clear();
		}
		List<TemplateEntity> listByTemplateId;
		if (!templateIds.isEmpty()) {
			args.add(new Pair<>(TEMPLATE_ID, templateIds));
			listByTemplateId = dbDao.fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, args);
		} else {
			args.add(new Pair<>(TEMPLATE_ID, ALL_TEMPLATES_IDS));
			listByTemplateId = dbDao.fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, args);
		}

		Set<Long> processed = CollectionUtils.createHashSet(listByDmsId.size() + listByTemplateId.size());
		deleteTemplates(listByTemplateId, processed);

		deleteTemplates(listByDmsId, processed);
	}

	/**
	 * Delete templates from the given list and tracks not to delete a template more than once using the given set of
	 * processed id if called multiple times with different lists.
	 *
	 * @param templates
	 *            the list by templates to delete
	 * @param processed
	 *            the already processed templates
	 */
	private void deleteTemplates(List<TemplateEntity> templates, Set<Long> processed) {
		for (TemplateEntity templateEntity : templates) {
			if (processed.contains(templateEntity.getId())) {
				continue;
			}
			processed.add(templateEntity.getId());

			LOGGER.debug("Deleting obsolete template with db id={} template id={} and dms id={}",
					templateEntity.getId(), templateEntity.getTemplateId(), templateEntity.getDmsId());

			delete(templateEntity);
		}
	}

	/**
	 * Process the given list of templates with option to force local repository synchronization and to return the
	 * content mapped by template id. The method also tries to detect if a template has been moved to other file or
	 * template id has been changed.
	 *
	 * @param <T>
	 *            the generic type
	 * @param tracker
	 *            the tracker
	 * @param templates
	 *            the templates
	 * @param dmsIds
	 *            the DMS ids that corresponds to the given templates. This is not an argument but rather a return
	 *            parameter. If not <code>null</code> then all DMS ids of the processed templates will be written here.
	 * @param returnContents
	 *            the return contents
	 * @param activateProcessedTemplates
	 *            indicates if the processed templates will be activated (via {@link TemplateService#activate(String)})
	 *            and new instances will be created for each one. Instances are created only if they are missing in the
	 *            system. The correspondingInstance field in the xml is checked for that. Flag is usually passed as true
	 *            when importing templates.
	 * @return the map with the content mapped by template id. The map will have <code>null</code> values if the
	 *         returnContents parameter is set to <code>false</code>
	 */
	private <T extends TemplateDefinition> Map<String, Serializable> processNewTemplates(TimeTracker tracker,
			List<T> templates, Set<String> dmsIds, boolean returnContents,
			boolean activateProcessedTemplates) {
		int updated = 0;
		Map<String, Serializable> contents = CollectionUtils.createHashMap(templates.size());
		List<String> idsForActivation = new LinkedList<>();
		for (T template : templates) {
			TemplateInstance instance = templateInstanceDao.createInstance(template, true);

			Serializable content = instance.get(DefaultProperties.CONTENT);
			if (!(content instanceof String)) {
				LOGGER.warn("The template '{}' does not have a content field with valid data. Template will ignored!",
						template.getIdentifier());
				continue;
			}
			// copy the original template id in the template instance
			instance.setDmsId(template.getDmsId());

			if (activateProcessedTemplates) {
				// we currently don't support template idoc compilation for emails
				boolean needsInstanceCreated = !EMAIL_TEMPLATE_TYPE.equals(instance.getForType());
				if (needsInstanceCreated) {
					// added separate transaction because when create instances the check for previous is not correct
					String correspondingInstanceId = transactionSupport
							.invokeInNewTx(() -> createCorrespondingInstance(instance, content.toString()));
					if (StringUtils.isNotNullOrEmpty(correspondingInstanceId)) {
						idsForActivation.add(correspondingInstanceId);
					}
				} else {
					activate(instance);
				}
			}
			if (returnContents) {
				contents.put(instance.getIdentifier(), content);
			} else {
				contents.put(instance.getIdentifier(), null);
			}
		}

		if (!idsForActivation.isEmpty()) {
				scheduleTemplatesForActivation(idsForActivation);
		}
		LOGGER.debug("Templates reload finished in {} ms updating {} templates",
				tracker != null ? tracker.stop() : "unknown", updated);
		return contents;
	}

	private void scheduleTemplatesForActivation(List<String> idsForActivation) {
		SchedulerContext schedulerContext = TemplateActivateScheduler.createExecutorContext(idsForActivation);
		SchedulerConfiguration configuration = schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		configuration.setScheduleTime(new Date())
				.setRemoveOnSuccess(true)
				.setSynchronous(false)
				.setPersistent(true)
				.setMaxRetryCount(5)
				.setRetryDelay(Long.valueOf(60))
				.setRunAs(RunAs.ADMIN);
		LOGGER.debug("Scheduling {} templates for activation", Long.valueOf(idsForActivation.size()));
		schedulerService.schedule(TemplateActivateScheduler.BEAN_ID, configuration, schedulerContext);
	}

	private String createCorrespondingInstance(TemplateInstance instance, String content) {
		try {
			String correspondingInstanceId = instance.getCorrespondingInstance();
			if (StringUtils.isNullOrEmpty(correspondingInstanceId)) {
				return createInstanceAndUploadToDms(instance, content);
			}
			Optional<InstanceReference> existingInstance = instanceTypeResolver
					.resolveReference(correspondingInstanceId);
			if (!existingInstance.isPresent()) {
				// Since a new corresponding instance ID will be generated when creating it, it has to go to the DMS as
				// well
				return createInstanceAndUploadToDms(instance, content);
			}
			// corresponding instance already exists in the system. Just return its ID for further processing
			return correspondingInstanceId;
		} catch (IllegalArgumentException e) {
			// skip the templates that don't have a valid forType so that the import process in not
			// interrupted
			LOGGER.warn("Template [{}] has invalid forType [{}] and will be skipped. Reason: {} ",
					instance.getIdentifier(), instance.getForType(), e.getMessage());
		}
		return null;
	}

	private String createInstanceAndUploadToDms(TemplateInstance instance, String content) {
		String newContent = parseViewContent(content).asHtml();
		String correspondingInstanceId = createTemplateInstanceInternal(instance, newContent);
		instance.setCorrespondingInstance(correspondingInstanceId);
		instance.setContent(newContent);
		uploadToDms(instance);
		LOGGER.debug("Uploaded to DMS newly created corresponding instance [{}] for type {}", correspondingInstanceId,
				instance.getForType());
		return correspondingInstanceId;
	}

	/**
	 * Download the file from DMS.
	 *
	 * @param instance
	 *            the instance
	 * @return the input stream to the file to <code>null</code> if failed
	 */
	private InputStream downloadContentFromDms(TemplateInstance instance) {
		try {

			TimeTracker tracker = TimeTracker.createAndStart();

			FileDescriptor descriptor = contentAdapterService.getContentDescriptor(instance);
			if (descriptor == null) {
				LOGGER.warn("Could not fetch the template content for: {}", instance);
				return null;
			}
			DefinitionCompilerCallback<TemplateDefinitionImpl> callback = new TemplateDefinitionCompilerCallbackProxy(
					compilerHelper, descriptor);
			List<TemplateDefinitionImpl> list = compiler.compileDefinitions(callback, false);

			Map<String, Serializable> templates = null;
			if (list.isEmpty()) {
				// we failed to download the file/file was invalid/file was missing(deleted from
				// DMS) - force file synchronization
				// the method will also clean any invalid templates
				securityContextManager.executeAsAdmin().consumer(this::reloadInternal, false);
				// we will try to load the file again bellow
			} else {
				// process the results and try to load the content
				templates = processNewTemplates(tracker, list, null, true, false);
			}

			// try to fetch the content from the returned map
			if (templates != null) {
				Serializable content = templates.get(instance.getIdentifier());
				if (content != null) {
					return new ByteArrayInputStream(content.toString().getBytes("UTF-8"));
				}
			}
			// if still not found well the template is broken or deleted we can't do anything more
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug(e.getMessage());
			LOGGER.trace("", e);
		} catch (Exception e) {
			LOGGER.warn("Could not reload templates", e);
		}
		return null;
	}

	/**
	 * Calculate digest.
	 *
	 * @param content
	 *            the content
	 * @return the string
	 */
	private static String calculateDigest(Serializable content) {
		if (content == null) {
			return null;
		}
		return DigestUtils.calculateDigest(content.toString());
	}

	/**
	 * Async task for loading content of a template and updating the content back to DMS/local store
	 *
	 * @author BBonev
	 */
	private class TemplateSaveTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 1223243504036243210L;
		/** The template. */
		private final TemplateInstance template;

		/**
		 * Instantiates a new template save task.
		 *
		 * @param template
		 *            the template
		 */
		public TemplateSaveTask(TemplateInstance template) {
			this.template = template;
		}

		@Override
		protected boolean executeTask() throws Exception {
			saveTemplateAndContent(template);
			return true;
		}
	}
}
