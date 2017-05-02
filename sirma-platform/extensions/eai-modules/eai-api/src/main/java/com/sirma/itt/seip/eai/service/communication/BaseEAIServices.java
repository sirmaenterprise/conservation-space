package com.sirma.itt.seip.eai.service.communication;

/**
 * Enum of basic services available at a remote integrated system
 * 
 * @author bbanchev
 */
public enum BaseEAIServices implements EAIServiceIdentifier {
	/** Search by query in external system. */
	SEARCH,
	/** Search single full object from external system. */
	RETRIEVE,
	/** Indicate error in SEIP to external system. */
	LOGGING,
	/** Direct access by full URI. */
	DIRECT;

	@Override
	public String getServiceId() {
		return name();
	}
}
