package com.sirmaenterprise.sep.object.browser;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.DocumentProperties;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The "/" char. */
	protected static final int SLASH = 47;

	/** Number of results to be loaded when query for loading is executed. */
	// TODO use configuration
	private static final int ROOT_SELECTOR_LIMIT_COUNT = 25;
	private static final String PURPOSE = "purpose";
	private static final String MIMETYPE = "mimetype";
	private static final String NAME = "name";
	private static final String ID_IN_NODE_PATH_MATCHER = "%s.+|.+%s.+";
	private static final String ID_MATCHER = "\\w+:[\\w+-.]+";
	private static final String APPLY_INSTANCES_BY_TYPE_FILTER_FLAG = "enableInstancesByTypeFilter";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "tree.pager.pagesize", type = Integer.class, defaultValue = "500", label = "Number of elements (children) to be loaded when expanding tree node without showing a button to load more.")
	protected ConfigurationProperty<Integer> treePageSize;

	/**
	 * List of sort fields to use when returning results in the tree. The elements should be comma separated, each
	 * defined field could define a order direction by added |DESC or |ASC to the field name. If no order is specified
	 * an ASC order will be considered. <b>Default value: title|ASC</b>
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "tree.pager.sort", type = Set.class, defaultValue = DefaultProperties.TITLE, label = "List of sort fields to use when returning results in the tree. The elements should be comma separated, each defined field could define a order direction by added |DESC or |ASC to the field name. If no order is specified an ASC order will be considered.")
	protected ConfigurationProperty<Set<String>> treeSortFields;

	@Inject
	private ScriptEvaluator scriptEvaluator;

	@Inject
	private LinkService linkService;

	@Inject
	protected SearchService searchService;

	/**
	 * Provides a json array with root instances to be displayed inside the object browser. Response is in format:
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
	 * @param header
	 *            the header
	 * @return the roots
	 */
	@Path("roots")
	@GET
	public Response getRoots(@QueryParam("rootType") String rootType, @QueryParam("limit") Integer limit,
			@QueryParam("term") String term, @QueryParam("header") String header) {
		if (debug) {
			LOG.info("ObjectsBrowserRestService.getRoots rootType[{}] limit[{}] term[{}]", rootType, limit, term);
		}
		if (StringUtils.isNotBlank(rootType)) {
			return Response.status(Status.NOT_FOUND).build();
		}

		JSONArray data = new JSONArray();
		Integer localLimit = limit;
		if (localLimit == null) {
			localLimit = Integer.valueOf(ROOT_SELECTOR_LIMIT_COUNT);
		}

		@SuppressWarnings("unchecked")
		List<Instance> roots = (List<Instance>) loadRootInstances(rootType, localLimit, term);

		for (Instance instance : roots) {
			JSONObject item = new JSONObject();
			JsonUtil.addToJson(item, INSTANCE_ID, instance.getId());
			JsonUtil.addToJson(item, INSTANCE_TYPE, instance.getClass().getSimpleName().toLowerCase());
			String instanceHeader = (String) instance.getProperties().get(
					StringUtils.isNotBlank(header) ? header : DefaultProperties.HEADER_COMPACT);
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
	private List<? extends Instance> loadRootInstances(String actualRootType, Integer limit, String term) {
		Resource currentUser = getFreshCurrentUser();
		if (currentUser == null) {
			return Collections.emptyList();
		}

		String filterName = "customQueries/" + actualRootType + "_rootFilter";
		Context<String, Object> context = new Context<>(1);
		Uri userid = typeConverter.convert(Uri.class, currentUser.getId());
		context.put("userid", userid.toString());
		if (StringUtils.isNotBlank(term)) {
			context.put("term", " AND title:\"*" + term + "*\" content:\"*" + term + "*\"");
		}
		SearchArguments<SearchInstance> arguments = searchService.getFilter(filterName, SearchInstance.class, context);
		if (arguments == null) {
			LOG.warn("No filter found: " + filterName);
			return Collections.emptyList();
		}

		if (limit != null) {
			arguments.setMaxSize(limit.intValue());
		}
		searchService.searchAndLoad(Instance.class, arguments);
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
	 * @param header
	 *            the header
	 * @return Response which contains the built data store json
	 */
	@Path("tree")
	@GET
	public Response tree(@QueryParam("node") String node, @QueryParam("currId") String currentInstanceId, // NOSONAR
			@QueryParam("allowSelection") boolean allowSelection, @QueryParam("filters") String filters,
			@QueryParam("clickableLinks") boolean clickableLinks,
			@QueryParam("clickOpenWindow") boolean clickOpenWindow, @QueryParam("manualExpand") boolean manualExpand,
			@DefaultValue("1") @QueryParam("page") Integer page, @QueryParam("header") String header) {
		LOG.debug(
				"EMFWeb: ObjectsBrowserRestService.load node={}, currentInstanceId={}, allowSelection={}, filters={}, clickableLinks={}, clickOpenWindow={}, manualExpand={}",
				node, currentInstanceId, allowSelection, filters, clickableLinks, clickOpenWindow, manualExpand);

		// check required arguments
		if (StringUtils.isBlank(node)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing required arguments!").build();
		}

		String rootInstanceId = extractInstanceId(node);
		// load instance that should be expanded later
		Instance rootInstance = fetchInstance(rootInstanceId);

		if (rootInstance == null) {
			return Response
					.status(Response.Status.BAD_REQUEST)
						.entity("Can't find instance with id=" + rootInstanceId)
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
		if (!manualExpand && StringUtils.isNotBlank(currentInstanceId)) {
			currentNodePath = buildPathToCurrentNode(currentInstanceId);
		}

		// buid JSON response
		JSONArray nodes = buildData(grouped, rootInstance, node, false, currentNodePath, allowSelection, clickableLinks,
				clickOpenWindow, filters, header);

		// TODO: add nextPage element to the result if there are more elements for pagination
		String data = nodes.toString();

		LOG.debug("Tree node {} expand took {} s", rootInstanceId, tracker.stopInSeconds());

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
	protected LinkSearchArguments buildSearchArguments(Instance rootInstance, Integer page) {
		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setTo(rootInstance.toReference());
		arguments.setLinkId(InstanceContextService.HAS_PARENT);
		arguments.setPageNumber(page);
		arguments.setPermissionsType(QueryResultPermissionFilter.READ);

		arguments.setPageSize(treePageSize.get().intValue());
		for (String string : treeSortFields.get()) {
			Sorter sorter = Sorter.buildSorterFromConfig(string);
			sorter.setAllowMissingValues();
			arguments.addSorter(sorter);
		}
		arguments.getQueryConfigurations().put(APPLY_INSTANCES_BY_TYPE_FILTER_FLAG, false);
		return arguments;
	}

	/**
	 * Builds the path to current node. The path contains instance id's separated with '/'. If given node is not
	 * instance but a group, then its name is used instead of id because they don't have one.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @return the path
	 */
	protected String buildPathToCurrentNode(String currentInstanceId) {
		Instance currentInstanceNode = fetchInstance(currentInstanceId);
		if (currentInstanceNode == null) {
			return "";
		}

		List<Instance> parentInstances = InstanceUtil.getParentPath(currentInstanceNode);

		StringBuilder path = new StringBuilder();
		for (Instance parent : parentInstances) {
			path.append(parent.getId()).append("/");
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
		return InstanceUtil.getRootInstance(currentInstance);
	}

	/**
	 * Extract instance id.
	 *
	 * @param node
	 *            the node
	 * @return the string
	 */
	protected String extractInstanceId(String node) {
		if (StringUtils.isBlank(node)) {
			return "";
		}
		return node.substring(node.lastIndexOf(SLASH) + 1);
	}

	/**
	 * Group instances by type.
	 *
	 * @param loadedInstances
	 *            the loaded instances
	 * @return the map
	 */
	protected Map<String, List<Instance>> groupByType(Iterable<Instance> loadedInstances) {
		Map<String, List<Instance>> grouped = new HashMap<>();
		if (loadedInstances != null) {
			for (Instance instance : loadedInstances) {
				mapInstance(instance, grouped);
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
	 * Creates mapping for given key if it doesn't exists.
	 *
	 * @param key
	 *            the key
	 * @param grouped
	 *            the grouped
	 */
	protected void createMapping(String key, Map<String, List<Instance>> grouped) {
		if (StringUtils.isBlank(key)) {
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
			return new LinkIterable<>(arguments.getResult(), arguments.isFrom());
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
	 *            if children of the node should be rendered under common root or immediately under the node
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
	 * @param header
	 *            the header
	 * @return the jSON object representing the data store
	 */
	protected JSONArray buildData(Map<String, List<Instance>> grouped, Instance root, String node, // NOSONAR
			boolean renderGrouped, String currentNodePath, boolean allowSelection, boolean clickableLinks,
			boolean clickOpenWindow, String filtersString, String header) {
		JSONArray children = new JSONArray();

		if (grouped != null) {
			for (String groupId : grouped.keySet()) {
				// except for sections, all instances are grouped under group nodes
				// TODO: for objects and documents we also may have nesting but not grouped
				// TODO: for workflows we also may have nesting but not grouped
				if (renderGrouped) {
					String accumulatedWithGroupId = node + "/" + groupId;
					JSONArray groupChildren = new JSONArray();
					addChildren(grouped, groupId, accumulatedWithGroupId, groupChildren, currentNodePath,
							allowSelection, clickableLinks, clickOpenWindow, filtersString, header);
					if (groupChildren.length() > 0) {
						JSONObject group = new JSONObject();
						JsonUtil.addToJson(group, "id", accumulatedWithGroupId);
						JsonUtil.addToJson(group, "text", getGroupLabel(groupId));
						JsonUtil.addToJson(group, "cls", groupId + "-group");
						JsonUtil.addToJson(group, "leaf", Boolean.FALSE);
						JsonUtil.addToJson(group, "cnPath", currentNodePath);
						JsonUtil.addToJson(group, "children", groupChildren);
						children.put(group);
					}
				} else {
					String accumulatedId = node;
					addChildren(grouped, groupId, accumulatedId, children, currentNodePath, allowSelection,
							clickableLinks, clickOpenWindow, filtersString, header);
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
	 *            the group children is the json array inside which the children data objects should be added
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
	 * @param header
	 *            the header
	 */
	private void addChildren(Map<String, List<Instance>> grouped, String groupId, String accumulatedId,
			JSONArray groupChildren, String currentNodePath, boolean allowSelection, boolean clickableLinks,
			boolean clickOpenWindow, String filtersString, String header) {
		List<Instance> instanceGroup = grouped.get(groupId);
		for (Instance instance : instanceGroup) {
			boolean isInstanceInTree = false;
			String instanceId = instance.getId().toString();

			// skip group ids
			if (instanceId.matches(ID_MATCHER)) {
				isInstanceInTree = isInstanceExistInTree(accumulatedId, instanceId);
			}

			if (!isInstanceInTree) {
				boolean hasFilter = StringUtils.isNotBlank(filtersString);
				JSONObject child = new JSONObject();
				String accumulatedWithGroupId = accumulatedId + "/" + instance.getId();
				JsonUtil.addToJson(child, "id", accumulatedWithGroupId);
				JsonUtil.addToJson(child, "dbId", instance.getId());
				String textValue = getNodeText(instance, clickableLinks, clickOpenWindow, header);
				JsonUtil.addToJson(child, "text", textValue);
				JsonUtil.addToJson(child, "cls", instance.getClass().getSimpleName().toLowerCase());
				// leaf instances are those that doesn't allow children
				JsonUtil.addToJson(child, "leaf", Boolean.valueOf(false));
				// current node path is used to find the node in js plugin when expanding the tree
				JsonUtil.addToJson(child, "cnPath", currentNodePath);

				// evaluate filters only if there are filters provided
				Boolean isSelectable = Boolean.FALSE;
				if (StringUtils.isNotBlank(filtersString)) {
					isSelectable = isSelectable(instance, filtersString);
				}
				// if selection is allowed and no filters are provided, we allow selection for all
				// instances
				if (allowSelection && !hasFilter) {
					JsonUtil.addToJson(child, "checked", Boolean.FALSE);
				}
				// if selection is allowed and a filter is provided, then we allow selection only
				// for
				// selectable instances
				else if (allowSelection && hasFilter && isSelectable) {
					JsonUtil.addToJson(child, "checked", Boolean.FALSE);
				}
				groupChildren.put(child);
			} else {
				LOGGER.debug("Leaf[" + instanceId + "] is skipped from path[" + accumulatedId + "]");
			}
		}
	}

	/**
	 * Checks if given instance node can be selected in the ui. Filters string is evaluated as javascript boolean
	 * expression. And if evaluation is successful and is 'true' then instance can be selectable in the object browser.
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
		Map<String, Object> bindings = new HashMap<>(3);
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
	 * Checks if given instance id exists in the tree path. The check is done by regEx matcher, which covers the entire
	 * tree path.
	 *
	 * @param notePath
	 *            string representation of the path in the tree, build by the instances ids and group ids
	 * @param instanceId
	 *            the id of the instance that is added to the tree
	 * @return <b>TRUE</b> if the instance id matches some of the ids in the tree path, <b>FALSE</b> otherwise
	 */
	private static boolean isInstanceExistInTree(String notePath, String instanceId) {
		String regex = String.format(ID_IN_NODE_PATH_MATCHER, instanceId, instanceId);
		return notePath.matches(regex);
	}

	/**
	 * Gets the node text modifying the text according to configuration provided.
	 *
	 * @param instance
	 *            the instance
	 * @param clickableLinks
	 *            If the links inside the node text should be clickable.
	 * @param clickOpenWindow
	 *            If the links inside the node text should open in new browser tab.
	 * @param header
	 *            the header
	 * @return the node text
	 */
	private String getNodeText(Instance instance, boolean clickableLinks, boolean clickOpenWindow, String header) {
		Serializable value = instance.getProperties().get(DefaultProperties.HEADER_BREADCRUMB);
		if (StringUtils.isNotBlank(header)) {
			value = instance.getProperties().get(header);
		}
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
		return linkService;
	}

}
