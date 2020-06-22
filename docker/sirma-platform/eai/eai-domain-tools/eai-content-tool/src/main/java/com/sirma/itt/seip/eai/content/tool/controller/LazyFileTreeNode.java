package com.sirma.itt.seip.eai.content.tool.controller;

import java.io.File;
import java.util.concurrent.Executors;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

/**
 * Custom {@link CheckBoxTreeItem} that loads lazily the children of a node. The {@link #getValue()} returns the file
 * related to node. {@link #equals(Object)} and {@link #hashCode()} are overridden to support comparison of node values.
 * 
 * @author bbanchev
 */
class LazyFileTreeNode extends CheckBoxTreeItem<File> {
	private boolean childrenLoaded = false;
	private FileFolderSelectorController bean;

	LazyFileTreeNode(File value, FileFolderSelectorController bean) {
		super(value);
		this.bean = bean;
	}

	@Override
	public boolean isLeaf() {
		if (childrenLoaded) {
			return getChildren().isEmpty();
		}
		return getValue().isFile();
	}

	@Override
	public ObservableList<TreeItem<File>> getChildren() {
		if (childrenLoaded) {
			return super.getChildren();
		}
		childrenLoaded = true;
		File[] files;
		try {
			files = Executors.privilegedCallable(() -> getValue().listFiles()).call();
		} catch (@SuppressWarnings("unused") Exception e) {// NOSONAR
			files = getValue().listFiles();
		}
		return bean.appendChildren(this, files).getChildren();
	}

	@Override
	public String toString() {
		return getValue().getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LazyFileTreeNode other = (LazyFileTreeNode) obj;
		if (getValue() == null) {
			if (other.getValue() != null)
				return false;
		} else if (!getValue().equals(other.getValue()))
			return false;
		return true;
	}

}