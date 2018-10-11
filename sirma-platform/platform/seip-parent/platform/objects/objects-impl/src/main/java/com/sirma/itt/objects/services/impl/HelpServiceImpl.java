package com.sirma.itt.objects.services.impl;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.objects.services.HelpService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Provides services for the contextual help logic.
 *
 * @author nvelkov
 */
public class HelpServiceImpl implements HelpService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String HELP_TARGET = EMF.HELP_TARGET.getLocalName();

	@Inject
	private SearchService searchService;

	@Override
	public Map<String, String> getHelpIdToTargetMapping() {
		TimeTracker tracker = TimeTracker.createAndStart();
		Map<String, Serializable> properties = CollectionUtils.createHashMap(2);
		properties.put(DefaultProperties.SEMANTIC_TYPE, EMF.HELP.toString());
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setArguments(properties);
		searchArgs.setPageSize(0);
		searchArgs.setMaxSize(-1);
		searchService.searchAndLoad(Instance.class, searchArgs);
		LOGGER.debug("Help instances retrieval took {} ms", Double.valueOf(tracker.stopInSeconds()));
		return getProperties(searchArgs.getResult());
	}

	/**
	 * Extract the id to target mapping for all instances.
	 *
	 * @param instances
	 *            the instances from which to extract the properties
	 * @return the id to target mapping
	 */
	private static Map<String, String> getProperties(List<Instance> instances) {
		Map<String, String> targetToIdMapping = new HashMap<>(instances.size());
		for (Instance instance : instances) {
			targetToIdMapping.putAll(instance.getAsCollection(HELP_TARGET, ArrayList::new).stream().collect(
					Collectors.toMap(Serializable::toString, target -> instance.getId().toString())));
		}
		return targetToIdMapping;
	}
}
