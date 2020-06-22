package com.sirma.itt.emf.revision.script;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Script provider for revision service operations
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 30)
public class RevisionsScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String REVISION_SCRIPT_JS = "revisions.js";

	@Inject
	private RevisionService revisionService;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("revisions", this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), REVISION_SCRIPT_JS);
	}

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	/**
	 * Sets the given script node as revision.
	 *
	 * @param node
	 *            the node to update as revision
	 */
	public void setAsRevision(ScriptNode node) {
		if (node != null) {
			// TODO: the value here should be set from the EMF ontology - but it's not possible now
			node.add(DefaultProperties.REVISION_TYPE, "emf:revision", fieldConverter);
		}
	}

	/**
	 * Gets the last revision for instance.
	 *
	 * @param node
	 *            the node
	 * @return the last revision for instance
	 */
	public ScriptNode getLastRevisionForInstance(ScriptNode node) {
		if (node == null || !revisionService.isRevisionSupported(node.getTarget())) {
			return null;
		}
		Instance revision = revisionService.getLastRevision(node.getTarget());
		return typeConverter.convert(ScriptNode.class, revision);
	}

	/**
	 * Checks if the given target instance of the passed node is revision.
	 *
	 * @param node
	 *            the node containing the instance that is checked
	 * @return true if the target instance is revision, otherwise false
	 */
	public boolean isRevision(ScriptNode node) {
		if (node == null || node.getTarget() == null) {
			LOGGER.debug("The target instance is null. Return false.");
			return false;
		}

		return revisionService.isRevision(node.getTarget());
	}

}
