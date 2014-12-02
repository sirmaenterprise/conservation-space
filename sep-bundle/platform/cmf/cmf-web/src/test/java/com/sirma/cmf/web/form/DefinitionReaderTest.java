package com.sirma.cmf.web.form;

import org.testng.annotations.Test;

/**
 * Test for definition reader.
 * 
 * @author svelikov
 */
@Test
public class DefinitionReaderTest {

	protected final DefinitionReaderBase reader;

	/**
	 * Constructor.
	 */
	public DefinitionReaderTest() {
		reader = new DefinitionReader() {
			@Override
			public String getMaxLengthForSingleLineField() {
				return "40";
			}
		};

	}

	/**
	 * Test if all builder types are properly returned if requested.
	 */
	// FIXME: test
	public void getAllBuilderTypesTest() {
		// BuilderType[] builderTypes = BuilderType.values();
		//
		// for (BuilderType type : builderTypes) {
		// FormBuilder builder = reader.initBuilder(type, null, null,
		// FormViewMode.EDIT);
		//
		// // check if builder is returned
		// Assert.assertNotNull(builder);
		//
		// // check if the builder type is of requested type
		// Assert.assertEquals(builder.getBuilderType(), type);
		// }
		//
		// // check if all builders are stored in the builders pool
		// Assert.assertEquals(reader.getBuilders().size(),
		// builderTypes.length);

	}

	/**
	 * Test if a builder is properly initialized and returned.
	 */
	// FIXME: test
	public void getBuilderTest() {

		// FormBuilder builder =
		// reader.initBuilder(BuilderType.SINGLE_LINE_FIELD_BUILDER, null, null,
		// FormViewMode.EDIT);
		//
		// // check if builder is returned
		// Assert.assertNotNull(builder);
		//
		// // component
		// Assert.assertNull(builder.getContainer());
		// Assert.assertNull(builder.getPropertyDefinition());
		//
		// UIComponent container = new HtmlPanelGroup();
		// FieldDefinition fieldDefinition = new FieldDefinition();
		// builder = reader.initBuilder(BuilderType.SINGLE_LINE_FIELD_BUILDER,
		// container,
		// fieldDefinition, FormViewMode.EDIT);
		//
		// // check if component is properly set in the builder
		// Assert.assertNotNull(builder.getContainer());
		//
		// // check if definition is properly set in the builder
		// Assert.assertNotNull(builder.getPropertyDefinition());

	}
}
