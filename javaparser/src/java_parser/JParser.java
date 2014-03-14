package java_parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import string_operations.StrOps;
import file_operations.FileOps;

public class JParser {
	public ArrayList<JavaFile> files = new ArrayList<JavaFile>();
	public boolean suppressOutput = false;
	
	public JParser(){
		
	}
	
	public void suppress(boolean suppress){
		suppressOutput = suppress;
	}
	
	public void readFile(String filename){
		try{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			JavaFile  jf = new JavaFile(filename);
			
			String line = br.readLine();
			while(line != null){
				//add to raw
				jf.raw_file.add(line);	
				jf.save_file += line;
				jf.save_file += "\n";
				line = br.readLine();
			}
			
			
			if(!quote_test(jf))
				if(!suppressOutput)System.out.println("[INPUT ERROR] cantrip.java_parser.JParser::Error within file. Quote Test Failed.");
			if(!braces_test(jf))
				if(!suppressOutput)System.out.println("[INPUT ERROR] cantrip.java_parser.JParser::Error within file. Braces Test Failed.");
			removeComments(jf);
			removeQuotes(jf);
			//jf.printRawFile();
			
			getPackage(jf);
			getImports(jf);

			parseClass(jf);
			files.add(jf);
			br.close();
		} catch(IOException e){
			System.out.println("[ERROR] cantrip.java_parser.JParser::Error reading " + filename);
			System.exit(-1);
		}
	}
	
	public ArrayList<String> getClasses(){
		ArrayList<String> list = new ArrayList<String>();
		for(int ii = 0; ii < files.size(); ii++){
			for(int jj = 0; jj < files.get(ii).classes.size(); jj++)
				list.add(files.get(ii).classes.get(jj).name);
		}
		return list;
	}
	
