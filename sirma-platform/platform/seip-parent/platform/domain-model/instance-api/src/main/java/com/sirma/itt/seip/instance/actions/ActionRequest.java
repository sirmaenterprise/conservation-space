package com.sirma.itt.seip.instance.actions;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Base object that represents a request for execution of particular user operation. The class itself should not be used
 * as operation request. For different operations should be defined concrete implementation and parsers that builds
 * them.
 * <p>
 * In order an {@link ActionRequest} to be valid then the methods {@link #getOperation()} and {@link #getTargetId()}
 * should not return <code>null</code> values.
 *
 * @author BBonev
 */
public abstract class ActionRequest implements Serializable {

	private static final long serialVersionUID = 544357395656732828L;

	private String userOperation;
	private Serializable targetId;
	private InstanceReference targetReference;
	private String placeholder;
	private List<Serializable> contextPath;

	/**
	 * The operation id that triggered this request. This is the server operation id.
	 *
	 * @return the operation
	 */
	public abstract String getOperation();

	/**
	 * Gets the user operation that need to be executed. If not specified then {@link #getOperation()} will be used for
	 * audit for example. It may be the same as the system operation. The difference is that for a single server
	 * operation may exist multiple server (business) operations.
	 *
	 * @return the user operation
	 */
	public String getUserOperation() {
		return userOperation;
	}

	/**
	 * Constructs an {@link Operation} instance based on the methods {@link #getOperation()} and
	 * {@link #getUserOperation()}.
	 *
	 * @return the operation
	 */
	public Operation toOperation() {
		return new Operation(getOperation(), getUserOperation(), true);
	}

	/**
	 * Sets the user operation that need to be executed. This operation will be logged in the audit log if enabled. This
	 * operation may be equal or not to the one set with {@link #setUserOperation(String)}.
	 *
	 * @param userOperation
	 *            the new user operation
	 */
	public void setUserOperation(String userOperation) {
		this.userOperation = userOperation;
	}

	/**
	 * Gets the target id of the instance/object that will be affected by this operation request.
	 *
	 * @return the target id
	 */
	public Serializable getTargetId() {
		return targetId;
	}

	/**
	 * Sets the target id of the instance/object that will be affected by this operation request.
	 *
	 * @param targetId
	 *            the new target id
	 */
	public void setTargetId(Serializable targetId) {
		this.targetId = targetId;
	}

	/**
	 * Resolved reference for the target Id
	 *
	 * @return the targetReference
	 */
	public InstanceReference getTargetReference() {
		return targetReference;
	}

	/**
	 * Set the resolved reference from the target id
	 *
	 * @param targetReference
	 *            the targetReference to set
	 */
	public void setTargetReference(InstanceReference targetReference) {
		this.targetReference = targetReference;
	}

	/**
	 * Gets the placeholder where this operation was triggered.
	 *
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * Sets the placeholder where this operation was triggered.
	 *
	 * @param placeholder
	 *            the new placeholder
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * Gets the context path that identifies the context there the instance/object is executed.
	 *
	 * @return the context path
	 */
	public List<Serializable> getContextPath() {
		return contextPath;
	}

	/**
	 * Sets the context path for the action execution. This should be a list of instance/object identifiers in the order
	 * of hierarchy so that the first element is the top of the context and the last is the instance just before the
	 * target instance/object provided via {@link #setTargetId(Serializable)}
	 *
	 * @param contextPath
	 *            the new context path
	 */
	@SuppressWarnings("unchecked")
	public void setContextPath(List<? extends Serializable> contextPath) {
		this.contextPath = (List<Serializable>) contextPath;
	}
}
