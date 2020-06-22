package com.sirmaenterprise.sep.jms.provision;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.function.Consumer;

import com.sirmaenterprise.sep.jms.annotations.AddressFullMessagePolicyType;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;

/**
 * Defines the configurable properties for JMS destinations and their address settings. The settings include for example
 * the specific dead letter queue name and the retry delay or multiplier and so on.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/05/2018
 */
public class DestinationDefinition {
	public static final long DEFAULT_MAX_REDELIVERY_DELAY = 24 * 60 * 60 * 1000L; // max 1 day
	public static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024L; // 10 MB
	public static final long DEFAULT_PAGE_SIZE = 5 * 1024 * 1024L; // 5 MB
	public static final int DEFAULT_MESSAGE_COUNTER_HISTORY_DAY_LIMIT = 14;
	public static final int DEFAULT_PAGE_MAX_CACHE_SIZE = -1;
	public static final int DEFAULT_MAX_REDELIVERY_ATTEMPTS = 20;
	public static final double DEFAULT_REDELIVERY_MULTIPLIER = 2.0;
	public static final long DEFAULT_REDELIVERY_DELAY = 1000L;
	public static final long DEFAULT_EXPIRY_DELAY = -1L;

	private DestinationType type;
	private String address;
	private String deadLetterAddress;
	private String expiryAddress;
	private long expiryDelay = DEFAULT_EXPIRY_DELAY;
	private long redeliveryDelay = DEFAULT_REDELIVERY_DELAY;
	private double redeliveryMultiplier = DEFAULT_REDELIVERY_MULTIPLIER;
	private int maxRedeliveryAttempts = DEFAULT_MAX_REDELIVERY_ATTEMPTS;
	private long maxRedeliveryDelay = DEFAULT_MAX_REDELIVERY_DELAY;
	private long maxSize = DEFAULT_MAX_SIZE;
	private long pageSize = DEFAULT_PAGE_SIZE;
	private int pageMaxCacheSize = DEFAULT_PAGE_MAX_CACHE_SIZE;
	private AddressFullMessagePolicyType addressFullPolicy = AddressFullMessagePolicyType.PAGE;
	private int messageCounterHistoryDayLimit = DEFAULT_MESSAGE_COUNTER_HISTORY_DAY_LIMIT;
	private boolean lastValueQueue;

	/**
	 * Instantiate a {@link DestinationDefinition} instance filled with properties from the given {@link DestinationDef}
	 *
	 * @param def the source destination definition annotation
	 * @return the created and populated destination definition
	 */
	public static DestinationDefinition from(DestinationDef def) {
		DestinationDefinition definition = new DestinationDefinition();
		definition.setType(def.type());
		definition.setAddress(def.value());
		if (isNotBlank(def.deadLetterAddress())) {
			definition.setDeadLetterAddress(def.deadLetterAddress());
		}
		if (isNotBlank(def.expiryAddress())) {
			definition.setExpiryAddress(def.expiryAddress());
		}
		setValue(definition::setExpiryDelay, def.expiryDelay(), DEFAULT_EXPIRY_DELAY);
		setValue(definition::setRedeliveryDelay, def.redeliveryDelay(), DEFAULT_REDELIVERY_DELAY);
		setValue(definition::setRedeliveryMultiplier, def.redeliveryMultiplier(), DEFAULT_REDELIVERY_MULTIPLIER);
		setValue(definition::setMaxRedeliveryAttempts, def.maxRedeliveryAttempts(), DEFAULT_MAX_REDELIVERY_ATTEMPTS);
		setValue(definition::setMaxRedeliveryDelay, def.maxRedeliveryDelay(), DEFAULT_MAX_REDELIVERY_DELAY);
		setValue(definition::setMaxSize, def.maxSize(), DEFAULT_MAX_SIZE);
		setValue(definition::setPageSize, def.pageSize(), DEFAULT_PAGE_SIZE);
		setValue(definition::setPageMaxCacheSize, def.pageMaxCacheSize(), DEFAULT_PAGE_MAX_CACHE_SIZE);
		setValue(definition::setMessageCounterHistoryDayLimit, def.messageCounterHistoryDayLimit(),
				DEFAULT_MESSAGE_COUNTER_HISTORY_DAY_LIMIT);
		definition.setAddressFullPolicy(def.addressFullPolicy());
		definition.setLastValueQueue(def.lastValueQueue());

		return definition;
	}

