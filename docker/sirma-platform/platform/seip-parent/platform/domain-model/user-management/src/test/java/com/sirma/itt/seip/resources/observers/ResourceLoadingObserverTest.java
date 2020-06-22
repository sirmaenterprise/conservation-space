package com.sirma.itt.seip.resources.observers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.domain.event.LoadItemsEvent;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceSorter;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * Test for resource filtering observer.
 *
 * @author svelikov
 */
public class ResourceLoadingObserverTest {

	private static final String AT_GROUP_MANAGER = "at_group_manager";
	private static final String UNEXISTING_GROUP = "unexisting_group";

	private final ResourceLoadingObserver observer;

	private final ResourceService resourceService;

	private ResourceSorter resourceSorter;

	/**
	 * Instantiates a new resource loading observer test.
	 */
	public ResourceLoadingObserverTest() {
		observer = new ResourceLoadingObserver() {
			@Override
			protected List<Resource> loadAllResources(String type, String sortingField) {
				Resource user1 = new EmfUser("user1");
				Resource user2 = new EmfUser("user2");
				Resource user3 = new EmfUser("user3");
				List<Resource> resources = new LinkedList<Resource>();
				resources.add(user1);
				resources.add(user2);
				resources.add(user3);
				return resources;
			}
			//
			// @Override
			// protected List<Resource> loadRequestedResources(String[] keywords) {
			// String resourceName = keywords[0];
			// if (resourceName.equals(UNEXISTING_GROUP)) {
			// return Collections.emptyList();
			// } else if (resourceName.equals(AT_GROUP_MANAGER)) {
			// Resource user1 = new EmfUser("user1");
			// List<Resource> resources = new LinkedList<Resource>();
			// resources.add(user1);
			// return resources;
			// }
			// return super.loadRequestedResources(keywords);
			// }
		};

		resourceService = Mockito.mock(ResourceService.class);
		resourceSorter = Mockito.mock(ResourceSorter.class);
		ReflectionUtils.setFieldValue(observer, "resourceService", resourceService);
		ReflectionUtils.setFieldValue(observer, "resourceSorter", resourceSorter);
	}

	/**
	 * Load filtered resources.
	 */
	@Test
	public void loadFilteredResources() {
		LoadItemsEvent event = new LoadItemsEvent();
		observer.loadFilteredResources(event);
		Assert.assertTrue(event.getItems().isEmpty(), "No items should be loaded if keywords are not provided!");
		Assert.assertFalse(event.isHandled(), "Event should not be handled if keywords are not provided!");

		//
		Map<String, Object> keywords = new HashMap<String, Object>();
		event.setKeywords(keywords);
		observer.loadFilteredResources(event);
		Assert.assertTrue(event.getItems().isEmpty(), "No items should be loaded if keywords are not provided!");
		Assert.assertFalse(event.isHandled(), "Event should not be handled if keywords are not provided!");

		//
		keywords.put("filterItems", UNEXISTING_GROUP);
		observer.loadFilteredResources(event);
		Assert.assertTrue(event.isHandled(), "Event should be handled!");
		Assert.assertTrue(event.getItems().size() == 3,
				"All resources should be returned in case requested resources does not exists!");

		Mockito.when(resourceService.getContainedResources(Arrays.asList(AT_GROUP_MANAGER), ResourceType.USER))
				.thenReturn(Arrays.asList(createUser(AT_GROUP_MANAGER)));
		keywords.clear();
		keywords.put("filterItems", AT_GROUP_MANAGER);
		observer.loadFilteredResources(event);
		Assert.assertTrue(event.isHandled(), "Event should be handled!");
		Assert.assertTrue(event.getItems().size() == 1,
				"Just requested resources and contained inside them should be returned!");
	}

	private static Resource createUser(String name) {
		return new EmfUser(name);
	}
}
