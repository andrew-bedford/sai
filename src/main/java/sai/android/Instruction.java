package sai.android;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Instruction {
	private String _line;
	private InstructionType _type;
	
	public Instruction(String line) {
		_line = line;
		_type = extractInstructionType(line);
	}

	private InstructionType extractInstructionType(String line) {
		InstructionType type = InstructionType.OTHER; //Default instruction type

		if (line.startsWith(".class")) { type = InstructionType.CLASS; }
		else if (line.startsWith(".super")) { type = InstructionType.SUPER; }
		else if (line.startsWith(".source")) { type = InstructionType.SOURCE; }
		else if (line.startsWith("#")) { type = InstructionType.COMMENT; }
		else if (line.startsWith(".field")) { type = InstructionType.FIELD; }
		else if (line.startsWith("return")) { type = InstructionType.RETURN; }
		else if (line.startsWith(".method")) { type = InstructionType.METHOD_BEGIN; }
		else if (line.startsWith(".end method")) { type = InstructionType.METHOD_END; }
		else if (line.startsWith("goto")) { type = InstructionType.GOTO; }
		else if (line.startsWith(":")) { type = InstructionType.LABEL; }
		else if (line.startsWith("new")) { type = InstructionType.NEW; }
		else if (line.startsWith("move-result")) { type = InstructionType.MOVE_RESULT; }
		else if (line.startsWith("invoke")) { type = InstructionType.INVOKE; }
		else if (line.startsWith(".catch")) { type = InstructionType.CATCH; }
		else if (line.startsWith("const")) { type = InstructionType.CONST; }
		else if (line.startsWith("aget") || line.startsWith("iget") || line.startsWith("sget")) { type = InstructionType.GET; }
		else if (line.startsWith("aput") || line.startsWith("iput") || line.startsWith("sput")) { type = InstructionType.PUT; }
		else if (line.startsWith("if")) { type = InstructionType.IF; }
		else if (line.startsWith("sparse-switch") || line.startsWith("packed-switch")) { type = InstructionType.SWITCH; }
		else if (line.startsWith(".locals")) { type = InstructionType.LOCALS; }

		return type;
	}
	
	public String getLine() {
		return _line;
	}
	
	public void setLine(String l) {
		_line = l;
		extractInstructionType(l);
	}

	public InstructionType getType() {
		return _type;
	}
	
	public List<String> getRegistersUsed() {
		Pattern pattern = Pattern.compile("(\\{|\\s)((v|p)\\d*)");
		Matcher matcher = pattern.matcher(_line);
		List<String> registers = new ArrayList<String>();
		while (matcher.find()) {
			registers.add(matcher.group(2));
		}
		
		return registers;
	}
	
	public String getInvokedClassName() {
		if (_type == InstructionType.INVOKE) {
			String[] w = _line.split(" ");
			return w[w.length-1].split("\\(")[0].split(";->")[0];
		}
		return "";
	}
	
	public String getInvokedMethodName() {
		if (_type == InstructionType.INVOKE) {
			String[] w = _line.split(" ");
			return w[w.length-1].split("->")[1];
		}
		return "";
	}
	
	public String toString() {
		return _line;
	}
}
