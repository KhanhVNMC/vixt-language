package vn.giakhanhvn.vixtlang.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import vn.giakhanhvn.vixtlang.FileLoader;
import vn.giakhanhvn.vixtlang.Main;
import vn.giakhanhvn.vixtlang.exec.Console;

public class Compiler {
	private File source; 
	
	public Compiler(File file, String[] flags) throws FileNotFoundException, IOException {
		this(file, FileLoader.bufferReader(file.toString()), flags);
	}
	
	public void compile() {
		long l = System.currentTimeMillis();
		try {
			this.def();
			String path = this.interpret(l);
			if (path == null) throw new RuntimeException();
			this.out("Jar goc: " + path);
			this.out(true, "Vi tri JAR: " + this.source.getAbsoluteFile().getParent() + File.separator + this.pgrn + ".jar");
			Files.move(Paths.get(path), Paths.get(this.source.getAbsoluteFile().getParent() + File.separator + this.pgrn + ".jar"), StandardCopyOption.REPLACE_EXISTING);
			if (this.rfl.clean) {
				this.out(true, "Dang don dep file tam thoi...");
				Main.deleteDir(new File(new File(path).getParent()));
				this.out("File tam thoi trong thu muc: " + new File(path).getParent());
			}
			if (!this.rfl.noRun) {
				String a = Main.OS == Main.OSType.WINDOWS ? "\"" : "";
				String cmd = "java -jar " + a + this.source.getAbsoluteFile().getParent() + File.separator + this.pgrn + ".jar" + a;
				this.out("[DEBUG] Opening console with starting command " + cmd);
				Console con = new Console(cmd, this);
				con.open();
			}
			if (this.rfl.noRun && this.rfl.absSilent) System.out.println("Thanh cong! " + (System.currentTimeMillis() - l) + "ms");
		} catch (Exception e) {
			if (this.rfl.absSilent) System.out.println("Da xay ra loi! Stack trace: ");
			this.out(true, "[!] Khong thanh cong trong viec compile! Vui long lien he developer va kiem tra lai code cua ban! Stack trace:");
			e.printStackTrace();			
		}
		this.out(true, "Chuong trinh chay trong " + (System.currentTimeMillis() - l) + "ms");
	}
	
	private String[] src;
	private String compiled;
	public CompilerFlagParsing rfl;
	private String pgrn;
	protected static final String FOOTER = "}";
	protected static final String HEADER = 
		"public class %regx {public static void main(String[] bienChuongTrinh) throws Exception {";
	
	Map<String, String> dtyp = new LinkedHashMap<>();
	private List<String> regx = new ArrayList<>();
	
	// TODO Instructions
	static String[] instruc = {
		"in",	// PRINT 0
		"đặt",	// DEFINE VARIABLE 1
		"inxd",	// PRINTLN 2 
		"xd",	// LINEBREAK 3
		"đợi",	// AWAIT 4 
		"nếu",	// IF 5 
		"không",// ELSE 6
		"thì", // THEN 7 
		"khi", // WHILE 8
		"cho",	// FOR 9
		"gọi",	// CALL 10
		"phá_lặp",	// BREAK 11
		"quay_lại",	// RETURN 12
		"nhảy_qua",	// CONTINUE 13
		"tạo_mảng", // DEFINE ARRAY 14
		"cho_mỗi",	// FOR EACH 15
		"cho"	,// FOR 16
		"nhập",	// INPUT 17
		"vào",	// TO 18
		"hàm", // FUNCTION 19
		"trả", // RETURN VAR 20
		"không_nếu", // ELSE IF 21
	};
	
