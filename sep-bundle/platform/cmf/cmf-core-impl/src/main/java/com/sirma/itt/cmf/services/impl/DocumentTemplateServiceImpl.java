package com.sirma.itt.cmf.services.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.ByteArrayAndPropertiesDescriptor;
import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.beans.definitions.compile.TemplateDefinitionCompilerCallbackProxy;
import com.sirma.itt.cmf.beans.definitions.impl.TemplateDefinitionImpl;
import com.sirma.itt.cmf.beans.entity.TemplateEntity;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.template.BeforeTemplatePersistEvent;
import com.sirma.itt.cmf.event.template.TemplateChageEvent;
import com.sirma.itt.cmf.event.template.TemplateOpenEvent;
import com.sirma.itt.cmf.event.template.TemplatePersistedEvent;
import com.sirma.itt.cmf.exceptions.DuplicateIdentifierException;
import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.cmf.services.adapter.CMFContentAdapterService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.compile.DefinitionCompiler;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.DefinitionValidationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.template.TemplateProperties;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.DigestUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Implementation of {@link DocumentServiceImpl}.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTemplateServiceImpl.class);

	/** The forbidden characters for template identifier */
	private static final Pattern FORBIDDEN_NAME_CHARACTERS = Pattern.compile("\\s|\\W");
	/** The file provider. */
	@Inject
	private TempFileProvider fileProvider;

	/** The definition compiler callback. */
	@Inject
	@DefinitionType(ObjectTypesCmf.TEMPLATE)
	private DefinitionCompilerCallback<TemplateDefinition> definitionCompilerCallback;

	/** The adapter service. */
	@Inject
	private CMFContentAdapterService contentAdapterService;

	/** The defintion adapter service. */
	@Inject
	private DMSDefintionAdapterService defintionAdapterService;

	/** The compiler. */
	@Inject
	private DefinitionCompiler compiler;

	/** The compiler helper. */
	@Inject
	private DefinitionCompilerHelper compilerHelper;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.TEMPLATE)
	private InstanceDao<TemplateInstance> instanceDao;

	/** The mapper. */
	@Inject
	private DozerMapper mapper;

	/** The autentication. */
	@Inject
	private Instance<AuthenticationService> autentication;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The event service. */
	@Inject
	private EventService eventService;

	@Inject
	private TaskExecutor executor;

	/** The enable parallel template save. */
	@Inject
	@Config(name = CmfConfigurationProperties.TEMPLATES_PARALLEL_SAVE, defaultValue = "false")
	private Boolean enableParallelTemplateSave;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public TemplateInstance getTemplateWithContent(Serializable templateId) {
		TemplateInstance instance = getTemplate(templateId);
		if (instance == null) {
			return null;
		}
		return loadContent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public TemplateInstance loadContent(TemplateInstance instance) {
		if (instance == null) {
			return null;
		}
		// read the content from local store or load the file from DMS if missing in cache
		try (InputStream inputStream = readContentFromLocalStore(instance)) {
			String content = null;
			if (inputStream != null) {
				content = IOUtils.toString(inputStream);
			}
			if (content == null) {
				content = getDefaultTemplateContent();
				// if we couldn't manage to find the template in the DMS then it's probably deleted
				// so we should remove the local information about it.
				delete(instance);
			}

			// set loaded content
			instance.getProperties().put(TemplateProperties.CONTENT, content);
			instance.getProperties().put(TemplateProperties.IS_CONTENT_LOADED, Boolean.TRUE);

			// notify that a template is loaded and is going to be returned to the user.
			eventService.fire(new TemplateOpenEvent(instance));
		} catch (IOException e) {
			LOGGER.warn("Failed to read template content due to: " + e.getMessage());
			LOGGER.debug("", e);
		}
		return instance;
	}

	/**
	 * Gets the default template content.
	 *
	 * @return the default template content
	 */
	protected String getDefaultTemplateContent() {
		return "<br/>";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TemplateInstance> getAllTemplates(String user) {
		String query = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER_KEY;
		List<Pair<String, Object>> params = new ArrayList<>(1);
		if (user == null) {
			params.add(new Pair<String, Object>("users", Arrays.asList("system", "<no_user>")));
		} else {
			params.add(new Pair<String, Object>("users", user));
		}
		List<Long> list = dbDao.fetchWithNamed(query, params);
		return instanceDao.loadInstancesByDbKey(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TemplateInstance> getTemplates(String documentType) {
		String query = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>("groupId", documentType));
		List<Long> list = dbDao.fetchWithNamed(query, params);
		return instanceDao.loadInstancesByDbKey(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemplateInstance getPrimaryTemplate(String documentType) {
		String query = DbQueryTemplates.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY;
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<String, Object>("groupId", documentType));
		List<Long> list = dbDao.fetchWithNamed(query, params);
		List<TemplateInstance> instances = instanceDao.loadInstancesByDbKey(list);
		if (instances.isEmpty()) {
			return null;
		}
		return instances.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public boolean setAsPrimaryTemplate(TemplateInstance template) {
		if (template == null) {
			return false;
		}
		// added parallel save and update of the old and new primary
		TemplateInstance oldPrimary = getPrimaryTemplate(template.getGroupId());
		List<GenericAsyncTask> tasks = new ArrayList<>(2);
		if (oldPrimary != null) {
			if (EqualsHelper.nullSafeEquals(template.getId(), oldPrimary.getId())
					&& EqualsHelper.nullSafeEquals(template.getIdentifier(),
							oldPrimary.getIdentifier())) {
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

		if (enableParallelTemplateSave) {
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

	/**
	 * Save template and content.
	 *
	 * @param template
	 *            the template
	 */
	private void saveTemplateAndContent(TemplateInstance template) {
		if (!template.isContentLoaded()) {
			loadContent(template);
		}
		saveInternal(template, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemplateInstance getTemplate(Serializable templateId) {
		if (templateId instanceof Long) {
			return instanceDao.loadInstance(templateId, null, true);
		} else if (templateId instanceof String) {
			return instanceDao.loadInstance(null, templateId, true);
		}
		// template id not supported or null
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public boolean save(TemplateInstance template, boolean asPublic) {
		if (template == null) {
			return false;
		}
		return saveInternal(template, asPublic);
	}

	/**
	 * Save internal.
	 *
	 * @param template
	 *            the template
	 * @param asPublic
	 *            the as public
	 * @return true, if successful
	 */
	private boolean saveInternal(TemplateInstance template, boolean asPublic) {
		boolean isNewTemplate = template.getId() == null;

		// generate identifier if missing
		if (template.getIdentifier() == null) {
			Serializable serializable = template.getProperties().get(TemplateProperties.TITLE);
			if (serializable == null) {
				// last resort
				// we should remove the '-' because are not valid char in the XSD for id
				serializable = buildIdFromTitle(UUID.randomUUID().toString());
			} else {
				serializable = buildIdFromTitle(serializable.toString());
			}
			template.setIdentifier(serializable.toString());
		}

		if (template.getContainer() == null) {
			template.setContainer(getContainer());
		}

		// set default group if missing
		if (template.getGroupId() == null) {
			template.setGroupId(TemplateProperties.DEFAULT_GROUP);
		}

		// check if the template data is unique for now
		if (isNewTemplate && !checkIfTemplateIdIsUnique(template)) {
			throw new DuplicateIdentifierException("The template id '" + template.getIdentifier()
					+ "' for template with title '"
					+ template.getProperties().get(TemplateProperties.TITLE)
					+ "' is already taken!");
		}

		String content = template.getContent();
		if ((content != null) && (template.getTemplateDigest() == null)) {
			template.setTemplateDigest(calculateDigest(content));
		}
		template.setPublicTemplate(asPublic);
		if (!asPublic && (template.getVisibleTo() == null)
				&& Boolean.FALSE.equals(template.getPrimary())) {
			template.setVisibleTo(getCurrentUser());
		}

		TemplateInstance oldPrimary = getPrimaryTemplate(template.getGroupId());
		if ((oldPrimary != null)
				&& !EqualsHelper.nullSafeEquals(template.getId(), oldPrimary.getId())
				&& Boolean.TRUE.equals(template.getPrimary())) {
			oldPrimary.setPrimary(Boolean.FALSE);
			// make it public
			template.setVisibleTo(null);
			// save the old template
			saveTemplateAndContent(oldPrimary);
		}

		eventService.fire(new TemplateChageEvent(template));
		BeforeTemplatePersistEvent event = null;
		if (isNewTemplate) {
			event = new BeforeTemplatePersistEvent(template);
			eventService.fire(event);
		}

		boolean result = false;
		if (template.getContent() == null) {
			// if the content is not present then save only the properties
			// no need to call the DMS
			instanceDao.instanceUpdated(template, true);
			result = true;
		} else {
			String dmsId = uploadToDms(template);
			if (dmsId != null) {
				template.setDmsId(dmsId);

				instanceDao.instanceUpdated(template, true);
				result = saveContentToLocalStore(template, content);
			}
		}
		// fire exit events
		if (result) {
			if (event != null) {
				eventService.fireNextPhase(event);
			}
			eventService.fire(new TemplatePersistedEvent(template, null));
		}

		return result;
	}

	/**
	 * Check if template id is unique. The criteria is {@link TemplateInstance#getIdentifier()} and
	 * {@link TemplateInstance#getContainer()}
	 *
	 * @param template
	 *            the template
	 * @return <code>true</code>, if the template is unique
	 */
	private boolean checkIfTemplateIdIsUnique(TemplateInstance template) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>("templateId", template.getIdentifier()));
		args.add(new Pair<String, Object>("container", template.getContainer()));
		List<Object> list = dbDao.fetchWithNamed(
				DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER_KEY, args);
		return list.isEmpty();
	}

	/**
	 * Builds the id from title.
	 *
	 * @param string
	 *            the string
	 * @return the string
	 */
	private String buildIdFromTitle(String string) {
		return FORBIDDEN_NAME_CHARACTERS.matcher(string).replaceAll("").toLowerCase();
	}

	/**
	 * Gets the current user.
	 *
	 * @return the current user
	 */
	private String getCurrentUser() {
		return null;
	}

	/**
	 * Upload the given template instance to DMS. The method first generates a definition from the
	 * instance, converts it to JAXB compatible class and transforms it XML. The XML is uploaded to
	 * DMS. The method does not upload template that does not have a content.
	 *
	 * @param template
	 *            the template
	 * @return the new DMS id or update one, If <code>null</code> the file was not uploaded/updated
	 *         in the DMS
	 */
	private String uploadToDms(TemplateInstance template) {
		if ((template == null) || (template.getContent() == null)) {
			// invalid template or missing content
			LOGGER.warn("Template not uploaded: no content!");
			return null;
		}

		TemplateDefinition definition = buildDefinition(template);

		com.sirma.itt.cmf.beans.jaxb.TemplateDefinition templateDefinition = mapper.getMapper()
				.map(definition, com.sirma.itt.cmf.beans.jaxb.TemplateDefinition.class);

		String xml = convertToXML(templateDefinition);

		try {
			Map<String, Serializable> properties = new LinkedHashMap<>(1, 1f);
			String fileName = templateDefinition.getId() + ".xml";
			final ByteArrayAndPropertiesDescriptor descriptor = new ByteArrayAndPropertiesDescriptor(
					fileName, xml.getBytes("utf-8"), template.getContainer(), properties);

			return SecurityContextManager.callAsSystem(new Callable<String>() {

				@Override
				public String call() throws Exception {
					String dmsId = defintionAdapterService.uploadDefinition(
							TemplateDefinition.class, descriptor);
					return dmsId;
				}
			});
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("", e);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to upload template {} to DMS due to {}", template.getIdentifier(),
					e.getMessage());
			LOGGER.debug("", e);
		}
		return null;
	}

	/**
	 * Gets the container.
	 *
	 * @return the container
	 */
	private String getContainer() {
		return SecurityContextManager.getCurrentContainer(autentication);
	}

	/**
	 * Convert to xml using JAXB.
	 *
	 * @param data
	 *            the data
	 * @return the string
	 */
	public String convertToXML(Object data) {
		JAXBContext context;
		try {
			StringWriter writer = new StringWriter();
			context = JAXBContext
					.newInstance(com.sirma.itt.cmf.beans.jaxb.TemplateDefinition.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(data, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new DefinitionValidationException("Cannot generate template due to "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Builds a definition from the given instance
	 *
	 * @param template
	 *            the template
	 * @return the template definition
	 */
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

	/**
	 * Creates the field for the given key and value
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the property definition
	 */
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
			if ((value instanceof Double) || (value instanceof Float)) {
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
		impl.setMandatory(false);
		// convert values to string depending on the type
		impl.setValue(typeConverter.convert(String.class, value));
		// fill the remaining default properties if any
		impl.setDefaultProperties();
		return impl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(Serializable data) {
		Entity<Serializable> instance = null;
		if ((data instanceof String) || (data instanceof Long)) {
			instance = getTemplate(data);
		} else if ((data instanceof TemplateInstance) || (data instanceof TemplateEntity)) {
			instance = (Entity<Serializable>) data;
		}
		if (instance != null) {
			instanceDao.delete(instance);
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reload() {
		// clear cache before reload
		instanceDao.clearInternalCache();

		reloadInternal(false);
	}

	/**
	 * Reload all templates from the server. The method could also force file synchronization with
	 * local repository. The method removes all invalid templates that are not found after the full
	 * synchronization.
	 *
	 * @param forceFilePersist
	 *            the force file persist to local repository
	 */
	private void reloadInternal(boolean forceFilePersist) {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<TemplateDefinition> templates = compiler.compileDefinitions(
				definitionCompilerCallback, false);

		Set<String> dmsIds = CollectionUtils.createLinkedHashSet(templates.size());
		Map<String, Serializable> map = processNewTemplates(tracker, templates, dmsIds,
				forceFilePersist, false);

		deleteNotUsedTemplates(dmsIds, map.keySet());

		// not implemented, yet
		// fixMissingPrimaryTemplate

		instanceDao.clearInternalCache();
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
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		List<TemplateEntity> listByDmsId = Collections.emptyList();
		if (!dmsIds.isEmpty()) {
			args.add(new Pair<String, Object>("dmsId", dmsIds));
			listByDmsId = dbDao.fetchWithNamed(
					DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY, args);

			args.clear();
		}
		List<TemplateEntity> listByTemplateId = Collections.emptyList();
		if (!templateIds.isEmpty()) {
			args.add(new Pair<String, Object>("templateId", templateIds));
			listByTemplateId = dbDao.fetchWithNamed(
					DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, args);
		}

		Set<Long> processed = CollectionUtils.createHashSet(listByDmsId.size()
				+ listByTemplateId.size());
		deleteTemplates(listByTemplateId, processed);

		deleteTemplates(listByDmsId, processed);
	}

	/**
	 * Delete templates from the given list and tracks not to delete a template more than once using
	 * the given set of processed id if called multiple times with different lists.
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
					templateEntity.getId(), templateEntity.getTemplateId(),
					templateEntity.getDmsId());

			delete(templateEntity);
		}
	}

	/**
	 * Process the given list of templates with option to force local repository synchronization and
	 * to return the content mapped by template id. The method also tries to detect if a template
	 * has been moved to other file or template id has been changed.
	 *
	 * @param <T>
	 *            the generic type
	 * @param tracker
	 *            the tracker
	 * @param templates
	 *            the templates
	 * @param dmsIds
	 *            the DMS ids that corresponds to the given templates. This is not an argument but
	 *            rather a return parameter. If not <code>null</code> then all DMS ids of the
	 *            processed templates will be written here.
	 * @param forceFilePersist
	 *            the force file persist
	 * @param returnContents
	 *            the return contents
	 * @return the map with the content mapped by template id. The map will have <code>null</code>
	 *         values if the returnContents parameter is set to <code>false</code>
	 */
	private <T extends TemplateDefinition> Map<String, Serializable> processNewTemplates(
			TimeTracker tracker, List<T> templates, Set<String> dmsIds, boolean forceFilePersist,
			boolean returnContents) {
		int updated = 0;
		Map<String, Serializable> contens = CollectionUtils.createHashMap(templates.size());

		for (T template : templates) {
			TemplateInstance instance = instanceDao.createInstance(template, true);
			// copy the original template id in the template instance
			instance.setDmsId(template.getDmsId());
			// remove content not to be stored in the DB but in external temporary file
			Serializable content = instance.getProperties().remove(TemplateProperties.CONTENT);
			if (!(content instanceof String)) {
				LOGGER.warn(
						"The template {} does not have a content field with valid data. Template will ignored!",
						template.getIdentifier());
				continue;
			}
			if (returnContents) {
				contens.put(instance.getIdentifier(), content);
			} else {
				contens.put(instance.getIdentifier(), null);
			}
			TemplateInstance existing = instanceDao.loadInstance(null, template.getIdentifier(),
					true);

			if ((dmsIds != null) && (template.getDmsId() != null)) {
				dmsIds.add(template.getDmsId());
			}
			if (existing == null) {
				// if not found by identifier check by DMS id in case we have modified the
				// identifier
				existing = instanceDao.loadInstance(null, template.getDmsId(), true);
				if (existing == null) {
					instance.setTemplateDigest(calculateDigest(content));
					instanceDao.persistChanges(instance);
					saveContentToLocalStore(instance, content);
					LOGGER.debug("Added new template " + template.getIdentifier());
					continue;
				}
				if (!returnContents) {
					// return the old id not to delete it accidentally
					contens.put(instance.getIdentifier(), null);
				}
				LOGGER.info(
						"Detected template identifier change from {} to {} in file with DMS id={}",
						existing.getIdentifier(), template.getContainer(), template.getDmsId());
				// else if found by DMS is means we have changed the template identifier
				existing.setIdentifier(template.getIdentifier());
			} else if (!EqualsHelper.nullSafeEquals(existing.getDmsId(), template.getDmsId())) {
				if (dmsIds != null) {
					// backup the id - this id going to be obsolete but when removing not used ids
					// we will better be sure that we not delete actual entry due to not flushed
					// transaction
					dmsIds.add(existing.getDmsId());
				}
				LOGGER.info("Detected moved template {} from DMS file {} to {}",
						template.getIdentifier(), existing.getDmsId(), template.getDmsId());
				// template has been moved to a new file (deleted and added again)
				if (template.getDmsId() != null) {
					existing.setDmsId(template.getDmsId());
				}
			}
			// merge properties but not the primary flag
			boolean currentIsPrimary = Boolean.TRUE.equals(existing.getPrimary());
			// these properties are not needed to be merged
			instance.getProperties().remove(DefaultProperties.CREATED_ON);
			instance.getProperties().remove(DefaultProperties.CREATED_BY);

			existing.getProperties().putAll(instance.getProperties());
			existing.setPrimary(currentIsPrimary);

			// check for changes in the new content
			String newDigest = calculateDigest(content);
			if (forceFilePersist
					|| !EqualsHelper.nullSafeEquals(newDigest, existing.getTemplateDigest())) {
				existing.setTemplateDigest(newDigest);
				instanceDao.instanceUpdated(existing, true);

				saveContentToLocalStore(instance, content);
			} else {
				instanceDao.instanceUpdated(existing, true);
			}

			updated++;
		}

		LOGGER.debug("Templates reload finished in {} s updating {} templates",
				(tracker != null ? tracker.stopInSeconds() : "unknown"), updated);
		return contens;
	}

	/**
	 * Read content from local store. The method will try to read the file from the local store. If
	 * the file does not exists will try to download the file from DMS. On download will try to save
	 * to local store. If failed will return the file content directly from the system temp dir. The
	 * method will return <code>null</code> if failed to read the file at all after all attempts.
	 *
	 * @param instance
	 *            the instance
	 * @return the input stream to the file to <code>null</code> if failed
	 */
	private InputStream readContentFromLocalStore(TemplateInstance instance) {
		File file = buildFilePath(instance);
		InputStream stream = getFileStream(file);
		if (stream != null) {
			// if the file is present we should return it and nothing to do more
			return stream;
		}
		try {
			// if the file is not found the there is a valid template location we will try to
			// re-download the file probably got deleted from the store
			TimeTracker tracker = TimeTracker.createAndStart();

			FileDescriptor descriptor = contentAdapterService.getContentDescriptor(instance);
			DefinitionCompilerCallback<TemplateDefinitionImpl> callback = new TemplateDefinitionCompilerCallbackProxy(
					compilerHelper, descriptor);
			List<TemplateDefinitionImpl> list = compiler.compileDefinitions(callback, false);

			Map<String, Serializable> templates = null;
			if (list.isEmpty()) {
				// we failed to download the file/file was invalid/file was missing(deleted from
				// DMS) - force file synchronization
				SecurityContextManager.callAsSystem(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						// the method will also clean any invalid templates
						reloadInternal(true);
						return null;
					}
				});
				// we will try to load the file again bellow
			} else {
				// process the results and try to load the content
				templates = processNewTemplates(tracker, list, null, true, true);
			}

			// try to read the file from local store again
			InputStream inputStream = getFileStream(file);
			if (inputStream != null) {
				return inputStream;
			}
			// if still not found then try to fetch the content from the returned map
			if (templates != null) {
				Serializable content = templates.get(instance.getIdentifier());
				if (content != null) {
					return new ByteArrayInputStream(content.toString().getBytes("UTF-8"));
				}
			}
			// if still not found well the template is broken or deleted we can't do anything more
		} catch (DMSException e) {
			LOGGER.warn("Failed to download content from DMS due to {}", e.getMessage());
			LOGGER.debug("", e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug(e.getMessage());
		}
		return null;
	}

	/**
	 * Gets the file stream for the given file only if the file exists, can be read and is not
	 * empty. The file should not point to a directory!
	 *
	 * @param file
	 *            the file to load
	 * @return the file stream or <code>null</code> if the file is not readable or empty
	 */
	private InputStream getFileStream(File file) {
		try {
			if ((file != null) && file.canRead() && file.isFile() && (file.length() > 0)) {
				return new FileInputStream(file);
			}
		} catch (FileNotFoundException e) {
			LOGGER.warn("Failed to read file {} due to {}", file.getAbsolutePath(), e.getMessage());
			LOGGER.debug(e.getMessage(), e);
		} catch (SecurityException e) {
			LOGGER.warn("No permissions to read file {} due to {}", file.getAbsolutePath(),
					e.getMessage());
			LOGGER.debug(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Save content to local store.
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return true, if successful
	 */
	private boolean saveContentToLocalStore(TemplateInstance instance, Serializable content) {
		if (content == null) {
			// no content to save
			return false;
		}
		File file = buildFilePath(instance);
		if ((file == null) || !file.canWrite() || file.isDirectory()) {
			LOGGER.warn("Cannot write template to " + file);
			return false;
		}
		try (FileOutputStream stream = new FileOutputStream(file)) {
			LOGGER.debug("Saving template file to: " + file.getAbsolutePath());
			IOUtils.write(content.toString(), stream);
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.warn("", e);
		} catch (IOException e) {
			LOGGER.warn("Failed to write template content to file: {}", file.getAbsolutePath(), e);
		} catch (SecurityException e) {
			LOGGER.warn("No permissions to write to file {} due to {}", file.getAbsolutePath(),
					e.getMessage());
			LOGGER.debug(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Builds the file path from the given instance
	 *
	 * @param instance
	 *            the instance
	 * @return the file
	 */
	private File buildFilePath(TemplateInstance instance) {
		StringBuilder builder = new StringBuilder(50);
		builder.append("templates").append(File.separatorChar).append(instance.getContainer())
				.append(File.separatorChar).append(instance.getGroupId());
		File dir = fileProvider.createLongLifeTempDir(builder.toString());
		File file = new File(dir, instance.getIdentifier() + ".xml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			LOGGER.error("Failed to create new empty file in the local store: {}",
					dir.getAbsolutePath(), e);
			return null;
		}
		return file;
	}

	/**
	 * Calculate digest.
	 *
	 * @param content
	 *            the content
	 * @return the string
	 */
	private String calculateDigest(Serializable content) {
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
