package com.sirma.itt.seip.runtime;

import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

/**
 * Resolver for {@link BootStrategy} implementation. If no other implementation is found then the
 * {@link DefaultBootStrategy} will be returned.
 *
 * @author BBonev
 */
@ApplicationScoped
public class BootStrategyResolver {

	@Inject
	private BeanManager beanManager;

	/**
	 * Resolves a boot strategy that can be used during startup.
	 *
	 * @return the boot strategy
	 */
	public BootStrategy resolveBootStrategy() {
		Set<Bean<?>> beans = beanManager.getBeans(BootStrategy.class);

		Bean<BootStrategy> provider = (Bean<BootStrategy>) resolveBeanStrategy(beans)
				.orElseThrow(() -> new ComponentValidationException("No " + BootStrategy.class + " instance found!"));

		CreationalContext<BootStrategy> cc = beanManager.createCreationalContext(provider);
		return (BootStrategy) beanManager.getReference(provider, BootStrategy.class, cc);
	}

	@SuppressWarnings("unchecked")
	private Optional<Bean<?>> resolveBeanStrategy(Set<Bean<?>> beans) {
		if (beans.isEmpty()) {
			return Optional.empty();
		} else if (beans.size() == 1) {
			return Optional.of(beanManager.resolve(beans));
		}
		return beans.stream()
				.filter(bean -> !DefaultBootStrategy.class.isAssignableFrom(bean.getBeanClass()))
				.findFirst();
	}
}
