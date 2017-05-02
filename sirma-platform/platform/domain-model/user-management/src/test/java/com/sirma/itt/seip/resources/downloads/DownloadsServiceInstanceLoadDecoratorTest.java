/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

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
import com.sirma.itt.seip.resources.downloads.DownloadsService;
import com.sirma.itt.seip.resources.downloads.DownloadsServiceInstanceLoadDecorator;

/**
 * Test for FavouritesServiceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Test
public class DownloadsServiceInstanceLoadDecoratorTest {

	@InjectMocks
	private DownloadsServiceInstanceLoadDecorator serviceDecorator = new DownloadsServiceInstanceLoadDecorator();

	@Mock
	private DownloadsService downloadsService;

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
	 * Result: DownloadsService.updateDownloadStateForInstance called at least once
	 * </pre>
	 */
	public void decorateInstance_callFavouritesService_updateInstance() {
		serviceDecorator.decorateInstance(any(Instance.class));
		verify(downloadsService, atLeastOnce()).updateDownloadStateForInstance(any(Instance.class));
	}

	/**
	 * <pre>
	 * Method: decorateResult
	 *
	 * Result: DownloadsService.updateDownloadStateForInstances called at least once
	 * </pre>
	 */
	public void decorateResult_callsFavoritesService_updateInstances() {
		serviceDecorator.decorateResult(Collections.emptyList());
		verify(downloadsService, atLeastOnce()).updateDownloadStateForInstances(Collections.emptyList());
	}
}
