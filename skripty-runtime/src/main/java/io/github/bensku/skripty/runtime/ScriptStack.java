package io.github.bensku.skripty.runtime;

/**
 * Stack used for interpreting scripts.
 *
 */
public class ScriptStack {

	/**
	 * Stack slots.
	 */
	private final Object[] slots;
	
	/**
	 * Current size of the stack.
	 */
	private int size;
	
	public ScriptStack(int stackSize) {
		this.slots = new Object[stackSize];
	}
	
	/**
	 * Gets current size of the stack.
	 * @return Current stack size.
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Pushes a value to this stack.
	 * @param value Value to push.
	 */
	public void push(Object value) {
		slots[size++] = value;
	}
	
	/**
	 * Pops the top value from this stack.
	 * @return Former top value of the stack.
	 */
	public Object pop() {
		return slots[--size];
	}
	
	/**
	 * Pops many values from top of this stack.
	 * @param count How many values to pop.
	 * @return The popped values.
	 */
	public Object[] pop(int count) {
		Object[] slice = peek(count);
		size -= count;
		return slice;
	}
	
	/**
	 * Pops values to given array.
	 * @param array Array to put popped values to.
	 * @param start Start index in given array.
	 * @param count How many values to pop.
	 */
	public void popInto(Object[] array, int start, int count) {
		System.arraycopy(slots, this.size - count, array, start, count);
		size -= count;
	}
	
	/**
	 * Peeks at the top value of this stack without removing it.
	 * @return Top value of the stack.
	 */
	public Object peek() {
		return slots[size - 1];
	}
	
	/**
	 * Returns a slice from top of stack.
	 * @param length Size of the slice.
	 * @return Slice of the stack.
	 */
	public Object[] peek(int length) {
		Object[] slice = new Object[length];
		System.arraycopy(slots, this.size - length, slice, 0, length);
		return slice;
	}

}
