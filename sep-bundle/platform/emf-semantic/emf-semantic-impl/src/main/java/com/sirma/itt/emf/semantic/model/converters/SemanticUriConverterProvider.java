package com.sirma.itt.emf.semantic.model.converters;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.extensions.UriConverterProvider;
import com.sirma.itt.emf.domain.model.ShortUri;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.semantic.model.OpenRdfStringUriProxy;
import com.sirma.itt.emf.semantic.model.OpenRdfUriProxy;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Type converter provider for {@link Uri} conversions.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Specializes
public class SemanticUriConverterProvider extends UriConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticUriConverterProvider.class);

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(String.class, Uri.class, new StringToUriConverter());
		converter.addConverter(URI.class, Uri.class, new Converter<URI, Uri>() {

			@Override
			public Uri convert(URI source) {
				return new OpenRdfUriProxy(source);
			}
		});
		converter.addConverter(OpenRdfUriProxy.class, String.class,
				new Converter<OpenRdfUriProxy, String>() {

					@Override
					public String convert(OpenRdfUriProxy source) {
						return source.toString();
					}
				});
		converter.addConverter(OpenRdfStringUriProxy.class, String.class,
				new Converter<OpenRdfStringUriProxy, String>() {

					@Override
					public String convert(OpenRdfStringUriProxy source) {
						return source.toString();
					}
				});
		converter.addConverter(Uri.class, ShortUri.class, new Converter<Uri, ShortUri>() {

			@Override
			public ShortUri convert(Uri source) {
				return new ShortUri(namespaceRegistryService.getShortUri(source));
			}
		});
		converter.addDynamicTwoStageConverter(String.class, Uri.class, ShortUri.class);
	}

	/**
	 * Converter for string to URI instance. The string could be in short of full URI format.
	 * 
	 * @author BBonev
	 */
	public class StringToUriConverter implements Converter<String, Uri> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Uri convert(String source) {
			try {
				URI uri = namespaceRegistryService.buildUri(source);
				if (uri != null) {
					return new OpenRdfUriProxy(uri);
				}
			} catch (RuntimeException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Invalid uri passed for conversion: {}", source, e);
				} else {
					LOGGER.warn("Invalid uri passed for conversion: {}", source);
				}
			}
			return null;
		}
	}

}
