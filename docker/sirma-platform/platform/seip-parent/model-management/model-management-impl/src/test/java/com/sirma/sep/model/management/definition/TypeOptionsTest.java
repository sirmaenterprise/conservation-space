package com.sirma.sep.model.management.definition;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link TypeOptions}
 * 
 * @author Stella Djulgerova
 */
public class TypeOptionsTest {

	@Test
	public void shouldResolveTypeOptionsCorrect() {
		String typeOption = TypeOptions.resolveTypeOption("an100", null);
		assertEquals("ALPHA_NUMERIC_FIXED_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("an..180", null);
		assertEquals("ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("any", null);
		assertEquals("ALPHA_NUMERIC_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("n..10,5", null);
		assertEquals("FLOATING_POINT_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("n8,2", null);
		assertEquals("FLOATING_POINT_FIXED_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("n..5", null);
		assertEquals("NUMERIC_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("n7", null);
		assertEquals("NUMERIC_FIXED_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("date", null);
		assertEquals("DATE_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("dateTime", null);
		assertEquals("DATETIME_TYPE", typeOption);
		typeOption = TypeOptions.resolveTypeOption("any", 4);
		assertEquals("CODELIST", typeOption);
		typeOption = TypeOptions.resolveTypeOption("boolean", null);
		assertEquals("BOOLEAN", typeOption);
		typeOption = TypeOptions.resolveTypeOption("uri", null);
		assertEquals("URI", typeOption);
	}
}
