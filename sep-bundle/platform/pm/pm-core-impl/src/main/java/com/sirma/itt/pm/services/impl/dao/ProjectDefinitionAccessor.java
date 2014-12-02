package com.sirma.itt.pm.services.impl.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.dao.CommonDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.DbQueryTemplatesPm;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the project definitions
 * and project instances.
 * 
 * @author BBonev
 */
@Stateless
public class ProjectDefinitionAccessor extends CommonDefinitionAccessor<ProjectDefinition>
implements DefinitionAccessor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6732165609200389513L;

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	/** The Constant CASE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 200), doc = @Documentation(""
			+ "Cache used to contain the project definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded project that is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * projectDefinitions * 10. Here 10 is the number of the different versions of a single project definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * projectDefinitions * 10"))
	private static final String PROJECT_DEFINITION_CACHE = "PROJECT_DEFINITION_CACHE";
	/** The Constant CASE_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Cache used to contain the latest project definitions. The cache will have at most an entry for every different project definition per active tenant. "
			+ "<br>Minimal value expression: tenants * projectDefinitions"))
	private static final String PROJECT_DEFINITION_MAX_REVISION_CACHE = "PROJECT_DEFINITION_MAX_REVISION_CACHE";

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(ProjectDefinition.class);
		SUPPORTED_OBJECTS.add(ProjectDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(ProjectInstance.class);
	}

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
		if (instance instanceof ProjectInstance) {
			return getDefinition(((ProjectInstance) instance).getContainer(),
					instance.getIdentifier(), instance.getRevision());
		}
		return null;
	}

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

	@Override
	protected Class<ProjectDefinition> getTargetDefinition() {
		return ProjectDefinition.class;
	}

	@Override
	protected String getBaseCacheName() {
		return PROJECT_DEFINITION_CACHE;
	}

	@Override
	protected String getMaxRevisionCacheName() {
		return PROJECT_DEFINITION_MAX_REVISION_CACHE;
	}

	@Override
	public Set<String> getActiveDefinitions() {
		List<Object[]> list = getDbDao()
				.fetch("select p.definitionId, p.container from ProjectEntity p group by p.definitionId, p.container",
						new ArrayList<Pair<String, Object>>(0));
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for (Object[] objects : list) {
			String definitionId = DefinitionIdentityUtil.createDefinitionId((String) objects[0],
					(String) objects[1]);
			set.add(definitionId);
		}
		return set;
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {

		// first fire event to notify that particular definitions are being updated
		fireEventForDefinitionMigration(
				DbQueryTemplatesPm.QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION_KEY,
				ProjectDefinition.class, ProjectInstance.class, definitionIds);

		String query = DbQueryTemplatesPm.UPDATE_ALL_PROJECT_DEFINITIONS_KEY;
		if (definitionIds != null && definitionIds.length > 0) {
			query = DbQueryTemplatesPm.UPDATE_PROJECT_DEFINITIONS_KEY;
		}
		return executeDefinitionMigrateQuery(query, ProjectDefinition.class, definitionIds);
	}

}
