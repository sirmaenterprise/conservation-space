package com.sirma.itt.seip.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Ascii;

/**
 * Utility class for transliterating non ASCII charters to ASCII version
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/10/2018
 */
public class TransliterationUtil {

	private static final Map<Character, String> CYRILLIC_TO_ASCII = new HashMap<>();

	static {
		CYRILLIC_TO_ASCII.put('а', "a");
		CYRILLIC_TO_ASCII.put('б', "b");
		CYRILLIC_TO_ASCII.put('в', "v");
		CYRILLIC_TO_ASCII.put('г', "g");
		CYRILLIC_TO_ASCII.put('д', "d");
		CYRILLIC_TO_ASCII.put('е', "e");
		CYRILLIC_TO_ASCII.put('ж', "zh");
		CYRILLIC_TO_ASCII.put('з', "z");
		CYRILLIC_TO_ASCII.put('и', "i");
		CYRILLIC_TO_ASCII.put('й', "ee");
		CYRILLIC_TO_ASCII.put('к', "k");
		CYRILLIC_TO_ASCII.put('л', "l");
		CYRILLIC_TO_ASCII.put('м', "m");
		CYRILLIC_TO_ASCII.put('н', "n");
		CYRILLIC_TO_ASCII.put('о', "o");
		CYRILLIC_TO_ASCII.put('п', "p");
		CYRILLIC_TO_ASCII.put('р', "r");
		CYRILLIC_TO_ASCII.put('с', "s");
		CYRILLIC_TO_ASCII.put('т', "t");
		CYRILLIC_TO_ASCII.put('у', "u");
		CYRILLIC_TO_ASCII.put('ф', "f");
		CYRILLIC_TO_ASCII.put('х', "h");
		CYRILLIC_TO_ASCII.put('ц', "ts");
		CYRILLIC_TO_ASCII.put('ч', "ch");
		CYRILLIC_TO_ASCII.put('ш', "sh");
		CYRILLIC_TO_ASCII.put('щ', "sht");
		CYRILLIC_TO_ASCII.put('ъ', "a");
		CYRILLIC_TO_ASCII.put('ь', "a");
		CYRILLIC_TO_ASCII.put('ю', "yu");
		CYRILLIC_TO_ASCII.put('я', "ya");

		CYRILLIC_TO_ASCII.put('А', "A");
		CYRILLIC_TO_ASCII.put('Б', "B");
		CYRILLIC_TO_ASCII.put('В', "V");
		CYRILLIC_TO_ASCII.put('Г', "G");
		CYRILLIC_TO_ASCII.put('Д', "D");
		CYRILLIC_TO_ASCII.put('Е', "E");
		CYRILLIC_TO_ASCII.put('Ж', "Zh");
		CYRILLIC_TO_ASCII.put('З', "Z");
		CYRILLIC_TO_ASCII.put('И', "I");
		CYRILLIC_TO_ASCII.put('Й', "Ee");
		CYRILLIC_TO_ASCII.put('К', "K");
		CYRILLIC_TO_ASCII.put('Л', "L");
		CYRILLIC_TO_ASCII.put('М', "M");
		CYRILLIC_TO_ASCII.put('Н', "N");
		CYRILLIC_TO_ASCII.put('О', "O");
		CYRILLIC_TO_ASCII.put('П', "P");
		CYRILLIC_TO_ASCII.put('Р', "R");
		CYRILLIC_TO_ASCII.put('С', "S");
		CYRILLIC_TO_ASCII.put('Т', "T");
		CYRILLIC_TO_ASCII.put('У', "U");
		CYRILLIC_TO_ASCII.put('Ф', "F");
		CYRILLIC_TO_ASCII.put('Х', "H");
		CYRILLIC_TO_ASCII.put('Ц', "Ts");
		CYRILLIC_TO_ASCII.put('Ч', "Ch");
		CYRILLIC_TO_ASCII.put('Ш', "Sh");
		CYRILLIC_TO_ASCII.put('Щ', "Sht");
		CYRILLIC_TO_ASCII.put('Ъ', "A");
		CYRILLIC_TO_ASCII.put('Ь', "A");
		CYRILLIC_TO_ASCII.put('Ю', "Yu");
		CYRILLIC_TO_ASCII.put('Я', "Ya");
	}

	private TransliterationUtil() {

	}

	/**
	 * Transliterate a single character. If the char does is already ASCII or is not supported then the original input
	 * will be returned. The method returns a string as some characters after transliteration has more symbols.
	 *
	 * @param aChar a char to be transliterated
	 * @return the original character or transliterated version of it
	 */
	public static String transliterateChar(char aChar) {
		String charAsString = String.valueOf(aChar);
		if (aChar > Ascii.MAX) {
			return CYRILLIC_TO_ASCII.getOrDefault(aChar, charAsString);
		}
		return charAsString;
	}

	/**
	 * Transliterate entire String. Note that the result string may be longer than the input string as some characters
	 * are replaced with more than one symbol.
	 *
	 * @param string the input value to transliterate
	 * @return the transliterated text or the original text if transliteration is not needed
	 * @see #transliterateChar(char)
	 */
	public static String transliterate(String string) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		char[] chars = string.toCharArray();
		StringBuilder builder = new StringBuilder((int) (chars.length * 1.2));
		for (char c : chars) {
			builder.append(transliterateChar(c));
		}
		return builder.toString();
	}
}