	private void getPackage(JavaFile jf){
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			int index = StrOps.findPattern(line, "package");
			int index_stop = StrOps.findPattern(line, "{");
			if(index != -1){
				if(index_stop == -1 || (index < index_stop)){
					index += "package".length();
					String pack = line.substring(index);
					pack = StrOps.trimString(pack);
					pack = StrOps.deleteAllInstances(pack, ";");
					jf.pack = pack;
				}
			}
			if(index_stop != -1)
				break;
		}
	}
	
	private void getImports(JavaFile jf){
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			int index = StrOps.findPattern(line, "import");
			int index_stop = StrOps.findPattern(line, "{");
			if(index != -1){
				if(index_stop == -1 || (index < index_stop)){
					index += "package".length();
					String imp = line.substring(index);
					imp = StrOps.trimString(imp);
					imp = StrOps.deleteAllInstances(imp, ";");
					jf.imports.add(imp);
				}
			}
			if(index_stop != -1)
				break;
		}
	}
	
	private boolean quote_test(JavaFile jf){
		int double_quotes = 0;
		int single_quotes = 0;
		
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			
			for(int index = 0; index < line.length(); index++){
				char cc = line.charAt(index);
				if(cc == '\"'){
					if(index < 1 || (index > 0 && line.charAt(index-1)!='\\')){
						if(double_quotes > 0)
							double_quotes--;
						else
							double_quotes++;
					}
				}
				if(cc == '\''){
					if(index < 1 || (index > 0 && line.charAt(index-1)!='\\')){
						if(single_quotes > 0)
							single_quotes--;
						else
							single_quotes++;
					}
				}
			}
		}
		
		if(double_quotes != 0 || single_quotes != 0)
			return false;
		return true;
	}
	
	private boolean braces_test(JavaFile jf){
		int braces = 0;
		boolean in_single_quotes = false;
		boolean in_double_quotes = false;
		
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			for(int index = 0; index < line.length(); index++){
				char cc = line.charAt(index);
				if(cc == '{' && !in_single_quotes && !in_double_quotes){
					braces++;
				} else if(cc == '}' && !in_single_quotes && !in_double_quotes){
					braces--;
				} else if(cc == '\'' && (index == 0 ||(index > 0 && line.charAt(index-1) != '\\'))){
					if(!in_single_quotes)
						in_single_quotes = true;
					else
						in_single_quotes = false;
				} else if(cc == '\"' && (index == 0 ||(index > 0 && line.charAt(index-1) != '\\'))){
					if(!in_double_quotes)
						in_double_quotes = true;
					else
						in_double_quotes = false;
				}
			}
		}
		
		if(braces != 0)
			return false;
		return true;
	}
	
	private void parseClass(JavaFile jf){
		boolean looking_for_brace = false;
		int braces = 0;
		
		//first find all classes
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			int index = 0;
			
			if(!looking_for_brace){
				index = StrOps.findPattern(line, " class ");
				if(index != -1){
					index += " class ".length();
					String class_name = "";
					while(index < line.length() && line.charAt(index) != ',' && line.charAt(index) != ':' && line.charAt(index) != '{' && line.charAt(index) != ' ' ){
						class_name += line.charAt(index++);
					}
					class_name = StrOps.trimString(class_name);
					jf.classes.add(new Class(class_name));
					looking_for_brace = true;
				}
			}
			
			if(looking_for_brace){
				while(index < line.length() && looking_for_brace){
					if(line.charAt(index) == '{'){
						looking_for_brace = false;
						jf.classes.get(jf.classes.size()-1).line_start = ii;
						jf.classes.get(jf.classes.size()-1).index_start = index;
					}
					index++;
				}
			}
		}
		
		//then find all the stuff in the classes
		for(int ii = 0; ii < jf.classes.size(); ii++){
			int line_n = jf.classes.get(ii).line_start;
			int index = jf.classes.get(ii).index_start;
			String line = jf.raw_file.get(line_n);
			braces = 0;
			String build = "";
			
			do{
				char cc = line.charAt(index);
				if(cc == '{'){
					braces++;
					if(braces == 2){
						parseBuild(build, jf, ii);
						build = "";
					}
				}
				else if(cc == '}'){
					braces--;
					if(braces == 0){
						parseBuild(build, jf, ii);
						build = "";
					}
				}
				else if(braces == 1){
					if(cc == ';'){
						parseBuild(build, jf, ii);
						build = "";
					} else
						build += cc;
				}
				
				index++;
				if(index == line.length()){
					line_n++;
					if(line_n < jf.raw_file.size()){
						line = jf.raw_file.get(line_n);
						while(line.length() == 0){
							line_n++;
							if(line_n < jf.raw_file.size())
								line = jf.raw_file.get(line_n);
							else{
								break;
							}
						}
						if(line_n == jf.raw_file.size())
							break;
						index = 0;
					} else
						break;
				}
			} while(braces != 0);
		}
	}
	
	private void parseBuild(String build, JavaFile jf, int cl){
		build = StrOps.trimString(build);
		if(!build.equals("")){
			boolean var = true;
			int index = StrOps.findPattern(build, "=");
			if(index != -1){
				var = true;
				build = build.substring(0,index);
				build = StrOps.trimString(build);
			} else{
				index = StrOps.findPattern(build, "(");
				if(index != -1)
					var = false;
			}
			
			if(var){
				boolean pub = false, pri = false, pro = false, abs = false, fin = false, sta = false;
				index = StrOps.findPattern(build, "public");
				if(index != -1){
					pub = true;
					build = build.substring(0,index) + build.substring(index + "public".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "private");
				if(index != -1){
					pri = true;
					build = build.substring(0,index) + build.substring(index + "private".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "protected");
				if(index != -1){
					pro = true;
					build = build.substring(0,index) + build.substring(index + "protected".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "abstract");
				if(index != -1){
					abs = true;
					build = build.substring(0,index) + build.substring(index + "abstract".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "final");
				if(index != -1){
					fin = true;
					build = build.substring(0,index) + build.substring(index + "final".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "static");
				if(index != -1){
					sta = true;
					build = build.substring(0,index) + build.substring(index + "static".length());
					build = StrOps.trimString(build);
				}
				
				String type = StrOps.trimString(StrOps.getDilineatedSubstring(build, " ", 0, false));
				String name = StrOps.trimString(StrOps.getDilineatedSubstring(build, " ", 1, false));
				Variable varr = new Variable(name, type);
				varr.v_public = pub;
				varr.v_private = pri;
				varr.v_protected = pro;
				varr.v_abstract = abs;
				varr.v_final = fin;
				varr.v_static = sta;
				jf.classes.get(cl).global_variables.add(varr);
			} else{
				boolean pub = false, pri = false, pro = false, abs = false, fin = false, sta = false;
				index = StrOps.findPattern(build, "public");
				if(index != -1){
					pub = true;
					build = build.substring(0,index) + build.substring(index + "public".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "private");
				if(index != -1){
					pri = true;
					build = build.substring(0,index) + build.substring(index + "private".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "protected");
				if(index != -1){
					pro = true;
					build = build.substring(0,index) + build.substring(index + "protected".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "abstract");
				if(index != -1){
					abs = true;
					build = build.substring(0,index) + build.substring(index + "abstract".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "final");
				if(index != -1){
					fin = true;
					build = build.substring(0,index) + build.substring(index + "final".length());
					build = StrOps.trimString(build);
				}
				
				index = StrOps.findPattern(build, "static");
				if(index != -1){
					sta = true;
					build = build.substring(0,index) + build.substring(index + "static".length());
					build = StrOps.trimString(build);
				}
				String type = StrOps.trimString(StrOps.getDilineatedSubstring(build, " ", 0, false));
				String name = StrOps.trimString(StrOps.getDilineatedSubstring(build, " ", 1, false));
				name = StrOps.trimString(StrOps.getDilineatedSubstring(name, "(", 0, false));
				
				String args = StrOps.trimString(StrOps.get_substring_between_patterns(build, "(", ")"));
				ArrayList<Variable> arguments = new ArrayList<Variable>();
				int commas = StrOps.countInstances(args, ",");
				for(int qq = 0; qq <= commas; qq++){
					String aa = StrOps.getDilineatedSubstring(args, ",", qq, false);
					aa = StrOps.trimString(aa);
					String t = StrOps.trimString(StrOps.getDilineatedSubstring(aa, " ", 0, false));
					String n = StrOps.trimString(StrOps.getDilineatedSubstring(aa, " ", 1, false));
					if(!(t.equals("") || t.equals("")))
						arguments.add(new Variable(n,t));
				}
				
				Method me = new Method(name, type);
				me.m_abstract = abs;
				me.m_final = fin;
				me.m_private = pri;
				me.m_protected = pro;
				me.m_public = pub;
				me.m_static = sta;
				me.arguments = arguments;
				jf.classes.get(cl).methods.add(me);
			}
		}
	}
	
	private void removeComments(JavaFile jf){
		boolean in_multi = false;
		boolean in_single_quotes = false;
		boolean in_double_quotes = false;
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			String new_line = "";
			for(int index = 0; index < line.length(); index++){
				char cc = line.charAt(index);
				if(in_multi){
					if(cc == '*' && index <line.length()-1 && line.charAt(index+1) == '/'){
						in_multi = false;
						index++;
					}
				} else{
					if(cc == '\'' && (index == 0 || (index > 0 && (line.charAt(index-1) != '\\' || (index > 1 && line.charAt(index-2) == '\\'))))){
						if(in_single_quotes)
							in_single_quotes = false;
						else
							in_single_quotes = true;
						new_line += cc;
					} else if(cc == '\"' && (index == 0 || (index > 0 && (line.charAt(index-1) != '\\' || (index > 1 && line.charAt(index-2) == '\\'))))){
						if(in_double_quotes)
							in_double_quotes = false;
						else
							in_double_quotes = true;
						new_line += cc;
					} else if(cc == '/' && !in_single_quotes && !in_double_quotes){
						if(index < line.length()-1 && line.charAt(index+1) == '*')
							in_multi = true;
						else if(index < line.length()-1 && line.charAt(index+1) == '/')
							break;
						else
							new_line += '/';
					}
					else new_line += cc;
				}
			}
			jf.raw_file.set(ii, new_line);
		}
	}
	
	private void removeQuotes(JavaFile jf){
		boolean in_single_quotes = false;
		boolean in_double_quotes = false;
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			String new_line = "";
			for(int index = 0; index < line.length(); index++){
				char cc = line.charAt(index);
				if(in_single_quotes){
					if(cc == '\'' && (index == 0 || (index > 0 && (line.charAt(index-1) != '\\' || (index > 1 && line.charAt(index-2) == '\\')))))
						in_single_quotes = false;
				} else if(in_double_quotes){
					if(cc == '\"' && (index == 0 || (index > 0 && (line.charAt(index-1) != '\\' || (index > 1 && line.charAt(index-2) == '\\')))))
						in_double_quotes = false;
				} else{
					if(cc == '\'')
						in_single_quotes = true;
					else if(cc == '\"')
						in_double_quotes = true;
					else
						new_line += cc;
				}
			}
			jf.raw_file.set(ii, new_line);
		}
	}
	
	public void print(){
		for(int ii = 0; ii < files.size(); ii++){
			files.get(ii).print("");
		}
	}
	
	public int findFile(String file_name){
		for(int ii = 0; ii < files.size(); ii++){
			String path = files.get(ii).file_path;
			String name = StrOps.getDilineatedSubstring(path, "\\", 0, true);
			if(name.equals(file_name))
				return ii;
		}
		return -1;
	}
	
	public void renameFile(String orig_file, String new_name){
		int index = -1;
		for(int ii = 0; ii < files.size(); ii++){
			String path = files.get(ii).file_path;
			if(path.equals(orig_file)){
				index = ii;
				break;
			}
		}
		if(index == -1){
			System.out.println("[ERROR] cantrip.java_parser.JParser cannot find file " + orig_file);
			System.exit(-1);
		}
		
		files.get(index).file_path = FileOps.renameFile(orig_file, new_name, true);
	}

	public void renameClass(String orig_file, String orig_class, String new_class){
		int index = -1;
		for(int ii = 0; ii < files.size(); ii++){
			String path = files.get(ii).file_path;
			if(path.equals(orig_file)){
				index = ii;
				break;
			}
		}
		if(index == -1){
			System.out.println("[ERROR] cantrip.java_parser.JParser cannot find file " + orig_file);
			System.exit(-1);
		}
		for(int ii = 0; ii < files.size(); ii++){
			files.get(ii).save_file = StrOps.replaceAll(files.get(ii).save_file, orig_class, new_class);
			files.get(ii).writeSaveFile();
		}
		renameFile(orig_file, new_class);		
	}
	
	public void renameMethodVariable(String original, String updated){
		for(int ii = 0; ii < files.size(); ii++){
			files.get(ii).save_file = StrOps.replaceAll(files.get(ii).save_file, original, updated);
			files.get(ii).writeSaveFile();
		}
	}

	public void addWrapperClass(String file, String cl){
		int index = -1;
		for(int ii = 0; ii < files.size(); ii++){
			String path = files.get(ii).file_path;
			if(path.equals(file)){
				index = ii;
				break;
			}
		}
		if(index == -1){
			System.out.println("[ERROR] cantrip.java_parser.JParser cannot find file " + file);
			System.exit(-1);
		}
		
		//Assume there is no package or imports
		String str = files.get(index).save_file;
		str = "\n public class " + cl + " {" + str + "\n\n}\n\n";
		files.get(index).save_file = str;
		files.get(index).writeSaveFile();
		renameFile(file, cl);
	}
	
	public void setPackage(String file, String pack){
		int index = -1;
		for(int ii = 0; ii < files.size(); ii++){
			String path = files.get(ii).file_path;
			if(path.equals(file)){
				index = ii;
				break;
			}
		}
		if(index == -1){
			System.out.println("[ERROR] cantrip.java_parser.JParser cannot find file " + file);
			System.exit(-1);
		}
		
		String str = files.get(index).save_file;
		String new_str = "";
		int ii = StrOps.findPattern(str, "package ");
		if(ii != -1){
			new_str = str.substring(0,ii);
			new_str += "package " + pack + ";\n\n";
			while(ii < str.length() && str.charAt(ii) != '\n')
				ii++;
			new_str += str.substring(ii);
		}
		else{
			new_str = "package " + pack + ";\n\n" + str;
		}
		files.get(index).save_file = new_str;
		files.get(index).writeSaveFile();
	}
	
	public boolean findString(String to_find){
		for(int ii = 0; ii < files.size(); ii++){
			String str = files.get(ii).save_file;
			int index = StrOps.findPattern(str, to_find);
			if(index != -1)
				return true;
		}
		return false;
	}
}
