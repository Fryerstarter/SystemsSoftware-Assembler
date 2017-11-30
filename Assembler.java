
import java.util.*;
import java.io.*;
/**
 * @author Emily Czarnecki, Brennan Ledbetter, Christopher Kile
 * COP3404 Fall 2017
 * DUE: NOVEMBER 30 2017
 * Build a two pass assembler that assembles SIC/XE instructions
 * TODO
 * handle # instructions
 * 2's complement bug
 * mod records for # and format 4 instructions
 * starting address wrong
 * printing textlines incorrectly
 * toUpperCase when searching OPTAB but not SYMTAB
 * NOT INLUCDED:
 *  -literals & floating point instructions
 *  -EQU, USE, CSECT directives
 *  -HIO,LPS,SKK,STI,STSW,SVC,SIO,TIO instructions
 *  -Macros and program blocks
 *  need to handle blank lines in input file
 */
public class Assembler {
    /**
     * MAIN METHOD: calls and runs pass one and pass two
     * @param args 
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Must provide source file as command line argument");
            System.exit(0);
        }
        //create SYMTAB and OBTAB hash tables
        Hashtable<String, Symbol> SYMTAB = new Hashtable<String, Symbol>();
        Hashtable<String, Operation> OPTAB = Operation.buildOPTAB();
        
        //run Pass One
        System.out.println("Running pass 1.");
        String LOCCTR = passOne(args[0], SYMTAB, OPTAB);
        System.out.println("Pass 1 complete.");
        //if -1 returned source code has errors
        if(LOCCTR.equals("-1")){
            System.out.println("Error in pass 1. No object code generated.\nPlease review the .lst file for errors");
        //if no errors, run Pass Two
        }else{
            System.out.println("Running pass 2.");
        	
            int error = passTwo(args[0], LOCCTR, SYMTAB, OPTAB);
            System.out.println("Pass 2 done.");
            //System.out.println("error value is " + error);
            if(error == -1){
            	System.out.println("Object code generation failed in pass 2, please review source file.");
            }
        }
    }
    /**
     * Pass One of the Assembler - reads source file and generates an intermediate file that builds
     * location addresses for each line of source code
     * @param filename
     * @param SYMTAB
     * @param OPTAB
     * @return 
     */
    public static String passOne(String filename, Hashtable<String, Symbol> SYMTAB, Hashtable<String, Operation> OPTAB) {
        //variable declarations
        String input, LOCCTR = "0000";
        String[] cLine = new String[2];
        boolean error = false, format4 = false;
        int lineNum = 0,format = 3;
        Line currentLine;
        
        try {
            BufferedReader inputFile = new BufferedReader(new FileReader(filename));
            BufferedWriter intermediateFile = new BufferedWriter(new FileWriter(filename + ".lst"));
            
            //read in first line
            input = inputFile.readLine();
            
            if (input == null) {
                System.exit(0);
            }
            //ignores processing any lines that start with '.' indicating a comment, just writes to .lst file
            while (input.startsWith(".")){
                intermediateFile.write(lineNum + "\t" + input);
                intermediateFile.newLine();
                input = inputFile.readLine();
            }
            //parse out source code if line contains a comment
            if (input.contains(".")){
                cLine = parseComment(input);
                currentLine = LineParser.parseLinePassOne(cLine[0]);
            }
            else
                currentLine = LineParser.parseLinePassOne(input);
            
            //increase line count
            lineNum++;
            
            if (currentLine.getOPCODE().equals("START")) {
                LOCCTR = HexUtil.formatLOCCTR(currentLine.getOPERAND());
            } else {
                LOCCTR = "0000";
            }
            //write line to .lst file
            intermediateFile.write(intermediateLine(LOCCTR, lineNum, input));
            intermediateFile.newLine();
            
            while ((input = inputFile.readLine()) != null) {
                String tempInput;
                tempInput = input.replaceAll("\\s+", "");
                if (tempInput.length() > 0) {
                    while (tempInput.startsWith(".")) {
                    	tempInput = input.replaceAll("\\s+", "");
                        lineNum++;
                        intermediateFile.write(lineNum + "\t" + input);
                        intermediateFile.newLine();
                        input = inputFile.readLine();
                        tempInput = input.replaceAll("\\s+", "");
                        while(!(tempInput.length() > 0)){
                        	input = inputFile.readLine();
                            tempInput = input.replaceAll("\\s+", "");
                        }
                    }
                    if (input.contains(".")) {
                        cLine = parseComment(input);
                        currentLine = LineParser.parseLinePassOne(cLine[0]);
                    } else
                        currentLine = LineParser.parseLinePassOne(input);

                    if (currentLine.getOPCODE().equals("END")){
                    	 intermediateFile.write("\t" + input);
                    		intermediateFile.newLine();
                        break;
                    }

                    if (input.contains("+")) {
                        format4 = true;
                    }

                    lineNum++;

                    //check for label in the line
                    if (currentLine.getSYMBOL() != null) {
                        Symbol currentSymbol = SYMTAB.get(currentLine.getSYMBOL());

                        if (SYMTAB.get(currentLine.getSYMBOL()) != null && !(currentLine.getSYMBOL().equals("")) && !(currentLine.getSYMBOL().isEmpty())) {

                            //SYMBOL already in SYMTAB, set error
                            error = true;

                            intermediateFile.write("Duplicate symbol: " + currentLine.getSYMBOL().toUpperCase() + " on line " + lineNum);
                            intermediateFile.newLine();
                        } else {

                            //SYMBOL not found in SYMTAB, enter into SYMTAB

                            SYMTAB.put(currentLine.getSYMBOL().toUpperCase(), new Symbol(currentLine.getSYMBOL().toUpperCase(), LOCCTR));
                        }
                    }

                    //save current LOCCTR for memory location
                    String preLOCCTR = LOCCTR;

                    Operation currentOp;

                    //determine what to add to LOCCTR for next instruction

                    if ((currentOp = OPTAB.get(currentLine.getOPCODE().toUpperCase())) != null) {
                        if (currentOp.getFormat() == -1) {
                            intermediateFile.write("Operation on line number " + lineNum + " is not supported.");
                            intermediateFile.newLine();
                            error = true;
                        } else if (format4) {
                            format = 4;
                        } else {
                            format = currentOp.getFormat();
                        }
                        LOCCTR = HexUtil.addHex(LOCCTR, HexUtil.decimalToHex((long) format, false));

                    } else if (currentLine.getOPCODE().equals("WORD")) {
                        LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(3));
                    } else if (currentLine.getOPCODE().equals("RESW")) {
                        int memAdd = 3 * Integer.parseInt(currentLine.getOPERAND());
                        String memAddHex = HexUtil.decimalToHex(memAdd, false);
                        LOCCTR = HexUtil.addHex(LOCCTR, memAddHex);
                    } else if (currentLine.getOPCODE().equals("RESB")) {
                       // System.out.println("RESB found, value of operand is :" + Integer.toString(Integer.parseInt(currentLine.getOPERAND())));
                        LOCCTR = HexUtil.addHex(LOCCTR, HexUtil.decimalToHex(Integer.parseInt(currentLine.getOPERAND()), false));

                    } else if (currentLine.getOPCODE().equals("BYTE")) {
                        LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(1));

                    } else if (currentLine.getOPCODE().equals("END")) {
                        intermediateFile.write("\t" + input);
                        intermediateFile.newLine();
                        break;
                    } else if (currentLine.getOPCODE().equals("EQU") || currentLine.getOPCODE().equals("USE") || currentLine.getOPCODE().equals("CSECT")) {
                        intermediateFile.write("Operation on line number " + lineNum + " is not supported.");
                        intermediateFile.newLine();
                    } else if (currentLine.getOPCODE().equals("BASE")) {

                    } else {

                        error = true;

                        intermediateFile.write("OPCODE on line " + lineNum + " not found. Please verify spelling.");
                        intermediateFile.newLine();

                    }

                    LOCCTR = HexUtil.formatLOCCTR(LOCCTR);

                    //write line to .lst
                    intermediateFile.write(intermediateLine(preLOCCTR, lineNum, input));
                    intermediateFile.newLine();


                    format4 = false;
                    // END OF FILE READ
                }
            }
            intermediateFile.close();
            inputFile.close();
        } catch (Exception e) {
            System.out.println("An unexpected error has occurred. Please try again.");
            //e.printStackTrace();
        }
        if(error == true){
            return "-1";
        }
        return LOCCTR;
    }
    /**
     * Pass two of the assembler - reads in lines from intermediate file and generates object code
     * @param filename
     * @param LOCCTR
     * @param SYMTAB
     * @param OPTAB 
     */
    public static int passTwo(String filename, String LOCCTR, Hashtable<String, Symbol> SYMTAB, Hashtable<String, Operation> OPTAB) {
        //variable declarations
        int lineNum = 0, format;
        String objectCode = "", operand = "",BASE = "", currentStartingAddress="",lengthOfObjectCodeInHalfBytes = "0",currentTextRecordLength = "0000";
        String[] cLine = new String[2];
        StringBuilder textRecord = new StringBuilder();
        boolean error = false, isBase = false, codeBreak = false, lineFull = true, newStart = false;
        ArrayList<String> modificationLines = new ArrayList<>();
        Symbol currentSymbol;
       
        try {
            
            BufferedReader intermediateFile = new BufferedReader(new FileReader(filename + ".lst"));
            BufferedWriter objectFile = new BufferedWriter(new FileWriter(filename + ".obj"));
            
            //read in first line
            String input = intermediateFile.readLine();
            
            //check for comments in line
            while (input != null && input.contains(".")){
                cLine = parseComment(input);
                String trim = cLine[0].trim();
                //skips line if only a comment
                if(Integer.toString(lineNum).equals(trim)){
                    input = intermediateFile.readLine();
                }
                //parses only source code
                else{
                    IntermediateLine line = LineParser.parseLinePassTwo(cLine[0]);
                }
            }
            lineNum++;
            IntermediateLine line = LineParser.parseLinePassTwo(input);
            String startLoc = "000000";
            
            //check if first line contains start
            if (line.getOPCODE().equals("START")){
                String progName = line.getSYMBOL();
                startLoc = line.getOPERAND();
                String length = HexUtil.subHex(LOCCTR, startLoc);
                String headerRecord = headerLine(progName, startLoc, length);
                objectFile.write(headerRecord.toUpperCase());
                objectFile.newLine();

            } else {
                startLoc = "000000"; //set starting location to 0 if there is no start line
            }

            //while loop that reads file until it encounters the end
            while ((input = intermediateFile.readLine()) != null) {
                codeBreak = false;
                String tempInput;
                tempInput = input.replaceAll("\\s+", "");
                if (tempInput.length() > 0){
                    //increase line count
                    lineNum++;

                //check if line is comment
                if (input.contains(".")) {
                    cLine = parseComment(input);
                    String trim = cLine[0].trim();
                    //skips line if entire line is a comment
                    if (Integer.toString(lineNum).equals(trim)) {
                        input = intermediateFile.readLine();
                        line = LineParser.parseLinePassTwo(input);
                    }
                    //parses only source code
                    else
                        line = LineParser.parseLinePassTwo(cLine[0]);

                } else
                    line = LineParser.parseLinePassTwo(input);

                //if after parsing the OPCODE = END - break from while loop
                if (line.getOPCODE().equals("END"))
                    break;

                if(newStart){
                	currentStartingAddress = line.LOCCTR;
                	newStart = false;
                }
                
                if (line.OPCODE.equals("BASE")) {
                    BASE = SYMTAB.get(line.OPERAND).getMemoryLocation();
                    //check if OPCODE is in OPTAB
                } else if (OPTAB.get(line.getOPCODE()) != null) {

                    if (line.getOPERAND() != null) {
                        //symbol found in SYMTAB
                        if ((currentSymbol = SYMTAB.get(line.getOPERAND())) != null) {
                            operand = currentSymbol.getMemoryLocation();
                        } else if (HexUtil.isRegister(line.getOPERAND())) {
                            operand = HexUtil.getRegister(line.getOPERAND());
                        } else if (!input.contains("#")) {

                            System.out.println("ERROR!!!! SETTING operand to 0000 on line num " + lineNum);
                            operand = "0000";
                            error = true;
                        }
                        //end has Symbol
                    } else {
                        System.out.println("SETTING OPERAND TO 0000 on line " + lineNum);
                        operand = "0000";
                    }
                    format = OPTAB.get(line.getOPCODE()).getFormat();
                    if (line.getFormat4()) {
                        format = 4;
                    }
                    if (line.getOPCODE().equals("BASE")) {
                        BASE = SYMTAB.get(line.getOPERAND()).getMemoryLocation();
                        isBase = true;
                    } else if (line.OPCODE.equals("RSUB")) {
                        objectCode = "4F0000";
                        lengthOfObjectCodeInHalfBytes = "6";
                    } else {
                        //test for break in code to force new text record. occurs after arrays and variables			
                        if (format != -1 && !isBase) {
                            int n = 0, i = 0, x = 0, e = 0;
                            if (format == 1) {
                                lengthOfObjectCodeInHalfBytes = "2";
                            } else if (format == 2) {
                                if (line.OPERAND2 != null) {
                                    //need a way to split registers or identify single register
                                    objectCode = HexUtil.buildFormat2(OPTAB.get(line.OPCODE).getOpCode(), line.OPERAND, line.OPERAND2);
                                } else {
                                    objectCode = HexUtil.buildFormat2(OPTAB.get(line.OPCODE).getOpCode(), line.OPERAND, "");
                                }
                                lengthOfObjectCodeInHalfBytes = "4";
                            } else if (format == 3) {
                                //need a way to identify index
                                if (input.contains(",")) {
                                    x = 1;
                                }

                                if (input.contains("#")) {
                                    if (SYMTAB.get(line.OPERAND) != null) {
                                        operand = SYMTAB.get(line.OPERAND).getMemoryLocation();
                                        //System.out.println("Searching in SYMTAB for OPERAND: " + SYMTAB.get(line.OPERAND).getLabel() + " " + SYMTAB.get(line.OPERAND).getMemoryLocation());
                                        n = 0;
                                        i = 1;
                                        objectCode = HexUtil.buildFormat3(n, i, x, 0, OPTAB.get(line.OPCODE).getOpCode(), operand, HexUtil.addHex(line.LOCCTR, "3"), BASE, lineNum);
                                    } else {
                                        n = 0;
                                        i = 1;
                                        String temp;
                                        temp = HexUtil.decimalToHex(Integer.parseInt(line.OPERAND), false);
                                        operand = temp;
                                        objectCode = HexUtil.buildFormat3Direct(n, i, 0, OPTAB.get(line.OPCODE).getOpCode(), operand);
                                    }
                                    //System.out.println("Op for line " + lineNum + " is " + OPTAB.get(line.OPCODE).getOpCode());

                                } else {
                                    n = 1;
                                    i = 1;
                                    objectCode = HexUtil.buildFormat3(n, i, x, 0, OPTAB.get(line.OPCODE).getOpCode(), operand, HexUtil.addHex(line.LOCCTR, "3"), BASE, lineNum);
                                }

                                lengthOfObjectCodeInHalfBytes = "6";

                            } else if (format == 4) {

                                if (input.contains(",")) {
                                    x = 1;
                                }

                                if (input.contains("#")) {
                                    if (SYMTAB.get(line.OPERAND) != null) {
                                        operand = SYMTAB.get(line.OPERAND).getMemoryLocation();
                                        n = 0;
                                        i = 1;
                                        modificationLines.add(modLine(line.LOCCTR));
                                    } else {
                                        n=0;
                                        i=1;
                                        String temp;
                                        temp = HexUtil.decimalToHex(Integer.parseInt(line.OPERAND), false);
                                        operand = temp;
                                    }
                                    objectCode = HexUtil.buildFormat4(n, i, x, 1, OPTAB.get(line.OPCODE).getOpCode(), operand);
                                } else {
                                    n = 1;
                                    i = 1;
                                    objectCode = HexUtil.buildFormat4(n, i, x, 1, OPTAB.get(line.OPCODE).getOpCode(), operand);
                                    modificationLines.add(modLine(line.LOCCTR));
                                }

                                lengthOfObjectCodeInHalfBytes = "8";

                            }
                        }
                    }
                    if (HexUtil.hexToDecimal((HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes))) > 60) {
                        //create new text record and write current to .obj file
                        if (objectCode.equals("-1")) {
                            error = true;
                        }
                        String textLine = textLine(currentStartingAddress, textRecord.toString());
                        textRecord = new StringBuilder();
                        currentTextRecordLength = "0";
                        objectFile.write(textLine);
                        objectFile.newLine();
                        lineFull = true;
                        codeBreak = false;
                        textRecord.append(objectCode.toUpperCase());
                        currentStartingAddress = line.LOCCTR;
                        currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                        currentTextRecordLength = HexUtil.formatDisplacement(currentTextRecordLength, 2);
                       // System.out.println("Object code for line " + lineNum + " is: " + objectCode.toUpperCase());
                    } else {
                        currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                        //System.out.println("Object code for line " + lineNum + " is: " + objectCode.toUpperCase());
                        textRecord.append(objectCode.toUpperCase());
                    }
                    //end if opcode is in OPTAB
                } else if (line.getOPCODE().equals("WORD")) {
                    objectCode = HexUtil.formatAddress(HexUtil.decimalToHex(Integer.parseInt(line.OPERAND), false));
                    //System.out.println("Object code for line " + lineNum + " is: " + objectCode.toUpperCase());
                    lengthOfObjectCodeInHalfBytes = "6";
                    //machine code = hex rep of decimal
                    if (objectCode.equals("-1")) {
                        error = true;
                    }
                    if (HexUtil.hexToDecimal((HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes))) > 60) {
                        //create new text record and write current to .obj file
                        if (objectCode.equals("-1")) {
                            error = true;
                        }
                        String textLine = textLine(currentStartingAddress, textRecord.toString());
                        textRecord = new StringBuilder();
                        currentTextRecordLength = "0";
                        objectFile.write(textLine);
                        objectFile.newLine();
                        lineFull = true;
                        codeBreak = false;
                        textRecord.append(objectCode.toUpperCase());
                        currentStartingAddress = line.LOCCTR;
                        currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                        currentTextRecordLength = HexUtil.formatDisplacement(currentTextRecordLength, 2);
                        //System.out.println("Object code for line " + lineNum + " is: " + objectCode.toUpperCase());
                    } else {
                        currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                        textRecord.append(objectCode.toUpperCase());
                    }

                } else if (line.getOPCODE().equals("RESW") || line.getOPCODE().equals("RESB")) {
                    //indicates code break, no machine code generated
                    codeBreak = true;
                    newStart = true;
                } else {
                    //operation unsupported
                    System.out.println("Operation unsupported, error! " + line.getOPCODE() + " " + lineNum);
                    error = true;
                }

                if (codeBreak && !currentTextRecordLength.equals("0") && !lineFull) {
                    String textLine = textLine(currentStartingAddress, textRecord.toString());
                    textRecord = new StringBuilder();
                    currentTextRecordLength = "0";
                    objectFile.write(textLine);
                    objectFile.newLine();
                    currentStartingAddress = line.LOCCTR;
                    lineFull = true;
                    codeBreak = false;
                }
                    if (lineFull || currentTextRecordLength.equals("0")) {
                        currentStartingAddress = line.LOCCTR;
                        lineFull = false;
                        codeBreak = false;
                    }

            }
            //end while loop reading input       
            }
			 
            for(int i = 0; i < modificationLines.size(); i++){
                objectFile.write(modificationLines.get(i).toUpperCase());
                objectFile.newLine();
            }
            
            //write end line
            objectFile.write(endLine(startLoc).toUpperCase());
            //close file
            intermediateFile.close();
            objectFile.close();
            
        //end of try    
        } catch (Exception e2) {
            System.out.println("An unexpected error has occurred on line " + lineNum +". Please try again.");
            e2.printStackTrace();
        }
        if(error == true){
        	return -1;
        }else{
        	return 0;
        }
    //END PASS 2
    }
    /**
     * Method to parse souce code and comment and story in string array
     * @param line
     * @return 
     */
    public static String[] parseComment(String line){
        String[] cLine = new String[2];
        int index = line.indexOf(".");
        String line1 = line.substring(0, index);
        String line2 = line.substring(index, line.length());
        cLine[0] = line1;
        cLine[1] = line2;
        return cLine;
    }
    /**
     * Build header line for object program
     * @param progName
     * @param startingAddress
     * @param progLength
     * @return 
     */
    public static String headerLine(String progName, String startingAddress, String progLength){
       StringBuilder builder = new StringBuilder();
       builder.append("H");
       builder.append(progName);

       for(int i = 0; i < 6 - progName.length(); i++){
           builder.append(" ");
       }
       startingAddress = HexUtil.formatAddress(startingAddress);
      // System.out.println("Starting address is " + startingAddress);
       builder.append(startingAddress);
       progLength = HexUtil.formatAddress(progLength);
      // System.out.println("Prog length is " + progLength);
       builder.append(progLength);
       return builder.toString();
    }
    /**
     * Build text line for object program
     * @param startingAddress
     * @param objectCode
     * @return 
     */
    public static String textLine(String startingAddress, String objectCode){
        StringBuilder builder = new StringBuilder();
        builder.append("T");
        builder.append(HexUtil.formatAddress(startingAddress));
        builder.append(HexUtil.formatDisplacement(HexUtil.decimalToHex((objectCode.length() / 2), false).toUpperCase(), 2));
        builder.append(objectCode);
        return builder.toString();
        
    }
    /**
     * build endline for object program
     * @param startingAddress
     * @return 
     */
    public static String endLine(String startingAddress){
        StringBuilder builder = new StringBuilder();
        builder.append("E");
        builder.append(HexUtil.formatAddress(startingAddress));
        return builder.toString();
    }
    /**
     * build modification lines for object program
     * @param startingAddress
     * @return 
     */
    public static String modLine(String startingAddress){
        StringBuilder builder = new StringBuilder();
        builder.append("M");
        builder.append(HexUtil.formatAddress(HexUtil.addHex(startingAddress, "1")));
        builder.append("05");
        return builder.toString();   
    }
    /**
     * builds line to be written in intermediate file created in pass one
     * @param LOCCTR
     * @param lineNum
     * @param currentLine
     * @return 
     */
    public static String intermediateLine(String LOCCTR, int lineNum, String currentLine) {
        //builds the line for writing to file
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(Integer.toString(lineNum));
        lineBuilder.append("\t");
        lineBuilder.append(LOCCTR);
        lineBuilder.append("\t");
        lineBuilder.append(currentLine);
        return lineBuilder.toString().toUpperCase();
    }
}
/**
 * Class to parse lines from source code for pass one and from intermediate file for pass two
 */
