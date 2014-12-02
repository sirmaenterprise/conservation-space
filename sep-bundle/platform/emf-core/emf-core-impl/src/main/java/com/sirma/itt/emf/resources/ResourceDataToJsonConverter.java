package com.sirma.itt.emf.resources;

import static com.sirma.itt.emf.util.JsonUtil.addToJson;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * The Class ResourceDataToJsonConverter registers and provides converters for emf resources and
 * roles.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class ResourceDataToJsonConverter implements TypeConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(EmfUser.class, JSONObject.class,
				new ResourceToJsonConverter<EmfUser>());
		converter.addConverter(EmfGroup.class, JSONObject.class,
				new ResourceToJsonConverter<EmfGroup>());
		converter.addConverter(EmfResource.class, JSONObject.class,
				new ResourceToJsonConverter<EmfResource>());
		converter.addConverter(ResourceRole.class, JSONObject.class,
				new ResourceRoleToJsonConverter());
		converter.addConverter(RoleIdentifier.class, JSONObject.class, new RoleToJsonConverter());
	}

	/**
	 * Converts a Resource to json object.
	 * 
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class ResourceToJsonConverter<T extends Resource> implements Converter<T, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(T source) {
			JSONObject object = new JSONObject();
			addToJson(object, "Id", source.getId());
			addToJson(object, "type", source.getType().getName());
			addToJson(object, "itemValue", source.getIdentifier());
			addToJson(object, "itemLabel",
					StringUtils.isNullOrEmpty(source.getDisplayName()) ? source.getIdentifier()
							: source.getDisplayName());
			addToJson(object, "Name",
					StringUtils.isNullOrEmpty(source.getDisplayName()) ? source.getIdentifier()
							: source.getDisplayName());
			if (source.getType() == ResourceType.USER) {
				addToJson(object, "iconPath", source.getProperties().get(ResourceProperties.AVATAR));
			} else {
				addToJson(object, "iconPath", "");
			}
			addToJson(object, "role", "");
			return object;
		}

	}

	/**
	 * Converts a RoleIdentifier to json object.
	 */
	public class RoleToJsonConverter implements Converter<RoleIdentifier, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(RoleIdentifier source) {
			JSONObject object = new JSONObject();
			addToJson(object, "label", source.getIdentifier());
			addToJson(object, "value", source.getIdentifier());

			return object;
		}
	}

	/**
	 * Converts a ResourceRole to json object.
	 */
	public class ResourceRoleToJsonConverter implements Converter<ResourceRole, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(ResourceRole source) {
			JSONObject object = new JSONObject();
			Resource resource = source.getResource();
			ResourceType resourceType = resource.getType();
			addToJson(object, "type", resourceType.getName());
			addToJson(object, "Id", resource.getId());
			addToJson(object, "itemValue", resource.getIdentifier());
			addToJson(object, "itemLabel", resource.getDisplayName());
			addToJson(object, "Name", resource.getDisplayName());
			if (resourceType == ResourceType.USER) {
				addToJson(object, "iconPath",
						resource.getProperties().get(ResourceProperties.AVATAR));
			}
			addToJson(object, "role", source.getRole());

			return object;
		}
	}
}
