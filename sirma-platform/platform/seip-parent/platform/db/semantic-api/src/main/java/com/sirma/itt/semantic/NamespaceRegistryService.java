package com.sirma.itt.semantic;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.Uri;

/**
 * <p>
 * Namespace registry service for holding/caching all defined namespaces in the underlying semantic repository and
 * building property full IRI string-representation according to its short definition.
 * </p>
 * <p>
 * The built string-representation of a full IRI could be used for creating RDF {@link org.eclipse.rdf4j.model.Literal}s by
 * using a {@link org.eclipse.rdf4j.model.ValueFactory}
 * </p>
 *
 * @see {@link org.eclipse.rdf4j.model.IRI}
 * @see {@link org.eclipse.rdf4j.model.Literal}
 * @see {@link org.eclipse.rdf4j.model.ValueFactory}
 * @author Valeri Tishev
 */
public interface NamespaceRegistryService {

	/**
	 * <p>
	 * Character delimiting the namespace prefix and the property name in a short IRI definition
	 * </p>
	 */
	String SHORT_URI_DELIMITER = ":";

	/**
	 * <p>
	 * Character delimiting the namespace and the property name in a full IRI definition
	 * </p>
	 */
	String FULL_URI_DELITIMER = "#";

	/**
	 * <p>
	 * Gets the namespace identified by given prefix
	 * </p>
	 *
	 * @param prefix
	 *            the prefix
	 * @return the namespace or <code>null</code> in case namespace with given prefix is not registered
	 */
	String getNamespace(String prefix);

	/**
	 * <p>
	 * Builds the property full IRI string-representation according to its short definition. The property <b>short
	 * IRI</b> is represented by a string in the following format {@code prefix:property}
	 * </p>
	 * <p>
	 * For example, if the {@code PREFIX dcterms:<http://purl.org/dc/terms/>} is defined in the semantic repository,
	 * invocation of the method with parameter {@code dcterms:title} should produce the following string
	 * {@code http://purl.org/dc/terms/title}
	 * </p>
	 *
	 * @param shortUri
	 *            the short IRI
	 * @return the property full IRI string-representation
	 * @throws IllegalArgumentException
	 *             in case the passed parameter is malformed or <code>null</code>
	 * @throws IllegalStateException
	 *             in case no namespace is registered for given prefix
	 */
	String buildFullUri(String shortUri);

	/**
	 * <p>
	 * Builds short by given full IRI, according to the cached namespaces and their prefixes in the underlying semantic
	 * repository.
	 * </p>
	 * <p>
	 * For example, if the {@code PREFIX dcterms:<http://purl.org/dc/terms/>} is defined in the semantic repository,
	 * invocation of the method with parameter {@code http://purl.org/dc/terms/title} should produce the following
	 * string {@code dcterms:title}
	 * </p>
	 *
	 * @param fullUri
	 *            the full IRI
	 * @return the short IRI
	 * @throws IllegalArgumentException
	 *             in case the passed parameter is <code>null</code>
	 * @throws IllegalStateException
	 *             in case no prefix is registered for the given full IRI
	 */
	String getShortUri(IRI fullUri);

	/**
	 * <p>
	 * Builds short by given full IRI, according to the cached namespaces and their prefixes in the underlying semantic
	 * repository.
	 * </p>
	 * <p>
	 * For example, if the {@code PREFIX dcterms:<http://purl.org/dc/terms/>} is defined in the semantic repository,
	 * invocation of the method with parameter {@code http://purl.org/dc/terms/title} should produce the following
	 * string {@code dcterms:title}
	 * </p>
	 *
	 * @param fullUri
	 *            the full IRI
	 * @return the short IRI
	 * @throws IllegalArgumentException
	 *             in case the passed parameter is <code>null</code>
	 * @throws IllegalStateException
	 *             in case no prefix is registered for the given full IRI
	 */
	String getShortUri(Uri fullUri);

	/**
	 * <p>
	 * Builds short by given full IRI given as String, according to the cached namespaces and their prefixes in the
	 * underlying semantic repository.
	 * </p>
	 * <p>
	 * For example, if the {@code PREFIX dcterms:<http://purl.org/dc/terms/>} is defined in the semantic repository,
	 * invocation of the method with parameter {@code http://purl.org/dc/terms/title} should produce the following
	 * string {@code dcterms:title}
	 * </p>
	 *
	 * @param fullUri
	 *            the full IRI
	 * @return the short IRI
	 * @throws IllegalArgumentException
	 *             in case the passed parameter is <code>null</code>
	 * @throws IllegalStateException
	 *             in case no prefix is registered for the given full IRI
	 */
	String getShortUri(String fullUri);

	/**
	 * <p>
	 * Gets a shallow copy of all defined prefixes and namespaces in the underlying semantic repository in order to be
	 * used in all semantic queries.
	 * </p>
	 * For example, if the following prefixes and corresponding namespaces are defined in the semantic repository
	 *
	 * <pre>
	 * dc : http://purl.org/dc/elements/1.1/
	 * ptop : http://www.ontotext.com/proton/protontop#
	 * emf : http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#
	 * </pre>
	 *
	 * the method should return the following string
	 *
	 * <pre>
	 * PREFIX dc:&lt;http://purl.org/dc/elements/1.1/&gt;
	 * PREFIX ptop:&lt;http://www.ontotext.com/proton/protontop#&gt;
	 * PREFIX emf:&lt;http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#&gt;
	 * </pre>
	 *
	 * @return string representation of all defined namespaces
	 */
	String getNamespaces();

	/**
	 * Builds IRI from the given short or long IRI in string format.
	 *
	 * @param IRI
	 *            the IRI
	 * @return the IRI
	 */
	IRI buildUri(String IRI);

	/**
	 * Gets the data graph. It's a method here due to the fact it's application specific and is configurable via
	 * external configuration.
	 *
	 * @return the data graph
	 */
	IRI getDataGraph();

	/**
	 * Gets all registered and cached namespaces in the underlying semantic repository.
	 *
	 * @return cached namespaces
	 */
	Map<String, String> getProvidedNamespaces();
}
