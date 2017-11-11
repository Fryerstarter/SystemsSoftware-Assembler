package syssoftware_project;
/*
 - * Emily Czarnecki, Brennan Ledbetter, Christopher Kile
 - * 
 - * 11/7/2017 - Work Completed:
 - * -built data structures
 - * -built hex utility functions
 - * -built machinecode assembler
 - * -passOne finished
 - * -passTwo partially implemented
 - * TODO
 - * -handle whitespace for comment lines
 - * -Build passTwo
 - * -test test test test test!
 - * -identify symbols in operand, eg SYMBOL,X     +, #
 */
import java.util.*;
import java.io.*;

public class Assembler {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Must provide source file as command line argument");
            System.exit(0);
        }

        Hashtable<String, Symbol> SYMTAB = new Hashtable<String, Symbol>();
        Hashtable<String, Operation> OPTAB = Operation.buildOPTAB();
        String LOCCTR = passOne(args[0], SYMTAB, OPTAB);
        if(LOCCTR.equals("-1")){
            System.out.println("Error in pass 1");
        }else{
            System.out.println("PASS 2 STARTING");
            passTwo(args[0], LOCCTR, SYMTAB, OPTAB);
            System.out.println("pass 2 done");
        }
    }

    public static String passOne(String filename, Hashtable<String, Symbol> SYMTAB, Hashtable<String, Operation> OPTAB) {
        String input, LOCCTR = "0000";
        boolean error = false;
        int lineNum = 0;
        int format = 3;
        Line currentLine;
        try {

            BufferedReader inputFile = new BufferedReader(new FileReader(filename));
            BufferedWriter intermediateFile = new BufferedWriter(new FileWriter(filename + ".lst"));
            boolean format4 = false;

            boolean isComment = false;

            input = inputFile.readLine();
            if (input == null) {
                System.exit(0);
            }
            lineNum++;
            currentLine = LineParser.parseLinePassOne(input);

            if (currentLine.getOPCODE().equals("START")) {

                LOCCTR = HexUtil.formatLOCCTR(currentLine.getOPERAND());
            } else {
                LOCCTR = "0000";
            }

            intermediateFile.write(intermediateLine(LOCCTR, lineNum, input));
            intermediateFile.newLine();
            while ((input = inputFile.readLine()) != null && !(currentLine.getOPCODE().equals("END"))) {
                currentLine = LineParser.parseLinePassOne(input);
                if (input.contains("+")) {
                    format4 = true;
                }
                lineNum++;
                //check if comment
                if (!isComment) {
                    System.out.println(currentLine.getSYMBOL());
                    //check for label in the line
                    if (currentLine.getSYMBOL() != null) {
                        Symbol currentSymbol = SYMTAB.get(currentLine.getSYMBOL());
                        if (currentSymbol == null) {
                            System.out.println("Not found");
                        } else {
                            System.out.println("Found symbol: " + currentSymbol.getLabel() + " at mem loc: " + currentSymbol.getMemoryLocation());
                        }
                        if (SYMTAB.get(currentLine.getSYMBOL()) != null && !(currentLine.getSYMBOL().equals(""))) {

                            //SYMBOL already in SYMTAB, set error
                            error = true;

                            intermediateFile.write("Duplicate symbol: " + currentLine.getSYMBOL().toUpperCase() + "on line " + lineNum);
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
                    System.out.println("Current SYMBOL is: " + currentLine.getSYMBOL());
                    if ((currentOp = OPTAB.get(currentLine.getOPCODE())) != null) {
                        if (currentOp.getFormat() == -1) {
                            intermediateFile.write("Operation on line number " + lineNum + " is not supported.");
                            intermediateFile.newLine();
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
                        LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(Integer.parseInt(currentLine.getOPERAND())));

                    } else if (currentLine.getOPCODE().equals("BYTE")) {
                        LOCCTR = HexUtil.addHex(LOCCTR, Integer.toString(1));

                    } else if (currentLine.getOPCODE().equals("END")) {
                        intermediateFile.write("\t" + input);
                        intermediateFile.newLine();
                        break;
                    } else if (currentLine.getOPCODE().equals("EQU") || currentLine.getOPCODE().equals("USE") || currentLine.getOPCODE().equals("CSECT")) {
                        intermediateFile.write("Operation on line number " + lineNum + " is not supported.");
                        intermediateFile.newLine();
                    } else if(currentLine.getOPCODE().equals("BASE")){
                   
                    }else{

                        error = true;

                        intermediateFile.write("OPCODE on line " + lineNum + " not found. Please verify spelling.");
                        intermediateFile.newLine();

                    }
                    LOCCTR = HexUtil.formatLOCCTR(LOCCTR);
                    // end of IS COMMENT
                    //write line to .lst
                    intermediateFile.write(intermediateLine(preLOCCTR, lineNum, input));
                    intermediateFile.newLine();
                }

                format4 = false;
                // END OF FILE READ
            }

            intermediateFile.close();
            inputFile.close();
        } catch (Exception e) {
            System.out.println("An unexpected error has occurred. Please try again.");
            e.printStackTrace();
        }
        if(error == true){
            return "-1";
        }
        return LOCCTR;
    }

    public static void passTwo(String filename, String LOCCTR, Hashtable<String, Symbol> SYMTAB, Hashtable<String, Operation> OPTAB) {
        

        try {
            String currentTextRecordLength = "0000";
            StringBuilder textRecord = new StringBuilder();
            //textRecord.append("T");
            String BASE = "";
            boolean error = false;
            boolean isComment = false;
            BufferedReader intermediateFile = new BufferedReader(new FileReader(filename + ".lst"));
            BufferedWriter objectFile = new BufferedWriter(new FileWriter(filename + ".obj"));
            //read first line and parse
            String input = intermediateFile.readLine();
            IntermediateLine line = LineParser.parseLinePassTwo(input);
            String startLoc = "000000";
            if (line.getOPCODE().equals("START")) {
                String progName = line.getSYMBOL();
                startLoc = line.getOPERAND();
                String length = HexUtil.subHex(LOCCTR, startLoc);
                String headerRecord = headerLine(progName, startLoc, length);
                objectFile.write(headerRecord.toUpperCase());
                objectFile.newLine();

            } else {
                   startLoc = "000000";
                //does not start with START
            }

            
                System.out.println("reading lines");
                ArrayList<String> modificationLines = new ArrayList<String>();
                
                boolean isBase = false;
                boolean codeBreak = false;
                int format;
                String objectCode = "";
                String operand;
                String lengthOfObjectCodeInHalfBytes = "0";
                boolean lineFull = true;
                String currentStartingAddress="";
                Symbol currentSymbol;
                //check if OPCODE in OPTAB
                
                while ((input = intermediateFile.readLine()) != null && !(line.getOPCODE().equals("END"))) {
                    System.out.println(line.getOPCODE());
                    line = LineParser.parseLinePassTwo(input);
                    //System.out.println("Still not END");
                    if (!isComment) {
                        //line is not comment
                        if(lineFull){
                            currentStartingAddress = line.LOCCTR;
                            lineFull = false;
                        }
                        System.out.println("CURRENT OPCODE IS " +line.OPCODE);
                        if (OPTAB.get(line.getOPCODE()) != null) {
                            //Line has symbol
                            
                            
                            if (line.getSYMBOL() != null) {
                                //symbol found in SYMTAB

                                if ((currentSymbol = SYMTAB.get(line.getSYMBOL())) != null) {
                                    operand = currentSymbol.getMemoryLocation();
                                } else {
                                    operand = "0000";
                                    error = true;
                                }
                                //end has Symbol
                            } else {
                                operand = "0000";
                            }
                            format = OPTAB.get(line.getOPCODE()).getFormat();
                            if (line.getFormat4()) {
                                format = 4;
                            }
                            if (line.getOPCODE().equals("BASE")) {
                                BASE = SYMTAB.get(line.getOPERAND()).getMemoryLocation();
                                isBase = true;
                            }
                            else{
                                
                                //test for break in code to force new text record. occurs after arrays and variables
                                if (format != -1 && !isBase) {
                                    int n = 0, i = 0, x = 0, e = 0;
                                    if (format == 1) {
                                        lengthOfObjectCodeInHalfBytes = "2";
                                    } else if (format == 2) {
                                        if (line.OPERAND2 != null){
                                        //need a way to split registers or identify single register
                                            objectCode = HexUtil.buildFormat2(line.OPCODE, line.OPERAND, line.OPERAND2);
                                        }
                                        else{
                                           objectCode = HexUtil.buildFormat2(line.OPCODE, line.OPERAND, ""); 
                                        }
                                        lengthOfObjectCodeInHalfBytes = "4";
                                    } else if (format == 3) {
                                        //need a way to identify index
                                        if (input.contains("#")){
                                            n = 0;
                                            i = 1;
                                        }
                                        else{
                                            n=1;
                                            i=1;
                                        }
                                        if (input.contains(",")){
                                            x = 1;
                                        }
                                        
                                        objectCode = HexUtil.buildFormat3(n, i, x, 0, line.OPCODE, operand, LOCCTR, BASE);
                                        lengthOfObjectCodeInHalfBytes = "6";
                                    } else if (format == 4) {
                                         if (input.contains("#")){
                                            n = 0;
                                            i = 1;
                                        }
                                        else{
                                            n=1;
                                            i=1;
                                        }
                                        if (input.contains(",")){
                                            x = 1;
                                        }

                                        lengthOfObjectCodeInHalfBytes = "8";
                                        objectCode = HexUtil.buildFormat4(n, i, x, 1, line.OPCODE, operand);
                                    }

                                }

                                
                            }
                            if (HexUtil.hexToDecimal((HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes))) > 60 || codeBreak) {
                               //create new text record and write current to .obj file

                               String textLine = textLine(currentStartingAddress, textRecord.toString());
                               textRecord = new StringBuilder();
                               objectFile.write(textLine);
                               objectFile.newLine();
                               lineFull = false;
                               codeBreak = false;
                           } else {
                               currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                               textRecord.append(objectCode.toUpperCase());
                           }
                        }else  if(line.getOPCODE().equals("WORD")){
                            objectCode = HexUtil.decimalToHex(Integer.parseInt(line.OPERAND), false);
                            lengthOfObjectCodeInHalfBytes = "6";
                            //machine code = hex rep of decimal
                            if (HexUtil.hexToDecimal((HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes))) > 60 || codeBreak) {
                               //create new text record and write current to .obj file

                               String textLine = textLine(currentStartingAddress, textRecord.toString());
                               textRecord = new StringBuilder();
                               currentTextRecordLength = "0";
                               objectFile.write(textLine);
                               objectFile.newLine();
                               lineFull = false;
                               codeBreak = false;
                           } else {
                               currentTextRecordLength = HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes);
                               textRecord.append(objectCode.toUpperCase());
                           }
                            
                        }else if(line.getOPCODE().equals("RESW") || line.getOPCODE().equals("RESB")){
                           //indicates code break, no machine code generated
                           codeBreak = true;
                        }else
                        {
                            //operation unsupported
                            error = true;
                            
                        }
                        if(codeBreak && textRecord.length() != 0){
                            String textLine = textLine(currentStartingAddress, textRecord.toString());
                            textRecord = new StringBuilder();
                            currentTextRecordLength = "0";
                            objectFile.write(textLine);
                            objectFile.newLine();
                            lineFull = false;
                            codeBreak = false;
                        }
                        
                            
                        
                       
                            
                        //end isComment
                    
                }
             }
                objectFile.write(endLine(startLoc).toUpperCase());
     
                
                                    
            

            System.out.println(line);

            objectFile.close();
        } catch (Exception e2) {
            System.out.println("An unexpected error has occurred. Please try again.");
            e2.printStackTrace();
        }

        //END PASS 2
    }
    
    public static String headerLine(String progName, String progLength, String startingAddress){
       StringBuilder builder = new StringBuilder();
       builder.append("H");
       builder.append(progName);
       System.out.println(progName.length());
       for(int i = 0; i < 6 - progName.length(); i++){
           builder.append(" ");
       }
       startingAddress = HexUtil.formatAddress(startingAddress);
       builder.append(startingAddress);
       progLength = HexUtil.formatAddress(progLength);
       builder.append(progLength);
       return builder.toString();
    }
    
    public static String textLine(String startingAddress, String objectCode){
        StringBuilder builder = new StringBuilder();
        builder.append("T");
        builder.append(HexUtil.formatAddress(startingAddress));
        builder.append(Integer.toString(objectCode.length()));
        builder.append(objectCode);
        return builder.toString();
        
    }
    public static String endLine(String startingAddress){
        StringBuilder builder = new StringBuilder();
        builder.append("E");
        builder.append(HexUtil.formatAddress(startingAddress));
        return builder.toString();
    }
    public static String modLine(String startingAddress){
        StringBuilder builder = new StringBuilder();
        builder.append("M");
        builder.append(HexUtil.formatAddress(HexUtil.addHex(startingAddress, "1")));
        builder.append("05");
        return builder.toString();
        
    }

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

