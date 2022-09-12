package vn.giakhanhvn.vixtlang.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Node {
	
	static int srcTracker = 0;
	static int globalTracker = 0;
	public static Map<Integer, Integer> tracker = new HashMap<>();
	public static Map<Integer, Compiler.WrappedInstruction> mapper = new HashMap<>();

	private StringBuilder core;
	
	public LinkedList<String> queue = new LinkedList<>();
	
	public Node() {
		this.core = new StringBuilder();
	}

	public Node(String s) {
		this.core = new StringBuilder(s);
	}
	public void appendIgnore(String s) {
		this.queue.add(s);
	}
	public void append(boolean ignore, String s) {
		if (ignore) this.appendIgnore(s);
		else this.append(s);
	}
	public void append(String s) {
		globalTracker++;
		tracker.put(globalTracker, srcTracker);
		this.queue.add(s);
	}
	
	public StringBuilder build() {
		this.queue.forEach(q -> {
			core.append(q + System.lineSeparator());
		});
		return this.core;
	}
}
