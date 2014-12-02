package com.sirma.itt.objects.services.actions;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.ConcurrentMultiValueCollection;
import com.sirma.itt.emf.executors.ExecutableOperationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.model.SavedFilter;

/**
 * Provides common logic used when creating and updating filters.
 * 
 * @author nvelkov
 */
@ApplicationScoped
public class FilterHelper {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FilterHelper.class);

	/** The search service. */
	@Inject
	private SearchService searchService;

	@Inject
	private ConcurrentMultiValueCollection collection;

	/**
	 * Checks if the filter is already saved in solr. If there is an uri passed to the method,
	 * someone is sending an update filter request, if there is no uri passed to the method, someone
	 * is sending a create filter request.
	 * <p>
	 * If the there is a filter with the title found in solr but there is no uri passed, someone is
	 * trying to create a filter with an already existing title.If there is a filter with the title
	 * found in solr and there is an uri passed, someone is trying to update a filter with an
	 * already existing title. In that case, if the filter, returned from solr, has the same uri as
	 * the one that is being updated, the operation can continue.
	 * 
	 * @param title
	 *            the title of the filter
	 * @param uri
	 *            the uri
	 * @return true, if the filter is persisted
	 */
	private boolean isPersisted(String title, String uri) {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		String solrFilterQuery = "title:\"" + title + "\" AND instanceType:\"savedfilter\"";
		searchArgs.setStringQuery(solrFilterQuery);
		searchArgs.getArguments().put("query", solrFilterQuery);
		searchArgs.setDialect("solr");
		searchService.search(Instance.class, searchArgs);
		if (!searchArgs.getResult().isEmpty()) {
			return !searchArgs.getResult().get(0).getId().equals(uri);
		}
		return false;
	}

	/**
	 * Add properties to the createFilter request, that shouldn't be changed from the user.
	 * 
	 * @param data
	 *            the data
	 */
	public void convertRequest(JSONObject data) {
		JsonUtil.addToJson(data, ExecutableOperationProperties.TYPE, SavedFilter.class
				.getSimpleName().toLowerCase());
		JsonUtil.addToJson(data, ExecutableOperationProperties.DEFINITION, "savedFilter");
	}

	/**
	 * First checks if there is a filter with the same title, currently being persisted (implemented
	 * to avoid creating/updating objects with the same title). If there isn't one, checks in solr
	 * if there is a persisted one. If there isn't, persists it and Calls {@link
	 * CreateObjectExecutor.}.
	 * 
	 * @param context
	 *            the context
	 * @return true, if the filter can be persisted, false otherwise
	 */
	public boolean validateFilter(SchedulerContext context) {
		@SuppressWarnings("unchecked")
		Map<String, String> dataProperties = (Map<String, String>) context.get("properties");
		String title = dataProperties.get("title");
		if (collection.keyContains("title", title)) {
			LOGGER.warn("A filter with title '" + title + "' already exists");
			return false;
		}
		collection.addToKey("title", title);
		collection.removeAfterTransaction("title", title);
		InstanceReference target = (InstanceReference) context.get("target");
		if (isPersisted(title, target.getIdentifier())) {
			LOGGER.warn("A filter with title '" + title + "' already exists");
			return false;
		}
		return true;
	}
}
