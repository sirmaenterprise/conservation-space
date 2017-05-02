package com.sirma.itt.emf.semantic.model.converters;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.model.OpenRdfStringUriProxy;
import com.sirma.itt.emf.semantic.model.OpenRdfUriProxy;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Type converter provider for {@link Uri} conversions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Specializes
public class SemanticUriConverterProvider extends UriConverterProvider {

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public void register(TypeConverter converter) {
		NamespaceRegistryService registry = namespaceRegistryService;
		converter.addConverter(String.class, URI.class, source -> registry.buildUri(source));
		converter.addConverter(URI.class, String.class, source -> registry.getShortUri(source));
		converter.addConverter(String.class, Uri.class, new StringToUriConverter(namespaceRegistryService));
		converter.addConverter(URI.class, Uri.class, source -> new OpenRdfUriProxy(source));
		converter.addConverter(OpenRdfUriProxy.class, String.class, source -> source.toString());
		converter.addConverter(OpenRdfStringUriProxy.class, String.class, source -> source.toString());
		converter.addConverter(Uri.class, ShortUri.class, source -> new ShortUri(registry.getShortUri(source)));
		converter.addDynamicTwoStageConverter(String.class, Uri.class, ShortUri.class);
	}

	/**
	 * Converter for string to URI instance. The string could be in short of full URI format.
	 *
	 * @author BBonev
	 */
	public static class StringToUriConverter implements Converter<String, Uri> {

		private static final String INVALID_URI_PASSED_FOR_CONVERSION = "Invalid uri passed for conversion: {}";
		/** The Constant LOGGER. */
		private static final Logger LOGGER = LoggerFactory.getLogger(SemanticUriConverterProvider.class);
		private NamespaceRegistryService registryService;

		/**
		 * Instantiates a new string to uri converter.
		 *
		 * @param namespaceRegistryService
		 *            the namespace registry service
		 */
		public StringToUriConverter(NamespaceRegistryService namespaceRegistryService) {
			registryService = namespaceRegistryService;
		}

		@Override
		public Uri convert(String source) {
			if (StringUtils.isNullOrEmpty(source) || !source.contains(":")) {
				LOGGER.warn(INVALID_URI_PASSED_FOR_CONVERSION, source);
				return null;
			}
			try {
				URI uri = registryService.buildUri(source);
				if (uri != null) {
					return new OpenRdfUriProxy(uri);
				}
			} catch (RuntimeException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(INVALID_URI_PASSED_FOR_CONVERSION, source, e);
				} else {
					LOGGER.warn(INVALID_URI_PASSED_FOR_CONVERSION, source);
				}
			}
			return null;
		}
	}

}
