/**
 *
 */
package com.sirma.itt.seip.security.authentication;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Context used when calling the authentication chain. It's purpose is to provide general means of requesting data so
 * that the authentication could be done.
 *
 * @author BBonev
 */
public interface AuthenticationContext {

	/** Property that can be used for initializing a correlation request id. */
	String CORRELATION_REQUEST_ID = "x-correlation-id";

	/**
	 * Gets all available keys in the context
	 *
	 * @return the keys
	 */
	Set<String> getKeys();

	/**
	 * Gets the property for the given key or <code>null</code> if no such key or the value is <code>null</code>.
	 *
	 * @param key
	 *            the key
	 * @return the property value if exists
	 */
	String getProperty(String key);

	/**
	 * Creates the empty unmodifiable context. This could be used when performing session authentication. No other
	 * authenticator will match this context.
	 *
	 * @return the authentication context
	 */
	static AuthenticationContext createEmpty() {
		return create(Collections.emptyMap());
	}

	/**
	 * Creates authentication context using simple map
	 *
	 * @param properties
	 *            the properties
	 * @return the authentication context
	 */
	static AuthenticationContext create(Map<String, String> properties) {
		Objects.requireNonNull(properties, "Properies map should not be null");
		return create(properties::get, properties::keySet);
	}

	/**
	 * Creates authentication context from given {@link HttpServletRequest}. The properties will be fetched from the
	 * request headers or parameters and headers are with higher priority
	 *
	 * @param request
	 *            the request
	 * @return the authentication context
	 */
	static AuthenticationContext create(HttpServletRequest request) {
		Objects.requireNonNull(request, "Request should not be null");
		return create(key -> {
			String value = request.getHeader(key);
			if (value == null) {
				value = request.getParameter(key);
			}
			return value;
		} , () -> {
			Set<String> keys = new HashSet<>();
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				keys.add(headerNames.nextElement());
			}
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				keys.add(parameterNames.nextElement());
			}
			return keys;
		});
	}

	/**
	 * Creates authentication context from given {@link ContainerRequestContext}. The properties will be fetched from
	 * the request headers.
	 *
	 * @param request
	 *            the request
	 * @return the authentication context
	 */
	static AuthenticationContext create(ContainerRequestContext request) {
		Objects.requireNonNull(request, "Request should not be null");
		return create(request::getHeaderString, request.getHeaders()::keySet);
	}

	/**
	 * Creates custom authentication requests using the given functions for the {@link #getProperty(String)} method and
	 * the supplier for the {@link #getKeys()} method.
	 *
	 * @param valueSupplier
	 *            the property supplier
	 * @param keys
	 *            the keys
	 * @return the authentication context
	 */
	static AuthenticationContext create(Function<String, String> valueSupplier, Supplier<Set<String>> keys) {
		Objects.requireNonNull(valueSupplier, "Property value provider should not be null");
		Objects.requireNonNull(keys, "Keys supplier should not be null");
		return new AuthenticationContext() {

			@Override
			public String getProperty(String key) {
				return valueSupplier.apply(key);
			}

			@Override
			public Set<String> getKeys() {
				return keys.get();
			}
		};
	}
}
