package com.sirma.itt.seip.mail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.ContextualMap;

import freemarker.cache.StringTemplateLoader;

/**
 * Implementation of the {@link ContextualTemplateLoader} needed for loading templates with freemarker. Contains the
 * same logic as {@link StringTemplateLoader} but stores the template in a contextual map instead.
 *
 * @author nvelkov
 */
public class ContextualTemplateLoaderImpl implements ContextualTemplateLoader {

	@Inject
	private ContextualMap<String, ContextualTemplateSource> templates;

	@Override
	public Object findTemplateSource(String name) throws IOException {
		return templates.get(name);
	}

	@Override
	public long getLastModified(Object templateSource) {
		return ((ContextualTemplateSource) templateSource).lastModified;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		return new StringReader(((ContextualTemplateSource) templateSource).source);
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		// Not needed. Not Implemented in StringTemplateLoader either.
	}

	@Override
	public void putTemplate(String name, String templateSource) {
		templates.put(name, new ContextualTemplateSource(name, templateSource, System.currentTimeMillis()));
	}

	private static class ContextualTemplateSource {
		private final String name;
		final String source;
		final long lastModified;

		ContextualTemplateSource(String name, String source, long lastModified) {
			if (name == null) {
				throw new IllegalArgumentException("name == null");
			}
			if (source == null) {
				throw new IllegalArgumentException("source == null");
			}
			if (lastModified < -1L) {
				throw new IllegalArgumentException("lastModified < -1L");
			}
			this.name = name;
			this.source = source;
			this.lastModified = lastModified;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ContextualTemplateSource) {
				return name.equals(((ContextualTemplateSource) obj).name);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
}
