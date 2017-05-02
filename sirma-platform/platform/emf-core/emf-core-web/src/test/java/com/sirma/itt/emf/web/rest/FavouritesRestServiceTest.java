/**
 *
 */
package com.sirma.itt.emf.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.web.rest.util.RestServiceTestsUtil;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.resources.favorites.FavouritesService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * General test for FavouritesRestService.
 *
 * @author A. Kunchev
 */
@Test
public class FavouritesRestServiceTest {

	private static final String JSON_DATA_NO_TYPE = "{\"instanceId\":\"123\",\"instanceType\":\"\"}";

	private static final String JSON_DATA = "{\"instanceId\":\"123\",\"instanceType\":\"instanceType\"}";

	private static final String WRONG_JSON_DATA = "{\"instanceId\123\",\"instanceType\":\"instanceType";

	@InjectMocks
	private FavouritesRestService favouritesRestService = new FavouritesRestService();

	@Mock
	private FavouritesService favoritesService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private EmfRestService emfRestService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private SecurityContext securityContext;

	/**
	 * Initialise test mocks.
	 */
	@BeforeClass
	public void setup() {
		MockitoAnnotations.initMocks(this);

	}

	// --------------------------------------------------------------------------
	// -------------------------- ADD FAVOURITES --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: addFavourite
	 *
	 * Request data: empty
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addFavourite_empty_data_badResponse() {
		favouritesRestService.addFavourite("");
	}

	/**
	 * <pre>
	 * Method: addFavourite
	 *
	 * Request data: null
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addFavourite_null_data_badResponse() {
		favouritesRestService.addFavourite(null);
	}

	/**
	 * <pre>
	 * Method: addFavourite
	 *
	 * Request data: wrong JSON
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addFavourite_nonexistent_instance_badResponse() {
		when(typeConverter.convert(Matchers.eq(InstanceReference.class), anyString())).thenReturn(null);
		favouritesRestService.addFavourite(WRONG_JSON_DATA);
	}

	/**
	 * <pre>
	 * Method: addFavourite
	 *
	 * Request data : correct
	 * Instance     : exists
	 * Link         : created
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void addFavourite_existent_instance_OKResponse_linkCreated() {
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		when(favoritesService.add(instanceReference)).thenReturn(true);
		Response response = favouritesRestService.addFavourite(JSON_DATA);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: addFavourite
	 *
	 * Request data : correct
	 * Instance     : exists
	 * Link         : not created
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void addFavourite_existent_instance_OKResponse_linkNotCreated() {
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		when(favoritesService.add(instanceReference)).thenReturn(false);
		when(labelProvider.getValue(anyString())).thenReturn("");
		Response response = favouritesRestService.addFavourite(JSON_DATA);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- REMOVE FAVOURITES -----------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: removeFavourite
	 *
	 * Request data: empty
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFavourite_empty_data_badResponse() {
		favouritesRestService.removeFavourite("");
	}

	/**
	 * <pre>
	 * Method: removeFavourite
	 *
	 * Request data: null
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFavourite_null_data_badResponse() {
		favouritesRestService.removeFavourite(null);
	}

	/**
	 * <pre>
	 * Method: removeFavourite
	 *
	 * Request data: missing type in JSON
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFavourite_nonexistent_instance_badResponse() {
		when(typeConverter.convert(Matchers.eq(InstanceReference.class), anyString())).thenReturn(null);
		favouritesRestService.removeFavourite(JSON_DATA_NO_TYPE);
	}

	/**
	 * <pre>
	 * Method: removeFavourite
	 *
	 * Request data: correct
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void removeFavourite_existent_instance_OKResponse() {
		Response response = favouritesRestService.removeFavourite(JSON_DATA);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- GET FAVOURITES --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getAllFavourites
	 *
	 * Favourite list: null
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getAllFavourites_nullFavoritesList_OKResponse() {
		RestServiceTestsUtil.prepareUser(favouritesRestService, labelProvider);
		when(favoritesService.getAllForCurrentUser()).thenReturn(null);
		Response response = favouritesRestService.getFavourites();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: getAllFavourites
	 *
	 * Favourite list: empty
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getAllFavourites_emptyFavoritesList_OKResponse() {
		RestServiceTestsUtil.prepareUser(favouritesRestService, labelProvider);
		when(favoritesService.getAllForCurrentUser()).thenReturn(Collections.emptyList());
		Response response = favouritesRestService.getFavourites();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: getAllFavourites
	 *
	 * Favourite list: at least one favourite instance
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getAllFavourites_notEmptyFavoritesList_OKResponse() {
		RestServiceTestsUtil.prepareUser(favouritesRestService, labelProvider);
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		List<InstanceReference> instances = new ArrayList<>();
		instances.add(instanceReference);
		when(favoritesService.getAllForCurrentUser()).thenReturn(instances);
		Response response = favouritesRestService.getFavourites();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

}
