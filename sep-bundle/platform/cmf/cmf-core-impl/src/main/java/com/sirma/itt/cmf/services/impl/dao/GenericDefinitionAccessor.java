package com.sirma.itt.cmf.services.impl.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionProxy;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.dao.CommonDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.Documentation;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the case definitions and
 * case instances.
 * 
 * @author BBonev
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GenericDefinitionAccessor extends CommonDefinitionAccessor<GenericDefinition>
implements DefinitionAccessor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3703684091702522270L;
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	/** The Constant GENERIC_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 250), doc = @Documentation(""
			+ "Cache used to contain the generic definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded instance that depends on a definition and is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * genericDefinitions * 10. Here 10 is the number of the different versions of a single case definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * genericDefinitions * 10"))
	private static final String GENERIC_DEFINITION_CACHE = "GENERIC_DEFINITION_CACHE";
	/** The Constant GENERIC_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Cache used to contain the latest generic definitions. The cache will have at most an entry for every different generic definition per active tenant. "
			+ "<br>Minimal value expression: tenants * genericDefinitions"))
	private static final String GENERIC_DEFINITION_MAX_REVISION_CACHE = "GENERIC_DEFINITION_MAX_REVISION_CACHE";

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The default user definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_USER_DEFINITION, defaultValue = "userDefinition")
	private String defaultUserDefinition;

	/** The default group definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_GROUP_DEFINITION, defaultValue = "groupDefinition")
	private String defaultGroupDefinition;

	/** The default comment definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_COMMENT_DEFINITION, defaultValue = "commentDefinition")
	private String defaultCommentDefinition;

	/** The default topic definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_TOPIC_DEFINITION, defaultValue = "topicDefinition")
	private String defaultTopicDefinition;

	/** The default relation definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_LINK_DEFINITION, defaultValue = "linkDefinition")
	private String defaultRelationDefinition;

	/** The default relation definition. */
	@Inject
	@Config(name = CmfConfigurationProperties.DEFAULT_IMAGE_ANNOTATION_DEFINITION, defaultValue = "imageAnnotationDefinition")
	private String defaultIADefinition;

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(GenericDefinition.class);
		SUPPORTED_OBJECTS.add(GenericDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(Resource.class);
		SUPPORTED_OBJECTS.add(EmfUser.class);
		SUPPORTED_OBJECTS.add(EmfGroup.class);
		SUPPORTED_OBJECTS.add(CommentInstance.class);
		SUPPORTED_OBJECTS.add(TopicInstance.class);
		SUPPORTED_OBJECTS.add(LinkInstance.class);
		SUPPORTED_OBJECTS.add(LinkReference.class);
		SUPPORTED_OBJECTS.add(ImageAnnotation.class);
		SUPPORTED_OBJECTS.add(SectionDefinitionProxy.class);
		SUPPORTED_OBJECTS.add(FolderInstance.class);
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
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <D extends DefinitionModel> D getDefinition(Instance instance) {
		String definitionId = instance.getIdentifier();
		if (instance instanceof Resource) {
			ResourceType type = ((Resource) instance).getType();
			switch (type) {
				case USER:
					definitionId = defaultUserDefinition;
					break;
				case GROUP:
					definitionId = defaultGroupDefinition;
					break;
				default:
					//thing to do
					break;
			}
		} else if (instance instanceof TopicInstance) {
			definitionId = defaultTopicDefinition;
		} else if (instance instanceof CommentInstance) {
			definitionId = defaultCommentDefinition;
		} else if ((instance instanceof LinkReference) || (instance instanceof LinkInstance)) {
			definitionId = defaultRelationDefinition;
		} else if (instance instanceof ImageAnnotation) {
			definitionId = defaultIADefinition;
		}

		if (definitionId == null) {
			LOGGER.warn("Failed to find generic definition for the instance with class: "
					+ instance.getClass().getSimpleName());
			return null;
		}
		if ((instance.getRevision() == null) || (instance.getRevision() <= 0L)) {
			// if no revision is supported use the max revision
			return getDefinition(getCurrentContainer(), definitionId);
		}
		return getDefinition(getCurrentContainer(), definitionId, instance.getRevision());
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
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly,
			boolean isMaxRevision) {
		super.updateCache(definition, propertiesOnly, isMaxRevision);
		GenericDefinition genericDefinition = (GenericDefinition) definition;
		for (GenericDefinition child : genericDefinition.getSubDefinitions()) {
			injectLabelProvider(child);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<GenericDefinition> getTargetDefinition() {
		return GenericDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getBaseCacheName() {
		return GENERIC_DEFINITION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMaxRevisionCacheName() {
		return GENERIC_DEFINITION_MAX_REVISION_CACHE;
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
		// first fire event to notify that particular definitions are being updated
		fireEventForDefinitionMigration(
				DbQueryTemplates.QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION_KEY,
				GenericDefinition.class, FolderInstance.class, definitionIds);
		int count = 0;
		if ((definitionIds == null) || (definitionIds.length == 0)) {
			count += executeDefinitionMigrateQuery(
					DbQueryTemplates.UPDATE_ALL_FOLDER_DEFINITIONS_KEY, GenericDefinition.class);
		} else {
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_FOLDER_DEFINITIONS_KEY,
					GenericDefinition.class, definitionIds);
		}
		return count;
	}

}
