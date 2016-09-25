package op;

import instructions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Schedule {

	ArrayList<String> instruction;
	//在setMethodInf()清空，在endMethod()传给localToLine
	ArrayList<String> local;
	ArrayList<String> line;
	
	String regex1 = "[p,v]\\d+";
	String regex2 = ";->";
	
	//每个方法的开始序号
	int method_begin_number = 0;
	int local_reg_number = 0;
	
	public void run() throws IOException, InterruptedException{


		int ttt = 0;
		int i = 0;
		while(globalArguments.rf.readLine()){
			if(globalArguments.rf.ifNull()){
				continue;
			}
			instruction = globalArguments.rf.getInstruction();		
			//方法的开始清空数据
			if(globalArguments.rf.ifNewMethod()){	
				globalArguments.clear();
				setMethodInf();
			}
			//记录类名
			else if(instruction.get(0).equals(".class")){
				setClassInf();
			}
			else if(instruction.get(0).equals(".super")){
				setSupClassInf();
			}
			else if(instruction.get(0).equals(".source")){
				globalArguments.sourceFile = instruction.get(1).substring(1, instruction.get(1).length()-1);
			}
			//记录接口信息
			else if(instruction.get(0).equals(".implements")){
				setInterfaceInf();
			}
			//记录字段信息
			else if(instruction.get(0).equals(".field")){
				setFieldInf();
			}
			//记录寄存器类型
			else if(instruction.get(0).equals(".param")){
				dealParam();
			}
			//为寄存器分配栈空间
			else if(globalArguments.rf.ifAnInstruction(instruction.get(0))){
				addNewReg();
			}
			//方法结束时再统一处理指令
			else if(instruction.get(0).equals(".end") && instruction.get(1).equals("method")){
				endMethod();
			}
			
			globalArguments.LineNumber++;
		}
	}
	
	public void setMethodInf(){
		local_reg_number = 0;
		method_begin_number = globalArguments.LineNumber;
		globalArguments.methodName = instruction.get(instruction.size()-1);
		globalArguments.finalByteCode.add(".method"+" "+globalArguments.methodName);
		globalArguments.finalByteCodePC++;
		globalArguments.method_info.add(instruction);
		globalArguments.method_count++;
		local = new ArrayList<>();
		line = new ArrayList<>();
		local.add(".local this, nnn");
		line.add(".line 0");
	}

	public void setClassInf(){
		globalArguments.className = instruction.get(instruction.size()-1);
		for(int cp=1;cp<instruction.size()-1;cp++){
			globalArguments.classProPerty.add(instruction.get(cp));
		}
	}
	public void setSupClassInf(){
		globalArguments.superClassName = instruction.get(instruction.size()-1);
	}
	public void setInterfaceInf(){
		globalArguments.inter_name.add(instruction.get(1));
		globalArguments.inter_count++;
	}
	public void setFieldInf(){
		globalArguments.field_info.add(instruction);
		globalArguments.field_count++;
	}
	public void dealParam(){
		if(globalArguments.registerQueue.getByDexName(instruction.get(1)) == null){
			globalArguments.registerQueue.addNewRegister(new Register(instruction.get(1), instruction.get(4), globalArguments.stackNumber++, globalArguments.LineNumber));
		}
	}
	public void dealLocal(){
		//记录方法里的局部变量个数
		String datatype = instruction.get(2).split(":")[1];
		if(datatype.equals("D") || datatype.equals("J")){
			local_reg_number+=2;
		}
		else{
			local_reg_number++;
		}
		int order = method_begin_number; //当前.local的标号
		ArrayList<String> lastIns;
		//分配栈
		Register register = globalArguments.registerQueue.getByDexName(instruction.get(1)); //.local指定的寄存器
		if(register == null){
			globalArguments.registerQueue.addNewRegister(new Register(instruction.get(1), null, globalArguments.stackNumber++));
			register = globalArguments.registerQueue.getByDexName(instruction.get(1));
			register.setIfTempVar();
		}
		else{
			register.setIfTempVar();
		}
		//记录寄存器类型
		order--;
		do{
			lastIns = globalArguments.rf.getInstruction(order);
			if(lastIns.get(0).equals(".line")){
				order--;
				continue;
			}
			else if(lastIns.get(0).equals("goto")){
				order--;
				continue;
			}
			else if(globalArguments.rf.ifAnInstruction(lastIns.get(0))){
				for(int i = 1;i<lastIns.size();i++){
					if(lastIns.get(i).equals(register.dexName)){
						register.updateType(order, datatype);
						break;
					}
				}
				break;
			}
			else{
				break;
			}
		}while(order >= 0);
		
		//保存local于对应的行号
		String localStr = "";
		String lineStr = "";
		localStr = instruction.get(0) +" "+ instruction.get(1)+" " + instruction.get(2);
		order = method_begin_number-1;
		do{
			lastIns = globalArguments.rf.getInstruction(order);
			if(lastIns.get(0).equals(".line")){
				do{
					order--;
					lastIns = globalArguments.rf.getInstruction(order);
				}while(!lastIns.get(0).equals(".line"));
				lineStr = lastIns.get(0) +" "+ lastIns.get(1);
				local.add(localStr);
				line.add(lineStr);
				break;
			}
			else if(globalArguments.rf.ifAnInstruction(lastIns.get(0))){
				while(!lastIns.get(0).equals(".line")){
					order--;
					lastIns = globalArguments.rf.getInstruction(order);
				}
				lineStr = lastIns.get(0) +" "+ lastIns.get(1);
				local.add(localStr);
				line.add(lineStr);
				break;
			}
			else{
				order--;
			}
		}while(order >= 0);
	}
	
	public void addNewReg(){
		int i=1;
		//分配栈空间
		if(i<instruction.size()){
			while(i<instruction.size() && instruction.get(i).matches(regex1)){
				if(globalArguments.registerQueue.getByDexName(instruction.get(i)) != null){
					i++;
				}
				else{
					globalArguments.registerQueue.addNewRegister(new Register(instruction.get(1), null, globalArguments.stackNumber++));
					i++;
				}
			}
		}
	}
	
	public void endMethod() throws IOException{
		
		
		int temp = method_begin_number;
        //获取寄存器类型
        while(method_begin_number < globalArguments.LineNumber){
            instruction = globalArguments.rf.getInstruction(method_begin_number);
            if(globalArguments.rf.ifAnInstruction(instruction.get(0))){
            	//处理switch的default
            	if(instruction.get(0).contains("switch")){
            		dealSwitch();
            	}
                if(instruction.get(0).contains("array") || instruction.get(0).contains("aget") || instruction.get(0).contains("aput"))
                    new _array().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("check"))
                    new _check().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("cmp"))
                    new _cmp().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("const"))
                    new _const().ifUpgrade(instruction, globalArguments.rf.getInstruction(method_begin_number + 1),globalArguments.rf.getInstruction(method_begin_number + 2), method_begin_number);
                else if(instruction.get(0).contains("goto"))
                    new _goto().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("if"))
                    new _if().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("iget"))
                    new _iget().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("instance"))
                    new _instance().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("invoke"))
                    new _invoke().ifUpgrade(instruction,method_begin_number);
                else if(instruction.get(0).contains("iput"))
                    new _iput().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("monitor"))
                    new _monitor().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("move"))
                    new _move().ifUpgrade(globalArguments.rf.getInstruction(method_begin_number-1),instruction,method_begin_number);
                else if(instruction.get(0).contains("neg") || instruction.get(0).contains("not") || instruction.get(0).contains("to"))
                    new _neg_not_to().ifUpgrade(instruction,method_begin_number);
                else if(instruction.get(0).contains("nop"))
                    new _nop().ifUpgrade(instruction, method_begin_number);
                else if(  instruction.get(0).contains("add-")
                        || instruction.get(0).contains("sub-")
                        || instruction.get(0).contains("mul-")
                        || instruction.get(0).contains("div-")
                        || instruction.get(0).contains("rem-")
                        || instruction.get(0).contains("and-")
                        || instruction.get(0).contains("or-")
                        || instruction.get(0).contains("xor-")
                        || instruction.get(0).contains("shl-")
                        || instruction.get(0).contains("shr-")
                        || instruction.get(0).contains("ushr-")) {
                    new _op().ifUpgrade(instruction, method_begin_number);
                }
                else if(instruction.get(0).contains("return"))
                    new _return().ifUpgrade(instruction,method_begin_number);
                else if(instruction.get(0).contains("sget"))
                    new _sget().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("sput"))
                    new _sput().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("switch"))
                    new _switch().ifUpgrade(instruction, method_begin_number);
                else if(instruction.get(0).contains("throw"))
                    new _throw().ifUpgrade(instruction, method_begin_number);
                else {
                    System.out.println("error instruction");
                }
            }
            //.local声明的变量为非临时变量
			else if(instruction.get(0).equals(".local")){
                globalArguments.finalByteCode.add(instruction.get(0)+" "+instruction.get(1)+" "+instruction.get(2));
                globalArguments.finalByteCodePC ++;
				dealLocal();
			}
			//处理标签: 和获取数组数据
			else if(instruction.get(0).startsWith(":")){
				dealLabel();
			}
			method_begin_number++;
		}
        //保存方法的local-line
      	globalArguments.method_local.add(local);
      	globalArguments.method_line.add(line);
        //保存方法的局部变量个数
        set_max_locals();
        
        globalArguments.switchDefaultIndex = 0;
        method_begin_number = temp;
        while(method_begin_number < globalArguments.LineNumber){
            instruction = globalArguments.rf.getInstruction(method_begin_number);
            if(globalArguments.rf.ifAnInstruction(instruction.get(0))){
                new Translation(instruction, method_begin_number).translateIns();//翻译指令，把指令编号也传进去
            }
            //将.line加入到byteCode中
            else if(instruction.get(0).equals(".line")){
            	globalArguments.finalByteCode.add(instruction.get(0) +" "+ instruction.get(1));
        		globalArguments.finalByteCodePC++;
            }
            //将标签也导入到byteCode中
            else if(instruction.get(0).startsWith(":")){
            	ArrayList<String> nextDexCode = globalArguments.rf.getInstruction(method_begin_number+1);
            	//是array表的标签就忽略,是switch表里的标签也忽略
            	if(nextDexCode.get(0).equals(".array-data") || nextDexCode.get(0).equals(".packed-switch") || nextDexCode.get(0).equals(".sparse-switch")){
            		method_begin_number++;
            		nextDexCode = globalArguments.rf.getInstruction(method_begin_number+1);
            		while(!nextDexCode.get(0).equals(".end")){
            			method_begin_number++;
                		nextDexCode = globalArguments.rf.getInstruction(method_begin_number+1);
            		}
            		method_begin_number++;
            	}
            	//正常标签加入到byteCode中
            	else{
            		globalArguments.finalByteCode.add(instruction.get(0));
            		globalArguments.finalByteCodePC++;
            	}
            }
            else if(instruction.get(0).equals(".method")){
            	int tempI = method_begin_number;
            	ArrayList<String> nextIns = new ArrayList<>();
            	nextIns = globalArguments.rf.getInstruction(tempI);
            	//下一条不是.line也不是指令
            	while(!(nextIns.get(0).equals(".line") || globalArguments.rf.ifAnInstruction(nextIns.get(0)))){
            		tempI++;
            		nextIns = globalArguments.rf.getInstruction(tempI);
            	}
            	if(globalArguments.rf.ifAnInstruction(nextIns.get(0))){
            		globalArguments.finalByteCode.add(".line -1");
            		globalArguments.finalByteCodePC++;
            	}
            }
            
            method_begin_number++;
        }
        globalArguments.finalByteCode.add(".end method");
		globalArguments.finalByteCodePC++;
        
        //指令优化，要放在处理跳转之前
        globalArguments.op.clear().readInf().initStackSize().initInstrSize().dispatchCodes().deal().output();
		//处理跳转
        globalArguments.tt.clear();
        globalArguments.tt.readInf();
        globalArguments.tt.traTab();
	}
	
	public void dealSwitch(){
		ArrayList<ArrayList<String>> instructions = globalArguments.rf.instructions;
        String defaultTab = null;
		int order = 0;
		for( order = method_begin_number + 2; order < instructions.size(); order++) {
            ArrayList<String> nextInstr = instructions.get(order);
            if(nextInstr.get(0).startsWith("."))
                continue;
            else if(nextInstr.get(0).startsWith(":")) {
                break;
            }
            else {
                //是一条指令
                defaultTab = ":default_" + globalArguments.switchDefaultIndex;//自己新建一个标签:default_index
                globalArguments.switchDefaultIndex++;
                ArrayList<String> tempDefaultTab = new ArrayList<String>();
                tempDefaultTab.add(defaultTab);
                instructions.add(order, tempDefaultTab);
                break;
            }
        }
	}
	public void dealLabel(){
		ArrayList<String> nextDexCode = globalArguments.rf.getInstruction(method_begin_number+1);
		//处理数组数据
		if(nextDexCode.get(0).equals(".array-data")){
			//标签
			String tab = instruction.get(0);
			ArrayList<String> ad = new ArrayList<>();
			method_begin_number += 2;
			nextDexCode = globalArguments.rf.getInstruction(method_begin_number);
			while(!nextDexCode.get(0).equals(".end")){
				ad.add(nextDexCode.get(0));
				method_begin_number++;
				nextDexCode = globalArguments.rf.getInstruction(method_begin_number);
			}
			globalArguments.arrayData.put(tab, ad);
			
		}
        //处理switch数据
        else if(nextDexCode.get(0).equals(".packed-switch")
                || nextDexCode.get(0).equals(".sparse-switch")) {
            String tab = instruction.get(0);
            String type = nextDexCode.get(0);//跳转类型
            ArrayList<SwitchData> data = new ArrayList<>();
            method_begin_number += 2;
            nextDexCode = globalArguments.rf.getInstruction(method_begin_number);
            int index = 0;
            if(type.contains("packed")){
                index = Integer.parseInt(globalArguments.rf.getInstruction(method_begin_number-1).get(1).substring(2));//获得最开始编号,转化成十进制
            }
            while(!nextDexCode.get(0).equals(".end")){
                if(type.contains("packed")) {
                    data.add(new SwitchData(nextDexCode.get(0), index++));
                }
                else
                    data.add(new SwitchData(nextDexCode.get(2), nextDexCode.get(0)));
                method_begin_number++;
                nextDexCode = globalArguments.rf.getInstruction(method_begin_number);
            }
            globalArguments.switchData.put(tab, data);
        }
	}

	public void set_max_locals(){
		String types =  globalArguments.methodName.substring(globalArguments.methodName.indexOf("(")+1,globalArguments.methodName.indexOf(")")+1);
		int i = 0,argNumber = 0;
		while (!types.equals(")")) {
			if (types.startsWith("L")) {
				argNumber++;
				types = types.substring(types.indexOf(";"));
			} else if (types.startsWith("[")) {
				String tempType = "";
				do {
					tempType += "[";
					types = types.substring(1);
				} while (types.startsWith("["));
				if ((types.charAt(0) + "").equals("L")) {
					argNumber++;
					types = types.substring(types.indexOf(";"));
				} else {
					argNumber++;
				}
			}
			else {
				argNumber++;
			}
			types = types.substring(1);
		}
		
		//多一个this
		globalArguments.method_max_locals.add(local_reg_number+1+argNumber);
	}
}
