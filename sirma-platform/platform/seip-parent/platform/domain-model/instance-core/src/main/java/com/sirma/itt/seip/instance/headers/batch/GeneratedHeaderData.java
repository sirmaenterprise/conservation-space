package com.sirma.itt.seip.instance.headers.batch;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Item used to pass from {@link InstanceHeaderItemProcessor} to {@link InstanceHeaderItemWriter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
public class GeneratedHeaderData implements Serializable {
	private final String instanceId;
	private final String header;

	/**
	 * Instantiate the item with the given instance id and header value
	 *
	 * @param instance the instance to fetch data from
	 * @param header the new header to persist
	 */
	GeneratedHeaderData(Instance instance, String header) {
		this(instance.getId().toString(), header);
	}

	/**
	 * Initialize the item with the given data
	 *  @param instanceId the affected instance identifier
	 * @param header the new value to set
	 */
	GeneratedHeaderData(String instanceId, String header) {
		this.instanceId = instanceId;
		this.header = header;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getHeader() {
		return header;
	}
}
