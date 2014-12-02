package com.sirma.itt.pm.web.search.facet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.cmf.web.search.facet.SearchArgumentUpdater;
import com.sirma.cmf.web.search.facet.SearchArgumentsUpdaterExtension;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class ProjectSearchArgumentsUpdater.
 * 
 * @author svelikov
 */
@Extension(target = SearchArgumentsUpdaterExtension.TARGET_NAME, order = 30)
public class ProjectSearchArgumentsUpdater extends SearchArgumentUpdater implements
		SearchArgumentsUpdaterExtension<Entity> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(ProjectInstance.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	@Override
	public void updateArguments(SearchArguments<Entity> searchArguments) {
		ProjectInstance context = getDocumentContext().getInstance(ProjectInstance.class);
		if (context == null) {
			return;
		}
		boolean nested = searchArguments.getQuery() != null;
		Query finalQuery = null;
		if (nested) {
			finalQuery = new Query(ProjectProperties.OWNED_INSTANCES, context, true).and(
					searchArguments.getQuery()).end();
		} else {
			searchArguments.getArguments().put(ProjectProperties.OWNED_INSTANCES, context);
		}

		searchArguments.setQuery(finalQuery);
	}

}
