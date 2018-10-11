package com.sirma.sep.export;

import java.io.Serializable;

import com.sirma.itt.seip.Named;

/**
 * Base export request that holds common properties for all requests.
 *
 * @author A. Kunchev
 */
public abstract class ExportRequest implements Named {

	/** The id of the target instance. */
	protected Serializable instanceId;

	/** The name of the generated file. */
	protected String fileName;

	public Serializable getInstanceId() {
		return instanceId;
	}

	public String getFileName() {
		return fileName;
	}

}
