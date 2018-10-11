package com.sirma.itt.seip.permissions.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * REST body writer for {@link Permissions} model
 *
 * @author BBonev
 */
@Provider
@Produces(Versions.V2_JSON)
public class PermissionsBodyWriter extends AbstractMessageBodyWriter<Permissions> {

	@Inject
	private ResourceService resourceService;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Permissions.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(Permissions permissions, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generateModel(permissions, generator);
			entityStream.flush();
		}
	}

	private void generateModel(Permissions permissions, JsonGenerator generator) {
		generator.writeStartObject();

		generator.writeStartArray("permissions");
		for (PermissionEntry entry : permissions) {
			if (entry.getAuthority() != null) {
				writePermissionEntry(entry, generator);
			}
		}
		generator.writeEnd();

		generator
				.write("editAllowed", permissions.isEditAllowed())
					.write("restoreAllowed", permissions.isRestoreAllowed())
					.write("isRoot", permissions.isRoot())
					.write("inheritedPermissionsEnabled", permissions.isInheritedPermissions())
					.write("inheritedLibraryPermissions", permissions.isInheritedLibraryPermissions());

		// these are class-related properties and should be returned only when we have a class instance
		if (permissions.getReference().getType().is("classinstance")) {
			generator.write("inheritLibraryPermissions", permissions.isAllowInheritLibraryPermissions()).write(
					"inheritParentPermissions", permissions.isAllowInheritParentPermissions());
		}

		generator.writeEnd();
	}

	private void writePermissionEntry(PermissionEntry entry, JsonGenerator generator) {
		Resource authority = (Resource) resourceService.loadByDbId(entry.getAuthority());
		if (authority == null) {
			return;
		}
		generator.writeStartObject();

		writeAuthority(generator, authority, entry.isManager());

		JSON.addIfNotNull(generator, "calculated", entry.getCalculated());
		JSON.addIfNotNull(generator, "special", entry.getSpecial());
		JSON.addIfNotNull(generator, "inherited", entry.getInherited());
		JSON.addIfNotNull(generator, "library", entry.getLibrary());

		generator.writeEnd();
	}

	private static void writeAuthority(JsonGenerator generator, Resource authority, boolean isManager) {
		ResourceType resourceType = authority.getType();
		generator
				.write("type", resourceType.getName())
					.write("id", authority.getId().toString())
					.write("name", authority.getName())
					.write("label", authority.getDisplayName())
					.write("isManager", isManager);

		if (resourceType == ResourceType.USER) {
			String avatar = authority.getString(ResourceProperties.AVATAR);
			if (StringUtils.isNotBlank(avatar)) {
				generator.write(THUMBNAIL_IMAGE, avatar);
			}
		}
	}

}
