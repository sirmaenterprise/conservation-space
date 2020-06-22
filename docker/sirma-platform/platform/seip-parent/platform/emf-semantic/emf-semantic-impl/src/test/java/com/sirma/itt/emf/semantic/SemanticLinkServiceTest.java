/**
 *
 */
package com.sirma.itt.emf.semantic;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.search.SemanticLinkServiceMock;
import com.sirma.itt.emf.semantic.persistence.SemanticLinkServiceImpl;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * @author kirq4e
 */
public class SemanticLinkServiceTest extends GeneralSemanticTest<SemanticLinkServiceImpl> {

	@BeforeClass
	public void init() {
		service = new SemanticLinkServiceMock(context);
	}

	/**
	 * Tests isLinked method
	 */
	@Test
	public void testIsLinked() {
		noTransaction();
		// all parameters are null
		InstanceReference from = null;
		InstanceReference to = null;
		String linkId = null;
		boolean isLinked = service.isLinked(from, to, linkId);
		Assert.assertFalse(isLinked);

		// pass from parameter
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		linkId = "emf:references";
		isLinked = service.isLinked(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass to parameter
		from = null;
		to = new InstanceReferenceMock();
		to.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		linkId = "emf:references";
		isLinked = service.isLinked(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass from and to parameter
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		to = new InstanceReferenceMock();
		to.setId("emf:12c4b931-0bb8-455f-a38d-3de27be6b956");
		linkId = "emf:references";
		isLinked = service.isLinked(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass from and to parameter and link id - null
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		to = new InstanceReferenceMock();
		to.setId("emf:12c4b931-0bb8-455f-a38d-3de27be6b956");
		linkId = null;
		isLinked = service.isLinked(from, to, linkId);
		Assert.assertFalse(isLinked);
	}

	@Test
	public void testIsLinkedInternal() {
		noTransaction();
		// all parameters are null
		InstanceReference from = null;
		InstanceReference to = null;
		String linkId = null;
		boolean isLinked = service.isLinkedSimple(from, to, linkId);
		Assert.assertFalse(isLinked);

		// pass from parameter
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		linkId = "emf:references";
		isLinked = service.isLinkedSimple(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass to parameter
		from = null;
		to = new InstanceReferenceMock();
		to.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		linkId = "emf:references";
		isLinked = service.isLinkedSimple(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass from and to parameter
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		to = new InstanceReferenceMock();
		to.setId("emf:12c4b931-0bb8-455f-a38d-3de27be6b956");
		linkId = "emf:references";
		isLinked = service.isLinkedSimple(from, to, linkId);
		Assert.assertTrue(isLinked);

		// pass from and to parameter and link id - null
		from = new InstanceReferenceMock();
		from.setId("emf:b6b4ed3b-cf62-4171-850e-0379723894db");
		to = new InstanceReferenceMock();
		to.setId("emf:12c4b931-0bb8-455f-a38d-3de27be6b956");
		linkId = null;
		isLinked = service.isLinked(from, to, linkId);
		Assert.assertFalse(isLinked);
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticLinkService.ttl";
	}

}
