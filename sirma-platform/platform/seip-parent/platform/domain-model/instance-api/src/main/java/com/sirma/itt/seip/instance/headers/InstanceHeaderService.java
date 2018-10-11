package com.sirma.itt.seip.instance.headers;

import java.util.Optional;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Service that manages the static instance label used for representing the instance and for primary search and sorting.
 * The service stores the header formats for all registered definitions and if change in the format is detected a
 * database reindexing is triggered to update the data. Only single format per definition type is supported.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
public interface InstanceHeaderService {

	/**
	 * Register header for the given definition. Call to this method will trigger header reindexing in the following
	 * cases:
	 * <ul>
	 * <li>There is nothing registered for the give definition</li>
	 * <li>There is registered header for the given definition and the formats differ</li>
	 * </ul>
	 *
	 * @param definitionId the definition identifier that will have a header assigned to
	 * @param headerValue the header value to assign to the given definition
	 */
	void trackHeader(String definitionId, String headerValue);

	/**
	 * Return the stored header format for the given definition id
	 *
	 * @param definitionId the requested definition identifier
	 * @return the found header if any
	 */
	Optional<String> getHeader(String definitionId);

	/**
	 * Evaluate the instance header tracked by the service. The result of the service is sanitized header stripped from
	 * all html tags.
	 *
	 * @param instance the source instance used for generating the header
	 * @return the result header value of such is supported
	 */
	Optional<String> evaluateHeader(Instance instance);

	/**
	 * Perform instance reindexing for the given definition. The request will ignore any current persisted values and
	 * will override them with newly generated values based on the persisted label for the given definition.
	 *
	 * @param definitionId the definition id to update.
	 */
	void reindexDefinition(String definitionId);
}
