package com.sirma.cmf.web.document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sirma.cmf.web.instance.AttachInstanceAction;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;

/**
 * Rest service for manipulating and managing documents.
 * 
 * @author svelikov
 */
@Secure
@Path("/document")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DocumentRestService extends EmfRestService {

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	@Inject
	private AttachInstanceAction attachInstanceAction;

	/**
	 * Attach selected document to current section.
	 * 
	 * @param data
	 *            the request content in JSON string. Contains sectionId and selectedItems fields.
	 * @return the response
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response attachDocuments(String data) {
		log.debug("ObjectsWeb: DocumentRestService.attachDocuments request:" + data);
		Response response = attachInstanceAction.attachDocuments(data, this, instanceService,
				ActionTypeConstants.ATTACH_DOCUMENT);
		return response;
	}

}
