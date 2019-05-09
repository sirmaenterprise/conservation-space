package com.sirma.sep.model.management;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link ModelDefinitionTest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/11/2018
 */
public class ModelDefinitionTest {

	public static final String FIELD_ID = "someField";

	@Test
	public void modelFields_shouldbeLinkedWithTheirParentAndChildFields() throws Exception {
		ModelDefinition root = createDefinition("root", null);
		ModelDefinition level0 = createDefinition("level_0", root);
		ModelDefinition level1 = createDefinition("level_1", level0);
		ModelDefinition level2_1 = createDefinition("level_2_1",level1);
		ModelDefinition level2_2 = createDefinition("level_2_2", level1);
		ModelDefinition level3_1 = createDefinition("level_3_1", level2_1);
		ModelDefinition level3_2 = createDefinition("level_3_2", level2_1);
		ModelDefinition level3_3 = createDefinition("level_3_3", level2_2);
		ModelDefinition level4 = createDefinition("level_4", level3_2);

		ModelField field = createField();
		root.addField(field);
		ModelField field1 = createField();
		level1.addField(field1);
		ModelField field2 = createField();
		level2_1.addField(field2);
		ModelField field3 = createField();
		level3_1.addField(field3);
		ModelField field4 = createField();
		level4.addField(field4);
		ModelField field5 = createField();
		level3_3.addField(field5);

		// verify the structure is valid
		assertNull(field.getParentReference());
		assertSame(field1.getParentReference(), field);
		assertSame(field2.getParentReference(), field1);
		assertSame(field5.getParentReference(), field1);
		assertSame(field3.getParentReference(), field2);

		// after field removal the parent of field 2 and 5 should be changed
		level1.removeField(FIELD_ID);
		assertSame(field2.getParentReference(), field);
		assertSame(field5.getParentReference(), field);
		assertSame(field3.getParentReference(), field2);

		// second field removal should not change the state
		level1.removeField(FIELD_ID);
		assertSame(field2.getParentReference(), field);
		assertSame(field5.getParentReference(), field);
		assertSame(field3.getParentReference(), field2);

		// removing the top level parent should clear the first level child references
		root.removeField(FIELD_ID);
		assertNull(field2.getParentReference());
		assertNull(field5.getParentReference());
		assertSame(field3.getParentReference(), field2);
	}

	private ModelDefinition createDefinition(String id, ModelDefinition parent) {
		ModelDefinition definition = new ModelDefinition().setId(id);
		if (parent != null) {
			definition.setParent(parent.getId());
			parent.addChild(definition);
		}
		return definition;
	}

	private ModelField createField() {
		ModelField field = new ModelField();
		field.setId(FIELD_ID);
		return field;
	}

}