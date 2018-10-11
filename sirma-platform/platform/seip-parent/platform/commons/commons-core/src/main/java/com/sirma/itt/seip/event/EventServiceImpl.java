package com.sirma.itt.seip.event;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Default implementation that uses {@link BeanManager} to fire the events.
 *
 * @author BBonev
 */
@ApplicationScoped
public class EventServiceImpl implements EventService {

	@Inject
	private BeanManager beanManager;

	@Override
	public void fire(EmfEvent event, Annotation... qualifiers) {
		if (event == null) {
			return;
		}
		// check for null qualifiers
		Annotation[] localQualifiers = removeNullQualifiers(qualifiers);
		// register qualifiers if any
		registerQualifiers(event, localQualifiers);
		// fire the updated event
		beanManager.fireEvent(event, localQualifiers);
	}

	/**
	 * Removes the null qualifiers.
	 *
	 * @param qualifiers
	 *            the qualifiers
	 * @return the annotation[]
	 */
	private static Annotation[] removeNullQualifiers(Annotation... qualifiers) {
		if (qualifiers != null && qualifiers.length > 0) {
			List<Annotation> list = new ArrayList<>(qualifiers.length);
			for (int i = 0; i < qualifiers.length; i++) {
				Annotation annotation = qualifiers[i];
				if (annotation != null) {
					list.add(annotation);
				}
			}
			return list.toArray(new Annotation[list.size()]);
		}
		return qualifiers;
	}

	/**
	 * Register qualifiers to the event instance if the instance is a {@link ContextEvent}
	 *
	 * @param event
	 *            the event
	 * @param qualifiers
	 *            the qualifiers
	 */
	private static void registerQualifiers(EmfEvent event, Annotation... qualifiers) {
		if (event instanceof ContextEvent && qualifiers != null && qualifiers.length > 0) {
			((ContextEvent) event).addToContext(ContextEvent.QUALIFIERS, qualifiers);
		}
	}
}
