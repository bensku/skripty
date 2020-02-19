package io.github.bensku.skripty.parser.log;

public class ParseResult<T> {
	
	/**
	 * Creates a new parse result indicating a success.
	 * @param block Parsed script block.
	 * @param messages Messages from the parser.
	 * @return A parse result.
	 */
	public static <T> ParseResult<T> success(T result, ParserMessage... messages) {
		return new ParseResult<>(true, result, messages);
	}
	
	/**
	 * Creates a new parse result indicating a failure.
	 * @param messages Messages from the parser. They should contain errors
	 * that tell why parsing failed.
	 * @return A parse result.
	 */
	public static <T> ParseResult<T> failure(ParserMessage... messages) {
		return new ParseResult<>(false, null, messages);
	}
	
	/**
	 * If this result indicates a success.
	 */
	private final boolean success;
	
	/**
	 * Result of successful parsing. Null when not successful.
	 */
	private final T result;
	
	/**
	 * Messages from parser that should be shown to user.
	 */
	private final ParserMessage[] messages;

	private ParseResult(boolean success, T result, ParserMessage[] messages) {
		this.success = success;
		this.result = result;
		this.messages = messages;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public T getResult() {
		if (!success) {
			throw new IllegalStateException("parsing failed");
		}
		return result;
	}
	
	public ParserMessage[] getMessages() {
		return messages;
	}
}
