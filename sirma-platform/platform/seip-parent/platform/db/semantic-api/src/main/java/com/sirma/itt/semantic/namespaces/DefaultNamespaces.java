/**
 *
 */
package com.sirma.itt.semantic.namespaces;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.semantic.model.vocabulary.CHD;
import com.sirma.itt.semantic.model.vocabulary.CNT;
import com.sirma.itt.semantic.model.vocabulary.Connectors;
import com.sirma.itt.semantic.model.vocabulary.DCMI;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.EQMS;
import com.sirma.itt.semantic.model.vocabulary.HRC;
import com.sirma.itt.semantic.model.vocabulary.OA;
import com.sirma.itt.semantic.model.vocabulary.PDM;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.itt.semantic.model.vocabulary.TCRM;

/**
 * Contains list of the default namespaces that are needed for the system to work property
 *
 * @author Kiril Penev
 */
public final class DefaultNamespaces {

	/**
	 * Contains list of the default namespaces that are needed for the system to work properly
	 */
	public static final Set<StringPair> DEFAULT_NAMESPACES;

	/**
	 * Contains list of all namespaces
	 */
	public static final Set<StringPair> ALL_NAMESPACES;

	static {
		Set<StringPair> tempNamespaces = new HashSet<>();
		// System namespaces
		tempNamespaces.add(new StringPair(DCTERMS.PREFIX, DCTERMS.NAMESPACE));
		tempNamespaces.add(new StringPair(OWL.PREFIX, OWL.NAMESPACE));
		tempNamespaces.add(new StringPair(RDF.PREFIX, RDF.NAMESPACE));
		tempNamespaces.add(new StringPair("xml", "http://www.w3.org/XML/1998/namespace"));
		tempNamespaces.add(new StringPair("xsd", "http://www.w3.org/2001/XMLSchema#"));
		tempNamespaces.add(new StringPair(RDFS.PREFIX, RDFS.NAMESPACE));
		tempNamespaces.add(new StringPair("solr", "http://www.ontotext.com/connectors/solr#"));
		tempNamespaces.add(new StringPair("sesame", "http://www.openrdf.org/schema/sesame#"));
		tempNamespaces.add(new StringPair("psys", "http://proton.semanticweb.org/protonsys#"));
		tempNamespaces.add(new StringPair("solr-inst", "http://www.ontotext.com/connectors/solr/instance#"));
		tempNamespaces.add(new StringPair(OA.PREFIX, OA.NAMESPACE));
		tempNamespaces.add(new StringPair(DC.PREFIX, DC.NAMESPACE));
		tempNamespaces.add(new StringPair(CNT.PREFIX, CNT.NAMESPACE));
		tempNamespaces.add(new StringPair(DCMI.PREFIX, DCMI.NAMESPACE));
		tempNamespaces.add(new StringPair("conc", "http://www.sirma.com/ontologies/2016/06/concepts#"));
		tempNamespaces.add(new StringPair(Connectors.PREFIX, Connectors.NAMESPACE));

		DEFAULT_NAMESPACES = Collections.unmodifiableSet(tempNamespaces);

		// all namespaces
		tempNamespaces = new HashSet<>();
		tempNamespaces.addAll(DEFAULT_NAMESPACES);
		// default application namespaces
		tempNamespaces.add(new StringPair(EMF.PREFIX, EMF.NAMESPACE));
		tempNamespaces.add(new StringPair(Security.PREFIX, Security.NAMESPACE));
		tempNamespaces.add(new StringPair(Proton.PREFIX, Proton.NAMESPACE));
		tempNamespaces.add(new StringPair(SKOS.PREFIX, SKOS.NAMESPACE));
		tempNamespaces.add(new StringPair(CHD.PREFIX, CHD.NAMESPACE));
		tempNamespaces.add(new StringPair(PDM.PREFIX, PDM.NAMESPACE));
		tempNamespaces.add(new StringPair(EQMS.PREFIX, EQMS.NAMESPACE));
		tempNamespaces.add(new StringPair(TCRM.PREFIX, TCRM.NAMESPACE));
		tempNamespaces.add(new StringPair(HRC.PREFIX, HRC.NAMESPACE));

		ALL_NAMESPACES = Collections.unmodifiableSet(tempNamespaces);
	}

	private DefaultNamespaces() {
	}
}
