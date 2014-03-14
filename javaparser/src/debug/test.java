package debug;

import java_parser.JParser;

public class test {
	public static void main(String[] args){
		JParser jp = new JParser();
		jp.readFile("java_test.java");
		//jp.print();
		jp.setPackage("java_test.java", "crap");
	}
}
