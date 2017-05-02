package com.sirma.itt.emf.audit.processor;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.hasNotNullProperty;
import static com.sirma.itt.seip.util.EqualsHelper.hasNullProperty;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.observer.PropertyActionResolver;

/**
 * Implements the algorithm for audit activities reducing and combining.
 * <p>
 * For the given collection of audit activities are performed the following reduce steps:
 * <ol>
 * <li>Group all activities for the same request id
 * <li>For single request:
 * <ol>
 * <li>combine all target properties for the same added and removed relation
 * <li>resolve the relation action based on the relation and the purpose (add or remove) using the
 * {@link PropertyActionResolver}
 * <li>if the resolved action matches the primary action operation combine the information for all of them
 * <li>if there are add and remove events for the same relation combine them if configured (non <code>null</code> value
 * returned from the method {@link PropertyActionResolver#getChangeActionForProperty(String)})
 * <li>
 * </ol>
 * </ol>
 *
 * @author BBonev
 */
@Singleton
public class AuditActivityReducer {

	@Inject
	private PropertyActionResolver actionResolver;

	/**
	 * Perform the audit activity reduce algorithm.
	 *
	 * @param activities
	 *            the activities to convert and merge
	 * @return the stream of merged audit activities
	 */
	public Stream<StoredAuditActivity> reduce(Collection<AuditActivity> activities) {
		if (isEmpty(activities)) {
			return Stream.empty();
		}

		return groupByRequestId(activities)
				.flatMap(activitiesPerRequest -> reduceForSingleRequest(activitiesPerRequest))
					.flatMap(activitiesPerInstance -> reduceForSingleInstance(activitiesPerInstance));
	}

	private static Stream<List<StoredAuditActivity>> groupByRequestId(Collection<AuditActivity> activities) {
		return activities
				.stream()
					.map(toStoredActivity())
					.collect(groupingBy(StoredAuditActivity::getRequestId))
					.values()
					.stream();
	}

	private static Stream<List<StoredAuditActivity>> reduceForSingleRequest(
			Collection<StoredAuditActivity> activities) {
		return activities.stream().collect(groupingBy(StoredAuditActivity::getInstanceId)).values().stream();
	}

	/**
	 * Will merge all activities that has the same relation without taking into account the operation.
	 *
	 * @param activities
	 * @return
	 */
	private Stream<StoredAuditActivity> reduceForSingleInstance(Collection<StoredAuditActivity> activities) {
		// merge entries that are for a single instance and have the same relation id regardless of the operation
		// (add/remove). Also fill in the relation actions based on the relation
		Stream<StoredAuditActivity> groupedByRelation = activities
				.stream()
					.filter(hasNotNullProperty(StoredAuditActivity::getRelation))
					.map(fillRelationAction())
					.collect(groupingBy(StoredAuditActivity::getRelation))
					.values()
					.stream()
					.map(mergeAllActivities());

		// all other activities that are non relation
		Stream<StoredAuditActivity> nonRelationActivities = activities
				.stream()
					.filter(hasNullProperty(StoredAuditActivity::getRelation));

		return Stream
				.concat(groupedByRelation, nonRelationActivities)
					.filter(hasNotNullProperty(StoredAuditActivity::getAction))
					.collect(groupingBy(StoredAuditActivity::getAction))
					.values()
					.stream()
					.map(mergeAllActivities());
	}

	/**
	 * Merge entries that have the same relation id to a single entry
	 */
	private Function<List<StoredAuditActivity>, StoredAuditActivity> mergeAllActivities() {
		return activities -> activities.stream().collect(reducing(new StoredAuditActivity(), mergeActivities()));
	}

	private BinaryOperator<StoredAuditActivity> mergeActivities() {
		return (existing, newData) -> {
			copyActivityData(existing, newData);
			// if for single relation we have 'add' and 'remove' change the operation to 'change'
			if (!existing.getAddedTargetProperties().isEmpty() && !existing.getRemovedTargetProperties().isEmpty()) {
				existing.setOperation(AuditActivity.STATUS_CHANGE);
				// if relation has a specific action configured use it otherwise use the current value
				String changeAction = getActionForOperation(existing.getRelation(), existing.getOperation());
				existing.setAction(getOrDefault(changeAction, existing.getAction()));
			}
			return existing;
		};
	}

	private static void copyActivityData(StoredAuditActivity existing, StoredAuditActivity newData) {
		existing.setAction(getOrDefault(newData.getAction(), existing.getAction()));
		existing.setInstanceId(getOrDefault(newData.getInstanceId(), existing.getInstanceId()));
		existing.setInstanceType(getOrDefault(newData.getInstanceType(), existing.getInstanceType()));
		existing.setOperation(getOrDefault(newData.getOperation(), existing.getOperation()));
		existing.setRelation(getOrDefault(newData.getRelation(), existing.getRelation()));
		existing.setState(getOrDefault(newData.getState(), existing.getState()));
		existing.setRequestId(getOrDefault(newData.getRequestId(), existing.getRequestId()));
		existing.setTimestamp(getOrDefault(newData.getTimestamp(), existing.getTimestamp()));
		existing.setUserId(getOrDefault(newData.getUserId(), existing.getUserId()));

		newData.getAddedTargetProperties().forEach(existing::addAddedTargetProperty);
		newData.getRemovedTargetProperties().forEach(existing::addRemovedTargetProperty);
		newData.getIds().forEach(existing::addId);
	}

	private Function<StoredAuditActivity, StoredAuditActivity> fillRelationAction() {
		return activity -> {
			if (activity.getAction() == null) {
				activity.setAction(getActionForOperation(activity.getRelation(), activity.getOperation()));
			}
			return activity;
		};
	}

	private String getActionForOperation(String relation, String operation) {
		if (nullSafeEquals(AuditActivity.STATUS_ADDED, operation)) {
			return actionResolver.getAddActionForProperty(relation);
		} else if (nullSafeEquals(AuditActivity.STATUS_REMOVED, operation)) {
			return actionResolver.getRemoveActionForProperty(relation);
		} else if (nullSafeEquals(AuditActivity.STATUS_CHANGE, operation)) {
			return actionResolver.getChangeActionForProperty(relation);
		}
		return null;
	}

	private static Function<AuditActivity, StoredAuditActivity> toStoredActivity() {
		return activity -> {
			StoredAuditActivity result = new StoredAuditActivity();
			result.setAction(activity.getActionID());
			result.addId(activity.getId());
			result.setInstanceId(activity.getObjectSystemID());
			result.setInstanceType(activity.getObjectInstanceType());
			result.setOperation(activity.getRelationStatus());
			result.setRelation(activity.getRelationId());
			result.setState(activity.getObjectState());
			result.setRequestId(activity.getRequestId());
			if (nullSafeEquals(AuditActivity.STATUS_ADDED, activity.getRelationStatus())) {
				result.addAddedTargetProperty(activity.getTargetProperties());
			} else if (nullSafeEquals(AuditActivity.STATUS_REMOVED, activity.getRelationStatus())) {
				result.addRemovedTargetProperty(activity.getTargetProperties());
			}
				result.setTimestamp(activity.getEventDate());
			result.setUserId(activity.getUserId());

			if (result.getInstanceId() == null) {
				// there are requests that are not assigned to instance id so we will generate one not to break the
				// group functionalities
				result.setInstanceId(UUID.randomUUID().toString());
			}
			// for old audit events before request Id was added
			if (result.getRequestId() == null) {
				result.setRequestId(UUID.randomUUID().toString());
			}
			return result;
		};
	}

}
