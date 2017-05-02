/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

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
import com.sirma.itt.seip.resources.favorites.FavouritesService;
import com.sirma.itt.seip.resources.favorites.FavouritesServiceInstanceLoadDecorator;

/**
 * Test for FavouritesServiceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Test
public class FavouritesServiceInstanceLoadDecoratorTest {

	@InjectMocks
	private FavouritesServiceInstanceLoadDecorator serviceDecorator = new FavouritesServiceInstanceLoadDecorator();

	@Mock
	private FavouritesService favouritesService;

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
	 * Result: FavouritesService.updateFavoriteStateForInstance called at least once
	 * </pre>
	 */
	public void decorateInstance_callFavouritesService_updateInstance() {
		serviceDecorator.decorateInstance(any(Instance.class));
		verify(favouritesService, atLeastOnce()).updateFavoriteStateForInstance(any(Instance.class));
	}

	/**
	 * <pre>
	 * Method: decorateResult
	 *
	 * Result: FavouritesService.updateFavouriteState called at least once
	 * </pre>
	 */
	public void decorateResult_callsFavoritesService_updateInstances() {
		serviceDecorator.decorateResult(Collections.emptyList());
		verify(favouritesService, atLeastOnce()).updateFavouriteStateForInstances(Collections.emptyList());
	}
}
