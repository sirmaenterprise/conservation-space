package com.sirma.cmf.web.rest;

import java.util.regex.Pattern;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.HeadersService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.application.EmfApplication;
import com.sirma.itt.emf.web.resources.WebResourceUtil;

/**
 * This class represent REST controller that will handle tooltip management.
 * 
 * @author cdimitrov
 */
@Path("/tooltip")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TooltipsRestService extends EmfRestService {

	/** The Constant HEADER_TYPE. */
	private static final String HEADER_TYPE = "headerType";
	/** The Constant THUMBNAIL_SOURCE. */
	private static final String THUMBNAIL_SOURCE = "#thumbnailSource#";
	/** The Constant THUMBNAIL_PLACEHOLDER. */
	private static final Pattern THUMBNAIL_PLACEHOLDER = Pattern.compile(THUMBNAIL_SOURCE,
			Pattern.LITERAL);

	/** The rendition service. */
	@Inject
	private RenditionService renditionService;
	/** The emf application. */
	@Inject
	private EmfApplication emfApplication;
	@Inject
	private HeadersService headersService;

	/**
	 * This method will retrieve instance based on received type and id and will send the tooltip
	 * data from requested header type.
	 * 
	 * @param instanceId
	 *            the current instance id
	 * @param instanceType
	 *            the current instance type
	 * @param headerType
	 *            the headerType
	 * @return tooltip for the instance
	 */
	@Path("/")
	@GET
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Response getTooltip(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType,
			@QueryParam(HEADER_TYPE) String headerType) {
		// extract instance based on id and type
		Instance instance = fetchInstance(instanceId, instanceType);
		if (instance == null) {
			log.warn("Cannot extract instance with id={} and type={}", instanceId, instanceType);
			return buildResponse(Status.BAD_REQUEST, null);
		}
		// default header used as tooltip is a tooltip header
		String calculatedHeaderType = DefaultProperties.HEADER_TOOLTIP;
		// if header type is provided from the request
		if (StringUtils.isNotNullOrEmpty(headerType)
				&& DefaultProperties.DEFAULT_HEADERS.contains(headerType)) {
			calculatedHeaderType = headerType;
		}
		boolean isTooltipHeader = DefaultProperties.HEADER_TOOLTIP.equals(calculatedHeaderType);

		// generate the header for the instance
		String header = headersService.generateInstanceHeader(instance, calculatedHeaderType);
		// if no tooltip header is found
		if (header == null) {
			log.debug(
					"Not found tooltip header for instance[{}] with id[{}]! Default header will be used instead.",
					instance.getClass().getSimpleName(), instance.getId());
			header = (String) instance.getProperties().get(DefaultProperties.HEADER_DEFAULT);
			isTooltipHeader = false;
		}
		if (header == null) {
			return buildResponse(Status.OK, "");
		}
		// if the tooltip header is found, we try to load the thumbnail
		if (isTooltipHeader) {
			header = addThumbnail(instance, header);
		}

		JSONObject response = new JSONObject();
		JsonUtil.addToJson(response, "tooltip", header.toString());
		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * Adds the thumbnail image if the thumbnail source placeholder is found in the header
	 * definition.
	 * 
	 * @param instance
	 *            the instance
	 * @param header
	 *            the header
	 * @return the string
	 */
	private String addThumbnail(Instance instance, String header) {
		boolean hasThumbnailPlaceholder = header.contains(THUMBNAIL_SOURCE);
		if (!hasThumbnailPlaceholder) {
			return header;
		}
		String thumbnail = renditionService.getDefaultThumbnail(instance);
		if (StringUtils.isNullOrEmpty(thumbnail)) {
			String iconName = instance.getClass().getSimpleName().toLowerCase() + "-icon-64.png";
			thumbnail = WebResourceUtil.getIconUrl(emfApplication.getContextPath(), iconName);
		}
		return THUMBNAIL_PLACEHOLDER.matcher(header).replaceFirst(thumbnail);
	}
}
