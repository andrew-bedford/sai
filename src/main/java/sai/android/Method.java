package sai.android;

import java.util.List;

public class Method {
	private Class _parentClass;
	private List<Instruction> _instructions;
	private String _name;
	
	public Method(Class parentClass, List<Instruction> inst) {
		_parentClass = parentClass;
		_instructions = inst;

		if (_instructions.size() > 0) {
			Instruction firstInstruction = _instructions.get(0);
			if (firstInstruction.getType() == InstructionType.METHOD_BEGIN) {
				String[] w = firstInstruction.getLine().split(" ");
				_name =  w[w.length-1];
			}
		}
	}
	
	public List<Instruction> getInstructions() {
		return _instructions;
	}
	
	public void setInstructions(List<Instruction> instructions) {
		this._instructions = instructions;
	}

	public String getName() {
		return _name;
	}
	
	public Class getParentClass() {
		return _parentClass;
	}
	
	public int getNumberOfLocals() {
		int locals = 0;
		for (Instruction i: _instructions) {
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
		
		for (Instruction i: _instructions) {
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
