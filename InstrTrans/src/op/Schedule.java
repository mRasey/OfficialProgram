package op;

import instructions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Schedule {

	public void run() throws IOException, InterruptedException{
        new File(Output.byteCodeSavePath).delete();//如果原先输出文件存在则删除

		ArrayList<String> instruction;
		
		String regex1 = "[p,v]\\d+";
		String regex2 = ";->";
		
		//每个方法的开始序号
		int method_begin_number = 0;
		int local_reg_number = 0;
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
				local_reg_number = 0;
				method_begin_number = globalArguments.LineNumber;
				globalArguments.methodName = instruction.get(instruction.size()-1);
				globalArguments.finalByteCode.add(".method"+" "+globalArguments.methodName);
				globalArguments.finalByteCodePC++;
				
				globalArguments.method_info.add(instruction);
				globalArguments.method_count++;
				
			}
			//记录类名
			else if(instruction.get(0).equals(".class")){
				globalArguments.className = instruction.get(instruction.size()-1);
				for(int cp=1;cp<instruction.size()-1;cp++){
					globalArguments.classProPerty.add(instruction.get(cp));
				}
			}
			else if(instruction.get(0).equals(".super")){
				globalArguments.superClassName = instruction.get(instruction.size()-1);
			}
			//记录接口信息
			else if(instruction.get(0).equals(".implements")){
				globalArguments.inter_name.add(instruction.get(1));
				globalArguments.inter_count++;
			}
			else if(instruction.get(0).equals(".field")){
				globalArguments.field_info.add(instruction);
				globalArguments.field_count++;
			}
			//记录字段信息
			//记录寄存器类型
			else if(instruction.get(0).equals(".param")){
				if(globalArguments.registerQueue.getByDexName(instruction.get(1)) == null){
					globalArguments.registerQueue.addNewRegister(new Register(instruction.get(1), instruction.get(4), globalArguments.stackNumber++, globalArguments.LineNumber));
				}
			}
			//.local声明的变量为非临时变量
			else if(instruction.get(0).equals(".local")){
				local_reg_number++;
				Register register = globalArguments.registerQueue.getByDexName(instruction.get(1));
				if(register == null){
					globalArguments.registerQueue.addNewRegister(new Register(instruction.get(1), null, globalArguments.stackNumber++));
					register = globalArguments.registerQueue.getByDexName(instruction.get(1));
					register.setIfTempVar();
				}
				else{
					register.setIfTempVar();
				}
			}
			//为寄存器分配栈空间
			else if(globalArguments.rf.ifAnInstruction(instruction.get(0))){
				i=1;
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
			//方法结束时再统一处理指令
			else if(instruction.get(0).equals(".end") && instruction.get(1).equals("method")){
				//保存方法的局部变量个数
				globalArguments.method_localreg_number.add(local_reg_number);
                
				int temp = method_begin_number;
                //获取寄存器类型
                while(method_begin_number < globalArguments.LineNumber){
                    instruction = globalArguments.rf.getInstruction(method_begin_number);
                    if(globalArguments.rf.ifAnInstruction(instruction.get(0))){
                    	
                    	//处理switch的default
                    	if(instruction.get(0).contains("switch")){
                    		ArrayList<ArrayList<String>> instructions = globalArguments.rf.instructions;
                            String defaultTab = null;
                    		int order = 0;
                    		for( order = method_begin_number + 2; order < instructions.size(); i++) {
                                ArrayList<String> nextInstr = instructions.get(i);
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
					//处理标签: 和获取数组数据
					else if(instruction.get(0).startsWith(":")){
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
					method_begin_number++;
				}
                
                
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
                globalArguments.op.clear().readInf().initInstrSize().dispatchCodes().deal().output();
                
//                for(;ttt<globalArguments.optimizedByteCodePC;ttt++){
//                	System.out.println(globalArguments.optimizedByteCode.get(ttt));
//                }
                
                //处理跳转
                globalArguments.tt.clear();
                globalArguments.tt.readInf();
                globalArguments.tt.traTab();
                
			}
			
			globalArguments.LineNumber++;
		}
		//优化ldc指令
		globalArguments.rl.replace();
		globalArguments.cd.complete();
		
		//处理常量池
        globalArguments.cp.strConstPool();
//        for(;ttt<globalArguments.traTabByteCodePC;ttt++){
//        	System.out.println(globalArguments.traTabByteCode.get(ttt));
//        }
		//输出
		globalArguments.ot.print();
	}
}
