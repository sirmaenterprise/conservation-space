package com.sirma.itt.seip.instance.library;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service that provides access to class libraries.
 *
 * @author BBonev
 */
@ApplicationScoped
@Path("/libraries")
@Produces(Versions.V2_JSON)
public class LibraryRestService {

	@Inject
	private LibraryProvider libraryProvider;

	/**
	 * Gets the list of libraries for which the user has permissions to view. The libraries are sorted by title.
	 *
	 * @return the list of allowed libraries
	 */
	@GET
	public SearchArguments<Instance> getLibraries() {
		List<Instance> libraries = libraryProvider.getLibraries(ActionTypeConstants.VIEW_DETAILS);
		SearchArguments<Instance> result = new SearchArguments<>();
		result.setTotalItems(libraries.size());
		result.setResult(libraries);
		return result;
	}
}
