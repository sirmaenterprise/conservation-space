package com.sirmaenterprise.sep.bpm.camunda.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * @author bbanchev
 */
public class CamundaBPMServiceTest {

	@Test(expected = NullPointerException.class)
	public void testIsActivityOnNull() throws Exception {
		CamundaBPMService.isActivity(null);
	}

	@Test
	public void testIsActivityFalse() throws Exception {
		Instance instance = mock(Instance.class);
		assertFalse(CamundaBPMService.isActivity(instance));
	}

	@Test
	public void testIsActivityTrue() throws Exception {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ProcessConstants.ACTIVITY_ID)).thenReturn(true);
		assertTrue(CamundaBPMService.isActivity(instance));
	}

	@Test(expected = NullPointerException.class)
	public void testGetActivityIdOnNull() throws Exception {
		CamundaBPMService.getActivityId(null);
	}

	@Test
	public void testGetActivityIdNull() throws Exception {
		Instance instance = mock(Instance.class);
		assertNull(CamundaBPMService.getActivityId(instance));
	}

	@Test
	public void testGetActivityIdNonNull() throws Exception {
		Instance instance = mock(Instance.class);
		when(instance.get(ProcessConstants.ACTIVITY_ID)).thenReturn("id");
		when(instance.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn("id");
		assertEquals("id", CamundaBPMService.getActivityId(instance));
	}

}
