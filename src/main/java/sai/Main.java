package sai;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.ApkHelper;
import sai.analysis.CallGraph;
import sai.analysis.ControlFlowGraph;
import sai.analysis.ProcessAlgebra;
import sai.android.Application;
import sai.monitor.Inliner;

public class Main {

	static final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
	
	public static void main(String[] args) {
		parseArguments(args);
		if (parameters.isEmpty() || parameters.containsKey("-help")) {
			System.out.println("Options:");
			System.out.println("-disassemble [file]\t\t Disassembles APK");
			System.out.println("-load [folder]\t\t Loads an already disassembled APK in memory");
			System.out.println("-assemble\t\t Reassembles APK");
			System.out.println("-inline-monitor\t\t Inserts monitor ");
		}
		else if (parameters.containsKey("-disassemble")) {
			String apkPath = parameters.get("-disassemble").get(0);
			File apkFile = new File(apkPath);
			ApkHelper.disassemble(apkFile, Configuration.apktoolPath, Configuration.disassembledApkPath);
		}
		else if (parameters.containsKey("-load")) {
			Application app = new Application(new File(Configuration.disassembledApkPath));
		}
		else if (parameters.containsKey("-assemble")) {
			ApkHelper.assemble(Configuration.apktoolPath, Configuration.disassembledApkPath);
		}
		else if (parameters.containsKey("-inline-monitor")) {
			Application app = new Application(new File(Configuration.disassembledApkPath));
			Inliner.inline(app);
			app.saveChanges();
		}
		else if (parameters.containsKey("-call-graph")) {
			Application app = new Application(new File(Configuration.disassembledApkPath));
			CallGraph.generate(app);
		}
		else if (parameters.containsKey("-control-flow-graph")) {
			Application app = new Application(new File(Configuration.disassembledApkPath));
			ControlFlowGraph.generate(app);
		}
		else if (parameters.containsKey("-process-algebra")) {
			Application app = new Application(new File(Configuration.disassembledApkPath));
			ProcessAlgebra.generateCCS(app);
		}
	}

	private static void parseArguments(String[] args) {
		List<String> options = null;
		for (int i = 0; i < args.length; i++) {
		    final String a = args[i];

		    if (a.charAt(0) == '-') {
		        if (a.length() < 2) {
		            System.err.println("Error at argument " + a);
		            return;
		        }

		        options = new ArrayList<String>();
		        parameters.put(a.substring(1), options);
		    }
		    else if (options != null) {
		        options.add(a);
		    }
		    else {
		        System.err.println("Illegal parameter usage");
		        return;
		    }
		}
	}
}
