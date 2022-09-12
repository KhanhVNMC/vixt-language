package vn.giakhanhvn.vixtlang.compiler;

import vn.giakhanhvn.vixtlang.Main;

import java.io.File;

public class CompilerFlagParsing {
    public boolean noRun;
    public boolean clean;
    public boolean debug;
    public boolean absSilent;
    public boolean nativefeel;
    public Compiler.Language languageSetting = new Compiler.Language(Compiler.BuiltInLang.VN);
    private final String[] flags;

    public CompilerFlagParsing(String[] f) {
        this.flags = f;
        this.checkFlags();
    }

    void checkFlags() {
        this.noRun = false;
        this.clean = false;
        for (String fl : this.flags) {
            if (!fl.contains("--")) continue;
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
            } else if (ffl.contains("langc=")) {
				try {
					this.languageSetting = new Compiler.Language(
						new File(Main.VIXT_PATH + "langs"
								+ Main.SEPR + ffl.replace("langc=", "") + ".vl")
					);
				} catch (Exception e) {
                    e.printStackTrace();
					throw new IllegalStateException("Unable to load language file");
				}
			} else if (ffl.contains("lang=")) {
				try {
					this.languageSetting = new Compiler.Language(
							Compiler.BuiltInLang.valueOf(
									ffl.replace("lang=", "").toUpperCase()
							)
					);
				} catch (Exception e) {
					throw new IllegalStateException("Unable to load language! Wrong type");
				}
			}
        }
    }
}
