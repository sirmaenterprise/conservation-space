package com.sirma.itt.seip.instance.version.compare;

import java.io.Serializable;

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

	/** Authentication used when constructing version download url */
	private static final String AUTHENTICATION = "authentication";

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
	 * @param auth
	 *            Authentication used when constructing version download url
	 * @return new {@link VersionCompareContext}
	 */
	public static VersionCompareContext create(Serializable firstIdentifier, Serializable secondIdentifier, String auth) {
		VersionCompareContext context = new VersionCompareContext();
		context.put(FIRST_SOURCE, firstIdentifier);
		context.put(SECOND_SOURCE, secondIdentifier);
		context.put(AUTHENTICATION, auth);
		return context;
	}

	public Serializable getFirstIdentifier() {
		return getIfSameType(FIRST_SOURCE, Serializable.class);
	}

	public Serializable getSecondIdentifier() {
		return getIfSameType(SECOND_SOURCE, Serializable.class);
	}

	public String getAuthentication() {
		return getIfSameType(AUTHENTICATION, String.class);
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
