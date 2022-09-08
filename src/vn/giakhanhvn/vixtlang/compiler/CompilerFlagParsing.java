package vn.giakhanhvn.vixtlang.compiler;

public class CompilerFlagParsing {
	private String[] flags;
	public boolean noRun;
	public boolean clean;
	public boolean debug;
	public boolean absSilent;
	public boolean nativefeel;
	
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
				else if (ffl.equalsIgnoreCase("silent"))
				this.absSilent = true;
				else if (ffl.equalsIgnoreCase("nv")) {
					this.absSilent = true;
					this.nativefeel = true;
					this.debug = false;
				}
				
			}
		}
	}
	
}
