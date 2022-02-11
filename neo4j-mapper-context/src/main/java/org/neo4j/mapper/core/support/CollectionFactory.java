package org.neo4j.mapper.core.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.Nullable;

/**
 * @author Gerrit Meier
 */
public class CollectionFactory {

	public static <E> Collection<E> createCollection(Class<?> collectionType, @Nullable Class<?> elementType, int capacity) {
		Assert.notNull(collectionType, "Collection type must not be null");
		if (collectionType.isInterface()) {
			if (Set.class != collectionType && Collection.class != collectionType) {
				if (List.class == collectionType) {
					return new ArrayList(capacity);
				} else if (SortedSet.class != collectionType && NavigableSet.class != collectionType) {
					throw new IllegalArgumentException("Unsupported Collection interface: " + collectionType.getName());
				} else {
					return new TreeSet();
				}
			} else {
				return new LinkedHashSet(capacity);
			}
		} else if (EnumSet.class.isAssignableFrom(collectionType)) {
			Assert.notNull(elementType, "Cannot create EnumSet for unknown element type");
			return EnumSet.noneOf(asEnumType(elementType));
		} else if (!Collection.class.isAssignableFrom(collectionType)) {
			throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
		} else {
			try {
				return (Collection) accessibleConstructor(collectionType, new Class[0]).newInstance();
			} catch (Throwable var4) {
				throw new IllegalArgumentException("Could not instantiate Collection type: " + collectionType.getName(), var4);
			}
		}
	}

	private static Class<? extends Enum> asEnumType(Class<?> enumType) {
		Assert.notNull(enumType, "Enum type must not be null");
		if (!Enum.class.isAssignableFrom(enumType)) {
			throw new IllegalArgumentException("Supplied type is not an enum: " + enumType.getName());
		} else {
			return enumType.asSubclass(Enum.class);
		}
	}

	public static <T> Constructor<T> accessibleConstructor(Class<T> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
		Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
		makeAccessible(ctor);
		return ctor;
	}

	public static void makeAccessible(Constructor<?> ctor) {
		if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}

	}
}
