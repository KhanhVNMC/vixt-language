package vn.giakhanhvn.vixtlang.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import vn.giakhanhvn.vixtlang.compiler.Compiler;

public class Console {
	private String cmd;
	private Compiler c;

	public Console(String cmd, Compiler c) {
		this.cmd = cmd;
		this.c = c;
	}
	
	public boolean chetChua(Process p) {
		try {
			p.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}

	public void open() throws Exception {
		if (c == null || (c != null && !c.rfl.absSilent)) {
			System.out.println("[VIXT] Dang khoi chay Vixt IConsole...");
			System.out.println("[VIXT] Dang chay chuong trinh...");
		}
		long milis = System.currentTimeMillis();
		if (!this.c.rfl.nativefeel)
			System.out.println("=======================- ICONSOLE -=======================");
		Process process = Runtime.getRuntime().exec(this.cmd);
		InputStream out = process.getInputStream();
		OutputStream in = process.getOutputStream();
		byte[] bb = new byte[4000];
		while (!this.chetChua(process)) {
			int a = out.available();
			if (a > 0) {
				int n = out.read(bb, 0, Math.min(a, bb.length));
				System.out.println(new String(bb, 0, n));
			}
			int ni = System.in.available();
			if (ni > 0) {
				int n = System.in.read(bb, 0, Math.min(ni, bb.length));
				in.write(bb, 0, n);
				in.flush();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		if (process.exitValue() != 0) {
			InputStream err = process.getErrorStream();
			String line;
			BufferedReader bre = new BufferedReader(new InputStreamReader(err));
			while ((line = bre.readLine()) != null) {
				System.err.println("[ICONSOLE] " + line);
			}
		}
		System.out.println("");
		System.out.println("[ICONSOLE] Exit code: " + process.exitValue());
		System.out.println("[ICONSOLE] Thoi gian thuc hien: " + (System.currentTimeMillis() - milis) + "ms");
	}
}
