package com.sirma.itt.seip.instance.event;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Tests for {@link ParentChangedEvent}.
 *
 * @author Boyan Tonchev.
 */
public class ParentChangedEventTest {

	@Test
	public void should_ArgumentsPassedToConstructorBeProperlySet_When_EventIsCreated() {
		Instance instance = Mockito.mock(Instance.class);
		Instance oldParent = Mockito.mock(Instance.class);
		Instance newParent = Mockito.mock(Instance.class);

		ParentChangedEvent event = new ParentChangedEvent(instance, oldParent, newParent);

		assertEquals(instance, event.getInstance());
		assertEquals(oldParent, event.getOldParent());
		assertEquals(newParent, event.getNewParent());
	}
}