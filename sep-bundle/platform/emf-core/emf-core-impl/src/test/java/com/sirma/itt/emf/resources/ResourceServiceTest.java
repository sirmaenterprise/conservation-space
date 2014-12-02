package com.sirma.itt.emf.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class ResourceServiceTest.
 * 
 * @author BBonev
 */
@Test(enabled = false)
public class ResourceServiceTest extends EmfTest {

	/**
	 * Test equals.
	 */
	public void testResourceEquals() {
		ResourceServiceImpl service = Mockito.mock(ResourceServiceImpl.class);
		when(service.areEqual(any(), any())).thenCallRealMethod();
		TypeConverter converter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setField(service, "typeConverter", converter);

		// null checks
		Assert.assertFalse(service.areEqual(null, null));
		Assert.assertFalse(service.areEqual("", null));
		Assert.assertFalse(service.areEqual(null, ""));
		Assert.assertFalse(service.areEqual("", ""));

		// test before initialization a.k.a missing users
		Assert.assertFalse(service.areEqual("test", "test"));

		Resource testResource = createResource("emf:test", "test");

		when(service.getResource(eq("test"), eq(ResourceType.USER))).thenReturn(testResource);
		Assert.assertTrue(service.areEqual("test", "test"));

		// test with different users
		when(service.getResource(eq("test2"), eq(ResourceType.USER))).thenReturn(
				createResource("emf:test2", "test2"));
		Assert.assertFalse(service.areEqual("test", "test2"));

		when(service.getResource("emf:test")).thenReturn(testResource);

		// test mixed id checks
		Assert.assertTrue(service.areEqual("emf:test", "test"));
		Assert.assertTrue(service.areEqual("emf:test", "emf:test"));
		Assert.assertTrue(service.areEqual("test", "emf:test"));

		// test with instance reference as string to instance conversion
		String jsonUser = "{instanceType:\"user\", instanceId=\"emf:test\"}";
		when(converter.convert(eq(InstanceReference.class), eq(jsonUser))).thenReturn(
				createInstanceRef("emf:test"));
		Assert.assertTrue(service.areEqual("test", jsonUser));

		// test with resource instance
		Assert.assertTrue(service.areEqual("test", testResource));

		// test with custom type conversion to resource
		Pair<Class<?>, String> pair = new Pair<Class<?>, String>(EmfUser.class, "emf:test");
		when(converter.convert(eq(Resource.class), eq(pair))).thenReturn(testResource);
		Assert.assertTrue(service.areEqual("test", pair));
	}

	/**
	 * Creates the instance ref.
	 * 
	 * @param id
	 *            the id
	 * @return the instance reference
	 */
	private InstanceReference createInstanceRef(String id) {
		LinkSourceId sourceId = new LinkSourceId();
		sourceId.setIdentifier(id);
		ReflectionUtils.setField(sourceId, "instance", createResource(id, id));
		return sourceId;
	}

	/**
	 * Creates the resource.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @return the resource
	 */
	private Resource createResource(String id, String name) {
		EmfUser user = new EmfUser(name);
		user.setId(id);
		return user;
	}

}
