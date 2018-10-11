package com.sirma.itt.seip.rest;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Class that represents HTTP Range header. Can be used as parameter type to inject the header from the request. <br>
 * Note that the range is 0 based.<br>
 * If the range has open end (till the end of the range) the {@link #getTo()} will return -1 and {@link #isToTheEnd()}
 * will return <code>true</code>.
 *
 * @author BBonev
 */
public class Range implements Serializable {
	private static final long serialVersionUID = -8384042792255156226L;

	/** Default unit: bytes */
	public static final String BYTES = "bytes";

	/** The Range HTTP header name. */
	public static final String HEADER = "Range";

	/** The default range value for all content. */
	public static final String DEFAULT_RANGE = "bytes=0-";

	/** Accept-Ranges HTTP header constant. */
	public static final String HTTP_HEADER_ACCEPT_RANGES = "Accept-Ranges";

	/** Content-Range HTTP header constant. */
	public static final String HTTP_HEADER_CONTENT_RANGE = "Content-Range";

	/**
	 * Range that represents all available content and equals to the definition of {@code bytes=0-}
	 */
	public static final Range ALL = Range.fromString(DEFAULT_RANGE);

	private final long from;
	private final long to;
	private final String unit;

	/**
	 * Instantiates a new range.
	 *
	 * @param unit
	 *            the unit of the range, default value is {@code bytes}
	 * @param from
	 *            the beginning of the range (zero based)
	 * @param to
	 *            the end of the range
	 */
	public Range(String unit, long from, long to) {
		this.unit = StringUtils.isBlank(unit) ? BYTES : unit;
		this.from = from;
		this.to = to;
	}

	/**
	 * Parses the given string into {@link Range}. The possible formats are:
	 * <ul>
	 * <li>{@code unit=from-to}
	 * <li>{@code unit=from-}
	 * <li>{@code from-to} (with default units bytes)
	 * <li>{@code from-} (with default units bytes)
	 * </ul>
	 *
	 * @param rangeToParse
	 *            the range to parse
	 * @return the range object or {@link #ALL} if the given string format is not supported
	 */
	public static Range fromString(String rangeToParse) {
		if (StringUtils.isBlank(rangeToParse)) {
			return ALL;
		}
		String[] strings = rangeToParse.split("=");
		String unit = BYTES;
		String range = strings[0];
		if (strings.length >= 2) {
			unit = strings[0];
			range = strings[1];
		}
		String[] split = range.split("-");
		long start = Long.parseLong(split[0]);
		long end = -1L;
		if (split.length > 1 && StringUtils.isNotBlank(split[1])) {
			end = Long.parseLong(split[1]);
		}
		return new Range(unit, start, end);
	}

	/**
	 * Gets the start of the range
	 *
	 * @return the from
	 */
	public long getFrom() {
		return from;
	}

	/**
	 * Gets the end of the range
	 *
	 * @return the end or -1 to indicate that it's open range
	 */
	public long getTo() {
		return to;
	}

	/**
	 * Gets the unit for this range
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Checks if the range is in bytes.
	 *
	 * @return true, if is in bytes
	 */
	public boolean isInBytes() {
		return BYTES.equals(unit);
	}

	/**
	 * Checks if the current range is with open end and the server should return all remaining bytes.
	 *
	 * @return true, if is to the end
	 */
	public boolean isToTheEnd() {
		return to == -1L;
	}

	/**
	 * Checks if all content is requested by this range object. The method checks if the current range object is the
	 * same as {@link #ALL}.
	 *
	 * @return true, if is all requested
	 */
	public boolean isAllRequested() {
		return ALL.equals(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (from ^ from >>> 32);
		result = prime * result + (int) (to ^ to >>> 32);
		result = prime * result + (unit == null ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Range)) {
			return false;
		}
		Range other = (Range) obj;
		return nullSafeEquals(unit, other.unit) && from == other.from && to == other.to;
	}

	/**
	 * Gets the requested range length or -1 if the requested range is open and should return all
	 * <p>
	 * Example: if the range is defined 0-999 this method will return 1000
	 *
	 * @return the range length
	 */
	public long getRangeLength() {
		if (to > from) {
			return to - from + 1;
		}
		return -1L;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append("Range [")
					.append(unit)
					.append('=')
					.append(from)
					.append('-')
					.append(to == -1L ? "" : String.valueOf(to))
					.append(']')
					.toString();
	}

	/**
	 * Returns the current range as response string
	 *
	 * @param total
	 *            the total content length
	 * @return the range as string
	 */
	public String asResponse(long total) {
		return new StringBuilder()
				.append(getUnit())
					.append(" ")
					.append(getFrom())
					.append("-")
					.append(isToTheEnd() ? total - 1L : getTo())
					.append("/")
					.append(total)
					.toString();
	}

}
