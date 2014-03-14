package java_parser;

import java.util.ArrayList;

import java_parser.Variable;

public class Method {
	public String name;
	public boolean m_private = false;
	public boolean m_public = false;
	public boolean m_protected = false;
	public boolean m_abstract = false;
	public boolean m_final = false;
	public boolean m_static = false;
	public String return_type;
	public ArrayList<Variable> arguments = new ArrayList<Variable>();
	public ArrayList<Variable> local_variables = new ArrayList<Variable>();
	
	public Method(String name, String return_type){
		this.name = name;
		this.return_type = return_type;
	}
	
	public void print(String tabs){
		System.out.println(tabs + "METHOD: " + name);
		System.out.print(tabs + "STATUS: ");
		if(m_private)
			System.out.print("private ");
		if(m_public)
			System.out.print("public ");
		if(m_protected)
			System.out.print("protected ");
		if(m_abstract)
			System.out.print("abstract ");
		if(m_final)
			System.out.print("final ");
		if(m_static)
			System.out.print("static ");
		System.out.println("");
		System.out.println(tabs + "RETURN: " + return_type);
		System.out.println(tabs + "ARGUMENTS:\n");
		for(int ii = 0; ii < arguments.size(); ii++)
			arguments.get(ii).print(tabs+"\t");
		System.out.println(tabs + "LOCALS:\n");
		for(int ii = 0; ii < local_variables.size(); ii++)
			local_variables.get(ii).print(tabs + "\t");
		System.out.println("\n");
	}
}
