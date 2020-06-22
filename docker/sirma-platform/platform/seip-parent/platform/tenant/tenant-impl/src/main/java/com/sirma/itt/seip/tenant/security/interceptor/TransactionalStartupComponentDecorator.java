package com.sirma.itt.seip.tenant.security.interceptor;

import com.sirma.itt.seip.runtime.AbstractDecoratingStartupComponent;
import com.sirma.itt.seip.runtime.StartupComponent;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Startup component decorator to add a transactions functionality
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2017
 */
public class TransactionalStartupComponentDecorator extends AbstractDecoratingStartupComponent {

	private final TransactionSupport transactionSupport;

	/**
	 * Instantiates a new abstract decorating component.
	 *
	 * @param decorated the decorated
	 * @param transactionSupport transactions manager instance to use
	 */
	public TransactionalStartupComponentDecorator(StartupComponent decorated, TransactionSupport transactionSupport) {
		super(decorated);
		this.transactionSupport = transactionSupport;
	}

	@Override
	public void execute() {
		transactionSupport.invokeInNewTx(getDecorated()::execute);
	}
}
