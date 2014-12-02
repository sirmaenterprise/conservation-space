package com.sirma.cmf.web.object.browser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.cmf.web.configuration.CmfWebConfigurationProperties;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.db.SolrDb;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.instance.InstanceContextInitializer;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkSearchArguments;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.script.ScriptEvaluator;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.LinkIterable;

/**
 * A controller for the Objects browser component.
 * 
 * @author svelikov
 */
@Path("/objects/browse")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TreeRestService extends EmfRestService {

	/** The "/" char. */
	private static final int SLASH = 47;

	/** Number of results to be loaded when query for loading is executed. */
	// TODO use configuration
	private static final int ROOT_SELECTOR_LIMIT_COUNT = 25;
	private static final String PURPOSE = "purpose";
	private static final String MIMETYPE = "mimetype";
	private static final String NAME = "name";
	private static final String OBJECTINSTANCE = "objectinstance";
	protected static final String CASEINSTANCE = "caseinstance";
	protected static final String PROJECTINSTANCE = "projectinstance";
	protected static final String TASKS = "tasks";
	protected static final String OBJECTS_SECTION = "objectsSection";
	protected static final String DOCUMENTS_SECTION = "documentsSection";

	private final Set<String> leafInstances = new HashSet<String>(Arrays.asList("taskinstance"));

	/** The tree page size. */
	@Inject
	@Config(name = CmfWebConfigurationProperties.TREE_PAGER_PAGESIZE, defaultValue = "500")
	private Integer treePageSize;

	@Inject
	private ScriptEvaluator scriptEvaluator;

	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	@Inject
	@SolrDb
	private javax.enterprise.inject.Instance<LinkService> solrLinkService;

	@Inject
	private LinkService linkService;

	@Inject
	private SearchService searchService;

	/**
	 * Provides a json array with root instances to be displayed inside the object browser. Response
	 * is in format:
	 * 
	 * <pre>
	 * 	[
	 * 		{instanceId:'id1',header:'project 1 header', name:'project 1'},
	 *  	{instanceId:'id2',header:'project 2 header', name:'project 2'}
	 * ];
	 * </pre>
	 * 
	 * @param rootType
	 *            the root type ('projectinstance' for example)
	 * @param limit
	 *            the limit (passed by the extjs tree store)
	 * @param term
	 *            the term
	 * @return the roots
	 */
	@Path("roots")
	@GET
	public Response getRoots(@QueryParam("rootType") String rootType,
			@QueryParam("limit") Integer limit, @QueryParam("term") String term) {
		if (debug) {
			log.info("ObjectsBrowserRestService.getRoots rootType[{}] limit[{}] term[{}]",
					rootType, limit, term);
		}

		String actualRootType = PROJECTINSTANCE;
		if (StringUtils.isNotNullOrEmpty(rootType)) {
			actualRootType = rootType;
		}

		JSONArray data = new JSONArray();
		Integer localLimit = limit;
		if (localLimit == null) {
			localLimit = Integer.valueOf(ROOT_SELECTOR_LIMIT_COUNT);
		}

		@SuppressWarnings("unchecked")
		List<Instance> roots = (List<Instance>) loadRootInstances(actualRootType, localLimit, term);

		for (Instance instance : roots) {
			JSONObject item = new JSONObject();
			JsonUtil.addToJson(item, INSTANCE_ID, instance.getId());
			JsonUtil.addToJson(item, INSTANCE_TYPE, instance.getClass().getSimpleName()
					.toLowerCase());
			String instanceHeader = (String) instance.getProperties().get(
					DefaultProperties.HEADER_COMPACT);
			instanceHeader = convertLinkToSpan(instanceHeader);
			JsonUtil.addToJson(item, HEADER, instanceHeader);
			JsonUtil.addToJson(item, NAME, instance.getProperties().get(DefaultProperties.TITLE));
			data.put(item);
		}

		return Response.status(Response.Status.OK).entity(data.toString()).build();
	}

	/**
	 * Load root instances.
	 * 
	 * @param actualRootType
	 *            the actual root type
	 * @param limit
	 *            the limit
	 * @param term
	 *            the term
	 * @return the list
	 */
	private List<? extends Instance> loadRootInstances(String actualRootType, Integer limit,
			String term) {
		Resource currentUser = getCurrentUser();
		if (currentUser == null) {
			return Collections.emptyList();
		}

		String filterName = "customQueries/" + actualRootType + "_rootFilter";
		Context<String, Object> context = new Context<String, Object>(1);
		Uri userid = typeConverter.convert(Uri.class, currentUser.getId());
		context.put("userid", userid.toString());
		if (StringUtils.isNotNullOrEmpty(term)) {
			context.put("term", " AND all_text:\"*" + term + "*\"");
		}
		SearchArguments<SearchInstance> arguments = searchService.getFilter(filterName,
				SearchInstance.class, context);
		if (arguments == null) {
			log.warn("No filter found: " + filterName);
			return Collections.emptyList();
		}

		if (limit != null) {
			arguments.setMaxSize(limit.intValue());
		}
		searchService.search(Instance.class, arguments);
		return arguments.getResult();
	}

	/**
	 * Load the data for the objects browser.
	 * 
	 * @param node
	 *            the node is the path for the current instance
	 * @param type
	 *            the type of the node being requested
	 * @param currentInstanceId
	 *            the current instance id
	 * @param currentInstanceType
	 *            the current instance type
	 * @param allowSelection
	 *            If selection of nodes should be allowed.
	 * @param filters
	 *            the filters
	 * @param clickableLinks
	 *            If the links inside the node text should be clickable.
	 * @param clickOpenWindow
	 *            If the links inside the node text should open in new browser tab
	 * @param manualExpand
	 *            If the tree is manually expanded by user, we may skip node path building process.
	 * @param page
	 *            the page that need to be returned
	 * @return Response which contains the built data store json
	 */
	@Path("tree")
	@GET
	public Response tree(@QueryParam("node") String node, @QueryParam("type") String type,
			@QueryParam("currId") String currentInstanceId,
			@QueryParam("currType") String currentInstanceType,
			@QueryParam("allowSelection") boolean allowSelection,
			@QueryParam("filters") String filters,
			@QueryParam("clickableLinks") boolean clickableLinks,
			@QueryParam("clickOpenWindow") boolean clickOpenWindow,
			@QueryParam("manualExpand") boolean manualExpand,
			@DefaultValue("0") @QueryParam("page") Integer page) {
		if (debug) {
			log.debug("EMFWeb: ObjectsBrowserRestService.load node=" + node + ", type=" + type
					+ ", currentInstanceId=" + currentInstanceId + ", currentInstanceType="
					+ currentInstanceType + ", allowSelection=" + allowSelection + ", filters="
					+ filters + ", clickableLinks=" + clickableLinks + ", clickOpenWindow="
					+ clickOpenWindow + ", manualExpand=" + manualExpand);
		}

		// check required arguments
		if (StringUtils.isNullOrEmpty(node) || StringUtils.isNullOrEmpty(type)) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Missing required arguments!").build();
		}

		String rootInstanceId = extractInstanceId(node);
		// load instance that should be expanded later
		Instance rootInstance = fetchInstance(rootInstanceId, type);

		if (rootInstance == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Can't find instance type=" + type + " with id=" + rootInstanceId)
					.build();
		}

		TimeTracker tracker = TimeTracker.createAndStart();

		LinkSearchArguments arguments = buildSearchArguments(rootInstance, page);

		// load the children of the current node being expanded
		Iterable<Instance> loadedInstances = fetchInstanceChildren(arguments);

		// group instance by type and purpose for section instances
		Map<String, List<Instance>> grouped = groupByType(loadedInstances);

		String currentNodePath = node;
		// If the tree is manually expanded by user, we may skip node path building process.
		if (!manualExpand) {
			currentNodePath = buildPathToCurrentNode(currentInstanceId, currentInstanceType,
					rootInstance);
		}

		// buid JSON response
		JSONArray nodes = buildData(grouped, rootInstance, node, shouldBeGrouped(rootInstance),
				currentNodePath, allowSelection, clickableLinks, clickOpenWindow, filters);

		// TODO: add nextPage element to the result if there are more elements for pagination
		String data = nodes.toString();

		log.debug("Tree node {} expand took {} s", rootInstanceId, tracker.stopInSeconds());

		return Response.status(Response.Status.OK).entity(data).build();
	}

	/**
	 * Builds the search arguments.
	 * 
	 * @param rootInstance
	 *            the root instance
	 * @param page
	 *            the page
	 * @return the link search arguments
	 */
	private LinkSearchArguments buildSearchArguments(Instance rootInstance, Integer page) {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(rootInstance.toReference());
		arguments.setLinkId(LinkConstants.TREE_PARENT_TO_CHILD);
		arguments.setPageNumber(page);
		arguments.setMaxSize(treePageSize.intValue());
		return arguments;
	}

	/**
	 * Builds the path to current node. The path contains instance id's separated with '/'. If given
	 * node is not instance but a group, then its name is used instead of id because they don't have
	 * one.
	 * 
	 * @param currentInstanceId
	 *            the current instance id
	 * @param currentInstanceType
	 *            the current instance type
	 * @param rootInstance
	 *            the root instance
	 * @return the string
	 */
	private String buildPathToCurrentNode(String currentInstanceId, String currentInstanceType,
			Instance rootInstance) {
		Instance currentInstanceNode = fetchInstance(currentInstanceId, currentInstanceType);
		if (currentInstanceNode == null) {
			return "";
		}

		instanceContextInitializer.restoreHierarchy(currentInstanceNode, rootInstance);
		List<Instance> parentInstances = InstanceUtil.getParentPath(currentInstanceNode, true);

		StringBuilder path = new StringBuilder();
		for (Instance parent : parentInstances) {
			String instanceType = parent.getClass().getSimpleName().toLowerCase();
			if (parent instanceof RootInstanceContext) {
				path.append(parent.getId()).append("/");
			} else if (parent instanceof CaseInstance) {
				path.append(instanceType).append("/").append(parent.getId()).append("/");
			} else if (parent instanceof SectionInstance) {
				String purpose = ((SectionInstance) parent).getPurpose();
				purpose = StringUtils.isNullOrEmpty(purpose) ? DOCUMENTS_SECTION : purpose;
				path.append(purpose).append("/").append(parent.getId()).append("/");
			} else if (parent instanceof DocumentInstance) {
				path.append(parent.getId()).append("/");
			} else {
				if (OBJECTINSTANCE.equals(instanceType)) {
					path.append(parent.getId()).append("/");
				} else if (parent instanceof WorkflowInstanceContext) {
					path.append(instanceType).append("/").append(parent.getId()).append("/");
				} else if (parent instanceof AbstractTaskInstance) {
					path.append(TASKS).append("/").append(parent.getId()).append("/");
				}
			}
		}
		if (path.length() > 0) {
			path = path.delete(path.length() - 1, path.length());
		}
		return path.toString();
	}

	/**
	 * Gets the root instance.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @return the root instance
	 */
	protected Instance getRootInstance(Instance currentInstance) {
		if (currentInstance instanceof RootInstanceContext) {
			return currentInstance;
		}
		return InstanceUtil.getRootInstance(currentInstance, true);
	}

	/**
	 * Extract instance id.
	 * 
	 * @param node
	 *            the node
	 * @return the string
	 */
	protected String extractInstanceId(String node) {
		if (StringUtils.isNullOrEmpty(node)) {
			return "";
		}
		return node.substring(node.lastIndexOf(SLASH) + 1);
	}

	/**
	 * If children should be grouped under common group node or not.
	 * 
	 * @param currentInstanceNode
	 *            the current instance node
	 * @return true, if is section
	 */
	protected boolean shouldBeGrouped(Instance currentInstanceNode) {
		return (currentInstanceNode instanceof RootInstanceContext)
				|| (currentInstanceNode instanceof CaseInstance);
	}

	/**
	 * Group instances by type.
	 * 
	 * @param loadedInstances
	 *            the loaded instances
	 * @return the map
	 */
	protected Map<String, List<Instance>> groupByType(Iterable<Instance> loadedInstances) {
		Map<String, List<Instance>> grouped = new HashMap<String, List<Instance>>();
		if (loadedInstances != null) {
			for (Instance instance : loadedInstances) {
				if (instance instanceof SectionInstance) {
					mapSectionInstance((SectionInstance) instance, grouped);
				} else if (instance instanceof AbstractTaskInstance) {
					mapTaskInstance((AbstractTaskInstance) instance, grouped);
				} else {
					mapInstance(instance, grouped);
				}
			}
		}
		return grouped;
	}

	/**
	 * Map an instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param grouped
	 *            the grouped
	 */
	protected void mapInstance(Instance instance, Map<String, List<Instance>> grouped) {
		String key = instance.getClass().getSimpleName().toLowerCase();
		createMapping(key, grouped);
		grouped.get(key).add(instance);
	}

	/**
	 * Map section instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param grouped
	 *            the grouped
	 */
	protected void mapSectionInstance(SectionInstance instance, Map<String, List<Instance>> grouped) {
		String purpose = instance.getPurpose();
		if (StringUtils.isNullOrEmpty(purpose) || DOCUMENTS_SECTION.equals(purpose)) {
			createMapping(DOCUMENTS_SECTION, grouped);
			grouped.get(DOCUMENTS_SECTION).add(instance);
		} else if (OBJECTS_SECTION.equals(purpose)) {
			createMapping(OBJECTS_SECTION, grouped);
			grouped.get(OBJECTS_SECTION).add(instance);
		} else if (SectionProperties.PURPOSE_FOLDER.equals(purpose)) {
			createMapping(SectionProperties.PURPOSE_FOLDER, grouped);
			grouped.get(SectionProperties.PURPOSE_FOLDER).add(instance);
		}
	}

	/**
	 * Map task instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param grouped
	 *            the grouped
	 */
	protected void mapTaskInstance(AbstractTaskInstance instance,
			Map<String, List<Instance>> grouped) {
		createMapping(TASKS, grouped);
		grouped.get(TASKS).add(instance);
	}

	/**
	 * Creates mapping for given key if it doesn't exists.
	 * 
	 * @param key
	 *            the key
	 * @param grouped
	 *            the grouped
	 */
	protected void createMapping(String key, Map<String, List<Instance>> grouped) {
		if (StringUtils.isNullOrEmpty(key)) {
			return;
		}
		if (grouped.get(key) == null) {
			grouped.put(key, new ArrayList<Instance>());
		}
	}

	/**
	 * Fetch instance children.
	 * 
	 * @param arguments
	 *            the arguments
	 * @return the list
	 */
	protected Iterable<Instance> fetchInstanceChildren(LinkSearchArguments arguments) {
		getService().searchLinks(arguments);

		List<Instance> instances = Collections.emptyList();
		if (arguments.getResult() != null) {
			// creates direct iterator for TO values of the links
			return new LinkIterable<>(arguments.getResult());
		}
		return instances;
	}

	/**
	 * Builds the data store to be returned to the client.
	 * 
	 * @param grouped
	 *            the grouped instances
	 * @param root
	 *            the root instance for which is the request
	 * @param node
	 *            the node id represented as path
	 * @param renderGrouped
	 *            if children of the node should be rendered under common root or immediately under
	 *            the node
	 * @param currentNodePath
	 *            the current node path
	 * @param allowSelection
	 *            If selection of nodes should be allowed.
	 * @param clickableLinks
	 *            If the links inside the node text should be clickable.
	 * @param clickOpenWindow
	 *            If the links inside the node text should open in new browser tab.
	 * @param filtersString
	 *            the filters string
	 * @return the jSON object representing the data store
	 */
	protected JSONArray buildData(Map<String, List<Instance>> grouped, Instance root, String node,
			boolean renderGrouped, String currentNodePath, boolean allowSelection,
			boolean clickableLinks, boolean clickOpenWindow, String filtersString) {
		JSONArray children = new JSONArray();

		if (grouped != null) {
			for (String groupId : grouped.keySet()) {
				// except for sections, all instances are grouped under group nodes
				// TODO: for objects and documents we also may have nesting but not grouped
				// TODO: for workflows we also may have nesting but not grouped
				if (renderGrouped) {
					String accumulatedWithGroupId = node + "/" + groupId;
					JSONObject group = new JSONObject();
					JsonUtil.addToJson(group, "id", accumulatedWithGroupId);
					JsonUtil.addToJson(group, "text", getGroupLabel(groupId));
					JsonUtil.addToJson(group, "cls", groupId + "-group");
					JsonUtil.addToJson(group, "leaf", Boolean.FALSE);
					JsonUtil.addToJson(group, "cnPath", currentNodePath);
					JSONArray groupChildren = new JSONArray();
					JsonUtil.addToJson(group, "children", groupChildren);
					children.put(group);
					addChildren(grouped, groupId, accumulatedWithGroupId, groupChildren,
							currentNodePath, allowSelection, clickableLinks, clickOpenWindow,
							filtersString);
				} else {
					String accumulatedId = node;
					addChildren(grouped, groupId, accumulatedId, children, currentNodePath,
							allowSelection, clickableLinks, clickOpenWindow, filtersString);
				}
			}
		}

		return children;
	}

	/**
	 * Adds children data in the data store json for any group.
	 * 
	 * @param grouped
	 *            the grouped instances
	 * @param groupId
	 *            the group id is the key under which the children for the group are mapped
	 * @param accumulatedId
	 *            the accumulated id is the current instance path with appended group id
	 * @param groupChildren
	 *            the group children is the json array inside which the children data objects should
	 *            be added
	 * @param currentNodePath
	 *            the current node path
	 * @param allowSelection
	 *            If selection of node should be allowed.
	 * @param clickableLinks
	 *            If the links inside the node text should be clickable.
	 * @param clickOpenWindow
	 *            If the links inside the node text should open in new browser tab.
	 * @param filtersString
	 *            the filters string
	 */
	private void addChildren(Map<String, List<Instance>> grouped, String groupId,
			String accumulatedId, JSONArray groupChildren, String currentNodePath,
			boolean allowSelection, boolean clickableLinks, boolean clickOpenWindow,
			String filtersString) {
		List<Instance> instanceGroup = grouped.get(groupId);
		for (Instance instance : instanceGroup) {
			boolean isLeaf = isLeaf(instance);
			boolean hasFilter = StringUtils.isNotNullOrEmpty(filtersString);
			JSONObject child = new JSONObject();
			String accumulatedWithGroupId = accumulatedId + "/" + instance.getId();
			JsonUtil.addToJson(child, "id", accumulatedWithGroupId);
			JsonUtil.addToJson(child, "dbId", instance.getId());
			String textValue = getNodeText(
					instance.getProperties().get(DefaultProperties.HEADER_COMPACT), clickableLinks,
					clickOpenWindow);
			JsonUtil.addToJson(child, "text", textValue);
			JsonUtil.addToJson(child, "cls", instance.getClass().getSimpleName().toLowerCase());
			// leaf instances are those that doesn't allow children
			JsonUtil.addToJson(child, "leaf", Boolean.valueOf(isLeaf));
			// current node path is used to find the node in js plugin when expanding the tree
			JsonUtil.addToJson(child, "cnPath", currentNodePath);

			// evaluate filters only if there are filters provided
			Boolean isSelectable = Boolean.FALSE;
			if (StringUtils.isNotNullOrEmpty(filtersString)) {
				isSelectable = isSelectable(instance, filtersString);
			}
			// if selection is allowed and no filters are provided, we allow selection for all
			// instances
			if (allowSelection && !hasFilter) {
				JsonUtil.addToJson(child, "checked", Boolean.FALSE);
			}
			// if selection is allowed and a filter is provided, then we allow selection only for
			// selectable instances
			else if (allowSelection && hasFilter && isSelectable) {
				JsonUtil.addToJson(child, "checked", Boolean.FALSE);
			}
			groupChildren.put(child);
		}
	}

	/**
	 * Checks if given instance node can be selected in the ui. Filters string is evaluated as
	 * javascript boolean expression. And if evaluation is successful and is 'true' then instance
	 * can be selectable in the object browser.
	 * 
	 * @param instance
	 *            the instance
	 * @param filtersString
	 *            the filters string
	 * @return true, if is selectable
	 */
	protected Boolean isSelectable(Instance instance, String filtersString) {
		if (instance == null) {
			return Boolean.FALSE;
		}

		Map<String, Object> bindings = createFilterBindings(instance);
		Object evaluated = scriptEvaluator.eval(filtersString, bindings);
		if (evaluated != null) {
			return (Boolean) evaluated;
		}
		return Boolean.FALSE;
	}

	/**
	 * Creates the filter bindings to be used in filter evaluation.
	 * 
	 * @param instance
	 *            the instance
	 * @return the map
	 */
	private Map<String, Object> createFilterBindings(Instance instance) {
		Map<String, Object> bindings = new HashMap<String, Object>(3);
		// set instance type
		bindings.put("instanceType", instance.getClass().getSimpleName().toLowerCase());
		// set purpose if instance can have purpose
		// !!! for document sections we have no purpose, so the filter should pass purpose=null if
		// documents section is required to be selectable
		if (instance instanceof Purposable) {
			bindings.put(PURPOSE, ((Purposable) instance).getPurpose());
		} else {
			bindings.put(PURPOSE, null);
		}
		// set mimetype if exists
		String mimetype = (String) instance.getProperties().get(DocumentProperties.MIMETYPE);
		if (mimetype != null) {
			bindings.put(MIMETYPE, mimetype);
		} else {
			bindings.put(MIMETYPE, "");
		}
		return bindings;
	}

	/**
	 * Checks if a node is leaf.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is leaf
	 */
	private boolean isLeaf(Instance instance) {
		return leafInstances.contains(instance.getClass().getSimpleName().toLowerCase());
	}

	/**
	 * Gets the node text modifying the text according to configuration provided.
	 * 
	 * @param value
	 *            the value
	 * @param clickableLinks
	 *            If the links inside the node text should be clickable.
	 * @param clickOpenWindow
	 *            If the links inside the node text should open in new browser tab.
	 * @return the node text
	 */
	private String getNodeText(Serializable value, boolean clickableLinks, boolean clickOpenWindow) {
		if (value == null) {
			return "";
		}
		String result = (String) value;
		if (clickableLinks) {
			// if a link should be opened in a new browser tab, we append a target=blank attribute
			// to the link
			if (clickOpenWindow) {
				return result.replaceAll("<a ", "<a target=\"_blank\" ");
			}
			return result;
		}

		// if no clickbale links is required, we replace all links with span tags
		return convertLinkToSpan(result);
	}

	/**
	 * Gets a group label from bundle by key.
	 * 
	 * @param key
	 *            the key
	 * @return the group label
	 */
	private String getGroupLabel(String key) {
		return labelProvider.getValue("objects.explorer.group." + key);
	}

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	private LinkService getService() {
		if (solrLinkService.isUnsatisfied()) {
			return linkService;
		}
		return solrLinkService.get();
	}

}
