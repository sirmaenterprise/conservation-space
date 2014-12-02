package com.sirma.itt.cmf.services.impl.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
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

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the workflow definitions
 * and workflow instance contexts.
 *
 * @author BBonev
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WorkflowDefinitionAccessor extends CommonDefinitionAccessor<WorkflowDefinition>
implements DefinitionAccessor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6636422499453367881L;
	/** The Constant WORKFLOW_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Cache used to contain the workflow definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded workflow that is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * workflowDefinitions * 10. Here 10 is the number of the different versions of a single workflow definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * workflowDefinitions * 10"))
	private static final String WORKFLOW_DEFINITION_CACHE = "WORKFLOW_DEFINITION_CACHE";
	/** The Constant WORKFLOW_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), doc = @Documentation(""
			+ "Cache used to contain the latest workflow definitions. The cache will have at most an entry for every different workflow definition per active tenant. "
			+ "<br>Minimal value expression: tenants * workflowDefinitions"))
	private static final String WORKFLOW_DEFINITION_MAX_REVISION_CACHE = "WORKFLOW_DEFINITION_MAX_REVISION_CACHE";

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WorkflowDefinitionAccessor.class);

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(WorkflowDefinition.class);
		SUPPORTED_OBJECTS.add(WorkflowInstanceContext.class);
		SUPPORTED_OBJECTS.add(TaskInstance.class);
		SUPPORTED_OBJECTS.add(WorkflowDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(TaskDefinitionRefImpl.class);
		SUPPORTED_OBJECTS.add(TaskDefinitionRef.class);
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
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <D extends DefinitionModel> D getDefinition(Instance instance) {
		if (instance instanceof WorkflowInstanceContext) {
			WorkflowDefinition definition = getDefinition(
					((WorkflowInstanceContext) instance).getContainer(), instance.getIdentifier(),
					instance.getRevision());
			return (D) definition;
		} else if (instance instanceof TaskInstance) {
			TaskInstance taskInstance = (TaskInstance) instance;
			WorkflowInstanceContext context = taskInstance.getContext();

			WorkflowDefinition workflowDefinition = null;
			if (context != null) {
				workflowDefinition = getDefinition(context.getContainer(), context.getIdentifier(),
						context.getRevision());
			} else {
				String workflowDefinitionId = taskInstance.getWorkflowDefinitionId();
				if (instance.getRevision() == null) {
					// this should not happen, but to be sure.. well it can lead
					// to a nasty bug as well...
					workflowDefinition = getDefinition(getCurrentContainer(), workflowDefinitionId);
					LOGGER.warn("Trying to get task definition , but there is not revision specified! The instance is: "
							+ instance);
				} else {
					workflowDefinition = getDefinition(getCurrentContainer(), workflowDefinitionId,
							taskInstance.getRevision());
				}
			}
			if (workflowDefinition != null) {
				return (D) WorkflowHelper.getTaskById(workflowDefinition,
						taskInstance.getIdentifier());
			}
		}

		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	/**
	 * Update workflow definition caches.
	 *
	 * @param definition
	 *            the new definition
	 * @param propertiesOnly
	 *            the properties only
	 * @param isMaxRevision
	 *            the is max revision
	 */
	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly,
			boolean isMaxRevision) {
		WorkflowDefinition newDefinition = (WorkflowDefinition) definition;
		super.updateCache(newDefinition, propertiesOnly, isMaxRevision);
		// populate the label provider in all definitions
		injectLabelProvider(newDefinition.getTasks());
	}

	@Override
	protected Class<WorkflowDefinition> getTargetDefinition() {
		return WorkflowDefinition.class;
	}

	@Override
	protected String getBaseCacheName() {
		return WORKFLOW_DEFINITION_CACHE;
	}

	@Override
	protected String getMaxRevisionCacheName() {
		return WORKFLOW_DEFINITION_MAX_REVISION_CACHE;
	}

	@Override
	public Set<String> getActiveDefinitions() {
		List<Object[]> list = getDbDao()
				.fetch("select w.workflowDefinitionId, w.container from WorkflowInstanceContextEntity w group by w.workflowDefinitionId, w.container",
						new ArrayList<Pair<String, Object>>(0));
		Set<String> set = new LinkedHashSet<String>();
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
		fireEventForDefinitionMigration(DbQueryTemplates.QUERY_WF_DEFINITIONS_FOR_MIGRATION_KEY,
				WorkflowDefinition.class, WorkflowInstanceContext.class, definitionIds);
		fireEventForDefinitionMigration(DbQueryTemplates.QUERY_WF_DEFINITIONS_FOR_MIGRATION_KEY,
				WorkflowDefinition.class, TaskInstance.class, definitionIds);

		int count = 0;
		if (definitionIds != null && definitionIds.length > 0) {
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_WORKFLOW_DEFINITIONS_KEY, WorkflowDefinition.class, definitionIds);
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_WF_TASK_DEFINITIONS_KEY, WorkflowDefinition.class, definitionIds);
		} else  {
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_ALL_WORKFLOW_DEFINITIONS_KEY, WorkflowDefinition.class, definitionIds);
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_ALL_WF_TASK_DEFINITIONS_KEY, WorkflowDefinition.class, definitionIds);
		}
		return count;
	}

	@Override
	protected List<Pair<String, Long>> getDefinitionsAndMaxRevisions(Class<?> targetInstance,
			List<Pair<String, Object>> params, Set<String> tempDefinitions) {
		List<Pair<String, Long>> definitionsAndMaxRevisions = super.getDefinitionsAndMaxRevisions(
				targetInstance, params, tempDefinitions);
		if (TaskInstance.class.equals(targetInstance)) {
			List<Pair<String, Long>> taskDefinitions = new LinkedList<Pair<String, Long>>();
			for (Pair<String, Long> pair : definitionsAndMaxRevisions) {
				WorkflowDefinition definitionModel = getDefinition(getCurrentContainer(),
						pair.getFirst());
				for (TaskDefinitionRef taskDefinitionRef : definitionModel.getTasks()) {
					taskDefinitions.add(new Pair<String, Long>(taskDefinitionRef.getIdentifier(),
							definitionModel.getRevision()));
				}
			}
			return taskDefinitions;
		}
		return definitionsAndMaxRevisions;
	}

}
