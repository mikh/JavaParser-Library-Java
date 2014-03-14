package java_parser;

import java.util.ArrayList;

public class Class {
	public String name;
	public boolean c_private = false;
	public boolean c_public = false;
	public boolean c_protected = false;
	public boolean c_abstract = false;
	public boolean c_final = false;
	public boolean c_static = false;
	public int line_start;
	public int index_start;
	
	public ArrayList<Variable> global_variables = new ArrayList<Variable>();
	public ArrayList<Method> methods = new ArrayList<Method>();
	
	public Class(String name){
		this.name = name;
	}
	
	public void print(String tabs){
		System.out.println(tabs + "CLASS: " + name);
		System.out.print(tabs + "STATUS: ");
		if(c_private)
			System.out.print("private ");
		if(c_public)
			System.out.print("public ");
		if(c_protected)
			System.out.print("protected ");
		if(c_abstract)
			System.out.print("abstract ");
		if(c_final)
			System.out.print("final ");
		if(c_static)
			System.out.print("static ");
		System.out.println("");
		System.out.println(tabs + "LINESTART: " + line_start);
		System.out.println(tabs + "INDEXSTART: " + index_start);
		System.out.println(tabs + "GLOBALS:\n");
		for(int ii = 0; ii < global_variables.size(); ii++)
			global_variables.get(ii).print(tabs+"\t");
		System.out.println(tabs + "METHODS:\n");
		for(int ii = 0; ii < methods.size(); ii++)
			methods.get(ii).print(tabs + "\t");
		System.out.println("\n");
	}
}
