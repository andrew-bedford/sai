package sai.android;

import java.util.List;

public class Method {
	private Class parentClass;
	private List<Instruction> instructions;
	private String name;
	
	public Method(Class cls, List<Instruction> inst) {
		parentClass = cls;
		instructions = inst;

		if (instructions.size() > 0) {
			Instruction firstInstruction = instructions.get(0);
			if (firstInstruction.getType() == InstructionType.METHOD_BEGIN) {
				String[] w = firstInstruction.getLine().split(" ");
				name =  w[w.length-1];
			}
		}
	}
	
	public List<Instruction> getInstructions() {
		return instructions;
	}
	
	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	public String getName() {
		return name;
	}
	
	public Class getParentClass() {
		return parentClass;
	}
	
	public int getNumberOfLocals() {
		int locals = 0;
		for (Instruction i:instructions) {
			if (i.getType() == InstructionType.LOCALS) {
				locals = Integer.valueOf(i.getLine().replace(".locals ", ""));
				break;
			}
		}
		return locals;
	}
	
	public void setNumberOfLocals(int numberOfLocals) {
		if (numberOfLocals < 0) { 
			//TODO Throw exception 
		}
		
		for (Instruction i:instructions) {
			if (i.getType() == InstructionType.LOCALS) {
				i.setLine(".locals " + String.valueOf(numberOfLocals));
				break;
			}
		}
	}
	
	public void incrementNumberOfLocals() {
		setNumberOfLocals(getNumberOfLocals()+1);
	}
}
