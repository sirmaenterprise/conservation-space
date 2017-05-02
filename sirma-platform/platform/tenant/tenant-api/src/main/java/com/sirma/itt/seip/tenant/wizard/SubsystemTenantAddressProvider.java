/**
 *
 */
package com.sirma.itt.seip.tenant.wizard;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Provides address where to initialize concrete subsystem when new tenant is created.
 * <p>
 * This could act as a dispatcher for new tenants. For example if there are 2 servers that can facilitate a particular
 * sub system and one of them has more tenants than the other the provider purpose is to determine that and balance the
 * load.
 *
 * @author BBonev
 */
public interface SubsystemTenantAddressProvider {

	/**
	 * Provide address for new tenant.
	 *
	 * @return the uri to the host
	 */
	default URI provideAddressForNewTenant() {
		return provideAddressForNewTenant(null);
	}

	/**
	 * Provide address for new tenant.
	 *
	 * @param preferredHost
	 *            the preferred server host if available or <code>null</code>
	 * @return the uri to the host
	 */
	URI provideAddressForNewTenant(String preferredHost);

	/**
	 * Calculate available host by using the given {@link Stream} of used addresses and the set of available addresses
	 * that could be used. If available hosts set is empty then the undeterminedPicker will be called with empty
	 * collection so it will be to him to decide what to happen in this case.
	 * <p>
	 * The algorithm tries to find the least used host with usage lowered than the given threshold. If no such host is
	 * found then the {@code undeterminedPicker} function will be called with the available options to pick one. The
	 * method returns a host until it reaches the given threshold then will try to select new host.
	 *
	 * @param usedHosts
	 *            the currently occupied hosts
	 * @param availableHosts
	 *            the available hosts. If empty the undeterminedPicker will be called with empty collection so it will
	 *            be to him to decide what to happen in this case.
	 * @param hostThreshold
	 *            the max usage per host
	 * @param undeterminedPicker
	 *            the undetermined address picker is called pick one of the values based on some other algorithm. This
	 *            is called when no deterministic decision could be made or all of the passed addresses could be fine to
	 *            for pick. Here could be passed a reference to the method {@link #getRandomHost(Collection)} or
	 *            function that returns the first element of the argument
	 * @return the optional
	 */
	static Optional<String> resolveAvailableHost(Stream<String> usedHosts, Set<String> availableHosts,
			int hostThreshold, Function<Collection<String>, String> undeterminedPicker) {

		if (availableHosts.isEmpty()) {
			// if provided picker has some default value it could be returned
			return Optional.ofNullable(undeterminedPicker.apply(availableHosts));
		}

		Map<String, Long> distribution = usedHosts.collect(CollectionUtils.distribution());

		String host = null;

		// nothing is used select random host and return it
		if (distribution.isEmpty()) {
			host = undeterminedPicker.apply(availableHosts);
		} else {
			// some of the host may have been removed from available hosts they should not be
			// included in the calculations
			distribution.keySet().retainAll(availableHosts);

			// find the host that has fewer tenants that the threshold
			Optional<Entry<String, Long>> min = distribution
					.entrySet()
						.stream()
						.filter(e -> e.getValue().longValue() < hostThreshold)
						.min((e1, e2) -> e1.getValue().compareTo(e2.getValue()));

			boolean areMoreThanOne = distribution
					.values()
						.stream()
						.filter(e -> e.longValue() < hostThreshold)
						.count() > 1;

			boolean allAreEqual = distribution
					.values()
						.stream()
						.filter(e -> e.longValue() < hostThreshold)
						.collect(CollectionUtils.distribution())
						.size() == 1;

			// this is needed because the min result above is not correct if all are of the same
			// value so we should call the picker to chose one (probably we have a preferred value)
			boolean allAreEquallyLoaded = areMoreThanOne && allAreEqual;

			// if such exists use it
			if (min.isPresent() && !allAreEquallyLoaded) {
				host = min.get().getKey();
			} else if (availableHosts.size() == distribution.size()) {
				// we have more than one choice call the picker
				if (allAreEquallyLoaded) {
					host = undeterminedPicker.apply(availableHosts);
				} else {
					// all hosts are full and we will try to find the host that is fewer tenants
					min = distribution.entrySet().stream().min(Entry.comparingByValue());
					host = min.get().getKey();
				}
			} else {
				// the used hosts are full get new from the pool
				HashSet<String> allAvailable = new HashSet<>(availableHosts);
				allAvailable.removeAll(distribution.keySet());
				host = undeterminedPicker.apply(allAvailable);
			}
		}
		return Optional.of(host);
	}
}
