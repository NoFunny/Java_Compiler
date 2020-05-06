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
import compiler.parser.AST.value.CallArrayMember;
import compiler.parser.AST.value.GenericValue;

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
//            offsetInit += 4;
            offsetFloat += 4;
            fw.append("\tmovss\t %xmm0,".concat(String.format(" -%d(rbp)\n", offsetFloat + offsetInit)));
            pointVarInit.put(node.getId().getValue(), offsetFloat + offsetInit);
        }
    }

    private void addConditionalNode(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append(".L"+ cntJmp +":\n");
        cntJmp++;
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
        }
        if(((NodeConditional)statement).getExpression().getlValue() instanceof GenericValue) {
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
        if(((NodeConditional) statement).getExpression().getOperator().getValue().equals("LESS")) {
            fw.append("\tjle\t.L").append(String.valueOf(cntJmp)).append("\n");
        }else if(((NodeConditional) statement).getExpression().getOperator().getValue().equals("GREATER")) {
            fw.append("\tjge\t.L").append(String.valueOf(cntJmp)).append("\n");
        }
        fw.append(".L"+ cntJmp +":\n");
        cntJmp++;
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
        for(NodeStatement node : statement.getStatementList()) {
            if(node instanceof NodeConditional) {
                addConditionalNode(node, fw);
            }
        }
    }

    private void addScanlnNode(NodeStatement statement, FileWriter fw) {
    }

    private void addPrintlNode(NodeStatement statement, FileWriter fw) {
    }

    private void addReturnNode(NodeStatement statement, FileWriter fw) {
    }
}
