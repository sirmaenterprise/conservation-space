package com.sirma.sep.ocr.service;

import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Pattern converter for the mimetype pattern from application.properties. The configuration property holds the value as
 * a String and this class will convert it to a pattern.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @see TesseractOCRProperties
 * @since 17/10/2017
 */
@Component
@ConfigurationPropertiesBinding
public class MimetypeConfigPatternConverter implements Converter<String, Pattern> {

	@Override
	public Pattern convert(String source) {
		if (source == null) {
			return null;
		}
		return Pattern.compile(source);
	}

}
