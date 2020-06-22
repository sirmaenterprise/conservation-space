package com.sirma.itt.seip.eai.cs;

/**
 * Generic constants and properties used during model preparation/parsing and model transfer among services
 * 
 * @author bbanchev
 */
public class EAIServicesConstants {
	/** semantic class for Cultural object. */
	public static final String TYPE_CULTURAL_OBJECT = "http://www.sirma.com/ontologies/2016/02/culturalHeritageConservation#CulturalObject";
	/** semantic class for image object. */
	public static final String TYPE_IMAGE = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image";

	/** String for new line. */
	public static final String NEW_LINE = "\r\n";
	/** Flag to set the parameter whether to include search result references. By default true. */
	public static final String SEARCH_INCLUDE_REFERENCES = "includeReferences";
	/** Flag whether to instantiate missing in the system instance after search. Used in automatic operations. */
	public static final String SEARCH_INSTANTIATE_MISSING_INSTANCES = "instantiateMissing";
	/** Flag to set the parameter whether to include search result thumbnails. */
	public static final String SEARCH_INCLUDE_THUMBNAILS = "includeThumbnails";

	/** uri for external id. */
	public static final String URI_EXTERNAL_ID = "emf:externalID";
	/** uri for external type. */
	public static final String URI_EXTERNAL_TYPE = "chc:externalType";
	/** uri for external system id. */
	public static final String URI_SUB_SYSTEM_ID = "chc:sourceSystemId";

	public static final String PROPERTY_EXTERNAL_TYPE = "externalType";
	/** property for flag for integration. */
	public static final String PROPERTY_INTEGRATED_FLAG_ID = "integrated";
	/** property for flag for the integraion system id - TMS. */
	public static final String PROPERTY_INTEGRATED_SYSTEM_ID = "integratedSystemId";
	/** property for associated with value. */
	public static final String PROPERTY_REFERENCES = "references";

	private EAIServicesConstants() {
		// constants class
	}
}
