package com.sirma.itt.seip.permissions.sync.batch;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link PermissionSyncInstanceReader}.
 *
 * @author A. Kunchev
 */
public class PermissionSyncInstanceReaderTest {

	@InjectMocks
	private PermissionSyncInstanceReader reader;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Before
	public void setup() {
		reader = new PermissionSyncInstanceReader();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void loadBatchData_instanceResolverAndConsumerCalled() {
		InstanceReferenceMock reference = InstanceReferenceMock.createGeneric("instance-id");
		when(instanceTypeResolver.resolveReferences(anyCollection())).thenReturn(Collections.singleton(reference));
		BiConsumer<String, InstanceReference> consumer = mock(BiConsumer.class);
		reader.loadBatchData(Collections.singleton("instance-id"), consumer);
		verify(consumer).accept("instance-id", reference);
	}

}
