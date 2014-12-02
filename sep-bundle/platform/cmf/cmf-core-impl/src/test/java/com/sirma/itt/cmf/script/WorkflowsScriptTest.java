package com.sirma.itt.cmf.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.script.GlobalBindingsExtension;
import com.sirma.itt.emf.script.InstanceToScriptNodeConverterProvider;
import com.sirma.itt.emf.script.ScriptEvaluatorImpl;
import com.sirma.itt.emf.script.ScriptNode;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class WorkflowsScriptTest.
 *
 * @author BBonev
 */
@Test
public class WorkflowsScriptTest extends CmfTest {

	/** The change document state from wf. */
	private String CHANGE_DOCUMENT_STATE_FROM_WF = "var documents = root.getProcessingDocuments();"
			+ "for (var i = 0; i < documents.length; i++) {"
			+ "	var document = documents[i];"
			+ "	document.changeState(\"toReview\").save();" +
			"}";

	/** The converter. */
	private TypeConverter converter;

	/** The instance service. */
	@SuppressWarnings("rawtypes")
	private InstanceService instanceService;

	/** The link service. */
	private LinkService linkService;

	/** The state service. */
	private StateService stateService;

	/**
	 * Initializes the field variables.
	 */
	@BeforeMethod
	public void init() {
		converter = createTypeConverter();
		instanceService = Mockito.mock(InstanceService.class);
		linkService = Mockito.mock(LinkService.class);
		stateService = Mockito.mock(StateService.class);
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

		provider = new CmfInstanceToScritpNodeConverterProvider();
		ReflectionUtils.setField(provider, "workflowNodes", new InstanceProxyMock<ScriptNode>(
				new ScriptNode()) {
			@Override
			public ScriptNode get() {
				return createWorkflowScriptNode();
			}

			@Override
			public Iterator<ScriptNode> iterator() {
				List<ScriptNode> list = new ArrayList<ScriptNode>(1);
				list.add(get());
				return list.iterator();
			}
		});
		ReflectionUtils.setField(provider, "documentNodes", new InstanceProxyMock<ScriptNode>(
				new ScriptNode()) {
			@Override
			public ScriptNode get() {
				return createDocumentScriptNode();
			}

			@Override
			public Iterator<ScriptNode> iterator() {
				List<ScriptNode> list = new ArrayList<ScriptNode>(1);
				list.add(get());
				return list.iterator();
			}
		});

		provider.register(converter);
	}

	/**
	 * Test document state change.
	 */
	@SuppressWarnings("unchecked")
	public void testDocumentStateChange() {
		ScriptEvaluatorImpl evaluator = createScriptEvaluator(null);

		Map<String, Object> bindings = new HashMap<String, Object>();
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("workflow");
		context.setProperties(new HashMap<String, Serializable>());
		setReferenceField(context);

		DocumentInstance document = new DocumentInstance();
		document.setId("document");
		document.setProperties(new HashMap<String, Serializable>());
		setReferenceField(document);

		List<LinkReference> references = new ArrayList<LinkReference>(1);
		LinkReference ref = new LinkReference();
		ref.setId("link1");
		ref.setFrom(new LinkSourceId("workflow", createDataType(context)));
		ref.setTo(new LinkSourceId("document", createDataType(document)));
		references.add(ref);
		Mockito.when(
				linkService.getLinks(Mockito.eq(context.toReference()), Mockito.eq("emf:processes")))
				.thenReturn(references);

		List<LinkInstance> instances = new ArrayList<LinkInstance>(1);
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setFrom(context);

		linkInstance.setTo(document);
		instances.add(linkInstance);

		Mockito.when(linkService.convertToLinkInstance(Mockito.eq(references), Mockito.eq(true)))
				.thenReturn(instances);

		ScriptNode node = converter.convert(ScriptNode.class, context);
		bindings.put("root", node);

		evaluator.eval(CHANGE_DOCUMENT_STATE_FROM_WF, bindings);

		Mockito.verify(instanceService, Mockito.atLeastOnce()).save(
				Mockito.any(DocumentInstance.class),
				Mockito.any(Operation.class));
	}

	/**
	 * Creates the script evaluator.
	 *
	 * @param globalBindings
	 *            the global bindings
	 * @return the script evaluator impl
	 */
	private ScriptEvaluatorImpl createScriptEvaluator(final Map<String, Object> globalBindings) {
		ScriptEvaluatorImpl evaluator = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(evaluator, "scriptEngineName", "javascript");
		ReflectionUtils.setField(evaluator, "globalBindings",
				new InstanceProxyMock<GlobalBindingsExtension>(new GlobalBindingsExtension() {
					@Override
					public Map<String, Object> getBindings() {
						return globalBindings == null ? Collections.<String, Object> emptyMap()
								: globalBindings;
					}
				}));
		evaluator.initialize();
		return evaluator;
	}

	/**
	 * Creates the script node.
	 *
	 * @return the script node
	 */
	private ScriptNode createScriptNode() {
		ScriptNode node = new ScriptNode();
		initNodeServices(node);
		return node;
	}

	/**
	 * Creates the script node.
	 *
	 * @return the script node
	 */
	private ScriptNode createWorkflowScriptNode() {
		WorkflowScriptNode node = new WorkflowScriptNode();
		initNodeServices(node);
		return node;
	}

	/**
	 * Creates the script node.
	 *
	 * @return the script node
	 */
	private ScriptNode createDocumentScriptNode() {
		DocumentScriptNode node = new DocumentScriptNode();
		initNodeServices(node);
		return node;
	}

	/**
	 * Initializes the node services.
	 *
	 * @param node
	 *            the node
	 */
	private void initNodeServices(ScriptNode node) {
		ReflectionUtils.setField(node, "instanceService", instanceService);
		ReflectionUtils.setField(node, "typeConverter", converter);
		ReflectionUtils.setField(node, "linkService", linkService);
		ReflectionUtils.setField(node, "stateService", stateService);
	}

}
