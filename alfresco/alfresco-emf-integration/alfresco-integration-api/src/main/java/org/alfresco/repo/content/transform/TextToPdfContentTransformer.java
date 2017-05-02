/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of
 * Alfresco Alfresco is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. Alfresco is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.transform;

import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.io.IOUtils;

import com.itextpdf.text.Document;

/**
 * Uses IText to convert text to pdf.
 * 
 * @author Derek Hulley
 * @author bbanchev
 * @since 2.1.0
 */
public class TextToPdfContentTransformer extends AbstractContentTransformer2 {

	/** The transformer. */
	private ITextPDFWorker transformer;

	/**
	 * creates new delegate transformer.
	 */
	public TextToPdfContentTransformer() {
		transformer = new ITextPDFWorker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#
	 * getTransformationTime()
	 */
	/**
	 * Gets the transformation time.
	 *
	 * @return the transformation time
	 */
	@Override
	public synchronized long getTransformationTime() {
		return 0l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.content.transform.ContentTransformer#isTransformable
	 * (java.lang.String, java.lang.String,
	 * org.alfresco.service.cmr.repository.TransformationOptions)
	 */
	/**
	 * Checks if is transformable.
	 *
	 * @param sourceMimetype the source mimetype
	 * @param targetMimetype the target mimetype
	 * @param options the options
	 * @return true, if is transformable
	 */
	@Override
	public boolean isTransformable(String sourceMimetype, String targetMimetype,
			TransformationOptions options) {
		if ((!MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) && //
				!MimetypeMap.MIMETYPE_TEXT_CSV.equals(sourceMimetype) && //
				!MimetypeMap.MIMETYPE_XML.equals(sourceMimetype))//
				|| !MimetypeMap.MIMETYPE_PDF.equals(targetMimetype)) {
			// only support (text/plain OR text/csv OR text/xml) to
			// (application/pdf)
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#
	 * transformInternal(org.alfresco.service.cmr.repository.ContentReader,
	 * org.alfresco.service.cmr.repository.ContentWriter,
	 * org.alfresco.service.cmr.repository.TransformationOptions)
	 */
	/**
	 * Transform internal.
	 *
	 * @param reader the reader
	 * @param writer the writer
	 * @param options the options
	 * @throws Exception the exception
	 */
	@Override
	protected void transformInternal(ContentReader reader, ContentWriter writer,
			TransformationOptions options) throws Exception {
		Document pdf = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			is = reader.getContentInputStream();
			os = writer.getContentOutputStream();
			pdf = transformer.createPDFFromText(is, reader.getEncoding(), os);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				pdf = transformer.createEmptyPDF(os);
			} catch (Exception e2) {
				// skip
			}
		} finally {
			if (pdf != null) {
				try {
					pdf.close();
				} catch (Throwable e) {
					// skip
				}
			}
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);

		}
	}
}
