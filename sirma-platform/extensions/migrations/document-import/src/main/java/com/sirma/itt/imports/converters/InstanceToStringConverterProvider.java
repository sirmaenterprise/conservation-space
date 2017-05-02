package com.sirma.itt.imports.converters;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * The Class InstanceToStringConverterProvider.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class InstanceToStringConverterProvider implements TypeConverterProvider {

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(ObjectInstance.class, String.class, new ObjectToStringConverter());
	}

	/**
	 * The Class ObjectToStringConverter.
	 * 
	 * @author BBonev
	 */
	public class ObjectToStringConverter implements Converter<ObjectInstance, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String convert(ObjectInstance source) {
			JSONObject object = new JSONObject();
			InstanceReference reference = source.toReference();
			JsonUtil.addToJson(object, "instanceId", reference.getIdentifier());
			JsonUtil.addToJson(object, "instanceType", reference.getReferenceType().getName());
			return object.toString();
		}
	}

}
