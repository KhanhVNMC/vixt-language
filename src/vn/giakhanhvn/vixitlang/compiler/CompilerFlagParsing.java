package vn.giakhanhvn.vixitlang.compiler;

public class CompilerFlagParsing {
	private String[] flags;
	public boolean noRun;
	public boolean clean;
	public boolean debug;
	
	public CompilerFlagParsing(String[] f) {
		this.flags = f;
		this.checkFlags();
	}
	
	void checkFlags() {
		this.noRun = false;
		this.clean = false;
		for (String fl : this.flags) {
			if (fl.contains("--")) {
				String ffl = fl.replace("--", "");
				if (ffl.equalsIgnoreCase("clean"))
				this.clean = true;
				else if (ffl.equalsIgnoreCase("norun"))
				this.noRun = true;
				else if (ffl.equalsIgnoreCase("debug"))
				this.debug = true;
			}
		}
	}
	
}
