package com.sirma.itt.cmf.util.datatype;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.converter.TypeConverterProvider;

/**
 * Base class for {@link com.sirma.itt.emf.instance.model.Instance} to
 * {@link com.sirma.itt.emf.rest.model.RestInstance} converters
 */
public abstract class AbstractInstanceToRestInstanceConverterProvider implements
		TypeConverterProvider {

	/** The document service. */
	@Inject
	protected DocumentService documentService;

	/**
	 * Load content.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @return the string
	 */
	protected String loadContent(DocumentInstance documentInstance) {
		InputStream contentStream = documentService.getContentStream(documentInstance);
		if (contentStream != null) {
			try {
				return IOUtils.toString(contentStream, "UTF-8");
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	/**
	 * To string.
	 * 
	 * @param object
	 *            the serializable
	 * @return the string
	 */
	protected String convertString(Object object) {
		if (object == null) {
			return null;
		}
		return object.toString();
	}

}