package com.sirma.itt.seip.permissions.db.patches;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.role.EntityPermission;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link EntityPermissionLibrariesPatch}.
 *
 * @author Adrian Mitev
 */
public class EntityPermissionLibrariesPatchTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private DbDao dbDao;
	@Captor
	ArgumentCaptor<List<Pair<String, Object>>> captor;

	@InjectMocks
	private EntityPermissionLibrariesPatch entityPermissionLibrariesPatch;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldExecuteUpdateQueryForAllNonLibraryInstances() {
		List<Serializable> instances = Arrays.asList("o1", "o2", "o3", "l1", "l2");

		// requires for thenReturn
		when(dbDao.fetch(anyString(), any())).thenReturn(new ArrayList<>(instances));

		// Stub InstanceTypeResolver
		Map<Serializable, InstanceType> resolvedTypes = new HashMap<>();
		resolvedTypes.put("o1", createInstanceType("http://l1"));
		resolvedTypes.put("o2", createInstanceType("http://l2"));
		resolvedTypes.put("o3", createInstanceType("http://l2"));
		// emulate real library type
		resolvedTypes.put("l1", createInstanceType("TestClassDescription"));
		resolvedTypes.put("l2", createInstanceType("TestClassDescription"));

		when(instanceTypeResolver.resolve(instances)).thenReturn(resolvedTypes);

		// Stub TypeConverter
		Map<String, String> longToShortUris = new HashMap<>();
		longToShortUris.put("http://l1", "l1");
		longToShortUris.put("http://l2", "l2");

		when(typeConverter.convert(eq(ShortUri.class), anyString())).thenAnswer((invokation) -> {
			String shortUri = longToShortUris.get(invokation.getArgumentAt(1, String.class));
			return new ShortUri(shortUri);
		});

		entityPermissionLibrariesPatch.migrate();

		verify(dbDao, times(3)).executeUpdate(eq(EntityPermission.QUERY_UPDATE_LIBRARY_FOR_TARGET_KEY),
				captor.capture());

		List<List<Pair<String, Object>>> callArgs = captor.getAllValues();
		assertEquals(3, callArgs.size());

		verifyCallArg(callArgs.get(0), "o1", "l1");
		verifyCallArg(callArgs.get(1), "o2", "l2");
		verifyCallArg(callArgs.get(2), "o3", "l2");
	}

	private void verifyCallArg(List<Pair<String, Object>> args, String targetId, String libraryId) {
		assertEquals(2, args.size());

		assertEquals(targetId, args.get(0).getSecond());
		assertEquals(libraryId, args.get(1).getSecond());
	}

	private InstanceType createInstanceType(String id) {
		InstanceType mock = mock(InstanceType.class);
		when(mock.getId()).thenReturn(id);
		return mock;
	}

}
