package com.sirma.itt.pm.web.project.dashboard;

import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * ProjectDashboard is a marker interface for extension point plugins.
 * 
 * @author cdimitrov
 */
public interface ProjectDashboard extends Plugable {

	/** The Constant EXTENSION_POINT. */
	String EXTENSION_POINT = "projectDashboard";

}
