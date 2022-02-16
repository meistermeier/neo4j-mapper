package org.neo4j.mapper.core.mapping;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A very loose coupling between a dot path and its (possible) owning type.
 * This is due to the fact that the original PropertyPath does throw an exception on creation when a property
 * is not found on the entity.
 * Since we are supporting also querying for base classes with properties coming from the inheriting classes,
 * this test on creation is too strict.
 */
public final class RelaxedPropertyPath {
	private final String dotPath;
	private final Class<?> type;

	public static RelaxedPropertyPath withRootType(Class<?> type) {
		return new RelaxedPropertyPath("", type);
	}

	public String toDotPath() {
		return dotPath;
	}

	public String toDotPath(@Nullable String lastSegment) {

		if (lastSegment == null) {
			return this.toDotPath();
		}

		int idx = dotPath.lastIndexOf('.');
		if (idx < 0) {
			return lastSegment;
		}
		return dotPath.substring(0, idx + 1) + lastSegment;
	}

	public Class<?> getType() {
		return type;
	}

	private RelaxedPropertyPath(String dotPath, Class<?> type) {
		this.dotPath = dotPath;
		this.type = type;
	}

	public RelaxedPropertyPath append(String pathPart) {
		return new RelaxedPropertyPath(appendToDotPath(pathPart), getType());
	}

	public RelaxedPropertyPath prepend(String pathPart) {
		return new RelaxedPropertyPath(prependDotPathWith(pathPart), getType());
	}

	private String appendToDotPath(String pathPart) {
		return dotPath.isEmpty() ? pathPart : dotPath + "." + pathPart;
	}

	private String prependDotPathWith(String pathPart) {
		return dotPath.isEmpty() ? pathPart : pathPart + "." + dotPath;
	}

	public String getSegment() {

		int idx = dotPath.indexOf(".");
		if (idx < 0) {
			idx = dotPath.length();
		}
		return dotPath.substring(0, idx);
	}

	public RelaxedPropertyPath getLeafProperty() {

		int idx = dotPath.lastIndexOf('.');
		if (idx < 0) {
			return this;
		}

		return new RelaxedPropertyPath(dotPath.substring(idx + 1), this.type);
	}

	public String dotPath() {
		return dotPath;
	}

	public Class<?> type() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (RelaxedPropertyPath) obj;
		return Objects.equals(this.dotPath, that.dotPath) &&
				Objects.equals(this.type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dotPath, type);
	}

	@Override
	public String toString() {
		return "RelaxedPropertyPath[" +
				"dotPath=" + dotPath + ", " +
				"type=" + type + ']';
	}

}
