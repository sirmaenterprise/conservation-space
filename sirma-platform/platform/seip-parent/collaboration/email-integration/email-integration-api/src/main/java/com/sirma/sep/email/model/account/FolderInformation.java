package com.sirma.sep.email.model.account;

import java.util.List;

/**
 * Generic folder information class. Can store information on inner folders.
 *
 * @author g.tsankov
 */
public class FolderInformation {
	private String id;
	private String name;
	private Integer itemsCount;
	private List<FolderInformation> innerFolders;

	public FolderInformation(String id, String name, Integer itemsCount, List<FolderInformation> innerFolders) {
		this.id = id;
		this.name = name;
		this.itemsCount = itemsCount;
		this.innerFolders = innerFolders;
	}

	public FolderInformation() {
		// indicates non existent folder
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getItemsCount() {
		return this.itemsCount;
	}

	public void setItemsCount(Integer itemsCount) {
		this.itemsCount = itemsCount;
	}

	public List<FolderInformation> getInnerFolders() {
		return innerFolders;
	}

	public void setInnerFolders(List<FolderInformation> innerFolders) {
		this.innerFolders = innerFolders;
	}
}
