package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.event.LoadItemsEvent;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;

/**
 * Test for ResourcesRestService.
 *
 * @author A. Kunchev
 */
@Test
public class ResourcesRestServiceTest {

	private static final String FILTER_NAME = "filterName";
	private static final String TYPE = "type";
	private static final String FALSE = "false";
	private static final String TRUE = "true";

	@InjectMocks
	@Spy
	private ResourcesRestService service = new ResourcesRestService();

	@Mock
	private ResourceService resourceService;

	@Mock
	private EventService eventService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private Event<LoadItemsEvent> loadItemsEvent;

	@Mock
	private ResourceSorter resourceSorter;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ---------------------- load -----------------------------------------------

	public void load_unknownType_badRequest() {
		Response response = service.load(null, null, null, null, false);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		verify(typeConverter, never()).convert(any(), anyCollection());
	}

	public void load_allTypeNullFilternameEmptyKeywordsFalseActive_okRequest() {
		prepareResourceService();
		Response response = service.load(ResourceType.ALL.getName(), null, "", null, false);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		verify(typeConverter, times(1)).convert(any(), anyCollection());
	}

	public void load_userTypeEmptyFilternameEmptyKeywordsTrueActive_okRequest() {
		prepareResourceService();
		Response response = service.load(ResourceType.USER.getName(), "", "", null, true);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		verify(typeConverter, times(1)).convert(any(), anyCollection());
	}

	public void load_eventHandledFalse_okRequest() {
		loadInternal(false);
	}

	public void load_eventHandledTrue_okRequest() {
		loadInternal(true);
	}

	/**
	 * Helps test the load method.
	 *
	 * @param eventHandled
	 *            sets the event handled status
	 */
	private void loadInternal(boolean eventHandled) {
		LoadItemsEvent event = new LoadItemsEvent();
		event.setItems(buildUsersList());
		event.setHandled(eventHandled);
		prepareResourceService();
		when(service.fireEvent(anyString(), anyMap(), anyString(), anyString())).thenReturn(event);
		Response response = service.load(ResourceType.ALL.getName(), FILTER_NAME, "{filter: filterUsers}", "", true);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		verify(typeConverter, times(1)).convert(any(), anyCollection());
	}

	// ---------------------- fireEvent ------------------------------------------

	public void fireEvent_emptyFiltername_emptyEvent() {
		LoadItemsEvent event = service.fireEvent("", null, TYPE, null);
		assertEquals(null, event.getType());
	}

	public void fireEvent_nullFiltername_emptyEvent() {
		LoadItemsEvent event = service.fireEvent(null, null, TYPE, null);
		assertEquals(null, event.getType());
	}

	public void fireEvent_notNullFilternameNullKeywords_emptyEvent() {
		prepareLoadItemsEvent();
		LoadItemsEvent event = service.fireEvent(FILTER_NAME, null, TYPE, null);
		assertEquals(TYPE, event.getType());
		assertNull(event.getKeywords());
	}

	public void fireEvent_notNullFilternameNotNullKeywords_emptyEvent() {
		prepareLoadItemsEvent();
		Map<String, Object> keywords = new HashMap<>();
		keywords.put("key", "value");
		LoadItemsEvent event = service.fireEvent(FILTER_NAME, keywords, TYPE, null);
		assertEquals(TYPE, event.getType());
		assertEquals(keywords, event.getKeywords());
	}

	// ---------------------- refresh --------------------------------------------

	public void refresh_eventServiceCalled() {
		service.refresh(false);
		verify(eventService, times(1)).fire(any(EmfEvent.class));
	}

	// ---------------------- getResources ---------------------------------------

	public void getResources_nullType() {
		String result = service.getResources(null, false);
		assertEquals("[]", result);
	}

	public void getResources_emptyType() {
		String result = service.getResources("", false);
		assertEquals("[]", result);
	}

	public void getResources_userType() throws JSONException {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsersList());
		String result = service.getResources(ResourceType.USER.getName(), false);
		// XXX for some reason the typeConverter returns empty collection
		assertEquals(0, new JSONArray(result).length());
		verify(typeConverter, times(1)).convert(any(), anyCollection());
	}

	// ---------------------- common methods -------------------------------------

	private void prepareResourceService() {
		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(buildUsersList());
		when(resourceService.getAllResources(ResourceType.GROUP, null)).thenReturn(buildGroupsList());
		when(resourceService.getAllActiveResources(ResourceType.GROUP, null)).thenReturn(buildGroupsList());
		when(resourceService.getAllActiveResources(ResourceType.USER, null))
				.thenReturn(Arrays.asList(new EmfUser("activeUser1")));
	}

	private void prepareLoadItemsEvent() {
		when(loadItemsEvent.select(any())).thenReturn(loadItemsEvent);
		doNothing().when(loadItemsEvent).fire(any());
	}

	/**
	 * Builds the users list.
	 *
	 * @return the list
	 */
	private static List<Resource> buildUsersList() {
		List<Resource> resources = new LinkedList<>();
		resources.add(new EmfUser("user1"));
		resources.add(new EmfUser("user2"));
		resources.add(new EmfUser("user3"));
		return resources;
	}

	/**
	 * Builds the groups list.
	 *
	 * @return the list
	 */
	private static List<Resource> buildGroupsList() {
		List<Resource> resources = new LinkedList<>();
		resources.add(new EmfGroup("group1", "GROUP_group1"));
		resources.add(new EmfGroup("group2", "GROUP_group2"));
		resources.add(new EmfGroup("group3", "GROUP_group3"));
		return resources;
	}

}
