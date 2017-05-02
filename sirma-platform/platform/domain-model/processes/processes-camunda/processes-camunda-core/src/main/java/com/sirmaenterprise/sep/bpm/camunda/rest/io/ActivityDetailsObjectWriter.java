package com.sirmaenterprise.sep.bpm.camunda.rest.io;

import static com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil.resolveInstance;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirmaenterprise.sep.bpm.camunda.service.ActivityDetails;

/**
 * Writer class for {@link ActivityDetails}.
 * 
 * @author bbanchev
 * @author simeon iliev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ActivityDetailsObjectWriter extends AbstractMessageBodyWriter<ActivityDetails> {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;
	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;
	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ActivityDetails.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ActivityDetails bpmData, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject();
			Instance process = resolveInstance(bpmData.getCamundaProcessBusinessId(), instanceTypeResolver);
			generator.write("active", bpmData.isActive());
			generator.write("process", bpmData.isProcess());
			instanceLoadDecorator.decorateInstance(process);
			instanceSerializer.serialize(process, generator, "process");
			generator.writeEnd().flush();
		}

	}

}
