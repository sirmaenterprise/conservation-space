package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InstancePermissionsHierarchyResolverImpl}
 *
 * @author BBonev
 */
public class InstancePermissionsHierarchyResolverImplTest {

	@InjectMocks
	private InstancePermissionsHierarchyResolverImpl hierarchyResolver;

	@Mock
	private InstancePermissionsHierarchyProvider hierarchyProvider;
	@Spy
	private List<InstancePermissionsHierarchyProvider> hierarchyProviders = new ArrayList<>();
	@Mock
	private InstanceTypes instanceTypes;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		hierarchyProviders.clear();
		hierarchyProviders.add(hierarchyProvider);
	}

	@Test
	public void testGetPermissionInheritanceFrom() throws Exception {
		when(hierarchyProvider.getPermissionInheritanceFrom(any())).thenReturn(mock(InstanceReference.class));

		InstanceReference reference = InstanceReferenceMock.createGeneric("emf:instance");
		InstanceReference parent = hierarchyResolver.getPermissionInheritanceFrom(reference);
		assertNotNull(parent);
	}

	@Test
	public void testIsInstanceRoot() throws Exception {
		when(hierarchyProvider.isInstanceRoot(any())).thenReturn(Boolean.TRUE);

		assertTrue(hierarchyResolver.isInstanceRoot("emf:instance"));
	}

	public void testIsRoot_notSupported() throws Exception {
		assertFalse(hierarchyResolver.isInstanceRoot(null));
		assertFalse(hierarchyResolver.isInstanceRoot(""));
	}
}
