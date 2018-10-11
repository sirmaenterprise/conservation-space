package com.sirma.itt.seip.instance.content;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class. Contains mime types of MSOffice files.
 *
 * @author T. Dossev
 */
public final class MimetypeConstants {

	// DOC - Microsoft Word 97 - 2003 Document
	// DOT - Microsoft Word 97 - 2003 Template
	public static final String DOC_DOT = "application/msword";

	// DOCX - Word document
	public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	// DOTX - Word template
	public static final String DOTX = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";

	// DOCM - Word macro-enabled document; same as docx, but may contain macros and scripts
	public static final String DOCM = "application/vnd.ms-word.document.macroEnabled.12";

	// DOTM - Word macro-enabled template; same as dotx, but may contain macros and scripts
	public static final String DOTM = "application/vnd.ms-word.template.macroEnabled.12";

	// XLS - Microsoft Excel 97-2003 Worksheet
	// XLT - Microsoft Excel 97-2003 Template
	public static final String XLS_XLT = "application/vnd.ms-excel";

	// XLSX - Excel workbook
	public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	// XLTX - Excel template
	public static final String XLTX = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";

	// XLSM - Excel macro-enabled workbook; same as xlsx but may contain macros and scripts
	public static final String XLSM = "application/vnd.ms-excel.sheet.macroEnabled.12";

	// XLTM - Excel macro-enabled template; same as xltx but may contain macros and scripts
	public static final String XLTM = "application/vnd.ms-excel.template.macroEnabled.12";

	// PPT - Microsoft PowerPoint 97-2003 Presentation
	// POT - Microsoft PowerPoint 97-2003 Template
	public static final String PPT_POT = "application/vnd.ms-powerpoint";

	// PPTX - PowerPoint presentation
	public static final String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

	// POTX - Excel template
	public static final String POTX = "application/vnd.openxmlformats-officedocument.presentationml.template";

	// PPTM - PowerPoint macro-enabled presentation
	public static final String PPTM = "application/vnd.ms-powerpoint.presentation.macroEnabled.12";

	// POTM - PowerPoint macro-enabled template
	public static final String POTM = "application/vnd.ms-powerpoint.template.macroEnabled.12";

	private static final Set<String> SUPPORTED_MIME_TYPES = new HashSet<>(
			Arrays.asList(MimetypeConstants.DOC_DOT, MimetypeConstants.DOCX, MimetypeConstants.DOCM,
					MimetypeConstants.DOTM, MimetypeConstants.DOTX, MimetypeConstants.XLS_XLT, MimetypeConstants.XLSM,
					MimetypeConstants.XLSX, MimetypeConstants.XLTM, MimetypeConstants.XLTX, MimetypeConstants.PPT_POT,
					MimetypeConstants.POTM, MimetypeConstants.POTX, MimetypeConstants.PPTM, MimetypeConstants.PPTX));

	/**
	 * Utility class constructor.
	 */
	private MimetypeConstants() {

	}

	/**
	 * Check if current mimetype is supported for edit offline.
	 * 
	 * @param mimeType
	 *            the checked mimetype.
	 * @return true if <code>mimeType</code> is supported for edit offline.
	 */
	public static boolean isMimeTypeSupported(String mimeType){
		return SUPPORTED_MIME_TYPES
				.stream()
					.anyMatch(constant -> constant.equalsIgnoreCase(mimeType));
	}
}
