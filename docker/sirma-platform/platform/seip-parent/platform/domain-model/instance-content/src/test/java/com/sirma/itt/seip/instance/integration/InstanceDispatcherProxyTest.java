/**
 *
 */
package com.sirma.itt.seip.instance.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.Content;

/**
 * @author BBonev
 *
 */
public class InstanceDispatcherProxyTest {
	@InjectMocks
	InstanceDispatcherProxy dispatcherProxy;

	@Spy
	List<InstanceDispatcher> dispatchers = new ArrayList<>();

	@Mock
	InstanceDispatcher dispatcher;
	@Mock
	Content content;

	Instance instance;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		dispatchers.clear();
		dispatchers.add(dispatcher);

		instance = new EmfInstance();
		instance.setId("emf:instance");
	}

	@Test
	public void getContentManagementSystem_null() throws Exception {
		assertNull(dispatcherProxy.getContentManagementSystem(instance, content));
		verify(dispatcher).getContentManagementSystem(instance, content);
	}

	@Test
	public void getContentManagementSystem() throws Exception {
		when(dispatcher.getContentManagementSystem(instance, content)).thenReturn("test");
		assertEquals("test", dispatcherProxy.getContentManagementSystem(instance, content));
		verify(dispatcher).getContentManagementSystem(instance, content);
	}

	@Test
	public void getViewManagementSystem() throws Exception {
		when(dispatcher.getViewManagementSystem(instance, content)).thenReturn("test");
		assertEquals("test", dispatcherProxy.getViewManagementSystem(instance, content));
		verify(dispatcher).getViewManagementSystem(instance, content);
	}

	@Test
	public void getDataSourceSystem() throws Exception {
		when(dispatcher.getDataSourceSystem(instance)).thenReturn("test");
		assertEquals("test", dispatcherProxy.getDataSourceSystem(instance));
		verify(dispatcher).getDataSourceSystem(instance);
	}
}
