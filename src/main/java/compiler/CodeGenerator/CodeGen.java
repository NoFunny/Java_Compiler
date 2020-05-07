package compiler.CodeGenerator;

import compiler.identifierTable.Table;
import compiler.parser.AST.ArrayMember.ArrayMemberId;
import compiler.parser.AST.Inits.CallFunc;
import compiler.parser.AST.Inits.ForkInitVar;
import compiler.parser.AST.Inits.NodeInit;
import compiler.parser.AST.Node;
import compiler.parser.AST.NodeArgsInit;
import compiler.parser.AST.NodeClass;
import compiler.parser.AST.statement.*;
import compiler.parser.AST.value.Attachment;
import compiler.parser.AST.value.CallArrayMember;
import compiler.parser.AST.value.GenericValue;
import compiler.parser.AST.value.Number;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CodeGen {
    private static final File dir = new File("main.asm");
    private static int offset = 0;
    private static int offsetInit = 0;
    private static int offsetFloat = 4;
    private static int reservReg = 0;
    private static int cntJmp = 1;
    private static Map<String, Integer> pointInit = new HashMap<>();
    private static Map<String,Integer> pointVarInit = new HashMap<>();

    public void go(NodeClass root, Table idTable) throws IOException {
//        File dir  = new File("main.asm");
        FileWriter fw = new FileWriter(dir.getName(), false);
//        fw.write("Hello ASM\n");
//        fw.write("ASM USELESS");
//        fw.close();
        next(root, fw);
        fw.close();
    }

    private void next(Node node, FileWriter fw) throws IOException {
        if (node instanceof NodeClass) {
            ArrayList<NodeArgsInit> nodeArgsInits = new ArrayList<>();
            for (int i = 0; i < ((NodeClass) node).getInitList().size(); i++) {
                if (!((NodeClass) node).getInitList().get(i).getForkInit().toString().isEmpty())
                fw.write("section .text\n\n.globl main\n.type main, @function\n\n");
                fw.append(((NodeClass) node).getInitList().get(i).getId().getValue());
                fw.write(":");
                fw.append("\n");
                fw.append("\t" + "pushq" + "\t" + "%rbp\n");
                fw.append("\t" + "movq" + "\t" + "%rsp, " + "%rbp\n");
                nodeArgsInits.addAll(((CallFunc) ((NodeClass) node).getInitList().get(i).getForkInit()).getNodeArgsInitList());
            }
            int i = 1;
            for (NodeArgsInit nodeArgs : nodeArgsInits) {
                if (nodeArgs.getDataType().getType().equals("INT")) {
                    offset = 24;
                    if(i == 1) {
                        fw.append("\tmovl\t %rdi,".concat(String.format(" -%d(rbp)\n",offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 2) {
                        offset += 4;
                        fw.append("\tmovl\t %esi,".concat(String.format(" -%d(rbp)\n", offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 3) {
                        offset += 4;
                        fw.append("\tmovl\t %rdx,".concat(String.format(" -%d(rbp)\n", offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }
                }else if(nodeArgs.getDataType().getType().equals("FLOAT")) {
                    offset = 24;
                    if(i == 1) {
                        fw.append("\tmovl\t %rdi,".concat(String.format(" -%d(rbp)\n",offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 2) {
                        offset += 4;
                        fw.append("\tmovss\t %xmm0,".concat(String.format(" -%d(rbp)\n", offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 3) {
                        offset += 4;
                        fw.append("\tmovss\t %xmm0,".concat(String.format(" -%d(rbp)\n", offset)));
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }
                }
            }
//            fw.close();
//            pointInit.forEach((x,y) -> System.out.println(x));
            for (NodeInit nodeInit : ((NodeClass) node).getInitList()) {
                for (NodeStatement statement : ((CallFunc) nodeInit.getForkInit()).getStatementList()) {
                    if (statement != null) {
                        addStatement(statement, fw);
                    }
                }
            }
        }
    }

    private void addStatement(NodeStatement statement, FileWriter fw) throws IOException {
        if (statement instanceof NodeInit) {
            addInitNode((NodeInit) statement, fw);
        } else if (statement instanceof NodeLoop) {
            addLoopNode((NodeLoop) statement, fw);
        } else if (statement instanceof NodeScanln) {
            addScanlnNode(statement, fw);
        } else if (statement instanceof NodePrintln) {
            addPrintlNode(statement, fw);
        }else if(statement instanceof NodeReturn) {
            addReturnNode(statement, fw);
        }
    }

    private void addInitNode(NodeInit node, FileWriter fw) throws IOException {
        if (node.getDataType().getType().equals("INT")) {
            offsetInit += 4;
            fw.append(String.format("\tmovl\t $%s, -%d(rbp)\n", ((ForkInitVar)node.getForkInit()).getExpression().getlValue().getValue().getValue() ,offsetInit));
            pointVarInit.put(node.getId().getValue(), offsetInit);
        } else if (node.getDataType().getType().equals("CHAR")) {
            offsetInit += 1;
            pointVarInit.put(node.getId().getValue(), offsetInit);
        } else if (node.getDataType().getType().equals("FLOAT") || node.getDataType().getType().equals("FLOOAT")) {
            fw.append("\tmovss\t .LC").append(String.valueOf(reservReg)).append("(%rip), -%xmm0\n");
            reservReg+=1;
            offsetFloat += 4;
            fw.append("\tmovss\t %xmm0,".concat(String.format(" -%d(rbp)\n", offsetFloat + offsetInit)));
            pointVarInit.put(node.getId().getValue(), offsetFloat + offsetInit);
        }
    }
    private void checkOnGenericValueOrCallArrayMember(NodeStatement statement, FileWriter fw) throws IOException {
        if(((NodeConditional)statement).getExpression().getlValue() instanceof CallArrayMember) {
//            System.out.println(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue());
            if (pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()) != null) {
                fw.append("\tmovl\t -").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                fw.append("\tcltq");
                fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx");
            } else if (pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()) != null) {
                fw.append("\tmovl\t -").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                fw.append("\tcltq\n");
                fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
            }

            if (pointInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                fw.append("\tmovq\t -" + pointInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
            } else if (pointVarInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                fw.append("\tmovq\t -" + pointVarInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
            }
            fw.append("\taddq\t%rdx, %rax\n");
            fw.append("\tmovl\t(%rax), %eax\n");
        }else if(((NodeConditional)statement).getExpression().getlValue() instanceof GenericValue) {
            if (((NodeConditional) statement).getExpression().getlValue() instanceof GenericValue) {
                if (pointInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                    fw.append("\tmovl\t-" + pointInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()) + "(%rbp), %eax\n");
                } else if (pointVarInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                    fw.append("\tmovl\t-" + pointVarInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()) + "(%rbp), %eax\n");
                }
            }
            if (((NodeConditional) statement).getExpression().getrExpression().getlValue() instanceof CallArrayMember) {
                if (pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()) != null) {
                    fw.append("\tmovl\t -").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                    fw.append("\tcltq");
                    fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx");
                } else if (pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()) != null) {
                    fw.append("\tmovl\t -").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                    fw.append("\tcltq\n");
                    fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) ((NodeConditional) statement).getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                }
            }
        }
        if(((NodeConditional) statement).getExpression().getrExpression().getlValue() instanceof GenericValue) {
            if(pointInit.containsKey(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue())) {
                fw.append("\tcmpl\t%eax, -" + pointInit.get(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue()) + "(%rbp)\n");
            }else if(pointVarInit.containsKey(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue())); {
                fw.append("\tcmpl\t%eax, -" + pointVarInit.get(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue()) + "(%rbp)\n");
            }
        }
    }
    private void addConditionalNode(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append(".L"+ cntJmp +":\n");
        cntJmp++;
       checkOnGenericValueOrCallArrayMember(statement, fw);
        if(((NodeConditional)statement).getExpression().getOperator().getValue().getType().equals("LESS")) {
            fw.append("\tjle\t.L"+ cntJmp +"\n");
//            cntJmp++;
        }else if(((NodeConditional)statement).getExpression().getOperator().getValue().getType().equals("GREATHER")) {
            fw.append("\tjge\t.L"+ cntJmp +"\n");
//            cntJmp++;
        }
        for(NodeStatement expression : ((NodeConditional) statement).getStatementList()) {
            if(((NodeExpression)expression).getlValue() instanceof Attachment) {
                nodeAttachment((Attachment) ((NodeExpression) expression).getlValue(), fw);
            }
        }
        fw.append(".L"+ cntJmp +":\n");
        cntJmp++;
    }

    private void nodeAttachment(Attachment expression, FileWriter fw) throws IOException {
        if (expression.getExpression() instanceof NodeExpression) {
            if (expression.getExpression().getrValue() == null) {
                if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tmovl\t-" + pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "(%rbp), %eax\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tmovl\t-" + pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "(%rbp), %eax\n");
                    }
                    fw.append("\tcltq\n");
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax," + pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax," + pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                    }
                    if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t-" + pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
                    } else if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t-" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
                    }
                    fw.append("\taddq\t%rdx, %rax\n");
                    fw.append("\tmovl\t(%rax), %eax\n");
                    if (pointVarInit.containsKey(expression.getValue().getValue())) {
                        fw.append("\tmovl\t%eax, -" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                    } else if (pointInit.containsKey(expression.getValue().getValue())) {
                        fw.append("\tmovl\t%eax, -" + pointInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                    }
                    return;
                }
            }
                int offsetExpr = 0;
                if (expression.getExpression().getlValue() instanceof Number && expression.getExpression().getrValue() instanceof Number) {
                    if (expression.getExpression().getlValue().getValue().getType().equals("INTEGER") || expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                        if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS"))
                            fw.append("\tmovl\t $" + (Integer.parseInt(expression.getExpression().getlValue().getValue().getValue()) + Integer.parseInt(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                        else if (expression.getExpression().getOperator().getValue().getType().equals("BINARSUB"))
                            fw.append("\tmovl\t $" + (Integer.parseInt(expression.getExpression().getlValue().getValue().getValue()) - Integer.parseInt(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                        return;
                    }
                } else if (expression.getExpression().getlValue() instanceof GenericValue && expression.getExpression().getrValue() instanceof GenericValue) {
                    if (expression.getExpression().getlValue().getValue().getType().equals("INT")) {
                        checkGenericValue(expression, fw);
                    } else if (expression.getExpression().getlValue().getValue().getType().equals("FLOOAT")) {
                    }

                }
                if (expression.getExpression().getlValue() instanceof Number) {
                    if (expression.getExpression().getlValue().getValue().getType().equals("INTEGER")) {
                        offsetExpr += 4;
                        fw.append("\tmovl\t-" + offsetExpr + "(%rbp), %eax\n");
                    } else if (expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                        offsetExpr += 4;
                        fw.append("\tmovl\t-" + offsetExpr + "(%rbp), %eax\n");
                    } else if (expression.getExpression().getlValue().getValue().getType().equals("CHAR")) {
                        offsetExpr += 4;
                        fw.append("\tmovl\t-" + offsetExpr + "(%rbp), %eax\n");
                    }
                    if (expression.getExpression().getrValue() instanceof CallArrayMember) {
                        fw.append("\tcltq\n");
                        if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue())) {
                            fw.append("\tleaq\t0(,%rax," + pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                        } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue())) {
                            fw.append("\tleaq\t0(,%rax," + pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                        }
                        if (pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue())) {
                            fw.append("\tmovq\t -" + pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()) + "(%rbp), %rax\n");
                        } else if (pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue())) {
                            fw.append("\tmovq\t -" + pointInit.get(expression.getExpression().getrValue().getValue().getValue()) + "(%rbp), %rax\n");
                        } else if (expression.getExpression().getlValue() instanceof GenericValue) {
                            if (expression.getExpression().getrValue() instanceof CallArrayMember) {

                            } else if (expression.getExpression().getrValue() instanceof Number) {

                            }
                        }
                    }
                } else if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                    offsetExpr += 4;
                    fw.append("\tmovl\t-" + offsetExpr + "(%rbp), %eax\n");
                    fw.append("\tcltq\n");
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax," + pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax," + pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()) + "), %rdx\n");
                    }
                    if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t -" + pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
                    } else if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t -" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + "(%rbp), %rax\n");
                    }
                }

                //Осталось условие только для genericValue
                fw.append("\taddq\t%rdx, %rax\n");
                if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
                    fw.append("\taddl\t$" + expression.getExpression().getlValue().getValue().getValue() + ", %eax\n");
                } else if (expression.getExpression().getOperator().getValue().getType().equals("BINARSUB")) {
                    if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                        fw.append("\tmovl\t(%rax), %eax\n\tsubl\t$" + expression.getExpression().getrValue().getValue().getValue() + ", %eax\n");
                    } else {
                        if (expression.getExpression().getlValue().toString().contains("GenericValue")) {
                            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + ",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx,%eax\n");
                            } else if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$" + pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) + ",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx,%eax\n");
                            }
                        } else if (expression.getExpression().getlValue().toString().contains("Number")) {
                            if (expression.getExpression().getlValue().getValue().getType().equals("INTEGER")) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$" + expression.getExpression().getlValue().getValue().getValue() + ",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx, %eax\n");
                            } else if (expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                                fw.append("\tmovl\t(%rax), %eax\n\tcvtsi2sdl\t %eax, %xmm1\n\tmovsd\t.LC" + reservReg + "(%rip), %xmm0\n\tsubsd\t%smm1,%xmm0\n\tcvttsd2sil\t%xmm0, %eax\n");
                                reservReg++;
                            }
                        }
                    }
                }
                if (pointVarInit.containsKey(expression.getValue().getValue())) {
                    fw.append("\tmovl\t%eax, -" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                } else if (pointInit.containsKey(expression.getValue().getValue())) {
                    fw.append("\tmovl\t%eax, -" + pointInit.get(expression.getValue().getValue()) + "(%rbp)\n");
                }
        }
    }

    private void checkGenericValue(Attachment expression, FileWriter fw) throws IOException {
        if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + pointInit.get(expression.getExpression().getrValue().getValue().getValue()) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) + pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + pointInit.get(expression.getExpression().getlValue().getValue().getValue()) + pointInit.get(expression.getExpression().getrValue().getValue().getValue()) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
        } else if (expression.getExpression().getOperator().getValue().getType().equals("BINASUB")) {
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + (pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointInit.get(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + (pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointVarInit.get(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + (pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointVarInit.get(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $" + (pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointInit.get(expression.getExpression().getrValue().getValue().getValue())) + ",\t-" + pointVarInit.get(expression.getValue().getValue()) + "(%rbp)\n");
        }
    }



    private void addLoopNode(NodeLoop statement, FileWriter fw) throws IOException {
//        pointVarInit.forEach((x,y) -> System.out.println(y));
        cntJmp+=1;
        fw.append("\tjmp\t\t.L"+cntJmp+"\n.L"+cntJmp+":\n");
        cntJmp++;
        fw.append("\tmovl\t -"+ pointVarInit.get(statement.getExpression().getlValue().getValue().getValue()) +"(%rbp), %eax\n");
        if(pointVarInit.containsKey(statement.getExpression().getrExpression().getlValue().getValue().getValue())) {
            fw.append("\tcmpl\t -" + pointVarInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()) + "(%rbp), %eax\n");
        }else if (pointInit.containsKey(statement.getExpression().getrExpression().getlValue().getValue().getValue())) {
            fw.append("\tcmpl\t -" + pointInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()) + "(%rbp), %eax\n");
        }
        fw.append("\tjl\t.L"+cntJmp+"\n");
        if(pointInit.containsKey(statement.getExpression().getlValue().getValue().getValue())) {
            fw.append("\tmovl\t-"+ pointInit.get(statement.getExpression().getlValue().getValue().getValue()) +"(%rbp), %eax\n");
        }else if (pointVarInit.containsKey(statement.getExpression().getlValue().getValue().getValue())) {
            fw.append("\tmovl\t-"+ pointVarInit.get(statement.getExpression().getlValue().getValue().getValue()) +"(%rbp), %eax\n");
        }
        fw.append("\tpopq\t%rbp\n\tret\n");
        for(NodeStatement node : statement.getStatementList()) {
            if(node instanceof NodeConditional) {
                addConditionalNode(node, fw);
            }else if(node instanceof NodeExpression) {
                if(((NodeExpression) node).getlValue() instanceof Attachment) {
                    if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue() != null && ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue() != null) {
                        if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
                            if(pointInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\taddl\t$"+ ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue() +", -"+ pointInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()) +"(%rbp)\n");
                            }else if(pointVarInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\taddl\t$" + ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue() + ", -" + pointVarInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()) + "(%rbp)\n");
                            }
                        }else if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getOperator().getValue().getType().equals("BINARSUB")) {
                            if(pointInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tsubl\t$"+ ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue() +", -"+ pointInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()) +"(%rbp)\n");
                            }else if(pointVarInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tsubl\t$" + ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue() + ", -" + pointVarInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()) + "(%rbp)\n");
                            }
                        }
                    }
                }
            }
        }
    }

    private void addScanlnNode(NodeStatement statement, FileWriter fw) {
    }

    private void addPrintlNode(NodeStatement statement, FileWriter fw) {
    }

    private void addReturnNode(NodeStatement statement, FileWriter fw) throws IOException {
//        if(((NodeReturn)statement).getExpression() instanceof NodeExpression)
//            fw.append("\tpopq\t%rbp\n\tret");
    }
}
