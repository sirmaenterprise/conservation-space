package com.sirma.itt.cmf.services.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.actions.OperationInvoker;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * @author BBonev
 */
public class BaseInstanceOperationTest extends CmfTest {

	@Mock
	protected InstanceDao instanceDao;
	@Mock
	protected InstanceEventProvider<Instance> eventProvider;
	@Mock
	protected EventService eventService;
	@Mock
	protected ServiceRegistry serviceRegistry;
	@Mock
	protected DictionaryService dictionaryService;

	@Spy
	protected Iterable<InstanceOperation> operations = new LinkedList<>();

	@InjectMocks
	protected InstanceOperations instanceOperations;
	@Mock
	protected SerializationHelper serializationHelper;

	@Spy
	@InjectMocks
	protected OperationInvoker invoker;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		when(serviceRegistry.getInstanceDao(any())).thenReturn(instanceDao);
		when(serviceRegistry.getEventProvider(any())).thenReturn(eventProvider);

		when(serializationHelper.copy(any())).then(a -> a.getArgumentAt(0, Object.class));
	}

	/**
	 * Adds the operations the invoker
	 *
	 * @param ops
	 *            the instance operations
	 */
	protected void addOperations(InstanceOperation... ops) {
		((List<InstanceOperation>) operations).clear();
		((List<InstanceOperation>) operations).addAll(Arrays.asList(ops));
	}

	/**
	 * Getter method for invoker.
	 *
	 * @return the invoker
	 */
	public OperationInvoker getInvoker() {
		return invoker;
	}

	/**
	 * Getter method for instanceOperations.
	 *
	 * @return the instanceOperations
	 */
	public InstanceOperations getInstanceOperations() {
		return instanceOperations;
	}
}
