package sai.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Class {

	private File file;
	private String name;
	private String superName;
	private List<Instruction> header;
	private List<Instruction> fields;
	private List<Method> methods;
	
	public Class(File f) throws FileNotFoundException {
		file = f;
		header = new LinkedList<Instruction>();
		fields = new LinkedList<Instruction>();
		methods = new LinkedList<Method>();
		
		Scanner scanner = new Scanner(f);
		scanner.useDelimiter("\n");
		String line;
		List<Instruction> methodInstructions = new LinkedList<Instruction>();
		Boolean insideMethod = false;
		while (scanner.hasNext()) {
			line = scanner.next().trim();
			if (!line.isEmpty()) {
				Instruction i = new Instruction(line);
				if (i.getType() == InstructionType.CLASS) {
					name = extractClassName(line);
					System.out.println("CLASS NAME: '" + name + "'");
				}
				
				if (i.getType() == InstructionType.SUPER) {
					superName = extractSuperName(line);
				}
				
				if (i.getType() == InstructionType.METHOD_BEGIN) {
					insideMethod = true;
				}
								
				if (insideMethod) {
					methodInstructions.add(i);
					
					if (i.getType() == InstructionType.METHOD_END) {
						methods.add(new Method(this, methodInstructions));
						insideMethod = false;
						methodInstructions = new LinkedList<Instruction>();
					}
				}
				else {
					header.add(i);
				}
					
				
			}
		}
		scanner.close();
	}

	private String extractClassName(String line) {
		String[] words = line.split(" ");
		String className = words[words.length-1].replace(";", "");
		return className;
	}
	
	private String extractSuperName(String line) {
		String[] words = line.split(" ");
		String superName = words[words.length-1].replace(";", "");
		return superName;
	}

	/**
	 * 
	 * @return Class name
	 */
	public String getName() {
		return name;
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	/**
	 * 
	 * @param methodName Name of the method to return
	 * @return 
	 */
	public Method getMethod(String methodName) {
		for (Method m : methods) {
			if (m.getName() != null && m.getName().equals(methodName)) {
				return m;
			}
		}
		//TODO Throw exception when method is not in class instead of returning null
		return null;
	}
	
	/**
	 * Writes 
	 * @throws IOException
	 */
	public void saveChanges() throws IOException {
		String lines = "";
		for (Instruction i : header) {
			lines += i.getLine() + "\n";
		}
		for (Method m : methods) {
			List<Instruction> methodInstructions = m.getInstructions();
			for (Instruction i : methodInstructions) {
				lines += i.getLine() + "\n";
			}
		}

		FileWriter fw = new FileWriter(file, false);
		fw.write(lines);
		fw.close();
	}

	public String getSuperName() {
		return superName;
	}

}
