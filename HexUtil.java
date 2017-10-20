/* 
This is entirely experimental and untested
*/

static class HexUtil{

	public static String addHex(String num1, String num2){
		String result;
		int intNum1 = hexToDecimal(num1);
		int intNum2 = hexToDecimal(num2);
		int resultInt = intNum1 + intNum2;
		result = decimalToHex(resultInt);
		
		return result;
		
	}

	public static int hexToDecimal(String hex){
		int result = 0;
		int j = 0;
		for(int i = hex.length(); i > 0; i--{
			result += (hex.charAt(i) * Math.pow(16, j));
			j++;
		}
		
		return result;
	}

	public static String decimalToHex(int num){
		String result;
		StringBuilder resultBuilder = new StringBuilder();
		int power = 0;
		
		while(num % Math.pow(16, power) != 0){
			
			power++;
			
		}
		
		while(power >= 0){
			
			int currentVal = num % Math.pow(16,power);
			char nextChar = getHexValue(currentVal);
			resultBuilder.append(nextChar);
			power--;
		}
	}
	
	
	//value must be less than 16 (for now)
	public static char getHexValue(int value){
		//takes an int < 16 and returns the hex equivilent
		char result;
		if(value >= 16){
			result = null;
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

	public static String subHex(String num1, String num2){
		String result;
		int intNum1 = hexToDecimal(num1);
		int intNum2 = hexToDecimal(num2);
		int resultInt = intNum1 - intNum2;
		result = decimalToHex(resultInt);
		retun result;
	}
}
