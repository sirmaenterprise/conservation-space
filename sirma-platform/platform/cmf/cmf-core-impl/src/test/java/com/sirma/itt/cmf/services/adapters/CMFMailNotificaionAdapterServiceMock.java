package com.sirma.itt.cmf.services.adapters;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.adapter.CMFMailNotificaionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The CMFMailNotificaionAdapterService mock providing no templates.
 */
@ApplicationScoped
public class CMFMailNotificaionAdapterServiceMock implements CMFMailNotificaionAdapterService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1272977473638476911L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileDescriptor> getTemplates() throws DMSException {
		return Collections.emptyList();
	}

}
