package java_parser;

public class Variable {
	
	public String type;
	public boolean v_public = false;
	public boolean v_private = false;
	public boolean v_protected = false;
	public boolean v_abstract = false;
	public boolean v_final = false;
	public boolean v_static = false;
	public String name;
	
	public Variable(String name, String type){
		this.name = name;
		this.type = type;
	}
	
	public void print(String tabs){
		System.out.println(tabs + "VARIABLE: " + name);
		System.out.print(tabs + "STATUS: ");
		if(v_private)
			System.out.print("private ");
		if(v_public)
			System.out.print("public ");
		if(v_protected)
			System.out.print("protected ");
		if(v_abstract)
			System.out.print("abstract ");
		if(v_final)
			System.out.print("final ");
		if(v_static)
			System.out.print("static ");
		System.out.println("\n" + tabs + "TYPE: " + type);
		System.out.println("");
	}
}
