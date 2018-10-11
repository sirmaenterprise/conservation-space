package com.sirmaenterprise.sep.jms.impl.receiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents a snapshot of all registered  receivers and their current status.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/05/2017
 */
public class ReceiversInfo implements Serializable {

	private List<DestinationInfo> values = new LinkedList<>();

	public List<DestinationInfo> getValues() {
		return values;
	}

	/**
	 * Add new entry for the given destination, selector and the receiver status.
	 *
	 * @param destination the destination jndi
	 * @param selector the selector if any
	 * @param status the receiver status for the given destination
	 */
	public void add(String destination, String selector, String status) {
		Optional<DestinationInfo> destinationStatus = values.stream()
				.filter(d -> EqualsHelper.nullSafeEquals(d.getDestination(), destination))
				.findFirst();
		if (destinationStatus.isPresent()) {
			destinationStatus.get().add(selector, status);
		} else {
			DestinationInfo newDestination = new DestinationInfo(destination);
			newDestination.add(selector, status);
			values.add(newDestination);
		}
	}

	/**
	 * Represents the status for a single destination.
	 */
	public static class DestinationInfo implements Serializable {
		private String destination;
		private List<ReaderInfo> readers = new ArrayList<>(5);

		/**
		 * Initialize new destination info
		 *
		 * @param destination the destination jndi
		 */
		public DestinationInfo(String destination) {
			this.destination = destination;
		}

		public String getDestination() {
			return destination;
		}

		public List<ReaderInfo> getReaders() {
			return readers;
		}

		/**
		 * Add info for destination that matches the given selector and the receiver status
		 * @param selector
		 * @param status
		 */
		public void add(String selector, String status) {
			readers.add(new ReaderInfo(selector, status));
		}

	}

	/**
	 * Represents the status for a single receiver and it's custom selector if any.
	 */
	public static class ReaderInfo implements Serializable {
		private String selector;
		private String status;

		/**
		 * Instantiate reader info
		 *
		 * @param selector reader selector
		 * @param status the reader status
		 */
		public ReaderInfo(String selector, String status) {
			this.selector = selector;
			this.status = status;
		}

		public String getSelector() {
			return selector;
		}

		public String getStatus() {
			return status;
		}
	}
}
