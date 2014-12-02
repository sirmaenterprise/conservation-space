package com.sirma.itt.objects.constants;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Defines all configuration name properties for Objects module.
 * 
 * @author BBonev
 */
@Documentation("Object specific configurations")
public interface ObjectsConfigProperties extends Configuration {
	/** path to config file. */
	@Documentation("Location of PM specific converter configuration")
	String CONFIG_OBJECTS_CONVERTER_LOCATION = "objects.convertor.dms.location";

	/** Codelist number of the object status property. */
	@Documentation("Codelist number of the object status property.")
	String OBJECT_STATE_CODELIST = "object.state.codelist";
	/** The object initial state. */
	@Documentation("The object initial state.")
	String OBJECT_STATE_INITIAL = "object.state.initial";
	/** The object state in progress. */
	@Documentation("The object state in progress.")
	String OBJECT_STATE_IN_PROGRESS = "object.state.in_progress";
	/** The object state completed. */
	@Documentation("The object state completed.")
	String OBJECT_STATE_COMPLETED = "object.state.completed";
	/** The object state deleted. */
	@Documentation("The object state deleted.")
	String OBJECT_STATE_DELETED = "object.state.deleted";
	/** The object state submitted. */
	@Documentation("The object state submitted.")
	String OBJECT_STATE_SUBMITTED = "object.state.submitted";
	/** The object state approved. */
	@Documentation("The object state approved.")
	String OBJECT_STATE_APPROVED = "object.state.approved";
	/** The object state on hold. */
	@Documentation("The object state on hold.")
	String OBJECT_STATE_ON_HOLD = "object.state.on_hold";
	/** The object state stopped. */
	@Documentation("The object state stopped.")
	String OBJECT_STATE_STOPPED = "object.state.stopped";
	/** The object state archived. */
	@Documentation("The object state archived.")
	String OBJECT_STATE_ARCHIVED = "object.state.archived";
	/**
	 * The default object template to load if there is no template defined for any object. Default
	 * value: GEO10001
	 */
	@Documentation("The default object template to load if there is no template defined for any object. Default value: GEO10001")
	String DEFAULT_OBJECT_TEMPLATE = "object.default.template";
	
	@Documentation("Used to specify types selected by default when searching for objects to attach to a section.")
	String SEARCH_ATTACH_DEFAULT_SELECTED_TYPES = "search.attach.defaultSelectedTypes";

	@Documentation("Used to filter the types available when searching for an object to attach to a section.")
	String SEARCH_ATTACH_AVAILABLE_TYPES_FILTER = "search.attach.availableTypesFilter";
}
