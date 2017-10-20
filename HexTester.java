public class HexTester{
	public static void main(String args[]){
		System.out.println(HexUtil.getHexValue(10));
		System.out.println(HexUtil.getHexValue(11));
		System.out.println(HexUtil.getHexValue(12));
		System.out.println(HexUtil.getHexValue(13));
		System.out.println(HexUtil.getHexValue(14));
		System.out.println(HexUtil.getHexValue(15));
		System.out.println(HexUtil.getHexValue(16));
		System.out.println(HexUtil.getDecimalValue('A'));
		System.out.println(HexUtil.getDecimalValue('B'));
		System.out.println(HexUtil.getDecimalValue('C'));
		System.out.println(HexUtil.getDecimalValue('D'));
		System.out.println(HexUtil.getDecimalValue('E'));
		System.out.println(HexUtil.getDecimalValue('F'));
		System.out.println(HexUtil.getDecimalValue('1'));
		System.out.println(HexUtil.getDecimalValue('2'));
		System.out.println(HexUtil.getDecimalValue('3'));
		System.out.println(HexUtil.getDecimalValue('4'));
		
	}
}

class HexUtil{

	public static String addHex(String num1, String num2){
		String result;
		double doubleNum1 = hexToDecimal(num1);
		double doubleNum2 = hexToDecimal(num2);
		double resultdouble = doubleNum1 + doubleNum2;
		result = decimalToHex(resultdouble);
		
		return result;
		
	}

	public static double hexToDecimal(String hex){
		double result = 0;
		double j = 0;
		for(int i = hex.length()-1; i >= 0; i--){
			result += (HexUtil.getHexValue(hex.charAt(i)) * Math.pow(16, j));
			j++;
		}
		
		return result;
	}

	public static String decimalToHex(double num){
		String result;
		StringBuilder resultBuilder = new StringBuilder();
		double power = 0;
		
		while(num / Math.pow(16, power) != 0){
			
			power++;
			
		}
		
		while(power >= 0){
			
			double currentVal = num / Math.pow(16,power);
			num = num % Math.pow(16,power);
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
		double doubleNum1 = hexToDecimal(num1);
		double doubleNum2 = hexToDecimal(num2);
		double resultdouble = doubleNum1 - doubleNum2;
		result = decimalToHex(resultdouble);
		return result;
	}
}