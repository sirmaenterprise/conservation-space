package com.sirma.itt.seip.permissions.db.patches;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.role.EntityPermission;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link EntityPermissionFixMissingParentsPatch}.
 *
 * @author Adrian Mitev
 */
public class EntityPermissionFixMissingParentsPatchTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private DbDao dbDao;

	@Captor
	ArgumentCaptor<List<Pair<String, Object>>> captor;

	@InjectMocks
	private EntityPermissionFixMissingParentsPatch entityPermissionFixMissingParentsPatch;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldUpdateMissingParentRelationships() {
		List<Serializable> instances = Arrays.asList("o1", "o2");

		// requires for thenReturn
		when(dbDao.fetch(anyString(), any())).thenReturn(new ArrayList<>(instances));

		// Stub InstanceTypeResolver
		InstanceReferenceMock o1 = InstanceReferenceMock.createGeneric("o1");
		InstanceReferenceMock o2 = InstanceReferenceMock.createGeneric("o2");
		InstanceReferenceMock p1 = InstanceReferenceMock.createGeneric("p1");

		List<InstanceReference> references = Arrays.asList(o1, o2);

		when(instanceTypeResolver.resolveReferences(instances)).thenReturn(references);

		// Stub TypeConverter
		when(hierarchyResolver.getPermissionInheritanceFrom(o1)).thenReturn(p1);

		entityPermissionFixMissingParentsPatch.migrate();

		verify(dbDao, times(1)).executeUpdate(eq(EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET_KEY),
				captor.capture());

		List<List<Pair<String, Object>>> callArgs = captor.getAllValues();
		assertEquals(callArgs.size(), 1);

		verifyCallArg(callArgs.get(0), "o1", "p1");
	}

	private void verifyCallArg(List<Pair<String, Object>> args, String targetId, String parentId) {
		assertEquals(2, args.size());

		assertEquals(Collections.singletonList(targetId), args.get(0).getSecond());
		assertEquals(parentId, args.get(1).getSecond());
	}

}
