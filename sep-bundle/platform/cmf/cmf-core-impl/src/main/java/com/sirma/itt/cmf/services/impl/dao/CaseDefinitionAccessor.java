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

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionProxy;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.dao.CommonDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the case definitions and
 * case instances.
 *
 * @author BBonev
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CaseDefinitionAccessor extends CommonDefinitionAccessor<CaseDefinition> implements
		DefinitionAccessor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8567515787773054313L;
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	/** The Constant CASE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), doc = @Documentation(""
			+ "Cache used to contain the case definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded case that is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * caseDefinitions * 10. Here 10 is the number of the different versions of a single case definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * caseDefinitions * 10"))
	private static final String CASE_DEFINITION_CACHE = "CASE_DEFINITION_CACHE";
	/** The Constant CASE_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), doc = @Documentation(""
			+ "Cache used to contain the latest case definitions. The cache will have at most an entry for every different case definition per active tenant. "
			+ "<br>Minimal value expression: tenants * caseDefinitions"))
	private static final String CASE_DEFINITION_MAX_REVISION_CACHE = "CASE_DEFINITION_MAX_REVISION_CACHE";

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(CaseDefinition.class);
		SUPPORTED_OBJECTS.add(CaseInstance.class);
		SUPPORTED_OBJECTS.add(DocumentInstance.class);
		SUPPORTED_OBJECTS.add(CaseDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(SectionInstance.class);
		SUPPORTED_OBJECTS.add(DocumentDefinitionRefImpl.class);
		SUPPORTED_OBJECTS.add(SectionDefinition.class);
		SUPPORTED_OBJECTS.add(SectionDefinitionImpl.class);
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
	@SuppressWarnings("unchecked")
	public <D extends DefinitionModel> D getDefinition(Instance instance) {
		if (instance instanceof CaseInstance) {
			CaseDefinition definition = getDefinition(((CaseInstance) instance).getContainer(),
					instance.getIdentifier(), instance.getRevision());
			return (D) definition;
		} else if (instance instanceof DocumentInstance) {
			DocumentInstance documentInstance = (DocumentInstance) instance;
			if (documentInstance.isStandalone()) {
				// handle the definition for the standalone document, the once that are not
				// connected to cases
				return (D) new DocumentDefinitionRefProxy(dictionaryServiceInstance.get()
						.getDefinition(DocumentDefinitionTemplate.class, instance.getIdentifier(),
								instance.getRevision()));
			}
			// get the document path
			String parentPath = documentInstance.getParentPath();
			// get the case definition ID
			String root = PathHelper.extractRootPath(parentPath);
			// get the case definition for the given data
			CaseDefinition caseDefinition = getDefinition(getCurrentContainer(), root,
					instance.getRevision());
			// and from the definition extract the document definition by his path
			Identity identity = PathHelper.iterateByPath(caseDefinition, parentPath);
			if (identity instanceof DocumentDefinitionRef) {
				return (D) identity;
			}
			// this could happen if there is some problem with the document path or the case in
			// general - for example after definition migration the document is not part of the case
			// any more
			LOGGER.warn(
					"Invalid document instance with path {} and revision {}. Returning document template {}",
					parentPath, instance.getRevision(), instance.getIdentifier());
			return (D) new DocumentDefinitionRefProxy(dictionaryServiceInstance.get()
					.getDefinition(DocumentDefinitionTemplate.class, instance.getIdentifier(),
							instance.getRevision()));
		} else if (instance instanceof SectionInstance) {
			SectionInstance sectionInstance = (SectionInstance) instance;
			if (sectionInstance.isStandalone()) {
				// handle the definition for the standalone sections, the once that are not
				// connected to cases
				return (D) new SectionDefinitionProxy(dictionaryServiceInstance.get()
						.getDefinition(GenericDefinition.class, instance.getIdentifier(),
								instance.getRevision()));
			}
			String caseDefinition = PathHelper.extractRootPath(sectionInstance.getDefinitionPath());
			CaseDefinition definition = getDefinition(((SectionInstance) instance).getContainer(),
					caseDefinition, instance.getRevision());
			return (D) PathHelper.find(definition.getSectionDefinitions(),
					sectionInstance.getIdentifier());
		}
		return null;
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
		CaseDefinition caseDefinition = (CaseDefinition) definition;
		for (SectionDefinition sectionDefinition : caseDefinition.getSectionDefinitions()) {
			injectLabelProvider(sectionDefinition);
			injectLabelProvider(sectionDefinition.getDocumentDefinitions());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<CaseDefinition> getTargetDefinition() {
		return CaseDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getBaseCacheName() {
		return CASE_DEFINITION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMaxRevisionCacheName() {
		return CASE_DEFINITION_MAX_REVISION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getActiveDefinitions() {
		// REVIEW: move to named query
		List<Object[]> list = getDbDao()
				.fetch("select c.caseDefinitionId, c.container from CaseEntity c group by c.caseDefinitionId, c.container",
						new ArrayList<Pair<String, Object>>(0));
		Set<String> set = new LinkedHashSet<String>();
		for (Object[] objects : list) {
			String definitionId = DefinitionIdentityUtil.createDefinitionId((String) objects[0],
					(String) objects[1]);
			set.add(definitionId);
		}
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {

		// first fire event to notify that particular definitions are being updated
		fireEventForDefinitionMigration(DbQueryTemplates.QUERY_CASE_DEFINITIONS_FOR_MIGRATION_KEY,
				CaseDefinition.class, CaseInstance.class, definitionIds);
		fireEventForDefinitionMigration(DbQueryTemplates.QUERY_CASE_DEFINITIONS_FOR_MIGRATION_KEY,
				CaseDefinition.class, SectionInstance.class, definitionIds);
		fireEventForDefinitionMigration(DbQueryTemplates.QUERY_CASE_DEFINITIONS_FOR_MIGRATION_KEY,
				CaseDefinition.class, DocumentInstance.class, definitionIds);

		int count = 0;
		if ((definitionIds == null) || (definitionIds.length == 0)) {
			count += executeDefinitionMigrateQuery(
					DbQueryTemplates.UPDATE_ALL_CASE_DEFINITIONS_KEY, CaseDefinition.class);
			count += executeDefinitionMigrateQuery(
					DbQueryTemplates.UPDATE_ALL_SECTION_DEFINITIONS_KEY, CaseDefinition.class);
			count += executeDefinitionMigrateQuery(
					DbQueryTemplates.UPDATE_ALL_DOCUMENT_DEFINITIONS_KEY, CaseDefinition.class);
		} else {
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_CASE_DEFINITIONS_KEY,
					CaseDefinition.class, definitionIds);
			count += executeDefinitionMigrateQuery(DbQueryTemplates.UPDATE_SECTION_DEFINITIONS_KEY,
					CaseDefinition.class, definitionIds);
			count += executeDefinitionMigrateQuery(
					DbQueryTemplates.UPDATE_DOCUMENT_DEFINITIONS_KEY, CaseDefinition.class,
					definitionIds);
		}
		return count;
	}

	@Override
	protected List<Pair<String, Long>> getDefinitionsAndMaxRevisions(Class<?> targetInstance,
			List<Pair<String, Object>> params, Set<String> tempDefinitions) {
		List<Pair<String, Long>> definitionsAndMaxRevisions = super.getDefinitionsAndMaxRevisions(
				targetInstance, params, tempDefinitions);
		if (DocumentInstance.class.equals(targetInstance)) {
			List<Pair<String, Long>> documentDefinitions = new LinkedList<Pair<String, Long>>();
			for (Pair<String, Long> pair : definitionsAndMaxRevisions) {
				CaseDefinition definitionModel = getDefinition(getCurrentContainer(),
						pair.getFirst());
				for (SectionDefinition sectionDefinition : definitionModel.getSectionDefinitions()) {
					collectDefinitionRevisions(documentDefinitions,
							sectionDefinition.getDocumentDefinitions(),
							definitionModel.getRevision());
				}
			}
			return documentDefinitions;
		} else if (SectionInstance.class.equals(targetInstance)) {
			List<Pair<String, Long>> sectionDefinitions = new LinkedList<Pair<String, Long>>();
			for (Pair<String, Long> pair : definitionsAndMaxRevisions) {
				CaseDefinition definitionModel = getDefinition(getCurrentContainer(),
						pair.getFirst());
				collectDefinitionRevisions(sectionDefinitions,
						definitionModel.getSectionDefinitions(), definitionModel.getRevision());
			}
			return sectionDefinitions;
		}
		return definitionsAndMaxRevisions;
	}

	/**
	 * Collect definition revisions.
	 * 
	 * @param documentDefinitions
	 *            the document definitions
	 * @param list
	 *            the list
	 * @param revision
	 *            the revision
	 */
	private void collectDefinitionRevisions(List<Pair<String, Long>> documentDefinitions,
			List<? extends DefinitionModel> list, Long revision) {
		for (DefinitionModel definitionRef : list) {
			documentDefinitions
					.add(new Pair<String, Long>(definitionRef.getIdentifier(), revision));
		}
	}

}