	Compiler(File f, String[] file, String[] flags) {
		this.source = f;
		this.src = file;
		this.rfl = new CompilerFlagParsing(flags);
		// FLOAT
		this.dtyp.put("st", "float");
		this.dtyp.put("sothuc", "float");
		this.dtyp.put("float", "float");
		// INT
		this.dtyp.put("stn", "int");
		this.dtyp.put("sotunhien", "int");
		this.dtyp.put("int", "int");
		// LONG
		this.dtyp.put("stnl", "long");
		this.dtyp.put("sotunhienlon", "long");
		this.dtyp.put("long", "long");
		// DOUBLE
		this.dtyp.put("stp", "double");
		this.dtyp.put("sothapphan", "double");
		this.dtyp.put("double", "double");
		// BOOLEAN
		this.dtyp.put("bool", "boolean");
		this.dtyp.put("bole", "boolean");
		// STRING
		this.dtyp.put("xkt", "String");
		this.dtyp.put("xaukytu", "String");
		this.dtyp.put("str", "String");
		this.dtyp.put("string", "String");
		// OBJ
		this.dtyp.put("vodinh", "Object");
		this.dtyp.put("?", "Object");
		this.dtyp.put("udf", "Object");
		this.dtyp.put("object", "Object");
		this.dtyp.put("obj", "Object");
		// AUTO
		this.dtyp.put("auto", "var");
		this.dtyp.put("td", "var");
		this.dtyp.put("tudong", "var");
		this.dtyp.put("$", "var");
	}
	// Bat dau compile
	private void def() {
		if (src[0].contains("#ten ")) 
			pgrn = src[0].replace("#ten ", "");
		else throw new RuntimeException();
		this.init();
	}
	
	//Khoi chay interpret
	private void init() {
		this.cleanFile();
		List<String> imports = new ArrayList<>();
		List<String> sinImports = new ArrayList<>();
		for (int i = 0 ; i < this.regx.size(); i++) {
			String instr = this.regx.get(i);
			if (instr.charAt(0) != '#') continue;
			String[] str = instr.split("\\s+");
			if (str[0].strip().equalsIgnoreCase("#thuvien")) {
				imports.add(instr.replace("#thuvien", "")
					.replace("<", "").replace(">", "")
					.replace(" ", "")
				);
			} else if (str[0].strip().equalsIgnoreCase("#import")) {
				sinImports.add(instr.replace("#import", "")
					.replace("<", "").replace(">", "")
					.replace(" ", ""));
			}
		}
		List<String[]> importedCache = new ArrayList<>();
		for (String sin : sinImports) {
			importedCache.add(new String[] {"import " + sin + ";", ""});
		}
		importedCache.add(new String[] {"import java.util.Scanner;", ""});
		for (String i : imports) {
			importedCache.add(this.importLibraries(i));
		}
		StringBuilder sb = new StringBuilder();
		for (String[] imp : importedCache) {
			sb.append(imp[0]);
		}
		sb.append(Compiler.HEADER
			.replace("%regx", this.pgrn
			.replace(" ", ""))
		);
		String udef = randString(random(10,25));
		sb.append("Scanner " + udef + "_java_util_Scanner_nativeInterface = new Scanner(System.in);");
		int curFunc = -1;
		boolean funcParsing = false;
		Map<Integer, List<String>> funcMap = new HashMap<>();
		for (int i = 0 ; i < this.regx.size(); i++) {
			String instr = this.regx.get(i);
			instr = instr.replace("	", "");		
			String node[] = instr.split("\\s+");
			if (node[0].strip().equalsIgnoreCase(instruc[19])) {
				curFunc++;
				if (!funcMap.containsKey(curFunc))
					funcMap.put(curFunc, new ArrayList<>());
				funcMap.get(curFunc).add(instr);
				funcParsing = true;
			} else if (funcParsing) 
				funcMap.get(curFunc).add(instr);
			if (!funcParsing) {
				this.parse(node, instr, sb, udef);
				if (instr.toCharArray()[0] == '{'
					|| instr.toCharArray()[instr.toCharArray().length - 1] == '{') 
				sb.append("{");
				if (instr.toCharArray()[0] == '}') 
				sb.append("}");
			}
			if (instr.toCharArray().length >= 2 && instr.toCharArray()[0] == '}' && instr.toCharArray()[1] == ';' && funcParsing) {
				funcParsing = false;
			}
		}
		sb.append("}");
		sb.append(Compiler.STD_RANGE);
		funcMap.keySet().forEach(k -> sb.append(this.parseMethodFunction(udef, funcMap.get(k))));
		for (String[] imp : importedCache) {
			sb.append(imp[1]);
		}
		sb.append("}");
		//funcMap.keySet().forEach(k -> funcMap.get(k).forEach(f -> this.out(true, f)));
		this.compiled = sb.toString();
	}
	boolean java = false;

