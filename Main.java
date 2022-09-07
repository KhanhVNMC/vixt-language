package vn.giakhanhvn.jassembly.filehandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) throws Exception {
		File sf = new File("std.lyb");
		if (!sf.exists()) {
			sf.createNewFile();
			FileWriter w = new FileWriter("std.lyb");
			w.write(Main.STD_LIB);
			w.close();
		}
		File f = new File("test.txt");
		if (!f.exists())
			f.createNewFile();
		String[] vnc = FileLoader.bufferReader("test.txt");
		Compiler c = new Compiler(f, args);
		c.compile();
	}

	public static <T> void xáo(T[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			T a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public static double thành_stp(String st) {
		try {
			return Double.parseDouble(st);
		} catch (Exception e) {
			return -1d;
		}
	}

	public static int thành_stn(String st) {
		try {
			return Integer.parseInt(st);
		} catch (Exception e) {
			return -1;
		}
	}

	public static long thành_stnl(String st) {
		try {
			return Long.parseLong(st);
		} catch (Exception e) {
			return -1;
		}
	}

	private static void printLines(String cmd, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			System.out.println(cmd + " " + line);
		}
	}

	private static void runProcess(String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
		printLines(command + " stdout:", pro.getInputStream());
		printLines(command + " stderr:", pro.getErrorStream());
		pro.waitFor();
		System.out.println(command + " exitValue() " + pro.exitValue());
	}

	static final String STD_LIB = "// Standard BStray library\n" + "// @author GiaKhanhVN\n" + "// version 0.1\n" + "\n"
			+ "$Imports(\"java.util.Arrays\",\"java.util.Random\")\n" + "\n" + "public static class %lname% {\n"
			+ " public static final String VERSION = \"ALPHA-0.1-B0144\";"
			+ "	public static int random(int min, int max) {\n" + "		if (min < 0) min = 0;\n"
			+ "		if (max < 0) max = 0;\n" + "		return new Random().nextInt((max - min) + 1) + min;\n" + "	}\n"
			+ "	static String xâu_random(int targetStringLength) {\n" + "		int leftLimit = 97;\n"
			+ "		int rightLimit = 122;\n" + "		var random = new Random();\n"
			+ "		var generatedString = random.ints(leftLimit, rightLimit + 1)\n"
			+ "			.limit(targetStringLength)\n"
			+ "			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)\n"
			+ "			.toString();\n" + "		return generatedString;\n" + "	} \n"
			+ "	public static <T> void sắp_xếp(T[] t) {\n" + "		Arrays.sort(t);\n" + "	}\n"
			+ "	public static <T> void xếp(T[] t) {\n" + "		sắp_xếp(t);\n" + "	}\n"
			+ "	public static <T> void xáo_mảng(T[] ar) {\n" + "		xáo(ar);\n" + "	}\n" + "\n"
			+ "	public static <T> void xáo(T[] ar) {\n" + "		Random rnd = new Random();\n"
			+ "		for (int i = ar.length - 1; i > 0; i--) {\n" + "			int index = rnd.nextInt(i + 1);\n"
			+ "			T a = ar[index];\n" + "			ar[index] = ar[i];\n" + "			ar[i] = a;\n" + "		}\n"
			+ "	}\n" + "	public static double thành_stp(String st) {\n" + "		try {\n"
			+ "			return Double.parseDouble(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1d;\n" + "		}\n" + "	}\n" + "	public static int thành_stn(String st) {\n"
			+ "		try {\n" + "			return Integer.parseInt(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1;\n" + "		}\n" + "	}\n" + "	public static long thành_stnl(String st) {\n"
			+ "		try {\n" + "			return Long.parseLong(st);\n" + "		} catch (Exception e) {\n"
			+ "			return -1;\n" + "		}\n" + "	}\n" + "}";
}
