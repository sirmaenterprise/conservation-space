package com.sirma.itt.pm.constants;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Defines all configuration name properties for pm.
 *
 * @author bbanchev
 */
@Documentation("PM specific configurations")
public interface PMConfigProperties extends Configuration {
	/** path to config file. */
	@Documentation("Location of PM specific converter configuration")
	String CONFIG_PM_CONVERTER_LOCATION = "pm.convertor.dms.location";

	/** Codelist number of the project status property. */
	@Documentation("Codelist number of the project status property.")
	String PROJECT_STATE_CODELIST = "project.state.codelist";
	/** The project initial state. */
	@Documentation("The project initial state.")
	String PROJECT_STATE_INITIAL = "project.state.initial";
	/** The project state in progress. */
	@Documentation("The project state in progress.")
	String PROJECT_STATE_IN_PROGRESS = "project.state.in_progress";
	/** The project state completed. */
	@Documentation("The project state completed.")
	String PROJECT_STATE_COMPLETED = "project.state.completed";
	/** The project state deleted. */
	@Documentation("The project state deleted.")
	String PROJECT_STATE_DELETED = "project.state.deleted";
	/** The project state submitted. */
	@Documentation("The project state submitted.")
	String PROJECT_STATE_SUBMITTED = "project.state.submitted";
	/** The project state approved. */
	@Documentation("The project state approved.")
	String PROJECT_STATE_APPROVED = "project.state.approved";
	/** The project state on hold. */
	@Documentation("The project state on hold.")
	String PROJECT_STATE_ON_HOLD = "project.state.on_hold";
	/** The project state stopped. */
	@Documentation("The project state stopped.")
	String PROJECT_STATE_STOPPED = "project.state.stopped";
	/** The project state archived. */
	@Documentation("The project state archived.")
	String PROJECT_STATE_ARCHIVED = "project.state.archived";
}
