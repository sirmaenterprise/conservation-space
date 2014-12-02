package com.sirma.itt.emf.configuration;

import com.sirma.itt.emf.util.Documentation;

/**
 * State configuration properties for default instances such as Topics, links, users and groups
 * 
 * @author BBonev
 */
@Documentation("State configuration properties for default instances such as Topics, links, users and groups")
public interface EmfStateConfigurationProperties extends Configuration {
	/*
	 * GENERIC STATES
	 */
	/** The default state codelist. */
	@Documentation("The default state codelist. Default value: 11")
	String DEFAULT_STATE_CODELIST = "default.state.codelist";
	/** The default initial state. */
	@Documentation("The default initial state.")
	String DEFAULT_STATE_INITIAL = "default.state.initial";
	/** The default state in progress. */
	@Documentation("The default state in progress.")
	String DEFAULT_STATE_IN_PROGRESS = "default.state.in_progress";
	/** The default state completed. */
	@Documentation("The default state completed.")
	String DEFAULT_STATE_COMPLETED = "default.state.completed";
	/** The default state deleted. */
	@Documentation("The default state deleted.")
	String DEFAULT_STATE_DELETED = "default.state.deleted";
	/** The default state submitted. */
	@Documentation("The default state submitted.")
	String DEFAULT_STATE_SUBMITTED = "default.state.submitted";
	/** The default state approved. */
	@Documentation("The default state approved.")
	String DEFAULT_STATE_APPROVED = "default.state.approved";
	/** The default state on hold. */
	@Documentation("The default state on hold.")
	String DEFAULT_STATE_ON_HOLD = "default.state.on_hold";
	/** The default state stopped. */
	@Documentation("The default state stopped.")
	String DEFAULT_STATE_STOPPED = "default.state.stopped";
	/** The default state archived. */
	@Documentation("The default state archived.")
	String DEFAULT_STATE_ARCHIVED = "default.state.archived";
}
