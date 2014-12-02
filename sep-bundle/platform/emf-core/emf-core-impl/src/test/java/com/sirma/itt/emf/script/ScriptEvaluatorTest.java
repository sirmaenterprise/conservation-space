package com.sirma.itt.emf.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class ScriptEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class ScriptEvaluatorTest extends EmfTest {

	/** The link service. */
	private LinkService linkService;

	/** The instance service. */
	private InstanceService<?, ?> instanceService;

	/** The converter. */
	private TypeConverter converter;

	/**
	 * Initializes the.
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
	 * Test engine integration.
	 */
	public void testEngineIntegration() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>());
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		impl.initialize();

		TimeTracker tracker = TimeTracker.createAndStart();
		Object object = impl.eval("{  2 + 2; }", null);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 4.0);
		long notCompiled = tracker.stop();

		tracker.begin();
		object = impl.eval("{  2 + 2; }", null);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 4.0);
		long compiled = tracker.stop();
	}

	/**
	 * Test null script.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class, expectedExceptionsMessageRegExp = ".*null.*")
	public void testNullScript() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		impl.eval(null, null);
	}

	/**
	 * Test invalid scripts.
	 */
	@Test(expectedExceptions = ScriptException.class, expectedExceptionsMessageRegExp = ".*parsing.*")
	public void testInvalidScripts() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>());
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		impl.initialize();
		impl.eval("{ return 2 + 2; }", null);
	}

	/**
	 * Test not compilable scripts.
	 */
	public void testNotCompilableScripts() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>());
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		ReflectionUtils.setField(impl, "compilationEnabled", false);
		impl.initialize();

		Object object = impl.eval("{  2 + 2; }", null);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 4.0);
	}

	/**
	 * Test not compilable scripts.
	 */
	public void testWithBindings() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>());
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		impl.initialize();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 2);
		map.put("b", 2);
		Object object = impl.eval("{ a + b; }", map);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 4.0);
	}

	/**
	 * Test with global bindings.
	 */
	public void testWithGlobalBindings() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		GlobalBindingsExtension extension = Mockito.mock(GlobalBindingsExtension.class);
		Map<String, Object> global = new HashMap<String, Object>();
		global.put("a", 2);
		Mockito.when(extension.getBindings()).thenReturn(global);

		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>(Arrays.asList(extension)));
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		impl.initialize();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("b", 2);
		Object object = impl.eval("a + b", map);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 4.0);
	}

	/**
	 * Test with script node.
	 */
	public void testWithScriptNode() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(impl, "globalBindings", new ArrayList<>());
		ReflectionUtils.setField(impl, "scriptEngineName", "javascript");
		impl.initialize();

		Map<String, Object> map = new HashMap<String, Object>();
		Instance instance = new EmfInstance();
		instance.setId("id");
		instance.setProperties(new HashMap<String, Serializable>());
		ReflectionUtils.setField(instance, "reference", new LinkSourceId("id", new DataType(),
				instance));

		ScriptNode node = converter.convert(ScriptNode.class, instance);
		map.put("workflow", node);

		List<LinkReference> references = new ArrayList<LinkReference>(1);
		LinkReference ref = new LinkReference();
		ref.setFrom(new LinkSourceId("id", new DataType()));
		ref.setTo(new LinkSourceId("id2", new DataType()));
		references.add(ref);
		Mockito.when(
				linkService.getLinks(Mockito.any(InstanceReference.class), Mockito.eq("linkId")))
				.thenReturn(references);

		List<LinkInstance> instances = new ArrayList<LinkInstance>(1);
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setFrom(instance);

		Instance extected = new EmfInstance();
		extected.setId("id2");
		extected.setProperties(new HashMap<String, Serializable>());
		linkInstance.setTo(extected);
		instances.add(linkInstance);

		Mockito.when(linkService.convertToLinkInstance(Mockito.eq(references), Mockito.eq(true)))
				.thenReturn(instances);

		Object object = impl.eval(" workflow.getLinks(\"linkId\");", map);
		Assert.assertNotNull(object);
		Assert.assertTrue(object.getClass().isArray());
		ScriptNode[] list = (ScriptNode[]) object;
		Object value = list[0];
		Assert.assertTrue(value instanceof ScriptNode);
		ScriptNode response = (ScriptNode) value;
		Assert.assertNotNull(response.getTarget());
		Assert.assertEquals(response.getTarget().getId(), "id2");
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
}
