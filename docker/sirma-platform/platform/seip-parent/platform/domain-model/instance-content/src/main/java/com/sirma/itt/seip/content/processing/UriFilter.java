package com.sirma.itt.seip.content.processing;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link URI} filter could be used for defining custom filtration rules to allow or disallow accessing particular
 * network addresses. <br>
 * There are several predefined filter rules that can be used out of the box <ul>
 * <li>{@link #acceptAll()} - allows all addresses</li>
 * <li>{@link #denyAll()} - forbid all communication</li>
 * <li>{@link #whiteList(String...)} - blocks all communications unless the addess matches the given patterns</li>
 * <li>{@link #blackList(String...)} - allows all addresses except the one that matches the given patterns</li>
 * </ul>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/12/2018
 */
@FunctionalInterface
public interface UriFilter extends Predicate<String> {

	/**
	 * Filter that accepts all tested requests
	 *
	 * @return always accepting filter instance
	 */
	static UriFilter acceptAll() {
		return uri -> true;
	}

	/**
	 * Filter that denies all requests
	 *
	 * @return always rejecting filter
	 */
	static UriFilter denyAll() {
		return uri -> false;
	}

	/**
	 * Filter that approves addresses matching one of the given patterns. If no pattern are passed then nothing will be
	 * allowed.
	 *
	 * @param whiteListPatterns the white list of allowed pattern addresses
	 * @return filter that accepts only addresses matching a given patterns
	 */
	static UriFilter whiteList(String... whiteListPatterns) {
		if (whiteListPatterns == null || whiteListPatterns.length == 0) {
			return denyAll();
		}
		List<Pattern> patterns = Arrays.stream(whiteListPatterns).map(Pattern::compile).collect(Collectors.toList());
		return uri -> {
			if (StringUtils.isBlank(uri)) {
				return false;
			}
			return patterns.stream().anyMatch(pattern -> pattern.matcher(uri).matches());
		};
	}

	/**
	 * Filter that rejects address matching one of the given patterns and approves everything else. If patters are
	 * specified then everything is allowed.
	 *
	 * @param blackListPatterns the list of patterns of the forbidden addresses
	 * @return filter that blocks particular addresses
	 */
	static UriFilter blackList(String... blackListPatterns) {
		if (blackListPatterns == null || blackListPatterns.length == 0) {
			return acceptAll();
		}
		List<Pattern> patterns = Arrays.stream(blackListPatterns).map(Pattern::compile).collect(Collectors.toList());
		return uri -> {
			if (StringUtils.isBlank(uri)) {
				return false;
			}
			return patterns.stream().noneMatch(pattern -> pattern.matcher(uri).matches());
		};
	}
}
