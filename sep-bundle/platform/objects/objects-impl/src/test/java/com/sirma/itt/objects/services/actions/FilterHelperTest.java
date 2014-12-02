package com.sirma.itt.objects.services.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.concurrent.ConcurrentMultiValueCollection;
import com.sirma.itt.emf.concurrent.ConcurrentMultiValueCollectionEvent;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.executors.ExecutableOperationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.SearchServiceImpl;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.domain.model.SavedFilter;

/**
 * Testing the {@link FilterHelper}.
 * 
 * @author nvelkov
 */
public class FilterHelperTest extends EmfTest {

	/** The filter helper. */
	private FilterHelper filterHelper = new FilterHelper();

	/** The service. */
	private SearchService service;

	/** The collection. */
	private ConcurrentMultiValueCollection collection;

	/**
	 * Mock the {@link SearchService} and the events in the {@link ConcurrentMultiValueCollection}.
	 */
	@BeforeClass
	public void setupClass() {
		service = Mockito.mock(SearchServiceImpl.class);
		collection = new ConcurrentMultiValueCollection();
		ReflectionUtils.setField(filterHelper, "searchService", service);
		ReflectionUtils.setField(filterHelper, "collection", new ConcurrentMultiValueCollection());

		@SuppressWarnings("unchecked")
		Event<ConcurrentMultiValueCollectionEvent> events = Mockito.mock(Event.class);
		ReflectionUtils.setField(collection, "events", events);
		ReflectionUtils.setField(filterHelper, "collection", collection);
	}

	/**
	 * Clear the collection before each test.
	 */
	@BeforeMethod
	public void setupMethod() {
		collection.getMultiValueCollection().clear();
	}

	/**
	 * Creates a valid filter when no other filter with the same title exists.
	 */
	@Test
	public void createValidFilter() {
		mockServiceReturnNothing();
		SchedulerContext createdFilter = createFilterContext("title", "somethingGenerated");
		Assert.assertTrue(filterHelper.validateFilter(createdFilter));
	}

	/**
	 * Tries to create a filter with a title, that is already taken by another filter.
	 */
	@Test
	public void createFilterTitleTaken() {
		Instance returnedFilter = createFilterInstance("title", "anotherObject");
		mockServiceReturnResult(returnedFilter);

		SchedulerContext createdFilter = createFilterContext("title", "somethingGenerated");
		Assert.assertFalse(filterHelper.validateFilter(createdFilter));
	}

	/**
	 * Tries to update a filter with a title, that is already taken by another filter.
	 */
	@Test
	public void updateFilterTitleTaken() {
		Instance returnedFilter = createFilterInstance("title", "anotherObject");
		mockServiceReturnResult(returnedFilter);

		SchedulerContext createdFilter = createFilterContext("title", "thisobject");
		Assert.assertFalse(filterHelper.validateFilter(createdFilter));
	}

	/**
	 * Updates a filter with a title, that is already taken from the same filter, so there is no
	 * problem.
	 */
	@Test
	public void updateFilterSameUri() {
		Instance returnedFilter = createFilterInstance("title", "thisobject");
		mockServiceReturnResult(returnedFilter);

		SchedulerContext createdFilter = createFilterContext("title", "thisobject");
		Assert.assertTrue(filterHelper.validateFilter(createdFilter));
	}

	/**
	 * Update a filter when no other filter with the same title exists.
	 */
	@Test
	public void updateFilter() {
		mockServiceReturnNothing();

		SchedulerContext createdFilter = createFilterContext("title", "thisobject");
		Assert.assertTrue(filterHelper.validateFilter(createdFilter));
	}

	/**
	 * Tries to create two filters (different objects) with the same title. The first one should be
	 * persisted, while the second one should'nt because there is already a filter being persisted
	 * with the same title.
	 */
	@Test
	public void createTwoFiltersTitleTaken() {
		mockServiceReturnNothing();
		SchedulerContext firstFilter = createFilterContext("title", "someuri");
		SchedulerContext secondFilter = createFilterContext("title", "anotheruri");
		Assert.assertTrue(filterHelper.validateFilter(firstFilter));
		Assert.assertFalse(filterHelper.validateFilter(secondFilter));
	}

	/**
	 * Test convert request.
	 */
	@Test
	public void testConvertRequest() {
		JSONObject jsonObject = new JSONObject();
		filterHelper.convertRequest(jsonObject);
		Assert.assertEquals(
				JsonUtil.getStringValue(jsonObject, ExecutableOperationProperties.TYPE),
				SavedFilter.class.getSimpleName().toLowerCase());
		Assert.assertEquals(
				JsonUtil.getStringValue(jsonObject, ExecutableOperationProperties.DEFINITION),
				"savedFilter");
	}

	/**
	 * Mocking the {@link SearchService} so it returns SearchService nothing when
	 * {@link SearchService#search(Class, SearchArguments)} is performed.
	 * 
	 * @param instance
	 *            the instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mockServiceReturnResult(final Instance instance) {
		final ArgumentCaptor<SearchArguments> searchArgsCaptor = ArgumentCaptor
				.forClass(SearchArguments.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				List<Instance> results = new ArrayList<>();
				results.add(instance);
				searchArgsCaptor.getValue().setResult(results);
				return null;
			}
		}).when(service).search(Mockito.any(Class.class), searchArgsCaptor.capture());
	}

	/**
	 * Mocking the {@link SearchService} so it returns SearchService the given instance when
	 * {@link SearchService#search(Class, SearchArguments)} is performed.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mockServiceReturnNothing() {
		final ArgumentCaptor<SearchArguments> searchArgsCaptor = ArgumentCaptor
				.forClass(SearchArguments.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				List<String> results = new ArrayList<>();
				searchArgsCaptor.getValue().setResult(results);
				return null;
			}
		}).when(service).search(Mockito.any(Class.class), searchArgsCaptor.capture());
	}

	/**
	 * Creates a filter {@link Instance}.
	 * 
	 * @param title
	 *            the title
	 * @param uri
	 *            the uri
	 * @return the instance
	 */
	private Instance createFilterInstance(String title, String uri) {
		Instance instance = new ObjectInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", title);
		instance.setId(uri);
		instance.setProperties(properties);
		return instance;
	}

	/**
	 * Creates a filter {@link SchedulerContext}.
	 * 
	 * @param title
	 *            the title
	 * @param uri
	 *            the uri
	 * @return the scheduler context
	 */
	private SchedulerContext createFilterContext(String title, String uri) {
		InstanceReference instanceRef = new LinkSourceId();
		instanceRef.setIdentifier(uri);
		SchedulerContext context = new SchedulerContext();
		HashMap<String, String> filterProperties = new HashMap<>();
		filterProperties.put("title", title);
		context.put("properties", filterProperties);
		context.put("target", instanceRef);
		return context;
	}
}
