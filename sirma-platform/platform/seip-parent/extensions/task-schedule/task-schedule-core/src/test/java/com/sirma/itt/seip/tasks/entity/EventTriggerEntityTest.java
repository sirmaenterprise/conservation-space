package com.sirma.itt.seip.tasks.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link EventTriggerEntity}
 *
 * @author hlungov
 */
public class EventTriggerEntityTest {

	@Test
	public void testEquals() {
		EventTriggerEntity eventTriggerEntity = new EventTriggerEntity();
		eventTriggerEntity.setEventClassId(10);
		eventTriggerEntity.setUserOperation("userOperation");
		eventTriggerEntity.setSemanticTargetClass("semanticClass");
		eventTriggerEntity.setServerOperation("serverOperation");
		eventTriggerEntity.setTargetId("testId");

		EventTriggerEntity clone = eventTriggerEntity.createCopy();

		Assert.assertTrue(eventTriggerEntity.equals(clone));
	}
}
