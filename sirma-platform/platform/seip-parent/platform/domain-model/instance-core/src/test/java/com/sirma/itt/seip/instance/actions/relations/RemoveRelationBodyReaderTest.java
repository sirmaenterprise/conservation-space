package com.sirma.itt.seip.instance.actions.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Tests for {@link AddRelationBodyReader}
 *
 * @author BBonev
 */
public class RemoveRelationBodyReaderTest {

	@InjectMocks
	private RemoveRelationBodyReader bodyReader;

	@Mock
	private RequestInfo requestInfo;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(requestInfo.getUriInfo()).then(a -> {
			UriInfo info = mock(UriInfo.class);
			MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
			map.add(RequestParams.PATH_ID.key, "emf:instanceId");
			when(info.getPathParameters()).thenReturn(map);
			return info;
		});
	}


	@Test
	public void isReadable_wrongType() {
		assertFalse(bodyReader.isReadable(AddRelationRequest.class, null, null, null));
	}

	@Test
	public void isReadable_RemoveRelationRequestForType() {
		assertTrue(bodyReader.isReadable(RemoveRelationRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void readEmptyRequest() throws Exception {
		bodyReader.readFrom(null, null, null, null, null,
				new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void readRequest_singleRelation() throws Exception {
		RemoveRelationRequest request = bodyReader.readFrom(null, null, null, null, null,
				getResourceStream("removeRelation.json"));
		assertNotNull(request);
		assertEquals("userOperation", request.getUserOperation());
		assertNotNull(request.getRelations());
		assertFalse(request.getRelations().isEmpty());
		assertTrue(request.getRelations().containsKey("emf:addParent"));
		assertEquals(new HashSet<>(Arrays.asList("emf:instance1", "emf:instance2")),
				request.getRelations().get("emf:addParent"));
	}

	@Test
	public void readRequest_multipleRelation() throws Exception {
		RemoveRelationRequest request = bodyReader.readFrom(null, null, null, null, null,
				getResourceStream("removeTwoRelations.json"));
		assertNotNull(request);
		assertEquals("removeParent", request.getUserOperation());
		assertNotNull(request.getRelations());
		assertFalse(request.getRelations().isEmpty());
		assertTrue(request.getRelations().containsKey("emf:addParent"));
		assertTrue(request.getRelations().containsKey("emf:partOfTree"));
		assertEquals(new HashSet<>(Arrays.asList("emf:instance1", "emf:instance2")),
				request.getRelations().get("emf:addParent"));
		assertEquals(new HashSet<>(Arrays.asList("emf:instance1", "emf:instance2")),
				request.getRelations().get("emf:partOfTree"));
	}

	private static InputStream getResourceStream(String string) {
		return RemoveRelationBodyReaderTest.class.getClassLoader().getResourceAsStream(
				RemoveRelationBodyReaderTest.class.getPackage().getName().replace('.', '/') + "/" + string);
	}
}
