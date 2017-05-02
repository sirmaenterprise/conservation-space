package com.sirma.cmf.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.seip.instance.tooltip.InstanceTooltipsService;

/**
 * Test for TooltipsRestService class.
 *
 * @author nvelkov
 */
public class TooltipsRestServiceTest extends CMFTest {

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
