package vn.giakhanhvn.vixtlang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

import vn.giakhanhvn.vixtlang.compiler.Compiler;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {
	public static final String VERSION = "v0.1.2-beta Build 20";
	public static final int BUILD_VERSION = 1023;

	public static String VIXT_PATH = "C:\\Vixt\\";
	public static String SEPR = File.separator;
	public static OSType OS = OSType.WINDOWS;

	public static void main(String[] args) throws Exception {
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
			VIXT_PATH = "";
			Main.OS = OSType.UNIX;
		}
		try {
			Runtime.getRuntime().exec("javac -version");
		} catch (Exception e) {
			Main.out(
					"[!] Khong tim thay JDK tren may cua ban! Vui long cai dat tai duong link nay\nhttps://www.oracle.com/java/technologies/downloads/#jdk18-windows\nBam enter de thoat!");
			Scanner s = new Scanner(System.in);
			s.nextLine();
			s.close();
			System.exit(0);
			return;
		}
		long mil = System.currentTimeMillis();
		StringBuilder s = new StringBuilder();
		if (args.length <= 0) {
			err("[!] Lenh Khong hop le! Yeu cau it nhat vi tri cua file can compile");
			err("File can compile phai la mot ma nguon Vixt hop le (duoi .vx)");
			out("");
			Main.printHowTo();
			return;
		}
		if (args[0].equalsIgnoreCase("-version")) {
			Main.out("Vixt Development Kit (VDK) " + VERSION + "\nAuthor: GiaKhanhVN @ XiTrayTechnologies");
			Main.out("Build Version Numeral ID: " + BUILD_VERSION);
			return;
		} else if (args[0].equalsIgnoreCase("-git")) {
			Main.out("Author: GiaKhanhVN aka KhanhVNMC or Henser1255 @ XiTrayTechnologies");
			Main.out("Official GitHub repository: https://github.com/KhanhVNMC/vixt-language");
			return;
		}
		for (var a : args) {
			s.append(a + " ");
		}
		boolean spaced = false;
		if (s.charAt(0) == '"')
			spaced = true;
		String fr = s.toString().split("\\.vx")[0] + ".vx" + (spaced ? "\"" : "");
		File src = new File(new File(fr).getAbsolutePath());
		if (!src.exists()) {
			err("[!] Lenh Khong hop le! Khong tim thay file!");
			err("File can compile phai la mot ma nguon Vixt hop le (duoi .vx)");
			out("");
			Main.printHowTo();
			return;
		}
		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal signal) {
				out("");
				out("Chuong trinh da bi buoc dong! Thoi gian hoat dong: " + (System.currentTimeMillis() - mil) + "ms");
				System.exit(0);
			}
		});
		Main.createOrLoadLibs();
		Main.createOrLoadLangs();
		Compiler c = new Compiler(src, args);
		c.compile();
	}

	public static enum OSType {
		WINDOWS, UNIX, APPLE
	}

	static String outStream(InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			return line;
		}
		return null;
	}

	public static void printHowTo() {
		out("Cach kiem tra cac thong tin");
		out(" -version  Kiem tra phien ban hien tai cua Vixt Lang");
		out(" -git  Mo GitHub chinh thuc cua Vixt PL");
		out("Vi du: vixt -version");
		out("");
		out("Cach su dung: vixt tenfile.vx <cac flags, khong co cung duoc>");
		out("");
		out("Ngoai ra, con co cac Compiler flag nhu sau");
		out(" --clean   Don dep cac file tam thoi ngay lap tuc");
		out(" --silent   Han che cac console print cua phan mem");
		out(" --nv   Han che hoan toan console print cua phan mem");
		out(" --norun   Chi compile nhung khong interpret phan mem");
		out(" --debug   Bat che do debug phan mem");
		out(" --lang=<en/vi>   Su dung nhung built-in language syntax (VN/EN)");
		out(" --langc=<file name>   Su dung custom language syntax (.vl) (Example: --langc=sivn)");
		out("Vi du:  vixt tenfile.vx --clean --silent");
	}

	public static void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				if (!Files.isSymbolicLink(f.toPath())) {
					deleteDir(f);
				}
			}
		}
		file.delete();
	}

	public static void out(String s) {
		System.out.println(s);
	}

	public static void err(String s) {
		System.err.println(s);
	}

	public static void createOrLoadLibs() throws IOException {
		Main.STD_LIB = Main.format(Main.STD_LIB, "\n", System.lineSeparator());
		File fld = new File(Main.VIXT_PATH + "libs/");
		if (!fld.exists())
			fld.mkdirs();
		File sf = new File(Main.VIXT_PATH + "libs/std.lyb");
		if (!sf.exists()) {
			sf.createNewFile();
			FileWriter w = new FileWriter(Main.VIXT_PATH + "libs/std.lyb", StandardCharsets.UTF_8);
			w.write(Main.STD_LIB);
			w.close();
		}
	}
	public static void createOrLoadLangs() throws IOException {
		Main.SIMPLIFIED_VNESE = Main.format(Main.SIMPLIFIED_VNESE, "\n", System.lineSeparator());
		Main.EXAMPLE_FILE = Main.format(Main.EXAMPLE_FILE, "\n", System.lineSeparator());
		File fld = new File(Main.VIXT_PATH + "langs/");
		if (!fld.exists())
			fld.mkdirs();
		File sf = new File(Main.VIXT_PATH + "langs/sivn.vl");
		if (!sf.exists()) {
			sf.createNewFile();
			FileWriter w = new FileWriter(Main.VIXT_PATH + "langs/sivn.vl", StandardCharsets.UTF_8);
			w.write(Main.SIMPLIFIED_VNESE);
			w.close();
		}
		File sfx = new File(Main.VIXT_PATH + "langs/customSyntax.vl");
		if (!sfx.exists()) {
			sfx.createNewFile();
			FileWriter w = new FileWriter(Main.VIXT_PATH + "langs/customSyntax.vl", StandardCharsets.UTF_8);
			w.write(Main.EXAMPLE_FILE);
			w.close();
		}
	}
	public static String format(String str, Object... repl) {
		return Main.format(str, "%d", repl);
	}

	public static String format(String str, String replBy, Object... repl) {
		for (Object o : repl) {
			str = str.replace(replBy, o.toString());
		}
		return str;
	}
	static String EXAMPLE_FILE = "# Custom Language File used for the Vixt Programming Language\n" +
			"# The order of the syntax translation must kept the same\n" +
			"# Lines with //, #, ; at the beginning are ignored\n" +
			"# \"#\" can be used anywhere and do not interfere with the config\n" +
			"\n" +
			"# Those settings below are an example on how Vixt Syntax can be customized\n" +
			"# The syntax used below are an exact copy of the Compiler.BuiltInLang.EN (English) syntax\n" +
			"\n" +
			"# In order to use custom syntax in the Vixt Compiler, add \"--langc=<custom lang file>\" and\n" +
			"# you can use custom syntax without flags modification by adding \"#langc <custom lang file>\" to the source file\n" +
			"# while <custom lang file> is the syntax file name. For example with this file \"--langc=customSyntax\"\n" +
			"\n" +
			"; Printing\n" +
			"prt # Change the text before this\n" +
			"\n" +
			"; Define a variable\n" +
			"set # Change the text before this\n" +
			"\n" +
			"; Printing with a Line\n" +
			"prtln # Change the text before this\n" +
			"\n" +
			"; Linebreak (\\n for Unix and \\r\\n for NT)\n" +
			"lb # Change the text before this\n" +
			"\n" +
			"; Thread Sleeping (Stop the code for specific amount of seconds)\n" +
			"await # Change the text before this\n" +
			"\n" +
			"; If statement\n" +
			"if # Change the text before this\n" +
			"\n" +
			"; Else statement\n" +
			"else # Change the text before this\n" +
			"\n" +
			"; Then statement\n" +
			"then # Change the text before this\n" +
			"\n" +
			"; While Loop\n" +
			"while # Change the text before this\n" +
			"\n" +
			"; For loop \n" +
			"for # Change the text before this\n" +
			"\n" +
			"; Call Function\n" +
			"call # Change the text before this\n" +
			"\n" +
			"; Break Function\n" +
			"break # Break out of the loop\n" +
			"\n" +
			"; Return (use to return (stop the code) only)\n" +
			"return # Change the text before this\n" +
			"\n" +
			"; Continue (Skip this part in a Loop)\n" +
			"continue # Change the text before this\n" +
			"\n" +
			"; Defining Arrays\n" +
			"defarr # Change the text before this\n" +
			"\n" +
			"; For each statement\n" +
			"foreach # Change the text before this\n" +
			"\n" +
			"; For loop \n" +
			"for # Change the text before this\n" +
			"\n" +
			"; Await for user input \n" +
			"input # Change the text before this\n" +
			"\n" +
			"; To statement (=)\n" +
			"to # Change the text before this\n" +
			"\n" +
			"; Function Defining\n" +
			"fun # Change the text before this\n" +
			"\n" +
			"; Return a parameter\n" +
			"return # Change the text before this\n" +
			"\n" +
			"; Else If statement\n" +
			"elseIf # Change the text before this\n" +
			"\n" +
			"; As statement\n" +
			"as # Change the text before this\n" +
			"\n" +
			"; In statement\n" +
			"in # Change the text before this";
	static String SIMPLIFIED_VNESE = "# ENGLISH\n" +
			"#\n" +
			"# This is the non-UTF8/simplified Vietnamese Instructions \n" +
			"# implementation for Vixt Programming Language\n" +
			"# This is a built-in language option\n" +
			"#\n" +
			"# VIETNAMESE\n" +
			"#\n" +
			"# Day la bo cu phap da duoc don gian hoa/phi UTF-8\n" +
			"# cho tieng Viet danh cho ngon ngu lap trinh Vixt\n" +
			"# Day la mot tuy chon ngon ngu duoc tich hop san\n" +
			"#\n" +
			"# How to use/cach su dung:\n" +
			"# You can add syntax option to your source file \"#langc <sivn>\"\n" +
			"# or use compiler flags modification \"vixt <file> <flags> --langc=sivn\"\n" +
			"#\n" +
			"# @author: GiaKhanhVN aka KhanhVN\n" +
			"\n" +
			"\n" +
			"; Printing\n" +
			"in # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Define a variable\n" +
			"dat # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Printing with a Line\n" +
			"inxd # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Linebreak (\\n for Unix and \\r\\n for NT)\n" +
			"xd # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Thread Sleeping (Stop the code for specific amount of seconds)\n" +
			"doi # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; If statement\n" +
			"neu # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Else statement\n" +
			"khong # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Then statement\n" +
			"thi # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; While Loop\n" +
			"khi # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; For loop \n" +
			"cho # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Call Function\n" +
			"goi # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Break Function\n" +
			"pha_lap # Break out of the loop\n" +
			"\n" +
			"; Return (use to return (stop the code) only)\n" +
			"quay_lai # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Continue (Skip this part in a Loop)\n" +
			"nhay_qua # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Defining Arrays\n" +
			"tao_mang # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; For each statement\n" +
			"cho_moi # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; For loop \n" +
			"cho # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Await for user input \n" +
			"nhap # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; To statement (=)\n" +
			"vao # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Function Defining\n" +
			"ham # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Return a parameter\n" +
			"tra # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; Else If statement\n" +
			"khong_neu # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; As statement\n" +
			"la # Vixt Simplified Vietnamese\n" +
			"\n" +
			"; In statement\n" +
			"trong # Vixt Simplified Vietnamese";
	static String STD_LIB = "// Standard VixtLang library\n" + "// @author GiaKhanhVN\n" + "// version 0.1\n" + "\n"
			+ "$Imports(\"java.util.Arrays\",\"java.util.Random\")\n" + "\n" + "public static class %lname% {\n"
			+ " public static final String VERSION = \"ALPHA-0.1-B0155\";\n"
			+ "	public static int random(int min, int max) {\n" + "		if (min < 0) min = 0;\n"
			+ "		if (max < 0) max = 0;\n" + "		return new Random().nextInt((max - min) + 1) + min;\n" + "	}\n"
			+ "	static String xau_random(int targetStringLength) {\n" + "		int leftLimit = 97;\n"
			+ "		int rightLimit = 122;\n" + "		var random = new Random();\n"
			+ "		var generatedString = random.ints(leftLimit, rightLimit + 1)\n"
			+ "			.limit(targetStringLength)\n"
			+ "			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)\n"
			+ "			.toString();\n" + "		return generatedString;\n" + "	} \n"
			+ "	public static <T> void sapxep(T[] t) {\n" + "		Arrays.sort(t);\n" + "	}\n"
			+ "	public static <T> void xep(T[] t) {\n" + "		sapxep(t);\n" + "	}\n"
			+ "	public static <T> void xao_mang(T[] ar) {\n" + "		xao(ar);\n" + "	}\n" + "\n"
			+ "	public static <T> void xao(T[] ar) {\n" + "		Random rnd = new Random();\n"
			+ "		for (int i = ar.length - 1; i > 0; i--) {\n" + "			int index = rnd.nextInt(i + 1);\n"
			+ "			T a = ar[index];\n" + "			ar[index] = ar[i];\n" + "			ar[i] = a;\n" + "		}\n"
			+ "	}\n" + "	public static double thanh_stp(String st) {\n" + "		try {\n"
			+ "			return Double.parseDouble(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1d;\n" + "		}\n" + "	}\n" + "	public static int thanh_stn(String st) {\n"
			+ "		try {\n" + "			return Integer.parseInt(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1;\n" + "		}\n" + "	}\n" + "	public static long thanh_stnl(String st) {\n"
			+ "		try {\n" + "			return Long.parseLong(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1;\n" + "		}\n" + "	}\n	public static long[] rangeLong(int fr, int to) {\n"
			+ "		if (fr < 0 || to < 0)\n" + "			return new long[0];\n" + "		if (fr > to) {\n"
			+ "			int cfr = fr;\n" + "			int cto = to;\n" + "			to = cfr;\n"
			+ "			fr = cto;\n" + "		}\n" + "		long[] ret = new long[(to - fr) + 1];\n"
			+ "		int index = 0;\n" + "		for (int i = fr; i < to + 1; i++) {\n" + "			ret[index] = i;\n"
			+ "			index++;\n" + "		}\n" + "		return ret;\n" + "	}\n" + "" + "}";
}
