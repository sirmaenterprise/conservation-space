package com.sirma.itt.emf.link.converters;

import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Base converter for {@link com.sirma.itt.emf.instance.model.Instance} to {@link InstanceReference}
 * conversions.
 *
 * @author BBonev
 */
public abstract class AbstractInstanceToInstanceReferenceConverterProvider implements
		TypeConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractInstanceToInstanceReferenceConverterProvider.class);
	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private InstanceDao<CommonInstance> instanceDao;

	/**
	 * String to link {@link InstanceReference} converter. The converter created new
	 * {@link InstanceReference} implementation and sets the type based on the given string
	 * argument.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class StringToLinkSourceConverter<T extends InstanceReference> implements
			Converter<String, T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T convert(String source) {
			if (source.startsWith("{")) {
				try {
					JSONObject jsonObject = new JSONObject(source);
					return (T) toReference(jsonObject);
				} catch (JSONException e) {
					LOGGER.warn("Failed to extract information for instance reference from {}",
							source, e);
				}
			}
			return (T) new LinkSourceId(null, getType(source));
		}
	}

	/**
	 * String to link {@link InstanceReference} converter. The converter created new
	 * {@link InstanceReference} implementation and sets the type based on the given string
	 * argument.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class JsonObjectToLinkSourceConverter<T extends InstanceReference> implements
			Converter<JSONObject, T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T convert(JSONObject source) {
			return (T) toReference(source);
		}
	}

	/**
	 * Converter class for any {@link Instance} class to {@link LinkSourceId}.
	 *
	 * @param <E>
	 *            the element type
	 * @param <I>
	 *            the generic type
	 * @author BBonev
	 */
	public class EntityInstanceToLinkSourceConverter<E extends Instance, I extends InstanceReference>
			implements Converter<E, I> {

		/** The target class. */
		private Class<E> targetClass;

		/**
		 * Instantiates a new entity instance to link source converter.
		 *
		 * @param targetClass
		 *            the target class
		 */
		public EntityInstanceToLinkSourceConverter(Class<E> targetClass) {
			this.targetClass = targetClass;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public I convert(E source) {
			return (I) new LinkSourceId(String.valueOf(source.getId()), getType(targetClass),
					source);
		}
	}

	/**
	 * Common instance to String converter.
	 * 
	 * @author BBonev
	 * @param <I>
	 *            the instance type
	 */
	public class InstanceToStringConverter<I extends Instance> implements Converter<I, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String convert(I source) {
			if (source instanceof CommonInstance) {
				instanceDao.persistChanges((CommonInstance) source);
			}
			InstanceReference reference = source.toReference();
			if (reference != null) {
				// or use the type converter to transform the reference to jsonObject
				// this is just faster
				return JsonUtil.toJsonObject(reference).toString();
			}
			return null;
		}
	}

	/**
	 * Builds the reference.
	 *
	 * @param source
	 *            the source
	 * @return the instance reference
	 */
	protected InstanceReference toReference(JSONObject source) {
		String id = JsonUtil.getStringValue(source, "instanceId");
		String type = JsonUtil.getStringValue(source, "instanceType");
		return new LinkSourceId(id, getType(type));
	}

	/**
	 * Gets the data type based on the given class.
	 *
	 * @param name
	 *            the source class
	 * @return the data type
	 */
	protected DataTypeDefinition getType(String name) {
		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(name);
		if (typeDefinition == null) {
			throw new TypeConversionException("The given source type " + name
					+ " is not supported!");
		}
		return typeDefinition;
	}

	/**
	 * Gets the data type based on the given class.
	 *
	 * @param clazz
	 *            the source class
	 * @return the data type
	 */
	protected DataTypeDefinition getType(Class<?> clazz) {
		DataTypeDefinition typeDefinition = dictionaryService
				.getDataTypeDefinition(clazz.getName());
		if (typeDefinition == null) {
			throw new TypeConversionException("The given source class " + clazz.getName()
					+ " is not supported!");
		}
		return typeDefinition;
	}

	/**
	 * Adds the entity converter.<br>
	 * Note the method also registers a string to given instance converter using the method
	 *
	 * @param <F>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param converter
	 *            the converter
	 * @param from
	 *            the from
	 * @param to
	 *            the to {@link #addStringToEntityConverter(TypeConverter, Class)}.
	 */
	protected <F extends Instance, T extends InstanceReference> void addEntityConverter(
			TypeConverter converter, Class<F> from, Class<T> to) {
		converter.addConverter(from, to, new EntityInstanceToLinkSourceConverter<F, T>(from));
		// register direct string conversion
		addStringToEntityConverter(converter, to);
		addInstanceToStringConverter(converter, from);
	}

	/**
	 * Adds the string to entity converter.
	 *
	 * @param <T>
	 *            the generic type
	 * @param converter
	 *            the converter
	 * @param to
	 *            the to
	 */
	protected <T extends InstanceReference> void addStringToEntityConverter(
			TypeConverter converter, Class<T> to) {
		converter.addConverter(String.class, to, new StringToLinkSourceConverter<T>());
		converter.addConverter(JSONObject.class, to, new JsonObjectToLinkSourceConverter<T>());
	}

	/**
	 * Adds the string to entity converter.
	 *
	 * @param <T>
	 *            the generic type
	 * @param converter
	 *            the converter
	 * @param from
	 *            the from
	 */
	protected <T extends Instance> void addInstanceToStringConverter(TypeConverter converter,
			Class<T> from) {
		converter.addConverter(from, String.class, new InstanceToStringConverter<T>());
	}

}
