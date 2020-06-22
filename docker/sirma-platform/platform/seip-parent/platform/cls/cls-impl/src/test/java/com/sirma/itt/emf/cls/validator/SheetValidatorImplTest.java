package com.sirma.itt.emf.cls.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;

import jxl.Sheet;

/**
 * Tests the methods of {@link SheetValidator}.
 *
 * @author Mihail Radkov
 */
public class SheetValidatorImplTest {

	private SheetValidator validator;

	/**
	 * Executed before every method annotated with @Test.
	 */
	@Before
	public void before() {
		validator = new SheetValidatorImpl();
	}

	/**
	 * Performs null test - gives null value as parameter.
	 *
	 * @throws SheetValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = SheetValidatorException.class)
	public void nullTest() throws SheetValidatorException {
		validator.getValidatedCodeListSheet(null);
		fail();
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with code lists and values.
	 */
	@Test
	public void validFileTest() {
		Sheet sheet = null;
		InputStream testFile = getTestFile("valid-codelists.xls");
		try {
			sheet = validator.getValidatedCodeListSheet(testFile);
			assertNotNull(sheet);
		} catch (SheetValidatorException e) {
			fail();
		}
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with empty code lists and values
	 * sheet.
	 *
	 * @throws SheetValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = SheetValidatorException.class)
	public void emptyFileTest() throws SheetValidatorException {
		InputStream testFile = getTestFile("empty-codelists.xls");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * Tests the validation by providing an {@link InputStream} of valid XLS file with invalid sheets.
	 *
	 * @throws SheetValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = SheetValidatorException.class)
	public void emptySheetTest() throws SheetValidatorException {
		InputStream testFile = getTestFile("empty-sheet.xls");
		validator.getValidatedCodeListSheet(testFile);
		fail();
	}

	/**
	 * * Tests the validation by providing an {@link InputStream} of invalid file.
	 *
	 * @throws SheetValidatorException
	 *             if the validator cannot validate the provided sheet
	 */
	@Test(expected = SheetValidatorException.class)
	public void incorrectFileTest() throws SheetValidatorException {
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
		return SheetValidatorImplTest.class.getResourceAsStream("../" + filePath);
	}
}
