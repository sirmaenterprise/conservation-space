package com.sirma.itt.emf.semantic.model.converters;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.semantic.model.Rdf4JStringUriProxy;
import com.sirma.itt.emf.semantic.model.Rdf4JUriProxy;
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
		converter.addConverter(String.class, IRI.class, registry::buildUri);
		converter.addConverter(IRI.class, String.class, registry::getShortUri);
		converter.addConverter(String.class, Uri.class, new StringToUriConverter(namespaceRegistryService));
		converter.addConverter(IRI.class, Uri.class, Rdf4JUriProxy::new);
		converter.addConverter(Rdf4JUriProxy.class, String.class, Rdf4JUriProxy::toString);
		converter.addConverter(Rdf4JStringUriProxy.class, String.class, Rdf4JStringUriProxy::toString);
		converter.addConverter(Uri.class, ShortUri.class, source -> new ShortUri(registry.getShortUri(source)));
		converter.addConverter(URIImpl.class, String.class, URIImpl::stringValue);
		converter.addConverter(URIImpl.class, ShortUri.class, source -> new ShortUri(source.stringValue()));
		converter.addDynamicTwoStageConverter(String.class, Uri.class, ShortUri.class);
	}

	/**
	 * Converter for string to IRI instance. The string could be in short of full IRI format.
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
			if (StringUtils.isBlank(source) || !source.contains(":")) {
				LOGGER.warn(INVALID_URI_PASSED_FOR_CONVERSION, source);
				return null;
			}
			try {
				IRI uri = registryService.buildUri(source);
				if (uri != null) {
					return new Rdf4JUriProxy(uri);
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