	private static <N extends Number> void setValue(Consumer<N> numberConsumer, N value, N defaultValue) {
		if (value.doubleValue() >= 0.0) {
			numberConsumer.accept(value);
		} else {
			numberConsumer.accept(defaultValue);
		}
	}

	/**
	 * Copy all properties from the given instance to the current one. If any of the source properties is not pass a
	 * default value for that property will be set.
	 *
	 * @param definition the source definition to copy from
	 * @return the current instance for chaining
	 */
	public DestinationDefinition copyFrom(DestinationDefinition definition) {
		if (definition.getAddress() != null) {
			setAddress(definition.getAddress());
		}
		if (definition.getType() == null) {
			if (getAddress() != null) {
				setType(getAddress().toLowerCase().contains("topic") ? DestinationType.TOPIC : DestinationType.QUEUE);
			}
		} else {
			setType(definition.getType());
		}
		if (isNotBlank(definition.getDeadLetterAddress())) {
			setDeadLetterAddress(definition.getDeadLetterAddress());
		}
		if (isNotBlank(definition.getExpiryAddress())) {
			setExpiryAddress(definition.getExpiryAddress());
		}
		setValue(this::setExpiryDelay, definition.getExpiryDelay(), DEFAULT_EXPIRY_DELAY);
		setValue(this::setRedeliveryDelay, definition.getRedeliveryDelay(), DEFAULT_REDELIVERY_DELAY);
		setValue(this::setRedeliveryMultiplier, definition.getRedeliveryMultiplier(), DEFAULT_REDELIVERY_MULTIPLIER);
		setValue(this::setMaxRedeliveryAttempts, definition.getMaxRedeliveryAttempts(),
				DEFAULT_MAX_REDELIVERY_ATTEMPTS);
		setValue(this::setMaxRedeliveryDelay, definition.getMaxRedeliveryDelay(), DEFAULT_MAX_REDELIVERY_DELAY);
		setValue(this::setMaxSize, definition.getMaxSize(), DEFAULT_MAX_SIZE);
		setValue(this::setPageSize, definition.getPageSize(), DEFAULT_PAGE_SIZE);
		setValue(this::setPageMaxCacheSize, definition.getPageMaxCacheSize(), DEFAULT_PAGE_MAX_CACHE_SIZE);
		setAddressFullPolicy(definition.getAddressFullPolicy());
		setValue(this::setMessageCounterHistoryDayLimit, definition.getMessageCounterHistoryDayLimit(),
				DEFAULT_MESSAGE_COUNTER_HISTORY_DAY_LIMIT);
		setLastValueQueue(definition.isLastValueQueue());

		return this;
	}

	/**
	 * Destination name
	 *
	 * @return the destination name
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * The dead letter address
	 *
	 * @return DLQ address
	 */
	public String getDeadLetterAddress() {
		return deadLetterAddress;
	}

	/**
	 * Defines where to send a message that has expired.
	 *
	 * @return the expiration queue address
	 */
	public String getExpiryAddress() {
		return expiryAddress;
	}

	/**
	 * Specifies the destination type. Default is JMS Queue.
	 *
	 * @return the destination type
	 */
	public DestinationType getType() {
		return type;
	}

	/**
	 * Defines the expiration time that will be used for messages using the default expiration time
	 *
	 * @return the expiration delay
	 */
	public long getExpiryDelay() {
		return expiryDelay;
	}

	/**
	 * Defines how long to wait before attempting redelivery of a cancelled message
	 *
	 * @return the redelivery delay
	 */
	public long getRedeliveryDelay() {
		return redeliveryDelay;
	}

	/**
	 * Multiplier to apply to the redelivery-delay parameter
	 *
	 * @return the redelivery multiplier
	 */
	public double getRedeliveryMultiplier() {
		return redeliveryMultiplier;
	}

	/**
	 * Defines how many time a cancelled message can be redelivered before sending to the dead-letter-address
	 *
	 * @return the max redelivery attempts
	 */
	public int getMaxRedeliveryAttempts() {
		return maxRedeliveryAttempts;
	}

