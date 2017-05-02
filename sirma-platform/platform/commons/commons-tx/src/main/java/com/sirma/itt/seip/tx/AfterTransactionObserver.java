/**
 *
 */
package com.sirma.itt.seip.tx;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Observer that handles after transaction execution events.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AfterTransactionObserver {

	@SuppressWarnings("static-method")
	void onTransactionCompletion(@Observes(during = TransactionPhase.AFTER_COMPLETION) AfterTransactionEvent event) {
		event.invoke();
	}

	@SuppressWarnings("static-method")
	void onTransactionSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) AfterTransactionSuccessEvent event) {
		event.invoke();
	}

	@SuppressWarnings("static-method")
	void onTransactionFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) AfterTransactionFailureEvent event) {
		event.invoke();
	}

	@SuppressWarnings("static-method")
	void onTransactionBeforeCompletion(
			@Observes(during = TransactionPhase.BEFORE_COMPLETION) BeforeTransactionEvent event) {
		event.invoke();
	}
}
