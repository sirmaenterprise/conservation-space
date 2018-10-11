package com.sirma.itt.seip.instance.tooltip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.sep.content.rendition.RenditionService;

/**
 * Test the instance tooltip service.
 *
 * @author nvelkov
 */
public class InstanceTooltipsServiceTest {

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private HeadersService headersService;
	
	@Mock
	private RenditionService renditionService;

	@InjectMocks
	private InstanceTooltipsService tooltipsService = new InstanceTooltipsServiceImpl();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = InstanceNotFoundException.class)
	public void testTooltipRetrievalMissingInstance() {
		Mockito.when(domainInstanceService.loadInstance(Matchers.anyString()))
				.thenThrow(InstanceNotFoundException.class);
		tooltipsService.getTooltip("test");
	}

	@Test
	public void testTooltipRetrievalMissingHeader() {
		Instance instance = new EmfInstance();
		Mockito.when(domainInstanceService.loadInstance(Matchers.anyString())).thenReturn(new EmfInstance());
		Mockito.when(headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP))
				.thenReturn(null);
		String tooltip = tooltipsService.getTooltip("test");
		Assert.assertNull(tooltip);
	}

	@Test
	public void testTooltipRetrieval() {
		Instance instance = new EmfInstance();
		Mockito.when(domainInstanceService.loadInstance(Matchers.anyString())).thenReturn(new EmfInstance());
		Mockito.when(headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP))
				.thenReturn("header");
		String tooltip = tooltipsService.getTooltip("test");
		Assert.assertEquals("header", tooltip);
	}
}
