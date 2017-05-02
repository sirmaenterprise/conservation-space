package com.sirma.itt.seip.script.extensions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.script.extensions.TypeConverterScriptExtension;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * The Class TypeConverterScriptExtensionTest.
 *
 * @author BBonev
 */
@Test
public class TypeConverterScriptExtensionTest extends ScriptTest {

	/**
	 * Test convert.
	 */
	public void testConvert() {
		Object object = eval("convert(java.lang.Integer, \"444\")");
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 444);
	}

	/**
	 * Test instance reference convert.
	 */
	public void testInstanceReferenceConvert() {
		Object object = eval("toReference(\"commoninstance\", \"444\")");
		Assert.assertNotNull(object);
		Assert.assertTrue(object instanceof InstanceReference);
		Assert.assertEquals(((Identity) object).getIdentifier(), "444");
	}

	/**
	 * Test to instance.
	 */
	public void testToInstance() {
		Object object = eval("toInstance(\"commoninstance\", \"444\")");
		Assert.assertNotNull(object);
		Assert.assertTrue(object instanceof ScriptInstance);
	}

	@Override
	protected void registerConverters(TypeConverter typeConverter) {
		typeConverter.addConverter(String.class, InstanceReference.class, source -> new InstanceReferenceMock(null, null, new CommonInstance()));
		typeConverter.addConverter(CommonInstance.class, ScriptInstance.class, source -> {
			ScriptInstance node = mock(ScriptInstance.class);
			when(node.getTarget()).thenReturn(source);
			return node;
		});
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		TypeConverterScriptExtension extension = new TypeConverterScriptExtension();
		ReflectionUtils.setField(extension, "typeConverter", converter);
		bindingsExtensions.add(extension);
	}

}
