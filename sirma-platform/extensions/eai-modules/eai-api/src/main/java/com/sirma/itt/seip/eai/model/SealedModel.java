package com.sirma.itt.seip.eai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sirma.itt.seip.Sealable;

/**
 * Base implementation for sealed models. Although class has a default implementation it is not intended for a direct
 * usage
 * 
 * @author bbanchev
 */
public abstract class SealedModel implements Sealable {
	@JsonIgnore
	private boolean sealed = false;

	@Override
	public void seal() {
		sealed = true;
	}

	@Override
	@JsonIgnore
	public boolean isSealed() {
		return sealed;
	}

}
