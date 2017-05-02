package com.sirma.itt.cmf.services.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;

/**
 * Test for {@link InstanceMoveOperation}.
 */
public class InstanceMoveOperationTest {

	@Mock
	private LinkService linkService;

	@Mock
	private CMFDocumentAdapterService documentAdapterService;

	@Mock
	private InstanceService instanceService;

	@Mock
	protected EventService eventService;

	@InjectMocks
	private InstanceMoveOperation instanceMoveOperation = new InstanceMoveOperation();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the move instance operation.
	 *
	 * @throws DMSException
	 *             if an exception has occurred in the dms system
	 */
	@Test
	public void testMoveOperation_successful() throws DMSException {
		mockDocumentAdapterService();
		mockTypeConverter();
		Context<String, Object> executionContext = new Context<>();
		Instance targetInstance = new EmfInstance();

		EmfInstance sourceInstance = new EmfInstance();

		Instance parent = new EmfInstance();
		parent.setId("parent");

		sourceInstance.setOwningInstance(parent);
		Operation operation = new Operation();

		executionContext.put(InstanceOperationProperties.INSTANCE, targetInstance);
		executionContext.put(InstanceOperationProperties.SOURCE_INSTANCE, sourceInstance);
		executionContext.put(InstanceOperationProperties.OPERATION, operation);

		instanceMoveOperation.execute(executionContext);
		verify(eventService, times(1)).fireNextPhase(any(TwoPhaseEvent.class));
		verify(linkService, times(3)).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString(), anyString());
	}

	@Test
	public void testMoveInSameParent() throws DMSException {
		mockDocumentAdapterService();
		mockTypeConverter();
		Context<String, Object> executionContext = new Context<>();

		EmfInstance sourceInstance = new EmfInstance();

		Instance parent = new EmfInstance();
		parent.setId("parent");

		sourceInstance.setOwningInstance(parent);
		Operation operation = new Operation();

		executionContext.put(InstanceOperationProperties.INSTANCE, parent);
		executionContext.put(InstanceOperationProperties.SOURCE_INSTANCE, sourceInstance);
		executionContext.put(InstanceOperationProperties.OPERATION, operation);

		instanceMoveOperation.execute(executionContext);
		verify(eventService, times(1)).fireNextPhase(any(TwoPhaseEvent.class));
		verify(linkService, times(0)).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
												   anyString(), anyString());
	}

	/**
	 * Mock the document adapter service and the properties it returns from the move operation.
	 *
	 * @throws DMSException
	 *             if an exception has occurred in the dms system
	 */
	private void mockDocumentAdapterService() throws DMSException {
		FileAndPropertiesDescriptor fileAndPropertiesDescriptor = Mockito.mock(FileAndPropertiesDescriptor.class);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("testKey", "testValue");
		when(fileAndPropertiesDescriptor.getProperties()).thenReturn(properties);
		when(documentAdapterService.moveDocument(any(DMSInstance.class), any(DMSInstance.class)))
				.thenReturn(fileAndPropertiesDescriptor);
	}

	/**
	 * Mock the type converter to support conversion of case and document instances to instance references. This is done
	 * because of the .toReference calls in the {@link InstanceMoveOperation}.
	 */
	@SuppressWarnings("unchecked")
	private static void mockTypeConverter() {
		TypeConverterImpl typeConverter = new TypeConverterImpl();
		typeConverter.addConverter(EmfInstance.class, InstanceReference.class, Mockito.mock(Converter.class));
		TypeConverterUtil.setTypeConverter(typeConverter);
	}
}
