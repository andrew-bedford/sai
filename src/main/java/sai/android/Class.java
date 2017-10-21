package sai.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Class {

	private File _file;
	private String _name;
	private String _superClassName;
	private List<Instruction> _header;
	private List<Instruction> _fields;
	private List<Method> _methods;
	
	public Class(File f) throws FileNotFoundException {
		_file = f;
		_header = new LinkedList<Instruction>();
		_fields = new LinkedList<Instruction>();
		_methods = new LinkedList<Method>();
		
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
					_name = extractClassName(line);
					System.out.println("CLASS NAME: '" + _name + "'");
				}
				
				if (i.getType() == InstructionType.SUPER) {
					_superClassName = extractSuperName(line);
				}
				
				if (i.getType() == InstructionType.METHOD_BEGIN) {
					insideMethod = true;
				}
								
				if (insideMethod) {
					methodInstructions.add(i);
					
					if (i.getType() == InstructionType.METHOD_END) {
						_methods.add(new Method(this, methodInstructions));
						insideMethod = false;
						methodInstructions = new LinkedList<Instruction>();
					}
				}
				else {
					_header.add(i);
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
	 * @return Class _name
	 */
	public String getName() {
		return _name;
	}
	
	public List<Method> getMethods() {
		return _methods;
	}
	
	/**
	 * 
	 * @param methodName Name of the method to return
	 * @return 
	 */
	public Method getMethod(String methodName) {
		for (Method m : _methods) {
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
		for (Instruction i : _header) {
			lines += i.getLine() + "\n";
		}
		for (Method m : _methods) {
			List<Instruction> methodInstructions = m.getInstructions();
			for (Instruction i : methodInstructions) {
				lines += i.getLine() + "\n";
			}
		}

		FileWriter fw = new FileWriter(_file, false);
		fw.write(lines);
		fw.close();
	}

	public String getSuperClassName() {
		return _superClassName;
	}

}
