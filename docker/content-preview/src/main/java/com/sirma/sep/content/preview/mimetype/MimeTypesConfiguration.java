package com.sirma.sep.content.preview.mimetype;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Configuration provider for the available {@link MimeType} mappings in configuration files provided to the
 * application.
 *
 * @author Mihail Radkov
 */
@Validated
@Component
@ConfigurationProperties(prefix = "content.preview")
public class MimeTypesConfiguration {

	@NotNull
	private List<MimeType> mimeTypes;

	public List<MimeType> getMimeTypes() {
		return mimeTypes;
	}

	public void setMimeTypes(List<MimeType> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	/**
	 * Custom configuration converter for {@link MimeTypeSupport} from {@link Boolean}. Required because YAML considers
	 * "yes" for <code>true</code> and "no" for <code>false</code>.
	 *
	 * @author Mihail Radkov
	 */
	@Component
	@ConfigurationPropertiesBinding
	public static class MimeTypeSupportConverter implements Converter<Boolean, MimeTypeSupport> {

		@Override
		public MimeTypeSupport convert(Boolean source) {
			return source ? MimeTypeSupport.YES : MimeTypeSupport.NO;
		}
	}

}
