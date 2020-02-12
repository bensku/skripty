package io.github.bensku.skripty.parser.util;

import java.util.Objects;

public class ArrayHelpers {

	public static <T> boolean contains(T[] array, T value) {
		for (T e : array) {
			if (Objects.deepEquals(e, value)) {
				return true;
			}
		}
		return false;
	}

}