	private void parse(String[] node, String instr, StringBuilder sb, String udef) {
		if ((node[0].strip().equalsIgnoreCase("$java") 
		|| node[0].strip().equalsIgnoreCase("$java{"))
		&& instr.contains("{")) {
			java = true;
			return;
		}
		if (instr.charAt(0) == '}' && node[0].strip().equalsIgnoreCase("}$end") && java) {
			java = false;
			return;		
		}
		if (java) {
			sb.append(instr);
			return;
		}
		if (node[0].strip().equalsIgnoreCase(instruc[0])) 
			sb.append(this.parsePrintFunction(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[1])) 
			sb.append(this.parseVarDeclare(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[2])) 
			sb.append(this.parsePrintLnFunction(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[3])) 
			sb.append(this.parseNewLineFunction());
		if (node[0].strip().equalsIgnoreCase(instruc[4])) 
			sb.append(this.parseWaitFunction(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[5])) 
			sb.append(this.parseConditionIf(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[8])) 
			sb.append(this.parseWhileLoop(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[6])) 
			sb.append(this.parseConditionElse(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[10])) 
			sb.append(instr.replaceFirst(instruc[10] + " ", "") + ";");
		if (node[0].strip().equalsIgnoreCase(instruc[11])) 
			sb.append("break;");
		if (node[0].strip().equalsIgnoreCase(instruc[12])) 
			sb.append("return;");
		if (node[0].strip().equalsIgnoreCase(instruc[13])) 
			sb.append("continue;");
		if (node[0].strip().equalsIgnoreCase(instruc[14])) 
			sb.append(this.parseArrayDeclare(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[15])) 
			sb.append(this.parseForEach(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[16])) 
			sb.append(this.parseFor(instr));
		if (node[0].strip().equalsIgnoreCase(instruc[17])) 
			sb.append(this.parseWaitForInput(udef, instr));
		if (node[0].strip().equalsIgnoreCase(instruc[21])) 
			sb.append(this.parseConditionElseIf(instr));
	}
	
	private void out(boolean serv, String string) {
		if (this.rfl.absSilent) return;
		if (!this.rfl.debug && !serv) return;
		System.out.println("[VIXT] "+ string);
	}
	
	private void out(String string) {
		this.out(false, string);
	}

	public void deb() {
		this.out(this.compiled);
	}
	private void cleanFile() {
		for (int i = 0 ; i < this.src.length ; i++) {
			// Xoa het comments
			src[i] = src[i]
				.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
			src[i] = this.normalize(src[i]);
			if (!src[i].isBlank()) regx.add(src[i]);
		}
	}
	private String parsePrintLnFunction(String in) {
		in = in.replaceFirst("inxd ", "");
		return "System.out.println(" + in + ");";
	}
	
	private String parsePrintFunction(String in) {
		in = in.replaceFirst("in ", "");
		return "System.out.print(" + in + ");";
	}
	
	private String parseWaitFunction(String in) {
		in = in.replace(Compiler.instruc[4] + " ", "");
		in = in.replace(" ", "");
		in = in.replace("s", "");
		try {
			return "Thread.sleep(" + Math.round(Double.parseDouble(in) * 1000L) + "L);";
		} catch (Exception e) {
			return "Thread.sleep(0);";
		}
	}
	
