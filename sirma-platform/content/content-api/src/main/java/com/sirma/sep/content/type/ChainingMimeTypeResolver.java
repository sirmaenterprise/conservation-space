package com.sirma.sep.content.type;

import java.io.File;
import java.io.InputStream;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Concrete implementation of {@link MimeTypeResolver}. It is used to collect others implementations and call them
 * consecutively, until the mime type of the input is resolved or there are no more implementations. If one of the
 * implementations returns result, the resolvers calls are stopped and the result is returned.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ChainingMimeTypeResolver implements MimeTypeResolver {

	@Inject
	@ExtensionPoint(MimeTypeResolver.TARGET_NAME)
	private Plugins<MimeTypeResolver> resolvers;

	@Override
	public String getMimeType(InputStream stream, String fileName) {
		return callResolvers(resolver -> resolver.getMimeType(stream, fileName));
	}

	@Override
	public String getMimeType(File file) {
		return callResolvers(resolver -> resolver.getMimeType(file));
	}

	@Override
	public String getMimeType(byte[] bytes, String fileName) {
		return callResolvers(resolver -> resolver.getMimeType(bytes, fileName));
	}

	@Override
	public String resolveFromName(String fileName) {
		return callResolvers(resolver -> resolver.resolveFromName(fileName));
	}

	private String callResolvers(Function<MimeTypeResolver, String> resolverFunc) {
		for (MimeTypeResolver resolver : resolvers) {
			String type = resolverFunc.apply(resolver);
			if (StringUtils.isNotBlank(type)) {
				return type;
			}
		}
		return null;
	}

}
