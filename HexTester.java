/*
TODO:

1. implement two's complement for negative hex values
2. Subtraction only works if result is positive


*/

public class HexTester{
	public static void main(String args[]){
		System.out.println(HexUtil.addHex("101A","2FAC"));
		System.out.println(HexUtil.hexToDecimal("ABBF123A"));
		System.out.println(HexUtil.decimalToHex(11089753, false));
		System.out.println(HexUtil.getHexValue(14));
		System.out.println(HexUtil.getDecimalValue('F'));
		System.out.println(HexUtil.subHex("0BCA", "AABF"));
		
		
		
	}
}

class HexUtil{
	
	//Takes in two hex strings and returns the sum as a hex string
	public static String addHex(String num1, String num2){
		
		String result;
		boolean negative = false;
		long doubleNum1 = hexToDecimal(num1);
		long doubleNum2 = hexToDecimal(num2);
		long resultdouble = doubleNum1 + doubleNum2;
		if(resultdouble < 0)
			negative = true;
		result = decimalToHex(resultdouble, negative);
		
		return result;
		
	}

	//Takes a hex string and returns the decimal equivilent
	public static long hexToDecimal(String hex){
		long result = 0;
		int power = 0;
		for(int i = hex.length() - 1; i >= 0; i--){
			//System.out.println("The character is " + hex.charAt(i) + " and the power is " + power);
			result += Character.getNumericValue(hex.charAt(i)) * Math.pow(16, power);
			power++;
		}
		
		return result;
	}

	//Takes a decimal value and converts it into a hex string
	//negative boolean will need to be reworked using two's complement for negative decimal values
	public static String decimalToHex(long num, boolean negative){
		String result;
		StringBuilder resultBuilder = new StringBuilder();
		int power = 0;
		
		while((long)(num / (Math.pow(16, power))) > 0){
			
			power++;
			
		}
		power--;
		if(negative == true){
			resultBuilder.append('-');
		}
		while(power >= 0){
			
			long currentVal = (long)(num / Math.pow(16,power));
			num = (long)(num % Math.pow(16,power));
			char nextChar = getHexValue(currentVal);
			resultBuilder.append(nextChar);
			power--;
		}
		return resultBuilder.toString();
		
	}
	
	
	//returns the decimal value of a hex character. If value is >= 16, return character iz 'Z' indicating error
	public static char getHexValue(long value){
		
		//takes an double < 16 and returns the hex equivilent
		char result;
		if(value >= 16){
			result = 'Z';
		}else if(value == 10){
			result = 'A';
		}else if(value == 11){
			result = 'B';
		}else if(value == 12){
			result = 'C';
		}else if(value == 13){
			result = 'D';
		}else if(value == 14){
			result = 'E';
		}else if(value == 15){
			result = 'F';
		}else{
			result = (char)(value + '0');
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

	//Takes two hex strings and subtracts them, returning a hex string
	//Only works for positive results currently
	public static String subHex(String num1, String num2){
		String result;
		boolean negative= false;
		long doubleNum1 = hexToDecimal(num1);
		long doubleNum2 = hexToDecimal(num2);
		long resultdouble = doubleNum1 - doubleNum2;
		if(resultdouble < 0)
			negative = true;
		result = decimalToHex(resultdouble, negative);
		return result;
	}
}