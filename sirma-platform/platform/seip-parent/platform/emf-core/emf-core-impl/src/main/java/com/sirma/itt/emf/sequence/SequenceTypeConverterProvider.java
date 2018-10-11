package com.sirma.itt.emf.sequence;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.emf.sequence.entity.SequenceEntity;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;

/**
 * Adds type converters to handle sequence objects
 *
 * @author BBonev
 */
@ApplicationScoped
public class SequenceTypeConverterProvider implements TypeConverterProvider {

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(JSONObject.class, Sequence.class, source -> {
			SequenceEntity entity = new SequenceEntity();
			entity.fromJSONObject(source);
			return entity;
		});
	}

}
