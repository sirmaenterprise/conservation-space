package com.sirma.itt.objects.services.impl.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.definition.dao.CommonDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.event.DefinitionMigrationEvent;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.domain.model.SavedFilter;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the project definitions
 * and project instances.
 *
 * @author BBonev
 */
@Stateless
public class ObjectDefinitionAccessor extends CommonDefinitionAccessor<ObjectDefinition>
implements DefinitionAccessor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6732165609200389513L;

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	/** The Constant OBJECT_DEFINITION_CACHE. */
	@CacheConfiguration(container = "obj", eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Cache used to contain the object definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded object that is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * objectDefinitions * 10. Here 10 is the number of the different versions of a single object definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * objectDefinitions * 10"))
	private static final String OBJECT_DEFINITION_CACHE = "OBJECT_DEFINITION_CACHE";
	/** The Constant OBJECT_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(container = "obj", eviction = @Eviction(maxEntries = 50), doc = @Documentation(""
			+ "Cache used to contain the latest object definitions. The cache will have at most an entry for every different object definition per active tenant. "
			+ "<br>Minimal value expression: tenants * objectDefinitions"))
	private static final String OBJECT_DEFINITION_MAX_REVISION_CACHE = "OBJECT_DEFINITION_MAX_REVISION_CACHE";

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(ObjectDefinition.class);
		SUPPORTED_OBJECTS.add(ObjectDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(ObjectInstance.class);
		SUPPORTED_OBJECTS.add(SavedFilter.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostConstruct
	public void initinializeCache() {
		super.initinializeCache();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		if (instance instanceof ObjectInstance) {
			return getDefinition(((ObjectInstance) instance).getContainer(),
					instance.getIdentifier(), instance.getRevision());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ObjectDefinition> getTargetDefinition() {
		return ObjectDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getBaseCacheName() {
		return OBJECT_DEFINITION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMaxRevisionCacheName() {
		return OBJECT_DEFINITION_MAX_REVISION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getActiveDefinitions() {
		return Collections.emptySet();
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {
		// we does not have anything in database to update so we should just fire the event to
		// update the semantic db.
		Set<String> definitions = Collections.emptySet();
		if (definitionIds != null && definitionIds.length > 0) {
			definitions = new HashSet<String>(Arrays.asList(definitionIds));
		}
		eventService.fire(new DefinitionMigrationEvent(ObjectInstance.class,
				getDefinitionsAndMaxRevisions(ObjectInstance.class, null, definitions)));
		// mark at least one migrated - we can't tell how many are they but we should not return 0
		// either - if updated only object definitions the response will be not successful.
		return 1;
	}

	@Override
	protected List<String> getUsedDefinitionsNotAtMaxRevision(String query, Class<?> targetDefinition,
			List<Pair<String, Object>> params) {
		// no need to run anything we will override the method that uses the result from this one
		return Collections.emptyList();
	}

	@Override
	protected List<Pair<String, Long>> getDefinitionsAndMaxRevisions(Class<?> targetInstance,
			List<Pair<String, Object>> params, Set<String> tempDefinitions) {
		// no need to collect definitions with custom query just get all definitions from the cache
		List<DefinitionModel> definitions = getAllDefinitions(getCurrentContainer());
		List<Pair<String, Long>> result = new ArrayList<>(definitions.size());

		for (DefinitionModel definitionModel : definitions) {
			// if the model is empty than we need all definitions or is not only the one in the set
			if (tempDefinitions.isEmpty()
					|| tempDefinitions.contains(definitionModel.getIdentifier())) {
				result.add(new Pair<String, Long>(definitionModel.getIdentifier(), definitionModel
						.getRevision()));
			}
		}
		return result;
	}

}
