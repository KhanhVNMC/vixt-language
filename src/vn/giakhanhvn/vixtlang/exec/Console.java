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
#lang <en>

#include <std.l>
#import <java.util.Arrays>

#define *int width = 140
#define *int height = 34
#define *float iSpeed = 0.8f
	
#define *float A = 0.0f
#define *float B = 0.0f
#define *float C = 0.0f
#define *float hOffset = 0.0f

#define *float cwidth = 20
#define *float[] zBuffer = new float[width * height]
#define *float[] buffer = new float[width * height]
#define *char background = ' '
#define *int dist = 100
#define *float K1 = 40
#define *float x = 0.0f
#define *float y = 0.0f
#define *float z = 0.0f
#define *float ooz = 0.0f
#define *int xp = 0
#define *int yp = 0
#define *int idx = 0

start()

fun *void start() {
	printf "\033[H\033[2J"  
	set *long lastFrame = std.time()
	set *long touch = std.time() 
	set *long secPassed = 1
	set *long frames = 0
		
	while true then {
		Arrays.fill(buffer, background)
		Arrays.fill(zBuffer, 0)
		cwidth = 20
		hOffset = -2 * cwidth
		set *float[][] szs = new float[6][3]
			
		for *float cX = -cwidth; cX < cwidth; cX += iSpeed {
			for *float cY = -cwidth; cY < cwidth; cY += iSpeed {
				szs[0] = clsurf(cX, cY, -cwidth, '$')
				szs[1] = clsurf(cwidth, cY, cX, 'a')
				szs[2] = clsurf(-cwidth, cY, -cX, '!')
				szs[3] = clsurf(-cX, cY, cwidth, '|')
				szs[4] = clsurf(cX, -cwidth, -cY, 'i')
				szs[5] = clsurf(cX, cwidth, cY, '*')
			}
		}
		printf "\033[H\033[2J"  
		foreach k in range(0, (width * height) - 1) {
			printf (char) (k % width != 0 ? buffer[k] : 10)
		}
		frames++
		printf "\n\nFrame rendered in: %dms", (std.time() - lastFrame)
		if std.time() - touch > 1000 {
			touch = std.time()
			secPassed++
		}
		printf "\nAverage FPS: %dfps", std.roundComma((double)frames/(double)secPassed)
		lastFrame = std.time()
		printf "\n"
		
		foreach i in range(0, szs.length-1) {
			set *float[] side = szs[i]
			printf "\nSide #%d: x=%d y=%d z=%d", i,side[0],side[1],side[2]
		}
		await 0.025s	
		A += 0.05
		B += 0.05
		C += 0.01
	}
}

fun *float calX(*float i, *float j, *float k) {
	return j * std.sinFloat(A) * std.sinFloat(B) * std.cosFloat(C) - k * std.cosFloat(A) * std.sinFloat(B) * std.cosFloat(C) + j * std.cosFloat(A) * std.sinFloat(C) + k * std.sinFloat(A) * std.sinFloat(C) + i * std.cosFloat(B) * std.cosFloat(C)
}

fun *float calY(*float i, *float j, *float k) {
	return j * std.cosFloat(A) * std.cosFloat(C) + k * std.sinFloat(A) * std.cosFloat(C) - j * std.sinFloat(A) * std.sinFloat(B) * std.sinFloat(C) + k * std.cosFloat(A) * std.sinFloat(B) * std.sinFloat(C) - i * std.cosFloat(B) * std.sinFloat(C)
}

fun *float calZ(*float i, *float j, *float k) {
	return k * std.cosFloat(A) * std.cosFloat(B) - j * std.sinFloat(A) * std.cosFloat(B) + i * std.sinFloat(B)
}

fun *float[] clsurf(*float cX, *float cY, *float cubeZ, *int ch) {
	x = calX(cX, cY, cubeZ)
	y = calY(cX, cY, cubeZ)
	z = calZ(cX, cY, cubeZ) + dist
	ooz = 1 / z
	xp = (int) (width / 2 + hOffset + K1 * ooz * x * 2)
	yp = (int) (height / 2 + K1 * ooz * y)
	idx = xp + yp * width
	if idx >= 0 && idx < width * height {
		if ooz > zBuffer[idx] {
			zBuffer[idx] = ooz
			buffer[idx] = ch
		}
	}
	return new float[] {x,y,z}
}

