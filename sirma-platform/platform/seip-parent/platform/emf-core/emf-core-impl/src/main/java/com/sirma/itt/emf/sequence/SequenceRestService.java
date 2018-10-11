package com.sirma.itt.emf.sequence;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.rest.RestUtil;

/**
 * Rest service to provide access to sequence service.
 *
 * @author BBonev
 */
@Transactional
@Path("/sequences")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SequenceRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceRestService.class);

	@Inject
	private SequenceGeneratorService sequenceService;

	@Inject
	private TypeConverter typeConverter;

	/**
	 * List all sequences
	 *
	 * @return the response
	 */
	@GET
	public Response listAll() {
		Collection<Sequence> all = sequenceService.listAll();
		Collection<JSONObject> converted = typeConverter.convert(JSONObject.class, all);
		return buildResponse(converted);
	}

	/**
	 * Gets information for a single sequence.
	 *
	 * @param sequenceName
	 *            the sequence name
	 * @return the response
	 */
	@GET
	@Path("{sequenceName}")
	public Response get(@PathParam("sequenceName") String sequenceName) {
		Sequence sequence = sequenceService.getSequence(sequenceName);

		if (sequence == null) {
			return RestUtil.buildErrorResponse(Status.NOT_FOUND,
					"Sequence name [" + sequenceName + "] was not found in the system.");
		}

		return buildResponse(Collections.singletonList(typeConverter.convert(JSONObject.class, sequence)));
	}

	/**
	 * Initialize sequence if the sequence does not exists. If the sequence exists the method does nothing.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response initializeSequence(String data) {
		if (StringUtils.isBlank(data)) {
			return RestUtil.buildErrorResponse(Status.BAD_REQUEST, "Invalid request data is required!");
		}
		Object dataRequest = RestUtil.readDataRequest(data);

		List<Sequence> sequences = new LinkedList<>();
		if (dataRequest instanceof JSONArray) {
			readArrayRequestForSequenceInitialization(dataRequest, sequences);
		} else if (dataRequest instanceof JSONObject) {
			readObjectRequestForSequenceInitialization(dataRequest, sequences);
		} else {
			return RestUtil.buildErrorResponse(Status.BAD_REQUEST, "Invalid request format");
		}

		return buildResponse(typeConverter.convert(JSONObject.class, sequences));
	}

	/**
	 * Read object request for sequence initialization.
	 *
	 * @param dataRequest
	 *            the data request
	 * @param sequences
	 *            the sequences
	 */
	private void readObjectRequestForSequenceInitialization(Object dataRequest, List<Sequence> sequences) {
		Sequence sequence = initializeSequenceFromJson((JSONObject) dataRequest);
		if (sequence != null) {
			sequences.add(sequence);
		} else {
			LOGGER.warn("Invalid requst data for sequence creation: {}", dataRequest);
		}
	}

	/**
	 * Read array request for sequence initialization.
	 *
	 * @param dataRequest
	 *            the data request
	 * @param sequences
	 *            the sequences
	 */
	private void readArrayRequestForSequenceInitialization(Object dataRequest, List<Sequence> sequences) {
		JSONArray array = (JSONArray) dataRequest;
		for (int i = 0; i < array.length(); i++) {
			JSONObject element = array.optJSONObject(i);
			Sequence sequence = initializeSequenceFromJson(element);
			if (sequence != null) {
				sequences.add(sequence);
			} else {
				LOGGER.warn("Invalid requst data for sequence creation: {}", element);
			}
		}
	}

	/**
	 * Initialize sequence from json.
	 *
	 * @param jsonObject
	 *            the json object
	 * @return the sequence
	 */
	private Sequence initializeSequenceFromJson(JSONObject jsonObject) {
		Sequence sequence = typeConverter.convert(Sequence.class, jsonObject);
		if (sequence == null || sequence.getIdentifier() == null || sequence.getValue() == null) {
			return null;
		}

		// does not allow to reset an existing request
		Long currentId = sequenceService.getCurrentId(sequence.getIdentifier());
		if (currentId.compareTo(0L) == 0) {
			sequenceService.resetSequenceTo(sequence.getIdentifier(), sequence.getValue());
		}
		return sequenceService.getSequence(sequence.getIdentifier());
	}

	/**
	 * Gets the next value of sequence identified by name
	 *
	 * @param sequenceName
	 *            the sequence name
	 * @return the next sequence
	 */
	@POST
	@Path("{sequenceName}/next")
	public Response getNextSequence(@PathParam("sequenceName") String sequenceName) {
		// increase the sequence and store the changes
		return buildResponse(Collections.singletonList(
				typeConverter.convert(JSONObject.class, sequenceService.incrementSequence(sequenceName))));
	}

	/**
	 * Builds the response.
	 *
	 * @param converted
	 *            the converted
	 * @return the response
	 */
	private static Response buildResponse(Collection<JSONObject> converted) {
		return RestUtil.buildDataResponse(converted);
	}
}
