package com.sirma.itt.seip.tx.util;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Abstract transactional buffer that stores changes in a single transaction. <br>
 * This buffer will call, the registered via {@link #registerOnTransactionCompletionHandler(Consumer)}, consumer with
 * all added changes, via {@link #add(Object)}, only once at the end of the transaction in the transaction
 * completion phase in new transaction (the original transaction will be in state in which new writes will not be
 * possible). <br>
 * This buffer implementation is intended for collecting multiple changes collected from events in a single transaction
 * and then flushing them at the end of the transaction in a single call. <br>
 * This is defined as an abstract class as different usages of this functionality require new concrete implementation
 * to be provided to distinguish the different buffer intentions.
 * Typical use of this should be
 * <pre><code>
 *     &#64;TransactionScoped
 *     public class MyBuffer extends AbstractTransactionalChangesBuffer&lt;MyChangeDto&gt; {
 *
 *         // used for deserialization
 *         private MyBuffer() {
 *             super();
 *         }
 *
 *         &#64;Inject
 *         public MyBuffer(TransactionSupport transactionSupport) {
 *             super(transactionSupport);
 *         }
 *
 *         // some custom methods if needed
 *     }
 *
 *     &#64;TransactionScoped
 *     public class MyObserver {
 *         &#64;Inject
 *         private MyBuffer buffer;
 *
 *         &#64;PostConstruct
 *         void initialize() {
 *             buffer.registerOnTransactionCompletionHandler(this::flushChanges);
 *         }
 *         private void flushChanges(Collection&lt;MyChangeDto&gt changes) {
 *             // process changes
 *         }
 *
 *         void onEvent(&#64;Observers MyEvent event) {
 *             MyChangeDto change = createChange(event);
 *             buffer.add(change);
 *         }
 *
 *         private MyChangeDto createChange(MyEvent event) {
 *             // process the event and return a change instance
 *         }
 *     }
 * </code></pre>
 * <p>
 * <br>
 * <b>Sub classes of this class should be annotated with
 * {@link javax.transaction.TransactionScoped} in order this implementation to work. </b>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/11/2017
 */
public abstract class AbstractTransactionalChangesBuffer<E> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Set<E> changes = new LinkedHashSet<>();

	private TransactionSupport transactionSupport;
	private Consumer<Collection<E>> changesConsumer;

	protected AbstractTransactionalChangesBuffer() {
		// used during building instance proxy
	}

	/**
	 * Instantiate a buffer instance and inject a transaction support instance
	 *
	 * @param transactionSupport the transaction support instance to used to register for transaction events
	 */
	@Inject
	public AbstractTransactionalChangesBuffer(TransactionSupport transactionSupport) {
		this.transactionSupport = transactionSupport;
	}

	/**
	 * Adds a change request to the buffer. The buffer will ignore <code>null</code> or duplicate requests
	 *
	 * @param changeRequest the change request
	 * @return true, if successfully added the change request to the buffer. This is the case then the request was not
	 * <code>null</code> and not already present.
	 */
	public boolean add(E changeRequest) {
		return addNonNullValue(changes, changeRequest);
	}

	/**
	 * Gets the all stored changes
	 *
	 * @return the a copy of all stored changes
	 */
	public synchronized Collection<E> getAll() {
		return new ArrayList<>(changes);
	}

	/**
	 * Return all stored changes and clears the buffer.
	 *
	 * @return all changes
	 */
	public synchronized Collection<E> drainAll() {
		List<E> copy = new ArrayList<>(changes);
		changes.clear();
		return copy;
	}

	public void registerOnTransactionCompletionHandler(Consumer<Collection<E>> changesConsumer) {
		this.changesConsumer = changesConsumer;
		flushChangesAtTransactionEnd();
	}

	private void flushChangesAtTransactionEnd() {
		transactionSupport.invokeBeforeTransactionCompletion(this::flushChanges);
	}

	/**
	 * Manually flush changes to the registered handler. Note that this could be done only once. After calling this
	 * method all changes will be deleted.
	 */
	public void flushChanges() {
		if (changesConsumer == null) {
			return;
		}
		try {
			// get changes from the current transaction and reset the buffer so only the first to enter should trigger
			// update
			Collection<E> collectedChanges = drainAll();
			if (isEmpty(collectedChanges)) {
				return;
			}
			Consumer<Collection<E>> consumer = changesConsumer;
			transactionSupport.invokeOnSuccessfulTransactionInTx(() -> consumer.accept(collectedChanges));
		} catch (ContextNotActiveException e) {
			LOGGER.warn("Could not get buffered changes!", e);
		}
	}

}
