/**
 * Sets the given node as revision
 *
 * @param node
 *            the node to update
 */
function setInstanceAsRevision(node) {
	if (node) {
		revisions.setAsRevision(node);
	}
}

/**
 * Gets the last revision that is published for the given node.
 *
 * @param node
 *            to get his revision. Should not be a revision itself.
 * @returns a script node representing the revision or <code>null</code> if
 *          the node itself is <code>null</code> or the instance type is not
 *          revision supported or the node was not published, yet.
 */
function getLastRevision(node) {
	if (node) {
		return revisions.getLastRevisionForInstance(node);
	}
	return null;
}