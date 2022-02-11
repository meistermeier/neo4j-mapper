package org.neo4j.mapper.core.support;

import org.jetbrains.annotations.Nullable;

public final class StringUtils {

	private StringUtils(){}

	public static boolean hasText(@Nullable String str) {
		return str != null && !str.isEmpty() && containsText(str);
	}

	private static boolean containsText(CharSequence str) {
		int strLen = str.length();

		for(int i = 0; i < strLen; ++i) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}

		return false;
	}

	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (!hasLength(str)) {
			return str;
		} else {
			char baseChar = str.charAt(0);
			char updatedChar;
			if (capitalize) {
				updatedChar = Character.toUpperCase(baseChar);
			} else {
				updatedChar = Character.toLowerCase(baseChar);
			}

			if (baseChar == updatedChar) {
				return str;
			} else {
				char[] chars = str.toCharArray();
				chars[0] = updatedChar;
				return new String(chars);
			}
		}
	}

	public static boolean hasLength(@Nullable String str) {
		return str != null && !str.isEmpty();
	}
}
