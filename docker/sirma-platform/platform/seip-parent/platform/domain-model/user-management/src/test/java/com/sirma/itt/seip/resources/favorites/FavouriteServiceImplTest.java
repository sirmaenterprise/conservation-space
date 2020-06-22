/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HAS_FAVOURITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * General test for FavouritesServiceImpl.
 *
 * @author A. Kunchev
 */
@Test
public class FavouriteServiceImplTest {

	private static final String TEST_DEFAULT_HEADER = "<span class=\"truncate-element\"><span class=\"glyphicons"
			+ " dislikes favourites\" title=\"Add to favourites\" data-instanceId=\"emf:f5ea2f20-94e5-45c1-9ba0-"
			+ "0adef7ec3493\" ></span><a class=\"SUBMITTED emf:f5ea2f20-94e5-45c1-9ba0-0adef7ec3493 instance-link"
			+ " has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:f5ea2f20-94e5-45c1-9ba0"
			+ "-0adef7ec3493\" uid=\"282\"><b><span data-property=\"identifier\">282</span><span data-property=\"type\""
			+ "> (Project for testing)</span><span data-property=\"title\"> Alabala Starts everywhere</span><span "
			+ "data-property=\"status\"> (Submitted)</span></b></a></span><br /><span><label>Собственик: </label><span "
			+ "data-property=\"owner\"><a href=\"javascript:void(0)\">admin admin</a></span></span><span><label>, "
			+ "Създаден на: </label><span data-property=\"createdOn\">25.06.2015, 00:00</spann></span>";

	@InjectMocks
	private FavouritesServiceImpl service = new FavouritesServiceImpl();

	@Mock
	private LinkService chainingLinkService;

	@Mock
	private LinkService linkService;

	@Mock
	private EventService eventService;

	@Mock
	SecurityContext securityContext;

	@Mock
	private TypeConverter typeConverter;

	/**
	 * Initialise test mocks and data.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);

		EmfUser user = new EmfUser("admin");
		user.setId("emf:admin");
		when(securityContext.getAuthenticated()).thenReturn(user);

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(EmfUser.class))).then(a -> {
			Instance instance = a.getArgumentAt(1, Instance.class);
			return InstanceReferenceMock.createGeneric(instance, new DataTypeDefinitionMock(instance));
		});
		doNothing().when(eventService).fire(any(EmfEvent.class), any(Annotation.class));
	}

	// --------------------------------------------------------------------------
	// -------------------------------- ADD -------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference: null
	 *
	 * Expected result  : false
	 * </pre>
	 */
	public void add_nullParam_resultFalse() {
		boolean result = service.add(null);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference : not null
	 * Link              : created
	 *
	 * Expected result   : true
	 * </pre>
	 */
	public void add_notNullParam_linkCreated_resultTrue() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(chainingLinkService.linkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString()))
				.thenReturn(true);
		boolean result = service.add(instanceReference);
		assertEquals(true, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference : not null
	 * Link              : not created
	 *
	 * Expected result   : false
	 * </pre>
	 */
	public void add_notNullParam_linkNotCreated_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(chainingLinkService.linkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString()))
				.thenReturn(false);
		boolean result = service.add(instanceReference);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullFirstParam_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		boolean result = service.add(null, instanceReference);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullSecondParam_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		boolean result = service.add(instanceReference, null);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullBothParams_resultFalse() {
		boolean result = service.add(null, null);
		assertEquals(false, result);
	}

