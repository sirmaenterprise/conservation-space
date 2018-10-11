package com.sirma.itt.seip.json;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;

/**
 * Type converter provider for generic JSON transformations.
 *
 * @author BBonev
 */
@ApplicationScoped
public class JsonConverterProvider implements TypeConverterProvider {

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(JsonRepresentable.class, JSONObject.class, source -> source.toJSONObject());
	}

}
