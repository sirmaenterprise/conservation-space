package com.sirma.itt.seip.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/10/2018
 */
public class TransliterationUtilTest {

	@Test
	public void transliterate() throws Exception {
		String input = "а б в г д е ж з и й к л м н о п р с т у ф х ц ч ш щ ъ ь ю я_А Б В Г Д Е Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ъ Ь Ю Я";
		String expected = "a b v g d e zh z i ee k l m n o p r s t u f h ts ch sh sht a a yu ya_A B V G D E Zh Z I Ee K L M N O P R S T U F H Ts Ch Sh Sht A A Yu Ya";
		String output = TransliterationUtil.transliterate(input);
		assertEquals(expected, output);
	}

	@Test
	public void transliterateNoData() throws Exception {
		String input = "";
		String expected = "";
		String output = TransliterationUtil.transliterate(input);
		assertEquals(expected, output);
	}
}
