package sai.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import sai.android.Application;
import sai.android.Method;
import sai.android.Class;
import sai.android.Instruction;
import sai.android.InstructionType;

public class ControlFlowGraph {
	public static void generate(Application a) {
		for (Class c : a.getClasses()) {
			for (Method m : c.getMethods()) {
				generate(m);
			}
		}
	}
	
	public static DirectedGraph<String, DefaultEdge> generate(Method m) {
		DirectedGraph<String, DefaultEdge> cfg = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		List<Instruction> instructions = m.getInstructions();
		Map<Instruction,String> instrToStr = new HashMap<Instruction,String>();
		
		//Sequence
		int index = 0;
		for (Instruction i : instructions) {
			if (index == 0) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.addVertex(vertexLabel);
				instrToStr.put(i, vertexLabel);
			} else {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.addVertex(vertexLabel);
				instrToStr.put(i, vertexLabel);
				cfg.addEdge(instrToStr.get(instructions.get(index-1)), vertexLabel);
			}
			index++;
		}
		
		//Identify goto labels
		Map<String, Instruction> gotoLabelToInstr = new HashMap<String,Instruction>();
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.LABEL) {
				String gotoLabel = i.toString().replace(":","");
				gotoLabelToInstr.put(gotoLabel, i);
			}
			index++;
		}
		
		
		//Remove edges from return to the next, and add edge to the ".end method"
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.RETURN) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.removeEdge(vertexLabel, instrToStr.get(instructions.get(index+1)));
				cfg.addEdge(vertexLabel, instrToStr.get(instructions.get(instructions.size()-1))); //return -> end method
			}
			index++;
		}
		
		//Remove the edge from a goto to the next instructions and add edge to goto label
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.GOTO) {
				//Remove edge from next instruction
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.removeEdge(vertexLabel, instrToStr.get(instructions.get(index+1)));
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of if
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.IF) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of catch
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.CATCH) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String[] w = i.toString().split(":");
				String gotoLabel = w[w.length-1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of switch and its cases
		index = 0;
		for (Instruction i : instructions) {
			//Add edge from the switch command to the label before switch cases
			if (i.getType() == InstructionType.SWITCH) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			
			//Add edges from switch cases to the label
			if (i.getType() == InstructionType.OTHER && (i.toString().contains(":sswitch") || i.toString().contains(":pswitch"))) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(new IntegerComponentNameProvider(), new StringComponentNameProvider<String>(), null);
		try {
			String fileName = (m.getName()).replaceAll("[^\\w\\s]","");
			//TODO Better file names/emplacement
			exporter.exportGraph(cfg,  new FileWriter("./tmp/"+fileName+".dot"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cfg;
	}
	
	public static DirectedGraph<String, DefaultEdge> generateWithOriginalNames(Method m) {
		DirectedGraph<String, DefaultEdge> cfg = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		List<Instruction> instructions = m.getInstructions();
		Map<Instruction,String> instrToStr = new HashMap<Instruction,String>();
		
		//Sequence
		int index = 0;
		for (Instruction i : instructions) {
			if (index == 0) {
				String vertexLabel = index+":"+i.toString();
				cfg.addVertex(vertexLabel);
				instrToStr.put(i, vertexLabel);
			} else {
				String vertexLabel = index+":"+i.toString();
				cfg.addVertex(vertexLabel);
				instrToStr.put(i, vertexLabel);
				cfg.addEdge(instrToStr.get(instructions.get(index-1)), vertexLabel);
			}
			index++;
		}
		
		//Identify goto labels
		Map<String, Instruction> gotoLabelToInstr = new HashMap<String,Instruction>();
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.LABEL) {
				String gotoLabel = i.toString().replace(":","");
				gotoLabelToInstr.put(gotoLabel, i);
			}
			index++;
		}
		
		
		//Remove edges from return to the next, and add edge to the ".end method"
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.RETURN) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.removeEdge(vertexLabel, instrToStr.get(instructions.get(index+1)));
				cfg.addEdge(vertexLabel, instrToStr.get(instructions.get(instructions.size()-1))); //return -> end method
			}
			index++;
		}
		
		//Remove the edge from a goto to the next instructions and add edge to goto label
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.GOTO) {
				//Remove edge from next instruction
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				cfg.removeEdge(vertexLabel, instrToStr.get(instructions.get(index+1)));
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of if
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.IF) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of catch
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.CATCH) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String[] w = i.toString().split(":");
				String gotoLabel = w[w.length-1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of switch and its cases
		index = 0;
		for (Instruction i : instructions) {
			//Add edge from the switch command to the label before switch cases
			if (i.getType() == InstructionType.SWITCH) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			
			//Add edges from switch cases to the label
			if (i.getType() == InstructionType.OTHER && (i.toString().contains(":sswitch") || i.toString().contains(":pswitch"))) {
				String vertexLabel = index+":"+i.toString().replaceAll("[^\\w\\s]","");
				
				//Add edge to label
				String gotoLabel = i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(new IntegerComponentNameProvider(), new StringComponentNameProvider<String>(), null);
		try {
			String fileName = (m.getName()).replaceAll("[^\\w\\s]","");
			//TODO Better file names/emplacement
			exporter.exportGraph(cfg,  new FileWriter("./tmp/"+fileName+".dot"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cfg;
	}
}