	/**
	 * Maximum value for the redelivery-delay (in ms).
	 *
	 * @return the max redelivery delay
	 */
	public long getMaxRedeliveryDelay() {
		return maxRedeliveryDelay;
	}

	/**
	 * The max bytes size.
	 *
	 * @return the maximum total size of the destination bodies
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * The paging size.
	 *
	 * @return the page size
	 */
	public long getPageSize() {
		return pageSize;
	}

	/**
	 * The number of page files to keep in memory to optimize IO during paging navigation.
	 *
	 * @return the number of pages to keep in memory
	 */
	public int getPageMaxCacheSize() {
		return pageMaxCacheSize;
	}

	/**
	 * Determines what happens when an address where max-size-bytes is specified becomes full. (PAGE, DROP or BLOCK)
	 *
	 * @return the full address message policy
	 */
	public AddressFullMessagePolicyType getAddressFullPolicy() {
		return addressFullPolicy;
	}

	/**
	 * Day limit for the message counter history.
	 *
	 * @return the message counter history days
	 */
	public int getMessageCounterHistoryDayLimit() {
		return messageCounterHistoryDayLimit;
	}

	/**
	 * Defines whether a queue only uses last values or not
	 *
	 * @return is the queue last value queue
	 */
	public boolean isLastValueQueue() {
		return lastValueQueue;
	}

	/**
	 * Sets the destination address. If the value is non blank and the DLQ address or the expiration address are not
	 * set they will be set with default values based on the set address by appending _DLQ or _EQ
	 *
	 * @param address the address to set
	 * @return the current instance for chaining
	 */
	public DestinationDefinition setAddress(String address) {
		this.address = address;
		if (isNotBlank(address)) {
			if (isBlank(getDeadLetterAddress())) {
				setDeadLetterAddress(address + "_DLQ");
			}
			if (isBlank(getExpiryAddress())) {
				setExpiryAddress(address + "_EQ");
			}
		}
		return this;
	}

	public DestinationDefinition setType(DestinationType type) {
		this.type = type;
		return this;
	}

	public DestinationDefinition setExpiryDelay(long expiryDelay) {
		this.expiryDelay = expiryDelay;
		return this;
	}

	public DestinationDefinition setRedeliveryDelay(long redeliveryDelay) {
		this.redeliveryDelay = redeliveryDelay;
		return this;
	}

	public DestinationDefinition setRedeliveryMultiplier(double redeliveryMultiplier) {
		this.redeliveryMultiplier = redeliveryMultiplier;
		return this;
	}

	public DestinationDefinition setMaxRedeliveryAttempts(int maxRedeliveryAttempts) {
		this.maxRedeliveryAttempts = maxRedeliveryAttempts;
		return this;
	}

	public DestinationDefinition setMaxRedeliveryDelay(long maxRedeliveryDelay) {
		this.maxRedeliveryDelay = maxRedeliveryDelay;
		return this;
	}

	public DestinationDefinition setMaxSize(long maxSize) {
		this.maxSize = maxSize;
		return this;
	}

	public DestinationDefinition setPageSize(long pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public DestinationDefinition setPageMaxCacheSize(int pageMaxCacheSize) {
		this.pageMaxCacheSize = pageMaxCacheSize;
		return this;
	}

	public DestinationDefinition setAddressFullPolicy(AddressFullMessagePolicyType addressFullPolicy) {
		this.addressFullPolicy = addressFullPolicy;
		return this;
	}

	public DestinationDefinition setMessageCounterHistoryDayLimit(int messageCounterHistoryDayLimit) {
		this.messageCounterHistoryDayLimit = messageCounterHistoryDayLimit;
		return this;
	}

	public DestinationDefinition setLastValueQueue(boolean lastValueQueue) {
		this.lastValueQueue = lastValueQueue;
		return this;
	}

	public DestinationDefinition setDeadLetterAddress(String deadLetterAddress) {
		this.deadLetterAddress = deadLetterAddress;
		return this;
	}

	public DestinationDefinition setExpiryAddress(String expiryAddress) {
		this.expiryAddress = expiryAddress;
		return this;
	}
}
