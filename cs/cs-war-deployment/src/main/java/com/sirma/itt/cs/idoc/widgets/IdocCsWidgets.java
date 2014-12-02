package com.sirma.itt.cs.idoc.widgets;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.idoc.web.widget.IdocExtension;
import com.sirma.itt.idoc.widgets.IdocDefaultWidgets;

/**
 * Idoc widgets for CS project.
 * 
 * @author svelikov
 */
public class IdocCsWidgets extends IdocDefaultWidgets {

	/**
	 * The Class ReportingWidgetDisabled.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = false, order = 1000, priority = 2)
	public static class ReportingWidgetDisabled implements IdocExtension {

		@Override
		public String getPath() {
			return "/widgets/reporting/";
		}

		@Override
		public Boolean hasStylesheet() {
			return Boolean.TRUE;
		}
	}

	/**
	 * Definition for the objects list widget.
	 */
	@Extension(target = IdocExtension.EXTENSION_POINT, enabled = false, order = 1300, priority = 2)
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
