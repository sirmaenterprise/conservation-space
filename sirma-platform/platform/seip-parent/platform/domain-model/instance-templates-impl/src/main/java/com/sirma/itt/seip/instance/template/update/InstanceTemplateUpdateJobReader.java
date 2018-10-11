package com.sirma.itt.seip.instance.template.update;

import javax.inject.Named;

import com.sirma.sep.instance.batch.reader.DefaultItemReader;

/**
 * Reader for the instance template update job.
 *
 * @author Adrian Mitev
 */
@Named
public class InstanceTemplateUpdateJobReader extends DefaultItemReader {

	@Override
	protected int getChunkSize() {
		return 20;
	}

}
