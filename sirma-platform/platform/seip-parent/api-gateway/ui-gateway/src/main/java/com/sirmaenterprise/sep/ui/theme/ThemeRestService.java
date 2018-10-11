package com.sirmaenterprise.sep.ui.theme;

import com.sirma.itt.seip.rest.utils.Versions;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Rest service used to retrieve UI specific theme information.
 *
 * @author g.tsankov
 */
@Path("theme")
@ApplicationScoped
@Produces(Versions.V2_JSON)
public class ThemeRestService {

	@Inject private ThemeService themeService;


	/**
	 * Retrieves the theme values defined in the definition model.
	 *
	 * @return map of property-value styles that should be applied to the UI.
	 * Empty map if an error occurs with definiton serialization.
	 *
	 *
	 */
	@GET
	public Map<String, String> getTheme()  {
		return themeService.getUiTheme();
	}
}
