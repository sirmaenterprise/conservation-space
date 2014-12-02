package com.sirma.itt.pm.mock.action;

import javax.enterprise.inject.Specializes;

import com.sirma.cmf.mock.beans.Dummy;
import com.sirma.itt.emf.security.Secure;

/**
 * @author BBonev
 */
@Specializes
public class PmDummy extends Dummy {
	/**
	 * Test.
	 */
	@Secure
	public void test() {

	}
}