class LineParser {
    //used to parse each line of input and determine format 3/4

    public static IntermediateLine parseLinePassTwo(String input) {
        IntermediateLine line = null;
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
            if(input.contains("END")){
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
                }
                if (TARGET.contains(",")) {
                    String[] target = TARGET.split(",");
                    String TARGET1 = target[0];
                    String TARGET2 = target[1];
                    line = new IntermediateLine(LOCCTR, OPCODE, TARGET1, TARGET2);
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
                }
                if (TARGET.contains(",")) {
                    String[] target = TARGET.split(",");
                    String TARGET1 = target[0];
                    String TARGET2 = target[1];
                    line = new IntermediateLine(LOCCTR, OPCODE, TARGET1, TARGET2);
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
                SYMBOL = null;
                OPCODE = splitArray[0];
                TARGET = splitArray[1];
                if (TARGET.equals("RSUB")) {
                    OPCODE = "RSUB";
                }
                if (OPCODE.contains("+")) {
                    OPCODE = OPCODE.substring(1, OPCODE.length() - 1);
                    format4 = true;
                }
                line = new Line(OPCODE, TARGET);

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
}

class Line {
    //used to store important values as each line is processed

    String OPCODE, OPERAND, SYMBOL, LOCCTR;
    boolean format4 = false;

    public Line(String OPCODE, String OPERAND) {
        this.SYMBOL = null;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
    }

    public Line(String SYMBOL, String OPCODE, String OPERAND) {
        this.SYMBOL = SYMBOL;
        this.OPCODE = OPCODE;
        this.OPERAND = OPERAND;
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
            //System.out.println("The character is " + hex.charAt(i) + " and the power is " + power);
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
            for (int i = 0; i < result.length(); i++) {
                if (result.charAt(i) == 'f') {
                    count++;
                }
            }
            result = result.substring(count - 1, 16);
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
        if (n == 0 && i == 1) {
            return addHex(op, "1");
        } else if (n == 1 && i == 0) {
            return addHex(op, "2");
        } else {
            return addHex(op, "3");
        }

    }

    public static String formatDisplacement(String displacement, int format) {
        //pads the displacement with 0's to meet required length. supports format 3 and format 4 instructions.
        int requiredLength = 0;
        StringBuilder fDisp = new StringBuilder();
        if (format == 3) {
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
    

    public static String buildFormat3(int n, int i, int x, int e, String opCode, String memoryLoc, String LOCCTR, String BASE) {
        //returns -1 if unable to use base of pc relative
        String machineCode;
        int b = 0, p = 0;

        String displacement = "";

        StringBuilder codeBuilder = new StringBuilder();

        int pcDec = hexToDecimal(LOCCTR);
        int memLocDec = hexToDecimal(memoryLoc);
        int displacementDec = memLocDec - pcDec;
        if (displacementDec <= 2047 && displacementDec >= -2048) {
            b = 0;
            p = 1;
        } else {
            displacementDec = memLocDec - hexToDecimal(BASE);
            if (displacementDec >= 0 && displacementDec <= 4095) {
                b = 1;
                p = 0;
            } else {

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
