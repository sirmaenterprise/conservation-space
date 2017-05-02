/**
 *
 */
package com.sirma.itt.seip.security.decorators;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.mocks.SecureEventMock;
import com.sirma.itt.seip.security.util.SecureExecutor;

/**
 * @author BBonev
 */
@Test
public class SecureEventServiceDecoratorTest {

	@Mock
	EventService delegate;
	@Mock
	SecurityContextManager securityContextManager;

	@InjectMocks
	SecureEventServiceDecorator decorator = new SecureEventServiceDecoratorImpl();

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.executeAs()).thenReturn(ContextualExecutor.NoContextualExecutor.INSTANCE);
	}

	public void testFireMethod() {
		SecureEventMock event = new SecureEventMock();
		decorator.fire(event);

		assertNotNull(event.getSecureExecutor());

		verify(delegate).fire(event);
		verify(securityContextManager).executeAs();
	}

	public void testFireNextPhaseMethod() {
		SecureEventMockNextPhase event = new SecureEventMockNextPhase();
		decorator.fireNextPhase(event);

		assertNotNull(event.getSecureExecutor());

		verify(delegate).fireNextPhase(event);
		verify(securityContextManager).executeAs();
	}

	public void testFireMethod_executorSet() {
		SecureEventMock event = new SecureEventMock();
		SecureExecutor secureExecutor = new SecureExecutor(securityContextManager);
		event.setSecureExecutor(secureExecutor);
		decorator.fire(event);

		assertNotNull(event.getSecureExecutor());
		assertEquals(event.getSecureExecutor(), secureExecutor);

		verify(delegate).fire(event);
		verify(securityContextManager).executeAs();
	}

	public void testFireNextPhaseMethod_executorSet() {
		SecureEventMockNextPhase event = new SecureEventMockNextPhase();
		SecureExecutor secureExecutor = new SecureExecutor(securityContextManager);
		event.setSecureExecutor(secureExecutor);

		decorator.fireNextPhase(event);

		assertNotNull(event.getSecureExecutor());
		assertEquals(event.getSecureExecutor(), secureExecutor);

		verify(delegate).fireNextPhase(event);
		verify(securityContextManager).executeAs();
	}

	private static class SecureEventMockNextPhase extends SecureEventMock implements TwoPhaseEvent {

		@Override
		public Map<String, Object> getContext() {
			return null;
		}

		@Override
		public void addToContext(String key, Object value) {
			// nothing
		}

		@Override
		public <E extends TwoPhaseEvent> E getNextPhaseEvent() {
			return null;
		}
	}

	private static class SecureEventServiceDecoratorImpl extends SecureEventServiceDecorator {
		// nothing to add
	}
}
