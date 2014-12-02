package com.sirma.itt.emf.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.InstanceProxyMock;

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
	private InstanceService<?, ?> instanceService;
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
		InstanceProxyMock<ScriptNode> proxyMock = new InstanceProxyMock<ScriptNode>(
				new ScriptNode()) {
			@Override
			public ScriptNode get() {
				return createScriptNode();
			}

			@Override
			public Iterator<ScriptNode> iterator() {
				List<ScriptNode> list = new ArrayList<ScriptNode>(1);
				list.add(get());
				return list.iterator();
			}
		};
		InstanceToScriptNodeConverterProvider provider = new InstanceToScriptNodeConverterProvider();
		ReflectionUtils.setField(provider, "nodes", proxyMock);

		provider.register(converter);
	}

	/**
	 * Creates the script node.
	 * 
	 * @return the script node
	 */
	private ScriptNode createScriptNode() {
		ScriptNode node = new ScriptNode();
		ReflectionUtils.setField(node, "instanceService", instanceService);
		ReflectionUtils.setField(node, "typeConverter", converter);
		ReflectionUtils.setField(node, "linkService", linkService);
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

		TopicInstance instance = new TopicInstance();
		instance.setId("1");
		node = converter.convert(ScriptNode.class, instance);
		Assert.assertNotNull(node);
		Assert.assertNotNull(node.getTarget());
		Assert.assertEquals(node.getTarget().getId(), "1");
	}


}
