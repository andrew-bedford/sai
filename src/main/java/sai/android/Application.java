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

	private File _disassembledFilesDirectory;
	private Collection<Class> _classes;
	private Manifest _manifest;
	
	public Application(File filesDirectory) {
		this._disassembledFilesDirectory = filesDirectory;
		loadClasses();
		loadManifest();		
	}

	private void loadManifest() {
		String manifestPath = Configuration.disassembledApkPath + "/AndroidManifest.xml";
		_manifest = new Manifest(new File(manifestPath));
		
	}

	public void disassemble() {
		ApkHelper.disassemble(this._disassembledFilesDirectory, Configuration.apktoolPath, Configuration.disassembledApkPath);
	}
	
	public void assemble() {
		ApkHelper.assemble(Configuration.apktoolPath, Configuration.disassembledApkPath);
	}
	
	private void loadClasses() {
		_classes = new HashSet<Class>();
		Collection<File> classFiles = FileHelper.listFiles(Configuration.disassembledApkPath, ".smali", true);
		for (File f : classFiles) {
			try {
				_classes.add(new Class(f));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public Collection<Class> getClasses() {
		return _classes;
	}
	
	public Class getClass(String name) {
		for (Class c : _classes) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		
		return null;	//TODO Throw exception instead 
	}
	
	public void saveChanges() {
		for (Class c : _classes) {
			try {
				c.saveChanges();
			} catch (IOException e) {
				System.err.println("Unable to save changes made to class "+c.getName()+": " + e.getMessage());
			}
		}
	}
	
}
