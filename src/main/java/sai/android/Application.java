package sai.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import helpers.ApkHelper;
import helpers.FileHelper;
import sai.Configuration;

public class Application {

	private File disassembledFilesDirectory;
	private Collection<Class> classes;
	private Manifest manifest;
	public Map<String,String> methods;
	
	public Application(File filesDirectory) {
		this.disassembledFilesDirectory = filesDirectory;
		loadClasses();
		loadManifest();		
	}
	
//	private void loadMethods() {
//		methods = new HashMap<String,String>();
//		Collection<File> classFiles = FileHelper.listFiles(Configuration.disassembledApkPath, ".smali", true);
//		for (File f : classFiles) {
//			System.out.println("FILE:"+f.getAbsolutePath());
//			String classCode = FileHelper.convertFileToString(f);
//			
//			String[] classLines = classCode.split(System.getProperty("line.separator"));
//			
//			String[] cw = classLines[0].split(" ");
//			String className = cw[cw.length-1].replace(";", "");
//			System.out.println("CLASS:" + className);
//			
//			
//			Pattern pattern = Pattern.compile("\\.method.*?\\.end method", Pattern.MULTILINE | Pattern.DOTALL);
//			Matcher matcher = pattern.matcher(classCode);
//			while (matcher.find()) {
//				System.out.println("METHOD:");
//				String methodCode = matcher.group();
//				String[] methodLines = methodCode.split(System.getProperty("line.separator"));
//				String[] w = methodLines[0].split(" ");
//				System.out.println(methodCode);
//				String methodName =  w[w.length-1];
//				methods.put(className+"->"+methodName, methodCode);
//				System.out.println("METHOD NAME:" + methodName);
//				
//			}
//		}
//	}

	private void loadManifest() {
		String manifestPath = Configuration.disassembledApkPath + "/AndroidManifest.xml";
		manifest = new Manifest(new File(manifestPath));
		
	}

	public void disassemble() {
		ApkHelper.disassemble(this.disassembledFilesDirectory, Configuration.apktoolPath, Configuration.disassembledApkPath);
	}
	
	public void assemble() {
		ApkHelper.assemble(Configuration.apktoolPath, Configuration.disassembledApkPath);
	}
	
	private void loadClasses() {
		classes = new HashSet<Class>();
		Collection<File> classFiles = FileHelper.listFiles(Configuration.disassembledApkPath, ".smali", true);
		for (File f : classFiles) {
			try {
				classes.add(new Class(f));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public Collection<Class> getClasses() {
		return classes;
	}
	
	public Class getClass(String name) {
		for (Class c : classes) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		
		return null;	//TODO Throw exception instead 
	}
	
	public void saveChanges() {
		for (Class c : classes) {
			try {
				c.saveChanges();
			} catch (IOException e) {
				System.err.println("Unable to save changes made to class "+c.getName()+": " + e.getMessage());
			}
		}
	}
	
}
