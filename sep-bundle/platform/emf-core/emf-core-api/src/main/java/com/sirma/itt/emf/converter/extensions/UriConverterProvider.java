package com.sirma.itt.emf.converter.extensions;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.domain.model.ShortUri;
import com.sirma.itt.emf.domain.model.Uri;

/**
 * Type converter provider for {@link Uri} conversions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class UriConverterProvider implements TypeConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UriConverterProvider.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(String.class, Uri.class, new StringToUriConverter<Uri>());
		converter.addConverter(String.class, StringUriProxy.class,
				new StringToUriConverter<StringUriProxy>());
		converter.addConverter(StringUriProxy.class, String.class,
				new Converter<StringUriProxy, String>() {

					@Override
					public String convert(StringUriProxy source) {
						return source.toString();
					}
				});
		converter.addConverter(StringUriProxy.class, ShortUri.class, new UriToShortUriConverter());
		converter.addDynamicTwoStageConverter(String.class, StringUriProxy.class, ShortUri.class);
	}

	/**
	 * Converter for string to URI instance. The string could be in short of full URI format.
	 * 
	 * @author BBonev
	 * @param <R>
	 *            the generic type
	 */
	public class StringToUriConverter<R extends Uri> implements Converter<String, R> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public R convert(String source) {
			try {
				return (R) new StringUriProxy(source);
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

	/**
	 * Default dummy implementation if semantic is missing
	 *
	 * @author BBonev
	 */
	public class UriToShortUriConverter implements Converter<StringUriProxy, ShortUri> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ShortUri convert(StringUriProxy source) {
			// default dummy implementation if semantic is missing
			return new ShortUri("emf:" + source.getLocalName());
		}

	}

	/**
	 * Dummy class to represent uri implementation if semantic is missing.
	 */
	public static class StringUriProxy implements Uri {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -7302648219312402976L;
		/** The uri. */
		private String uri;
		/** The namespace. */
		private String namespace;

		private String localName;

		/**
		 * Instantiates a new string uri proxy.
		 */
		public StringUriProxy() {
		}

		/**
		 * Instantiates a new string uri proxy.
		 *
		 * @param uri
		 *            the uri
		 */
		public StringUriProxy(String uri) {
			this.uri = uri;

			String[] split = uri.split("#|:|/");
			localName = split[split.length - 1];
			namespace = uri.substring(0, uri.length() - localName.length() - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getNamespace() {
			return namespace;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getLocalName() {
			return localName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			StringUriProxy other = (StringUriProxy) obj;
			if (uri == null) {
				if (other.uri != null) {
					return false;
				}
			} else if (!uri.equals(other.uri)) {
				return false;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return uri;
		}

	}
}
