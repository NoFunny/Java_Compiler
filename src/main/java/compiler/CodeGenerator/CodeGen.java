package compiler.CodeGenerator;

import compiler.identifierTable.Table;
import compiler.lexer.Token;
import compiler.parser.AST.ArrayMember.ArrayMemberId;
import compiler.parser.AST.Inits.*;
import compiler.parser.AST.Node;
import compiler.parser.AST.NodeArgsInit;
import compiler.parser.AST.NodeClass;
import compiler.parser.AST.NodeMainMethod;
import compiler.parser.AST.statement.*;
import compiler.parser.AST.value.*;
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
    private static NodeStatement returnVariable;
    private static NodeScanln scanlnNode;
    private static int offsetForScanln = 0;
    private static int stringForOutInput = 0;
    private static String startLoop = "";

    public void go(NodeClass root, Table idTable) throws IOException {
        FileWriter fw = new FileWriter(dir.getName(), false);
        next(root, fw);
        fw.close();
    }

    private void next(Node node, FileWriter fw) throws IOException {
        if (node instanceof NodeClass) {
            ArrayList<NodeArgsInit> nodeArgsInits = new ArrayList<>();
            for (int i = 0; i < ((NodeClass) node).getInitList().size(); i++) {
                if (!((NodeClass) node).getInitList().get(i).getForkInit().toString().isEmpty())
                fw.write(".text\n\n");
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
                        fw.append("\tmovq\t %rdi, -").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 2) {
                        offset += 4;
                        fw.append("\tmovl\t %esi, -").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 3) {
                        offset += 4;
                        fw.append("\tmovl\t %rdi, -").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }
                }else if(nodeArgs.getDataType().getType().equals("FLOAT")) {
                    offset = 24;
                    if(i == 1) {
                        fw.append("\tmovl\t %rdi, -").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 2) {
                        offset += 4;
                        fw.append("\tmovss\t %xmm0,").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }else if (i == 3) {
                        offset += 4;
                        fw.append("\tmovss\t %xmm0,").append(String.valueOf(offset)).append("(%rbp)\n");
                        pointInit.put(nodeArgs.getId().getValue(),offset);
                        i++;
                    }
                }
            }
            for(NodeInit nodeInit : ((NodeClass) node).getInitList()) {
                    for(int count = 0; count < ((CallFunc)nodeInit.getForkInit()).getStatementList().size(); count++) {
                        if(((CallFunc)nodeInit.getForkInit()).getStatementList().get(count) instanceof NodeReturn) {
                            returnVariable = ((CallFunc)nodeInit.getForkInit()).getStatementList().get(count);
                        }
                }
            }
            for (NodeInit nodeInit : ((NodeClass) node).getInitList()) {
                for (NodeStatement statement : ((CallFunc) nodeInit.getForkInit()).getStatementList()) {
                    if (statement != null) {
                        addStatement(statement, fw);
                    }
                }
            }
            if(((NodeClass) node).getMainMethod() != null) {
                codeGenMainMethod(((NodeClass) node).getMainMethod(),fw);
            }
        }
    }

    private void codeGenMainMethod(NodeMainMethod mainMethod, FileWriter fw) throws IOException {
        fw.append("\n.globl main\n.type main, @function\n");
        pointVarInit.clear();
        fw.append("main:\n");
        fw.append("\tpushq\t%rbp\n\tmovq\t%rsp, %rbp\n");
        for (NodeStatement statement : mainMethod.getStatementList()) {
            if (statement instanceof NodeInit) {
                if (((NodeInit) statement).getForkInit() instanceof ForkInitArray) {
                    if (((ForkInitArray) ((NodeInit) statement).getForkInit()).getIndex() instanceof ArrayMemberId &&
                            ((ForkInitArray) ((NodeInit) statement).getForkInit()).getIndex().getId() instanceof Token) {
                        fw.append("\tpushq\t%r15\n\tpushq\t%r14\n\tpushq\t%r13\n\tpushq\t%r12\n\tpushq\t%rbx\n");
                        fw.append("\tsubq\t$40, %rsp\n\tmovq\t%rsp, %rax\n\tmovq\t%rax, %rbx\n");
                    } else {
                        fw.append("\tsubq\t$32, %rsp\n");
                    }
                }
            }else if(statement instanceof NodeScanln) {
                scanlnNode = (NodeScanln) statement;
            }
        }
        offset = 0;
        for (NodeStatement statement : mainMethod.getStatementList()) {
            if (statement != null) {
                if(statement instanceof NodeInit) {
                if(((NodeInit)statement).getForkInit() instanceof ForkInitArray) {
                    addArray(statement, fw);
                }else if (((ForkInitVar)((NodeInit) statement).getForkInit()).getExpression().getlValue() instanceof FuncCall) {
                    addFuncCall(statement, fw);
                } else {
                    addStatement(statement, fw);
                }
                }else{
                    addStatement(statement, fw);
                }
            }
        }
        addReturnNode(fw);
        if(stringForOutInput != 0) {
            for (int i = 0; i < stringForOutInput; i++) {
                if(i !=0) {
                    fw.append(".LC").append(String.valueOf(i)).append(":\n");
                    fw.append("\t.string " + "\"" + "%d\\n" + "\"" + "\n");
                }else {
                    fw.append(".LC").append(String.valueOf(i)).append(":\n");
                    fw.append("\t.string " + "\"" + "%d" + "\"" + "\n");
                }
            }
        }
        if(reservReg != 0) {
            for (int i = 0; i < reservReg; i++) {
                fw.append(".FL").append(String.valueOf(i)).append(":\n");
                fw.append("\t.long " + 1067030938 + "\n");
            }
        }
    }

    private void addFuncCall(NodeStatement statement, FileWriter fw) throws IOException {
        ArrayList<GenericValue> gv = new ArrayList<>();

        gv.addAll(0,((FuncCall)((ForkInitVar)((NodeInit)statement).getForkInit()).getExpression().getlValue()).getArgsCall());
        for(int i = 0; i < gv.size(); i++) {
            fw.append("\tmovq\t-").append(String.valueOf(pointVarInit.get(gv.get(i).getValue().getValue()))).append("(%rbp), %rax\n");
            fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(gv.get(i+1).getValue().getValue()))).append("(%rbp), %edx\n");
            break;
        }
        fw.append("\tmovl\t%edx, %esi\n\tmovq\t%rax, %rdi\n");
        fw.append("\tcall\t").append(((ForkInitVar) ((NodeInit) statement).getForkInit()).getExpression().getlValue().getValue().getValue()).append("\n");
        offsetInit+=4;
        fw.append("\tmovl\t%eax, -").append(String.valueOf(offsetInit)).append("(%rbp)\n");
        pointVarInit.put(((NodeInit) statement).getId().getValue(), offsetInit);
    }

    private void addArray(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(((ForkInitArray) ((NodeInit) statement).getForkInit()).getIndex().getId().getValue()))).append("(%rbp), %eax\n");
        fw.append("\tmovslq\t%eax, %rdx\n\tsubq\t$1, %rdx\n\tmovq\t%rdx, -64(%rbp)\n\tmovslq\t%eax, %rdx\n\tmovq\t%rdx, %r14\n\tmovl\t$0, %r15d\n");
        fw.append("\tmovslq\t%eax, %rdx\n\tmovq\t%rdx, %r12\n\tmovl\t$0, %r13d\n\tcltq\n");
        fw.append("\tleaq\t0(,%rax,4), %rdx\n");
        fw.append("\tmovl\t$16, %eax\n\tsubq\t$1, %rax\n\taddq\t%rdx, %rax\n\tmovl\t$16, %ecx\n\tmovl\t$0, %edx\n\tdivq\t%rcx\n");
        fw.append("\timulq\t$16, %rax, %rax\n\tsubq\t%rax, %rsp\n\tmovq\t%rsp, %rax\n\taddq\t$3, %rax\n\tshrq\t$2, %rax\n\tsalq\t$2, %rax\n");
        System.out.println(offsetInit);
        System.out.println(offset);
        offsetInit+=16;
        pointVarInit.put(((NodeInit) statement).getId().getValue(), offsetInit);
        fw.append("\tmovq\t%rax, -"+ offsetInit +"(%rbp)\n");
    }

    private void addStatement(NodeStatement statement, FileWriter fw) throws IOException {
        if (statement instanceof NodeInit) {
            addInitNode((NodeInit) statement, fw);
        } else if (statement instanceof NodeLoop) {
            addLoopNode((NodeLoop) statement, fw);
        } else if (statement instanceof NodeScanln) {
            addScanlnNode(statement, fw);
        }else if (statement instanceof NodeExpression) {
            addExpressionNode(statement, fw);
        } else if (statement instanceof NodePrintln) {
            addPrintlNode(statement, fw);
        }
    }

    private void addExpressionNode(NodeStatement statement, FileWriter fw) throws IOException {
        if(((NodeExpression)statement).getlValue() instanceof CallArrayMember) {
            fw.append("\tmovq\t-").append(String.valueOf(offsetInit)).append("(%rbp), %rax\n");
            if(offset == 0)
                fw.append("\tmovl\t$").append(((NodeExpression) statement).getrExpression().getlValue().getValue().getValue()).append(", (%rax)\n");
            else
                fw.append("\tmovl\t$").append(((NodeExpression) statement).getrExpression().getlValue().getValue().getValue()).append(", ").append(String.valueOf(offset)).append("(%rax)\n");
            if(((NodeExpression) statement).getrExpression().getlValue().getValue().getType().equals("INT") || ((NodeExpression) statement).getrExpression().getlValue().getValue().getType().equals("INTEGER") ||
                    ((NodeExpression) statement).getrExpression().getlValue().getValue().getType().equals("FLOAT") || ((NodeExpression) statement).getrExpression().getlValue().getValue().getType().equals("FLOOAT")) {
                offset+=4;
            }else if(((NodeExpression) statement).getrExpression().getlValue().getValue().getType().equals("CHAR")) {
                offset+=1;
            }
        }
    }

    private void addInitNode(NodeInit node, FileWriter fw) throws IOException {
        if(node.getForkInit() instanceof ForkInitArray) {
            return;
        }
        offsetForScanln = 80;
        switch (node.getDataType().getType()) {
            case "INT":
                offsetInit += 4;
                if(scanlnNode != null) {
                    if (node.getId().getValue().equals(scanlnNode.getId().getValue())) {
                        fw.append("\tmovl\t $"+((ForkInitVar) node.getForkInit()).getExpression().getlValue().getValue().getValue()+", -"+offsetForScanln+"(%rbp)\n");
                        pointVarInit.put(node.getId().getValue(), offsetForScanln);
                    }else {
                        offsetInit = 52;
                        fw.append("\tmovl\t $"+((ForkInitVar) node.getForkInit()).getExpression().getlValue().getValue().getValue()+", -"+offsetInit+"(%rbp)\n");
                        pointVarInit.put(node.getId().getValue(), offsetInit);
                    }
                    break;

                } else {
                    fw.append("\tmovl\t $"+((ForkInitVar) node.getForkInit()).getExpression().getlValue().getValue().getValue()+", -"+  offsetInit +"(%rbp)\n");
                    pointVarInit.put(node.getId().getValue(), offsetInit);
                }
                break;
            case "CHAR":
                offsetInit += 1;
                pointVarInit.put(node.getId().getValue(), offsetInit);
                break;
            case "FLOAT":
            case "FLOOAT":
                fw.append("\tmovss\t .FL").append(String.valueOf(reservReg)).append("(%rip), -%xmm0\n");
                reservReg += 1;
                offsetFloat += 4;
                fw.append("\tmovss\t %xmm0, -"+ offsetFloat + offsetInit +"(%rbp)\n");
                pointVarInit.put(node.getId().getValue(), offsetFloat + offsetInit);
                break;
        }
    }
    private void checkOnGenericValueOrCallArrayMember(NodeStatement statement, FileWriter fw) throws IOException {
        if(((NodeConditional)statement).getExpression().getlValue() instanceof CallArrayMember) {
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
                fw.append("\tmovq\t -").append(String.valueOf(pointInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
            } else if (pointVarInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                fw.append("\tmovq\t -").append(String.valueOf(pointVarInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
            }
            fw.append("\taddq\t%rdx, %rax\n");
            fw.append("\tmovl\t(%rax), %eax\n");
        }else if(((NodeConditional)statement).getExpression().getlValue() instanceof GenericValue) {
            if (((NodeConditional) statement).getExpression().getlValue() instanceof GenericValue) {
                if (pointInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                    fw.append("\tmovl\t-").append(String.valueOf(pointInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
                } else if (pointVarInit.containsKey(((NodeConditional) statement).getExpression().getlValue().getValue().getValue())) {
                    fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(((NodeConditional) statement).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
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
                fw.append("\tcmpl\t%eax, -").append(String.valueOf(pointInit.get(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
            }else if(pointVarInit.containsKey(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue())); {
                fw.append("\tcmpl\t%eax, -").append(String.valueOf(pointVarInit.get(((NodeConditional) statement).getExpression().getrExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
            }
        }
    }
    private void addConditionalNode(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append(".L").append(String.valueOf(cntJmp)).append(":\n");
        cntJmp++;
       checkOnGenericValueOrCallArrayMember(statement, fw);
        if(((NodeConditional)statement).getExpression().getOperator().getValue().getType().equals("LESS")) {
            fw.append("\tjle\t.L").append(String.valueOf(cntJmp)).append("\n");
        }else if(((NodeConditional)statement).getExpression().getOperator().getValue().getType().equals("GREATHER")) {
            fw.append("\tjge\t.L").append(String.valueOf(cntJmp)).append("\n");
        }
        for(NodeStatement expression : ((NodeConditional) statement).getStatementList()) {
            if(((NodeExpression)expression).getlValue() instanceof Attachment) {
                nodeAttachment((Attachment) ((NodeExpression) expression).getlValue(), fw);
            }
        }
        fw.append(".L").append(String.valueOf(cntJmp)).append(":\n");
        cntJmp++;
    }

    private void nodeAttachment(Attachment expression, FileWriter fw) throws IOException {
        if (expression.getExpression() instanceof NodeExpression) {
            if (expression.getExpression().getrValue() == null) {
                if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tmovl\t-").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("(%rbp), %eax\n");
                    }
                    fw.append("\tcltq\n");
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                    }
                    if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t-").append(String.valueOf(pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
                    } else if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t-").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
                    }
                    fw.append("\taddq\t%rdx, %rax\n");
                    fw.append("\tmovl\t(%rax), %eax\n");
                    if (pointVarInit.containsKey(expression.getValue().getValue())) {
                        fw.append("\tmovl\t%eax, -").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
                    } else if (pointInit.containsKey(expression.getValue().getValue())) {
                        fw.append("\tmovl\t%eax, -").append(String.valueOf(pointInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
                    }
                    return;
                }
            }
                int offsetExpr = 0;
                if (expression.getExpression().getlValue() instanceof Number && expression.getExpression().getrValue() instanceof Number) {
                    if (expression.getExpression().getlValue().getValue().getType().equals("INTEGER") || expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                        if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS"))
                            fw.append("\tmovl\t $").append(String.valueOf(Integer.parseInt(expression.getExpression().getlValue().getValue().getValue()) + Integer.parseInt(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
                        else if (expression.getExpression().getOperator().getValue().getType().equals("BINARSUB"))
                            fw.append("\tmovl\t $").append(String.valueOf(Integer.parseInt(expression.getExpression().getlValue().getValue().getValue()) - Integer.parseInt(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
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
                        fw.append("\tmovl\t-").append(String.valueOf(offsetExpr)).append("(%rbp), %eax\n");
                    } else if (expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                        offsetExpr += 4;
                        fw.append("\tmovl\t-").append(String.valueOf(offsetExpr)).append("(%rbp), %eax\n");
                    } else if (expression.getExpression().getlValue().getValue().getType().equals("CHAR")) {
                        offsetExpr += 4;
                        fw.append("\tmovl\t-").append(String.valueOf(offsetExpr)).append("(%rbp), %eax\n");
                    }
                    if (expression.getExpression().getrValue() instanceof CallArrayMember) {
                        fw.append("\tcltq\n");
                        if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue())) {
                            fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                        } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue())) {
                            fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getrValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                        }
                        if (pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue())) {
                            fw.append("\tmovq\t -").append(String.valueOf(pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()))).append("(%rbp), %rax\n");
                        } else if (pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue())) {
                            fw.append("\tmovq\t -").append(String.valueOf(pointInit.get(expression.getExpression().getrValue().getValue().getValue()))).append("(%rbp), %rax\n");
                        } else if (expression.getExpression().getlValue() instanceof GenericValue) {
                            if (expression.getExpression().getrValue() instanceof CallArrayMember) {

                            } else if (expression.getExpression().getrValue() instanceof Number) {

                            }
                        }
                    }
                } else if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                    offsetExpr += 4;
                    fw.append("\tmovl\t-").append(String.valueOf(offsetExpr)).append("(%rbp), %eax\n");
                    fw.append("\tcltq\n");
                    if (pointVarInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointVarInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                    } else if (pointInit.containsKey(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue())) {
                        fw.append("\tleaq\t0(,%rax,").append(String.valueOf(pointInit.get(((ArrayMemberId) ((CallArrayMember) expression.getExpression().getlValue()).getArrayMember()).getId().getValue()))).append("), %rdx\n");
                    }
                    if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t -").append(String.valueOf(pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
                    } else if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                        fw.append("\tmovq\t -").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append("(%rbp), %rax\n");
                    }
                }

                //Осталось условие только для genericValue
                fw.append("\taddq\t%rdx, %rax\n");
                if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
                    fw.append("\taddl\t$").append(expression.getExpression().getlValue().getValue().getValue()).append(", %eax\n");
                } else if (expression.getExpression().getOperator().getValue().getType().equals("BINARSUB")) {
                    if (expression.getExpression().getlValue() instanceof CallArrayMember) {
                        fw.append("\tmovl\t(%rax), %eax\n\tsubl\t$").append(expression.getExpression().getrValue().getValue().getValue()).append(", %eax\n");
                    } else {
                        if (expression.getExpression().getlValue().toString().contains("GenericValue")) {
                            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx,%eax\n");
                            } else if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$").append(String.valueOf(pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx,%eax\n");
                            }
                        } else if (expression.getExpression().getlValue().toString().contains("Number")) {
                            if (expression.getExpression().getlValue().getValue().getType().equals("INTEGER")) {
                                fw.append("\tmovl\t(%rax), %eax\n\tmovl\t$").append(expression.getExpression().getlValue().getValue().getValue()).append(",%edx\n\tsubl\t%eax, %edx\n\tmovl\t%edx, %eax\n");
                            } else if (expression.getExpression().getlValue().getValue().getType().equals("FLOAT")) {
                                fw.append("\tmovl\t(%rax), %eax\n\tcvtsi2sdl\t %eax, %xmm1\n\tmovsd\t.LC").append(String.valueOf(reservReg)).append("(%rip), %xmm0\n\tsubsd\t%smm1,%xmm0\n\tcvttsd2sil\t%xmm0, %eax\n");
                                reservReg++;
                            }
                        }
                    }
                }
                if (pointVarInit.containsKey(expression.getValue().getValue())) {
                    fw.append("\tmovl\t%eax, -").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
                } else if (pointInit.containsKey(expression.getValue().getValue())) {
                    fw.append("\tmovl\t%eax, -").append(String.valueOf(pointInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
                }
        }
    }

    private void checkGenericValue(Attachment expression, FileWriter fw) throws IOException {
        if (expression.getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(String.valueOf(pointInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(String.valueOf(pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(String.valueOf(pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()))).append(String.valueOf(pointInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
        } else if (expression.getExpression().getOperator().getValue().getType().equals("BINASUB")) {
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointVarInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointVarInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointVarInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointVarInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
            if (pointInit.containsKey(expression.getExpression().getlValue().getValue().getValue()) || pointInit.containsKey(expression.getExpression().getrValue().getValue().getValue()))
                fw.append("\tmovl\t $").append(String.valueOf(pointInit.get(expression.getExpression().getlValue().getValue().getValue()) - pointInit.get(expression.getExpression().getrValue().getValue().getValue()))).append(",\t-").append(String.valueOf(pointVarInit.get(expression.getValue().getValue()))).append("(%rbp)\n");
        }
    }



    private void addLoopNode(NodeLoop statement, FileWriter fw) throws IOException {
//        pointVarInit.forEach((x,y) -> System.out.println(y));
        cntJmp+=1;
        fw.append("\tjmp\t\t.L").append(String.valueOf(cntJmp)).append("\n");
        startLoop = startLoop.concat("\n.L").concat(String.valueOf(cntJmp)).concat(":\n");
//        fw.append("\tjmp\t\t.L").append(String.valueOf(cntJmp)).append("\n.L").append(String.valueOf(cntJmp)).append(":\n");
        cntJmp++;
        startLoop = startLoop.concat("\tmovl\t -").concat(String.valueOf(pointVarInit.get(statement.getExpression().getlValue().getValue().getValue()))).concat("(%rbp), %eax\n");
//        fw.append("\tmovl\t -").append(String.valueOf(pointVarInit.get(statement.getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        if(pointVarInit.containsKey(statement.getExpression().getrExpression().getlValue().getValue().getValue())) {
            startLoop = startLoop.concat("\tcmpl\t -").concat(String.valueOf(pointVarInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()))).concat("(%rbp), %eax\n");
//            fw.append("\tcmpl\t -").append(String.valueOf(pointVarInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        }else if (pointInit.containsKey(statement.getExpression().getrExpression().getlValue().getValue().getValue())) {
            startLoop = startLoop.concat("\tcmpl\t -").concat(String.valueOf(pointInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()))).concat("(%rbp), %eax\n");
//            fw.append("\tcmpl\t -").append(String.valueOf(pointInit.get(statement.getExpression().getrExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        }
        startLoop = startLoop.concat("\tjl\t.L").concat(String.valueOf(cntJmp)).concat("\n");
//        fw.append("\tjl\t.L").append(String.valueOf(cntJmp)).append("\n");
        if(pointInit.containsKey(statement.getExpression().getlValue().getValue().getValue())) {
            startLoop = startLoop.concat("\tmovl\t-").concat(String.valueOf(pointInit.get(((NodeReturn) returnVariable).getExpression().getlValue().getValue().getValue()))).concat("(%rbp), %eax\n");
//            fw.append("\tmovl\t-").append(String.valueOf(pointInit.get(((NodeReturn) returnVariable).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        }else if (pointVarInit.containsKey(statement.getExpression().getlValue().getValue().getValue())) {
            startLoop = startLoop.concat("\tmovl\t-").concat(String.valueOf(pointVarInit.get(((NodeReturn) returnVariable).getExpression().getlValue().getValue().getValue()))).concat("(%rbp), %eax\n");
//            fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(((NodeReturn) returnVariable).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        }
        startLoop = startLoop.concat("\tpopq\t%rbp\n\tret\n");
//        fw.append("\tpopq\t%rbp\n\tret\n");
        for(NodeStatement node : statement.getStatementList()) {
            if(node instanceof NodeConditional) {
                addConditionalNode(node, fw);
            }else if(node instanceof NodeExpression) {
                if(((NodeExpression) node).getlValue() instanceof Attachment) {
                    if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue() != null && ((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue() != null) {
                        if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getOperator().getValue().getType().equals("BINARPLUS")) {
                            if(pointInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\taddl\t$").append(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue()).append(", -").append(String.valueOf(pointInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
                            }else if(pointVarInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\taddl\t$").append(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue()).append(", -").append(String.valueOf(pointVarInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
                            }
                        }else if(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getOperator().getValue().getType().equals("BINARSUB")) {
                            if(pointInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tsubl\t$").append(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue()).append(", -").append(String.valueOf(pointInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
                            }else if(pointVarInit.containsKey(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue())) {
                                fw.append("\tsubl\t$").append(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getrValue().getValue().getValue()).append(", -").append(String.valueOf(pointVarInit.get(((Attachment) ((NodeExpression) node).getlValue()).getExpression().getlValue().getValue().getValue()))).append("(%rbp)\n");
                            }
                        }
                    }
                }
            }
        }
        System.out.println(startLoop);
        fw.append(startLoop);
    }

    private void addScanlnNode(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append("\tleaq\t-").append(String.valueOf(offsetForScanln)).append("(%rbp), %rax\n");
        fw.append("\tmovq\t%rax, %rsi\n");
        fw.append("\tmovl\t$.LC").append(String.valueOf(stringForOutInput)).append(", %edi\n");
        stringForOutInput=+1;
        fw.append("\tmovl\t$0, %eax\n");
        fw.append("\tcall\t__isoc99_scanf\n");
    }

    private void addPrintlNode(NodeStatement statement, FileWriter fw) throws IOException {
        fw.append("\tmovl\t-").append(String.valueOf(pointVarInit.get(((NodePrintln) statement).getExpression().getlValue().getValue().getValue()))).append("(%rbp), %eax\n");
        fw.append("\tmovl\t%eax, %esi\n");
        fw.append("\tmovl\t$.LC").append(String.valueOf(stringForOutInput)).append(", %edi\n");
        stringForOutInput+=1;
        fw.append("\tmovl\t$0, %eax\n");
        fw.append("\tcall\tprintf\n");
    }

    private void addReturnNode(FileWriter fw) throws IOException {
//        if(((NodeReturn)statement).getExpression() instanceof NodeExpression)
        fw.append("\tnop\n");
        fw.append("\tmovq\t%rbx, %rsp\n");
        fw.append("\tleaq\t-40(%rbp), %rsp\n");
        fw.append("\tpopq\t%rbx\n\tpopq\t%r12\n\tpopq\t%r13\n\tpopq\t%r14\n\tpopq\t%r15\n\tpopq\t%rbp\n\tret\n");
    }
}
