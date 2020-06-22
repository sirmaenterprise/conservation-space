package com.sirma.sep.bpm.camunda.bpmn.execution.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tests for {@link PublishObject}.
 *
 * @author simeon iliev
 */
public class PublishObjectTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private InstanceService instanceService;
	@Spy
	private TypeConverter typeConverter = new TypeConverterImpl();

	@InjectMocks
	private PublishObject object;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		new DefaultTypeConverter().register(typeConverter);
	}

	@Test
	public void testValidateParameters() throws Exception {
		object.setRelations(mock(FixedValue.class));
		object.setSource(mock(Expression.class));
		object.validateParameters();
	}

	@Test(expected = NullPointerException.class)
	public void testValidateParametersNullPoineterExecption() throws Exception {
		object.setRelations(mock(FixedValue.class));
		object.setSource(null);
		object.validateParameters();
	}

	@Test
	public void testExecute() throws Exception {
		DelegateExecution execution = mock(DelegateExecution.class);
		when(execution.getId()).thenReturn("ID", "ID");
		when(execution.getProcessInstance()).thenReturn(execution);
		String busineskeyValue = "123456789";
		when(execution.getBusinessKey()).thenReturn(busineskeyValue);
		ExecutionEntity entitty = mock(ExecutionEntity.class);
		when(entitty.getBusinessKey()).thenReturn(busineskeyValue);
		Expression source = mock(Expression.class);
		when(source.getValue(eq(execution))).thenReturn(entitty);
		object.setSource(source);
		FixedValue fixedValue = mock(FixedValue.class);
		when(fixedValue.getExpressionText()).thenReturn("p1,p2");
		object.setRelations(fixedValue);
		Instance instance = mock(Instance.class);
		when(instance.getAsString(eq("p1"))).thenReturn("emf:1");
		when(instance.getAsString(eq("p2"))).thenReturn("emf:2");
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenReturn(instance);
		Optional<InstanceReference> option = Optional.of(reference);
		when(instanceTypeResolver.resolveReference(busineskeyValue)).thenReturn(option);
		List<Instance> resultList = new ArrayList<>();
		resultList.add(mock(Instance.class));
		resultList.add(mock(Instance.class));
		when(instanceTypeResolver.resolveInstances(any())).thenReturn(resultList);
		object.execute(execution, object);
		verify(instanceService, times(2)).publish(any(), any());
	}

	@Test
	public void testExecuteWithMultivalues() throws Exception {
		DelegateExecution execution = mock(DelegateExecution.class);
		when(execution.getId()).thenReturn("ID", "ID");
		when(execution.getProcessInstance()).thenReturn(execution);
		String busineskeyValue = "123456789";
		when(execution.getBusinessKey()).thenReturn(busineskeyValue);
		ExecutionEntity entitty = mock(ExecutionEntity.class);
		when(entitty.getBusinessKey()).thenReturn(busineskeyValue);
		Expression source = mock(Expression.class);
		when(source.getValue(eq(execution))).thenReturn(entitty);
		object.setSource(source);
		FixedValue fixedValue = mock(FixedValue.class);
		when(fixedValue.getExpressionText()).thenReturn("p1,p2");
		object.setRelations(fixedValue);
		ObjectInstance instance = new ObjectInstance();
		instance.append("p1","one");
		instance.append("p1","two");
		instance.add("p2", new SomeRandomObject(" emf:2"));
		instance.enableChangesTracking();
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenReturn(instance);
		Optional<InstanceReference> option = Optional.of(reference);
		when(instanceTypeResolver.resolveReference(busineskeyValue)).thenReturn(option);
		List<Instance> resultList = new ArrayList<>();
		resultList.add(mock(Instance.class));
		resultList.add(mock(Instance.class));
		resultList.add(mock(Instance.class));
		when(instanceTypeResolver.resolveInstances(any())).then(a -> {
			Collection<Serializable> ids = a.getArgumentAt(0, Collection.class);
			return ids.stream().map(EmfInstance::new).collect(Collectors.toList());
		});
		object.execute(execution, object);
		verify(instanceService, times(3)).publish(any(), any());
	}

	private static class SomeRandomObject implements Serializable {
		private final String id;

		private SomeRandomObject(String id) {this.id = id;}

		@Override
		public String toString() {
			return id;
		}
	}
}
