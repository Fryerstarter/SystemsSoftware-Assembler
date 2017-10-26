/*
Intro to Systems Software Group Project
Christopher Kile
*/

class OPTAB{
	
	
}

class SYMTAB{
	
	
}



class Operation{
	String op;
	String opCode;
	int length;
	
	public Operation(String op, String opCode, int length){
		this.op = op;
		this.opCode = opCode;
		this.length = length;
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

}
