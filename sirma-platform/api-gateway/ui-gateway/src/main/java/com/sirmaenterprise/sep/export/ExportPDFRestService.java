package com.sirmaenterprise.sep.export;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Web service to route requests to export server.
 *
 * @author iborisov
 *
 */
@Path("/export")
@ApplicationScoped
@Produces(Versions.V2_JSON)
public class ExportPDFRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "export.server.url", system = true, sensitive = true, shared = false, label = "Specifies the full url to the export server.")
	private ConfigurationProperty<String> exportServerURL;

	/**
	 * Route export pdf request to export server.
	 *
	 * @param info
	 *            request information. Headers, parameters, etc.
	 * @param content
	 *            export pdf request content. JSON object with URL to be exported and name for the exported file
	 * @return response with status 200 and generated PDF as payload or server error response (500)
	 */
	@POST
	@Path("/pdf")
	@Consumes(Versions.V2_JSON)
	public Response exportPDF(@BeanParam RequestInfo info, String content) {
		if (exportServerURL.isNotSet()) {
			LOGGER.error("{} not configured!", exportServerURL.getName());
			return Response.serverError().build();
		}

		String exportPDFURL = exportServerURL.get();
		if (exportPDFURL.endsWith("/")) {
			exportPDFURL = exportPDFURL.substring(0, exportPDFURL.length() - 1);
		}
		exportPDFURL += "/export/pdf";

		PostMethod post = new PostMethod(exportPDFURL);

		copyRequestHeaders(info.getHeaders(), post);

		try {
			post.setRequestEntity(new StringRequestEntity(content, null, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Failed to set export request body content", e);
		}

		try {
			HttpClient client = createHttpClient();
			int returnCode = client.executeMethod(post);
			if (returnCode == HttpStatus.SC_OK) {
				ResponseBuilder responseBuilder = Response.ok(post.getResponseBody(),
						MediaType.APPLICATION_OCTET_STREAM);
				Header[] responseHeaders = post.getResponseHeaders();
				copyResponseHeaders(responseHeaders, responseBuilder);
				return responseBuilder.build();
			}
			return Response.status(returnCode).entity(post.getResponseBody()).build();
		} catch (IOException e) {
			LOGGER.error("Error performing export request.", e);
		}
		return Response.serverError().build();
	}

	protected HttpClient createHttpClient() {
		return new HttpClient();
	}

	private static void copyRequestHeaders(HttpHeaders requestHeaders, PostMethod postMethod) {
		requestHeaders.getRequestHeaders()
				.forEach((key, value) -> value.forEach(headerValue -> postMethod.addRequestHeader(key, headerValue)));
	}

	private static void copyResponseHeaders(Header[] responseHeaders, ResponseBuilder responseBuilder) {
		for (Header header : responseHeaders) {
			responseBuilder.header(header.getName(), header.getValue());
		}
	}
}
