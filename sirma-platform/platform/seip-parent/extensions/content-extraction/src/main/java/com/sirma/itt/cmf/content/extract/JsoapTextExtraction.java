/**
 *
 */
package com.sirma.itt.cmf.content.extract;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.TextExtractorExtension;

/**
 * Text extractor implementation that uses Jsoup for the extraction.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = TextExtractorExtension.TARGET_NAME, order = 5)
public class JsoapTextExtraction implements TextExtractorExtension {
	/**
	 * Pattern that matches html and xml file mimetypes.
	 */
	public static final String DEFAULT_JSOAP_MIMETYPE_PATTERN = "text/xml|text/x?html|application/xml.*";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.extract.jsoap.mimetype.pattern", type = Pattern.class, defaultValue = DEFAULT_JSOAP_MIMETYPE_PATTERN, sensitive = true, system = true, label = "The pattern used to check if a file mimetype is applicable for Jsonp content extration. The pattern should check if the type is supported by jsoap.")
	private ConfigurationProperty<Pattern> mimetypeMatchPattern;

	@Override
	public String extract(FileDescriptor instance) throws IOException {
		Document document = Jsoup.parse(instance.getInputStream(), StandardCharsets.UTF_8.name(), "");
		return StringUtils.trimToNull(document.text());
	}

	@Override
	public boolean isApplicable(String mimetype, FileDescriptor fileDescriptor) {
		if (StringUtils.isBlank(mimetype)) {
			return false;
		}
		return mimetypeMatchPattern.get().matcher(mimetype).matches();
	}

}
