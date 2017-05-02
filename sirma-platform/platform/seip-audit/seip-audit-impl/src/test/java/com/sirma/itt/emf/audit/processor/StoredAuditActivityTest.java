package com.sirma.itt.emf.audit.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link StoredAuditActivity}
 *
 * @author BBonev
 */
public class StoredAuditActivityTest {

	@Test
	public void toMapMethodShouldReturnAllData() throws Exception {
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setInstanceId("instanceId");
		activity.setInstanceType("instanceType");
		activity.setRelation("relation");
		activity.setUserId("userId");
		activity.setTimestamp(new Date());
		activity.setOperation("operation");
		activity.setRequestId("requestId");
		activity.addAddedTargetProperty("addedProperty");
		activity.addAddedTargetProperty("addedProperty2");
		activity.addRemovedTargetProperty("removedProperty");
		activity.addRemovedTargetProperty("removedProperty3");
		activity.addId(1L);
		activity.addId(2L);

		Map<String, Object> map = activity.toMap();
		assertNotNull(map);

		StoredAuditActivity copy = new StoredAuditActivity(map);

		assertEquals(activity.getAction(), copy.getAction());
		assertEquals(activity.getInstanceId(), copy.getInstanceId());
		assertEquals(activity.getInstanceType(), copy.getInstanceType());
		assertEquals(activity.getOperation(), copy.getOperation());
		assertEquals(activity.getRelation(), copy.getRelation());
		assertEquals(activity.getRequestId(), copy.getRequestId());
		assertEquals(activity.getTimestamp(), copy.getTimestamp());
		assertEquals(activity.getUserId(), copy.getUserId());
		assertEquals(activity.getAddedTargetProperties(), copy.getAddedTargetProperties());
		assertEquals(activity.getRemovedTargetProperties(), copy.getRemovedTargetProperties());
		assertEquals(activity.getIds(), copy.getIds());
	}

	@Test
	public void getIdsShouldReturnNonNullValue() throws Exception {
		assertNotNull(new StoredAuditActivity().getIds());
	}

	@Test
	public void getAddedTargetPropertiesShouldReturnNonNullValue() throws Exception {
		assertNotNull(new StoredAuditActivity().getAddedTargetProperties());
	}

	@Test
	public void getRemovedTargetPropertiesShouldReturnNonNullValue() throws Exception {
		assertNotNull(new StoredAuditActivity().getRemovedTargetProperties());
	}
}
