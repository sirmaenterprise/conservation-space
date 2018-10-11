package com.sirma.itt.cmf.alfresco4.content;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.sep.content.ContentStore;

/**
 * Special {@link ContentStore} implementation that extends the {@link Alfresco4ContentStore} to handle the instance
 * views. The views are marked with different aspect in alfresco.
 *
 * @author BBonev
 */
@Named(Alfresco4ViewContentStore.VIEW_STORE_NAME)
@ApplicationScoped
public class Alfresco4ViewContentStore extends Alfresco4ContentStore {

	private static final Set<String> ATTACHMENT_ASPECT = Collections
			.singleton(DocumentProperties.TYPE_DOCUMENT_STRUCTURED);

	static final String VIEW_STORE_NAME = "alfresco4View";

	@Override
	public String getName() {
		return Alfresco4ViewContentStore.VIEW_STORE_NAME;
	}

	@Override
	protected Set<String> getContentAspect() {
		return ATTACHMENT_ASPECT;
	}

}
