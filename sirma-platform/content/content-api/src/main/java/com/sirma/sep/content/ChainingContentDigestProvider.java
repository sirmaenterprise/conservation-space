package com.sirma.sep.content;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Wrapper for the {@link ContentDigestProvider} extension
 * 
 * @author BBonev
 */
@Singleton
public class ChainingContentDigestProvider implements ContentDigestProvider {

	@Inject
	@ExtensionPoint(ContentDigestProvider.EXTENSION_NAME)
	private Plugins<ContentDigestProvider> extension;

	@Override
	public boolean accept(Content content) {
		return extension.stream().anyMatch(plugin -> plugin.accept(content));
	}

	@Override
	public String digest(Content content) {
		for (ContentDigestProvider provider : extension) {
			if (provider.accept(content)) {
				String digest = provider.digest(content);
				if (digest != null) {
					return digest;
				}
			}
		}
		return null;
	}

}
