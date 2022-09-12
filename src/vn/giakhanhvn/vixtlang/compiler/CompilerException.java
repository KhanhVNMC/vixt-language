package vn.giakhanhvn.vixtlang.compiler;

public class CompilerException extends Exception {

	private static final long serialVersionUID = 1L;

	public CompilerException(Compiler.WrappedInstruction instr, String error) {
		super("File \"" + instr.fileName + "\", line " 
			+ instr.line + "\n   >> " 
			+ instr.content + "\n          ^" +
			"\nError: " + error
		);
	}

	public CompilerException(String msg) {
		super(msg);
	}
}
