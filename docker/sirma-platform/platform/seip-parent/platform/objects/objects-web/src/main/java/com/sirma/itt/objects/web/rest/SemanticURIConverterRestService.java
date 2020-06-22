package com.sirma.itt.objects.web.rest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Used to convert between short and full semantic {@link IRI}s and vice versa.
 *
 * @author iborisov
 */
// TODO: used in UI2 - move
@Path("/semantic/uri/conversion")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class SemanticURIConverterRestService {
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Converts a list of full {@link IRI}s to short {@link IRI}s
	 * @param uris {@link List} with full {@link IRI}s
	 * @return a {@link Map} with input full {@link IRI}s as keys and converted short {@link IRI}s as values
	 */
	@POST
	@Path("/to-short")
	public Map<String, String> convertToShortURIs(List<String> uris) {
		return uris.stream().collect(Collectors.toMap(Function.identity(), uri -> fullToShortURI(uri)));
	}

	/**
	 * Converts a list of short {@link IRI}s to full {@link IRI}s
	 * @param uris {@link List} with short {@link IRI}s
	 * @return a {@link Map} with input short {@link IRI}s as keys and converted full {@link IRI}s as values
	 */
	@POST
	@Path("/to-full")
	public Map<String, String> convertToFullURIs(List<String> uris) {
		return uris.stream().collect(Collectors.toMap(Function.identity(), uri -> shortToFullURI(uri)));
	}

	private String fullToShortURI(String fullURI) {
		IRI uri = typeConverter.convert(IRI.class, fullURI);
		return typeConverter.convert(String.class, uri);
	}

	private String shortToFullURI(String shortURI) {
		IRI fullURI = typeConverter.convert(IRI.class, shortURI);
		return fullURI.stringValue();
	}
}
