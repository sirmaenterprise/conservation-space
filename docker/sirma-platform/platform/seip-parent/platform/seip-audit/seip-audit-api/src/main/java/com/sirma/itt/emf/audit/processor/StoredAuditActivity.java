package com.sirma.itt.emf.audit.processor;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * Audit ativity that is stored for indexing. The activity is generated from one or more {@link AuditActivity}s by the
 * {@link AuditActivityReducer}. Represents an information that is stored for indexing and then used for generating user
 * response to audit search requests.
 *
 * @author BBonev
 */
public class StoredAuditActivity {

	// the primary ids of all collapsed audit events
	private Collection<Long> ids;
	private String instanceId;
	private String instanceType;
	private String action;
	private String relation;
	private String state;
	private Collection<Serializable> addedTargetProperties;
	private Collection<Serializable> removedTargetProperties;
	private String userId;
	private Date timestamp;
	// add/remove/change
	private String operation;
	private String requestId;

	/**
	 * Instantiates a new audit activity.
	 */
	public StoredAuditActivity() {
		// nothing to do
	}

	/**
	 * Instantiates a new audit activity from the given mapping. The mapping should be produced by the method
	 * {@link #toMap()} or in the same format.
	 *
	 * @param source
	 *            the source mapping to read the data from
	 */
	@SuppressWarnings("unchecked")
	public StoredAuditActivity(Map<String, Object> source) {
		ids = (Collection<Long>) source.getOrDefault("ids", Collections.emptySet());
		instanceId = (String) source.get("instanceId");
		instanceType = (String) source.get("instanceType");
		action = (String) source.get("action");
		relation = (String) source.get("relation");
		addedTargetProperties = (Collection<Serializable>) source.getOrDefault("addedTargetProperties",
				Collections.emptySet());
		removedTargetProperties = (Collection<Serializable>) source.getOrDefault("removedTargetProperties",
				Collections.emptySet());
		userId = (String) source.get("userId");
		timestamp = (Date) source.get("timestamp");
		operation = (String) source.get("operation");
		requestId = (String) source.get("requestId");
	}

	public Collection<Long> getIds() {
		if (ids == null) {
			return Collections.emptySet();
		}
		return ids;
	}

	public void setIds(Set<Long> ids) {
		this.ids = ids;
	}

	/**
	 * Adds the id to the collection of ids
	 *
	 * @param id
	 *            the id
	 */
	public void addId(Long id) {
		if (ids == null) {
			ids = new HashSet<>(10);
		}
		addNonNullValue(ids, id);
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Collection<Serializable> getAddedTargetProperties() {
		if (addedTargetProperties == null) {
			return Collections.emptySet();
		}
		return addedTargetProperties;
	}

	public void setAddedTargetProperties(Set<Serializable> targetProperties) {
		addedTargetProperties = targetProperties;
	}

	/**
	 * Adds the target property to the collection of added properties
	 *
	 * @param property
	 *            the property
	 */
	public void addAddedTargetProperty(Serializable property) {
		if (addedTargetProperties == null) {
			addedTargetProperties = new HashSet<>();
		}
		addNonNullValue(addedTargetProperties, property);
	}

	public Collection<Serializable> getRemovedTargetProperties() {
		if (removedTargetProperties == null) {
			return Collections.emptySet();
		}
		return removedTargetProperties;
	}

	public void setRemovedTargetProperties(Set<Serializable> removedTargetProperties) {
		this.removedTargetProperties = removedTargetProperties;
	}

	/**
	 * Adds the target property to the collection of removed properties
	 *
	 * @param property
	 *            the property
	 */
	public void addRemovedTargetProperty(Serializable property) {
		if (removedTargetProperties == null) {
			removedTargetProperties = new HashSet<>();
		}
		addNonNullValue(removedTargetProperties, property);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return new StringBuilder(512)
				.append("StoredAuditActivity [requestId=")
					.append(requestId)
					.append(", userId=")
					.append(userId)
					.append(", instanceId=")
					.append(instanceId)
					.append(", action=")
					.append(action)
					.append(", relation=")
					.append(relation)
					.append(", state=")
					.append(state)
					.append(", instanceType=")
					.append(instanceType)
					.append(", addedTargetProperties=")
					.append(addedTargetProperties)
					.append(", removedTargetProperties=")
					.append(removedTargetProperties)
					.append(", timestamp=")
					.append(timestamp)
					.append(", operation=")
					.append(operation)
					.append(", ids=")
					.append(ids)
					.append("]")
					.toString();
	}

	/**
	 * Writes the contents of the current instance to mapping
	 *
	 * @return the map
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = createHashMap(12);

		addNonNullValue(map, "ids", ids);
		addNonNullValue(map, "instanceId", instanceId);
		addNonNullValue(map, "instanceType", instanceType);
		addNonNullValue(map, "action", action);
		addNonNullValue(map, "relation", relation);
		addNonNullValue(map, "state", state);
		addNonNullValue(map, "addedTargetProperties", addedTargetProperties);
		addNonNullValue(map, "removedTargetProperties", removedTargetProperties);
		addNonNullValue(map, "userId", userId);
		addNonNullValue(map, "timestamp", timestamp);
		addNonNullValue(map, "operation", operation);
		addNonNullValue(map, "requestId", requestId);

		return map;
	}
}
