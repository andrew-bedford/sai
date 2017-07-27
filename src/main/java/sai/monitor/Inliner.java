package sai.monitor;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import helpers.FileHelper;
import sai.Configuration;
import sai.datastructures.Application;
import sai.datastructures.Class;
import sai.datastructures.Instruction;
import sai.datastructures.InstructionType;
import sai.datastructures.Method;

public class Inliner {
	public static void inline(Application a) {
		taintSources(a);
		checkSinks(a);
		propagateTaints(a);
		updatePc(a);
}

	private static void taintSources(Application a) {
		for (Class c : a.getClasses()) {
			for (Method m : c.getMethods()) {
				m.incrementNumberOfLocals();
				int lastLocal = m.getNumberOfLocals()-1;
				
				List<Instruction> instructions = m.getInstructions();
				List<Instruction> newInstructions = new LinkedList<Instruction>();
				int index = 0;
				Boolean taintNextResult = false;
				for (Instruction i : instructions) {
					newInstructions.add(i);
					
					if (i.getType() == InstructionType.INVOKE && i.getLine().contains("Landroid/telephony/TelephonyManager;->getDeviceId")) {
						taintNextResult = true;
					}
					
					if (i.getType() == InstructionType.MOVE_RESULT && taintNextResult) {
						taintNextResult = false;
						List<String> registersToTaint = i.getRegistersUsed();
						for (String r : registersToTaint) {
							Instruction backupV0 = new Instruction("move/from16 v"+lastLocal+", v0");
							Instruction setV0 = new Instruction("move/from16 v0, "+r);
							Instruction taint = new Instruction("invoke-static {v0}, Lsai/Monitor;->taint(Ljava/lang/Object;)V");
							Instruction set2Temp = new Instruction("move/from16 "+r+", v0");
							Instruction restoreV0 = new Instruction("move/from16 v0, v"+lastLocal);
							newInstructions.add(backupV0);
							newInstructions.add(setV0);
							newInstructions.add(taint);
							newInstructions.add(restoreV0);
						}
					}
					index++;
				}
				m.setInstructions(newInstructions);
			}
		}
	}
	
	//TODO Pop PC
	private static void updatePc(Application a) {
		for (Class c : a.getClasses()) {
			for (Method m : c.getMethods()) {
				List<Instruction> instructions = m.getInstructions();
				List<Instruction> newInstructions = new LinkedList<Instruction>();
				int index = 0;
				for (Instruction i : instructions) {
					newInstructions.add(i);
					
					if (i.getType() == InstructionType.IF || i.getType() == InstructionType.SWITCH) {
						String registerOnWhichControlFlowDepends = i.getRegistersUsed().get(0);
						
						Instruction newInstruction = new Instruction("invoke-static {"+registerOnWhichControlFlowDepends+"}, Lsai/Monitor;->pushPC(Ljava/lang/Object;)V");
						newInstructions.add(index, newInstruction);
					}
					index++;
				}
				m.setInstructions(newInstructions);
			}
		}
	}
	
	//TODO Also propagate when arithmetic operations are used or conversions (see Dalvik operations)
	private static void propagateTaints(Application a) {
		for (Class c : a.getClasses()) {
			for (Method m : c.getMethods()) {
				m.incrementNumberOfLocals();
				int lastLocal = m.getNumberOfLocals()-1;
				
				List<Instruction> instructions = m.getInstructions();
				List<Instruction> newInstructions = new LinkedList<Instruction>();
				int index = 0;
				Boolean taintNextResult = false;
				List<String> registersToPropagate = new LinkedList<String>();
				for (Instruction i : instructions) {
					newInstructions.add(i);
					
					//TODO Instead of Landroid/Ljava, verify if the method is defined in the application
					if (i.getType() == InstructionType.INVOKE && i.getLine().contains("Landroid") || i.getLine().contains("Ljava")) {
						taintNextResult = true;
						registersToPropagate = i.getRegistersUsed();
					}
					
					if (i.getType() == InstructionType.MOVE_RESULT && taintNextResult) {
						
						List<String> registersToTaint = i.getRegistersUsed();
						for (String fromRegister : registersToPropagate) {
							for (String toRegister : registersToTaint) {
								if (!fromRegister.equals(toRegister)) {
									String tempRegister = "v0";
									if (fromRegister.equals("v0")) {
										tempRegister = "v1";
									}
									Instruction backupTemp = new Instruction("move/from16 v"+lastLocal+", "+tempRegister);
									Instruction setTemp = new Instruction("move/from16 "+tempRegister+", "+toRegister);
									Instruction propagateTaint = new Instruction("invoke-static {"+fromRegister+", "+tempRegister+"}, Lsai/Monitor;->propagateTaint(Ljava/lang/Object;Ljava/lang/Object;)V");
									Instruction set2Temp = new Instruction("move/from16 "+toRegister+", "+tempRegister);
									Instruction restoreTemp = new Instruction("move/from16 "+tempRegister+", v"+lastLocal);
									newInstructions.add(backupTemp);
									newInstructions.add(setTemp);
									newInstructions.add(propagateTaint);
									newInstructions.add(set2Temp);
									newInstructions.add(restoreTemp);
									
								}
							}
						}
						taintNextResult = false;
						registersToPropagate = new LinkedList<String>();
					}
					index++;
				}
				m.setInstructions(newInstructions);
			}
		}
	}
	
	private static void checkSinks(Application a) {
		for (Class c : a.getClasses()) {
			for (Method m : c.getMethods()) {
				List<Instruction> instructions = m.getInstructions();
				List<Instruction> newInstructions = new LinkedList<Instruction>();

				int index = 0;
				for (Instruction i : instructions) {
					newInstructions.add(i);
					
					if (i.getType() == InstructionType.INVOKE && i.getLine().contains("Landroid/telephony/SmsManager;->sendTextMessage")) {
						List<String> registersToCheck = i.getRegistersUsed();
						for (String r : registersToCheck) {
							Instruction newInstruction = new Instruction("invoke-static {"+r+"}, Lsai/Monitor;->check(Ljava/lang/Object;)V");
							newInstructions.add(index, newInstruction);
						}
					}
					index++;
				}
				m.setInstructions(newInstructions);

			}
		}
	}
}
