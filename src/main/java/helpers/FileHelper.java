package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileHelper {
	
	public static boolean fileExists(String filePath) {
		File f = new File(filePath);
		return f.exists();
	}
	

	
	/**
	 * Searches for files in a directory whose name (extension included) ends with a specific string
	 * @param directory Directory in which to search the files
	 * @param nameEndsWith 
	 * @param searchInSubdirectories If true, will search in subdirectories
	 * @return List of files found
	 */
	public static Collection<File> listFiles(String directoryPath, String nameEndsWith, boolean searchInSubdirectories) {
		File directory = new File(directoryPath); 
		List<File> filesFound = new LinkedList<File>();
		Queue<File> directoriesToExplore = new LinkedList<File>();
		directoriesToExplore.add(directory);
		while (!directoriesToExplore.isEmpty()) {
			for (File file : directoriesToExplore.poll().listFiles()) {
				if (file.isDirectory() && searchInSubdirectories == true) {
					directoriesToExplore.add(file);
				} 
				else if (file.isFile() && file.toString().endsWith(nameEndsWith)) {
					filesFound.add(file);
				}
			}
		}
		return filesFound;
	}
	
	/**
	 * Deletes a directory and its subdirectories
	 * @param directory Directory to delete
	 */
	public static void deleteDirectory(File directory) {
	    File[] files = directory.listFiles();
	    for (File myFile: files) {
	        if (myFile.isDirectory()) {  
	        	deleteDirectory(myFile);
	        } 
	        myFile.delete();
	    }
	}
	
	/**
	 * Copies a directory and its contents
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyDirectory(File src, File dest)
	    	throws IOException{

	    	if(src.isDirectory()){

	    		//if directory not exists, create it
	    		if(!dest.exists()){
	    		   dest.mkdir();
	    		   System.out.println("Directory copied from "
	                              + src + "  to " + dest);
	    		}

	    		//list all the directory contents
	    		String files[] = src.list();

	    		for (String file : files) {
	    		   //construct the src and dest file structure
	    		   File srcFile = new File(src, file);
	    		   File destFile = new File(dest, file);
	    		   //recursive copy
	    		   copyDirectory(srcFile,destFile);
	    		}

	    	}else{
	    		//if file, then copy it
	    		//Use bytes stream to support all file types
	    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest);

    	        byte[] buffer = new byte[1024];

    	        int length;
    	        //copy the file content in bytes
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }

    	        in.close();
    	        out.close();
	    	}
	    }

	
}
