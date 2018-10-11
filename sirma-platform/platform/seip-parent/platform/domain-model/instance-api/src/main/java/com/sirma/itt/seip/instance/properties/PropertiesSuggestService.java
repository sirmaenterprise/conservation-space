package com.sirma.itt.seip.instance.properties;

import java.util.List;

/**
 * Service for suggesting values for object ids on instance create. If the property is single valued tries to
 * determine only one value from the context hierarchy and relations. If its multiple valued then performs semantic
 * search and returns all found instances' ids.
 *
 * @author svetlozar.iliev
 */
public interface PropertiesSuggestService {

	/**
	 * Returns suggested ids for given context id, the type of the property & a flag which governs what is the
	 * type of the returned result either a multiple or a single valued result can be returned
	 *
	 * @param contextId
	 *            the context id
	 * @param type
	 *            the type of the property
	 * @param multivalued
	 *            if the property allows multiple values
	 * @return list of the suggested values
	 */
	List<String> suggestPropertiesIds(String contextId, String type, boolean multivalued);
}
