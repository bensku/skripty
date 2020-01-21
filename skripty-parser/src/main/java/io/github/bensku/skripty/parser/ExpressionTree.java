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
		 * A branch happens within this byte.
		 */
		private int branchIndex;
		
		/**
		 * The bit we branch on, from branchIndex.
		 */
		private int branchBit;
		
		/**
		 * Node where to branch to if bit at {@link #branchBit} is 0.
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
		
		private byte readOrNull(int index) {
			if (index >= bytes.length) {
				return 0;
			} else {
				return bytes[index];
			}
		}
		
		private void writeByte(int index, byte value) {
			if (index >= bytes.length) {
				byte[] newBytes = new byte[bytes.length * 2];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				bytes = newBytes;
			}
			bytes[index] = value;
		}
		
		public void write(byte[] data, int start, ExpressionInfo info) {
			int i;
			for (i = 0; start + i < data.length; i++) {
				byte oldValue = readOrNull(i);
				byte newValue = data[start + i];
				
				if (branchIndex == i) { // There is a branch that we need to follow
					// Select (or rare cases, create) a branch
					Node newNode = selectBranch(newValue);
					if (newNode == null) {
						newNode = createBranch(i, newValue);
					}
					newNode.write(data, start + i, info);
					return; // Assume that write succeeded somewhere down the line
				}
				
				// If we're not changing anything, things are very simple
				if (oldValue == newValue || oldValue == 0) { // Overwrite NULLs, too
					writeByte(i, newValue);
					continue;
				}
				
				// But otherwise we probably have a branch here!
				createBranch(i, newValue).write(data, start + i, info);
				return; // Written down the line, hopefully
			}
			
			// Reached a node and index matching the data
			addExpr(data.length - 1, info);
		}
		
		/**
		 * 
		 * @param value The byte at {@link #branchIndex} that will be used to
		 * decide which path we branch to.
		 */
		public Node selectBranch(byte value) {
			// Find the first differing bit
			int diffBit = Integer.numberOfLeadingZeros(bytes[branchIndex] ^ value) - 24;
			if (diffBit < branchBit) { // Difference BEFORE the branch bit!
				return null; // Caller might want to create a branch
			} else { // Just select a branch
				// Create a mask where this is 1, everything else zero
				int mismatchMask = 1 << (7 - diffBit);
				// Check whether our or their bit there is zero
				boolean ourZero = (mismatchMask & value) == 0;
				return ourZero ? branch0 : branch1;
			}
		}
		
		public Node createBranch(int index, byte value) {
			int globalIndex = nodeStart + index;
			int bitIndex = Integer.numberOfLeadingZeros(bytes[index] ^ value) - 24;
			
			// Create a mask where this is 1, everything else zero
			int mismatchMask = 1 << (7 - bitIndex);
			// Check whether our or their bit there is zero
			boolean ourZero = (mismatchMask & value) == 0;
			
			// Node for existing content
			Node oldNode = new Node(globalIndex);
			oldNode.bytes = new byte[bytes.length - index];
			System.arraycopy(bytes, index, oldNode.bytes, 0, oldNode.bytes.length);
			if (branchIndex != -1) { // There is another branch after this one
				oldNode.branchIndex = branchIndex - index;
				oldNode.branchBit = branchBit;
			}
			oldNode.branch0 = branch0;
			oldNode.branch1 = branch1;
			
			// Copy expressions to that node if needed
			ExpressionEntry firstToCopy = lastExpr;
			if (firstToCopy != null) {
				while (firstToCopy.index >= globalIndex && firstToCopy.before != null) {
					firstToCopy = firstToCopy.before;
				}
				oldNode.firstExpr = firstToCopy;
				oldNode.lastExpr = lastExpr;
			}
			
			// New node for new content
			Node newNode = new Node(globalIndex);
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
			
			if (firstToCopy != null) {
				lastExpr = firstToCopy.before;
				firstToCopy.before = null; // It is in new node now, won't need that
			}
			
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
		root.bytes = new byte[Node.INITIAL_SIZE];
	}
	
	public void put(byte[] pattern, ExpressionInfo expr) {
		root.write(pattern, 0, expr);
	}
}
