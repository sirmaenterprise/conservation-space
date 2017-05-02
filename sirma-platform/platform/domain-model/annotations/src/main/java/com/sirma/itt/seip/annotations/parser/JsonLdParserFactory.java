package com.sirma.itt.seip.annotations.parser;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;

import com.sirma.itt.seip.runtime.boot.Startup;

/**
 * JSON-LD parser factory that provides parser implementation for transforming data in JSON-LD format to RDF format
 *
 * @author kirq4e
 */
public class JsonLdParserFactory implements RDFParserFactory {

	/**
	 * Register the factory on application start. Note that this could be realized via service registry extension placed
	 * in {@code META-INF/services/org.openrdf.rio.RDFParserFactory} file
	 */
	@Startup
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
