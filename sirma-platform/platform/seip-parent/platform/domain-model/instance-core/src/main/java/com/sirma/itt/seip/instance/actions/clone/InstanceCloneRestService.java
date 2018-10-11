package com.sirma.itt.seip.instance.actions.clone;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Contains the web services that are needed for the clone operation.
 * <p>
 * The web uses several services for cloning the instance
 * <li>http GET that clones the {@link Instance} object, but without saving it
 * <li>http post to do the actual persist of the cloned instance
 * <p>
 * <br>
 * Created by Ivo Rusev on 9.12.2016 г.
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
@Transactional
public class InstanceCloneRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Actions actions;
	@Inject
	private EventService eventService;
	@Inject
	private DomainInstanceService domainInstanceService;

	/**
	 * Executes the clone operation. This is the service that persists an Instance with it's corresponding content.
	 *
	 * @param request
	 *            contains the request data.
	 * @return the cloned instance.
	 */
	@POST
	@Path("/{id}/actions/clone")
	public Instance executeCloneAction(InstanceCloneRequest request) {
		// CMF-22293 The normal behaviour of the audit operation is to log 'clone' operation for the new instance.
		// However
		// the default audit logic should be overridden because there's an improvement that states that only create
		// operation for the cloned object should be logged in the audit. See the comment in the Jira task for further
		// information.
		eventService.fire(new AuditableEvent(request.getClonedInstance(), ActionTypeConstants.CREATE));
		LOGGER.debug("Skipping audit log clone for instance {} ", request.getTargetId());
		return Options.DISABLE_AUDIT_LOG.wrap(() -> (Instance) actions.callAction(request)).get();
	}

	/**
	 * Executes clone on an {@link Instance} object. This method only clones a given {@link Instance} object without
	 * persisting it anywhere.
	 *
	 * @param id
	 *            the emf:... identifier of the instance.
	 * @return the newly cloned Instance.
	 */
	@GET
	@Path("/{id}/actions/clone")
	public Instance clone(@PathParam(KEY_ID) String id) {
		Operation operation = new Operation(InstanceCloneRequest.OPERATION_NAME, true);
		return domainInstanceService.clone(id, operation);
	}

}
