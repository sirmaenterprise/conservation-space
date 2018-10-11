package com.sirma.itt.emf.cls.rest;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.event.EventService;

/**
 * Provides means to execute administration operation over code lists.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
@Path("/administration")
public class CodelistAdministrationRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private EventService eventService;

	/**
	 * Fires an event that causes reset of the code lists.
	 */
	@GET
	@Path("resetCodelists")
	public Response reloadCodeLists() {
		LOGGER.info("Triggered code lists reload from WS port");
		eventService.fire(new ResetCodelistEvent());
		return Response.ok().build();
	}
}