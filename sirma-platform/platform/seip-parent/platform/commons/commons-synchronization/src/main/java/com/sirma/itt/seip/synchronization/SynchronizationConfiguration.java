package com.sirma.itt.seip.synchronization;

import java.util.function.BiPredicate;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link SynchronizationConfiguration} is an extension that provides means for implementing synchronization between 2
 * data sources: {@link #getSource()} and {@link #getDestination()}. The flow of data is from {@link #getSource()} to
 * {@link #getDestination()}. Any changes found in the data from {@link #getSource()} and not found in
 * {@link #getDestination()} will be written to the result object and will be provided to the method
 * {@link #save(SynchronizationResult, SyncRuntimeConfiguration)}. <br>
 * If merge operation is supported ({@link #isMergeSupported()} returns <code>true</code>) then the method
 * {@link #merge(Object, Object)} will be called to combine the data for elements found in both source and destination
 *
 * @param <I>
 *            the identity type of the items being synchronized
 * @param <E>
 *            the element type
 * @author BBbonev
 */
public interface SynchronizationConfiguration<I, E> extends Plugin, Named {
	/** The plugin name. */
	String PLUGIN_NAME = "synchronizationConfig";

	/**
	 * Gets the provider that fetches the remove data used for as source for the synchronization.
	 *
	 * @return the source data provider
	 */
	SynchronizationDataProvider<I, E> getSource();

	/**
	 * Gets the provider that fetches data located at the destination of the synchronized items.
	 *
	 * @return the destination data provider
	 */
	SynchronizationDataProvider<I, E> getDestination();

	/**
	 * If merge operation is supported and this method should return <code>true</code> then the method
	 * {@link #merge(Object, Object)} will be called to perform the merge operation.
	 *
	 * @return true, if is merge supported
	 */
	boolean isMergeSupported();

	/**
	 * Merge operation called to perform the merge between the destination item and the source item. The items returned
	 * from this method will be located in the result {@link SynchronizationResult#getModified()}.
	 *
	 * @param oldValue
	 *            the element returned by the {@link #getDestination()} provider
	 * @param newValue
	 *            the element returned by the {@link #getSource()} provider
	 * @return the merged element
	 */
	E merge(E oldValue, E newValue);

	/**
	 * Method called to save the changes found during synchronization
	 *
	 * @param result
	 *            the result from the synchronization
	 * @param runtimeConfiguration
	 *            the runtime configuration used to trigger the synchronization
	 */
	void save(SynchronizationResult<I, E> result, SyncRuntimeConfiguration runtimeConfiguration);

	/**
	 * Gets a comparator that will be used to determine if one element found in the source has any changes in the
	 * destination. The default implementation uses the {@link Object#equals(Object)} method.
	 *
	 * @return the comparator to use
	 */
	default BiPredicate<E, E> getComparator() {
		return EqualsHelper::nullSafeEquals;
	}
}
