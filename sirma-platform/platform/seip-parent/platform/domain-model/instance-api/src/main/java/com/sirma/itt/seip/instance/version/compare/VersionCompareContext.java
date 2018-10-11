package com.sirma.itt.seip.instance.version.compare;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Contains required information for successful content compare. The primary data stored in the context are the
 * identifiers of the versions which content will be compared and the identifier of the instance from which the version
 * originates.
 *
 * @author A. Kunchev
 */
public class VersionCompareContext extends Context<String, Object> {

	private static final long serialVersionUID = 8309459429471652228L;

	private static final int INITIAL_CONTEXT_MAP_SIZE = 3;

	private static final String FIRST_SOURCE = "firstSource";
	private static final String SECOND_SOURCE = "secondSource";
	private static final String ORIGINAL_INSTANCE_ID = "instanceId";

	/**
	 * Those are used later to get the authentication for downloading the files we need to compare.
	 */
	private static final String AUTHENTICATION_HEADERS = "authenticationHeaders";

	private VersionCompareContext() {
		super(INITIAL_CONTEXT_MAP_SIZE);
	}

	/**
	 * Creates new context and sets the identifiers of the versions which content will be compared.
	 *
	 * @param firstIdentifier
	 *            the instance id of the first version
	 * @param secondIdentifier
	 *            the instance id of the second version
	 * @param headers
	 *            the authentication headers retrieved from the current request. Those are used later to download the
	 *            files we need to compare
	 * @return new {@link VersionCompareContext}
	 */
	public static VersionCompareContext create(Serializable firstIdentifier, Serializable secondIdentifier,
			Map<String, String> headers) {
		VersionCompareContext context = new VersionCompareContext();
		context.put(FIRST_SOURCE, firstIdentifier);
		context.put(SECOND_SOURCE, secondIdentifier);
		context.put(AUTHENTICATION_HEADERS, headers);
		return context;
	}

	public Serializable getFirstIdentifier() {
		return getIfSameType(FIRST_SOURCE, Serializable.class);
	}

	public Serializable getSecondIdentifier() {
		return getIfSameType(SECOND_SOURCE, Serializable.class);
	}

	public Map<String, String> getAuthenticationHeaders() {
		return getIfSameType(AUTHENTICATION_HEADERS, Map.class);
	}

	/**
	 * Setter for the id of the instance from which are the compared versions. Primary used for some validations.
	 *
	 * @param id
	 *            of the original instance from which the version are coming
	 * @return current object to allow method chaining
	 */
	public VersionCompareContext setOriginalInstanceId(Serializable id) {
		put(ORIGINAL_INSTANCE_ID, id);
		return this;
	}

	/**
	 * Getter for the id of the instance from which are the compared versions. Primary used for some validations.
	 *
	 * @return the id of the original instance
	 */
	public Serializable getOriginalInstanceId() {
		return (Serializable) computeIfAbsent(ORIGINAL_INSTANCE_ID,
				unused -> InstanceVersionService.getIdFromVersionId(getFirstIdentifier()));
	}

}
