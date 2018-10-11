package com.sirma.sep.instance.properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InstancePropertiesRestService}.
 *
 * @author A. Kunchev
 */
public class InstancePropertiesRestServiceTest {

	@InjectMocks
	private InstancePropertiesRestService service;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private InstanceRelationsService instanceRelationsService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = BadRequestException.class)
	public void loadObjectProperty_emptyProperty() {
		service.loadObjectProperty("instance-id", "", 0, 0);
		verifyZeroInteractions(instanceRelationsService, instanceTypeResolver);
	}

	@Test(expected = BadRequestException.class)
	public void loadObjectProperty_nullProperty() {
		service.loadObjectProperty("instance-id", null, 0, 0);
		verifyZeroInteractions(instanceRelationsService, instanceTypeResolver);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadObjectProperty_noInstance() {
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.empty());
		service.loadObjectProperty("instance-id", "obj-property", 0, 0);
		verifyZeroInteractions(instanceRelationsService);
	}

	@Test
	public void loadObjectProperty_successfulPropertyEvaluation() {
		when(instanceTypeResolver.resolveReference(anyString()))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric("id")));
		when(instanceRelationsService.evaluateRelations(any(Instance.class), anyString(), anyInt(), anyInt()))
				.thenReturn(Arrays.asList("instance-id-1", "instance-id-2"));
		List<String> result = service.loadObjectProperty("instance-id", "obj-property", 0, 0);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}
}
