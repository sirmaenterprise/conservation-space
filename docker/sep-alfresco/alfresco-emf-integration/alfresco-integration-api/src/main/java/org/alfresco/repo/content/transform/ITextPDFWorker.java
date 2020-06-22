/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package org.alfresco.repo.content.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Using IText convert text to pdf.
 *
 * @author bbanchev
 */
public class ITextPDFWorker {
	private static final int MAX_LINE_SIZE = 5000;
	/** font file name. */
	private static final String ARIAL_TTF = "ARIALUN0.TTF";
	/** the base font. */
	public static final BaseFont BASE_FONT = getBaseFont();

	/** The Constant DEFAULT_FONT_SIZE. */
	private static final int DEFAULT_FONT_SIZE = 9;
	/** font file name. */
	protected Font textFont = new Font(BASE_FONT, DEFAULT_FONT_SIZE, Font.NORMAL);

	/**
	 * Obtains the base arial unicode font, contained in the war.
	 *
	 * @return the created and registered base font
	 */
	private static BaseFont getBaseFont() {
		try {
			InputStream resourceAsStream = ITextPDFWorker.class.getResourceAsStream(ARIAL_TTF);
			File tempDir = TempFileProvider.getSystemTempDir();
			File tempFontFile = new File(tempDir, ARIAL_TTF);
			if (!tempFontFile.exists() || !tempFontFile.canRead()) {
				FileOutputStream fileOutputStream = new FileOutputStream(
						tempFontFile.getAbsoluteFile());
				IOUtils.copy(resourceAsStream, fileOutputStream);
				IOUtils.closeQuietly(fileOutputStream);
			}
			FontFactory.register(tempFontFile.getAbsolutePath());
			return BaseFont.createFont(tempFontFile.getAbsolutePath(), BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				return BaseFont.createFont();
			} catch (Exception e1) {
				// shouldn't occur ever
				e1.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Creates pdf from input stream that contains text.
	 *
	 * @param text
	 *            is the input stream - should character stream
	 * @param encoding
	 *            the encoding
	 * @param output
	 *            is the output stream to store pdf in
	 * @return the created pdf or null on exception
	 * @throws DocumentException
	 *             on error while adding data
	 * @throws IOException
	 *             on read/write exception
	 */
	public Document createPDFFromText(InputStream text, String encoding, OutputStream output)
			throws DocumentException, IOException {
		Document doc = null;
		try {

			// new pdf
			doc = new Document(PageSize.A4);
			// it is needed
			PdfWriter.getInstance(doc, output);
			doc.open();
			// get utf8 reader
			BufferedReader data = new BufferedReader(new InputStreamReader(text, encoding));
			String nextLine = null;
			while ((nextLine = data.readLine()) != null) {
				if (nextLine.isEmpty()) {
					// add the empty rows
					nextLine = " \r\n";
				} else if (nextLine.length() > MAX_LINE_SIZE) {
					// add the empty rows
					nextLine = nextLine.substring(0, 5000) + "...Text too long... \r\n";
				}
				Paragraph element = new Paragraph(nextLine, textFont);
				element.setFirstLineIndent(getTrimSize(nextLine) * DEFAULT_FONT_SIZE);
				doc.add(element);
			}
			return doc;

		} finally {
			// if (writer != null) {
			// writer.close();
			// }
		}
	}

	/**
	 * Creates pdf that is empty.
	 *
	 * @param output
	 *            is the output stream to store pdf in
	 * @return the created pdf or null on exception
	 * @throws DocumentException
	 *             on error while adding data
	 */
	public Document createEmptyPDF(OutputStream output) throws DocumentException {
		Document doc = null;

		// new pdf
		doc = new Document(PageSize.A4);
		// it is needed
		PdfWriter.getInstance(doc, output);
		doc.open();
		return doc;

	}

	/**
	 * Calculates the trailing spaces for a string.
	 *
	 * @param stringWithTrailingSpaces
	 *            is the string containing or not trailing spaces
	 * @return the count of spaces (tab = 4 spaces)
	 */
	public int getTrimSize(String stringWithTrailingSpaces) {
		try {
			char[] val = stringWithTrailingSpaces.toCharArray();
			int len = val.length;
			int index = 0;
			int count = 0;
			while (index < len) {
				char c = val[index];
				if (c > 32) {
					break;
				}
				if (c == 9) {
					count += 4;
				} else if (c > 27) {
					count++;
				}
				index++;
			}
			return count;
		} catch (Exception e) {
			// skip
		}
		return 0;
	}

	/**
	 * Set new font size.
	 *
	 * @param size
	 *            is the size - default is {@value #DEFAULT_FONT_SIZE}
	 */
	public void setFontSize(int size) {
		textFont = new Font(BASE_FONT, size, Font.NORMAL);
	}
}
