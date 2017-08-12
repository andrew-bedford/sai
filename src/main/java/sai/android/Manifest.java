package sai.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class Manifest {
	private File file;
	public Collection<String> permissions;
	public Collection<String> intents;
	
	public Manifest(File manifest) {
		this.file = manifest;
		extractPermissions();
		extractIntents();
	}
	
	private void extractPermissions() {
		int isPermissionPresent;
		int permissionBegin;
		int permissionEnd;
		String line;
		this.permissions = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file)); 				
			while ((line = br.readLine()) != null) {
				isPermissionPresent = line.indexOf("<uses-permission ");
				if (isPermissionPresent != -1) {
					permissionBegin = line.indexOf("\"")+1;
					permissionEnd = line.indexOf("\"",permissionBegin);
					this.permissions.add(line.substring(permissionBegin, permissionEnd).toLowerCase());
				}
			}
			br.close();
		}  
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
	 
	private void extractIntents() {
		int index;
		String line;
		intents = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file)); 				
			while ((line = br.readLine()) != null) {
				index = line.indexOf("android.intent.");
				if (index != -1) {
					intents.add(line.substring(index, line.length()-3).toLowerCase());
				}
			}
			br.close();
		}  
		catch (IOException e) {
			e.printStackTrace();
		}
	} 
}
