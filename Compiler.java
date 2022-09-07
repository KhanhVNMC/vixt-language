package vn.giakhanhvn.jassembly.filehandling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Compiler {
	private String[] src;
	private String[] fls;
	private String[] nod;
	private String pgrn;
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
		"sửa",	// MODIFY 10
		"phá_lặp",	// BREAK 11
		"quay_lại",	// RETURN 12
		"nhảy_qua",	// CONTINUE 13
		"tạo_mảng", // DEFINE ARRAY 14
		"cho_mỗi",	// FOR EACH 15
		"cho"	,// FOR 16
		"nhập",	// INPUT 17
		"vào",	// TO 18
	};
	protected static final String FOOTER = "}}";
	protected static final String HEADER = 
		"public class %regx {public static void main(String[] args) throws Exception {";
	
	Map<String, String> dtyp = new HashMap<>();
	
	private List<String> regx = new ArrayList<>();
	public Compiler(String[] file, String[] flags) {
		this.src = file;
		this.fls = flags;
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
		// FLOAT
		this.dtyp.put("st", "float");
		this.dtyp.put("sothuc", "float");
		this.dtyp.put("float", "float");
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
	public void compile() {
		if (src[0].contains("#ten ")) 
			pgrn = src[0].replace("#ten ", "");
		else throw new RuntimeException();
		this.init();
	}
	
	public void init() {
		this.cleanFile();
		List<String> imports = new ArrayList<>();
		for (int i = 0 ; i < this.regx.size(); i++) {
			String instr = this.regx.get(i);
			if (instr.charAt(0) != '#') continue;
			String[] str = instr.split("\\s+");
			this.out(str[0]);
			if (str[0].strip().equalsIgnoreCase("#thuvien")) {
				imports.add(instr.replace("#thuvien", "")
					.replace("<", "").replace(">", "")
					.replace(" ", "")
				);
			}
		}
		List<String[]> importedCache = new ArrayList<>();
		for (String i : imports) {
			importedCache.add(this.importLibraries(i));
		}
		this.nod = new String[regx.size()]; 
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
		boolean java = false;
		for (int i = 0 ; i < this.regx.size(); i++) {
			String instr = this.regx.get(i);
			instr = instr.replace("	", "");		
			String node[] = instr.split("\\s+");
			if (node[0].strip().equalsIgnoreCase("$java") && instr.contains("{")) {
				java = true;
				continue;
			}
			if (instr.charAt(0) == '}' && node[0].strip().equalsIgnoreCase("}$end") && java) {
				java = false;
				continue;
			}
			if (java) {
				sb.append(instr);
				continue;
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
			if (instr.toCharArray()[0] == '{'
				|| instr.toCharArray()[instr.toCharArray().length - 1] == '{') 
			sb.append("{");
			if (instr.toCharArray()[0] == '}') 
			sb.append("}");
		}
		sb.append("}");
		for (String[] imp : importedCache) {
			sb.append(imp[1]);
		}
		sb.append("}");
		nod[0] = sb.toString().replace("	", "");
	}
	
	private void out(String string) {
		System.out.println(string);
	}

	public void deb() {
		out("");
		for (String s : this.nod) {
			System.out.println(s);
		}
	}
	private void cleanFile() {
		for (int i = 0 ; i < this.src.length ; i++) {
			// Xoa het comments
			src[i] = src[i]
				.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
			if (!src[i].isBlank()) regx.add(src[i]);
		}
	}
	public String parsePrintLnFunction(String in) {
		in = in.replaceFirst("inxd ", "");
		return "System.out.println(" + in + ");";
	}
	
	public String parsePrintFunction(String in) {
		in = in.replaceFirst("in ", "");
		return "System.out.print(" + in + ");";
	}
	
	public String parseWaitFunction(String in) {
		in = in.replace(Compiler.instruc[4] + " ", "");
		in = in.replace(" ", "");
		in = in.replace("s", "");
		try {
			return "Thread.sleep(" + Math.round(Double.parseDouble(in) * 1000L) + "L);";
		} catch (Exception e) {
			return "Thread.sleep(0);";
		}
	}
	
	public String parseNewLineFunction() {
		return "System.out.println();";
	}
	
	public String parseForEach(String in) {
		in = in.replace(Compiler.instruc[15] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		in = in.replace(" trong ", ":").replace(" ", "");
		return "for(var " + in + ")";
	}
	
	public String parseFor(String in) {
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
	
	public String parseArrayDeclare(String in) {
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
	public String parseVarDeclare(String in) {
		in = in.replace(Compiler.instruc[1] + " ", "");
		String node[] = in.split("\\s+");
		// dat *stn x la 10
		// -1    0  1  2  
		// Parse kieu du lieu
		String ptr = in.split("\\s+là\\s+")[1];
		String type = "vodinh";
		if (node[0].toCharArray()[0] == '*') {
				type = node[0].replace("*", "");
			if (!this.dtyp.containsKey(type)) 
				type = "vodinh";
			String rtp = this.dtyp.get(type);
	
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
	
	public String parseConditionIf(String in) {
		in = in.replace(Compiler.instruc[5] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		return "if(" + in + ")";
	}
	public String parseWhileLoop(String in) {
		in = in.replace(Compiler.instruc[8] + " ", "");
		in = in.replace("{", "");
		in = in.replace(" " + Compiler.instruc[7], "");
		return "while(" + in + ")";
	}
	public String parseConditionElse(String in) {
		return "else";
	}
	public String parseWaitForInput(String udef, String in) {
		in = in.replace(Compiler.instruc[17] + " ", "");
		in = in.replace(Compiler.instruc[18], "=");
		String node[] = in.split("\\s+");
		String type = "vodinh";
		String attachTo = "";
		if (node[0].contains("*")) 
			type = node[0].replace("*", "");
		if (node[1].equalsIgnoreCase("=")) 
			attachTo = node[2] + "=";
		if (!this.dtyp.containsKey(type)) 
			type = "";
		String rtp = this.dtyp.get(type);
		if (rtp.equalsIgnoreCase("var") || rtp.equalsIgnoreCase("String")) rtp = "";
		String nxt = rtp == "" ? "" : rtp.substring(0, 1).toUpperCase() 
			+ rtp.replace(rtp.substring(0, 1), "");
		return attachTo + udef + "_java_util_Scanner_nativeInterface.next" + nxt + "();";
	}
	public String[] importLibraries(String libname) {
		List<String> libInternal = new ArrayList<>();
		List<String> libImports = new ArrayList<>();	
		if (!libname.split("\\.")[1].strip().equalsIgnoreCase("lyb")) 
			return new String[] {"import java.util.Scanner;",""};
		File sf = new File(libname);
		if (!sf.exists()) 
			return new String[] {"import java.util.Scanner;",""};
		String[] buffer = new String[0];
		libImports.add("java.util.Scanner");
		try {
			buffer = FileLoader.bufferReader(libname);
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
}
