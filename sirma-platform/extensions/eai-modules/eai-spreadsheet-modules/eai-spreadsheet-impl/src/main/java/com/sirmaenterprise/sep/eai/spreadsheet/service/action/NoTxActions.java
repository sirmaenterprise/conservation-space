package com.sirmaenterprise.sep.eai.spreadsheet.service.action;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Extended {@link Actions} service that does not support transactions implicitly.
 * 
 * @author bbanchev
 */
@ApplicationScoped
@NoTx
public class NoTxActions extends Actions {

	@Override
	@Transactional(TxType.NOT_SUPPORTED)
	public Object callAction(ActionRequest request) {
		return super.callAction(request);
	}
}
