package com.sirma.itt.emf.instance.model;

import java.io.Serializable;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.Identity;

/**
 * Defines a interface that represents a reference to a concrete instance. The instance is
 * represented with identifier (it may not be the same identifier that is used in the instance) and
 * a type definition.
 * 
 * @author BBonev
 */
public interface InstanceReference extends Identity, Serializable {

	/**
	 * Gets the reference object type.
	 * 
	 * @return the sourceType
	 */
	public DataTypeDefinition getReferenceType();

	/**
	 * Set the reference object type.
	 * 
	 * @param referenceType
	 *            the new reference type
	 */
	public void setReferenceType(DataTypeDefinition referenceType);

	/**
	 * Converts the given instance reference to instance representation. The implementation could
	 * cache the created instance to performance. <br>
	 * The method could return <code>null</code> if the current reference is incomplete or the
	 * actual instance has been deleted.
	 * <p>
	 * <b>NOTE 1:</b> calling this method on large number of references could be have performance
	 * penalty for loading instances one by one. For optimization use some of the utility methods
	 * for batch loading.
	 * <p>
	 * <b>NOTE 2: Chaining the method calls will result in new instance loading every time!</b> <br>
	 * The issue is displayed in the following code snippet: <br>
	 * 
	 * <pre>
	 * <code>Instance instance1 = ...;
	 * InstanceReference reference = instance1.toReference();
	 * Instance instance2 = reference.toInstance();
	 * if (instance1 == instance2) {
	 *   &#47;&#47;equal - will never enter here
	 * } else {
	 *   &#47;&#47;not equal - always will enter here
	 * }
	 *  </code>
	 * </pre>
	 * 
	 * <b>UPDATE:<b> The issue above is no longer valid if the initial reference is fetched using
	 * the {@link Instance#toReference()} method. The returned instance reference will hold the same
	 * instance that created the reference the first time.
	 * 
	 * @return the instance represented by the current instance reference.
	 */
	Instance toInstance();
}
