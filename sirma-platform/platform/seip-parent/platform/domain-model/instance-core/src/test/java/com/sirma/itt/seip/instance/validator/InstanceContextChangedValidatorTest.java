package com.sirma.itt.seip.instance.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test for {@link InstanceContextChangedValidator}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class InstanceContextChangedValidatorTest {

	private static final boolean CAN_WRITE = true;
	private static final boolean CAN_NOT_WRITE = false;

	private static final boolean WITH_PARENT = true;
	private static final boolean WITHOUT_PARENT = false;

	private static final boolean CHILDREN_ALLOWED = true;
	private static final boolean CHILDREN_NOT_ALLOWED = false;

	private static final boolean CONTEXT_IS_CHANGED = true;
	private static final boolean CONTEXT_IS_NOT_CHANGED = false;

	private static final String PARENT_ID = "emf:parentId";

	private static final String DEFINITION_ID = "definitionId";

	@Mock
	private Instance instance;

	@Mock
	private Instance newParent;

	@Mock
	private InstanceContextService instanceContextService;

	@Mock
	private InstanceService instanceService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private DomainInstanceService domainInstanceService;

	@InjectMocks
	private InstanceContextChangedValidator changeInstanceContextValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(instance.getIdentifier()).thenReturn(DEFINITION_ID);
		when(domainInstanceService.loadInstance(PARENT_ID)).thenReturn(newParent);
	}

	@Test
	@UseDataProvider("scenarioWithErrorsDP")
	public void should_HasError(boolean withParent, boolean canWrite, boolean isChildAllowed) throws Exception {
		ValidationContext validationContext = createValidationContext();
		setupTest(CONTEXT_IS_CHANGED, withParent, canWrite, isChildAllowed);

		changeInstanceContextValidator.validate(validationContext);

		assertFalse(validationContext.getMessages().isEmpty());
	}

	@DataProvider
	public static Object[][] scenarioWithErrorsDP() {
		return new Object[][] {
				{ WITH_PARENT, CAN_NOT_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITH_PARENT, CAN_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITH_PARENT, CAN_NOT_WRITE, CHILDREN_ALLOWED },
		};
	}

	@Test
	@UseDataProvider("scenarioWithoutErrorsDP")
	public void should_HasNotError(boolean withParent, boolean contextChanged, boolean canWrite, boolean isChildAllowed) throws Exception {
		ValidationContext validationContext = createValidationContext();
		setupTest(contextChanged, withParent, canWrite, isChildAllowed);

		changeInstanceContextValidator.validate(validationContext);

		assertTrue(validationContext.getMessages().isEmpty());
	}

	@DataProvider
	public static Object[][] scenarioWithoutErrorsDP() {
		return new Object[][] {
				{ WITH_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_WRITE, CHILDREN_ALLOWED },
				{ WITH_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITH_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_NOT_WRITE, CHILDREN_ALLOWED },
				{ WITH_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_NOT_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITHOUT_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_WRITE, CHILDREN_ALLOWED },
				{ WITHOUT_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITHOUT_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_NOT_WRITE, CHILDREN_ALLOWED },
				{ WITHOUT_PARENT, CONTEXT_IS_NOT_CHANGED, CAN_NOT_WRITE, CHILDREN_NOT_ALLOWED },
				{ WITH_PARENT, CONTEXT_IS_CHANGED, CAN_WRITE, CHILDREN_ALLOWED },
				{ WITHOUT_PARENT, CONTEXT_IS_CHANGED, CAN_WRITE, CHILDREN_ALLOWED }
		};
	}

	private ValidationContext createValidationContext() {
		return new ValidationContext(instance, new Operation("userOperation", true));
	}

	private void setupTest(boolean contextChanged, boolean withParent, boolean canWrite, boolean childAllowed) {
		if (withParent) {
			when(instance.getAsString(eq(InstanceContextService.HAS_PARENT), any(InstancePropertyNameResolver.class))).thenReturn(PARENT_ID);
		}

		if (!childAllowed) {
			DefinitionModel definitionModel = Mockito.mock(DefinitionModel.class);
			when(definitionModel.getIdentifier()).thenReturn("allowedDefinitionId");
			Map<String, List<DefinitionModel>> allowedChildren = new HashMap<>(1);
			allowedChildren.put("CASE", Arrays.asList(definitionModel));
			when(instanceService.getAllowedChildren(newParent)).thenReturn(allowedChildren);
		}

		when(newParent.isWriteAllowed()).thenReturn(canWrite);
		when(instanceContextService.isContextChanged(instance)).thenReturn(contextChanged);
	}
}
