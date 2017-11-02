/*
 * Emily Czarnecki, Brennan Ledbetter, Christopher Kile
 * 
 * 11/2/2017 - Work Completed:
 * -built data structures
 * -built hex utility functions
 * -built machinecode assembler
 * -most of Pass 1 is built - currently debugging
 * 
 */
import java.util.*;
import java.io.*;

public class Assembler{
	public static void main(String[] args){
		
		Hashtable<String, Symbol> SYMTAB = new Hashtable<String, Symbol>();
		Hashtable<String, Operation> OPTAB = Operation.buildOPTAB();
		Pass1.readFile(args[0], SYMTAB, OPTAB);
		
		
		
	}
	
	
}

class Pass1{
    
    public static void readFile(String filename, Hashtable<String, Symbol> SYMTAB, Hashtable<String, Operation> OPTAB) {
        String input,label = null, opcode, operand, LOCCTR;
        String[] inputArray;
        int lineNum = 0;
        
        
        try {
            BufferedReader inputFile = new BufferedReader(new FileReader(filename));
            BufferedWriter intermediateFile = new BufferedWriter(new FileWriter(filename+".lst"));
            input = inputFile.readLine();
            lineNum++;
           
            inputArray = input.split("\\s+");
            
           if (inputArray.length == 3){
                    label = inputArray[0];
                    opcode = inputArray[1];
                    operand = inputArray[2];
            }
            else{
                opcode = inputArray[0];
                operand = inputArray[1];
            }
           
   //START OF PASS1 ALGORITHM
            if ("START".equals(opcode)){
            	
                LOCCTR = operand;
                intermediateFile.write(intermediateLine(LOCCTR, lineNum, input));
                intermediateFile.newLine();
                //write to intermediate file - make method for this
                input = inputFile.readLine();
                lineNum++;
                inputArray = input.split("\\s+");
                if (inputArray.length == 3){
                    label = inputArray[0];
                    opcode = inputArray[1];
                    operand = inputArray[2];
                }
                else{
                    opcode = inputArray[0];
                    operand = inputArray[1];
                }
            }
            else
                LOCCTR = "0";
            
            while (!"END".equals(opcode)){
            	//this does not handle whitespace in comment
                if (!input.startsWith(".")){
                  if (label != null){
                    if (SYMTAB.get(label) != null){
                        //duplicate label error
                    }   
                    else{
                    	intermediateFile.write(intermediateLine(LOCCTR, lineNum, input));
                        intermediateFile.newLine();
                        
                    	if(OPTAB.get(opcode) != null){
                    		Operation currentOp = OPTAB.get(opcode);
                    		int format = currentOp.getFormat();
                    		LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(format));
                    		
                    	}else if(opcode.equals("WORD")){
                    		LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(3));
                    	}else if(opcode.equals("RESW")){
                    		int memAdd = 3 * Integer.parseInt(operand);
                    		LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(memAdd));
                    	}else if(opcode.equals("RESB")){
                    		LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(Integer.parseInt(operand)));
                    		
                    	}else if(opcode.equals("BYTE")){
                    		LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(1));
                    		
                    	}else{
                    		//set error flag
                    	}
                    SYMTAB.put(label, new Symbol(label, LOCCTR));
                }
                  
                
            }
            
            
          } //end comment check
                
          
                input = inputFile.readLine();
                lineNum++;
                System.out.print(input+"\n");
                inputArray = input.split("\\s+");
                
               if (inputArray.length == 3){
                        label = inputArray[0];
                        opcode = inputArray[1];
                        operand = inputArray[2];
                }
                else{
                    opcode = inputArray[0];
                    operand = inputArray[1];
                }
               System.out.println("opcode is " + opcode);
        }
            intermediateFile.write(intermediateLine(LOCCTR, lineNum, input));
            intermediateFile.newLine();
            System.out.println(SYMTAB);
            intermediateFile.close();
            
        }catch(FileNotFoundException fnfe) {
            System.out.format("The program could not find the file: %s.\n", filename);
        }
        catch(IOException ioe) {
            System.out.format("An error occurred while reading the file: %s.\n", filename);
        }
        catch(Exception ex) {
            System.out.format("An unexpected error occurred.  Error information: %s.\n", ex.getMessage());
        }
    }
    
    public static String intermediateLine(String LOCCTR, int lineNum, String currentLine){
    	StringBuilder lineBuilder = new StringBuilder();
    	lineBuilder.append(Integer.toString(lineNum));
    	lineBuilder.append("\t");
    	lineBuilder.append(LOCCTR);
    	lineBuilder.append("\t");
    	lineBuilder.append(currentLine);
    	return lineBuilder.toString();
    	
    }
}




