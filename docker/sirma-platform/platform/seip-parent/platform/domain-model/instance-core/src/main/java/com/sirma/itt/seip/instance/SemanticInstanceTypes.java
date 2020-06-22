package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static java.lang.reflect.Proxy.isProxyClass;
import static java.lang.reflect.Proxy.newProxyInstance;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.AbstractInvocationHandler;
import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.types.NoClassInstnaceTypeResolver;
import com.sirma.itt.seip.instance.types.NoClassInstnaceTypeResolver.PluginConfiguration;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * {@link InstanceTypes} implementation that loads the types from the {@link ClassInstance}s provided by the
 * {@link SemanticDefinitionService}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticInstanceTypes implements InstanceTypes {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private javax.enterprise.inject.Instance<InstanceTypeResolver> instanceTypeResolver;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private TypeMappingProvider typeMapping;
	@Inject
	@ExtensionPoint(value = PluginConfiguration.NAME)
	private Plugins<NoClassInstnaceTypeResolver> noClassInstanceTypeResolvers;

	@Override
	public boolean is(InstanceReference reference, Serializable type) {
		Optional<InstanceType> currentType = resolveImmediately(reference);
		Optional<InstanceType> referenceType = resolveImmediately(type);
		if (referenceType.isPresent()) {
			return currentType.filter(aType -> aType.instanceOf(referenceType.get())).isPresent();
		}
		return false;
	}

	@Override
	public boolean hasTrait(InstanceReference reference, Serializable trait) {
		String traitAsString = typeConverter.tryConvert(String.class, trait);
		if (StringUtils.isBlank(traitAsString)) {
			return false;
		}
		return resolveImmediately(reference).filter(type -> type.hasTrait(traitAsString)).isPresent();
	}

	@Override
	public Collection<InstanceType> forCategory(String category) {
		if (StringUtils.isBlank(category)) {
			return Collections.emptyList();
		}
		String expandedName = typeMapping.getDataTypeName(category);
		return semanticDefinitionService
				.getClasses()
					.stream()
					.map(ClassInstance::type)
					.filter(filterByCategory(category, expandedName))
					.collect(Collectors.toSet());
	}

	private static Predicate<InstanceType> filterByCategory(String category, String expandedName) {
		return type -> {
			String current = type.getCategory();
			return nullSafeEquals(current, category) || nullSafeEquals(current, expandedName);
		};
	}

	@Override
	public Optional<InstanceType> from(Serializable source) {
		if (source == null) {
			return Optional.empty();
		}
		String type = typeConverter.tryConvert(String.class, source);
		return resolveType(type);
	}

	private Optional<InstanceType> resolveType(String source) {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(source);
		if (classInstance != null) {
			return Optional.of(classInstance.type());
		}

		return noClassInstanceTypeResolvers
					.select(resolver -> resolver.canResolve(source))
					.map(resolver -> resolver.resolve(source))
					.orElseGet(() -> { // should not get here, because of the last resolver
						LOGGER.trace("Failed to resolve type for instance - {}", source);
						return Optional.empty();
					});
	}

	@Override
	public Optional<InstanceType> from(Instance source) {
		if (source == null) {
			return Optional.empty();
		}
		return resolveLazily(source);
	}

	@Override
	public Optional<InstanceType> from(InstanceReference source) {
		if (source == null) {
			return Optional.empty();
		}
		return resolveLazily(source);
	}

	/**
	 * Returns a {@link InstanceType} {@link Proxy} that evaluates when the type is used
	 */
	private Optional<InstanceType> resolveLazily(Serializable source) {
		InstanceType lazyLoadedType = getKnowLazyLoadedType(source);
		// if the given source contains already lazy loaded type
		if (lazyLoadedType != null) {
			return Optional.of(lazyLoadedType);
		}
		InstanceType instanceType = buildProxy(resolveTypeFromChain(buildResolveChain(source)));
		setType(source, instanceType);
		return Optional.of(instanceType);
	}

	/**
	 * Does not build a proxy object but rather resolve the type immediately
	 */
	private Optional<InstanceType> resolveImmediately(Serializable source) {
		if (source == null) {
			return Optional.empty();
		}
		InstanceType lazyLoaded = getKnowLazyLoadedType(source);
		if (lazyLoaded != null) {
			return Optional.of(lazyLoaded);
		}
		InstanceType instanceType = resolveTypeFromChain(buildResolveChain(source)).get();
		setType(source, instanceType);
		return Optional.ofNullable(instanceType);
	}

	private static InstanceType getKnowLazyLoadedType(Serializable source) {
		if (source instanceof InstanceType) {
			return (InstanceType) source;
		} else if (source instanceof InstanceReference) {
			return resetProxy(((InstanceReference) source).getType());
		} else if (source instanceof Instance) {
			return resetProxy(((Instance) source).type());
		}
		return null;
	}

	private static InstanceType resetProxy(InstanceType instanceType) {
		// if not a proxy class we should return null so the code will fetch new copy and not use the old instance in
		// case the resolve method is called to refresh the type
		if (instanceType == null || !isProxyClass(instanceType.getClass())) {
			return null;
		}
		if (instanceType.getId() == null) {
			// the proxy is not valid and should be resolved again
			return null;
		}
		// this here will reset and force proxy reuse instead of creating new instance from scratch
		InvocationHandler handler = Proxy.getInvocationHandler(instanceType);
		if (handler instanceof InstanceTypeProxyHandler) {
			Resettable.reset(((InstanceTypeProxyHandler) handler).source);
		} else {
			throw new IllegalArgumentException("The provided InstanceType proxy was produced from other source");
		}
		return instanceType;
	}

	private List<Serializable> buildResolveChain(Serializable source) {
		List<Serializable> resolveChain = new ArrayList<>(3);
		if (source instanceof InstanceReference) {
			InstanceReference reference = (InstanceReference) source;
			if (reference.getType() != null) {
				addNonNullValue(resolveChain, reference.getType().getId());
			}
			addNonNullValue(resolveChain, reference.getId());
		} else if (source instanceof Instance) {
			Instance instance = (Instance) source;
			if (instance.type() != null) {
				addNonNullValue(resolveChain, instance.type().getId());
			}
			addNonNullValue(resolveChain, instance.getAsString(SEMANTIC_TYPE));
			addNonNullValue(resolveChain, instance.getId());
		} else {
			addNonNullValue(resolveChain, typeConverter.tryConvert(String.class, source));
		}
		return resolveChain;
	}

	private Supplier<InstanceType> resolveTypeFromChain(List<Serializable> resolveChain) {
		return () -> resolveChain
				.stream()
					.map(this::from)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst()
					.orElse(null);
	}

	private static void setType(Serializable target, InstanceType instanceType) {
		if (target instanceof InstanceReference) {
			((InstanceReference) target).setType(instanceType);
		} else if (target instanceof Instance) {
			((Instance) target).setType(instanceType);
		}
	}

	@Override
	public Optional<InstanceType> from(DefinitionModel model) {
		if (model == null) {
			return Optional.empty();
		}
		return model.getField(SEMANTIC_TYPE).map(PropertyDefinition::getDefaultValue).flatMap(this::from);
	}

	@Override
	public void resolveTypes(Collection<? extends Instance> instances) {
		if (isEmpty(instances)) {
			return;
		}
		instances.stream().filter(hasSemanticType()).forEach(this::from);

		Set<Serializable> toResolve = instances
				.stream()
					.filter(hasSemanticType().negate())
					.map(Instance::getId)
					.collect(Collectors.toSet());

		Map<Serializable, InstanceType> resolved = instanceTypeResolver.get().resolve(toResolve);

		for (Instance toUpdate : instances) {
			InstanceType type = resolved.get(toUpdate.getId());
			if (type != null) {
				toUpdate.setType(type);
			}
		}
	}

	private static Predicate<Instance> hasSemanticType() {
		return instance -> instance.type() != null || instance.isValueNotNull(SEMANTIC_TYPE);
	}

	/**
	 * Build a {@link InstanceType} {@link Proxy} instance that will call the given supplier on first use of any of the
	 * methods of the return instance.
	 *
	 * @param instanceTypeSupplier
	 *            supplier to use to provide the actual instance type
	 * @return instance type proxy instance
	 */
	private static InstanceType buildProxy(Supplier<InstanceType> instanceTypeSupplier) {
		return (InstanceType) newProxyInstance(InstanceType.class.getClassLoader(), new Class[] { InstanceType.class },
				new InstanceTypeProxyHandler(instanceTypeSupplier));
	}

	/**
	 * Proxy handler that stores the code for lazy evaluation of an instance type
	 *
	 * @author BBonev
	 */
	private static class InstanceTypeProxyHandler extends AbstractInvocationHandler {

		private final Supplier<InstanceType> source;

		InstanceTypeProxyHandler(Supplier<InstanceType> source) {
			this.source = new CachingSupplier<>(source);
		}

		@Override
		protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
			InstanceType instanceType = source.get();
			if (instanceType == null) {
				if (method.getReturnType().equals(boolean.class)) {
					return Boolean.FALSE;
				}
				return null;
			}
			return method.invoke(instanceType, args);
		}

		@Override
		public String toString() {
			return source.get().toString();
		}

		@Override
		public int hashCode() {
			return source.get().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof InstanceTypeProxyHandler) {
				InstanceTypeProxyHandler other = (InstanceTypeProxyHandler) obj;
				return Objects.equals(source.get(), other.source.get());
			}
			if (obj instanceof InstanceType) {
				return Objects.equals(source.get(), obj);
			}
			return super.equals(obj);
		}
	}
}
