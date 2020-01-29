package io.github.bensku.skripty.parser;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;

/**
 * A radix tree used for expression lookups. Operates on UTF-8 string data.
 *
 */
public class RadixTree<T> {

	private static class Node<T> {
		
		private static final int INITIAL_SIZE = 4;
		
		/**
		 * Start index of the node.
		 */
		private final int nodeStart;
		
		/**
		 * Backing byte (UTF-8 character) array.
		 */
		private byte[] bytes;
		
		/**
		 * An index of the byte that contains branch of this node. If there
		 * are no branches, this is -1.
		 */
		private int branchIndex;
		
		/**
		 * The bit we branch on, in byte at {@link #branchIndex}.
		 */
		private int branchBit;
		
		/**
		 * Node to branch to if bit at {@link #branchBit} is 0.
		 */
		private Node<T> branch0;
		
		/**
		 * Node to branch to if branch bit is 1.
		 */
		private Node<T> branch1;
		
		/**
		 * The first data entry in this node.
		 */
		private DataEntry<T> firstEntry;
		
		/**
		 * The last data entry in this node.
		 */
		private DataEntry<T> lastEntry;
		
		/**
		 * Creates a new node.
		 * @param nodeStart Global start index for the node.
		 */
		Node(int nodeStart) {
			this.nodeStart = nodeStart;
			this.branchIndex = -1;
		}
		
		/**
		 * Reads a byte from this node.
		 * @param index Index.
		 * @return The byte, or 0 if index is too large.
		 */
		private byte readByte(int index) {
			if (index >= bytes.length) {
				return 0;
			} else {
				return bytes[index];
			}
		}
		
		/**
		 * Writes a byte to this node. Enlarges {@link #bytes} if needed.
		 * @param index Index.
		 * @param value New value for byte at index.
		 */
		private void writeByte(int index, byte value) {
			if (index >= bytes.length) {
				byte[] newBytes = new byte[bytes.length * 2];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				bytes = newBytes;
			}
			bytes[index] = value;
		}
		
		/**
		 * Writes given data at a node at end of given key.
		 * @param key Key for expression.
		 * @param start Start in data array.
		 * @param data Data to write.
		 */
		public void write(byte[] key, int start, T data) {
			int i;
			for (i = 0; start + i < key.length; i++) {
				byte oldValue = readByte(i);
				byte newValue = key[start + i];
				
				if (branchIndex == i) { // There is a branch that we need to follow
					// Select (or rare cases, create) a branch
					Node<T> newNode = selectBranch(newValue);
					if (newNode == null) {
						newNode = createBranch(i, newValue);
					}
					newNode.write(key, start + i, data);
					return; // Assume that write succeeded somewhere down the line
				}
				
				// If we're not changing anything, things are very simple
				if (oldValue == newValue || oldValue == 0) { // Overwrite NULLs, too
					writeByte(i, newValue);
					continue;
				}
				
				// But otherwise we probably have a branch here!
				createBranch(i, newValue).write(key, start + i, data);
				return; // Written down the line, hopefully
			}
			
			// Reached a node and index matching the data
			addData(key.length - 1, data);
		}
		
		public void read(ResultCollector<T> collector, byte[] key, int start) {
			DataEntry<T> currentEntry = firstEntry;
			for (int i = 0; start + i < key.length; i++) {
				byte value = key[start + i];
				if (i >= bytes.length) {
					break; // Ran out of things in this node
				} else if (branchIndex == i) { // Need to select a branch
					Node<T> branch = selectBranch(value);
					if (branch != null) { // If there is a suitable branch, go for it
						branch.read(collector, key, start + i);
					}
					break;
				} else if (bytes[i] != value) {
					break; // No branch and values don't match -> we're out
				}
				
				// Nothing interrupted us? Check if we found an expression
				if (currentEntry != null && currentEntry.index == nodeStart + i) {
					collector.add(currentEntry.data);
					currentEntry = currentEntry.after; // Next (maybe null, that's ok)
				}
			}
		}
		
