package io.github.bensku.skripty.parser;

/**
 * A radix tree used for expression lookups. Operates on UTF-8 string data.
 *
 */
public class ExpressionTree {

	private static class Node {
		
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
		 * At this bit, branch to {@link #branch0} or {@link #branch1},
		 * depending on value of the bit in the matched string.
		 */
		private int branchIndex;
		
		/**
		 * Node where to branch to if bit at {@link #branchIndex} is 0.
		 */
		private Node branch0;
		
		private Node branch1;
		
		/**
		 * The first expression in this node.
		 */
		private ExpressionEntry firstExpr;
		
		/**
		 * The last expression in this node.
		 */
		private ExpressionEntry lastExpr;
		
		public Node(int nodeStart) {
			this.nodeStart = nodeStart;
			this.branchIndex = -1;
		}
		
		public Node write(int globalIndex, int bytesLeft, byte value) {
			int index = globalIndex - nodeStart;
			
			if (bytes == null) {
				bytes = new byte[INITIAL_SIZE];
				bytes[index] = value;
				return this;
			} else if (index >= bytes.length) { // Need to enlarge array
				byte[] newArray = new byte[bytes.length * 2];
				System.arraycopy(bytes, 0, newArray, 0, bytes.length);
				bytes = newArray;
				bytes[index] = value;
				return this;
			} else if (bytes[index] == 0) { // NULL, write over it
				bytes[index] = value;
				return this;
			} else if (bytes[index] == value) { // Same value, ignore
				return this; // Do nothing
			} else if (branchIndex * 8 == index) { // Must select a branch at this byte
				Node newNode = selectBranch(index, value);
				byte fused = (byte) (bytes[index] | newNode.bytes[0]);
				if (fused == value) {
					return newNode; // No more branching
				}
				// TODO
			} else { // Need to branch at this point
				return makeBranch(index, value, bytesLeft);
			}
		}
		
		private Node selectBranch(int index, byte value) {
			throw new UnsupportedOperationException();
		}
		
		private Node makeBranch(int index, byte value, int bytesLeft) {
			int globalIndex = nodeStart + index;
			
			// Find the first bit which is different
			int bitIndex = Integer.numberOfLeadingZeros(bytes[index] ^ value) - 24;
			// Create a mask where this is 1, everything else zero
			int mismatchMask = 1 << (7 - bitIndex);
			// Check whether our or their bit there is zero
			boolean ourZero = (mismatchMask & value) == 0;
			
			// Create masks for before and after the differing byte
			int startMask = 0xff << (7 - bitIndex) & 0xff;
			int endMask = 0xff >>> bitIndex;
			
			// Node for existing content
			Node oldNode = new Node(globalIndex);
			oldNode.bytes = new byte[bytes.length - index - 1];
			oldNode.bytes[0] = (byte) (bytes[index] & endMask);
			if (oldNode.bytes.length > 1) {
				System.arraycopy(bytes, index + 1, oldNode.bytes, 1, oldNode.bytes.length - 1);
			}
			if (branchIndex != -1) { // There is another branch after this one
				oldNode.branchIndex = branchIndex - index * 8;
			}
			oldNode.branch0 = branch0;
			oldNode.branch1 = branch1;
			
			// Copy expressions to that node if needed
			ExpressionEntry firstToCopy = lastExpr;
			while (firstToCopy.index >= globalIndex && firstToCopy.before != null) {
				firstToCopy = firstToCopy.before;
			}
			oldNode.firstExpr = firstToCopy;
			oldNode.lastExpr = lastExpr;
			
			// New node for new content
			Node newNode = new Node(globalIndex);
			newNode.bytes = new byte[bytesLeft];
			newNode.bytes[0] = (byte) (value & endMask);
			
			// Update this node
			branchIndex = index * 8 + bitIndex;
			if (ourZero) { // Old to branch1, new to branch0
				branch1 = oldNode;
				branch0 = newNode;
			} else { // Old to branch0, new to branch1
				branch0 = oldNode;
				branch1 = newNode;
			}
			lastExpr = firstToCopy.before;
			firstToCopy.before = null; // It is in new node now, won't need that
			
			// Set this byte to bits that are same + zeroes to end
			bytes[index] &= startMask;
			
			return newNode;
		}
		
		public void addExpr(int globalIndex, ExpressionInfo info) {
			ExpressionEntry entry = new ExpressionEntry(globalIndex, info);
			if (firstExpr == null) { // First and last expression here
				firstExpr = entry;
				lastExpr = entry;
			} else { // Link to some other expression
				ExpressionEntry before = firstExpr;
				// Go forward to find expression before this one
				while (before.index < globalIndex && before.after != null) {
					before = before.after;
				}
				// Went too far, take one step back
				if (before.index > globalIndex) {
					before = before.before;
				}
				if (before.after == null) {
					lastExpr = entry; // This is now last expression in node
				}
				
				// Place ourself between before and after
				ExpressionEntry after = before.after;
				before.after = entry;
				entry.before = before;
				entry.after = after;
			}
		}
	}
	
	private static class ExpressionEntry {
		
		/**
		 * Index of byte where this appears.
		 */
		private final int index;
		
		/**
		 * Expression information for the parser.
		 */
		private final ExpressionInfo info;
		
		/**
		 * The entry before this one. May be null.
		 */
		private ExpressionEntry before;
		
		/**
		 * The entry after this one. May be null.
		 */
		private ExpressionEntry after;
		
		public ExpressionEntry(int index, ExpressionInfo info) {
			this.index = index;
			this.info = info;
		}
	}
	
	private final Node root;
	
	public ExpressionTree() {
		this.root = new Node(0);
	}
	
	public void put(byte[] pattern, ExpressionInfo expr) {
		Node node = root;
		for (int i = 0; i < pattern.length; i++) {
			node = node.write(i, pattern.length - i, pattern[i]);
		}
		node.addExpr(pattern.length - 1, expr);
	}
}
