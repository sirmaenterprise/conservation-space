package com.sirma.itt.seip.instance.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Test for {@link DynamicCodeListFilter}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class DynamicCodeListFilterTest {

	private final DynamicCodeListFilter filter = new DynamicCodeListFilter();

	@Test
	public void isFilterValid_true() throws Exception {
		filter.setValues(Collections.singletonList("CL_VALUE"));
		filter.setReRenderFieldName("some_field");
		filter.setFilterSource("source_field");
		filter.setInclusive(Boolean.TRUE);
		assertTrue(filter.isFilterValid());
	}

	@Test
	public void isFilterValid_missingValue() throws Exception {
		filter.setReRenderFieldName("some_field");
		filter.setFilterSource("source_field");
		filter.setInclusive(Boolean.TRUE);
		assertFalse(filter.isFilterValid());
	}

	@Test
	public void isFilterValid_missingReRenderField() throws Exception {
		filter.setValues(Collections.singletonList("CL_VALUE"));
		filter.setFilterSource("source_field");
		filter.setInclusive(Boolean.TRUE);
		assertFalse(filter.isFilterValid());
	}

	@Test
	public void isFilterValid_missingSourceField() throws Exception {
		filter.setValues(Collections.singletonList("CL_VALUE"));
		filter.setReRenderFieldName("some_field");
		filter.setInclusive(Boolean.TRUE);
		assertFalse(filter.isFilterValid());
	}

	@Test
	public void isFilterValid_missingIsInclusive() throws Exception {
		filter.setValues(Arrays.asList("CL_VALUE1", "CL_VALUE2"));
		filter.setFilterSource("source_field");
		filter.setReRenderFieldName("some_field");
		assertFalse(filter.isFilterValid());
	}
}