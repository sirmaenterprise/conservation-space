package com.sirma.itt.seip.permissions.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * JSON body reader to {@link Permissions}
 *
 * @author BBonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class PermissionsBodyReader implements MessageBodyReader<Permissions> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Permissions.class.isAssignableFrom(type);
	}

	@Override
	public Permissions readFrom(Class<Permissions> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {
		return JSON.readObject(entityStream, json -> readPermissions(json));
	}

	private static Permissions readPermissions(JsonObject json) {
		Permissions permissions = new Permissions();
		permissions.setInheritedPermissions(json.getBoolean("inheritedPermissionsEnabled", false));
		permissions.setInheritedLibraryPermissions(json.getBoolean("inheritedLibraryPermissions", false));
		JsonArray array = json.getJsonArray("permissions");
		if (array != null) {
			array.getValuesAs(JsonObject.class).forEach(item -> readPermission(permissions, item));
		}

		return permissions;
	}

	private static void readPermission(Permissions permissions, JsonObject item) {
		String authority = item.getString("id");
		String permission = item.getString("special", null);
		permissions.getForAuthority(authority).setSpecial(permission);
	}

}
