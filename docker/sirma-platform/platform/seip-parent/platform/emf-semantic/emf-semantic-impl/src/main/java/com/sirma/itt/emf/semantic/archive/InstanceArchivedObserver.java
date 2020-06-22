package com.sirma.itt.emf.semantic.archive;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.instance.archive.ArchivedInstanceAddedEvent;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Observer that listens for removal of an instance and executes a semantic query to mark all child instances as
 * deleted. The query is: {@value #MARK_INSTANCE_GRAPH_FOR_DELETED}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceArchivedObserver {

	private static final String MARK_INSTANCE_GRAPH_FOR_DELETED = "instanceQueries/markInstanceGraphForDeleted";
	private static final String MARK_TOPICS_GRAPH_FOR_DELETED = "instanceQueries/markInstanceTopicsGraphForDeleted";
	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceArchivedObserver.class);
	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;
	@Inject
	private SearchService searchService;
	@Inject
	private NamespaceRegistryService registryService;

	/**
	 * Listen for event on scheduling an instance for archival to mark all instance graph for deleted.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceArchivedEvent(@Observes ArchivedInstanceAddedEvent event) {
		Instance instance = event.getInstance();
		Context<String, Object> context = new Context<>();
		context.put("objectUri", instance.getId());
		context.put("transactionId", event.getTransactionId());
		// we first delete the topics because the query depends on the instances not to be deleted to be able to run
		// properly
		executeQuery(instance, context, MARK_TOPICS_GRAPH_FOR_DELETED);
		executeQuery(instance, context, MARK_INSTANCE_GRAPH_FOR_DELETED);
	}

	private void executeQuery(Instance instance, Context<String, Object> context, String query) {
		SearchArguments<SearchInstance> filter = searchService.getFilter(query,
				SearchInstance.class, context);

		if (filter == null || StringUtils.isBlank(filter.getStringQuery())) {
			LOGGER.warn("Could not find query with name: {}", query);
			return;
		}

		RepositoryConnection connection = repositoryConnection.get();
		try {
			TimeTracker timeTracker = TimeTracker.createAndStart();

			Update update = connection.prepareUpdate(QueryLanguage.SPARQL,
					registryService.getNamespaces() + filter.getStringQuery());
			// URI instanceUri = registryService.buildUri(instance.getId().toString())
			// update.setBinding("objectUri", instanceUri)
			update.setIncludeInferred(true);
			update.execute();

			LOGGER.debug("Marking the graph for deleted took {} ms", timeTracker.stop());
		} catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
			LOGGER.error("Failed to mark instance({}) graph for deleted on archival", instance.getId(), e);
		}
	}

}
