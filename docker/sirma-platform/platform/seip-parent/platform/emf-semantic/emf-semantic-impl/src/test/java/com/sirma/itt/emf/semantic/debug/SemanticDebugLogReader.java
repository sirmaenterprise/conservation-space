package com.sirma.itt.emf.semantic.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Simple reader of the semantic debug dumped files
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/03/2019
 */
public class SemanticDebugLogReader {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Specify path to the log file to read as argument");
			System.exit(-1);
		}
		for (String arg : args) {
			if (arg == null || arg.trim().isEmpty()) {
				continue;
			}
			File file = new File(arg.trim());
			if (file.isDirectory()) {
				File[] filesList = file.listFiles();
				if (filesList != null) {
					for (File child : filesList) {
						dumpFile(child);
					}
				}
			} else {
				dumpFile(file);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void dumpFile(File file) {
		System.out.println("Reading: " + file.getAbsolutePath());
		try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
			List<Object[]> list = (List<Object[]>) inputStream.readObject();
			int rowNumber = 0;
			for (Object[] row : list) {
				Object timestamp = null;
				if (row.length>=4) {
					timestamp = row[3];
				}
				System.out.println(rowNumber++ + ": " + row[0] + " : " + readTimestamp(timestamp) + " : " + row[2] + " : " + readData(row[1]));
			}
		} catch (Exception e) {
			System.err.println("Invalid file: " + file.getAbsolutePath() + " : " + e.getMessage());
		}
	}

	private static String readTimestamp(Object timestamp) {
		if (timestamp == null) {
			return "(no timestamp)";
		}
		return ISO8601DateFormat.format(new Date((Long) timestamp));
	}

	@SuppressWarnings("unchecked")
	private static Object readData(Object cell) {
		if (cell instanceof Map) {
			return ((Map<String, Object>) cell).entrySet()
					.stream()
					.map(entry -> entry.getKey() + "=" + readData(entry.getValue()))
					.collect(Collectors.joining(", "));
		} else if (cell instanceof Collection) {
			return ((Collection) cell).stream()
					.map(SemanticDebugLogReader::readData)
					.collect(Collectors.joining("," + System.lineSeparator() + "\t", "[", "]"));
		} else if (cell == null) {
			return "null";
		} else if (cell.getClass().isArray()) {
			return Arrays.toString((Object[]) cell);
		}
		return cell.toString();
	}
}
