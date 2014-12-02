package com.sirma.itt.cmf.script;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.emf.script.ScriptNode;

/**
 * Script node implementation that has specific workflow methods
 * 
 * @author BBonev
 */
public class DocumentScriptNode extends ScriptNode {

	/**
	 * Gets the processed by instances
	 * 
	 * @return the processed by instances
	 */
	public ScriptNode[] getProcessedBy() {
		if (target == null) {
			return new ScriptNode[0];
		}
		return getLinks(LinkConstantsCmf.PROCESSED_BY);
	}

	/**
	 * Checks if the current document is locked.
	 * 
	 * @return true, if it's locked
	 */
	public boolean isLocked() {
		if (target == null) {
			return false;
		}
		return ((DocumentInstance) target).isLocked();
	}

	/**
	 * Gets the locked by user
	 * 
	 * @return the locked by user
	 */
	public String getLockedBy() {
		if (target == null) {
			return null;
		}
		return ((DocumentInstance) target).getLockedBy();
	}
}
