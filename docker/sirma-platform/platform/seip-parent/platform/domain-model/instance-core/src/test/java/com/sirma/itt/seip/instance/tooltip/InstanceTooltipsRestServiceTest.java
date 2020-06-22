package com.sirma.itt.seip.instance.tooltip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link InstanceTooltipsRestService}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 08/12/2017
 */
public class InstanceTooltipsRestServiceTest {

	@InjectMocks
	private InstanceTooltipsRestService cut;
	@Mock
	private InstanceTooltipsService tooltipService;

	@Test
	public void getTooltip() {
		MockitoAnnotations.initMocks(this);
		String instanceId = "emf:id";
		when(tooltipService.getTooltip(instanceId)).thenReturn("header");
		String tooltip = cut.getTooltip(instanceId);
		verify(tooltipService).getTooltip(instanceId);
		assertEquals("header", tooltip);
	}
}