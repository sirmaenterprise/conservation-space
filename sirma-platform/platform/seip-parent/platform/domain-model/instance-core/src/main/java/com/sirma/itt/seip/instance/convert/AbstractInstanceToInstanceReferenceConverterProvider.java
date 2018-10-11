package com.sirma.itt.seip.instance.convert;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Base converter for {@link com.sirma.itt.seip.domain.instance.Instance} to {@link InstanceReference} conversions.
 *
 * @author BBonev
 */
public abstract class AbstractInstanceToInstanceReferenceConverterProvider implements TypeConverterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected DefinitionService definitionService;

	@Inject
	@com.sirma.itt.seip.instance.dao.InstanceType(type = ObjectTypes.INSTANCE)
	protected InstanceDao instanceDao;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	protected InstanceTypes instanceTypes;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * String to link {@link InstanceReference} converter. The converter created new {@link InstanceReference}
	 * implementation and sets the type based on the given string argument.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class StringToLinkSourceConverter<T extends InstanceReference> implements Converter<String, T> {

		@Override
		@SuppressWarnings("unchecked")
		public T convert(String source) {
			if (source.startsWith("{")) {
				try {
					JSONObject jsonObject = new JSONObject(source);
					return (T) toReference(jsonObject);
				} catch (JSONException e) {
					LOGGER.warn("Failed to extract information for instance reference from {}", source, e);
				}
			}
			return (T) new LinkSourceId(null, getType(source));
		}
	}

	/**
	 * String to link {@link InstanceReference} converter. The converter created new {@link InstanceReference}
	 * implementation and sets the type based on the given string argument.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class JsonObjectToLinkSourceConverter<T extends InstanceReference> implements Converter<JSONObject, T> {

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

		@Override
		@SuppressWarnings("unchecked")
		public I convert(E source) {
			return (I) new LinkSourceId(Objects.toString(source.getId(), null), getType(targetClass), source.type(),
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
		if (type == null && id != null) {
			return instanceTypeResolver.resolveReference(id).orElse(null);
		}
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
		DataTypeDefinition typeDefinition = definitionService.getDataTypeDefinition(name);
		if (typeDefinition == null && name.contains(":")) {
			// if semantic class, we need the full URI otherwise will not find the class
			String fullUri = namespaceRegistryService.buildFullUri(name);
			typeDefinition = definitionService.getDataTypeDefinition(fullUri);
		}

		if (typeDefinition == null) {
			throw new TypeConversionException("The given source type [" + name + "] is not supported!");
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
		DataTypeDefinition typeDefinition = definitionService.getDataTypeDefinition(clazz.getName());
		if (typeDefinition == null) {
			throw new TypeConversionException("The given source class " + clazz.getName() + " is not supported!");
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
	protected <F extends Instance, T extends InstanceReference> void addEntityConverter(TypeConverter converter,
			Class<F> from, Class<T> to) {
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
	protected <T extends InstanceReference> void addStringToEntityConverter(TypeConverter converter, Class<T> to) {
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
	protected <T extends Instance> void addInstanceToStringConverter(TypeConverter converter, Class<T> from) {
		converter.addConverter(from, String.class, new InstanceToStringConverter<T>());
	}
}
