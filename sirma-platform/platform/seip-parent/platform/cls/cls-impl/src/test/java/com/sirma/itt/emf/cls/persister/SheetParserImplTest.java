package com.sirma.itt.emf.cls.persister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sirma.itt.emf.cls.validator.SheetValidator;
import com.sirma.itt.emf.cls.validator.SheetValidatorImpl;
import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;

import jxl.Cell;
import jxl.Sheet;

/**
 * Testing the {@link SheetParserImpl}'s parsing capabilites.
 *
 * @author Nikolay Velkov
 */
public class SheetParserImplTest {

	private SheetParserImpl parser = new SheetParserImpl();

	/** The code list ids. */
	private String[] codeListIds = new String[] { "1", "2", "3", "4", "5", "6", "7", "101", "102", "106", "200", "208",
			"210", "215", "227", "229", "234", "237", "238" };

	/**
	 * Test the parse XLS method of the {@link SheetValidator}. Compares each
	 *
	 * @throws SheetValidatorException
	 *             if any of the code list id's is not found in the codeListIds array {@link CodeList}'s id with the
	 *             ones in the codeListIds array. Uses the valid-codelsits.xls from the test files
	 */
	@Test
	public void testParseFromXLS() throws SheetValidatorException {
		SheetValidator validator = new SheetValidatorImpl();
		InputStream is = getTestFileAsStream("valid-codelists.xls");
		Sheet sheet = validator.getValidatedCodeListSheet(is);
		List<CodeList> codeLists = parser.parseFromSheet(sheet).getCodeLists();
		for (CodeList codeList : codeLists) {
			boolean isPresent = false;
			for (String id : codeListIds) {
				if (codeList.getValue().equals(id)) {
					isPresent = true;
				}
			}
			if (!isPresent) {
				fail("code list with value " + codeList.getValue() + " is not present ");
			}
		}
	}

	@Test
	public void testParseToXLS() throws SheetValidatorException {
		List<CodeList> codeLists = Arrays.asList(getCodeList("1"), getCodeList("2"));

		// append the test code values to the first code list
		codeLists.get(0).getValues().add(getCodeValue("1", true));
		codeLists.get(0).getValues().add(getCodeValue("2", true));
		codeLists.get(0).getValues().add(getCodeValue("cl01", false));
		codeLists.get(0).getValues().add(getCodeValue("CL02", false));
		// append the test code values to the second code list
		codeLists.get(1).getValues().add(getCodeValue("CL10", false));
		codeLists.get(1).getValues().add(getCodeValue("cl11", false));
		codeLists.get(1).getValues().add(getCodeValue("CL12", false));

		CodeListSheet codeSheet = new CodeListSheet();
		codeSheet.setCodeLists(codeLists);
		Sheet sheet = parser.parseFromList(codeSheet);

		Cell[] columns = sheet.getRow(0);
		// validate that the column names are correct
		assertEquals("Code", columns[0].getContents());
		assertEquals("extra1", columns[1].getContents());
		assertEquals("extra2", columns[2].getContents());
		assertEquals("extra3", columns[3].getContents());
		assertEquals("Active", columns[4].getContents());

		assertEquals("descrEN", columns[5].getContents());
		assertEquals("descrFR", columns[6].getContents());
		assertEquals("descrBG", columns[7].getContents());
		assertEquals("descrDE", columns[8].getContents());
		assertEquals("descrRO", columns[9].getContents());

		assertEquals("CommentEN", columns[10].getContents());
		assertEquals("CommentFR", columns[11].getContents());
		assertEquals("CommentBG", columns[12].getContents());
		assertEquals("CommentDE", columns[13].getContents());
		assertEquals("CommentRO", columns[14].getContents());

		// validate that the contents of the sheet are correct based on the list
		for (int codeList = 0, row = 1; codeList < codeLists.size(); codeList++) {
			Cell[] codeListRow = sheet.getRow(row);
			CodeList codeListElem = codeLists.get(codeList);

			assertEquals(codeListElem.getValue(), codeListRow[0].getContents());
			assertEquals(codeListElem.getExtra1(), codeListRow[1].getContents());
			assertEquals(codeListElem.getExtra2(), codeListRow[2].getContents());
			assertEquals(codeListElem.getExtra3(), codeListRow[3].getContents());
			assertEquals("", codeListRow[4].getContents()); // must be empty cell

			int col = 5; // since code values have the EN descriptions
			for (CodeDescription description : codeListElem.getDescriptions()) {
				assertEquals(description.getName(), codeListRow[col++].getContents());
			}
			++col; // skip one column which represents the RO description
			for (CodeDescription description : codeListElem.getDescriptions()) {
				assertEquals(description.getComment(), codeListRow[col++].getContents());
			}

			for (CodeValue codeValue : codeListElem.getValues()) {
				Cell[] codeValueRow = sheet.getRow(++row);
				String isCodeValueActive = String.valueOf(codeValue.isActive());

				assertEquals(codeValue.getValue(), codeValueRow[0].getContents());
				assertEquals(codeValue.getExtra1(), codeValueRow[1].getContents());
				assertEquals(codeValue.getExtra2(), codeValueRow[2].getContents());
				assertEquals(codeValue.getExtra3(), codeValueRow[3].getContents());
				assertEquals(isCodeValueActive, codeValueRow[4].getContents());

				col = 6; // since code values have no EN descriptions
				for (CodeDescription description : codeValue.getDescriptions()) {
					assertEquals(description.getName(), codeValueRow[col++].getContents());
				}
				++col; // since code values have no EN comments present
				for (CodeDescription description : codeValue.getDescriptions()) {
					assertEquals(description.getComment(), codeValueRow[col++].getContents());
				}
			}
			row += 2; // skip the next row since it has noting populated in it
		}
	}

