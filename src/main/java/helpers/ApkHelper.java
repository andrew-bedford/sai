package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ApkHelper {

	public static void disassemble(File apk, String apktoolPath, String disassembledApkPath) {
		try {
			String[] apktoolArgs = {"java", "-jar", apktoolPath, "d", "--force", "--output", disassembledApkPath, apk.getAbsolutePath()};
			ProcessBuilder pb = new ProcessBuilder(apktoolArgs); 
			Process p = pb.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errors = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			while ((line = errors.readLine()) != null) {
				System.err.println(line);
			}
			p.waitFor();
			input.close();
			errors.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Builds an apk
	 * @param apktoolPath
	 * @param disassembledApkPath
	 */
	public static void assemble(String apktoolPath, String disassembledApkPath) {
		try {
			String[] apktoolArgs = {"java", "-jar", apktoolPath, "b", disassembledApkPath};
			//String[] apktoolArgs = {"java", "-jar", "./lib/smali-2.2.1.jar", "a", "./tmp/apk/smali", "-o", "test.dex"};
			ProcessBuilder pb = new ProcessBuilder(apktoolArgs); 
			Process p = pb.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errors = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			while ((line = errors.readLine()) != null) {
				System.err.println(line);
			}
			p.waitFor();
			input.close();
			errors.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
