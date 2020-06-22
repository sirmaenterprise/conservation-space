package com.sirma.sep.model.management;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.openrdf.model.vocabulary.SKOS;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.CHD;
import com.sirma.itt.semantic.model.vocabulary.CNT;
import com.sirma.itt.semantic.model.vocabulary.Connectors;
import com.sirma.itt.semantic.model.vocabulary.DCMI;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.HRC;
import com.sirma.itt.semantic.model.vocabulary.OA;
import com.sirma.itt.semantic.model.vocabulary.PDM;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.itt.semantic.model.vocabulary.TCRM;

/**
 * Fake implementation of {@link NamespaceRegistryService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/08/2018
 */
public class NamespaceRegistryFake implements NamespaceRegistryService {

	private static Map<String, String> prefixToNamespace = new HashMap<>();
	private static Map<String, String> namespaceToPrefix = new HashMap<>();

	static {
		prefixToNamespace.put(EMF.PREFIX, EMF.NAMESPACE);
		prefixToNamespace.put(Proton.PREFIX, Proton.NAMESPACE);
		prefixToNamespace.put(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
		prefixToNamespace.put(CHD.PREFIX, CHD.NAMESPACE);
		prefixToNamespace.put(CNT.PREFIX, CNT.NAMESPACE);
		prefixToNamespace.put(Connectors.PREFIX, Connectors.NAMESPACE);
		prefixToNamespace.put(DCMI.PREFIX, DCMI.NAMESPACE);
		prefixToNamespace.put(HRC.PREFIX, HRC.NAMESPACE);
		prefixToNamespace.put(OA.PREFIX, OA.NAMESPACE);
		prefixToNamespace.put(PDM.PREFIX, PDM.NAMESPACE);
		prefixToNamespace.put(TCRM.PREFIX, TCRM.NAMESPACE);
		prefixToNamespace.put(Security.PREFIX, Security.NAMESPACE);
		prefixToNamespace.put(SKOS.PREFIX, SKOS.NAMESPACE);
		prefixToNamespace.put(RDF.PREFIX, RDF.NAMESPACE);
		prefixToNamespace.put(RDFS.PREFIX, RDFS.NAMESPACE);
		prefixToNamespace.put(XMLSchema.PREFIX, XMLSchema.NAMESPACE);

		prefixToNamespace.forEach((prefix, namespace) -> namespaceToPrefix.put(namespace, prefix));
	}

	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Override
	public String getNamespace(String prefix) {
		return prefix;
	}

	@Override
	public String buildFullUri(String shortUri) {
		int separatorIndex = shortUri.indexOf(':');
		if (separatorIndex < 0) {
			throw new IllegalArgumentException("Invalid URI: " + shortUri);
		}
		String prefix = shortUri.substring(0, separatorIndex);

		if (StringUtils.isBlank(prefix)) {
			return shortUri;
		}

		String name = shortUri.substring(separatorIndex + 1);

		return prefixToNamespace.computeIfAbsent(prefix, p -> p + ":") + name;
	}

	@Override
	public String getShortUri(IRI fullUri) {
		String namespacePreffix = namespaceToPrefix.get(fullUri.getNamespace());
		return namespacePreffix + SHORT_URI_DELIMITER + fullUri.getLocalName();
	}

	@Override
	public String getShortUri(Uri fullUri) {
		return null;
	}

	@Override
	public String getShortUri(String fullUri) {
		if (!fullUri.startsWith("http")) {
			return fullUri;
		}
		int delimiterIndex = getFullUriDelimiterIndex(fullUri);
		String namespace = fullUri.substring(0, delimiterIndex + 1);
		String name = fullUri.substring(delimiterIndex + 1);
		return namespaceToPrefix.get(namespace) + SHORT_URI_DELIMITER + name;
	}

	private static int getFullUriDelimiterIndex(String fullUri) {
		int delimiterIndex = fullUri.lastIndexOf('#');
		if (delimiterIndex < 0) {
			delimiterIndex = fullUri.lastIndexOf('/');
		}
		if (delimiterIndex < 0) {
			throw new IllegalArgumentException("Unknown URI delimiter");
		}
		return delimiterIndex;
	}

	@Override
	public String getNamespaces() {
		throw new UnsupportedOperationException("Fake not implemented");
	}

	@Override
	public IRI buildUri(String iri) {
		return valueFactory.createIRI(iri);
	}

	@Override
	public IRI getDataGraph() {
		return EMF.DATA_CONTEXT;
	}

	@Override
	public Map<String, String> getProvidedNamespaces() {
		return prefixToNamespace;
	}
}
