package com.sirma.itt.seip.resources.rest;

import static com.sirma.itt.seip.rest.utils.JsonKeys.HIGHLIGHT;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Rest service for user and group administration page. The service could be accessed only by tenant administrators
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/08/2017
 */
@AdminResource
@ApplicationScoped
@Path("/administration")
@Produces(Versions.V2_JSON)
public class ResourceAdministrationRestService {

	@Inject
	private ResourceService resourceService;
	@Inject
	private SearchService searchService;
	@Inject
	private SecurityContext securityContext;

	/**
	 * Returns all users or part of them based on the search query arguments. The service will return active and inactive users
	 *
	 * @param resourceName a resource to select. If present the returned result page will be positioned to include the
	 * given resource. In this case the offset parameter will be ignored and only the page size will be considered
	 * @param uriInfo all request information
	 * @return a search arguments that contains the found users
	 */
	@GET
	@Path("/users")
	@Consumes(Versions.V2_JSON)
	public SearchArguments<Instance> getAllUsers(@QueryParam(HIGHLIGHT) String resourceName, @Context UriInfo uriInfo) {
		return getAllResources(ResourceType.USER, appendTenantIfNeeded(resourceName), uriInfo);
	}

	private String appendTenantIfNeeded(String resourceName) {
		if (resourceName == null) {
			return null;
		}
		if (resourceName.contains(":")) {
			// it's database id should not modify it
			return resourceName;
		}
		// make sure the user name is in full format username@tenant
		return SecurityUtil.buildTenantUserId(resourceName, securityContext.getCurrentTenantId());
	}

	/**
	 * Returns all groups or part of them based on the search query arguments.
	 *
	 * @param resourceName a resource to select. If present the returned result page will be positioned to include the
	 * given resource. In this case the offset parameter will be ignored and only the page size will be considered
	 * @param uriInfo all request information
	 * @return a search arguments that contains the found groups
	 */
	@GET
	@Path("/groups")
	@Consumes(Versions.V2_JSON)
	public SearchArguments<Instance> getAllGroups(@QueryParam(HIGHLIGHT) String resourceName,
			@Context UriInfo uriInfo) {
		return getAllResources(ResourceType.GROUP, resourceName, uriInfo);
	}

	private SearchArguments<Instance> getAllResources(ResourceType resourceType, String resourceName,
			UriInfo uriInfo) {
		SearchRequest searchRequest = new SearchRequest(uriInfo.getQueryParameters());
		SearchArguments<Instance> arguments = searchService.parseRequest(searchRequest);
		if (arguments == null) {
			throw new ResourceException(Response.Status.BAD_REQUEST, "Cannot parse search request");
		}

		List<Resource> allResources = resourceService.getAllResources(resourceType, DefaultProperties.TITLE);
		if (resourceType.equals(ResourceType.GROUP)) {
			allResources = allResources.stream()
					.filter(filterOutAllOthersGroup(resourceService.getAllOtherUsers().getId()))
					.collect(Collectors.toList());
		}

		Collection<Resource> resources = selectPage(allResources, arguments, resourceName);

		arguments.setResult(resources.stream()
				.map(Instance.class::cast)
				.collect(Collectors.toList()));
		arguments.setTotalItems(allResources.size());
		return arguments;
	}

	private static Predicate<Resource> filterOutAllOthersGroup(Serializable allOtherUsersId) {
		return resource -> !resource.getId().equals(allOtherUsersId);
	}

	private static Collection<Resource> selectPage(List<Resource> resources, SearchArguments<Instance> arguments,
			String resourceName) {
		if (StringUtils.isBlank(resourceName)) {
			int start = Math.min((arguments.getPageNumber() - 1) * arguments.getPageSize(), resources.size());
			int end = Math.min(start + arguments.getPageSize(), resources.size());
			return resources.subList(start, end);
		}

		AtomicInteger resourcePageNumber = new AtomicInteger(0);

		return FragmentedWork.doWorkWithResult(resources, arguments.getPageSize(), page -> {
			resourcePageNumber.incrementAndGet();
			Optional<Resource> resource = page.stream().filter(matchResource(resourceName)).findFirst();
			if (resource.isPresent()) {
				// we found the resource in the current page, return it
				arguments.setPageNumber(resourcePageNumber.get());
				arguments.setHighlight(Collections.singletonList(resource.get().getId().toString()));
				return page;
			}
			// not found in the page, skip the page
			return Collections.emptyList();
		});
	}

	private static Predicate<Resource> matchResource(String resourceName) {
		return resource -> resource.getName().equalsIgnoreCase(resourceName) || resource.getId().toString().equalsIgnoreCase(resourceName);
	}
}
