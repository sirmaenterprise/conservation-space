package com.sirma.itt.seip.domain;

import org.junit.Test;

/**
 * The Class CountingTreeNodeTest.
 *
 * @author BBonev
 */
public class CountingTreeNodeTest {

	/** The generator. */
	private static long generator = 1;

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	@Test
	public void test() {
		CountingTreeNode<String> root = createNode();
		root.addChild(createNode());
		root.addChild(createNode());
		root.addChild(createNode());
		root.addChild(createNode());

		for (CountingTreeNode<String> node : root.getChildren()) {
			node.addChild(createNode());
		}

		CountingTreeNode<String> temp = createNode();
		temp.addChild(createNode());
		temp.addChild(createNode());
		temp.addChild(createNode());
		CountingTreeNode<String> treeNode = createNode();
		temp.addChild(treeNode);

		Node node = root.getChild("1-4");
		((CountingTreeNode<String>) node).addChild(temp);

	}

	/**
	 * Creates the node.
	 *
	 * @return the counting tree node
	 */
	static CountingTreeNode<String> createNode() {
		CountingTreeNode<String> node = new CountingTreeNode<String>();
		node.setId(generator);
		node.setIdentifier("1-" + generator);
		generator++;
		return node;
	}

}
