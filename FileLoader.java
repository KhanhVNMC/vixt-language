package vn.giakhanhvn.jassembly.filehandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileLoader {
	public static String[] bufferReader(String fileIndex) throws FileNotFoundException, IOException {
		ArrayList<String> result = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileIndex))) {
			while (br.ready()) {
				result.add(br.readLine());
			}
		}
		return result.toArray(new String[]{});
	}
	public static void write(String filename, String[] content) throws IOException {
		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter(filename));
		for (int i = 0; i < content.length; i++) {
			outputWriter.write(content[i]);
			outputWriter.newLine();
		}
		outputWriter.flush();  
		outputWriter.close();  
	}
}
