/**
 *
 */
package com.sirma.itt.seip.tenant.wizard;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.testng.annotations.Test;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * @author BBonev
 */
@Test
public class SubsystemTenantAddressProviderTest {

	@Test(expectedExceptions = RollbackedRuntimeException.class, expectedExceptionsMessageRegExp = "Could not determine host to use!")
	public void noHosts_noPreferred() {
		new SubsystemTenantAddressProviderStub(Collections.emptyList(), Collections.emptySet(), 1)
				.provideAddressForNewTenant(null);
	}

	@Test(expectedExceptions = RollbackedRuntimeException.class, expectedExceptionsMessageRegExp = "Could not determine host to use!")
	public void noHosts_withPreferred() {
		new SubsystemTenantAddressProviderStub(Collections.emptyList(), Collections.emptySet(), 1)
				.provideAddressForNewTenant("localhost");
	}

	@Test
	public void resolveHost_maxReached() {
		Collection<String> usedAddresses = Arrays.asList("host1");
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1"));
		int maxTenantsPerHost = 1;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant(), URI.create("http://host1:8080"));
	}

	@Test
	public void resolveHost_noUsed() {
		Collection<String> usedAddresses = Collections.emptyList();
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1"));
		int maxTenantsPerHost = 1;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant(), URI.create("http://host1:8080"));
	}

	@Test(invocationCount = 5)
	public void resolveHost_usedWithOpenSpot() {
		Collection<String> usedAddresses = Arrays.asList("host1", "host1", "host2");
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1", "host2"));
		int maxTenantsPerHost = 2;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant(), URI.create("http://host2:8080"));
	}

	@Test
	public void resolveHost_allFull_useNew() {
		Collection<String> usedAddresses = Arrays.asList("host1", "host1");
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1", "host2"));
		int maxTenantsPerHost = 2;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant(), URI.create("http://host2:8080"));
	}

	@Test
	public void resolveHost_leastUsed() {
		Collection<String> usedAddresses = Arrays.asList("host1", "host1", "host2", "host2", "host3");
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1", "host2", "host3"));
		int maxTenantsPerHost = 3;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant(), URI.create("http://host3:8080"));
	}

	@Test
	public void resolveHost_preffered() {
		Collection<String> usedAddresses = Arrays.asList("host1", "host1", "host2", "host2", "host3", "host3");
		Set<String> availableAddresses = new HashSet<>(Arrays.asList("host1", "host2", "host3"));
		int maxTenantsPerHost = 3;
		SubsystemTenantAddressProvider provider = new SubsystemTenantAddressProviderStub(usedAddresses,
				availableAddresses, maxTenantsPerHost);
		assertEquals(provider.provideAddressForNewTenant("host2"), URI.create("http://host2:8080"));
	}

	private static class SubsystemTenantAddressProviderStub extends BaseSubsystemTenantAddressProvider {

		private Collection<String> usedAddresses;
		private Set<String> availableAddresses;
		private int maxTenantsPerHost;

		SubsystemTenantAddressProviderStub(Collection<String> usedAddresses, Set<String> availableAddresses,
				int maxTenantsPerHost) {
			this.usedAddresses = usedAddresses;
			this.availableAddresses = availableAddresses;
			this.maxTenantsPerHost = maxTenantsPerHost;
		}

		@Override
		public URI provideAddressForNewTenant(String preferredHost) {

			Optional<String> host = SubsystemTenantAddressProvider.resolveAvailableHost(usedAddresses.stream(),
					availableAddresses, maxTenantsPerHost, createUndeterminedPicker(preferredHost));

			if (!host.isPresent()) {
				throw new RollbackedRuntimeException("Could not determine host to use!");
			}

			try {
				return new URI("http", null, host.get(), 8080, null, null, null);
			} catch (URISyntaxException e) {
				throw new RollbackedRuntimeException(e);
			}
		}

	}
}