class LineParser {
    //used to parse each line of input and determine format 3/4

    public static IntermediateLine parseLinePassTwo(String input) {
        IntermediateLine line = null;
        boolean isDirect = false;
        try {
            String LOCCTR = "";
            String SYMBOL = "";
            String OPCODE = "";
            String TARGET = "";

            boolean format4 = false;
            String[] splitArray = input.split("\\s+");
            //System.out.print("Array size is: " + splitArray.length + "\n");
            //if(input.contains("+"))
            //format4 = true;
            if(splitArray[1].equals("END")){
                OPCODE = "END";
                line = new IntermediateLine(LOCCTR, OPCODE, TARGET);
            }else if (splitArray.length == 5) {
                LOCCTR = splitArray[1];
                SYMBOL = splitArray[2];
                OPCODE = splitArray[3];
                TARGET = splitArray[4];
                

                if (OPCODE.contains("+")) {
                    OPCODE = OPCODE.substring(1, OPCODE.length());
                    format4 = true;
                }
                if (TARGET.contains("#")) {
                    TARGET = TARGET.substring(1, TARGET.length());
                    isDirect = true;
                }
                if (TARGET.contains(",")) {
                    String[] target = TARGET.split(",");
                    String TARGET1 = target[0];
                    String TARGET2 = target[1];
                    if(HexUtil.isRegister(TARGET1)){
                       // System.out.println("ITS A REGISTER!");
                        line = new IntermediateLine(LOCCTR, OPCODE, TARGET1, TARGET2);
                    }else{
                        line = new IntermediateLine(LOCCTR, SYMBOL, OPCODE, TARGET1);
                    }

                   // System.out.print("TARGET is: " + TARGET1 + " " + TARGET2 + "\n");
                } else {
                    line = new IntermediateLine(LOCCTR, SYMBOL, OPCODE, TARGET);
                    //System.out.print("LOCCTR: " + LOCCTR + " OPCODE: " + OPCODE + " TARGET: " + TARGET + " Format 4 is: " + format4 + "\n");
                }

            } else if (splitArray.length == 4) {
                SYMBOL = null;
                LOCCTR = splitArray[1];
                OPCODE = splitArray[2];
                TARGET = splitArray[3];

                if (OPCODE.contains("+")) {
                    OPCODE = OPCODE.substring(1, OPCODE.length());
                    format4 = true;
                }
                if (TARGET.contains("#")) {
                    TARGET = TARGET.substring(1, TARGET.length());
                    isDirect = true;
                }
                if (TARGET.contains(",")) {
                    String[] target = TARGET.split(",");
                    String TARGET1 = target[0];
                    String TARGET2 = target[1];
                    if(HexUtil.isRegister(TARGET1)){
                      //  System.out.println("ITS A REGISTER!");
                        line = new IntermediateLine(LOCCTR, "", OPCODE, TARGET1, TARGET2);
                    }else{
                        line = new IntermediateLine(LOCCTR, SYMBOL, OPCODE, TARGET1);
                    }
                    //line = new IntermediateLine(LOCCTR, OPCODE, TARGET1, TARGET2);
                    //System.out.print("LOCCTR: " + LOCCTR + " OPCODE: " + OPCODE + " TARGETs are: " + TARGET1 + " " + TARGET2 + "\n");
                } else {
                    line = new IntermediateLine(LOCCTR, OPCODE, TARGET);
                    //System.out.print("LOCCTR: " + LOCCTR + " OPCODE: " + OPCODE + " TARGET: " + TARGET + " Format 4 is: " + format4 + "\n");
                }

            } else if (splitArray.length == 3) {
                LOCCTR = splitArray[1];
                OPCODE = splitArray[2];
                //System.out.print("LOCCTR: " + LOCCTR + " OPCODE: " + OPCODE + "\n");
                line = new IntermediateLine(LOCCTR, OPCODE, TARGET);
            }
            line.setFormat4(format4);
            line.setIsDirect(isDirect);

        } catch (Exception e) {

        }

        return line;

    }



