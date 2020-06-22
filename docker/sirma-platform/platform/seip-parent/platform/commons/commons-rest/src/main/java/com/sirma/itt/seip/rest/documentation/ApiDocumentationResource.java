package com.sirma.itt.seip.rest.documentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;

import io.swagger.annotations.Api;
import io.swagger.config.FilterFactory;
import io.swagger.config.Scanner;
import io.swagger.config.ScannerFactory;
import io.swagger.config.SwaggerConfig;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.config.ReaderConfigUtils;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Swagger;

/**
 * Exposes the swagger generated api documentation.
 *
 * @author yasko
 * @author BBonev
 */
@Api(hidden = true)
@Path("/api-docs")
@ApplicationScoped
public class ApiDocumentationResource {
	private Swagger swagger;

	/**
	 * Provides documentation for a specific endpoint.
	 *
	 * @param uriInfo
	 *            request URI info
	 * @param headers
	 *            HTTP request headers
	 * @param servletConfig
	 *            the servlet configuration instance
	 * @param application
	 *            the JAX-RS application instance
	 * @param context
	 *            the servlet context instance
	 * @return The generated documentation for that endpoint.
	 */
	@GET
	@Path("/swagger.json")
	@PublicResource
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPathDocs(@Context UriInfo uriInfo, @Context HttpHeaders headers,
			@Context ServletConfig servletConfig, @Context Application application, @Context ServletContext context) {
		Swagger swaggerInstance = getSwagger(servletConfig, application, context);
		Swagger filteredSwagger = getFilteredSwagger(swaggerInstance, uriInfo, headers);
		return Response
				.ok(filteredSwagger, MediaType.APPLICATION_JSON_TYPE)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.header("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization")
					.build();
	}

	private synchronized Swagger getSwagger(ServletConfig servletConfig, Application application,
			ServletContext context) {
		if (swagger == null) {
			swagger = scan(servletConfig, application, context);
		}
		return swagger;
	}

	private static Swagger getFilteredSwagger(Swagger toFilter, UriInfo uriInfo, HttpHeaders headers) {
		SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
		if (filterImpl != null) {
			SpecFilter f = new SpecFilter();
			return f.filter(toFilter, filterImpl, getQueryParams(uriInfo.getQueryParameters()), getCookies(headers),
					getHeaders(headers));
		}
		return toFilter;
	}

	private static Swagger scan(ServletConfig servletConfig, Application application, ServletContext context) {
		Scanner scanner = ScannerFactory.getScanner();
		Swagger localSwagger = null;
		if (scanner != null) {
			SwaggerSerializers.setPrettyPrint(scanner.getPrettyPrint());
			localSwagger = (Swagger) context.getAttribute("swagger");

			Set<Class<?>> classes;
			if (scanner instanceof JaxrsScanner) {
				JaxrsScanner jaxrsScanner = (JaxrsScanner) scanner;
				classes = jaxrsScanner.classesFromContext(application, servletConfig);
			} else {
				classes = scanner.classes();
			}
			if (classes != null) {
				localSwagger = buildAndConfigureSwagger(scanner, localSwagger, classes, context);
				context.setAttribute("swagger", localSwagger);
			}
		}
		if (localSwagger == null) {
			throw new IllegalStateException("Could not create Swagger instance");
		}
		return fillDefaultInfo(localSwagger);
	}

	private static Swagger buildAndConfigureSwagger(Scanner scanner, Swagger sourceSwagger, Set<Class<?>> classes,
			ServletContext context) {
		Reader reader = new Reader(sourceSwagger, ReaderConfigUtils.getReaderConfig(context));
		Swagger localSwagger = reader.read(classes);
		if (scanner instanceof SwaggerConfig) {
			localSwagger = ((SwaggerConfig) scanner).configure(localSwagger);
		} else {
			SwaggerConfig configurator = (SwaggerConfig) context.getAttribute("reader");
			if (configurator != null) {
				configurator.configure(localSwagger);
			}
		}
		return localSwagger;
	}

	private static Map<String, List<String>> getHeaders(HttpHeaders headers) {
		Map<String, List<String>> output = new HashMap<>();
		if (headers != null) {
			output.putAll(headers.getRequestHeaders());
		}
		return output;
	}

	private static Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
		Map<String, List<String>> output = new HashMap<>();
		if (params != null) {
			output.putAll(params);
		}
		return output;
	}

	private static Map<String, String> getCookies(HttpHeaders headers) {
		Map<String, String> output = new HashMap<>();
		if (headers != null) {
			for (Map.Entry<String, Cookie> entry : headers.getCookies().entrySet()) {
				output.put(entry.getKey(), entry.getValue().getValue());
			}
		}
		return output;
	}

	private static Swagger fillDefaultInfo(Swagger swagger) {
		swagger.getInfo().setTitle("Sirma Enterprise Platform REST API");
		swagger.getInfo().setDescription("This is a REST API reference for the Sirma Enterprise Platform");
		swagger.getInfo().setVersion("2.x");
		return swagger;
	}
}
