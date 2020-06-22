package com.sirma.itt.seip.instance.script;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link CodelistScriptProvider}
 *
 * @author Valeri Tishev
 */
public class CodelistScriptProviderTest extends ScriptTest {

	private static final CodelistScriptProvider CUT = new CodelistScriptProvider();

	@Mock
	private CodelistService codelistServiceMock;

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);

		ReflectionUtils.setFieldValue(CUT, "codelistService", codelistServiceMock);
		bindingsExtensions.add(CUT);
	}

	/**
	 * Test getting codelist description.
	 */
	@Test
	public void testGettingCodelistDescription() {
		final Integer codelistNumber = 101;
		final String codelistValue = "SOME_VALUE";
		final String language = "en";
		final String expectedDescription = "Some codelist value decsription";

		when(codelistServiceMock.getDescription(codelistNumber, codelistValue, language))
				.thenReturn(expectedDescription);

		final String script = String.format("codelist.getDescription(%s, '%s', '%s')", codelistNumber, codelistValue,
				language);
		String actualDescription = (String) eval(script);
		assertEquals(actualDescription, expectedDescription);
	}

	/**
	 * Test getting codelist description with null arguments.
	 *
	 * @param codelistNumber
	 *            the codelist number
	 * @param codelistValue
	 *            the codelist value
	 * @param language
	 *            the language
	 */
	@Test(dataProvider = "nullArgumentsProvider", expectedExceptions = NullPointerException.class)
	public void testGettingCodelistDescriptionWithIllegalArguments(Integer codelistNumber, String codelistValue,
			String language) {

		final String script = String.format("codelist.getDescription(%s, %s, %s)", codelistNumber, codelistValue,
				language);

		eval(script);
	}

	@DataProvider
	private Object[][] nullArgumentsProvider() {
		return new Object[][] {

				{ null, null, null },

				{ 101, null, null }, { 101, "'SOME_CODE'", null }, { 101, null, "'bg'" },

				{ null, "'SOME_CODE'", null }, { 101, "'SOME_CODE'", null }, { null, "'SOME_CODE'", "'bg'" },

				{ null, null, "'bg'" }, { null, "'SOME_CODE'", "'bg'" }, { 101, null, "'bg'" }, };
	}

}
