package com.sirma.itt.emf.instance.observer;

import static org.mockito.Mockito.mock;

import java.io.Serializable;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class AutolinkObserverTest.
 *
 * @author BBonev
 */
@Test
public class AutolinkObserverTest extends EmfTest {

	/** The observer. */
	@InjectMocks
	private AutolinkObserver observer;

	/** The link service. */
	@Mock
	private LinkService linkService;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		super.beforeMethod();
		createTypeConverter();
		LinkConstants.init(mock(SecurityContextManager.class), ContextualMap.create());
	}

	/**
	 * On after instance created_ ref.
	 */
	public void onAfterInstanceCreated_Ref() {
		EmfInstance child = createInstance("emf:instance");
		EmfInstance parent = createInstance("emf:parent");
		child.setOwningReference(parent.toReference());
		// this should not be used
		EmfInstance parentInst = new EmfInstance();
		parent.setId("emf:parentInst");
		child.setOwningInstance(parentInst);

		observer.onAfterInstanceCreated(new AfterInstancePersistEvent<Instance, TwoPhaseEvent>(child));
		verifyParetToChildLinks(parent, child);
	}

	/**
	 * On after instance created_ inst.
	 */
	public void onAfterInstanceCreated_Inst() {
		EmfInstance child = createInstance("emf:instance");
		EmfInstance parent = createInstance("emf:parent");
		child.setOwningInstance(parent);

		observer.onAfterInstanceCreated(new AfterInstancePersistEvent<Instance, TwoPhaseEvent>(child));
		verifyParetToChildLinks(parent, child);
	}

	/**
	 * Verify paret to child links.
	 *
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 */
	private void verifyParetToChildLinks(EmfInstance parent, EmfInstance child) {
		Mockito.verify(linkService).linkSimple(parent.toReference(), child.toReference(),
				LinkConstants.TREE_PARENT_TO_CHILD, LinkConstants.TREE_CHILD_TO_PARENT);
		Mockito.verify(linkService).link(parent.toReference(), child.toReference(), LinkConstants.PARENT_TO_CHILD,
				LinkConstants.CHILD_TO_PARENT, LinkConstants.getDefaultSystemProperties());
	}

	/**
	 * Creates the instance.
	 *
	 * @param id
	 *            the id
	 * @return the emf instance
	 */
	private EmfInstance createInstance(Serializable id) {
		EmfInstance child = new EmfInstance();
		child.setId(id);
		return child;
	}

}