    public static Line parseLinePassOne(String input) {
        Line line = null;
        try {
            String SYMBOL = "";
            String OPCODE = "";
            String TARGET = "";

            boolean format4 = false;
            String[] splitArray = input.split("\\s+");
            if (input.contains("+")) {
                format4 = true;
            }
            if (splitArray.length == 3) {
                SYMBOL = splitArray[0];
                OPCODE = splitArray[1];
                TARGET = splitArray[2];
                if (OPCODE.contains("+")) {
                    OPCODE = OPCODE.substring(1, OPCODE.length());
                    format4 = true;
                }

                line = new Line(SYMBOL, OPCODE, TARGET);

            } else if (splitArray.length == 2) {
				boolean res = false;
				if(OPCODE.equals("WORD") || OPCODE.equals("RESW") || OPCODE.equals("RESB") || OPCODE.equals("BYTE")){
					res = true;
				}else{
					SYMBOL = null;
					OPCODE = splitArray[0];
					TARGET = splitArray[1];
					if (TARGET.equals("RSUB")) {
						OPCODE = "RSUB";
					}
					if (OPCODE.contains("+")) {
						OPCODE = OPCODE.substring(1, OPCODE.length());
						format4 = true;
					}
					
				}
				line = new Line(OPCODE, TARGET, res);
            } else if (splitArray.length == 1) {
                OPCODE = splitArray[0];
            }
			
            line.setFormat4(format4);
		
        } catch (Exception e) {

        }
        return line;
    }

}

