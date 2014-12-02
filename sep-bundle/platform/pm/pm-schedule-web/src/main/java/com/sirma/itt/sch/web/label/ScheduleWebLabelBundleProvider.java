package com.sirma.itt.sch.web.label;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for resources located in pm-schedule-web module.
 * 
 * @author svelikov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 10)
public class ScheduleWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.schedule.i18n.i18n";
	}

}
