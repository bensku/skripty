package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Converts symbolic assembly language to IR.
 *
 */
public class IrAssembler {

	/**
	 * Parses LoadLiteral literals.
	 */
	private final Function<String, Object> literalParser;
	
	public IrAssembler(Function<String, Object> literalParser) {
		this.literalParser = literalParser;
	}
	
	/**
	 * Parses a block of assembly separated by new lines.
	 * @param source Source code.
	 * @return An IR block.
	 * @throws IllegalArgumentException When parsing one of the lines failed.
	 */
	public IrBlock parseBlock(String source) {
		String[] lines = source.split("\n");
		IrNode[] nodes = new IrNode[lines.length];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = parseNode(lines[i]);
		}
		return new IrBlock(nodes);
	}
	
	/**
	 * Parses a single IR node.
	 * @param line Line of assembly code.
	 * @return An IR node.
	 * @throws IllegalArgumentException When parsing line fails.
	 */
	public IrNode parseNode(String line) {
		line = line.stripLeading();
		String type = line.substring(0, line.indexOf(' '));
		String content = line.substring(type.length() + 1);
		switch (type) {
		case "Pop":
			return IrNode.Pop.INSTANCE;
		case "LoadLiteral":
			return new IrNode.LoadLiteral(parseLiteral(content));
		case "LoadConstant":
			throw new UnsupportedOperationException("LoadConstant is not yet supported");
		case "CallPlain":
			try {
				return new IrNode.CallPlain(parseMethodHandle(content), false);
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				throw new IllegalArgumentException("parsing call failed", e);
			}
		case "CallWithState":
			try {
				return new IrNode.CallWithState(parseMethodHandle(content), false);
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				throw new IllegalArgumentException("parsing call failed", e);
			}
		case "Jump":
			String[] args = content.split(" ");
			return new IrNode.Jump(parseScopeEntry(args[0]), Integer.parseInt(args[1]));
		default:
			throw new IllegalArgumentException("unknown instruction: " + type);
		}
	}
	
	private Object parseLiteral(String text) {
		Object value = literalParser.apply(text);
		if (value == null) {
			throw new IllegalArgumentException("could not parse literal: " + text);
		}
		return value;
	}
	
	private MethodHandle parseMethodHandle(String text) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		String[] parts = text.split(" ");
		Class<?> owner = parseClassName(parts[0]);
		String name = parts[1];
		Class<?> returnType = parseClassName(parts[2]);
		Class<?>[] paramTypes = new Class[parts.length - 3];
		for (int i = 0; i < paramTypes.length; i++) {
			paramTypes[i] = parseClassName(parts[i + 3]);
		}
		return MethodHandles.publicLookup().findVirtual(owner, name, MethodType.methodType(returnType, paramTypes));
	}
	
	private Class<?> parseClassName(String name) throws ClassNotFoundException {
		switch (name) {
		case "void":
			return void.class;
		case "byte":
			return byte.class;
		case "short":
			return short.class;
		case "char":
			return char.class;
		case "int":
			return int.class;
		case "long":
			return long.class;
		case "float":
			return float.class;
		case "double":
			return double.class;
		default:
			return Class.forName(name);
		}
	}
	
	private ScopeEntry parseScopeEntry(String text) {
		return ScopeEntry.valueOf(text);
	}
	
}
