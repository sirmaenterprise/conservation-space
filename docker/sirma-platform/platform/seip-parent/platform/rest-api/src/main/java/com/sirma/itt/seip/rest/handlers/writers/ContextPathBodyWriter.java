package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.rest.utils.JSON.addIfNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.utils.JsonKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.resources.instances.ContextPath;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Rest writer for {@link ContextPath}. The output is an array of instance id, type and header in the format:
 * <p>
 * <code><pre>[{
 *   "id": "emf:instance-parent",
 *   "type": "caseinstance"
 *   "compactHeader": "instance header",
 *   "readAllowed": true,
 *   "writeAllowed": true
 * },
 * {
 *   "id": "emf:instance",
 *   "type": "documentinstance"
 *   "compactHeader": "instance header",
 *   "readAllowed": true,
 *   "writeAllowed": false
 * }]</pre></code>
 *
 * @author BBonev
 */
@Provider
public class ContextPathBodyWriter extends AbstractMessageBodyWriter<ContextPath> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ContextPath.class.equals(type);
	}

	@Override
	public void writeTo(ContextPath path, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartArray();
			for (Instance pathInstance : path) {
				generator.writeStartObject();
				generator.write("id", (String) pathInstance.getId());
				if (!JSON.addIfNotNull(generator, "type", pathInstance.type().getCategory(), "objectinstance")) {
					LOGGER.warn("Found type {} without category for instance {}", pathInstance.type().getId(),
							pathInstance.getId());
				}
				addIfNotNull(generator, "compactHeader", pathInstance.getString(HEADER_BREADCRUMB));
				generator.write(JsonKeys.READ_ALLOWED, pathInstance.isReadAllowed());
				generator.write(JsonKeys.WRITE_ALLOWED, pathInstance.isWriteAllowed());
				generator.writeEnd();
			}
			generator.writeEnd();
			generator.flush();
		}
	}

}
