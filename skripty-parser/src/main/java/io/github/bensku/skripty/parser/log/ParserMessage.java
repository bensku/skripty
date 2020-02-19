package io.github.bensku.skripty.parser.log;

import io.github.bensku.skripty.parser.script.SourceNode;

/**
 * A message from parser to the user.
 *
 */
public class ParserMessage {
	
	/**
	 * Type of message.
	 *
	 */
	public enum Type {
		
		WARNING,
		ERROR
	}
	
	/**
	 * Creates a new parser message builder.
	 * @param type Type of the message.
	 * @param message The message text.
	 * @return A message builder.
	 */
	public static Builder msg(Type type, String message) {
		return new Builder(type, message);
	}
	
	/**
	 * Creates a new parser message builder for a
	 * {@link Type#WARNING warning} message.
	 * @param message The message text.
	 * @return A message builder.
	 */
	public static Builder warn(String message) {
		return msg(Type.WARNING, message);
	}
	
	/**
	 * Creates a new parser message builder for an
	 * {@link Type#ERROR error} message.
	 * @param message The message text.
	 * @return A message builder.
	 */
	public static Builder error(String message) {
		return msg(Type.ERROR, message);
	}
	
	/**
	 * A tiny builder for fluent creation of parser messages.
	 *
	 */
	public static class Builder {
		
		private final Type type;
		private final String message;
		
		private Builder(Type type, String message) {
			this.type = type;
			this.message = message;
		}
		
		public ParserMessage at(SourceNode source, int start, int end) {
			return new ParserMessage(type, source, start, end, message);
		}
	}
	
	/**
	 * Type of this message.
	 */
	private final Type type;

	/**
	 * Source node that this message is about.
	 */
	private final SourceNode source;
	
	/**
	 * Start (inclusive) and end (exclusive) indices of what this message
	 * describes in the source node.
	 */
	private final int start, end;
	
	/**
	 * The message.
	 */
	private final String message;

	private ParserMessage(Type type, SourceNode source, int start, int end, String message) {
		this.type = type;
		this.source = source;
		this.start = start;
		this.end = end;
		this.message = message;
	}
	
	public Type getType() {
		return type;
	}

	public SourceNode getSource() {
		return source;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getMessage() {
		return message;
	}
	
	/**
	 * Formats an user-friendly message from this.
	 * @return Text to show to the user.
	 */
	public String format() {
		return type.name() + ": " + message; // TODO better formatting, not needed yet
	}
}
