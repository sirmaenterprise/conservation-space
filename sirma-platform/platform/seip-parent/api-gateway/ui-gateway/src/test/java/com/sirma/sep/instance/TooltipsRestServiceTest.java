package com.sirma.sep.instance;

import com.sirma.itt.seip.instance.tooltip.InstanceTooltipsRestService;
import com.sirma.itt.seip.instance.tooltip.InstanceTooltipsService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test for TooltipsRestService class.
 *
 * @author nvelkov
 */
public class TooltipsRestServiceTest {

	@Mock
	private InstanceTooltipsService tooltipsService;

	@InjectMocks
	private InstanceTooltipsRestService restService;

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the getTooltip method.
	 */
	@Test
	public void getTooltipTest() {
		restService.getTooltip("instanceId");
		Mockito.verify(tooltipsService).getTooltip("instanceId");
	}
}
