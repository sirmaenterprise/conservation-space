package com.sirma.itt.cmf.state;


import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default CMF implementation for managing case states in CMF.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.CASE)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 10)
public class CaseStateServiceExtension extends BaseCaseTreeStateServiceExtension<CaseInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(CaseInstance instance, Operation operation) {

		if (operation != null) {
			// predefined state have a priority
			boolean isPrimaryUpdated = false;
			if (StringUtils.isNotNullOrEmpty(operation.getNextPrimaryState())) {
				changePrimaryState(instance, operation.getNextPrimaryState());
				isPrimaryUpdated = true;
			}
			if (StringUtils.isNotNullOrEmpty(operation.getNextSecondaryState())) {
				instance.getProperties().put(CaseProperties.SECONDARY_STATE,
						operation.getNextSecondaryState());
			}
			// if we have updated the primary state no need to continue
			// otherwise we still can update it
			if (isPrimaryUpdated) {
				return true;
			}
		}

		String operationType = null;
		if (operation != null) {
			operationType = operation.getOperation();
		}

		String nextStateAutomatically = getNextStateAutomatically(instance, operationType);
		if (StringUtils.isNotNullOrEmpty(nextStateAutomatically)) {
			String string = changePrimaryState(instance, nextStateAutomatically);
			return !EqualsHelper.nullSafeEquals(string, nextStateAutomatically);
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<CaseInstance> getInstanceClass() {
		return CaseInstance.class;
	}

}
