/**
 *
 */
package com.sirma.itt.seip.instance.script;

import java.util.Collections;
import java.util.Iterator;

import org.mockito.Mock;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Base script test that can process/evaluate scripts with script node instances
 *
 * @author BBonev
 */
public abstract class BaseInstanceScriptTest extends ScriptTest {
	@Mock
	protected StateService stateService;
	/** The link service. */
	@Mock
	protected LinkService linkService;
	/** The instance service. */
	@Mock
	protected InstanceService instanceService;

	@Override
	public void beforeMethod() {
		super.beforeMethod();
		InstanceToScriptNodeConverterProvider provider = new InstanceToScriptNodeConverterProvider();
		ReflectionUtils.setField(provider, "nodes", getDefaultNewScriptNodeProvider());

		provider.register(converter);
	}

	/**
	 * Gets the new script node provider.
	 *
	 * @return the new script node provider
	 */
	protected InstanceProxyMock<ScriptInstance> getDefaultNewScriptNodeProvider() {
		return new InstanceProxyMock<ScriptInstance>(new ScriptNode()) {
			@Override
			public ScriptInstance get() {
				return createScriptNode();
			}

			@Override
			public Iterator<ScriptInstance> iterator() {
				return Collections.singleton(get()).iterator();
			}
		};
	}

	/**
	 * Creates the script node.
	 *
	 * @return the script node
	 */
	protected ScriptInstance createScriptNode() {
		ScriptInstance node = new ScriptNode();
		return setScriptNodeServices(node);
	}

	/**
	 * Sets the script node services.
	 *
	 * @param <S>
	 *            the node type
	 * @param node
	 *            the node to update
	 * @return the same node
	 */
	protected <S extends ScriptInstance> S setScriptNodeServices(S node) {
		ReflectionUtils.setField(node, "instanceService", instanceService);
		ReflectionUtils.setField(node, "typeConverter", converter);
		ReflectionUtils.setField(node, "linkService", linkService);
		ReflectionUtils.setField(node, "stateService", stateService);
		ReflectionUtils.setField(node, "securityContext", securityContext);
		return node;
	}
}
