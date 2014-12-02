package com.sirma.itt.objects.web.mock;

import javax.enterprise.inject.Model;
import javax.inject.Named;

/**
 * The Class ODummy.
 * 
 * @author BBonev
 */
@Model
@Named("odummy")
public class ODummy {

	/**
	 * Test.
	 */
	public void test() {
		System.out.println("o dummy");
	}

}
