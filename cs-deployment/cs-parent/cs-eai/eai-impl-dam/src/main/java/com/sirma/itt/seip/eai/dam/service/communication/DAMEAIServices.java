package com.sirma.itt.seip.eai.dam.service.communication;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * Enum of basic services available at a remote integrated system
 * 
 * @author bbanchev
 */
public enum DAMEAIServices implements EAIServiceIdentifier {
	/** Download content as binary data. */
	@Tag(1)
	CONTENT;

	@Override
	public String getServiceId() {
		return name();
	}
}