	// --------------------------------------------------------------------------
	// ----------------------------- REMOVE -------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: remove(InstanceReference)
	 *
	 * InstanceReference : null
	 *
	 * Expected result   : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void remove_nullParam() {
		service.remove(null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullFirstParam() {
		InstanceReference user = mock(InstanceReference.class);
		service.remove(null, user);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullSecondParam() {
		InstanceReference instance = mock(InstanceReference.class);
		service.remove(instance, null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullBothParams() {
		service.remove(null, null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.unlinkSimple called at least once
	 * </pre>
	 */
	public void removeOverloaded_NotNullParams() {
		InstanceReference reference = mock(InstanceReference.class);
		InstanceReference user = mock(InstanceReference.class);
		service.remove(reference, user);
		verify(chainingLinkService, atLeastOnce()).unlinkSimple(user, reference, LinkConstants.HAS_FAVOURITE);
	}

	// --------------------------------------------------------------------------
	// ----------------------------- GET ----------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getAllForCurrentUser
	 *
	 * Favourite instances : no
	 *
	 * Expected result     : empty collection
	 * </pre>
	 */
	public void getAllForCurrentUser_noFavouriteInstances_resultEmptyList() {
		Collection<InstanceReference> favouriteInstances = service.getAllForCurrentUser();
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		assertEquals(new LinkIterable<>(Collections.emptySet(), false).size(), favouriteInstances.size());
	}

	/**
	 * <pre>
	 * Method: getAllForCurrentUser
	 *
	 * Favourite instances : one instance
	 *
	 * Expected result     : collection with one instance
	 * </pre>
	 */
	public void getAllForCurrentUser_notEmptyFavouriteInstances_resultAtLeastOne() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		LinkReference linkReference = new LinkReference();
		linkReference.setTo(instanceReference);
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(linkReference));
		Collection<InstanceReference> favouriteInstances = service.getAllForCurrentUser();
		assertEquals(new LinkIterable<>(Arrays.asList(linkReference)).size(), favouriteInstances.size());
	}

	/**
	 * <pre>
	 * Method: getAllForUser(UserInstanceReference)
	 *
	 * UserInstanceReference : null
	 *
	 * Expected result       : empty collection
	 * </pre>
	 */
	public void getAllForUser_noUser_resultEmptyList() {
		Collection<InstanceReference> favouriteInstances = service.getAllForUser(null);
		assertEquals(Collections.emptyList(), favouriteInstances);
	}

	// --------------------------------------------------------------------------
	// -------------------- UPDATE FAVOURITE STATE ------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: updateFavouriteStateForInstance(Instance)
	 *
	 * Instance        : null
	 *
	 * Expected result : null
	 * </pre>
	 */
	public void updateFavouriteStateForInstance_nullInstance_resultNull() {
		Instance instance = service.updateFavoriteStateForInstance(null);
		assertNull(instance);
	}

	/**
	 * <pre>
	 * Method: updateFavouriteStateForInstance(Instance)
	 *
	 * Instance        : not null
	 * In Favourites   : false
	 *
	 * Expected result : unchanged instance
	 * </pre>
	 */
	public void updateFavouriteStateForInstance_notInFavourite_sameInstance() {
		EmfInstance emfInstance = new EmfInstance();
		emfInstance.setIdentifier("123");
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString())).thenReturn(Collections.emptyList());
		EmfInstance sameInstance = service.updateFavoriteStateForInstance(emfInstance);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		assertEquals(emfInstance, sameInstance);
	}

	/**
	 * <pre>
	 * Method: updateFavouriteStateForInstance(Instance)
	 *
	 * Instance        : not null
	 * In Favourites   : true
	 *
	 * Expected result : updated instance
	 * </pre>
	 */
	public void updateFavouriteStateForInstance_notNullParams_updateInstance() {
		EmfInstance instance = prepareInstance();
		List<Instance> list = Arrays.asList(instance);
		service.updateFavouriteStateForInstances(list);
		Instance updatedInstance = service.updateFavoriteStateForInstance(instance);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		Assert.assertNotNull(updatedInstance);
		assertEquals(updatedInstance.get(HAS_FAVOURITE), Boolean.TRUE);
	}

	/**
	 * <pre>
	 * Method: updateFavouriteStateForInstances(Collection)
	 *
	 * Collection      : empty
	 *
	 * Expected result : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateFavouriteStateForInstances_emptyCollection() {
		service.updateFavouriteStateForInstances(Collections.emptyList());
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateFavouriteState(Collection)
	 *
	 * Collection      : null
	 *
	 * Expected result : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateFavouriteStateForInstances_nullCollection() {
		service.updateFavouriteStateForInstances(null);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateFavouriteState(Collection, UserInstanceReference)
	 *
	 * Collection            : with one instance
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateFavouriteStateForInstancesOverloaded_nullUser() {
		List<Instance> list = Arrays.asList(new EmfInstance());
		service.updateFavouriteStateForInstances(list, null);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateFavouriteState(Collection, UserInstanceReference)
	 *
	 * Collection            : with one instance
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.getSimplelinks called at least once
	 * </pre>
	 */
	public void updateFavouriteStateForInstancesOverloaded_notNullBothParams() {
		EmfInstance instance = prepareInstance();
		List<Instance> list = Arrays.asList(instance);
		service.updateFavouriteStateForInstances(list);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	// --------------------------------------------------------------------------
	// -------------------------- REUSABLE METHODS ------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Prepares test instance and mocks some services.
	 *
	 * @return mocked instance
	 */
	private EmfInstance prepareInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("123");
		HashMap<String, Serializable> properties = new HashMap<>(1);
		properties.put(DefaultProperties.HEADER_DEFAULT, TEST_DEFAULT_HEADER);
		instance.setProperties(properties);
		ReflectionUtils.setFieldValue(instance, "reference",
				InstanceReferenceMock.createGeneric(instance, new DataTypeDefinitionMock(instance)));
		LinkReference linkReference = new LinkReference();
		linkReference.setTo(instance.toReference());
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(linkReference));
		return instance;
	}
}
