
//Hex conversion, addition, subtraction

public class HexTester{
	public static void main(String args[]){
		System.out.println(HexUtil.addHex("101A","2FAC"));
		System.out.println(HexUtil.hexToDecimal("1C"));
		System.out.println(HexUtil.decimalToHex(-10000, true));
		System.out.println(HexUtil.getDecimalValue('F'));
		System.out.println(HexUtil.subHex("0006", "001A"));
		System.out.println(HexUtil.buildMachineCode(1, 1, 1, 0, "54", "36", "104E", "33", 3));
		System.out.println(HexUtil.buildMachineCode(1, 1, 0, 0, "74", "FFFF", "020", "33", 3));
		System.out.println(HexUtil.buildMachineCode(0, 1, 0, 0, "68", "0033", "0006", "33", 3));
		System.out.println(HexUtil.buildMachineCode(1, 1, 0, 1, "48", "1036", "000A", "33", 4));
		
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
		
		
		return codeBuilder.toString().toUpperCase();
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
		
		return machineCode.toUpperCase();
		
	}
	
	public static String buildFormat4(int n, int i, int x, int e, String opCode, String memoryLoc){
		int b = 0, p = 0;
		StringBuilder codeBuilder = new StringBuilder();
		
		codeBuilder.append(buildOpNI(opCode, n, i));
		codeBuilder.append(buildXBPE(x, b, p, e));
		codeBuilder.append(formatDisplacement(memoryLoc, 4));
		return codeBuilder.toString().toUpperCase();
		
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
		
		return machineCode;
	}
}
