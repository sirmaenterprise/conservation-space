package com.sirma.itt.emf.cls.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;

import jxl.Sheet;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the methods of {@link XLSValidator}.
 * 
 * @author Mihail Radkov
 */
public class XLSValidatorImplTest {

	private XLSValidator validator;

	/**
	 * Executed before every method annotated with @Test.
	 */
	@Before
	public void before() {
		validator = new XLSValidatorImpl();
	}

	/**
	 * Performs null test - gives null value as parameter.
	 * 
	 * @throws XLSValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = XLSValidatorException.class)
	public void nullTest() throws XLSValidatorException {
		validator.getValidatedCodeListSheet(null);
		fail();
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with code lists
	 * and values.
	 */
	@Test
	public void validFileTest() {
		Sheet sheet = null;
		InputStream testFile = getTestFile("valid-codelists.xls");
		try {
			sheet = validator.getValidatedCodeListSheet(testFile);
			assertNotNull(sheet);
		} catch (XLSValidatorException e) {
			fail();
		}
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of invalid XLS file with code lists
	 * and values.
	 * 
	 * @throws XLSValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = XLSValidatorException.class)
	public void invalidFileTest() throws XLSValidatorException {
		InputStream testFile = getTestFile("invalid-codelists.xls");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with empty code
	 * lists and values sheet.
	 * 
	 * @throws XLSValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = XLSValidatorException.class)
	public void emptyFileTest() throws XLSValidatorException {
		InputStream testFile = getTestFile("empty-codelists.xls");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with invalid
	 * sheets.
	 * 
	 * @throws XLSValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = XLSValidatorException.class)
	public void emptySheetTest() throws XLSValidatorException {
		InputStream testFile = getTestFile("empty-sheet.xls");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * * Tests the validation by providing an {@link InputStream} of invalid file.
	 * 
	 * @throws XLSValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = XLSValidatorException.class)
	public void incorrectFileTest() throws XLSValidatorException {
		InputStream testFile = getTestFile("incorrect-file.test");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * Returns a file as input stream based on its path.
	 * 
	 * @param filePath
	 *            the file's path
	 * @return the file as stream
	 */
	private InputStream getTestFile(String filePath) {
		return XLSValidatorImplTest.class.getResourceAsStream("../" + filePath);
	}
}
