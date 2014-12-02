package com.sirma.itt.objects.web.caseinstance.tab;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for case objects tab toolbar where action buttons can be provided as plugins.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ObjectsTabActionsExtensionPoint implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "case:tab:objects.actions";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * The Class CreateCaseSectionAction.
	 */
	@Extension(target = EXTENSION_POINT, order = 20, enabled = false)
	public static class CreateCaseSectionAction implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/create-case-section-action.xhtml";
		}

	}

	/**
	 * The Class AttachObjectAction.
	 */
	@Extension(target = EXTENSION_POINT, order = 30, enabled = false)
	public static class AttachObjectAction implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/attach-object-action.xhtml";
		}

	}

	/**
	 * The Class CreateObjectAction.
	 */
	@Extension(target = EXTENSION_POINT, order = 40, enabled = false)
	public static class CreateObjectAction implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/create-object-action.xhtml";
		}

	}
}
