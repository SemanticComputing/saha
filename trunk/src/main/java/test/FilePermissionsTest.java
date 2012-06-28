package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FilePermissionsTest {

	public static void main(String[] args) throws IOException {
		
		File f = new File("foo.txt");
		
		System.out.println(f.getAbsolutePath());
		
		if (!f.exists()) {
			FileWriter fw = new FileWriter(f);
			fw.write("foo");
			fw.close();
		}
		
		System.out.println("Setting permissions...");

		f.setWritable(true,false);
		f.setReadable(true,false);
		
		System.out.println("Done.");
		
	}
	
}
