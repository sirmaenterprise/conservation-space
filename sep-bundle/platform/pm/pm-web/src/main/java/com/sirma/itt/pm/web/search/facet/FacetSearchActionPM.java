package com.sirma.itt.pm.web.search.facet;

import javax.inject.Inject;

import com.sirma.cmf.web.search.facet.FacetSearchAction;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Action class backing the facet search component in PM
 * 
 * @param <E>
 *            the element type
 * @param <A>
 *            the generic type
 * @author BBonev
 */
public abstract class FacetSearchActionPM<E extends Entity, A extends SearchArguments<E>> extends
		FacetSearchAction<E, A> {

	@Inject
	protected ProjectService projectService;

	// @Override
	// public PageContext getDocumentContext() {
	// return (PageContext) super.getDocumentContext();
	// }

}
