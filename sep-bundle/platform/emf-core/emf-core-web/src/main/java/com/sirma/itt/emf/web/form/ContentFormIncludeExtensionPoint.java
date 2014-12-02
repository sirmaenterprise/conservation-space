package com.sirma.itt.emf.web.form;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point that allows inclution of facelet templates in the end of the main form.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ContentFormIncludeExtensionPoint implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "content.form.include.extension.point";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
