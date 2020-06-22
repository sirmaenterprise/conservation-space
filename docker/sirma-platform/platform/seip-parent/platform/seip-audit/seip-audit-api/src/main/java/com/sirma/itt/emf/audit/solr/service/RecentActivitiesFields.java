package com.sirma.itt.emf.audit.solr.service;

/**
 * Defines activities fields contents. Mainly used for converting solr documents to activities.
 *
 * @author A. Kunchev
 */
public final class RecentActivitiesFields {

	public static final String ADDED_TARGET_PROPERTIES = "addedTargetProperties";

	public static final String REMOVED_TARGET_PROPERTIES = "removedTargetProperties";

	public static final String IDS = "ids";

	public static final String INSTANCE_ID = "instanceId";

	public static final String INSTANCE_TYPE = "instanceType";

	public static final String RELATION = "relation";

	public static final String STATE = "state";

	public static final String USER_ID = "userId";

	public static final String TIMESTAMP = "timestamp";

	public static final String OPERATION = "operation";

	public static final String REQUEST_ID = "requestId";

	public static final String ACTION = "action";

	private RecentActivitiesFields() {
		// default constructor
	}

}
