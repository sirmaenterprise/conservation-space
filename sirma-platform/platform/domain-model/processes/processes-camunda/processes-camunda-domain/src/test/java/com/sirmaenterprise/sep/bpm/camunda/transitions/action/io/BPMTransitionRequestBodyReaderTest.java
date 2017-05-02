package com.sirmaenterprise.sep.bpm.camunda.transitions.action.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.bpm.camunda.transitions.action.BPMTransitionRequest;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMTransitionRequestBodyReaderTest {
	@Mock
	private InstanceResourceParser instanceResourceParser;

	@Mock
	private RequestInfo request;
	@InjectMocks
	private BPMTransitionRequestBodyReader bPMTransitionRequestBodyReader;

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.transitions.action.io.BPMTransitionRequestBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}.
	 */
	@Test
	public void testIsReadable() throws Exception {
		assertFalse(bPMTransitionRequestBodyReader.isReadable(Object.class, null, null, null));
		assertTrue(bPMTransitionRequestBodyReader.isReadable(BPMTransitionRequest.class, null, null, null));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.transitions.action.io.BPMTransitionRequestBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}.
	 */
	@Test
	public void testReadFromValid() throws Exception {
		UriInfo uriInfo = mock(UriInfo.class);
		MultivaluedMap<String, String> map = mock(MultivaluedMap.class);
		when(map.get(eq("id"))).thenReturn(Collections.singletonList("emf:targetId"));
		when(uriInfo.getPathParameters()).thenReturn(map);
		when(request.getUriInfo()).thenReturn(uriInfo);
		Instance nextActivity = mock(Instance.class);
		when(instanceResourceParser.toInstance(any(), isNull(String.class))).thenReturn(nextActivity);
		Instance currentActivity = mock(Instance.class);
		when(instanceResourceParser.toInstance(any(), eq("emf:targetId"))).thenReturn(currentActivity);
		InputStream stream = new ByteArrayInputStream(
				"{\"operation\":\"bpmTransition\",\"userOperation\":\"approve\",\"targetInstances\":[{\"instanceId\":\"emf:myId\",\"definitionId\":\"myDef\",\"properties\":{\"title\":\"TASKST\",\"assignee\":[\"emf:user-tenant.id\"]}}],\"currentInstance\":\"emf:targetId\"}"
						.getBytes());
		BPMTransitionRequest readFrom = bPMTransitionRequestBodyReader.readFrom(BPMTransitionRequest.class, null, null,
				null, null, stream);

		verify(instanceResourceParser).toInstance(any(), isNull(String.class));
		verify(instanceResourceParser).toInstance(any(), eq("emf:targetId"));

		assertEquals("emf:targetId", readFrom.getTargetId());
		assertEquals(2, readFrom.getTransitionData().size());
		Iterator<String> iterator = readFrom.getTransitionData().keySet().iterator();
		assertEquals("emf:myId", iterator.next());
		assertEquals("emf:targetId", iterator.next());
	}

}