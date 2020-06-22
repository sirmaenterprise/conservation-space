package com.sirma.sep.export;

import java.util.stream.Stream;

/**
 * Defines supported file formats for the export functionality.
 *
 * @author A. Kunchev
 */
public enum SupportedExportFormats {

	/**
	 * Used for export to PDF.
	 */
	PDF("pdf", "application/pdf"),

	/**
	 * Used for export to MS Word.
	 */
	WORD("word", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

	/**
	 * Used for export to MS Excel.
	 */
	XLS("excel", "application/excel"),

	/**
	 * Unknown export format used in {@link #getSupportedFormat(String)}.
	 */
	UNKNOWN("unknown", "unknown");

	private final String format;
	private final String mimeType;

	SupportedExportFormats(final String format, final String mimeType) {
		this.format = format;
		this.mimeType = mimeType;
	}

	public String getFormat() {
		return format;
	}

	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Gets the corresponding export format.
	 *
	 * @param format
	 * 		export format, "pdf", "word", etc.
	 * @return enum value of the corresponding format or null if no such was found.
	 */
	public static SupportedExportFormats getSupportedFormat(String format) {
		return Stream.of(values()).filter(element -> element.format.equals(format)).findFirst().orElse(UNKNOWN);
	}

}