	@Test
	public void testParseXLSVariousLanguages() throws SheetValidatorException, IOException {
		SheetValidator validator = new SheetValidatorImpl();
		InputStream is = getTestFileAsStream("multilang/test-codelists.xls");
		Sheet sheet = validator.getValidatedCodeListSheet(is);
		List<CodeList> codeLists = parser.parseFromSheet(sheet).getCodeLists();

		List<String> expectedExtra2 = IOUtils.readLines(getTestFileAsStream("multilang/expected.txt"));

		CodeList codeList = codeLists.get(0);

		List<CodeDescription> clDescriptions = codeList.getDescriptions();

		CodeDescription bgCodelistDescription = clDescriptions.get(0);
		assertEquals("BG", bgCodelistDescription.getLanguage());
		assertEquals("Material", bgCodelistDescription.getName());
		assertEquals("Comment bg", bgCodelistDescription.getComment());

		CodeDescription enCodelistDescription = clDescriptions.get(1);
		assertEquals("EN", enCodelistDescription.getLanguage());
		assertEquals("Language", enCodelistDescription.getName());
		assertEquals("Comment en", enCodelistDescription.getComment());

		List<CodeValue> codeValues = codeList.getValues();

		for (int i = 0; i < codeValues.size(); i++) {
			CodeValue current = codeValues.get(i);

			assertEquals(expectedExtra2.get(i), current.getExtra2());
		}

		List<CodeDescription> descriptions = codeValues.get(0).getDescriptions();

		CodeDescription bgDescription = descriptions.get(0);
		assertEquals("BG", bgDescription.getLanguage());
		assertEquals("Display", bgDescription.getName());
		assertEquals("Display comment", bgDescription.getComment());

		CodeDescription enDescription = descriptions.get(1);
		assertEquals("EN", enDescription.getLanguage());
		assertEquals("Dutch - Display", enDescription.getName());
		assertEquals("Dutch display comment", enDescription.getComment());
	}

	/**
	 * Returns a file as input stream based on its path after this class's upper package.
	 *
	 * @param filePath
	 *            the file's path
	 * @return the file as stream
	 */
	private static InputStream getTestFileAsStream(String filePath) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sirma/itt/emf/cls/" + filePath);
	}

	private static CodeList getCodeList(String value) {
		CodeList codeList = new CodeList();
		codeList.setValue(value);
		codeList.setExtra1("ListExtra 1");
		codeList.setExtra2("ListExtra 2");
		codeList.setExtra3("ListExtra 3");
		codeList.setValues(new ArrayList<>());
		codeList.setDescriptions(
				Arrays.asList(getDescription("EN"), getDescription("FR"), getDescription("BG"), getDescription("DE")));
		return codeList;
	}

	private static CodeValue getCodeValue(String value, boolean active) {
		CodeValue codeValue = new CodeValue();
		codeValue.setActive(active);
		codeValue.setValue(value);
		codeValue.setExtra1("ValueExtra 1");
		codeValue.setExtra2("ValueExtra 2");
		codeValue.setExtra3("ValueExtra 3");
		codeValue.setDescriptions(
				Arrays.asList(getDescription("FR"), getDescription("BG"), getDescription("DE"), getDescription("RO")));
		return codeValue;
	}

	private static CodeDescription getDescription(String language) {
		CodeDescription descr = new CodeDescription();
		descr.setLanguage(language);
		descr.setComment("Comment" + language);
		descr.setName("Description" + language);
		return descr;
	}
}
