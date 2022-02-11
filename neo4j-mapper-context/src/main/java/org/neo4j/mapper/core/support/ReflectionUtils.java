package org.neo4j.mapper.core.support;

/**
 * @author Gerrit Meier
 */
public class ReflectionUtils {

	public static Object getPrimitiveDefault(Class<?> type) {

		if (type == Byte.TYPE || type == Byte.class) {
			return (byte) 0;
		}

		if (type == Short.TYPE || type == Short.class) {
			return (short) 0;
		}

		if (type == Integer.TYPE || type == Integer.class) {
			return 0;
		}

		if (type == Long.TYPE || type == Long.class) {
			return 0L;
		}

		if (type == Float.TYPE || type == Float.class) {
			return 0F;
		}

		if (type == Double.TYPE || type == Double.class) {
			return 0D;
		}

		if (type == Character.TYPE || type == Character.class) {
			return '\u0000';
		}

		if (type == Boolean.TYPE) {
			return Boolean.FALSE;
		}

		throw new IllegalArgumentException(String.format("Primitive type %s not supported!", type));
	}
}
