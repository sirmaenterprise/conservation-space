package com.sirma.itt.seip.db.discovery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

/**
 * Default implementation of {@link EntityDiscovery} that uses {@link FastClasspathScanner} to lookup for persistence
 * classes. The instance caches the results after the first scan. <br>
 * The scanning is multi thread safe via internal synchronization
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 2017-04-11
 */
class FastClasspathScannerEntityDiscovery implements EntityDiscovery {

	protected static final String[] SEP_ROOT_PACKAGES = { "com.sirma", "com.sirmaenterprise" };
	/**
	 * Singleton instance that scans only once the class path for entities. For dynamic class discovery do not use this
	 * instance, but rather a new instance each type a discovery is needed
	 */
	public static final EntityDiscovery INSTANCE = new FastClasspathScannerEntityDiscovery();
	private static final String NO_BINDING = "$NO_BINDING$";
	private Map<String, Set<Class<?>>> entities = new ConcurrentHashMap<>();
	private String[] scanSpec;

	/**
	 * Instantiate the instance with it's defaults
	 */
	public FastClasspathScannerEntityDiscovery() {
		this(SEP_ROOT_PACKAGES);
	}

	/**
	 * Instantiate with custom scanning specification.
	 *
	 * @param scanSpec defines the white/black listed packages for scanning.
	 * @see FastClasspathScanner
	 */
	public FastClasspathScannerEntityDiscovery(String... scanSpec) {
		this.scanSpec = scanSpec;
	}

	@Override
	public Collection<Class<?>> getEntities(String persistenceUnit) {
		String key = persistenceUnit == null ? NO_BINDING : persistenceUnit;
		return entities.computeIfAbsent(key, unit -> {
			Set<Class<?>> local = new LinkedHashSet<>();
			ClassAnnotationMatchProcessor processor = filterByPersistenceUnit(unit, local::add);
			new FastClasspathScanner(scanSpec)
					.matchClassesWithAnnotation(javax.persistence.Entity.class, processor)
					.matchClassesWithAnnotation(javax.persistence.MappedSuperclass.class, processor)
					.matchClassesWithAnnotation(javax.persistence.Embeddable.class, processor)
					.scan();
			return Collections.unmodifiableSet(local);
		});
	}

	private static ClassAnnotationMatchProcessor filterByPersistenceUnit(String unitName, Consumer<Class<?>> consumer) {
		return clazz -> {
			PersistenceUnitBinding binding = clazz.getAnnotation(PersistenceUnitBinding.class);
			if (binding != null && !NO_BINDING.equals(unitName)) {
				if (matchUnitName(unitName, binding.value())) {
					consumer.accept(clazz);
				}
			} else {
				consumer.accept(clazz);
			}
		};
	}

	private static boolean matchUnitName(String unitName, String[] values) {
		for (String value : values) {
			if (value.equals(unitName)) {
				return true;
			}
		}
		return false;
	}
}
