package com.sirma.itt.cmf.services.adapter;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * CMF search adapter service
 *
 * @author BBonev
 */
public interface CMFSearchAdapterService extends CMFAdapterService {

	/**
	 * Adapter method for searching in DMS sub system
	 *
	 * @param <E>
	 *            the Expected result type
	 * @param args
	 *            the search arguments
	 * @param model
	 *            is the searched model class. currently only 1 class is supported
	 * @return the search arguments with filled search result
	 * @throw {@link DMSException} on dms error
	 */
	<E extends FileDescriptor> SearchArguments<E> search(SearchArguments<E> args,
			Class<? extends Instance> model) throws DMSException;
}
