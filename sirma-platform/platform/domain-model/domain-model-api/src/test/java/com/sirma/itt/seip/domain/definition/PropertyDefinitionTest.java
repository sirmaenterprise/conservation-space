package com.sirma.itt.seip.domain.definition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NOT_USED_PROPERTY_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Test for the default/static methods in {@link PropertyDefinition} interface
 *
 * @author BBonev
 */
public class PropertyDefinitionTest {

	@Test
	public void testHasValuePredicate() throws Exception {
		Predicate<PropertyDefinition> hasValue = PropertyDefinition.hasValue();
		PropertyDefinition property = mock(PropertyDefinition.class);
		assertFalse(hasValue.test(property));
		when(property.getDefaultValue()).thenReturn("", "  ", "some value");
		assertFalse(hasValue.test(property));
		assertFalse(hasValue.test(property));
		assertTrue(hasValue.test(property));
	}

	@Test
	public void testHasControlPredicate() throws Exception {
		Predicate<PropertyDefinition> hasControl = PropertyDefinition.hasControl("control_id");
		PropertyDefinition property = mock(PropertyDefinition.class);
		assertFalse(hasControl.test(property));

		ControlDefinition control = mock(ControlDefinition.class);
		when(property.getControlDefinition()).thenReturn(control);

		when(control.getIdentifier()).thenReturn(null, "", "   ", "someOtherControl", "control_id", "CONTROL_ID",
				"control_ID");
		assertFalse(hasControl.test(property));
		assertFalse(hasControl.test(property));
		assertFalse(hasControl.test(property));
		assertFalse(hasControl.test(property));

		assertTrue(hasControl.test(property));
		assertTrue(hasControl.test(property));
		assertTrue(hasControl.test(property));
	}

	@Test
	public void testByNamePredicate() throws Exception {
		Predicate<PropertyDefinition> byName = PropertyDefinition.hasName("fieldName");

		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getIdentifier()).thenReturn(null, "", "   ", "someOtherName", "fieldName");
		assertFalse(byName.test(property));
		assertFalse(byName.test(property));
		assertFalse(byName.test(property));
		assertFalse(byName.test(property));

		assertTrue(byName.test(property));
	}

	@Test
	public void testByUriPredicate() throws Exception {
		Predicate<PropertyDefinition> byUri = PropertyDefinition.hasUri("emf:some-uri");

		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(null, "", "   ", "emf:some-other-name", "emf:some-uri");
		assertFalse(byUri.test(property));
		assertFalse(byUri.test(property));
		assertFalse(byUri.test(property));
		assertFalse(byUri.test(property));

		assertTrue(byUri.test(property));
	}

	@Test
	public void testByDisplayTypePredicate_single() throws Exception {
		Predicate<PropertyDefinition> byDisplayTpe = PropertyDefinition.byDisplayType(DisplayType.EDITABLE);

		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getDisplayType()).thenReturn(null, DisplayType.HIDDEN, DisplayType.EDITABLE);
		assertFalse(byDisplayTpe.test(property));
		assertFalse(byDisplayTpe.test(property));

		assertTrue(byDisplayTpe.test(property));
	}

	@Test
	public void testByDisplayTypePredicate_multiple() throws Exception {
		Predicate<PropertyDefinition> byDisplayTpe = PropertyDefinition.byDisplayType(DisplayType.EDITABLE,
				DisplayType.HIDDEN);

		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getDisplayType()).thenReturn(null, DisplayType.SYSTEM, DisplayType.HIDDEN, DisplayType.EDITABLE);
		assertFalse(byDisplayTpe.test(property));
		assertFalse(byDisplayTpe.test(property));

		assertTrue(byDisplayTpe.test(property));
		assertTrue(byDisplayTpe.test(property));
	}

	@Test
	public void byType_notEquals() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		Predicate<PropertyDefinition> predicate = PropertyDefinition.hasType(DataTypeDefinition.LONG);
		assertFalse(predicate.test(property));
	}

	@Test
	public void byType_equals() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		Predicate<PropertyDefinition> predicate = PropertyDefinition.hasType(DataTypeDefinition.TEXT);
		assertTrue(predicate.test(property));
	}

	@Test
	public void byType_null() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(null);
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		Predicate<PropertyDefinition> predicate = PropertyDefinition.hasType(DataTypeDefinition.LONG);
		assertFalse(predicate.test(property));
	}

	@Test
	public void resolveUri_uriIdentidier() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getIdentifier()).thenReturn("uri:identifier");
		Function<PropertyDefinition, String> function = PropertyDefinition.resolveUri();
		assertEquals("uri:identifier", function.apply(property));
	}

	@Test
	public void resolveUri_forbiddenField() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getIdentifier()).thenReturn("identifier");
		when(property.getUri()).thenReturn(DefaultProperties.NOT_USED_PROPERTY_VALUE);
		Function<PropertyDefinition, String> function = PropertyDefinition.resolveUri();
		assertNull("uri:identifier", function.apply(property));
	}

	@Test
	public void resolveUri_fieldWithUri() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getIdentifier()).thenReturn("identifier");
		when(property.getUri()).thenReturn("uri");
		Function<PropertyDefinition, String> function = PropertyDefinition.resolveUri();
		assertEquals("uri", function.apply(property));
	}

	@Test
	public void isObjectProperty_true() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.URI);
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(property.getDisplayType()).thenReturn(DisplayType.EDITABLE);

		Predicate<PropertyDefinition> predicate = PropertyDefinition.isObjectProperty();
		assertTrue(predicate.test(property));
	}

	@Test
	public void isObjectProperty_notUri() {
		PropertyDefinition property = mock(PropertyDefinition.class);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(property.getDisplayType()).thenReturn(DisplayType.EDITABLE);

		Predicate<PropertyDefinition> predicate = PropertyDefinition.isObjectProperty();
		assertFalse(predicate.test(property));
	}

	@Test
	public void shouldReturnTrueOnValidType() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getDmsType()).thenReturn("cm:name");
		assertTrue(PropertyDefinition.hasDmsType().test(property));
	}

	@Test
	public void shouldReturnFalseOnInvalidType() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getDmsType()).thenReturn(NOT_USED_PROPERTY_VALUE);
		assertFalse(PropertyDefinition.hasDmsType().test(property));
	}

	@Test
	public void hasCodelist_shouldReturnTrueOnPositiveNumber() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getCodelist()).thenReturn(Integer.valueOf(2));
		assertTrue(PropertyDefinition.hasCodelist().test(property));
	}

	@Test
	public void hasCodelist_shouldReturnFalseOnNegativeNumber() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getCodelist()).thenReturn(Integer.valueOf(-2));
		assertFalse(PropertyDefinition.hasCodelist().test(property));
	}

	@Test
	public void hasCodelist_shouldReturnFalseOnMissingNumber() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		assertFalse(PropertyDefinition.hasCodelist().test(property));
	}

	@Test
	public void hasUri_shouldReturnTrueOnNameWithCollon() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn("rdf:type");
		assertTrue(PropertyDefinition.hasUri().test(property));
	}

	@Test
	public void hasUri_shouldReturnTrueOnValidUriField() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn("type");
		when(property.getUri()).thenReturn("rdf:type");
		assertTrue(PropertyDefinition.hasUri().test(property));
	}

	@Test
	public void hasUri_shouldReturnFalseOnForbiddenUriFieldAndNonUriName() throws Exception {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn("type");
		when(property.getUri()).thenReturn(NOT_USED_PROPERTY_VALUE);
		assertFalse(PropertyDefinition.hasUri().test(property));
	}
}
