package com.sirma.itt.seip.configuration;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.Synchronizer;
import com.sirma.itt.seip.context.ValidatingContextualReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.exception.TenantNotActiveException;

/**
 * Multi tenant configuration property. Calling methods {@link #get()}, {@link #isSet()} and {@link #valueUpdated()}
 * outside tenant context will result in {@link TenantNotActiveException}. The methods
 * {@link #addConfigurationChangeListener(ConfigurationChangeListener)} and
 * {@link #removeConfigurationChangeListener(ConfigurationChangeListener)} could be called in any context.
 *
 * @param <T>
 *            the generic type
 */
public class MultiTenantConfigurationProperty<T> extends ValidatingContextualReference<ConfigurationProperty<T>>
		implements ConfigurationProperty<T> {
	private static final long serialVersionUID = 1951306305535961997L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Synchronizer synchronizer;
	private final List<ConfigurationChangeListener<T>> callbacks = new CopyOnWriteArrayList<>();
	private final List<Consumer<T>> finalizers = new CopyOnWriteArrayList<>();
	private final String name;
	private final ConfigurationInstance definition;

	/**
	 * Instantiates a new multi tenant configuration property.
	 *
	 * @param instance
	 *            the configuration definition instance. Should be the definition of the configuration that defines the
	 *            current configuration
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param tenantRemoveObserverRegister
	 *            the tenant remove observer register
	 * @param synchronizer
	 *            the synchronizer used for synchronizations
	 * @param propertySupplier
	 *            the property supplier used to provide configuration properties to hold then per tenant values
	 */
	public MultiTenantConfigurationProperty(ConfigurationInstance instance, Supplier<String> contextIdSupplier,
			Consumer<Consumer<TenantInfo>> tenantRemoveObserverRegister, Synchronizer synchronizer,
			Supplier<ConfigurationProperty<T>> propertySupplier) {
		super(contextIdSupplier, propertySupplier,
				(p) -> Objects.requireNonNull(p, "Configuration property cannot be null"));

		this.definition = instance;
		this.name = instance.getName();
		this.synchronizer = synchronizer;

		tenantRemoveObserverRegister.accept(this::tenantRemoved);
	}

	@Override
	public T get() {
		return getContextValue().get();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Synchronizer getSynchronizer() {
		return synchronizer;
	}

	@Override
	public ConfigurationInstance getDefinition() {
		return definition;
	}

	@Override
	public void valueUpdated() {
		ConfigurationProperty<T> property = getContextValue();
		if (property.isInitialized()) {
			notifyForValueDisposal(property.get());
		}
		property.valueUpdated();
	}

	protected void notifyForChange(ConfigurationProperty<T> property) {
		try {
			callbacks.forEach(callback -> callback.onConfigurationChange(property));
		} catch (Exception e) {
			LOGGER.warn("Failed to execute callback on property change", e);
		}
	}

	protected void notifyForValueDisposal(T oldValue) {
		try {
			finalizers.forEach(destroyer -> destroyer.accept(oldValue));
		} catch (Exception e) {
			LOGGER.warn("Failed to execute callback on property value disposal", e);
		}
	}

	@Override
	public ConfigurationProperty<T> getContextValue() {
		ConfigurationProperty<T> property = getFromStore();
		if (property == null) {
			getSynchronizer().beginWrite();
			try {
				property = getFromStore();
				if (property == null) {
					property = createNewValueAndStoreIt();
				}
			} catch (Exception e) {
				throw new EmfRuntimeException(
						"Failed to retrive configuration property " + getName() + " for tenant " + getContextId(), e);
			} finally {
				getSynchronizer().endWrite();
			}
			// for new value properties register change listener that should be called when the actual property changes
			// it's value. We pass the current instance and not the changed property because all observers will be
			// notified and they should access the data via the current proxy instance and not the actual instance
			property.addConfigurationChangeListener(c -> notifyForChange(this));
		}
		return property;
	}

	/**
	 * Removes the tenant configurations. This method could be called at any time but if the tenant is not deleted the
	 * property will be initialized again.
	 *
	 * @param tenantContext
	 *            the tenant id
	 */
	void tenantRemoved(TenantInfo tenantContext) {
		removeFromStore(tenantContext.getTenantId());
	}

	@Override
	public void addConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		((CopyOnWriteArrayList<ConfigurationChangeListener<T>>) callbacks).addIfAbsent(callback);
	}

	// suppress warning for issue about performance of remove operation of CopyOnWriteArrayList
	// this is safe here as it's rarely used
	@SuppressWarnings("squid:S2250")
	@Override
	public void removeConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		callbacks.remove(callback);
	}

	@Override
	public void addValueDestroyListener(Consumer<T> oldValueConsumer) {
		((CopyOnWriteArrayList<Consumer<T>>) finalizers).addIfAbsent(oldValueConsumer);
	}

	@Override
	public boolean isInitialized() {
		ConfigurationProperty<T> property = getFromStore();
		return property != null && property.isInitialized();
	}

	@Override
	public boolean isSet() {
		return ConfigurationProperty.super.isSet();
	}

	@Override
	public boolean isNotNull() {
		return isSet();
	}

}
