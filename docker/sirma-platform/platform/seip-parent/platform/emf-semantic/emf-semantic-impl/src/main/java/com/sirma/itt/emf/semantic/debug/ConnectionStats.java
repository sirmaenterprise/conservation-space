package com.sirma.itt.emf.semantic.debug;

import java.io.Serializable;

/**
 * Represents semantic connections statistics DTO
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 06/03/2019
 */
public class ConnectionStats implements Serializable {
	private String id;
	private long createCount;
	private long readCount;
	private long updateCount;
	private long removeCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreateCount() {
		return createCount;
	}

	public void setCreateCount(long createCount) {
		this.createCount = createCount;
	}

	public long getReadCount() {
		return readCount;
	}

	public void setReadCount(long readCount) {
		this.readCount = readCount;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	public long getRemoveCount() {
		return removeCount;
	}

	public void setRemoveCount(long removeCount) {
		this.removeCount = removeCount;
	}
}
