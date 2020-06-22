package com.sirma.itt.seip.instance.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class TestScriptConverter.
 *
 * @author BBonev
 */
@Test
public class TestScriptConverter extends EmfTest {

	/** The link service. */
	private LinkService linkService;
	/** The instance service. */
	private InstanceService instanceService;
	/** The converter. */
	private TypeConverter converter;

	/**
	 * Initializes the field variables
	 */
	@BeforeMethod
	public void init() {
		converter = createTypeConverter();
		instanceService = Mockito.mock(InstanceService.class);
		linkService = Mockito.mock(LinkService.class);
		InstanceProxyMock<ScriptNode> proxyMock = new InstanceProxyMock<ScriptNode>(new ScriptNode()) {
			@Override
			public ScriptNode get() {
				return createScriptNode();
			}

			@Override
			public Iterator<ScriptNode> iterator() {
				List<ScriptNode> list = new ArrayList<>(1);
				list.add(get());
				return list.iterator();
			}
		};
		InstanceToScriptNodeConverterProvider provider = new InstanceToScriptNodeConverterProvider();
		ReflectionUtils.setFieldValue(provider, "nodes", proxyMock);

		provider.register(converter);
	}

	/**
	 * Creates the script node.
	 *
	 * @return the script node
	 */
	private ScriptNode createScriptNode() {
		ScriptNode node = new ScriptNode();
		ReflectionUtils.setFieldValue(node, "instanceService", instanceService);
		ReflectionUtils.setFieldValue(node, "typeConverter", converter);
		ReflectionUtils.setFieldValue(node, "linkService", linkService);
		return node;
	}

	/**
	 * Test basic convert.
	 */
	@Test
	public void testBasicConvert() {
		ScriptNode node = converter.convert(ScriptNode.class, new EmfInstance());
		Assert.assertNotNull(node);
		Assert.assertNotNull(node.getTarget());

		CommonInstance instance = new CommonInstance();
		instance.setId("1");
		node = converter.convert(ScriptNode.class, instance);
		Assert.assertNotNull(node);
		Assert.assertNotNull(node.getTarget());
		Assert.assertEquals(node.getTarget().getId(), "1");
	}

}
