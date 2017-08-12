package sai.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import sai.android.Application;
import sai.android.Class;
import sai.android.Instruction;
import sai.android.InstructionType;
import sai.android.Method;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class CallGraph {
	private static DirectedGraph<String, DefaultEdge> cg;
	
	public static void generate(Application a) {
		cg = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		cg.addVertex("begin");
		
		List<Method> entryPoints = new LinkedList<Method>();
		List<String> methodsExplored = new LinkedList<String>();
		Stack<Method> methodsToExplore = new Stack<Method>();
		for (Class c : a.getClasses()) {
			if (c.getSuperName().equals("Landroid/app/Activity") && !c.getName().startsWith("Landroid")) {
				Method m = c.getMethod("onCreate(Landroid/os/Bundle;)V");
				entryPoints.add(m);
				String entryPointName = c.getName() + ";->" + m.getName();
				cg.addVertex(entryPointName);
				cg.addEdge("begin", entryPointName);
				methodsToExplore.add(m);
			}
		}
		
		while (!methodsToExplore.isEmpty()) {
			Method m = methodsToExplore.pop();
			
			for (Instruction i : m.getInstructions()) {
				if (i.getType() == InstructionType.INVOKE && (!i.getInvokedClassName().startsWith("Landroid") && !i.getInvokedClassName().startsWith("Ljava"))) {
					String nameOfMethodInvoking = m.getParentClass().getName()+";->"+m.getName();
					String nameOfMethodInvoked = i.getInvokedClassName()+";->"+i.getInvokedMethodName();
					
					System.out.println(nameOfMethodInvoking + " CALLS " + nameOfMethodInvoked);
				
					cg.addVertex(nameOfMethodInvoking);
					
					Class classOfMethodInvoked = a.getClass(i.getInvokedClassName());
					Method methodInvoked = classOfMethodInvoked.getMethod(i.getInvokedMethodName());
					if (methodInvoked != null) {
						methodsToExplore.push(methodInvoked);
						cg.addVertex(nameOfMethodInvoked);
						cg.addEdge(nameOfMethodInvoking, nameOfMethodInvoked);
					}
				}
			}
		}

		exportGraphToDotFile(cg, "./tmp/call-graph.dot");
	}

	public static DirectedGraph<String, DefaultEdge> getGeneratedCallGraph() {
		return cg;
	}

	private static void exportGraphToDotFile(DirectedGraph<String, DefaultEdge> graph, String path) {
		DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(new IntegerComponentNameProvider(), new StringComponentNameProvider<String>(), null);
		try {
			exporter.exportGraph(graph,  new FileWriter(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
