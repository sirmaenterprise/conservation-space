package com.sirma.itt.seip.instance.tooltip;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

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
public class InstanceTooltipsServiceImplTest {

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
		when(domainInstanceService.loadInstance(Matchers.anyString()))
				.thenThrow(InstanceNotFoundException.class);
		tooltipsService.getTooltip("test");
	}

	@Test
	public void getTooltip_shouldLoadThumbnailImageIfMissing() {
		String instanceId = "emf:test";
		Instance instance = new EmfInstance(instanceId);
		when(domainInstanceService.loadInstance(instanceId)).thenReturn(instance);
		when(headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP))
				.thenReturn("header");
		when(renditionService.getThumbnail(instance.getId())).thenReturn("some thumbnail");
		tooltipsService.getTooltip(instanceId);
		assertNotNull(instance.get(DefaultProperties.THUMBNAIL_IMAGE));
	}

	@Test
	public void testTooltipRetrievalMissingHeader() {
		Instance instance = new EmfInstance();
		when(domainInstanceService.loadInstance(Matchers.anyString())).thenReturn(new EmfInstance());
		when(headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP))
				.thenReturn(null);
		String tooltip = tooltipsService.getTooltip("test");
		assertNull(tooltip);
	}

	@Test
	public void testTooltipRetrieval() {
		Instance instance = new EmfInstance();
		when(domainInstanceService.loadInstance(Matchers.anyString())).thenReturn(new EmfInstance());
		when(headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP))
				.thenReturn("header");
		String tooltip = tooltipsService.getTooltip("test");
		assertEquals("header", tooltip);
	}
}
