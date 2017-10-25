
//Hex conversion, addition, subtraction

public class HexTester{
	public static void main(String args[]){
		System.out.println(HexUtil.addHex("101A","2FAC"));
		System.out.println(HexUtil.hexToDecimal("1C"));
		System.out.println(HexUtil.decimalToHex(-10000, true));
		System.out.println(HexUtil.getDecimalValue('F'));
		System.out.println(HexUtil.subHex("0006", "001A"));
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

	//Takes two hex strings and subtracts them, returning a hex string
	public static String subHex(String num1, String num2){
		String result;
		boolean negative = false;
		long doubleNum1 = hexToDecimal(num1);
		long doubleNum2 = hexToDecimal(num2);
		long resultdouble = doubleNum1 - doubleNum2;
		if (resultdouble < 0){
			negative = true;
			result = decimalToHex(resultdouble, negative);
			return result;
		}
		result = decimalToHex(resultdouble,negative);
		return result;
	}
        
}
