package com.sirma.itt.emf.semantic.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.extension.PersistentProperties.PersistentPropertyKeys;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;

/**
 * Observer listening for a {@link InstancePersistedEvent} and storing the passed
 * {@link com.sirma.itt.emf.instance.model.Instance} properties into the semantic repository via the
 * {@link DbDao} service in case the instance class is listed as persistent one
 *
 * @see {@link com.sirma.itt.emf.semantic.persistence.PersistentInstances}
 * @author Valeri Tishev
 */
@ApplicationScoped
public class SemanticEventObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticEventObserver.class);

	@Inject
	@SemanticDb
	private DbDao dbDao;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_PERSISTENT_CLASSES, defaultValue = "caseinstance, "
			+ "documentinstance, "
			+ "projectinstance, "
			+ "sectioninstance, "
			+ "standalonetaskinstance, " + "taskinstance, " + "workflowinstancecontext")
	private Set<String> persistentInstanceClasses;

	private Set<Class<?>> instanceClassesToBePersisted;

	/**
	 * Register all persistent instance classes as listed in
	 * {@link com.sirma.itt.emf.semantic.persistence.PersistentInstances}
	 */
	@PostConstruct
	protected void registerPersistentInstanceClasses() {
		instanceClassesToBePersisted = new HashSet<>(persistentInstanceClasses.size());

		DataTypeDefinition dataTypeDefinition = null;

		for (String className : persistentInstanceClasses) {
			dataTypeDefinition = dictionaryService.getDataTypeDefinition(className);
			if (dataTypeDefinition != null) {
				instanceClassesToBePersisted.add(dataTypeDefinition.getJavaClass());
			} else {
				LOGGER.warn("No data type definition of class [{}]", className);
			}

		}

		LOGGER.debug("Registered [{}] instance classes to be persisted.",
				Integer.toString(instanceClassesToBePersisted.size()));
	}

	/**
	 * Set uri on instance create. The uri might be needed before the instance is persisted.
	 *
	 * @param event
	 *            is the event handled
	 */
	public void observeBeforeInstanceCreateEvent(@Observes InstanceCreateEvent<?> event) {
		Instance instance = event.getInstance();
		if (instance.getId() instanceof String) {
			// if the ID is filled then we ignore the call we probably have generated ID
			return;
		}
		String subjectUri = (String) instance.getProperties().get(
				PersistentPropertyKeys.URI.getKey());

		if (StringUtils.isNullOrEmpty(subjectUri)) {
			SequenceEntityGenerator.generateStringId(instance, true);
			instance.getProperties().put(PersistentPropertyKeys.URI.getKey(), subjectUri);
		}
	}

	/**
	 * The method listens for a {@link InstancePersistedEvent} in order to store the passed
	 * {@link com.sirma.itt.emf.instance.model.Instance} properties into the semantic repository via
	 * the {@link DbDao} service
	 *
	 * @param event
	 *            the fired event
	 */
	public void observeInstancePersistedEvent(@Observes InstancePersistedEvent<Instance> event) {
		Instance instance = event.getInstance();

		if (instanceClassesToBePersisted.contains(event.getInstance().getClass())) {
			dbDao.saveOrUpdate(instance, event.getOldVersion());
		} else {
			LOGGER.warn("Class of type [{}] is not listed as persistent.", instance.getClass());
		}
	}

	/**
	 * @param event
	 *            is the event handled
	 */
	public void observeAfterInstanceDeletedEvent(@Observes AfterInstanceDeleteEvent<?, ?> event) {
		Instance instance = event.getInstance();

		if (instanceClassesToBePersisted.contains(event.getInstance().getClass())) {
			dbDao.delete(instance.getClass(), instance.getId());
		} else {
			LOGGER.warn("Class of type [{}] is not listed as persistent.", instance.getClass());
		}
	}

}
