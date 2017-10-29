/*
Intro to Systems Software Group Project
Christopher Kile
*/

import java.util.*;

public class Assembler{
	public static void main(String[] args){
		
	}
	
	
}



class Operation{
	String op;
	String opCode;
	int length;
	
	public Operation(String op, String opCode, int length){
		this.op = op;
		this.opCode = opCode;
		this.length = length;
	}
	
	public void setOp(String op){
		this.op = op;
	}
	
	public String getOp(){
		return op;
	}
	
	public void setOpCode(String opCode){
		this.opCode = opCode;
	}
	
	public String getOpCode(){
		return opCode;
	}
	
	public String toString(){
		return op + " " + opCode + " " + format;
		
	}
	
	public Hashtable buildOPTAB(){
		//Sets format to -1 for unsupported instructions
		
		Hashtable OPTAB = new Hashtable();
		
		OPTAB.put("ADD", new Operation("ADD", "18", 3));
		OPTAB.put("ADDF", new Operation("ADDF", "58", -1));
		OPTAB.put("ADDR", new Operation("ADDR", "90", 2));
		OPTAB.put("AND", new Operation("AND", "40", 3));
		OPTAB.put("CLEAR", new Operation("CLEAR", "B4", 2));
		OPTAB.put("COMP", new Operation("COMP", "28", 3));
		OPTAB.put("COMPF", new Operation("COMPF", "88", 3));
		OPTAB.put("COMPR", new Operation("COMPR", "A0", 2));
		OPTAB.put("DIV", new Operation("DIV", "24", -1));
		OPTAB.put("DIVF", new Operation("DIVF", "64", -1));
		OPTAB.put("DIVR", new Operation("DIVR", "9C", -1));
		OPTAB.put("FIX", new Operation("FIX", "C4", -1));
		OPTAB.put("FLOAT", new Operation("FLOAT", "C0", -1));
		OPTAB.put("HIO", new Operation("HIO", "F4", 1));
		OPTAB.put("J", new Operation("J", "3C", 3));
		OPTAB.put("JEQ", new Operation("JEQ", "30", 3));
		OPTAB.put("JGT", new Operation("JGT", "34", 3));
		OPTAB.put("JLT", new Operation("JLT", "38", 3));
		OPTAB.put("JSUB", new Operation("JSUB", "48", 3));
		OPTAB.put("LDA", new Operation("LDA", "00", 3));
		OPTAB.put("LDB", new Operation("LDB", "68", 3));
		OPTAB.put("LDCH", new Operation("LDCH", "50", 3));
		OPTAB.put("LDF", new Operation("LDF", "70", 3));
		OPTAB.put("LDL", new Operation("LDL", "08", 3));
		OPTAB.put("LDS", new Operation("LDS", "6C", 3));
		OPTAB.put("LDT", new Operation("LDT", "74", 3));
		OPTAB.put("LDX", new Operation("LDX", "04", 3));
		OPTAB.put("LPS", new Operation("LPS", "D0", 3));
		OPTAB.put("MUL", new Operation("MUL", "20", 3));
		OPTAB.put("MULF", new Operation("MULF", "60", -1));
		OPTAB.put("MULR", new Operation("MULR", "98", 2));
		OPTAB.put("NORM", new Operation("NORM", "C8", -1));
		OPTAB.put("OR", new Operation("OR", "44", 3));
		OPTAB.put("RD", new Operation("RD", "D8", 3));
		OPTAB.put("RMO", new Operation("RMO", "AC", 2));
		OPTAB.put("RSUB", new Operation("RSUB", "4C", 3));
		OPTAB.put("SHIFTL", new Operation("SHIFTL", "A4", 2));
		OPTAB.put("SHIFTR", new Operation("SHIFTR", "A8", 2));
		OPTAB.put("SIO", new Operation("SIO", "F0", 1));
		OPTAB.put("SSK", new Operation("SSK", "EC", 3));
		OPTAB.put("STA", new Operation("STA", "0C", 3));
		OPTAB.put("STB", new Operation("STB", "78", 3));
		OPTAB.put("STCH", new Operation("STCH", "54", 3));
		OPTAB.put("STF", new Operation("STF", "80", 3));
		OPTAB.put("STI", new Operation("STI", "D4", 3));
		OPTAB.put("STL", new Operation("STL", "14", 3));
		OPTAB.put("STS", new Operation("STS", "7C", 3));
		OPTAB.put("STSW", new Operation("STSW", "E8", 3));
		OPTAB.put("STT", new Operation("STT", "84", 3));
		OPTAB.put("STX", new Operation("STX", "10", 3));
		OPTAB.put("SUB", new Operation("SUB", "1C", 3));
		OPTAB.put("SUBF", new Operation("SUBF", "5C", -1));
		OPTAB.put("SUBR", new Operation("SUBR", "94", 2));
		OPTAB.put("SVC", new Operation("SVC", "B0", 2));
		OPTAB.put("TD", new Operation("TD", "E0", 3));
		OPTAB.put("TIO", new Operation("TIO", "F8", 1));
		OPTAB.put("TIX", new Operation("TIX", "2C", 3));
		OPTAB.put("TIXR", new Operation("TIXR", "B8", 2));
		OPTAB.put("WD", new Operation("WD", "DC", 3));

		return OPTAB;
		
	}
	
}


class Symbol{
		String label;
		String memoryLocation;
		
	public Symbol(String label){
		this.label = label;
	}
		
	public Symbol(String label, String memoryLocation){
		this.label = label;
		this.memoryLocation = memoryLocation;
	}
		
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setMemoryLocation(String memoryLocation){
		this.memoryLocation = memoryLocation;
	}
	
	public String getMemoryLocation(){
		return memoryLocation;
	}

}
