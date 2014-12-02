package com.sirma.itt.emf.definition.compile.validator;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.label.LabelDefinition;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class ELExpressionValiatorTest.
 * 
 * @author BBonev
 */
public class ELExpressionValiatorTest extends EmfTest {

	/**
	 * The Class LabelDefinitionImplementation.
	 */
	private final class LabelDefinitionImplementation implements LabelDefinition {

		/** The identifier. */
		private String identifier;

		/** The labels. */
		private Map<String, String> labels = new HashMap<String, String>();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setId(Long id) {

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getId() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Map<String, String> getLabels() {
			return labels;
		}
	}

	/**
	 * Test field validation.
	 */
	@Test
	public void testFieldValidation() {
		ELExpressionValiator valiator = new ELExpressionValiator();
		FieldDefinitionImpl model = new FieldDefinitionImpl();
		model.setIdentifier("testField");
		model.setValue("");
		model.setRnc("");
		Assert.assertTrue(valiator.validate(model));

		model.setValue("${eval(${get([test])})}");
		Assert.assertTrue(valiator.validate(model));

		model.setRnc("${eval(${get([test])})}");
		Assert.assertTrue(valiator.validate(model));

		model.setRnc("${eval(${get([test]})}");
		Assert.assertFalse(valiator.validate(model));

		model.setValue("${eval($get([test])})}");
		Assert.assertFalse(valiator.validate(model));

		model.setRnc(null);
		Assert.assertFalse(valiator.validate(model));

		model.setValue(null);
		Assert.assertTrue(valiator.validate(model));
	}

	/**
	 * Test label validation.
	 */
	@Test
	public void testLabelValidation() {
		ELExpressionValiator valiator = new ELExpressionValiator();
		LabelDefinitionImplementation model = new LabelDefinitionImplementation();
		model.setIdentifier("test.label");

		Assert.assertTrue(valiator.validate(model));

		model.getLabels().put(
				"en",
				"${eval(<a class=\"${get([status])}\" href=\"${link(currentInstance)}\">"
						+ "<b>${id} ${CL([type])}"
						+ " (${CL([status])})</b></a><br />актуализирана от: "
						+ "<a href=\"${userLink(${get([modifiedBy])})}\">"
						+ "${user(${get([modifiedBy])})}</a>,"
						+ " ${date([modifiedOn]).format(dd.MM.yyyy, HH:mm)})}");
		Assert.assertTrue(valiator.validate(model));

		model.getLabels().put(
				"bg",
				"${eval(<a class=\"${get([status])}\" href=\"${link(currentInstance)}\">"
						+ "<b>${id} ${CL{[type])}"
						+ " (${CL([status])})</b></a><br />актуализирана от: "
						+ "<a href=\"${userLink(${get([modifiedBy])})}\">"
						+ "${user(${get([modifiedBy])})}</a>,"
						+ " ${date([modifiedOn]).format(dd.MM.yyyy, HH:mm)})}");
		Assert.assertFalse(valiator.validate(model));
	}

}
