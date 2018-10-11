package com.sirma.itt.seip.instance.version.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionsResponse;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service for instance version functionality. Contains logic for retrieving instance versions.
 *
 * @author A. Kunchev
 */
@Transactional
@Path("/instances")
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceVersionRestService {

	@Inject
	private InstanceVersionService instanceVersionService;

	/**
	 * Retrieves all versions of given instance id. The versions are returned as loaded instances. Each version is
	 * loaded with its own properties, when they are created. Note that this method will not return the current version
	 * for the passed instance id, it only returns its versions.
	 *
	 * @param id
	 *            the id of the instance which versions should be retrieved
	 * @param offset
	 *            the number of the results that should be skipped in the returned collection. Used for paging, if
	 *            missing its default value is <code>0</code>
	 * @param limit
	 *            the number of the results that should be returned. Used for paging, if missing its default value is
	 *            <code>-1</code>, which means that all found results will be returned
	 * @return the versions for the given instance
	 */
	@GET
	@Path("/{id}/versions")
	public VersionsResponse getVersions(@PathParam(KEY_ID) String id,
			@DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("-1") @QueryParam("limit") int limit) {
		return instanceVersionService.getInstanceVersions(id, offset, limit);
	}

}