class Operation{
	String op;
	String opCode;
	int format;
	
	public Operation(String op, String opCode, int format){
		this.op = op;
		this.opCode = opCode;
		this.format = format;
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
	
	public int getFormat(){
		return format;
	}
	
	public static Hashtable<String, Operation> buildOPTAB(){
		//Sets format to -1 for unsupported instructions
		
		Hashtable<String, Operation> OPTAB = new Hashtable<String, Operation>();
		
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
	
	public String toString(){
		return label + " " + memoryLocation;
		
	}

}

class HexUtil{
    
	//Takes in two hex strings and returns the sum as a hex string
	public static String addHex(String num1, String num2){
		String result;
		long doubleNum1 = hexToDecimal(num1);
		long doubleNum2 = hexToDecimal(num2);
		long resultdouble = doubleNum1 + doubleNum2;
		result = decimalToHex(resultdouble,false);
		
		return result;
		
	}
	
	//Takes two hex strings and subtracts them, returning a hex string
	public static String subHex(String num1, String num2){
		String result;
		boolean negative = false;
		long doubleNum1 = hexToDecimal(num1);
		long doubleNum2 = hexToDecimal(num2);
		long resultdouble = doubleNum1 - doubleNum2;
		if (resultdouble < 0){
			negative = true;
		}
		result = decimalToHex(resultdouble,negative);
		return result;
	}

	//Takes a hex string and returns the decimal equivilent
	public static int hexToDecimal(String hex){
		int result = 0;
		int power = 0;
		for(int i = hex.length() - 1; i >= 0; i--){
			//System.out.println("The character is " + hex.charAt(i) + " and the power is " + power);
			result += Character.getNumericValue(hex.charAt(i)) * Math.pow(16, power);
			power++;
		}
		
		return result;
	}

	//Takes a decimal value and converts it into a hex string
	public static String decimalToHex(long num, boolean negative){
		String result;
                int count = 0;
                result = Long.toHexString(num);
		
                //remove the extra F's since conversion results in 64-bits
                if (negative == true){
                    for (int i = 0; i < result.length(); i++){
                        if (result.charAt(i) == 'f')
                            count++;  
                     }
                result = result.substring(count-1, 16);
                }
                return result;
        }
	
	//Takes hex character and returns decimal value
	public static int getDecimalValue(char hex){
		int result;
		if(hex == 'A'){
			result = 10;
		}else if(hex == 'B'){
			result = 11;
		}else if(hex == 'C'){
			result = 12;
		}else if(hex == 'D'){
			result = 13;
		}else if(hex == 'E'){
			result = 14;
		}else if(hex == 'F'){
			result = 15;
		}else{
			result = Character.getNumericValue(hex);
		}
		return result;
	}

	
	
	public static String buildXBPE(int x, int b, int p, int e){
		//returns the HEX representation of the XBPE half-byte
		StringBuilder xbpeBinaryBuilder = new StringBuilder();
		xbpeBinaryBuilder.append(x);
		xbpeBinaryBuilder.append(b);
		xbpeBinaryBuilder.append(p);
		xbpeBinaryBuilder.append(e);
		int inDecimal = Integer.parseInt(xbpeBinaryBuilder.toString(), 2);
		
		return Integer.toString(inDecimal, 16);
		

	}
	
	public static String buildOpNI(String op, int n, int i){
		//builds the opcode half-byte
		if(n == 0 && i == 1){
			return addHex(op, "1");
		}else if(n == 1 && i == 0){
			return addHex(op, "2");
		}else{
			return addHex(op, "3");
		}
		
		
	}
	
	public static String formatDisplacement(String displacement, int format)
	{
		//pads the displacement with 0's to meet required length. supports format 3 and format 4 instructions.
		int requiredLength = 0;
		StringBuilder fDisp = new StringBuilder();
		if(format == 3){
			requiredLength = 3;
			
		}else if( format == 4){
			requiredLength = 5;
		}else{
			return "-1";
		}
		for( int i = 0; i < requiredLength - displacement.length(); i++){
			fDisp.append("0");
		}
		fDisp.append(displacement);
		return fDisp.toString();
		
	}
	
	
	public static String buildFormat2(String opCode, String r1, String r2){
		StringBuilder codeBuilder = new StringBuilder();
		codeBuilder.append(opCode);
		codeBuilder.append(r1);
		codeBuilder.append(r2);
		
		
		return codeBuilder.toString();
	}
	
	public static String buildFormat3(int n, int i, int x, int e, String opCode, String memoryLoc, String LOCCTR, String BASE){
		//returns -1 if unable to use base of pc relative
		String machineCode;
		int b = 0, p = 0;
		
		String displacement = "";
		
		StringBuilder codeBuilder = new StringBuilder();
	
		int pcDec = hexToDecimal(LOCCTR);
		int memLocDec = hexToDecimal(memoryLoc);
		int displacementDec = memLocDec - pcDec;
		if(displacementDec <=2047 && displacementDec >= -2048){
			b = 0;
			p = 1;
		}else{
			displacementDec = memLocDec - hexToDecimal(BASE);
			if( displacementDec>= 0 && displacementDec <= 4095){
				b = 1;
				p = 0;
			}else{
				
				return "-1";
			}
		
		}

		codeBuilder.append(buildOpNI(opCode, n, i));
		String xbpe = buildXBPE(x, b, p, e);
		codeBuilder.append(xbpe);
		
		if(b == 0 && p == 1){
			displacement = subHex(memoryLoc, LOCCTR);
		}else if(b == 1 && p == 0){
			displacement = subHex(memoryLoc, BASE);
		}
		
		
		
		codeBuilder.append(formatDisplacement(displacement, 3));
		machineCode = codeBuilder.toString();
		
		return machineCode;
		
	}
	
	public static String buildFormat4(int n, int i, int x, int e, String opCode, String memoryLoc){
		int b = 0, p = 0;
		StringBuilder codeBuilder = new StringBuilder();
		
		codeBuilder.append(buildOpNI(opCode, n, i));
		codeBuilder.append(buildXBPE(x, b, p, e));
		codeBuilder.append(formatDisplacement(memoryLoc, 4));
		return codeBuilder.toString();
		
	}
	
	
	
	
		
	
       
	public static String buildMachineCode(int n, int i, int x, int e, String opCode, String memoryLoc, String LOCCTR, String BASE, int format){
		//returns -1 if unable to use base of pc relative
		//format 2, for now, uses memoryLoc as r1 and BASE as r2
		String machineCode = "-2";
		if(format == 1){
			machineCode = opCode;
		}else if(format == 2){
			machineCode = buildFormat2(opCode, memoryLoc, BASE);
		}else if(format == 3){
			machineCode = buildFormat3(n, i, x, e, opCode, memoryLoc, LOCCTR, BASE);
		}else if(format == 4){
			machineCode = buildFormat4(n, i, x, e, opCode, memoryLoc);
		}
		
		return machineCode.toUpperCase();
	}
}

