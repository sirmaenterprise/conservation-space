package com.sirma.sep.keycloak.ldap;

import static com.sirma.sep.keycloak.ldap.LdapConstants.DEFAULT_USER_ATTRIBUTE_MAPPER;
import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_MAPPER_PROVIDER_TYPE;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.json.JSON;

/**
 * Responsible for retrieving Keycloak-LDAP model.
 *
 * The model represents user attribute mappings between LDAP and Keycloak. Its retrieved from a JSON file defined as
 * resource in the source code.
 *
 * @author smustafov
 */
class KeycloakLdapModelRetriever {

	/**
	 * Retrieves Keycloak-LDAP user attribute mappings converted to internal Keycloak {@link ComponentRepresentation}.
	 *
	 * @param ldapProviderId the id of the LDAP provider that will be set as parent to the user mappings
	 * @return a list of {@link ComponentRepresentation}s representing user attribute mappings
	 */
	public List<ComponentRepresentation> retrieve(String ldapProviderId) {
		return JSON.readArray(getModelInputStream(), modelsArray -> {
			List<ComponentRepresentation> models = new ArrayList<>();

			for (JsonValue jsonValue : modelsArray) {
				JsonObject jsonObject = (JsonObject) jsonValue;

				ComponentRepresentation model = new ComponentRepresentation();
				model.setParentId(ldapProviderId);
				model.setName(jsonObject.getString("name"));
				model.setProviderId(jsonObject.getString("providerId", DEFAULT_USER_ATTRIBUTE_MAPPER));
				model.setProviderType(jsonObject.getString("providerType", LDAP_MAPPER_PROVIDER_TYPE));

				MultivaluedHashMap<String, String> modelConfig = new MultivaluedHashMap<>();
				JsonObject configJson = jsonObject.getJsonObject("config");
				Map<String, Serializable> config = JSON.jsonToMap(configJson);

				for (Map.Entry<String, Serializable> configEntry : config.entrySet()) {
					modelConfig.putSingle(configEntry.getKey(), (String) configEntry.getValue());
				}

				model.setConfig(modelConfig);
				models.add(model);
			}

			return models;
		});
	}

	private InputStream getModelInputStream() {
		return ResourceLoadUtil.loadResource("keycloak-ldap-model.json", getClass());
	}

}
