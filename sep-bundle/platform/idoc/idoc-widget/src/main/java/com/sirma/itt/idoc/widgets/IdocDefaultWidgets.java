package com.sirma.itt.idoc.widgets;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.idoc.web.widget.IdocExtension;
import com.sirma.itt.idoc.web.widget.IdocWidget;

/**
 * Registered default idoc widgets.
 * 
 * @author svelikov
 */
public class IdocDefaultWidgets implements IdocWidget {

	/**
	 * The Class CheckboxWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 100, priority = 1)
	public static class CheckboxWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/checkbox/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class DatatableWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 200, priority = 1)
	public static class DatatableWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/datatable/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class DropdownWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 300, priority = 1)
	public static class DropdownWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/dropdown/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class ImageWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 400, priority = 1)
	public static class ImageWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/imageWidget/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class IFrameWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 500, priority = 1)
	public static class IFrameWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/insertFrame/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class ObjectDataWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 600, priority = 1)
	public static class ObjectDataWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/objectData/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class RadiobuttonWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 700, priority = 1)
	public static class RadiobuttonWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/radiobutton/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class RelatedDocumentsWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 800, priority = 1)
	public static class RelatedDocumentsWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/relatedDocuments/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class RelationshipsWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 900, priority = 1)
	public static class RelationshipsWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/relationships/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class ReportingWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 1000, priority = 1)
	public static class ReportingWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/reporting/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class TextboxWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 1100, priority = 1)
	public static class TextboxWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/textbox/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * The Class VersionsWidget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 1200, priority = 1)
	public static class VersionsWidget implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/versions/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * Definition for the objects list widget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = true, order = 1300, priority = 1)
	public static class ObjectsList implements IdocExtension {

		@Override
		public String getPath() {
			return "widgets/objectsList/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}
}