		/**
		 * Byte converted to int has this many more leading zeroes when used
		 * with {@link Integer#numberOfLeadingZeros(int)}. {@link Byte} does
		 * not provide an alternative method, so we'll just subtract from
		 * the result.
		 */
		private static final int LEADING_ZEROS_INT_BYTE = 24;
		
		/**
		 * Largest bit index in a byte.
		 */
		private static final int BYTE_BIT_MAX = 7;
		
		/**
		 * Selects a branch based on given value for byte at it.
		 * @param value The byte at {@link #branchIndex} that will be used to
		 * decide which path we branch to.
		 * @return Correct branch, or null if there is a difference before
		 * the existing branch.
		 */
		public Node<T> selectBranch(byte value) {
			// Find the first differing bit
			int diffBit = Integer.numberOfLeadingZeros(bytes[branchIndex] ^ value) - LEADING_ZEROS_INT_BYTE;
			if (diffBit < branchBit) { // Difference BEFORE the branch bit!
				return null; // Caller might want to create a branch
			} else { // Just select a branch
				// Create a mask where branch bit is 1, everything else zero
				int mismatchMask = 1 << (BYTE_BIT_MAX - branchBit);
				// Check whether our or their bit there is zero
				boolean ourZero = (mismatchMask & value) == 0;
				return ourZero ? branch0 : branch1;
			}
		}
		
		/**
		 * Creates a new branch.
		 * @param index Byte where to make the branch.
		 * @param value New value for byte that makes the branch necessary.
		 * @return The new branch.
		 */
		public Node<T> createBranch(int index, byte value) {
			int globalIndex = nodeStart + index;
			int bitIndex = Integer.numberOfLeadingZeros(bytes[index] ^ value) - LEADING_ZEROS_INT_BYTE;
			
			// Create a mask where this is 1, everything else zero
			int mismatchMask = 1 << (BYTE_BIT_MAX - bitIndex);
			// Check whether our or their bit there is zero
			boolean ourZero = (mismatchMask & value) == 0;
			
			// Node for existing content
			Node<T> oldNode = new Node<>(globalIndex);
			oldNode.bytes = new byte[bytes.length - index];
			System.arraycopy(bytes, index, oldNode.bytes, 0, oldNode.bytes.length);
			if (branchIndex != -1) { // There is another branch after this one
				oldNode.branchIndex = branchIndex - index;
				oldNode.branchBit = branchBit;
			}
			oldNode.branch0 = branch0;
			oldNode.branch1 = branch1;
			
			// Copy expressions to that node if needed
			DataEntry<T> firstToCopy = lastEntry;
			if (firstToCopy != null) {
				while (firstToCopy.index >= globalIndex && firstToCopy.before != null) {
					firstToCopy = firstToCopy.before;
				}
				
				// Check if we went too far and go back forwards
				if (firstToCopy.index < globalIndex) {
					firstToCopy = firstToCopy.after;
				}
				
				oldNode.firstEntry = firstToCopy;
				oldNode.lastEntry = lastEntry;
			}
			
			// New node for new content
			Node<T> newNode = new Node<>(globalIndex);
			newNode.bytes = new byte[INITIAL_SIZE]; // TODO we know total length, optimize this
			// Not writing first byte here; caller is responsible for that
			
			// Update this node
			branchIndex = index; // There is a branch, now
			branchBit = bitIndex;
			if (ourZero) { // Old to branch1, new to branch0
				branch1 = oldNode;
				branch0 = newNode;
			} else { // Old to branch0, new to branch1
				branch0 = oldNode;
				branch1 = newNode;
			}
			bytes[index] = value;
			
			if (firstToCopy != null) {
				if (firstEntry == lastEntry) {
					firstEntry = null;
				}
				lastEntry = firstToCopy.before;
				firstToCopy.before = null; // It is in new node now, won't need that
			}
			
			return newNode;
		}
		
