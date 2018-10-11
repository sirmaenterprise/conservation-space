package com.sirma.itt.cmf.integration.webscript;

import java.util.Map;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormDataReader;

/**
 * The BaseFormScript is base for multipart scripts.
 */
public abstract class BaseFormScript extends BaseAlfrescoScript {

	protected static final String KEY_FORMDATA = "formdata";

	/**
	 * Extract form data.
	 *
	 * @param request
	 *            the request
	 * @return the form data
	 */
	protected FormData extractFormData(WebScriptRequest request) {
		FormDataReader reader = new FormDataReader();

		Map<String, Object> read = reader.createScriptParameters(request, null);
		if (read.containsKey(KEY_FORMDATA)) {
			return (FormData) read.get(KEY_FORMDATA);
		}
		return null;
	}

}
