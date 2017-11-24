
objectCode = null;
if(line.OPCODE.equals("RESW") || line.OPCODE.equals("RESB")){
linebreak = true;
}else if(line.OPCODE.equals("BASE")){
	//set base equal to memory location of symbol
	objectCode = "-2"; //to differentiate between no object code from BASE vs RESW/RESB
}else if(line.OPCODE.equals("RSUB")){
	objectCode = "4F0000";
}else{
	//MAKE OBJECT CODE
}

if(textRecord == null){
	//initialize text record
	//start with length = "00"
	//starting memory location is LOCCTR
}

if(HexUtil.hexToDecimal((HexUtil.addHex(currentTextRecordLength, lengthOfObjectCodeInHalfBytes))) > 60 || codeBreak){
//does not fit OR line break due to RESW/RESB
//write line to .obj
codeBreak = false;
currentTextRecordLength = 0;
textRecord = null;
}else{
//FITS!
//append object code to text record, increment currentTextRecordLength by size of object code
currentTextRecordLength = currentTextRecordLength + objectCode.length();

}