class IntermediateLine {

    String OPCODE, OPERAND, OPERAND2, SYMBOL, LOCCTR;
    boolean format4 = false;
    boolean isDirect = false;

    public IntermediateLine(String LOCCTR, String OPCODE, String OPERAND) {
        this.LOCCTR = LOCCTR;
        this.SYMBOL = null;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
        this.OPERAND2 = null;
    }

    public IntermediateLine(String LOCCTR, String SYMBOL, String OPCODE, String OPERAND) {
        this.LOCCTR = LOCCTR;
        this.SYMBOL = SYMBOL;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
        this.OPERAND2 = null;
    }

    public IntermediateLine(String LOCCTR, String SYMBOL, String OPCODE, String OPERAND, String OPERAND2) {
        this.LOCCTR = LOCCTR;
        this.SYMBOL = SYMBOL;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
        this.OPERAND2 = OPERAND2;
    }

    public String toString() {
        return "LOCCTR is " + LOCCTR + " OPCODE is " + OPCODE + " SYMBOL is " + SYMBOL + " OPERAND is " + OPERAND;
    }

    public String getSYMBOL() {
        return SYMBOL;
    }

    public String getOPCODE() {
        return OPCODE;
    }

    public String getOPERAND() {
        return OPERAND;
    }

    public String getLOCCTR() {
        return LOCCTR;
    }

