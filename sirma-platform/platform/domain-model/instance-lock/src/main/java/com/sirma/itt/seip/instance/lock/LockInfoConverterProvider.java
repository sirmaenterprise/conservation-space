package com.sirma.itt.seip.instance.lock;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.rest.utils.JSON.addIfNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Converter provider for {@link Locking} generic conversions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class LockInfoConverterProvider implements TypeConverterProvider {
	private static final PropertiesFilterBuilder LOCKEDBY_USER_INFO = InstanceToJsonSerializer
			.onlyProperties(Arrays.asList(HEADER_COMPACT, HEADER_DEFAULT, THUMBNAIL_IMAGE, ResourceProperties.USER_ID));
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private ResourceService resourceService;

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Override
	public void register(TypeConverter converter) {
		ResourceService resources = resourceService;
		InstanceToJsonSerializer serializer = instanceSerializer;
		converter.addConverter(LockInfo.class, String.class,
				info -> lockInfoToString(info, converter, resources, serializer));
		converter.addConverter(LockInfo.class, JsonValue.class,
				info -> lockInfoToJsonObject(info, converter, resources, serializer));
	}

	private static String lockInfoToString(LockInfo info, TypeConverter typeConverter, ResourceService resources,
			InstanceToJsonSerializer instanceSerializer) {
		return lockInfoToJsonObject(info, typeConverter, resources, instanceSerializer).toString();
	}

	private static JsonObject lockInfoToJsonObject(LockInfo info, TypeConverter typeConverter,
			ResourceService resources, InstanceToJsonSerializer instanceSerializer) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("isLocked", info.isLocked());
		builder.add("isLockedByMe", info.isLockedByMe());
		addIfNotNull(builder, "lockInfo", info.getLockInfo());
		addIfNotNull(builder, "lockOn", typeConverter.convert(String.class, info.getLockedOn()));
		Resource user = resources.findResource(info.getLockedBy());
		if (user != null) {
			try (StringWriter writer = new StringWriter(4096); JsonGenerator generator = Json.createGenerator(writer)) {
				instanceSerializer.serialize(user, LOCKEDBY_USER_INFO, generator);
				generator.flush();
				writer.flush();
				builder.add("lockedBy", JSON.read(new StringReader(writer.toString()), JsonObject.class::cast));
			} catch (IOException e) {
				LOGGER.warn("Failed to serialize user data for {}", user, e);
			}
		}
		return builder.build();
	}
}
