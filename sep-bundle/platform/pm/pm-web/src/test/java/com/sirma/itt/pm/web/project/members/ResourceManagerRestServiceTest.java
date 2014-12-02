package com.sirma.itt.pm.web.project.members;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.sirma.cmf.web.entity.bookmark.BookmarkUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.label.LabelProviderImpl;
import com.sirma.itt.emf.resources.ResourceServiceImpl;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleService;
import com.sirma.itt.emf.security.RoleServiceImpl;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.impl.ProjectServiceImpl;

/**
 * The Class ResourceManagerRestServiceTest.
 * 
 * @author svelikov
 */
@Test
public class ResourceManagerRestServiceTest extends PMTest {

	private static final String RESPONSEJSON = "responsejson";

	/** The controller under test. */
	private ResourceManagerRestService controller;

	/** The resource service. */
	private ResourceServiceImpl resourceService;

	/** The project service. */
	private ProjectServiceImpl projectService;

	/** The role service. */
	private RoleService roleService;

	/** The label provider. */
	private LabelProviderImpl labelProvider;

	/** The bookmark util. */
	private BookmarkUtil bookmarkUtil;

	/** The event service. */
	private EventService eventService;

	/**
	 * Instantiates a new resource manager controller test.
	 */
	public ResourceManagerRestServiceTest() {
		controller = new ResourceManagerRestService() {
			@Override
			protected <R extends Resource> String buildResourceJson(List<R> resources)
					throws JSONException {
				return RESPONSEJSON;
			}

			@Override
			protected JSONArray getProjectResources(ProjectInstance projectInstance)
					throws JSONException {
				return new JSONArray();
			}

			@Override
			protected Collection<JSONObject> getActiveRoles() {
				return Collections.emptyList();
			}
		};

		ReflectionUtils.setField(controller, "log", SLF4J_LOG);

		// mock resource service
		resourceService = mock(ResourceServiceImpl.class);
		ReflectionUtils.setField(controller, "resourceService", resourceService);

		// mock project service
		projectService = mock(ProjectServiceImpl.class);
		ReflectionUtils.setField(controller, "projectService", projectService);

		// mock role service
		roleService = mock(RoleServiceImpl.class);
		ReflectionUtils.setField(controller, "roleService", roleService);

		// mock bookmark util
		bookmarkUtil = mock(BookmarkUtil.class);
		when(bookmarkUtil.buildLink(any(ProjectInstance.class))).thenReturn("bookmarklink");
		ReflectionUtils.setField(controller, "bookmarkUtil", bookmarkUtil);

		// mock label provider service
		labelProvider = mock(LabelProviderImpl.class);
		when(labelProvider.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_TYPE_UNEXPECTED))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_TYPE_UNEXPECTED);
		when(labelProvider.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_IN_SERVICE))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_IN_SERVICE);
		when(
				labelProvider
						.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_UNEXPECTED_PROJECTID))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_UNEXPECTED_PROJECTID);
		when(
				labelProvider
						.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_INVALID_REQUEST_DATA))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_INVALID_REQUEST_DATA);
		when(labelProvider.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_PROJECT_NOT_FOUND))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_PROJECT_NOT_FOUND);
		when(
				labelProvider
						.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_REQUIRED_PM_RESOURCE))
				.thenReturn(ResourceManagerRestServiceLabels.PM_RM_ERROR_REQUIRED_PM_RESOURCE);
		ReflectionUtils.setField(controller, "labelProvider", labelProvider);

		// mock event service
		eventService = mock(EventService.class);
		ReflectionUtils.setField(controller, "eventService", eventService);
	}

	/**
	 * Load items test. Only response status and messages are tested. Type conversions are tested
	 * separately.
	 */
	public void loadItemsTest() {
		try {
			Response response;
			String entity;

			// check if null is passed as type
			response = controller.loadItems(null, "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_TYPE_UNEXPECTED);
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

			// check if empty string is passed as type
			response = controller.loadItems("", "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_TYPE_UNEXPECTED);
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

			// check if user type is requested but resource service returns null
			when(resourceService.getAllResources(ResourceType.USER, "sortingfield")).thenReturn(
					null);
			response = controller.loadItems(ResourceType.USER.getName(), "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_IN_SERVICE);
			assertEquals(response.getStatus(),
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

			// check if user type is requested
			when(resourceService.getAllResources(ResourceType.USER, "sortingfield")).thenReturn(
					getUserResourceList());
			response = controller.loadItems(ResourceType.USER.getName(), "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, RESPONSEJSON);
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

			// check if group type is requested but service returns null
			when(resourceService.getAllResources(ResourceType.GROUP, "sortingfield")).thenReturn(
					null);
			response = controller.loadItems(ResourceType.GROUP.getName(), "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_IN_SERVICE);
			assertEquals(response.getStatus(),
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

			// check if group type is requested
			when(resourceService.getAllResources(ResourceType.GROUP, "sortingfield")).thenReturn(
					getGroupResourceList());
			response = controller.loadItems(ResourceType.GROUP.getName(), "sortingfield");
			entity = (String) response.getEntity();
			assertEquals(entity, RESPONSEJSON);
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load project items test. Test only response status. Conversions are tested separately.
	 */
	public void loadProjectItemsTest() {
		try {

			Response response = null;
			String entity = null;

			// projectId = null
			response = controller.loadProjectItems(null);
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_UNEXPECTED_PROJECTID);
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

			// projectId = empty string
			response = controller.loadProjectItems("");
			entity = (String) response.getEntity();
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_UNEXPECTED_PROJECTID);
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

			// real project id
			when(projectService.loadByDbId("1")).thenReturn(
					createProjectInstance(Long.valueOf(1), "dmsid"));
			response = controller.loadProjectItems("1");
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save items test.
	 */
	public void saveItemsTest() {
		try {

			Response response = null;
			String entity = null;

			// test with null data
			response = controller.saveItems(null);
			entity = (String) response.getEntity();
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_INVALID_REQUEST_DATA);

			// test with empty data
			response = controller.saveItems("");
			entity = (String) response.getEntity();
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_INVALID_REQUEST_DATA);

			// pass unexisting project instance id
			String items = "{projectId:'2',assignedResources:{1:'projectmanager',2:'consumer'}}";
			response = controller.saveItems(items);
			entity = (String) response.getEntity();
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_PROJECT_NOT_FOUND);

			// assigned resources is empty
			items = "{projectId:'1',assignedResources:{}}";
			response = controller.saveItems(items);
			entity = (String) response.getEntity();
			assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
			assertEquals(entity, ResourceManagerRestServiceLabels.PM_RM_ERROR_REQUIRED_PM_RESOURCE);

			//
			items = "{projectId:'1',assignedResources:{1:'projectmanager',2:'consumer'}}";
			doNothing().when(resourceService).setResources(anyMap(), any(ProjectInstance.class));
			response = controller.saveItems(items);
			entity = (String) response.getEntity();
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			assertEquals(entity, "{\"redirectUrl\":\"bookmarklink\"}");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the role test.
	 */
	public void getRoleTest() {
		try {

			JSONObject role = controller.getRole("rolelabel", "rolevalue");
			assertEquals(role.get("label"), "rolelabel");
			assertEquals(role.get("value"), "rolevalue");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the user resource list.
	 * 
	 * @return the user resource list
	 */
	private List<Resource> getUserResourceList() {
		List<Resource> resources = new ArrayList<Resource>();
		EmfUser emfUser1 = new EmfUser("John Smith");
		resources.add(emfUser1);
		EmfUser emfUser2 = new EmfUser("Mannfred Lilov");
		resources.add(emfUser2);
		return resources;
	}

	/**
	 * Gets the group resource list.
	 * 
	 * @return the group resource list
	 */
	private List<Resource> getGroupResourceList() {
		List<Resource> resources = new ArrayList<Resource>();
		EmfGroup emfGroup1 = new EmfGroup("1", "Administrators");
		resources.add(emfGroup1);
		EmfGroup emfGroup2 = new EmfGroup("2", "Project Managers");
		resources.add(emfGroup2);
		return resources;
	}
}