    public void setLOCCTR(String LOCCTR) {
        this.LOCCTR = LOCCTR;
    }

    public boolean getFormat4() {
        return format4;
    }

    public void setFormat4(boolean format4) {
        this.format4 = format4;
    }

    public void setIsDirect(boolean isDirect) {
        this.isDirect = isDirect;
    }
}

class Line {
    //used to store important values as each line is processed

    String OPCODE, OPERAND, SYMBOL, LOCCTR;
    boolean format4 = false;
    boolean isDirect = false;



    public Line(String OPCODE, String OPERAND, boolean res) {
		if(res){
			this.SYMBOL = OPERAND;
			this.OPCODE = OPCODE;
		}else{
			this.SYMBOL = null;
			this.OPCODE = OPCODE;
			this.OPERAND = OPERAND;
		}
    }

    public Line(String SYMBOL, String OPCODE, String OPERAND) {
        this.SYMBOL = SYMBOL;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
    }

    public void setIsDirect(boolean isDirect){
        this.isDirect = isDirect;
    }

    public String getSYMBOL() {
        return SYMBOL;
    }

    public String getOPCODE() {
        return OPCODE;
    }

    public String getOPERAND() {
        return OPERAND;
    }

    public boolean getFormat4() {
        return format4;
    }

    public void setFormat4(boolean format4) {
        this.format4 = format4;
    }

