package com.sirma.cmf.web.form.tasktree;

import static com.sirma.itt.emf.util.JsonUtil.addToJson;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Converter for standalone task instances to JSON.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class StandaloneTaskConverter implements TypeConverterProvider {

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(StandaloneTaskInstance.class, JSONObject.class,
				new StandaloneTaskToJsonConverter());
	}

	/**
	 * RoleToJsonConverter.
	 */
	public class StandaloneTaskToJsonConverter implements
			Converter<StandaloneTaskInstance, JSONObject> {

		@Override
		public JSONObject convert(StandaloneTaskInstance source) {
			JSONObject object = new JSONObject();
			Map<String, Serializable> properties = source.getProperties();
			String header = (String) properties.get(DefaultProperties.HEADER_DEFAULT);
			addToJson(object, DefaultProperties.ID, source.getId());
			addToJson(object, "header", header);
			return object;
		}
	}

}
