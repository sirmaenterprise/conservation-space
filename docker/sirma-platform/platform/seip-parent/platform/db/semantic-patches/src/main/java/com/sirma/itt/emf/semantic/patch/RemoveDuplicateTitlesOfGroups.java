package com.sirma.itt.emf.semantic.patch;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Removes duplicate titles for group instances and leaves only the correct ones.
 *
 * @author smustafov
 */
public class RemoveDuplicateTitlesOfGroups extends UpdateSemanticTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * select ?instance ?title where {
	 *    ?instance a emf:Group .
	 *    ?instance dcterms:title ?title .
	 *    {
	 *       select ?instance (count(*) as ?titleCount) {
	 *          ?instance dcterms:title ?title .
	 *       }
	 *	     group by ?instance
	 *	  }
	 *	  filter(?titleCount > 1).
	 *	}
	 */
	private static final String GROUPS_WITH_DUPLICATE_TITLE_QUERY = "select ?instance ?title where {"
			+ "?instance a emf:Group . ?instance dcterms:title ?title . {select ?instance (count(*) as ?titleCount) {"
			+ "?instance dcterms:title ?title } group by ?instance } filter(?titleCount > 1). }";

	private ResourceService resourceService;
	private NamespaceRegistryService namespaceRegistry;

	@Override
	public void setUp() throws SetupException {
		super.setUp();
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		namespaceRegistry = CDI.instantiateBean(NamespaceRegistryService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		RepositoryConnection repositoryConnection = connectionFactory.produceConnection();

		try {
			Map<Value, List<Value>> groupsWithDuplicateTitles = fetchGroupsWithDuplicateTitles(repositoryConnection);
			LOGGER.info("Groups with duplicate title: {}", groupsWithDuplicateTitles);

			removeTitleStatements(repositoryConnection, groupsWithDuplicateTitles);
		} finally {
			if (repositoryConnection != null) {
				connectionFactory.disposeConnection(repositoryConnection);
			}
		}
	}

	private Map<Value, List<Value>> fetchGroupsWithDuplicateTitles(RepositoryConnection repositoryConnection) {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection,
				GROUPS_WITH_DUPLICATE_TITLE_QUERY, CollectionUtils.emptyMap(), false);
		Map<Value, List<Value>> groupsTitleMapping = new HashMap<>();

		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : iterator) {
				Value instanceId = bindingSet.getValue(SPARQLQueryHelper.OBJECT);
				Value title = bindingSet.getValue("title");
				addToMapping(groupsTitleMapping, instanceId, title);
			}
		}

		return groupsTitleMapping;
	}

	private void addToMapping(Map<Value, List<Value>> groupsTitleMapping, Value instanceId, Value title) {
		String groupId = getShortUri(instanceId);
		Resource resource = resourceService.getResource(groupId);
		if (resource != null) {
			String correctTitle = resource.getDisplayName();
			if (!title.stringValue().equals(correctTitle)) {
				groupsTitleMapping.computeIfAbsent(instanceId, v -> new ArrayList<>()).add(title);
			}
		} else {
			LOGGER.warn("Tried to remove duplicate title for group that is missing in relational db: {}", groupId);
		}
	}

	private String getShortUri(Value value) {
		IRI uri = (IRI) value;
		return namespaceRegistry.getShortUri(uri);
	}

	private static void removeTitleStatements(RepositoryConnection repositoryConnection,
			Map<Value, List<Value>> groupsWithDuplicateTitles) {
		Model removeModel = new LinkedHashModel();

		groupsWithDuplicateTitles
				.forEach((group, value) -> value.forEach(title -> removeModel.add((IRI) group, DCTERMS.TITLE, title)));

		SemanticPersistenceHelper.removeModel(repositoryConnection, removeModel, EMF.DATA_CONTEXT);
	}

}
