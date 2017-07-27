package sai.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import sai.datastructures.Application;
import sai.datastructures.Class;
import sai.datastructures.Instruction;
import sai.datastructures.InstructionType;
import sai.datastructures.Method;

public class ProcessAlgebra {
	static Application app;
	static Map<Method, Integer> idCounter;
	
	public static void generateCCS(Application a) {
		app = a;
		idCounter = new HashMap<Method, Integer>();
		CallGraph.generate(a);
		DirectedGraph<String, DefaultEdge> cg = CallGraph.getGeneratedCallGraph();
		
		GraphIterator<String, DefaultEdge> iterator =new DepthFirstIterator<String, DefaultEdge>(cg);
        while (iterator.hasNext()) {
            String v = iterator.next();
            
            List<String> targets = new LinkedList<String>();
            for (DefaultEdge e : cg.outgoingEdgesOf(v)) {
	           	String eTarget = cg.getEdgeTarget(e);
	           	targets.add(formatProcessName(eTarget));
	           	
	           	String[] lr = eTarget.split(";->");
	            Class c = app.getClass(lr[0]);
	           	Method m = c.getMethod(lr[1]);
	           	ccsMethod(m);
	        }
            if (!targets.isEmpty()) {
	            String output = String.format("%s = (%s)", formatProcessName(v), String.join(" + ", targets));
	            System.out.println(output);
            }
            
            
	            
        }
    }
	
	private static String formatProcessName(String s) {
		return "P"+s.replaceAll("[^\\w\\s]","");
	}
	
	public static void ccsMethod(Method m) {
		
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
				String gotoLabel = i.toString();
				gotoLabelToInstr.put(gotoLabel, i);
			}
			index++;
		}
		
		
		//Remove edges from return to the next, and add edge to the ".end method"
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.RETURN) {
				String vertexLabel = index+":@:"+i.toString();
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
				String vertexLabel = index+":@:"+i.toString();
				cfg.removeEdge(vertexLabel, instrToStr.get(instructions.get(index+1)));
				
				//Add edge to label
				String gotoLabel = ":"+i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of if
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.IF) {
				String vertexLabel = index+":@:"+i.toString();
				
				//Add edge to label
				String gotoLabel = ":"+i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//Add edge to label of catch
		index = 0;
		for (Instruction i : instructions) {
			if (i.getType() == InstructionType.CATCH) {
				String vertexLabel = index+":@:"+i.toString();
				
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
				String vertexLabel = index+":@:"+i.toString();
				
				//Add edge to label
				String gotoLabel = ":"+i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			
			//Add edges from switch cases to the label
			if (i.getType() == InstructionType.OTHER && (i.toString().contains(":sswitch") || i.toString().contains(":pswitch"))) {
				String vertexLabel = index+":@:"+i.toString();
				
				//Add edge to label
				String gotoLabel = ":"+i.toString().split(":")[1];
				String labelVertexLabel = instrToStr.get(gotoLabelToInstr.get(gotoLabel));
				cfg.addEdge(vertexLabel, labelVertexLabel);
			}
			index++;
		}
		
		//From CFG, generate CCS
		GraphIterator<String, DefaultEdge> iterator =new DepthFirstIterator<String, DefaultEdge>(cfg);
        while (iterator.hasNext()) {
            String v = iterator.next();
            
            List<String> nextVertices = new LinkedList<String>();
            List<String> targets = new LinkedList<String>();
            for (DefaultEdge e : cfg.outgoingEdgesOf(v)) {
            	
            }
        }
            
	}
	
}
