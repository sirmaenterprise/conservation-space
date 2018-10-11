package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.json.JsonUtil.addToJson;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;

/**
 * The Class ResourceDataToJsonConverter registers and provides converters for emf resources and roles.
 *
 * @author svelikov
 */
@ApplicationScoped
public class ResourceDataToJsonConverter implements TypeConverterProvider {

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(EmfUser.class, JSONObject.class, new ResourceToJsonConverter<EmfUser>());
		converter.addConverter(EmfGroup.class, JSONObject.class, new ResourceToJsonConverter<EmfGroup>());
		converter.addConverter(EmfResource.class, JSONObject.class, new ResourceToJsonConverter<EmfResource>());
	}

	/**
	 * Converts a Resource to json object.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public static class ResourceToJsonConverter<T extends Resource> implements Converter<T, JSONObject> {

		@Override
		public JSONObject convert(T source) {
			JSONObject object = new JSONObject();
			addToJson(object, "Id", source.getId());
			addToJson(object, "type", source.getType().getName());
			addToJson(object, "itemValue", source.getName());
			addToJson(object, "itemLabel",
					StringUtils.isBlank(source.getDisplayName()) ? source.getName() : source.getDisplayName());
			addToJson(object, "Name",
					StringUtils.isBlank(source.getDisplayName()) ? source.getName() : source.getDisplayName());
			if (source.getType() == ResourceType.USER) {
				addToJson(object, "iconPath", source.getProperties().get(ResourceProperties.AVATAR));
			} else {
				addToJson(object, "iconPath", "");
			}
			addToJson(object, "role", "");
			return object;
		}

	}

}
