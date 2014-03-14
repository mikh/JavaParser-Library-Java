package java_parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class JavaFile {

	public ArrayList<Class> classes = new ArrayList<Class>();
	public String file_path;
	public ArrayList<String> imports = new ArrayList<String>();
	public String pack;
	public ArrayList<String> raw_file = new ArrayList<String>();
	public String save_file = "";
	
	public JavaFile(String file_path){
		this.file_path = file_path;
	}
	
	public void print(String tabs){
		System.out.println(tabs + "FILEPATH: " + file_path);
		System.out.println(tabs + "PACKAGE: " + pack);
		System.out.println(tabs + "IMPORTS:\n");
		for(int ii = 0; ii < imports.size(); ii++)
			System.out.println(tabs + "\t" + imports.get(ii));
		System.out.println("\nCLASSES:\n");
		for(int ii = 0; ii < classes.size(); ii++)
			classes.get(ii).print(tabs+"\t");
	}
	
	public void printRawFile(){
		for(int ii = 0; ii < raw_file.size(); ii++){
			System.out.println(raw_file.get(ii));
		}
	}
	
	public void writeSaveFile(){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file_path, false));
			bw.write(save_file);
			bw.close();
		} catch(IOException e){
			System.out.println("[ERROR] cantrip.java_parser.JavaFile Cannot write to " + file_path);
			System.exit(-1);
		}
	}
}
