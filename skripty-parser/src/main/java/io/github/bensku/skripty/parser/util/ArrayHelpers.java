package io.github.bensku.skripty.parser.util;

public class ArrayHelpers {

	public static <T> boolean contains(T[] array, T value) {
		for (T e : array) {
			if (value.equals(e)) {
				return true;
			}
		}
		return false;
	}
}
