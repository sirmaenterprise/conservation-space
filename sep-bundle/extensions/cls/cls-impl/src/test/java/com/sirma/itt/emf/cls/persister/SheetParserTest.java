package com.sirma.itt.emf.cls.persister;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import jxl.Sheet;

import org.junit.Test;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.service.CodeListServiceImplTest;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorException;
import com.sirma.itt.emf.cls.validator.XLSValidatorImpl;

// TODO: Auto-generated Javadoc
/**
 * Testing the {@link SheetParser}'s parsing capabilites.
 * @author Nikolay Velkov
 */
public class SheetParserTest {

	/** The code list ids. */
	String[] codeListIds = new String[] { "1", "2", "3", "4", "5", "6", "7",
			"101", "102", "106", "200", "208", "210", "215", "227", "229",
			"234", "237", "238" };

	/**
	 * Test the parse XLS method of the {@link XLSValidator}. Compares each
	 * 
	 * @throws XLSValidatorException
	 *             if any of the code list id's is not found in the codeListIds
	 *             array {@link CodeList}'s id with the ones in the codeListIds
	 *             array. Uses the valid-codelsits.xls from the test files.
	 */
	@Test
	public void testParseXLS() throws XLSValidatorException {
		XLSValidator validator = new XLSValidatorImpl();
		InputStream is = getTestFileAsStream("valid-codelists.xls");
		Sheet sheet = validator.getValidatedCodeListSheet(is);
		SheetParser parser = new SheetParser();
		List<CodeList> codeLists = parser.parseXLS(sheet);
		for (CodeList codeList : codeLists) {
			boolean isPresent = false;
			for (String id : codeListIds) {
				if (codeList.getValue().equals(id)) {
					isPresent = true;
				}
			}
			if (!isPresent) {
				fail("code list with value " + codeList.getValue()
						+ " is not present ");
			}
		}
	}

	/**
	 * Returns a file as input stream based on its path after this class's upper
	 * package.
	 * 
	 * @param filePath
	 *            the file's path
	 * @return the file as stream
	 */
	private InputStream getTestFileAsStream(String filePath) {
		return CodeListServiceImplTest.class
				.getResourceAsStream("../" + filePath);
	}
}
