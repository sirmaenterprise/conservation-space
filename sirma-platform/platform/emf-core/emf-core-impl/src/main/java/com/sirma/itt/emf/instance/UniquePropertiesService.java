package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.collections.ConcurrentMultiValueCollection;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;

/**
 * Service responsible for checking if an object with a given value for a specified property already exists.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class UniquePropertiesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SearchService searchService;

	@Inject
	private ConcurrentMultiValueCollection collection;

	/**
	 * Checks if the object is already persisted in the semantic db.
	 * <p>
	 * If the there is an object with the same properties, found in semantic db, but there is no uri passed, then it is
	 * certain, that another object with the same properties exists. If there is an object with the same properties
	 * ,found in the semantic db, and there is an uri passed, check the uri if it matches the one from the object.
	 *
	 * @param properties
	 *            the properties that must be unique
	 * @param uri
	 *            the uri of the object.
	 * @return true, if the object is persisted
	 */
	private boolean isObjectPersisted(Map<String, Serializable> properties, String uri) {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setArguments(properties);
		searchService.search(Instance.class, searchArgs);
		if (!searchArgs.getResult().isEmpty()) {
			return !searchArgs.getResult().get(0).getId().equals(uri);
		}
		return false;
	}

	/**
	 * Checks if there is an object with the same properties, currently being persisted or is already persisted. If
	 * there is an object found with the same property, checks if the passed uri matches the one from the found object.
	 * If it does then this is the same object and there is no other object with the same properties.
	 *
	 * @param properties
	 *            the properties that must be unique
	 * @param uri
	 *            the uri of the object.
	 * @return true, if there is no other object with the same properties, false otherwise
	 */
	public boolean objectExists(Map<String, Serializable> properties, String uri) {
		for (Entry<String, Serializable> property : properties.entrySet()) {
			if (isObjectCurrentlyBeingCreated(property.getKey(), property.getValue())) {
				return true;
			}
		}
		if (isObjectPersisted(properties, uri)) {
			LOGGER.debug("An object with uri '{}' already exists", uri);
			return true;
		}
		return false;
	}

	private boolean isObjectCurrentlyBeingCreated(String property, Serializable value) {
		if (collection.keyContains(property, value)) {
			LOGGER.debug("An object with {} '{}' is currently being created", property, value);
			return true;
		}
		collection.addToKey(property, value);
		collection.removeAfterTransaction(property, value);
		return false;
	}
}
