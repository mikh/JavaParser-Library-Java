package crap;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import string_operations.StrOps;

public class JParser {
	public ArrayList<JavaFile> files = new ArrayList<JavaFile>();
	
	public JParser(){
		
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
				line = br.readLine();
			}
			
			
			if(!quote_test(jf))
				System.out.println("[INPUT ERROR] cantrip.java_parser.JParser::Error within file. Quote Test Failed.");
			if(!braces_test(jf))
				System.out.println("[INPUT ERROR] cantrip.java_parser.JParser::Error within file. Braces Test Failed.");
			
			
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
		boolean in_class = false;
		boolean looking_for_brace = true;
		int braces = 0;
		boolean in_single_quotes = false;
		boolean in_double_quotes = false;
		for(int ii = 0; ii < jf.raw_file.size(); ii++){
			String line = jf.raw_file.get(ii);
			if(!in_class){
				int class_index = StrOps.findPattern(line, "class");
				for(int index = 0; index < line.length(); index++){
					char cc = line.charAt(index);
					if(cc == '\'' && (index == 0 ||(index > 0 && line.charAt(index-1) != '\\'))){
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
					
					if(class_index != -1 && index == class_index && !in_single_quotes && !in_double_quotes){
						//get class name
						index += "class".length();
						String class_name = "";
						cc = line.charAt(index);
						while(index < line.length() && cc != ',' && cc != ':' && cc != '{'){
							class_name += cc;
							index++;
							if(index < line.length()) cc = line.charAt(index);
						}
						class_name = StrOps.trimString(class_name);
						jf.classes.add(new Class(class_name));
						in_class = true;
					}
				}
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
					if(cc == '\'' && (index == 0 || (index > 0 && line.charAt(index-1) != '\\'))){
						if(in_single_quotes)
							in_single_quotes = false;
						else
							in_single_quotes = true;
					} else if(cc == '\"' && (index == 0 || (index > 0 && line.charAt(index-1) != '\\'))){
						if(in_double_quotes)
							in_double_quotes = false;
						else
							in_double_quotes = true;
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
	
	public void print(){
		for(int ii = 0; ii < files.size(); ii++){
			files.get(ii).print("");
		}
	}
	
	
	/*
	 * 
	 * this is a test
	 */
}
	class stuff {   }
