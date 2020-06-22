package com.sirma.itt.emf.audit.processor;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.observer.PropertyActionResolver;

/**
 * Test for {@link AuditActivityReducer}
 *
 * @author BBonev
 */
public class AuditActivityReducerTest {

	@InjectMocks
	private AuditActivityReducer activityReducer;
	@Mock
	private PropertyActionResolver actionResolver;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(actionResolver.getAddActionForProperty("emf:references")).thenReturn("changeReferences");
		when(actionResolver.getRemoveActionForProperty("emf:references")).thenReturn("changeReferences");
		when(actionResolver.getChangeActionForProperty("emf:references")).thenReturn("changeReferences");
	}

	@Test
	public void shouldReturnEmptyStreamOnNoActivities() throws Exception {
		assertEquals(0L, activityReducer.reduce(null).count());
		assertEquals(0L, activityReducer.reduce(Collections.emptyList()).count());
	}

	@Test
	public void reduceShouldCollapseEntiesWithSameRelationAndInstance() throws Exception {
		List<AuditActivity> activities = new ArrayList<>();
		activities.add(buildInstanceActivity("rq11", "emf:instance_1", "changeReferences"));

		activities.add(buildRelationAdd("rq11", "emf:instance_1", "emf:references", "emf:instance_2"));
		activities.add(buildRelationAdd("rq11", "emf:instance_1", "emf:references", "emf:instance_3"));
		activities.add(buildRelationRemove("rq11", "emf:instance_1", "emf:references", "emf:instance_4"));

		List<StoredAuditActivity> reducedActivities = activityReducer.reduce(activities).collect(toList());
		assertEquals(1, reducedActivities.size());
		StoredAuditActivity activity = reducedActivities.get(0);
		assertEquals(2, activity.getAddedTargetProperties().size());
		assertEquals(1, activity.getRemovedTargetProperties().size());
	}

	@Test
	public void reduceShouldNotCollapseEntiesWithDifferentRequestId() throws Exception {
		List<AuditActivity> activities = new ArrayList<>();
		activities.add(buildInstanceActivity("rq11", "emf:instance_1", "changeReferences"));
		activities.add(buildInstanceActivity("rq12", "emf:instance_1", "changeReferences"));

		List<StoredAuditActivity> reducedActivities = activityReducer.reduce(activities).collect(toList());
		assertEquals(2, reducedActivities.size());
	}

	@Test
	public void reduceShouldNotCollapseEntiesWithDifferentInstanceIds() throws Exception {
		List<AuditActivity> activities = new ArrayList<>();
		activities.add(buildInstanceActivity("rq11", "emf:instance_1", "changeReferences"));

		activities.add(buildRelationAdd("rq11", "emf:instance_1", "emf:references", "emf:instance_2"));
		activities.add(buildRelationRemove("rq11", "emf:instance_2", "emf:references", "emf:instance_1"));
		activities.add(buildRelationAdd("rq11", "emf:instance_1", "emf:references", "emf:instance_3"));
		activities.add(buildRelationRemove("rq11", "emf:instance_3", "emf:references", "emf:instance_1"));
		activities.add(buildRelationRemove("rq11", "emf:instance_1", "emf:references", "emf:instance_4"));

		List<StoredAuditActivity> reducedActivities = activityReducer.reduce(activities).collect(toList());
		assertEquals(3, reducedActivities.size());
	}

	private static AuditActivity buildRelationAdd(String request, String instanceId, String relation,
			String targetProperty) {
		AuditActivity activity = new AuditActivity();
		activity.setRelationStatus(AuditActivity.STATUS_ADDED);
		activity.setRequestId(request);
		activity.setObjectSystemID(instanceId);
		activity.setRelationId(relation);
		activity.setTargetProperties(targetProperty);
		activity.setEventDate(new Date());
		return activity;
	}

	private static AuditActivity buildRelationRemove(String request, String instanceId, String relation,
			String targetProperty) {
		AuditActivity activity = new AuditActivity();
		activity.setRelationStatus(AuditActivity.STATUS_REMOVED);
		activity.setRequestId(request);
		activity.setObjectSystemID(instanceId);
		activity.setRelationId(relation);
		activity.setTargetProperties(targetProperty);
		activity.setEventDate(new Date());
		return activity;
	}

	private static AuditActivity buildInstanceActivity(String request, String instanceId, String action) {
		AuditActivity activity = new AuditActivity();
		activity.setRequestId(request);
		activity.setObjectSystemID(instanceId);
		activity.setActionID(action);
		activity.setEventDate(new Date());
		return activity;
	}
}
