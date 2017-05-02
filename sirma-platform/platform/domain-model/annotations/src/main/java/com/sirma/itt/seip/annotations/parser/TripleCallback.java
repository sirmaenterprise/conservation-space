package com.sirma.itt.seip.annotations.parser;

import static com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.ParseErrorLogger;
import org.openrdf.rio.helpers.RDFParserHelper;
import org.openrdf.rio.helpers.StatementCollector;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFDataset.Node;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;

/**
 * @author kirq4e
 */
public class TripleCallback implements JsonLdTripleCallback {

	public static final String BLANK_NODE_PREFIX = "_:";

	private ValueFactory vf;

	private RDFHandler handler;

	private ParserConfig parserConfig;

	private final ParseErrorListener parseErrorListener;

	/**
	 * Default header
	 */
	public TripleCallback() {
		this(new StatementCollector(new LinkedHashModel()));
	}

	/**
	 * Creates TipleCallback with the passed argument
	 *
	 * @param nextHandler
	 *            The handler for the produced Statements
	 */
	public TripleCallback(RDFHandler nextHandler) {
		this(nextHandler, ValueFactoryImpl.getInstance());
	}

	/**
	 * Creates TipleCallback with the passed arguments
	 *
	 * @param nextHandler
	 *            The handler for the produced Statements
	 * @param vf
	 *            Value Factory
	 */
	public TripleCallback(RDFHandler nextHandler, ValueFactory vf) {
		this(nextHandler, vf, new ParserConfig(), new ParseErrorLogger());
	}

	/**
	 * Creates TipleCallback with the passed arguments
	 *
	 * @param nextHandler
	 *            The handler for the produced Statements
	 * @param vf
	 *            Value Factory
	 * @param parserConfig
	 *            Parser configuration
	 * @param parseErrorListener
	 *            Error listener
	 */
	public TripleCallback(RDFHandler nextHandler, ValueFactory vf, ParserConfig parserConfig,
			ParseErrorListener parseErrorListener) {
		handler = nextHandler;
		this.vf = vf;
		this.parserConfig = parserConfig;
		this.parseErrorListener = parseErrorListener;
	}

	/**
	 * Transforms the passed data set as RDF Statements
	 *
	 * @param dataset
	 *            The set of data
	 */
	@Override
	public Object call(final RDFDataset dataset) {
		if (getHandler() == null) {
			return null;
		}
		for (String graphName : dataset.keySet()) {
			final List<RDFDataset.Quad> quads = dataset.getQuads(graphName);
			if ("@default".equals(graphName)) {
				graphName = null;
			}
			final String graphNameConst = graphName;
			quads.forEach(quad -> triple(quad, graphNameConst));
		}

		return getHandler();
	}

	private void triple(RDFDataset.Quad quad, String graphName) {
		String subject = quad.getSubject().getValue();
		String predicate = quad.getPredicate().getValue();
		Node value = quad.getObject();

		Value object;
		String stringValue = value.getValue();
		if (value.isLiteral()) {
			String datatype = value.getDatatype();
			// do not set data type for string values
			final URI datatypeURI = datatype == null || XMLSchema.STRING.toString().equals(datatype) ? null
					: vf.createURI(datatype);

			try {
				object = RDFParserHelper.createLiteral(stringValue, value.getLanguage(), datatypeURI, getParserConfig(),
						getParserErrorListener(), getValueFactory());
			} catch (final RDFParseException e) {
				throw new SemanticPersistenceException(e);
			}
		} else {
			object = createURI(stringValue);
		}

		Statement result;
		if (isNullOrEmpty(graphName)) {
			result = vf.createStatement(createURI(subject), vf.createURI(predicate), object);
		} else {
			result = vf.createStatement(createURI(subject), vf.createURI(predicate), object, createURI(graphName));
		}

		try {
			getHandler().handleStatement(result);
		} catch (final RDFHandlerException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	private Resource createURI(String resource) {
		// Blank node without any given identifier
		if (BLANK_NODE_PREFIX.equals(resource)) {
			return vf.createBNode();
		} else if (resource.startsWith(BLANK_NODE_PREFIX)) {
			return vf.createBNode(resource.substring(2));
		} else {
			return vf.createURI(resource);
		}
	}

	public ParseErrorListener getParserErrorListener() {
		return parseErrorListener;
	}

	/**
	 * @return the handler
	 */
	public RDFHandler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 *            the handler to set
	 */
	public void setHandler(RDFHandler handler) {
		this.handler = handler;
	}

	/**
	 * @return the parserConfig
	 */
	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	/**
	 * @param parserConfig
	 *            the parserConfig to set
	 */
	public void setParserConfig(ParserConfig parserConfig) {
		this.parserConfig = parserConfig;
	}

	/**
	 * @return the vf
	 */
	public ValueFactory getValueFactory() {
		return vf;
	}

	/**
	 * @param vf
	 *            the vf to set
	 */
	public void setValueFactory(ValueFactory vf) {
		this.vf = vf;
	}

}