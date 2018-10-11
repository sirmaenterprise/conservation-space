package com.sirma.sep.ocr.service;

import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * The {@link TesseractOCRProperties} is configuration component holding the current 'tesseract. ...' configuration values.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@Component
@ConfigurationProperties("tesseract")
public class TesseractOCRProperties {

	private String datapath;
	private String language;
	private int engineMode;

	@NestedConfigurationProperty
	private Mimetype mimetype;

	/**
	 * Mimetype sub configuration group.
	 */
	public static class Mimetype {

		private Pattern pattern;

		public Pattern getPattern() {
			return pattern;
		}

		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
		}
	}

	public String getDatapath() {
		return datapath;
	}

	public void setDatapath(String datapath) {
		this.datapath = datapath;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Mimetype getMimetype() {
		return mimetype;
	}

	public void setMimetype(Mimetype mimetype) {
		this.mimetype = mimetype;
	}

	public int getEngineMode() {
		return engineMode;
	}

	public void setEngineMode(int engineMode) {
		this.engineMode = engineMode;
	}
}