package com.sirma.sep.keycloak;

/**
 * Holds authentication client properties.
 */
public class ClientProperties {

	public static final String OIDC_LOGIN_PROTOCOL = "openid-connect";

	public static final String SEP_BACKEND_CLIENT_ID = "sep-backend";

	public static final String SEP_UI_CLIENT_ID = "sep-ui";

	public static final String SEP_EAI_CLIENT_ID = "sep-eai";

	public static final String TENANT_MAPPER_NAME = "tenant";

	public static final String USERNAME_MAPPER_NAME = "username";

	public static final String USERNAME_CLAIM_NAME = "preferred_username";

	public static final String SCRIPT_PROTOCOL_MAPPER = "oidc-script-based-protocol-mapper";

	public static final String USER_MODEL_PROPERTY_MAPPER = "oidc-usermodel-property-mapper";

	public static final String PROTOCOL_MAPPER_CLAIM_NAME = "claim.name";
	public static final String PROTOCOL_MAPPER_ACCESS_TOKEN = "access.token.claim";
	public static final String PROTOCOL_MAPPER_ID_TOKEN = "id.token.claim";
	public static final String PROTOCOL_MAPPER_USER_INFO_TOKEN = "userinfo.token.claim";
	public static final String PROTOCOL_MAPPER_VALUE_TYPE = "jsonType.label";
	public static final String PROTOCOL_MAPPER_SCRIPT = "script";
	public static final String PROTOCOL_MAPPER_USER_ATTRIBUTE = "user.attribute";

	public static final int ONE_WEEK_IN_SECONDS = 604800;

	public static final String SEP_THEME = "sep";

	private ClientProperties() {
		// constants class
	}

}
