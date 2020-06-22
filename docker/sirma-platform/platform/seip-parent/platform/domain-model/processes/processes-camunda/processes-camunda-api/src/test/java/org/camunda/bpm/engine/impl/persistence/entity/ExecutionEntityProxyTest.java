package org.camunda.bpm.engine.impl.persistence.entity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link ExecutionEntityProxy}
 *
 * @author hlungov
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionEntityProxyTest {

	@Test
	public void variablesCopyFromOriginalExecutionEntityTest() {
		VariableInstanceEntity variableInstanceMock = mock(VariableInstanceEntity.class);
		ExecutionEntityProxy executionEntityProxy = new ExecutionEntityProxy();
		executionEntityProxy.addVariables(Arrays.asList(variableInstanceMock));
		Assert.assertEquals(1, executionEntityProxy.getVariablesInternal().size());
		Assert.assertEquals(variableInstanceMock, executionEntityProxy.getVariablesInternal().iterator().next());
	}

	@Test
	public void setVariableLocalTest() {
		ExecutionEntityProxy executionEntityProxy = new ExecutionEntityProxy();
		VariableInstanceEntity test1 = createVariableInstanceEntityMock("test1");
		executionEntityProxy.addVariables(Arrays.asList(test1));
		Assert.assertEquals(1, executionEntityProxy.getVariablesInternal().size());
		Assert.assertEquals(test1, executionEntityProxy.getVariablesInternal().iterator().next());
		executionEntityProxy.addVariables(
				Arrays.asList(createVariableInstanceEntityMock("test1"), createVariableInstanceEntityMock("test2")));
		Assert.assertEquals(2, executionEntityProxy.getVariablesInternal().size());
	}

	private VariableInstanceEntity createVariableInstanceEntityMock(String variableName) {
		VariableInstanceEntity variableInstanceMock = mock(VariableInstanceEntity.class);
		when(variableInstanceMock.getName()).thenReturn(variableName);
		return variableInstanceMock;
	}

}