    public String toString() {
        if (SYMBOL == null) {
            return "SYMBOL   " + " OPCODE:" + OPCODE + " OPERAND: " + OPERAND;
        } else {
            return "SYMBOL: " + SYMBOL + " OPCODE:" + OPCODE + " OPERAND: " + OPERAND + " format4: " + format4;
        }
    }
}

class HexUtil {

    //Takes in two hex strings and returns the sum as a hex string
    public static String addHex(String num1, String num2) {
        String result;
        long doubleNum1 = hexToDecimal(num1);
        long doubleNum2 = hexToDecimal(num2);
        long resultdouble = doubleNum1 + doubleNum2;
        result = decimalToHex(resultdouble, false);

        return result;

    }

    //Takes two hex strings and subtracts them, returning a hex string
    public static String subHex(String num1, String num2) {
        String result;
        boolean negative = false;
        long doubleNum1 = hexToDecimal(num1);
        long doubleNum2 = hexToDecimal(num2);
        long resultdouble = doubleNum1 - doubleNum2;
        if (resultdouble < 0) {
            negative = true;
        }
        result = decimalToHex(resultdouble, negative);
        return result;
    }

    public static String formatLOCCTR(String LOCCTR) {
        int desiredLength = 4;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < desiredLength - LOCCTR.length(); i++) {
            builder.append("0");
        }
        builder.append(LOCCTR);
        return builder.toString();

    }
    public static String formatAddress(String LOCCTR) {
        int desiredLength = 6;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < desiredLength - LOCCTR.length(); i++) {
            builder.append("0");
        }
        builder.append(LOCCTR);
        return builder.toString();

    }
    //Takes a hex string and returns the decimal equivilent

    public static int hexToDecimal(String hex) {
        int result = 0;
        int power = 0;
        for (int i = hex.length() - 1; i >= 0; i--) {

            result += Character.getNumericValue(hex.charAt(i)) * Math.pow(16, power);
            power++;
        }

        return result;
    }

    //Takes a decimal value and converts it into a hex string
    public static String decimalToHex(long num, boolean negative) {
        String result;
        int count = 0;
        result = Long.toHexString(num);

        //remove the extra F's since conversion results in 64-bits
        if (negative == true) {

            result = result.substring(result.length()-3, result.length());

        }
        return result;
    }

    //Takes hex character and returns decimal value
    public static int getDecimalValue(char hex) {
        int result;
        if (hex == 'A') {
            result = 10;
        } else if (hex == 'B') {
            result = 11;
        } else if (hex == 'C') {
            result = 12;
        } else if (hex == 'D') {
            result = 13;
        } else if (hex == 'E') {
            result = 14;
        } else if (hex == 'F') {
            result = 15;
        } else {
            result = Character.getNumericValue(hex);
        }
        return result;
    }

    public static String buildXBPE(int x, int b, int p, int e) {
        //returns the HEX representation of the XBPE half-byte
        StringBuilder xbpeBinaryBuilder = new StringBuilder();
        xbpeBinaryBuilder.append(x);
        xbpeBinaryBuilder.append(b);
        xbpeBinaryBuilder.append(p);
        xbpeBinaryBuilder.append(e);
        int inDecimal = Integer.parseInt(xbpeBinaryBuilder.toString(), 2);

        return Integer.toString(inDecimal, 16);

    }

    public static String buildOpNI(String op, int n, int i) {
        //builds the opcode half-byte
		String temp;
		StringBuilder builder = new StringBuilder();
	
        if (n == 0 && i == 1) {
            op =  addHex(op, "1");
        } else if (n == 1 && i == 0) {
            op = addHex(op, "2");
        } else {
            op = addHex(op, "3");
        }
		
		for(int j = 0; j < 2 - op.length(); j++){
			builder.append("0");
		}
		builder.append(op);

		return builder.toString();
    }

    public static String formatDisplacement(String displacement, int format) {
        //pads the displacement with 0's to meet required length. supports format 3 and format 4 instructions.
        int requiredLength = 0;
        StringBuilder fDisp = new StringBuilder();
        if(format == 2){
        	requiredLength = 2;
        }else if (format == 3) {
            requiredLength = 3;

        } else if (format == 4) {
            requiredLength = 5;
        } else {
            return "-1";
        }
        for (int i = 0; i < requiredLength - displacement.length(); i++) {
            fDisp.append("0");
        }
        fDisp.append(displacement);
        return fDisp.toString();

    }

    public static String buildFormat2(String opCode, String r1, String r2) {
        StringBuilder codeBuilder = new StringBuilder();
        
        codeBuilder.append(opCode);
        codeBuilder.append(getRegister(r1));
        
        if (!r2.equals("")){
            codeBuilder.append(getRegister(r2));
        }
        else
            codeBuilder.append(0);

        return codeBuilder.toString();
    }

    public static boolean isRegister(String r){
        boolean reg = false;
        if (r.equals("A")){
           reg = true;
        }
        else if (r.equals("X")){
            reg = true;
        }
        else if (r.equals("L")){
            reg = true;
        }
        else if (r.equals("B")){
            reg = true;
        }
        else if (r.equals("S")){
            reg = true;
        }
        else if (r.equals("T")){
            reg = true;
        }
        else if (r.equals("PC")){
            reg = true;
        }
        else if (r.equals("SW")){
            reg = true;
        }
        return reg;
    }
    
    public static String getRegister(String r2){
        
        String r = ""; 
        
        if (r2.equals("A")){
            r = "0";
        }
        else if (r2.equals("X")){
            r = "1";
        }
        else if (r2.equals("L")){
            r ="2";
        }
        else if (r2.equals("B")){
            r = "3";
        }
        else if (r2.equals("S")){
            r = "4";
        }
        else if (r2.equals("T")){
            r = "5";
        }
        else if (r2.equals("PC")){
            r = "8";
        }
        else if (r2.equals("SW")){
            r = "9";
        }
        return r;
    }
    
	public static String buildFormat3Direct(int n, int i, int x, String opCode, String memoryLoc){
		String machineCode;
        int b = 0, p = 0;

        String displacement = "";

        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append(buildOpNI(opCode, n, i));
        String xbpe = buildXBPE(x, b, p, 0);
        codeBuilder.append(xbpe);
		for(int j = 0; j < 3 - memoryLoc.length(); j++){
			codeBuilder.append("0");
		}
		//System.out.println("MEMORY LOCATION in DIRECT FORMAT 3 IS: " + memoryLoc);
        codeBuilder.append(memoryLoc);
        machineCode = codeBuilder.toString();

        return machineCode;
		
	}
	
    public static String buildFormat3(int n, int i, int x, int e, String opCode, String memoryLoc, String LOCCTR, String BASE, int lineNum) {
        //returns -1 if unable to use base of pc relative
        String machineCode;
        int b = 0, p = 0;

        String displacement = "";

        StringBuilder codeBuilder = new StringBuilder();

        int pcDec = hexToDecimal(LOCCTR);
        int memLocDec = hexToDecimal(memoryLoc);
        int displacementDec = memLocDec - pcDec;
        if (displacementDec <= 2047 && displacementDec >= -2048) {
           // System.out.println("Using PC relative");
            b = 0;
            p = 1;
        } else {
           // System.out.println("Using Base Relative");
            displacementDec = memLocDec - hexToDecimal(BASE);
            if (displacementDec >= 0 && displacementDec <= 4095) {
                b = 1;
                p = 0;
            } else {
            	System.out.println("Object code generation failed on line " + lineNum + ". Base addressing failed.");
                return "-1";
            }

        }

        codeBuilder.append(buildOpNI(opCode, n, i));
        String xbpe = buildXBPE(x, b, p, e);
        codeBuilder.append(xbpe);

        if (b == 0 && p == 1) {
            displacement = subHex(memoryLoc, LOCCTR);
        } else if (b == 1 && p == 0) {
            displacement = subHex(memoryLoc, BASE);
        }
       // System.out.println("MEMORY LOCATION in REGULAR FORMAT 3 IS: " + memoryLoc);
        codeBuilder.append(formatDisplacement(displacement, 3));
        machineCode = codeBuilder.toString();

        return machineCode;

    }

    public static String buildFormat4(int n, int i, int x, int e, String opCode, String memoryLoc) {
        int b = 0, p = 0;
        StringBuilder codeBuilder = new StringBuilder();

        codeBuilder.append(buildOpNI(opCode, n, i));
        codeBuilder.append(buildXBPE(x, b, p, e));
        codeBuilder.append(formatDisplacement(memoryLoc, 4));
        return codeBuilder.toString();

    }

   
}

