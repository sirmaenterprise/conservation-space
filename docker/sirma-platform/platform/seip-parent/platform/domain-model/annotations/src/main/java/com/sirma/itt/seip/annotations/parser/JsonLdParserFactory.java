package com.sirma.itt.seip.annotations.parser;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserFactory;
import org.eclipse.rdf4j.rio.RDFParserRegistry;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * JSON-LD parser factory that provides parser implementation for transforming data in JSON-LD format to RDF format
 *
 * @author kirq4e
 */
public class JsonLdParserFactory implements RDFParserFactory {

	/**
	 * Register the factory on application start. Note that this could be realized via service registry extension placed
	 * in {@code META-INF/services/org.eclipse.rdf4j.rio.RDFParserFactory} file
	 */
	@Startup(transactionMode = TransactionMode.NOT_SUPPORTED)
	public void registerOnStartup() {
		RDFParserRegistry.getInstance().add(this);
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.JSONLD;
	}

	@Override
	public RDFParser getParser() {
		return new JSONLDParser();
	}

}
