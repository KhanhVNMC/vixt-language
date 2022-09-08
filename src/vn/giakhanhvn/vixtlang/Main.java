package vn.giakhanhvn.vixtlang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

import vn.giakhanhvn.vixtlang.compiler.Compiler;

public class Main {
	public static String VIXT_PATH = "C:\\Vixt\\";
	
	public static void main(String[] args) throws Exception {
		StringBuilder s = new StringBuilder();
		if (args.length <= 0) {
			err("[!] Lenh Khong hop le! Yeu cau it nhat vi tri cua file can compile");
			err("File can compile phai la mot ma nguon Vixt hop le (duoi .vx)");
			out("");
			Main.printHowTo();
			return;
		}
		for (var a : args) {
			s.append(a + " ");
		}
		boolean spaced = false;
		if (s.charAt(0) == '"') spaced = true;
		String fr = s.toString().split("\\.vx")[0] + ".vx" + (spaced ? "\"" : "");
		File src = new File(fr);
		if (!src.exists()) {
			err("[!] Lenh Khong hop le! Khong tim thay file!");
			err("File can compile phai la mot ma nguon Vixt hop le (duoi .vx)");
			out("");
			Main.printHowTo();
			return;
		}
		Main.createOrLoadLibs();
		Compiler c = new Compiler(src, args);
		c.compile();
	}
	public static void printHowTo() {
		out("Cach su dung: vixt tenfile.vx <cac flags, khong co cung duoc>");
		out("");
		out("Ngoai ra, con co cac Compiler flag nhu sau");
		out("--clean   Don dep cac file tam thoi ngay lap tuc");
		out("--silent   Han che cac console print cua phan mem");
		out("--nv   Han che hoan toan console print cua phan mem");
		out("--norun   Chi compile nhung khong interpret phan mem");
		out("--debug   Bat che do debug phan mem");
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
		File fld = new File(Main.VIXT_PATH + "libs/");
		if (!fld.exists()) fld.mkdirs();
		File sf = new File("libs/std.lyb");
		if (!sf.exists()) {
			sf.createNewFile();
			FileWriter w = new FileWriter(Main.VIXT_PATH + "libs/std.lyb", StandardCharsets.UTF_8);
			w.write(Main.STD_LIB);
			w.close();
		}
	}

	static final String STD_LIB = "// Standard BStray library\n" + "// @author GiaKhanhVN\n" + "// version 0.1\n" + "\n"
			+ "$Imports(\"java.util.Arrays\",\"java.util.Random\")\n" + "\n" + "public static class %lname% {\n"
			+ " public static final String VERSION = \"ALPHA-0.1-B0144\";"
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
			+ "			return -1;\n" + "		}\n" + "	}\n" + "}";
}
