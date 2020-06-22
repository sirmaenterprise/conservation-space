package com.sirma.itt.seip.configuration.rest;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.configuration.event.ConfigurationReloadRequest;
import com.sirma.itt.seip.event.EventService;

/**
 * Service providing read and write access to persisted configurations - system or per tenant.
 *
 * @author BBonev
 */
@Path("/configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class ConfigurationsRestService {

	@Inject
	private EventService eventService;

	@Inject
	private ConfigurationManagement configurationManagement;

	/**
	 * Triggers full configuration reloading. It will cycle through all available configurations and invoke
	 * {@link ConfigurationProperty#valueUpdated()} for each one of them.
	 * <p>
	 * The reloading is transactional to allow any eventual read/write from the database.
	 *
	 * @return response with status OK(200) when the configurations are reloaded
	 */
	@GET
	@Path("/reload")
	public Response reloadConfigurations() {
		eventService.fire(new ConfigurationReloadRequest());
		return Response.status(Response.Status.OK).build();
	}

	/**
	 * Returns all present configurations in the system - this includes both system and tenants' configurations.
	 *
	 * @param filter - a string used to filter configurations before returning them
	 * @return {@link Collection} of filtered {@link Configuration}
	 */
	@GET
	public Collection<Configuration> getAllConfigurations(@QueryParam("q") String filter) {
		Collection<Configuration> collection = configurationManagement.getAllConfigurations();
		return filter(collection.stream(), filter);
	}

	/**
	 * Gets the configurations of the tenant in which the user is logged in and performing the request.
	 *
	 * @param filter - a string used to filter configurations before returning them
	 * @return {@link Collection} of filtered tenant {@link Configuration}
	 */
	@GET
	@Path("/tenant")
	public Collection<Configuration> getConfigurations(@QueryParam("q") String filter) {
		Collection<Configuration> collection = configurationManagement.getCurrentTenantConfigurations();
		return filter(collection.stream(), filter);
	}

	/**
	 * Returns the available system configurations - those who are not tenant aware but are part of the application set.
	 *
	 * @param filter - a string used to filter configurations before returning them
	 * @return {@link Collection} of filtered system {@link Configuration}
	 */
	@GET
	@Path("/system")
	public Collection<Configuration> getSystemConfigurations(@QueryParam("q") String filter) {
		Collection<Configuration> collection = configurationManagement.getSystemConfigurations();
		return filter(collection.stream().filter(Configuration::isSystem), filter);
	}

	private static Collection<Configuration> filter(Stream<Configuration> configurations, String filter) {
		return configurations.filter(buildFilter(filter)).collect(Collectors.toList());
	}

	private static Predicate<? super Configuration> buildFilter(String filter) {
		if (StringUtils.isBlank(filter)) {
			return c -> true;
		}
		String filterPattern = filter.replace(".", "\\.").replace("*", ".*");
		Pattern pattern = Pattern.compile(filterPattern);
		return c -> pattern.matcher(c.getConfigurationKey()).matches() || pattern.matcher(c.getAlias()).matches()
				|| pattern.matcher(c.getSubSystem()).matches();
	}

	/**
	 * Updates the provided configurations for the tenant in which the user is logged in and performing the request.
	 * After the update is complete, the service returns all of the tenant's configurations.
	 * <p>
	 * The update needs only key/value to be present in {@link Configuration} for it to work.
	 * <p>
	 * Only the tenant administrator could perform this request.
	 *
	 * @param configurations - the provided {@link Collection} of {@link Configuration} for updating
	 * @return {@link Collection} of the tenant's {@link Configuration} after the update
	 */
	@POST
	@Path("/tenant")
	public Collection<Configuration> updateTenantConfig(Collection<Configuration> configurations) {
		configurationManagement.updateConfigurations(configurations);
		return getConfigurations(null);
	}

	/**
	 * Updates the system(application) configurations with the provided ones.
	 * After the update is complete, the service returns all of the system configurations.
	 * <p>
	 * The update needs only key/value to be present in {@link Configuration} for it to work.
	 * <p>
	 * Only the system administrator could perform this request.
	 *
	 * @param configurations - the provided {@link Collection} of {@link Configuration} for updating
	 * @return {@link Collection} of the system's {@link Configuration} after the update
	 */
	@POST
	@Path("/system")
	public Collection<Configuration> updateSystemConfig(Collection<Configuration> configurations) {
		configurationManagement.updateSystemConfigurations(configurations);
		return getSystemConfigurations(null);
	}
}
