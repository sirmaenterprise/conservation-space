package com.sirma.cmf.web.document.facet;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * DocumentFacet panels extension point.
 * 
 * @author svelikov
 */
@Named
public class DocumentFacetPanel implements DocumentFacet {

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * DocumentPropertiesFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class DocumentPropertiesFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-document-properties.xhtml";
		}
	}

	/**
	 * ReferencesFacet extension. <br />
	 * NOTE: temporary hide this facet - CS-391
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = false, order = 30, priority = 1)
	public static class ReferencesFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-references.xhtml";
		}
	}

	/**
	 * DocumentTagsFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = false, order = 40, priority = 1)
	public static class DocumentTagsFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-document-tags.xhtml";
		}
	}

	/**
	 * ShareLinkFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = true, order = 50, priority = 1)
	public static class ShareLinkFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-share-link.xhtml";
		}
	}

	/**
	 * PermissionsFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = false, order = 60, priority = 1)
	public static class PermissionsFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-permissions.xhtml";
		}
	}

	/**
	 * PublishHistoryFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = false, order = 70, priority = 1)
	public static class PublishHistoryFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-publish-history.xhtml";
		}
	}

	/**
	 * VersionHistoryFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = true, order = 80, priority = 1)
	public static class VersionHistoryFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-version-history.xhtml";
		}
	}

}
