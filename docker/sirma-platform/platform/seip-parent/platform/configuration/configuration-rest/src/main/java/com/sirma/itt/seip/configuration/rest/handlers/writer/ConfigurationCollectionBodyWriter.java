package com.sirma.itt.seip.configuration.rest.handlers.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Converts a {@link Collection} of {@link Configuration} to a {@link javax.json.JsonArray}.
 * <p>
 * The conversion supports {@link Number}, {@link Collection} and {@link String} values. If the type is not recognized,
 * it will use toString() before constructing the JSON object.
 *
 * @author Mihail Radkov
 */
@Provider
public class ConfigurationCollectionBodyWriter extends AbstractMessageBodyWriter<Collection<Configuration>> {

	private static final String VALUE = "value";
	private static final String KEY = "key";
	private static final String RAW_VALUE = "rawValue";
	private static final String DEFAULT_VALUE = "defaultValue";
	private static final String TENANT_ID = "tenantId";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String JAVA_TYPE = "javaType";
	private static final String SHARED = "shared";
	private static final String SYSTEM = "system";
	private static final String ALIAS = "alias";
	private static final String SUB_SYSTEM = "subSystem";
	private static final String COMPLEX = "complex";
	private static final String SENSITIVE = "sensitive";
	private static final String PASSWORD = "password"; //NOSONAR
	private static final String DEPENDS_ON = "dependsOn";

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(type, genericType);
		boolean isAssignable = collectionType != null && Configuration.class.isAssignableFrom(collectionType);
		return Collection.class.isAssignableFrom(type) && isAssignable;
	}

	@Override
	public void writeTo(Collection<Configuration> configurations, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartArray();
			for (Configuration configuration : configurations) {
				toJson(configuration, generator);
			}
			generator.writeEnd();
		}
	}

	private static void toJson(Configuration configuration, JsonGenerator generator) {
		generator.writeStartObject();

		writeConfigurationData(configuration, generator);

		ConfigurationInstance definition = configuration.getDefinition();
		if (definition != null) {
			writeConfigurationInstanceData(definition, generator);
		}

		generator.writeEnd();
	}

	private static void writeConfigurationData(Configuration configuration, JsonGenerator generator) {
		JSON.addIfNotNull(generator, KEY, configuration.getConfigurationKey());
		if (configuration.getValue() != null) {
			writeValue(configuration.getValue(), generator);
		}
		JSON.addIfNotNull(generator, RAW_VALUE, configuration.getRawValue());
		if (configuration.getDefaultValue() != null) {
			generator.write(DEFAULT_VALUE, configuration.getDefaultValue().toString());
		}
		JSON.addIfNotNull(generator, TENANT_ID, configuration.getTenantId());
	}

	private static void writeValue(Object value, JsonGenerator generator) {
		if (value instanceof Number) {
			writeNumericValue((Number) value, generator);
		} else if (value instanceof Collection) {
			writeCollectionValue((Collection<?>) value, generator);
		} else if (value instanceof Boolean) {
			generator.write(VALUE, ((Boolean) value).booleanValue());
		} else {
			generator.write(VALUE, value.toString());
		}
	}

	private static void writeNumericValue(Number value, JsonGenerator generator) {
		if (value instanceof Integer) {
			generator.write(VALUE, value.intValue());
		} else if (value instanceof Long) {
			generator.write(VALUE, value.longValue());
		} else if (value instanceof Double) {
			generator.write(VALUE, value.doubleValue());
		} else {
			// Write as string if the value type is not recognized
			// TODO: Throw or log ?
			generator.write(VALUE, value.toString());
		}
	}

	private static void writeCollectionValue(Collection<?> values, JsonGenerator generator) {
		generator.writeStartArray(VALUE);
		if (!values.isEmpty()) {
			values.stream().filter(Objects::nonNull).forEach(value -> generator.write(value.toString()));
		}
		generator.writeEnd();
	}

	private static void writeConfigurationInstanceData(ConfigurationInstance configurationInstance,
			JsonGenerator generator) {
		JSON.addIfNotNull(generator, LABEL, configurationInstance.getLabel());
		JSON.addIfNotNull(generator, JAVA_TYPE, configurationInstance.getType().getName());
		JSON.addIfNotNull(generator, ALIAS, configurationInstance.getAlias());
		JSON.addIfNotNull(generator, SUB_SYSTEM, configurationInstance.getSubSystem());
		JSON.addIfNotNull(generator, NAME, configurationInstance.getName());
		generator.write(SHARED, configurationInstance.isSharedConfiguration());
		generator.write(SYSTEM, configurationInstance.isSystemConfiguration());
		generator.write(COMPLEX, configurationInstance.isComplex());
		generator.write(SENSITIVE, configurationInstance.isSensitive());
		generator.write(PASSWORD, configurationInstance.isPassword());

		Annotation annotation = configurationInstance.getAnnotation();
		if (annotation instanceof ConfigurationGroupDefinition) {
			writeDependentProperties((ConfigurationGroupDefinition) annotation, generator);
		}
	}

	private static void writeDependentProperties(ConfigurationGroupDefinition groupDefinition,
			JsonGenerator generator) {
		String[] properties = groupDefinition.properties();
		if (properties.length > 0) {
			generator.writeStartArray(DEPENDS_ON);
			for (String property : properties) {
				generator.write(property);
			}
			generator.writeEnd();
		}
	}
}