		/**
		 * Adds a data value to this node.
		 * @param globalIndex Global (byte) index for the expression.
		 * @param data Data value.
		 */
		public void addData(int globalIndex, T data) {
			DataEntry<T> entry = new DataEntry<>(globalIndex, data);
			if (firstEntry == null) { // First and last expression here
				firstEntry = entry;
				lastEntry = entry;
			} else { // Link to some other expression
				DataEntry<T> before = firstEntry;
				// Go forward to find expression before this one
				while (before.index < globalIndex && before.after != null) {
					before = before.after;
				}
				// Went too far, take one step back
				if (before.index > globalIndex) {
					before = before.before;
				}
				if (before.after == null) {
					lastEntry = entry; // This is now last expression in node
				}
				
				// Place ourself between before and after
				DataEntry<T> after = before.after;
				before.after = entry;
				entry.before = before;
				entry.after = after;
			}
		}
	}
	
	/**
	 * Represents a data entry in the tree. Data entries that share a node are
	 * double linked together in order.
	 */
	private static class DataEntry<T> {
		
		/**
		 * Index of byte where this appears.
		 */
		private final int index;
		
		/**
		 * Data this entry is associated with.
		 */
		private final T data;
		
		/**
		 * The entry before this one. May be null.
		 */
		private DataEntry<T> before;
		
		/**
		 * The entry after this one. May be null.
		 */
		private DataEntry<T> after;
		
		DataEntry(int index, T data) {
			this.index = index;
			this.data = data;
		}
	}
	
	/**
	 * Type of elements in this tree.
	 */
	private final Class<T> type;
	
	/**
	 * Root node of this tree.
	 */
	private final Node<T> root;
	
	public RadixTree(Class<T> type) {
		this.type = type;
		this.root = new Node<>(0);
		root.bytes = new byte[Node.INITIAL_SIZE];
	}
	
	/**
	 * Puts data to this tree.
	 * @param key Key for the data.
	 * @param data The data.
	 */
	public void put(byte[] key, T data) {
		root.write(key, 0, data);
	}
	
	/**
	 * Puts data to this tree.
	 * @param key Key for the data.
	 * @param data The data.
	 */
	public void put(String key, T data) {
		put(key.getBytes(StandardCharsets.UTF_8), data);
	}
	
	/**
	 * How many results can be returned. TODO don't use a fixed-size array
	 */
	private static final int MAX_RESULTS = 128;
	
	/**
	 * Collects values found from the tree.
	 *
	 */
	@SuppressWarnings("unchecked") // We're not exposing our unsafe stuff in public API
	private static class ResultCollector<T> {
		
		/**
		 * Type of components in array.
		 */
		private final Class<T> type;
		
		/**
		 * Backing array.
		 */
		private final T[] array;
		
		/**
		 * Amount of results in the array.
		 */
		private int count;
		
		ResultCollector(Class<T> type) {
			this.type = type;
			this.array = (T[]) Array.newInstance(type, MAX_RESULTS);
		}
		
		/**
		 * Adds a result to this collector.
		 * @param result Result value.
		 */
		public void add(T result) {
			array[count++] = result;
		}
		
		/**
		 * Gets a copy of results in an array and resets this collector.
		 * Note that references to results are kept until they're written over
		 * at some later point. Since this radix tree doesn't support deletion
		 * of values, this does not cause memory leaks.
		 * @return The result values in an array.
		 */
		public T[] copyAndReset() {
			T[] results = (T[]) Array.newInstance(type, count);
			System.arraycopy(array, 0, results, 0, count);
			count = 0;
			return results;
		}
	}
	
	/**
	 * The result collector. One per thread is used to avoid race conditions
	 * in reading.
	 */
	private final ThreadLocal<ResultCollector<T>> resultCollector = new ThreadLocal<>();
	
	/**
	 * Gets all data that is the given key or prefix of it.
	 * @param key Key for the data.
	 * @return List of data that is found.
	 */
	public T[] get(byte[] key, int start) {
		ResultCollector<T> collector = resultCollector.get();
		if (collector == null) {
			collector = new ResultCollector<>(type);
			resultCollector.set(collector);
		}
		root.read(collector, key, start);
		return collector.copyAndReset();
	}
	
	/**
	 * Gets all data that is the given key or prefix of it.
	 * @param key Key for the data.
	 * @return List of data that is found.
	 */
	public T[] get(String key) {
		return get(key.getBytes(StandardCharsets.UTF_8), 0);
	}
}
