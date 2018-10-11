package com.sirma.itt.seip.eai.model.request.query;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Util class used to debug/represent an QueryEntry as formatted string with tree like structure
 * 
 * @author bbanchev
 */
public class QueryToString {

	private QueryToString() {
		// utl class
	}

	/**
	 * {@link RawQuery} as string
	 *
	 * @param query
	 *            the {@link RawQuery} query to represent
	 * @return the formatted model of {@link RawQuery} parameter
	 */
	public static String toString(RawQuery query) {
		StringBuilder builder = new StringBuilder();
		build(query, builder, 0);
		return builder.toString();
	}

	/**
	 * Builds any type of {@link QueryEntry} in the provided string builder.
	 *
	 * @param entry
	 *            the entry to represent
	 * @param builder
	 *            the buffer to use
	 * @param level
	 *            the outdent level - starts at 0
	 */
	public static void build(QueryEntry entry, StringBuilder builder, int level) {
		if (entry instanceof RawQuery) {
			build((RawQuery) entry, builder, level);
		} else if (entry instanceof RawQueryEntry) {
			builder.append("\n").append(tabulation(level)).append("{").append(entry.toString()).append("}");
		}
	}

	private static void build(RawQuery query, StringBuilder builder, int level) {
		int currentLevel = level + 1;
		List<QueryEntry> entries = query.getEntries();
		ListIterator<QueryEntry> iterator = entries.listIterator();
		while (iterator.hasNext()) {
			QueryEntry entry = iterator.next();
			boolean isNested = entry instanceof RawQuery;
			String str = tabulation(level);

			if (isNested) {
				builder.append("\n");
				builder.append(tabulation(currentLevel));
				builder.append("(");
			}
			int nextIndex = iterator.nextIndex();
			if (nextIndex >= entries.size() || entries.get(nextIndex) instanceof RawQuery) {
				return;
			}
			build(entry, builder, currentLevel);
			builder.append(str);
			if (isNested) {
				builder.append("\n");
				builder.append(tabulation(currentLevel));
				builder.append(")");
			}
		}
	}

	private static String tabulation(int level) {
		char[] outdent = new char[level];
		Arrays.fill(outdent, ' ');
		return new String(outdent);
	}
}
