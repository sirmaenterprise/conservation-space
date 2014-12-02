package com.sirma.itt.emf.instance.observer;

import java.io.Serializable;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class AutolinkObserverTest.
 * 
 * @author BBonev
 */
@Test
public class AutolinkObserverTest extends EmfTest {

	/** The observer. */
	private AutolinkObserver observer;

	/** The link service. */
	private LinkService linkService;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		createTypeConverter();
		observer = new AutolinkObserver();
		linkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setField(observer, "linkService", linkService);
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

		observer.onAfterInstanceCreated(new AfterInstancePersistEvent<Instance, TwoPhaseEvent>(
				child));
		verifyParetToChildLinks(parent, child);
	}

	/**
	 * On after instance created_ inst.
	 */
	public void onAfterInstanceCreated_Inst() {
		EmfInstance child = createInstance("emf:instance");
		EmfInstance parent = createInstance("emf:parent");
		child.setOwningInstance(parent);

		observer.onAfterInstanceCreated(new AfterInstancePersistEvent<Instance, TwoPhaseEvent>(
				child));
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
		Mockito.verify(linkService).link(parent.toReference(), child.toReference(),
				LinkConstants.PARENT_TO_CHILD, LinkConstants.CHILD_TO_PARENT,
				LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
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

	/**
	 * On instance attached event.
	 */
	public void onInstanceAttachedEvent() {
		EmfInstance child = createInstance("emf:instance");
		EmfInstance parent = createInstance("emf:parent");

		observer.onInstanceAttachedEvent(new InstanceAttachedEvent<Instance>(parent, child));
		verifyParetToChildLinks(parent, child);

		Mockito.verify(linkService).linkSimple(child.toReference(), parent.toReference(),
				LinkConstants.PART_OF_URI);
	}

	/**
	 * On instance detached event.
	 */
	public void onInstanceDetachedEvent() {
		EmfInstance child = createInstance("emf:instance");
		EmfInstance parent = createInstance("emf:parent");

		observer.onInstanceDetachedEvent(new InstanceDetachedEvent<Instance>(parent, child));

		Mockito.verify(linkService).unlink(parent.toReference(), child.toReference(),
				LinkConstants.PARENT_TO_CHILD, LinkConstants.CHILD_TO_PARENT);
		Mockito.verify(linkService).unlinkSimple(child.toReference(), parent.toReference(),
				LinkConstants.PART_OF_URI);

		Mockito.verify(linkService).unlinkSimple(parent.toReference(), child.toReference(),
				LinkConstants.TREE_PARENT_TO_CHILD, LinkConstants.TREE_CHILD_TO_PARENT);
	}

	/**
	 * On instance deleted.
	 */
	public void onInstanceDeleted() {
		EmfInstance instance = createInstance("emf:instance");
		observer.onInstanceDeleted(new AfterInstanceDeleteEvent<Instance, TwoPhaseEvent>(instance));
		Mockito.verify(linkService).removeLinksFor(instance.toReference());
	}

}
