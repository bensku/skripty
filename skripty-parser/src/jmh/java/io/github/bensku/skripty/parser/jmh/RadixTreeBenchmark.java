package io.github.bensku.skripty.parser.jmh;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import io.github.bensku.skripty.parser.util.RadixTree;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RadixTreeBenchmark {
	
	public static final int RANDOM_COUNT = 1_000_000;
	public static final int RANDOM_SEED = 47868;

	@State(Scope.Thread)
	public static class Randoms {
		
		private Random rng;
		private byte[][] array;
		
		private int index = 0;
		
		public Randoms() {
			this.rng = new Random(RANDOM_SEED);
			this.array = new byte[RANDOM_COUNT][];
			for (int i = 0; i < RANDOM_COUNT; i++) {
				array[i] = generate();
			}
		}
		
		private byte[] generate() {
			int len = rng.nextInt(128) + 1;
			byte[] bytes = new byte[len];
			rng.nextBytes(bytes);
			return bytes;
		}
		
		public byte[] get() {
			if (index == array.length) {
				index = 0;
			}
			return array[index++];
		}
	}
	
	@State(Scope.Thread)
	public static class EmptyState {
		public Object value = new Object();
		public RadixTree<Object> tree = new RadixTree<>(Object.class);
	}

	@Benchmark
	public void putTest(EmptyState state, Randoms randoms, Blackhole bh) {
		state.tree.put(randoms.get(), state.value);
		bh.consume(state.tree);
	}
	
	@State(Scope.Thread)
	public static class FullState {
		public RadixTree<Object> tree = new RadixTree<>(Object.class);
		
		public FullState() {
			Randoms randoms = new Randoms();
			Object value = new Object();
			tree.put(randoms.get(), value);
		}
	}
	
	@Benchmark
	public void getTest(FullState state, Randoms randoms, Blackhole bh) {
		bh.consume(state.tree.get(randoms.get(), 0));
	}
}
