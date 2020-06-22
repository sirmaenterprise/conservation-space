package com.sirma.itt.seip.instance.lock;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.json.JSON.addIfNotNull;

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
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.time.ISO8601DateFormat;

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
		converter.addConverter(LockInfo.class, String.class, this::lockInfoToString);
		converter.addConverter(LockInfo.class, JsonValue.class, this::lockInfoToJsonObject);
	}

	private JsonObject lockInfoToJsonObject(LockInfo info) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("isLocked", info.isLocked());
		builder.add("isLockedByMe", info.isLockedByMe());
		addIfNotNull(builder, "lockInfo", info.getLockInfo());
		addIfNotNull(builder, "lockOn", ISO8601DateFormat.format(info.getLockedOn()));
		addLockedByIfAvailable(info, builder);
		return builder.build();
	}

	private void addLockedByIfAvailable(LockInfo info, JsonObjectBuilder builder) {
		Resource user = resourceService.findResource(info.getLockedBy());
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
	}

	private String lockInfoToString(LockInfo info) {
		try (StringWriter writer = new StringWriter(4096); JsonGenerator generator = Json.createGenerator(writer)) {
			generator.writeStartObject();
			generator.write("isLocked", info.isLocked());
			generator.write("isLockedByMe", info.isLockedByMe());
			addIfNotNull(generator, "lockInfo", info.getLockInfo());
			addIfNotNull(generator, "lockOn", ISO8601DateFormat.format(info.getLockedOn()));
			addLockedByIfAvailable(generator, info);
			generator.writeEnd().flush();
			return writer.toString();
		} catch (IOException e) {
			LOGGER.warn("Failed to serialize lock data for {}", info.getLockedInstance().getId(), e);
		}
		return "{}";
	}

	private void addLockedByIfAvailable(JsonGenerator generator, LockInfo info) {
		Resource user = resourceService.findResource(info.getLockedBy());
		if (user != null) {
			instanceSerializer.serialize(LOCKED_BY, user, LOCKEDBY_USER_INFO, generator);
		}
	}

}
