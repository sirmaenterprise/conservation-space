package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.evaluation.IdEvaluator;

/**
 * Overrides the default implementation to strip the workflow activity id from definition ids
 * 
 * @author BBonev
 */
@Specializes
@ApplicationScoped
public class CmfIdEvaluator extends IdEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1861410160828894328L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable cleanId(Serializable id) {
		if (id != null) {
			return WorkflowHelper.stripEngineId(id.toString());
		}
		return null;
	}
}
