package com.sirma.itt.emf.evaluation;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.instance.model.EmfInstance;

/**
 * The Class IdEvaluatorTest.
 * 
 * @author BBonev
 */
public class IdEvaluatorTest extends BaseEvaluatorTest {

	private DictionaryService dictionaryService;

	/**
	 * Test evaluation.
	 */
	@Test
	public void testEvaluation() {
		ExpressionsManager manager = createManager();
		Purpose target = new Purpose();
		target.setContentManagementId("cmId");
		target.setDmsId("dmsId");
		target.setIdentifier("identifier");
		target.setId("dbId");

		ExpressionContext context = manager.createDefaultContext(target, null, null);
		Assert.assertEquals("cmId", manager.evaluateRule("${id}", String.class, context));
		Assert.assertEquals("cmId", manager.evaluateRule("${id.cm}", String.class, context));
		Assert.assertEquals("dmsId", manager.evaluateRule("${id.dm}", String.class, context));
		Assert.assertEquals("identifier", manager.evaluateRule("${id.type}", String.class, context));
		Assert.assertEquals("dbId", manager.evaluateRule("${id.db}", String.class, context));

		Assert.assertEquals("null", manager.evaluateRule("${id.purpose}", String.class, context));
		target.setPurpose("purpose");
		Assert.assertEquals("purpose", manager.evaluateRule("${id.purpose}", String.class, context));

		GenericDefinition definition = Mockito.mock(GenericDefinition.class);
		Mockito.when(definition.getIdentifier()).thenReturn("identifier");
		Mockito.when(definition.getPath()).thenReturn("identifier");
		Mockito.when(definition.hasChildren()).thenReturn(true);
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setIdentifier("type");
		impl.setValue("value");
		Mockito.when(definition.getFields()).thenReturn(Arrays.asList((PropertyDefinition) impl));
		Mockito.when(dictionaryService.getInstanceDefinition(target)).thenReturn(definition);

		Assert.assertEquals(manager.evaluateRule("${id.type}", String.class, context), "value");

		// add matcher and from extensions
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add id evaluator
		IdEvaluator evaluator = new IdEvaluator();
		dictionaryService = Mockito.mock(DictionaryService.class);
		ReflectionUtils.setField(evaluator, "dictionaryService", dictionaryService);
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}

	/**
	 * The Class Purpose.
	 * 
	 * @author bbonev
	 */
	private class Purpose extends EmfInstance implements Purposable {

		private String purpose;

		@Override
		public String getPurpose() {
			return purpose;
		}

		@Override
		public void setPurpose(String purpose) {
			this.purpose = purpose;
		}

	}

}
