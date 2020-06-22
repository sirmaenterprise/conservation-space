package com.sirma.itt.seip.template.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TemplateUtilsTest {

	@Test
	public void should_Remove_Whitespace_And_Special_Characters_For_Identifier() {
		String title = " title   &&&&  $№%$№%  123";
		String identifier = TemplateUtils.buildIdFromTitle(title);
		assertEquals("title123", identifier);
	}

	@Test
	public void should_Convert_Identifier_To_Lower_Case() {
		String title = " TiTle TeStInG   &&&&    123";
		String identifier = TemplateUtils.buildIdFromTitle(title);
		assertEquals("titletesting123", identifier);
	}

	@Test
	public void should_Allow_Unicode_Word_Characters_For_Identifier() {
		String title = "   Кирилица بِيَّة  汉字汉 **@@ 123 %$%$%$";
		String identifier = TemplateUtils.buildIdFromTitle(title);
		assertEquals("кирилицаبِيَّة汉字汉123", identifier);
	}
}
