package com.sirma.itt.emf.instance;

import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.concurrent.collections.ConcurrentMultiValueCollection;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Testing the {@link UniquePropertiesService}.
 *
 * @author nvelkov
 */
public class UniquePropertiesServiceTest extends EmfTest {

	/** The unique properties service. */
	@InjectMocks
	private UniquePropertiesService uniquePropertiesService = new UniquePropertiesService();

	/** The search service. */
	@Mock
	private SearchService service;

	/** The collection. */
	@Spy
	private ConcurrentMultiValueCollection collection;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		collection = new ConcurrentMultiValueCollection(ContextualMap.create(), mock(TransactionSupport.class));
		super.beforeMethod();
	}

	/**
	 * Creates a valid instance when no other instance with the same title exists.
	 */
	@Test
	public void createValidInstance() {
		mockServiceReturnNothing();
		Assert.assertFalse(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "somethingGenerated"));
	}

	/**
	 * Tries to create a instance with a title, that is already taken by another instance.
	 */
	@Test
	public void createInstanceTitleTaken() {
		Instance returnedInstance = createInstance("title", "anotherObject");
		mockServiceReturnResult(returnedInstance);
		Assert.assertTrue(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "somethingGenerated"));
	}

	/**
	 * Tries to update a instance with a title, that is already taken by another instance.
	 */
	@Test
	public void updateInstanceTitleTaken() {
		Instance returnedInstance = createInstance("title", "anotherObject");
		mockServiceReturnResult(returnedInstance);
		Assert.assertTrue(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "thisobject"));
	}

	/**
	 * Updates a instance with a title, that is already taken from the same instance, so there is no problem.
	 */
	@Test
	public void updateInstanceSameUri() {
		Instance returnedInstance = createInstance("title", "thisobject");
		mockServiceReturnResult(returnedInstance);
		Assert.assertFalse(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "thisobject"));
	}

	/**
	 * Update a instance when no other instance with the same title exists.
	 */
	@Test
	public void updateInstance() {
		mockServiceReturnNothing();
		Assert.assertFalse(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "thisobject"));
	}

	/**
	 * Tries to create two instances (different objects) with the same title. The first one should be persisted, while
	 * the second one should'nt because there is already a instance being persisted with the same title.
	 */
	@Test
	public void createTwoInstancesTitleTaken() {
		mockServiceReturnNothing();
		Assert.assertFalse(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "someuri"));
		Assert.assertTrue(uniquePropertiesService.objectExists(createTitlePropertyMap("title"), "anotheruri"));
	}

	/**
	 * Mocking the {@link SearchService} so it returns the instance {@link SearchService#searchAndLoad(Class, SearchArguments)}
	 * is called.
	 *
	 * @param instance
	 *            the instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	private void mockServiceReturnResult(final Instance instance) {
		final ArgumentCaptor<SearchArguments> searchArgsCaptor = ArgumentCaptor.forClass(SearchArguments.class);
		Mockito.doAnswer(invocation -> {
			List<Instance> results = new ArrayList<>();
			results.add(instance);
			searchArgsCaptor.getValue().setResult(results);
			return null;
		}).when(service).search(Matchers.any(Class.class), searchArgsCaptor.capture());
	}

	/**
	 * Mocking the {@link SearchService} so it returns nothing when {@link SearchService#searchAndLoad(Class, SearchArguments)}
	 * is called.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mockServiceReturnNothing() {
		final ArgumentCaptor<SearchArguments> searchArgsCaptor = ArgumentCaptor.forClass(SearchArguments.class);
		Mockito.doAnswer(invocation -> {
			List<String> results = new ArrayList<>();
			searchArgsCaptor.getValue().setResult(results);
			return null;
		}).when(service).search(Matchers.any(Class.class), searchArgsCaptor.capture());
	}

	/**
	 * Creates an {@link Instance}.
	 *
	 * @param title
	 *            the title
	 * @param uri
	 *            the uri
	 * @return the instance
	 */
	private Instance createInstance(String title, String uri) {
		Instance instance = Mockito.mock(Instance.class);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", title);
		Mockito.when(instance.getProperties()).thenReturn(properties);
		Mockito.when(instance.getId()).thenReturn(uri);
		return instance;
	}

	/**
	 * Creates a property map with the given title.
	 *
	 * @param title
	 *            the title
	 * @return the property map
	 */
	private Map<String, Serializable> createTitlePropertyMap(String title) {
		Map<String, Serializable> properties = CollectionUtils.createHashMap(1);
		properties.put("title", title);
		return properties;

	}
}
