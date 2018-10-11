/**
 *
 */
package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.RenditionServiceInstanceLoadDecorator;

/**
 * Test for RenditionServiceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Test
public class RenditionServiceInstanceLoadDecoratorTest {

	@InjectMocks
	private RenditionServiceInstanceLoadDecorator serviceDecorator = new RenditionServiceInstanceLoadDecorator();

	@Mock
	private RenditionService renditionService;

	/**
	 * Initialise test mocks.
	 */
	@BeforeClass
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * <pre>
	 * Method: decorateInstance
	 *
	 * Result: RenditionService.loadThumbnail called at least once
	 * </pre>
	 */
	public void decorateInstance_callRenditionService_loadThumbnail() {
		serviceDecorator.decorateInstance(any(Instance.class));
		verify(renditionService, atLeastOnce()).loadThumbnail(any(Instance.class));
	}

	/**
	 * <pre>
	 * Method: decorateResult
	 *
	 * Result: RenditionService.loadThumbnails called at least once
	 * </pre>
	 */
	public void decorateResult_callRenditionService_loadThumbnails() {
		serviceDecorator.decorateResult(Collections.emptyList());
		verify(renditionService, atLeastOnce()).loadThumbnails(Collections.emptyList());
	}
}
