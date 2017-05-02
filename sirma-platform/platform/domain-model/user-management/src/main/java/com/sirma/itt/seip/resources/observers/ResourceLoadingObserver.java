package com.sirma.itt.seip.resources.observers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.event.ItemsFilter;
import com.sirma.itt.seip.domain.event.LoadItemsEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceSorter;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * An asynchronous update interface for receiving notifications about ResourceLoading information as the ResourceLoading
 * is constructed.
 *
 * @author svelikov
 */
@ApplicationScoped
public class ResourceLoadingObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoadingObserver.class);

	@Inject
	private ResourceService resourceService;

	@Inject
	private ResourceSorter resourceSorter;

	/**
	 * Handles {@link LoadItemsEvent} in order to load resources possibly filtered by some criteria.
	 *
	 * @param event
	 *            the event
	 */
	public void loadFilteredResources(@Observes @ItemsFilter("assigneeListItemsFilter") LoadItemsEvent event) {
		// Find out if requested resources exists and return only them as groups should be expanded.
		//
		// - iterate trough keywords that contains groups and/or users
		// -- load every resource that is requested
		// -- expand groups
		// - if found resources is empty then return all resources
		// - else return found resources
		Map<String, Object> keywordsMap = event.getKeywords();
		if (keywordsMap != null && !keywordsMap.isEmpty()) {
			List<Instance> filteredResources = new ArrayList<>();
			for (Entry<String, Object> entry : keywordsMap.entrySet()) {
				String keywordsCSV = (String) keywordsMap.get(entry.getKey());
				String[] keywords = keywordsCSV.trim().replaceAll(" ", "").split(",");
				filteredResources
						.addAll(resourceService.getContainedResources(Arrays.asList(keywords), ResourceType.USER));
				resourceSorter.sort(filteredResources);
			}
			// not found any resources for provided keywords -> load all instead
			if (filteredResources.isEmpty()) {
				filteredResources.addAll(loadAllResources(event.getType(), event.getSortBy()));
			}
			List<Instance> all = new LinkedList<>(filteredResources);
			event.setItems(all);
			event.setHandled(true);
			LOGGER.debug("Handled LoadItemsEvent for [assigneeListItemsFilter]");
		}

	}

	/**
	 * Loads all available resources.
	 *
	 * @param type
	 *            the type
	 * @param sortingField
	 *            the sorting field
	 * @return the list< resource>
	 */
	protected List<Resource> loadAllResources(String type, String sortingField) {
		ResourceType resourceType = ResourceType.getByType(type);
		List<Resource> resources = new LinkedList<>();
		if (resourceType == ResourceType.ALL) {
			List<Resource> users = resourceService.getAllResources(ResourceType.USER, sortingField);
			resourceSorter.sort(users);
			List<Resource> groups = resourceService.getAllResources(ResourceType.GROUP, sortingField);
			resourceSorter.sort(groups);

			resources.addAll(users);
			resources.addAll(groups);
		} else {
			resources = resourceService.getAllResources(resourceType, sortingField);
			resourceSorter.sort(resources);
		}
		return resources;
	}

}
