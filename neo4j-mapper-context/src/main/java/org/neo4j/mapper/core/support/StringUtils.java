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
}
