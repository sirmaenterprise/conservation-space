package com.sirma.itt.seip.annotations.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFParser;

import com.github.jsonldjava.core.Context;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;

/**
 * Implementation of parser from JSON-LD to RDF by caching the parsed context. The default context store
 * {@link ContextStore} could be overridden using the method {@link #setContextProvider(BiFunction)}.
 *
 * @author kirq4e
 * @author BBonev
 */
public class JSONLDParser extends AbstractRDFParser {

	public static final String CONTEXT = "@context";

	/** The context provided that can be used for creating new context instances or providing cached one. */
	private BiFunction<String, Function<Object, Context>, Context> contextProvider;

	/** The context builder that can be used for creating new context instances. */
	private BiFunction<Object, JsonLdOptions, Context> contextBuilder;

	/** The options builder used to build {@link JsonLdOptions} used for parsing the context and the to input model. */
	private Function<String, JsonLdOptions> optionsBuilder;

	/**
	 * Default constructor
	 */
	public JSONLDParser() {
		super();
	}

	/**
	 * Creates a JSONLD Parser using the given {@link ValueFactory} to create new {@link Value}s.
	 *
	 * @param valueFactory
	 *            The ValueFactory to use
	 */
	public JSONLDParser(final ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.JSONLD;
	}

	@Override
	public void parse(final InputStream in, final String baseURI) {
		final JsonLdOptions options = getOptionsBuilder().apply(baseURI);

		try {
			Object input = JsonUtils.fromInputStream(in);
			loadContext(input, baseURI, options);
			JsonLdProcessor.toRDF(input, getProcessorCallback(), options);
		} catch (final JsonLdError | IOException e) {
			throw new SemanticPersistenceException("Could not parse JSONLD", e);
		}
	}

	@Override
	public void parse(final Reader reader, final String baseURI) {
		final JsonLdOptions options = getOptionsBuilder().apply(baseURI);

		try {
			Object input = JsonUtils.fromReader(reader);
			loadContext(input, baseURI, options);
			JsonLdProcessor.toRDF(input, getProcessorCallback(), options);
		} catch (final JsonLdError | IOException e) {
			throw new SemanticPersistenceException("Could not parse JSONLD", e);
		}
	}

	/**
	 * Gets the processor callback. The callback will be used when calling
	 * {@link JsonLdProcessor#toRDF(Object, JsonLdTripleCallback, JsonLdOptions)}
	 *
	 * @return the processor callback
	 */
	protected JsonLdTripleCallback getProcessorCallback() {
		return new TripleCallback(getRDFHandler(), valueFactory, getParserConfig(), getParseErrorListener());
	}

	@SuppressWarnings("unchecked")
	private void loadContext(Object input, final String baseURI, final JsonLdOptions options) {
		if (input instanceof Collection<?>) {
			for (Object object : (Collection<?>) input) {
				loadContext(object, baseURI, options);
			}
			return;
		}
		Object context = ((Map<String, Object>) input).compute(CONTEXT,
				(key, currentContext) -> mergeContext(currentContext, baseURI, options));
		if (context == null) {
			throw new SemanticPersistenceException("Could not load Context from " + baseURI);
		}
	}

	private Object mergeContext(Object current, String baseUri, JsonLdOptions options) {
		if (current instanceof Context) {
			return current;
		} else if (current instanceof String) {
			options.setBase((String) current);
			Context context = getContextProvider().apply((String) current,
					uri -> getContextBuilder().apply(uri, options));
			if (context != null) {
				return context;
			}
		}
		return getContextProvider().apply(baseUri, uri -> getContextBuilder().apply(uri, options));
	}

	/**
	 * Returns the current context builder or the default if not specified.
	 *
	 * @return the context builder
	 */
	public BiFunction<Object, JsonLdOptions, Context> getContextBuilder() {
		return contextBuilder != null ? contextBuilder : getDefaultContextBuilder();
	}

	/**
	 * Sets the context builder that can be used for building {@link Context}. The first argument will the the URI of
	 * the context and the second are the options to be used for building.
	 *
	 * @param contextProvider
	 *            the contextProvider to set
	 * @return the current instance for method chaining
	 */
	public JSONLDParser setContextBuilder(BiFunction<Object, JsonLdOptions, Context> contextBuilder) {
		this.contextBuilder = contextBuilder;
		return this;
	}

	/**
	 * Gets the default context builder {@link ContextBuilder}.
	 *
	 * @return the default context provider
	 * @see ContextBuilder
	 */
	@SuppressWarnings("static-method")
	protected BiFunction<Object, JsonLdOptions, Context> getDefaultContextBuilder() {
		return ContextBuilder.getInstance()::build;
	}

	/**
	 * Returns the current context provider or the default if not specified.
	 *
	 * @return the contextProvider
	 */
	public BiFunction<String, Function<Object, Context>, Context> getContextProvider() {
		return contextProvider != null ? contextProvider : getDefaultContextProvider();
	}

	/**
	 * Sets the context provider that can be used for building {@link Context}. The first argument will the the URI of
	 * the context and the second builder that can be used to create new instance .
	 *
	 * @param contextProvider
	 *            the contextProvider to set
	 * @return the current instance for method chaining
	 */
	public JSONLDParser setContextProvider(BiFunction<String, Function<Object, Context>, Context> contextProvider) {
		this.contextProvider = contextProvider;
		return this;
	}

	/**
	 * Gets the default context provider {@link ContextStore}.
	 *
	 * @return the default context provider
	 * @see ContextStore
	 */
	@SuppressWarnings("static-method")
	protected BiFunction<String, Function<Object, Context>, Context> getDefaultContextProvider() {
		return ContextStore::get;
	}

	/**
	 * Gets the options builder to produce a options for parsing. If no custom option is passed the default will be
	 * returned.
	 *
	 * @return the optionsBuilder
	 */
	public Function<String, JsonLdOptions> getOptionsBuilder() {
		return optionsBuilder != null ? optionsBuilder : getDefaultOptionsBuilder();
	}

	/**
	 * Sets the options builder to be used to produce the {@link JsonLdOptions} used for parsing the {@link Context} and
	 * the input model with
	 * {@link JsonLdProcessor#toRDF(Object, com.github.jsonldjava.core.JsonLdTripleCallback, JsonLdOptions)}.
	 * <p>
	 * The builder argument is the base uri passed to parse method.
	 *
	 * @param optionsBuilder
	 *            the optionsBuilder to set
	 * @return the current instance for method chaining
	 */
	public JSONLDParser setOptionsBuilder(Function<String, JsonLdOptions> optionsBuilder) {
		this.optionsBuilder = optionsBuilder;
		return this;
	}

	/**
	 * Gets the default options builder to be used if no other is specified. The default implementation uses the
	 * {@link JsonLdOptions#JsonLdOptions(String)} constructor.
	 *
	 * @return the default options builder
	 */
	@SuppressWarnings("static-method")
	protected Function<String, JsonLdOptions> getDefaultOptionsBuilder() {
		return baseUri -> {
			JsonLdOptions options = new JsonLdOptions(baseUri);
			options.useNamespaces = Boolean.TRUE;
			return options;
		};
	}

}
