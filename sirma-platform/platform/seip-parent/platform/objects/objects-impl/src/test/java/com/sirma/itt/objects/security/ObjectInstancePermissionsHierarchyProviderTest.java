package com.sirma.itt.objects.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link ObjectInstancePermissionsHierarchyProvider}
 *
 * @author BBonev
 */
public class ObjectInstancePermissionsHierarchyProviderTest {

	@InjectMocks
	private ObjectInstancePermissionsHierarchyProvider hierarchyProvider;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private TypeConverter typeConverter;

	@Spy
	private InstanceContextServiceMock contextService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(ShortUri.class), anyString()))
				.then(a -> new ShortUri(a.getArgumentAt(1, String.class)));
		when(semanticDefinitionService.getClassInstance("emf:Case"))
				.then(a -> create(a.getArgumentAt(0, String.class), "caseinstance"));
		when(semanticDefinitionService.getClassInstance("emf:Document"))
				.then(a -> create(a.getArgumentAt(0, String.class), "documentinstance"));
		when(semanticDefinitionService.getClassInstance("emf:ClassDescription"))
				.then(a -> create(a.getArgumentAt(0, String.class), "class"));

		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class))).then(a -> {
			Instance instance = a.getArgumentAt(1, Instance.class);
			InstanceReferenceMock reference = new InstanceReferenceMock(instance.getId().toString(),
					mock(DataTypeDefinition.class), instance);
			reference.setType(instance.type());
			return reference;
		});
		TypeConverterUtil.setTypeConverter(typeConverter);
	}

	@Test
	public void testParent4InstanceWithParent() throws Exception {
		InstanceReference instance = createObject("emf:instance", "emf:Document", "documentinstance");
		InstanceReference parent = createObject("emf:parent", "emf:Case", "documentinstance");

		contextService.bindContext(instance.toInstance(), parent);

		InstanceReference inheritanceFrom = hierarchyProvider.getPermissionInheritanceFrom(instance);
		assertEquals(parent, inheritanceFrom);
	}

	/**
	 * The user is not eligible parent.
	 */
	@Test
	public void testParent4InstanceWithParentUser() throws Exception {
		InstanceReference instance = createObject("emf:instance", "emf:Document", "documentinstance");
		InstanceReference parent = createObject("emf:user", "emf:User", "user");

		contextService.bindContext(instance.toInstance(), parent);

		InstanceReference inheritanceFrom = hierarchyProvider.getPermissionInheritanceFrom(instance);
		assertNull(inheritanceFrom);
	}

	@Test
	public void testParent4InstanceWithOutParent() throws Exception {
		InstanceReference instance = createObject("emf:instance", "emf:Document", "document");

		InstanceReference inheritanceFrom = hierarchyProvider.getPermissionInheritanceFrom(instance);
		assertNull(inheritanceFrom);
	}

	@Test
	public void testParent4Library() throws Exception {
		InstanceReference instance = createObject("emf:Case", "emf:ClassDescription", "class");

		assertNull(hierarchyProvider.getPermissionInheritanceFrom(instance));
	}

	@Test
	public void testIsLibrary() throws Exception {
		assertTrue(hierarchyProvider.isInstanceRoot("emf:Case"));
		assertFalse(hierarchyProvider.isInstanceRoot("emf:Instance"));
	}

	private static InstanceReference createObject(String id, String type, String category) {
		ObjectInstance instance = new ObjectInstance();
		instance.setId(id);
		ClassInstance classInstance = create(type, category);
		classInstance.setCategory(category);
		instance.setType(classInstance);
		return InstanceReferenceMock.createGeneric(instance);
	}

	private static ClassInstance create(String id, String category) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(id);
		classInstance.setCategory(category);
		return classInstance;
	}
}
