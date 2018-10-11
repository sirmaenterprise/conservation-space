package com.sirma.itt.seip.instance.draft;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link DraftEntityId}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class DraftEntityIdTest {

	@Test
	public void toString_returnsCorrectData() {
		DraftEntityId entityId = new DraftEntityId();
		entityId.setInstanceId("instance-id");
		entityId.setUserId("user-id");
		assertEquals("DraftEntityId [instanceId=instance-id, userId=user-id]", entityId.toString());
	}

}
