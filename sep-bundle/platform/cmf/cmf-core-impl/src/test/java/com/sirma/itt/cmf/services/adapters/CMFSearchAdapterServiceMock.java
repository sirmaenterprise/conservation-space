package com.sirma.itt.cmf.services.adapters;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The CMFSearchAdapterService mock service.
 */
@ApplicationScoped
public class CMFSearchAdapterServiceMock implements CMFSearchAdapterService {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public <E extends FileDescriptor> SearchArguments<E> search(SearchArguments<E> args,
			Class<? extends Instance> model) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}
}
