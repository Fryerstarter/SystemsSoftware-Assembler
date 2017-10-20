/*
TODO:

1. implement two's complement for negative hex values
2. problem with subHex

*/

public class HexTester{
	public static void main(String args[]){
		
		System.out.println(HexUtil.decimalToHex(256475, false));
		System.out.println(HexUtil.addHex("AAA", "BBB"));
		System.out.println(HexUtil.hexToDecimal("AAA"));
		System.out.println(HexUtil.hexToDecimal("BBB"));
		System.out.println(HexUtil.decimalToHex((HexUtil.hexToDecimal("AAA") - HexUtil.hexToDecimal("BBB")), true));
		System.out.println(HexUtil.subHex("AAA", "AAA"));
		
		
	}
}

class HexUtil{

	public static String addHex(String num1, String num2){
		String result;
		boolean negative = false;
		int doubleNum1 = hexToDecimal(num1);
		int doubleNum2 = hexToDecimal(num2);
		int resultdouble = doubleNum1 + doubleNum2;
		if(resultdouble < 0)
			negative = true;
		result = decimalToHex(resultdouble, negative);
		
		return result;
		
	}

	public static int hexToDecimal(String hex){
		int result = 0;
		int power = 0;
		for(int i = hex.length() - 1; i >= 0; i--){
			result += Character.getNumericValue(hex.charAt(i)) * Math.pow(16, power);
			power++;
		}
		
		return result;
	}

	public static String decimalToHex(int num, boolean negative){
		String result;
		StringBuilder resultBuilder = new StringBuilder();
		int power = 0;
		
		while((int)(num / (Math.pow(16, power))) > 0){
			
			power++;
			
		}
		power--;
		if(negative == true){
			resultBuilder.append('-');
		}
		while(power >= 0){
			
			int currentVal = (int)(num / Math.pow(16,power));
			num = (int)(num % Math.pow(16,power));
			char nextChar = getHexValue(currentVal);
			resultBuilder.append(nextChar);
			power--;
		}
		return resultBuilder.toString();
		
	}
	
	
	//value must be less than 16 (for now)
	public static char getHexValue(double value){
		assert(value < 16 && value >= 0);
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

	public static String subHex(String num1, String num2){
		String result;
		boolean negative= false;
		int doubleNum1 = hexToDecimal(num1);
		int doubleNum2 = hexToDecimal(num2);
		int resultdouble = doubleNum1 - doubleNum2;
		if(resultdouble < 0)
			negative = true;
		result = decimalToHex(resultdouble, negative);
		return result;
	}
}