package com.sirma.itt.idoc.converter;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Converter provider for CMF to add support for conversion between {@link DraftInstance} to
 * {@link JSONObject}
 *
 * @author bbanchev
 */
@ApplicationScoped
public class DraftInstanceToJSONObjectConverterProvider implements TypeConverterProvider {

	/**
	 * The DraftInstanceToJSONObjectConverter do the actual convert to json object.
	 *
	 * @author bbanchev
	 */
	public class DraftInstanceToJSONObjectConverter implements Converter<DraftInstance, JSONObject> {

		@Override
		public JSONObject convert(DraftInstance source) {

			JSONObject result = new JSONObject();
			// TODO date as ISO or ... ?
			JsonUtil.addToJson(result, DefaultProperties.CREATED_ON, source.getCreatedOn());
			JsonUtil.addToJson(result, DefaultProperties.CREATED_BY, source.getCreator());
			JsonUtil.addToJson(result, "properties", new JSONObject(source.getDraftProperties()));
			JsonUtil.addToJson(result, DocumentProperties.CONTENT, source.getDraftContent());
			return result;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(DraftInstance.class, JSONObject.class,
				new DraftInstanceToJSONObjectConverter());
	}

}