	private String parseNewLineFunction() {
		return "System.out.println();";
	}
	static final String STD_RANGE = "	"
			+ "	public static int[] range(int fr, int to) {\n"
			+ "		return khoảng(fr, to);\n"
			+ "	}\n"
			+ "	public static int[] khoảng(int fr, int to) {\n"
			+ "		if (fr < 0 || to < 0) return new int[0];\n"
			+ "		if (fr > to) {\n"
			+ "			int cfr = fr;\n"
			+ "			int cto = to;\n"
			+ "			to = cfr;\n"
			+ "			fr = cto;\n"
			+ "		}\n"
			+ "		int[] ret = new int[(to - fr) + 1];\n"
			+ "		int index = 0;\n"
			+ "		for (int i = fr; i < to + 1; i++) {\n"
			+ "			ret[index] = i;\n"
			+ "			index++;\n"
			+ "		}\n"
			+ "		return ret;\n"
			+ "	}";
	private String parseForEach(String in) {
		in = in.replace(Compiler.instruc[15] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		in = in.replace(" trong ", ":").replace(" ", "");
		return "for(var " + in + ")";
	}
	
	private String parseFor(String in) {
		in = in.replace(Compiler.instruc[16] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		in = in.replace("chạy", ";").replace("khi", ";");
		String node[] = in.split("\\s+");
		String type = "vodinh";
		String com = node[0];
		type = node[0].replace("*", "");
		if (!this.dtyp.containsKey(type)) 
			type = "vodinh";
		in = in.replace(com, "");
		String rtp = this.dtyp.get(type);
		return "for (" + rtp + "" + in + ")";
	}
	
	private String parseArrayDeclare(String in) {
		in = in.replace(Compiler.instruc[14] + " ", "");
		String node[] = in.split("\\s+");
		// tao_mang [1] *stn a
		String type = "vodinh";
		type = node[1].replace("*", "");
		if (!this.dtyp.containsKey(type)) 
			type = "vodinh";
		String rtp = this.dtyp.get(type);
		if (rtp.equalsIgnoreCase("var")) rtp = "Object";
		String arn = new String(node[0]).replaceAll("[\\d.]", "");
		return rtp + arn + " " + node[2] + " = new " + rtp + node[0] + ";" ;
	}
	
	private String parseVarDeclare(String in) {
		in = in.replace(Compiler.instruc[1] + " ", "");
		String node[] = in.split("\\s+");
		// -1    0  1  2  
		// Parse kieu du lieu
		String ptr;
		if (in.contains("là")) 
			ptr = in.split("\\s+là\\s+")[1];
		else ptr = in.split("\\s+=\\s+")[1];
		String type = "vodinh";
		boolean isCustom = false;
		if (node[0].toCharArray()[0] == '*' || node[0].toCharArray()[0] == '$') {
			type = node[0].replace("*", "");
			String rtp = this.dtyp.get(type);
			if (type.contains("$")) {
				rtp = type.replace("$", "");
				isCustom = true;
			}
			if (!this.dtyp.containsKey(type)) 
				type = "vodinh";
			if (!isCustom) rtp = this.dtyp.get(type);
			String nd2[] = in.replace("*" + type, "")
				.split("\\s+");
			// Parse gia tri bien
			if (nd2[0].equalsIgnoreCase("là")
			|| node[0].equalsIgnoreCase("=")) {
				return rtp + " " + node[1] + " = " + ptr + ";";
			} return rtp + " " + node[1] + " = " + ptr + ";";
		} else {
			String nd2[] = in.split("\\s+");
			// Parse gia tri bien
			if (nd2[0].equalsIgnoreCase("là")
			|| node[0].equalsIgnoreCase("=")) {
				return  "Object " + nd2[0] + " = " + ptr + ";";
			} return "Object " + nd2[0] + " = " + ptr + ";";
		}
	}
	
	int findArgPlacement(char[] t, char m) {
		for (int i = 0 ; i < t.length; i++) {
			if (t[i] == m) 
			return i;
		}
		return -1;
	}
	
	String appendFromIndexOf(char[] t, int ind) {
		StringBuilder sb = new StringBuilder();
		for (int i = ind; i < t.length; i++) {
			sb.append(t[i]);
		}
		return sb.toString();
	}
	
	private String parseConditionIf(String in) {
		in = in.replace(Compiler.instruc[5] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		return "if(" + in + ")";
	}
	
	private String parseConditionElseIf(String in) {
		in = in.replace(Compiler.instruc[21] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		return "else if(" + in + ")";
	}
	
	private String parseWhileLoop(String in) {
		in = in.replace(Compiler.instruc[8] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		return "while(" + in + ")";
	}
	
	private String parseConditionElse(String in) {
		return "else";
	}
	
	private String parseWaitForInput(String udef, String in) {
		in = in.replace(Compiler.instruc[17] + " ", "");
		in = in.replace(Compiler.instruc[18], "=");
		String node[] = in.split("\\s+");
		String type = "vodinh";
		String attachTo = "";
		if (node[0].contains("*")) 
			type = node[0].replace("*", "");
		if (!this.dtyp.containsKey(type)) 
			type = "vodinh";
		String rtp = this.dtyp.get(type);
		if (node[1].equalsIgnoreCase("=")) {
			attachTo = node[2] + "=";
			if (node[2].charAt(0) == '*') 
			attachTo = rtp + " " + attachTo.replace("*", "");
		}
		if (rtp.equalsIgnoreCase("var") || rtp.equalsIgnoreCase("String")) rtp = "Line";
		String nxt = rtp == "" ? "" : rtp.substring(0, 1).toUpperCase() 
			+ rtp.replace(rtp.substring(0, 1), "");
		return attachTo + udef + "_java_util_Scanner_nativeInterface.next" 
			+ nxt + "();";
	}
	
	private String parseMethodFunction(String udef, List<String> method) {
		String header = method.get(0);
		header = header.replace(instruc[19] + " ", "");
		String type = "void";
		String funcReturn = "void";
		// test(*stn a,*stn b){
		String[] headerNodes = header.split("\\s+");
		if (headerNodes[0].toCharArray()[0] == '*' 
			|| headerNodes[0].toCharArray()[0] == '$') {
			type = headerNodes[0].replaceFirst("\\*", "");
			if (!this.dtyp.containsKey(type)) 
				type = "void";
			if (!type.strip().equalsIgnoreCase("void"))
				funcReturn = this.dtyp.get(type);
			header = header.replaceFirst("\\" + headerNodes[0] + " ", funcReturn + " ");
			header = "public static " + header;
			if (headerNodes[0].toCharArray()[0] == '$') {
				header = header.replace("void", headerNodes[0].replace("$", ""));
				type = headerNodes[0].replace("$", "");
			}
		} else header = "public static void " + header;
		String methodBody = "";
		for (int i = 3; i < header.split("\\s+").length; i++) 
			methodBody += header.split("\\s+")[i] + (i >= header.split("\\s+").length - 1 ? "" : " ");
		String[] bodyNode = methodBody.split("\\s+");
		methodBody = "";
		for (String s1 : bodyNode) {
			methodBody += s1 + " ";
		}
		methodBody = methodBody.replace("$", "").replace("", "");
		for (String k : this.dtyp.keySet()) {
			if (methodBody.contains("*" + k + " "))
			methodBody = methodBody.replace("*" + k + " ", 
				this.dtyp.get(k) + " ");
			else if (methodBody.contains("*" + k + "[]"))
			methodBody = methodBody.replace("*" + k + "[]", 
				this.dtyp.get(k) + "[]");
		}
		String finalHeader = "";
		for (int i = 0; i < 3; i++) 
			finalHeader += header.split("\\s+")[i] + (i >= header.split("\\s+").length - 1 ? "" : " ");
		finalHeader += methodBody;
		//s.append(header);
		this.out(finalHeader);
		List<String> actualBody = method.subList(1, method.size() - 1);
		StringBuilder func = new StringBuilder();
		final String finalType = type;
		actualBody.forEach(a -> {
			String instr = a;
			instr = instr.replace("	", "");		
			String node[] = instr.split("\\s+");	
			this.parse(node, instr, func, udef);
			if (node[0].strip().equalsIgnoreCase(instruc[20]) && 
				!finalType.strip().equalsIgnoreCase("void")) {
				func.append(instr.replace(instruc[20] + " ", "return ") + ";");
			}
			if (instr.toCharArray()[0] == '{'
				|| instr.toCharArray()[instr.toCharArray().length - 1] == '{') 
				func.append("{");
			if (instr.toCharArray()[0] == '}') 
				func.append("}");
		});
		return finalHeader + func.toString() + "}";
	}
	
	private String[] importLibraries(String libname) {
		List<String> libInternal = new ArrayList<>();
		List<String> libImports = new ArrayList<>();	
		if (!libname.split("\\.")[1].strip().equalsIgnoreCase("lyb")) 
			return new String[] {"",""};
		File sf = new File(Main.VIXT_PATH + "libs/" + libname);
		if (!sf.exists()) 
			return new String[] {"",""};
		String[] buffer = new String[0];
		try {
			buffer = FileLoader.bufferReader(Main.VIXT_PATH + "libs/" + libname);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = Compiler.cleanComments(buffer);
		for (int i = 0 ; i < buffer.length; i++) {
			String buf = buffer[i];
			if (buf.charAt(0) == '$') {
				if (buf.contains("$Imports(")) {
					libImports.addAll(Arrays.asList(buf.replace("$Imports(", "")
						.replace(")", "")
						.replace("\"", "")
						.split(","))
					);
					continue;
				}
			}
			libInternal.add(buf.replace("%lname%", libname.split("\\.")[0]));
		}
		StringBuilder appender = new StringBuilder();
		libInternal.forEach(lib -> appender.append(lib));
		StringBuilder importAppender = new StringBuilder();
		libImports.forEach(lib -> importAppender.append("import " + lib + ";"));
		return new String[] { importAppender.toString(), appender.toString() };
	}
	
	static int random(int min, int max) {
		if (min < 0) min = 0;
		if (max < 0) max = 0;
		return new Random().nextInt((max - min) + 1) + min;
	}
	static String randString(int targetStringLength) {
		int leftLimit = 97;
		int rightLimit = 122;
		var random = new Random();
		var generatedString = random.ints(leftLimit, rightLimit + 1)
			.limit(targetStringLength)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
		return generatedString;
	}
	private static String[] cleanComments(String[] a) {
		List<String> regx = new ArrayList<>();
		for (int i = 0 ; i < a.length ; i++) {
			// Xoa het comments
			a[i] = a[i]
				.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
			if (!a[i].isBlank()) regx.add(a[i]);
		}
		return regx.toArray(new String[] {});
	}
	private String interpret(long start) throws Exception {
		UUID runtimeuuid = UUID.randomUUID();
		String writable = Main.VIXT_PATH;
		File fold = new File(writable + "bins/" + runtimeuuid.toString());
		if (!fold.exists()) fold.mkdirs();
		this.out(true, "Dang bien dich ma nguon...");
		var t = this.pgrn + ".java";
		File f = new File(fold, t);
		this.out(f.toString());
		if (!f.exists()) {
			f.createNewFile();
			try (OutputStreamWriter writer =
		             new OutputStreamWriter(new FileOutputStream(fold.toString() + File.separator + t), StandardCharsets.UTF_8)) {
				writer.write(this.compiled);
				writer.close();
			} catch (Exception e) {
				throw e;
			}
			int o1 = this.runProcess("javac " + f.toString(), false);
			if (o1 != 0) {
				this.out(true, "[!] Khong thanh cong trong viec bien dich ma nguon! Hay kiem tra lai code cua ban!");
				return null;
			}
			this.out("Thanh cong tao file Jar!");
			this.out("Dang build file JAR...");
			new File(fold.toString() + File.separator + t).delete();
			StringBuilder classes = new StringBuilder();
			for (File fl : fold.listFiles()) {
				if (fl.getName().contains(".class") && fl.getName().contains(this.pgrn))
				classes.append(fl.getName() + " ");
			}
			this.out("Dang tao MANIFEST cho JAR...");
			int o = this.runProcessWithDir(fold, "jar cvfe original$_" + this.pgrn.toLowerCase() + ".jar " + this.pgrn + " " + classes);
			if (o != 0) {
				this.out(true, "[!] Khong thanh cong trong viec khoi tao MANIFEST va build jar! Vui long lien he developer!");
				return null;
			}
			this.out(true, "Thanh cong tao file Jar!");
			return fold.getPath() + File.separator + "original$_" + this.pgrn.toLowerCase() + ".jar";
		}
		return null;
	}
	String normalize(String s) {
		int n = -1;
		for (int i = 0 ; i < s.toCharArray().length; i++) {
			if (s.toCharArray()[i] != ' ' &&
				s.toCharArray()[i] != '	') {
				n = i;
				break;
			}
		}
		if (n < 0) return s;
		return s.substring(n);
	}
	void printLines(String cmd, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(
			new InputStreamReader(ins)
		);
		while ((line = in.readLine()) != null) {
			this.out(cmd + line);
		}
	}
	int runProcess(String command, boolean print) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
		if (print) this.printLines("[NI] ", pro.getInputStream());
		pro.waitFor();
		return pro.exitValue();
	}
	int runProcessWithDir(File dir, String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command, null, dir);
		this.printLines("[NI] ", pro.getInputStream());
		pro.waitFor();
		return pro.exitValue();
	}
}
