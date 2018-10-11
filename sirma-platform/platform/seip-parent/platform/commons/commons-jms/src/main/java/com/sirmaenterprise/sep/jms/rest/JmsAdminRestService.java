package com.sirmaenterprise.sep.jms.rest;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirmaenterprise.sep.jms.impl.receiver.JmsReceiverManager;
import com.sirmaenterprise.sep.jms.impl.receiver.ReceiversInfo;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;
import com.sirmaenterprise.sep.jms.provision.JmsProvisioner;

/**
 * Rest service for managing and monitoring the JMS sub system.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/05/2017
 */
@AdminResource
@Path("/jms/admin")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JmsAdminRestService {

	@Inject
	private JmsReceiverManager jmsReceiverManager;

	@Inject
	private Instance<JmsProvisioner> jmsProvisioner;

	/**
	 * Return information about all configured JMS destinations and their address settings including wildcard settings
	 *
	 * @return list of the active destination definitions found in the system
	 * @throws RollbackedException in case of failure to retrieve the information
	 */
	@GET
	@Path("destinations")
	public Collection<DestinationDefinition> getAllDefinitions() throws RollbackedException {
		return jmsProvisioner.get().resolveAll();
	}

	/**
	 * Resolves information about a single JMS destination and it's address settings. Can return wildcard settings as well.
	 *
	 * @param address the address setting name to resolve
	 * @return the found active destination address definition
	 * @throws RollbackedException in case of failure to retrieve the information
	 * @throws NotFoundException in case no destination is found with the given name
	 */
	@GET
	@Path("destinations/{address}")
	public DestinationDefinition getDefinition(@PathParam("address") String address) throws RollbackedException {
		String destinationAddress = buildDestinationAddress(address);
		return jmsProvisioner.get()
				.resolve(destinationAddress)
				.orElseThrow(() -> new NotFoundException("Destination '" + destinationAddress + "' could not be found"));
	}

	private String buildDestinationAddress(String address) {
		String destinationAddress;
		if (address.startsWith("/")) {
			destinationAddress = "java:" + address;
		} else if (address.startsWith("java:/")) {
			destinationAddress = address;
		} else {
			destinationAddress = "java:/" + address;
		}
		return destinationAddress;
	}

	/**
	 * Update the settings for existing address settings configuration or add new address settings for the given address
	 *
	 * @param address the target address that need to be match for the new configuration existing or new
	 * @param definition the definition to set. It could be full or partial definition. If updating existing definition
	 * the omitted values will be retried from the deployed settings and updated with the passed values
	 * @return the settings after the update
	 * @throws RollbackedException in case no destination is found with the given name
	 */
	@POST
	@Path("destinations/{address}")
	public DestinationDefinition updateDefinition(@PathParam("address") String address,
			DestinationDefinition definition) throws RollbackedException {
		String destinationAddress = buildDestinationAddress(address);
		JmsProvisioner provisioner = this.jmsProvisioner.get();
		DestinationDefinition current = provisioner
				.resolve(destinationAddress)
				.orElseGet(() -> definition.setAddress(destinationAddress));
		current.copyFrom(definition);
		provisioner.provisionDestination(current);
		return getDefinition(address);
	}

	/**
	 * Fetches the status of the active JMS receivers
	 *
	 * @return the status
	 */
	@GET
	@Path("/receivers/info")
	public ReceiversInfo getInfo() {
		return jmsReceiverManager.getInfo();
	}

	/**
	 * Restarts all receivers
	 *
	 * @return success
	 */
	@POST
	@Path("/receivers/restart")
	public Response restart() {
		jmsReceiverManager.restart();
		return Response.ok("{\"success\": true}").build();
	}
}
