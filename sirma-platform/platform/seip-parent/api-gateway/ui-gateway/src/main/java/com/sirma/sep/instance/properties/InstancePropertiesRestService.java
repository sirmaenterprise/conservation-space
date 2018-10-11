package com.sirma.sep.instance.properties;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Provides services for loading specific object properties for instance.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstancePropertiesRestService {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceRelationsService instanceRelationsService;

	/**
	 * Retrieves all values for specific object property for given instance. Supports paging by passing limit and
	 * offset. If all object properties should be returned offset should be {@code 0} and the limit {@code -1}.
	 *
	 * @param id of the instance from which the values should be retrieved
	 * @param property the name of the object property which value/s should be returned
	 * @param offset the offset, used for pagination.<i> Default value {@code 0}</i>
	 * @param limit the limit, used for pagination.<i> Default value {@code 5}</i>
	 * @return {@link List} of values for the requested object property
	 */
	@GET
	@Path("/{id}/object-properties")
	public List<String> loadObjectProperty(@PathParam(RequestParams.KEY_ID) String id,
			@QueryParam(RequestParams.KEY_PROPERTY_NAME) String property,
			@DefaultValue("0") @QueryParam(RequestParams.KEY_OFFSET) int offset,
			@DefaultValue("5") @QueryParam(RequestParams.KEY_LIMIT) int limit) {
		if (StringUtils.isBlank(property)) {
			throw new BadRequestException("Property name is required!");
		}

		Instance instance = instanceTypeResolver
				.resolveReference(id)
					.map(InstanceReference::toInstance)
					.orElseThrow(() -> new InstanceNotFoundException(id));
		return instanceRelationsService.evaluateRelations(instance, property, offset, limit);
	}
}