class Operation {

    String op;
    String opCode;
    int format;

    public Operation(String op, String opCode, int format) {
        this.op = op;
        this.opCode = opCode;
        this.format = format;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    public String getOpCode() {
        return opCode;
    }

    public int getFormat() {
        return format;
    }

    public static Hashtable<String, Operation> buildOPTAB() {
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
        OPTAB.put("HIO", new Operation("HIO", "F4", -1));
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
        OPTAB.put("LPS", new Operation("LPS", "D0", -1));
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
        OPTAB.put("SIO", new Operation("SIO", "F0", -1));
        OPTAB.put("SSK", new Operation("SSK", "EC", 3));
        OPTAB.put("STA", new Operation("STA", "0C", 3));
        OPTAB.put("STB", new Operation("STB", "78", 3));
        OPTAB.put("STCH", new Operation("STCH", "54", 3));
        OPTAB.put("STF", new Operation("STF", "80", 3));
        OPTAB.put("STI", new Operation("STI", "D4", -1));
        OPTAB.put("STL", new Operation("STL", "14", 3));
        OPTAB.put("STS", new Operation("STS", "7C", -1));
        OPTAB.put("STSW", new Operation("STSW", "E8", -1));
        OPTAB.put("STT", new Operation("STT", "84", 3));
        OPTAB.put("STX", new Operation("STX", "10", 3));
        OPTAB.put("SUB", new Operation("SUB", "1C", 3));
        OPTAB.put("SUBF", new Operation("SUBF", "5C", -1));
        OPTAB.put("SUBR", new Operation("SUBR", "94", 2));
        OPTAB.put("SVC", new Operation("SVC", "B0", -1));
        OPTAB.put("TD", new Operation("TD", "E0", 3));
        OPTAB.put("TIO", new Operation("TIO", "F8", -1));
        OPTAB.put("TIX", new Operation("TIX", "2C", 3));
        OPTAB.put("TIXR", new Operation("TIXR", "B8", 2));
        OPTAB.put("WD", new Operation("WD", "DC", 3));

        return OPTAB;

    }

}

class Symbol {

    String label;
    String memoryLocation;

    public Symbol(String label) {
        this.label = label;
    }

    public Symbol(String label, String memoryLocation) {
        this.label = label;
        this.memoryLocation = memoryLocation;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setMemoryLocation(String memoryLocation) {
        this.memoryLocation = memoryLocation;
    }

    public String getMemoryLocation() {
        return memoryLocation;
    }

    public String toString() {
        return label + " " + memoryLocation;

    }

}
