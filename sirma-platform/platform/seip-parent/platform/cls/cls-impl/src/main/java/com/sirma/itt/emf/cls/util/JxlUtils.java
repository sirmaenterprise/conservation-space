package com.sirma.itt.emf.cls.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.output.ByteArrayOutputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Utility class providing common entry points and functionality for the java excel api
 * 
 * @author svetlozar.iliev
 */
public class JxlUtils {

	/**
	 * Utility class
	 */
	private JxlUtils() {
	}

	/**
	 * Get an existing workbook from a file
	 * 
	 * @param file
	 *            the file from which to get the workbook
	 * @return the workbook parsed from the file
	 * @throws BiffException
	 *             thrown when reading a biff file
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 */
	public static Workbook getWorkbook(File file) throws BiffException, IOException {
		return Workbook.getWorkbook(file, settings());
	}

	/**
	 * Get an existing workbook from an input stream
	 * 
	 * @param stream
	 *            the input stream from which to get the workbook
	 * @return the workbook parsed from the input stream
	 * @throws BiffException
	 *             thrown when reading a biff file
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 */
	public static Workbook getWorkbook(InputStream stream) throws BiffException, IOException {
		return Workbook.getWorkbook(stream, settings());
	}

	/**
	 * Creates an empty excel workbook
	 * 
	 * @return the empty excel workbook
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 */
	public static WritableWorkbook createWorkbook() throws IOException {
		return createWorkbook(new ByteArrayOutputStream());
	}

	/**
	 * Creates an empty excel workbook attached to a file which will be populated once the workbook has written to it
	 * 
	 * @return empty excel workbook
	 * @param file
	 *            the output file used to save the workbook to
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 */
	public static WritableWorkbook createWorkbook(File file) throws IOException {
		return Workbook.createWorkbook(file, settings());
	}

	/**
	 * Creates an empty excel workbook attached to an output stream which will be populated once the workbook has
	 * written to it
	 * 
	 * @return empty excel workbook
	 * @param stream
	 *            the output stream used to save the workbook to
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 */
	public static WritableWorkbook createWorkbook(OutputStream stream) throws IOException {
		return Workbook.createWorkbook(stream, settings());
	}

	/**
	 * Creates an empty sheet for a given workbook. The new sheet is appended as last and is given a random name
	 * 
	 * @param workbook
	 *            the workbook for which to create the sheet
	 * @return the created sheet
	 */
	public static WritableSheet createSheet(WritableWorkbook workbook) {
		return workbook.createSheet(UUID.randomUUID().toString().substring(0, 10), workbook.getSheets().length);
	}

	/**
	 * Imports an existing sheet to a given workbook. The sheet inserted at last position and it's original name is used
	 * 
	 * @param workbook
	 *            the workbook for which to import the new sheet
	 * @param sheet
	 *            the sheet to be imported
	 * @return the imported sheet
	 */
	public static WritableSheet insertSheet(WritableWorkbook workbook, Sheet sheet) {
		return workbook.importSheet(sheet.getName(), workbook.getSheets().length, sheet);
	}

	/**
	 * Performs a hard write and persist for a given workbook writing and possibly closing all streams related to this
	 * workbook. This will make the workbook eligible for garbage collection and will flush and close all streams used
	 * by the workbook effectively saving the content of the workbook
	 * 
	 * @param workbook
	 *            the workbook which is to be persisted
	 * @throws IOException
	 *             thrown when unable to communicate with IO streams
	 * @throws WriteException
	 *             thrown when unable to write or flush related streams
	 */
	public static void persist(WritableWorkbook workbook) throws IOException, WriteException {
		workbook.write();
		workbook.close();
	}

	/**
	 * Creates a generic settings object for a workbook. Workbooks use this settings file merely as a suggestion when
	 * operating with a {@link Workbook}. Documentation specifies that usually using any specific settings are not
	 * required, but it is good practice to be verbose, any additional settings might be provided directly inside this
	 * method
	 * 
	 * @return the workbook settings
	 */
	private static WorkbookSettings settings() {
		WorkbookSettings settings = new WorkbookSettings();
		settings.setEncoding("Cp1252");
		return settings;
	}
}
