package com.sirma.itt.seip.eai.content.tool.model;

/**
 * Content API specific constants
 * 
 * @author gshefkedov
 * @author bbanchev
 */
public class EAIContentConstants {
	/** represents the content source as filename. */
	public static final String CONTENT_SOURCE = "contentSource";
	/** The content id that represents the primary content of the instance. */
	public static final String PRIMARY_CONTENT_ID = "emf:contentId";

	/** json2 mimetype value. */
	public static final String MIMETYPE_APPLICATION_VND_SEIP_V2_JSON = "application/vnd.seip.v2+json";
	/** header key for authorization. */
	public static final String HEADER_AUTHORIZATION = "Authorization";
	/** header key for content type. */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	/** header key for accept. */
	public static final String HEADER_ACCEPT = "Accept";
	/** key for http GET method to use with {@link #setRequestMethodType(String)}. */
	public static final String METHOD_GET = "GET";
	/** key for http POST method to use with {@link #setRequestMethodType(String)}. */
	public static final String METHOD_POST = "POST";

	private EAIContentConstants() {
		// constants class
	}
}
