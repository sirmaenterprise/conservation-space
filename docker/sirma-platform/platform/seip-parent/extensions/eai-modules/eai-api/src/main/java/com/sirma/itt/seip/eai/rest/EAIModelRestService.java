package com.sirma.itt.seip.eai.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.rest.RestService;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * The {@link EAIModelRestService} is the base API service that contains the common services for all integration
 * business logic.
 * 
 * @author bbanchev
 */
@Singleton
@Path("/integration")
public class EAIModelRestService implements RestService {
	@Inject
	private ModelService modelService;

	/**
	 * Provides the model used for mappings/conversions/requests. Response is in format: <br>
	 * <code>
	 * {"entities":[{"identifier":"NGACO7001","uri":"http://www.sirma.com/ontologies/2016/02/culturalHeritageConservation#Drawing","title":"Drawing","properties":[{"uri":"emf:modifiedOn","title":"System Modified On","propertyId":"modifiedOn","type":"dateTime","mandatory":false,"sealed":true,"externals":{"AS_CRITERIA":"cultObj:lastModifiedOn"}},{"uri":"dcterms:title","title":"Title (local primary)","propertyId":"title","type":"an..1000","mandatory":false,"sealed":true,"externals":{"AS_CRITERIA":"cultObj:title","AS_DATA":"cultObj:title"}},...]}],...}
	 * </code>
	 * 
	 * @param systemId
	 *            the system id - the subsytem identifier
	 * @return the list of {@link EntityType} holding all the related information as list of {@link EntityProperty} and
	 *         list of {@link EntityRelation}
	 */
	@GET
	@Path("/{systemId}/model")
	@Produces(value = { MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public ModelConfiguration provideModel(@PathParam("systemId") String systemId) {
		try {
			return modelService.getModelConfiguration(systemId.toUpperCase());
		} catch (Exception e) {
			throw new RestServiceException("Failed to retrieve system model for " + systemId,
					Status.INTERNAL_SERVER_ERROR, e);
		}
	}

}
