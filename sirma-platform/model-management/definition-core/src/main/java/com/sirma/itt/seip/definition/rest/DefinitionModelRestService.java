package com.sirma.itt.seip.definition.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.QUERY_IDS;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Can be used to retrieve {@link DefinitionModelObject} for given definition, without extracting the definition from
 * {@link Instance}.
 *
 * @see DictionaryService#find
 * @author A. Kunchev
 */
@Path("/definitions")
@ApplicationScoped
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
public class DefinitionModelRestService {

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * Retrieves {@link DefinitionModel}s for passed definition ids. The ids are passed as query parameters. Only the
	 * definition that are found will be returned in the result map.
	 *
	 * @param request
	 *            the {@link RequestInfo} from which we retrieve the definition ids
	 * @return map with definition ids as keys and build {@link DefinitionModelObject} for the found definitions as
	 *         values
	 */
	@GET
	public Map<String, DefinitionModelObject> getDefinitionModels(@BeanParam RequestInfo request) {
		Collection<String> definitionIds = QUERY_IDS.get(request);
		if (definitionIds.isEmpty()) {
			throw new BadRequestException("There are no definition ids in the request or the request key is wrong.");
		}

		return definitionIds.stream().map(dictionaryService::find).filter(Objects::nonNull).collect(
				Collectors.toMap(DefinitionModel::getIdentifier, new DefinitionModelObject()::setDefinitionModel));
	}

}
