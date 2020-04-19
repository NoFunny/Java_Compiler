package compiler.identifierTable;

import compiler.identifierTable.SemanticExceptioin.SemanticException;
import compiler.parser.AST.ArrayMember.ArrayMember;
import compiler.parser.AST.ArrayMember.ArrayMemberId;
import compiler.parser.AST.Else.NodeJustElse;
import compiler.parser.AST.Inits.*;
import compiler.parser.AST.Node;
import compiler.parser.AST.NodeArgsInit;
import compiler.parser.AST.NodeClass;
import compiler.parser.AST.NodeMainMethod;
import compiler.parser.AST.statement.*;
import compiler.parser.AST.value.Attachment;
import compiler.parser.AST.value.CallArrayMember;
import compiler.parser.AST.value.FuncCall;
import compiler.parser.AST.value.GenericValue;

import java.io.IOException;
import java.util.*;

public class Table implements GenericUnit {
    private String nameTable;
    private Map<String,GenericUnit> mainTable = new HashMap<>();
    private Table parentTable = null;
    private NodeStatement node;
    private Map<String, String> listInit =  new HashMap<>();
    private Map<String,String>previousVariable = new HashMap<>();

    public void setNameTable(String nameTable) {
        this.nameTable = nameTable;
    }

    public void setParentTable(Table parentTable) {
        this.parentTable = parentTable;
    }

    public void setNode(NodeStatement node) {
        this.node = node;
    }

    public Map<String, GenericUnit> getMainTable() {
        return mainTable;
    }

    public Table getParentTable() { return parentTable;}

    public NodeStatement getNode() {
        return node;
    }

    public String getNameTable() {
        return this.nameTable;
    }

    public void go(NodeClass root) throws SemanticException {
        nameTable = "GLOBAL_TABLE";
        next(root);
    }

    private void next(Node node) throws SemanticException {
        if (node instanceof NodeClass) {
            for (NodeInit nodeInit : ((NodeClass) node).getInitList()) {
                addInitNode(nodeInit);
            }

            Table newTable = new Table();
            newTable.setNameTable("MAIN_METHOD");
            newTable.setParentTable(this);
            newTable.next(((NodeClass) node).getMainMethod());
            mainTable.put("MainMethod", newTable);
        } else if (node instanceof CallFunc) {
            for (NodeArgsInit nodeArgsInit : ((CallFunc) node).getNodeArgsInitList()) {
                addArgInitNode(nodeArgsInit);
            }
            for (NodeStatement statement : ((CallFunc) node).getStatementList()) {
                if (statement != null) {
                    addStatement(statement);
                }
            }
        } else if (node instanceof NodeConditional) {
            for (NodeStatement statement : ((NodeConditional) node).getStatementList()) {
                if (statement != null) {
                    addStatement(statement);
                }
            }

            if (((NodeConditional) node).getElseNode() != null) {
                next(((NodeConditional) node).getElseNode());
            }
        } else if (node instanceof NodeLoop) {
            for (NodeStatement statement : ((NodeLoop) node).getStatementList()) {
                if (statement != null) {
                    addStatement(statement);
                }
            }
        } else if (node instanceof NodeJustElse) {
            for (NodeStatement statement : ((NodeJustElse) node).getStatementList()) {
                if (statement != null) {
                    addStatement(statement);
                }
            }
        } else if (node instanceof NodeMainMethod) {
            for (NodeStatement statement : ((NodeMainMethod) node).getStatementList()) {
                if (statement != null) {
                    addStatement(statement);
                }
            }
        }
    }

    private void addInitNode(NodeInit nodeInit) throws SemanticException {
        ForkInit tmpForkInit = nodeInit.getForkInit();

        if (tmpForkInit instanceof ForkInitVar ||
                tmpForkInit instanceof ForkInitArray) {
            String tmpNameID = nodeInit.getId().getValue();
            listInit.put(nodeInit.getId().getValue(), nodeInit.getDataType().getType());

            if (!containsKey(tmpNameID)) {
                mainTable.put(tmpNameID, new Id(nodeInit));
            } else {
                throw new SemanticException(String.format("%s already init in table <%s>",
                        tmpNameID,
                        nameTable));
            }
            if (tmpForkInit instanceof ForkInitVar) {
                if (!checkExpression(((ForkInitVar) tmpForkInit).getExpression())) {
                    throw new SemanticException(String.format("%s not init in table <%s>",
                            tmpForkInit,
                            nameTable));
                }
            }
            if (tmpForkInit instanceof ForkInitVar) {
                String tmpNameID1 = nodeInit.getId().getValue();

                String nameFunc = ((ForkInitVar)nodeInit.getForkInit()).getExpression().getlValue().getValue().getValue();

                if(((ForkInitVar) nodeInit.getForkInit()).getExpression().getlValue() instanceof FuncCall) {
                    if(!nodeInit.getDataType().getType().equals((((NodeInit)((Table)this.parentTable.mainTable.get(nameFunc)).getNode()).getDataType().getType()))) {
                        throw new SemanticException(String.format("error type variable: expected <%s> bun <%s> found in <%d:%d>",
                                (((NodeInit)((Table)this.parentTable.mainTable.get(nameFunc)).getNode()).getDataType().getType()),
                                nodeInit.getDataType().getType(),
                                nodeInit.getDataType().getLocation().getRow(),
                                nodeInit.getDataType().getLocation().getCol()));
                    }
                }

                        switch (((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getType()) {
                            case "INTEGER":
                                switch (listInit.get(tmpNameID1)) {
                                    case "INT":
                                        break;
                                    case "INTEGER":
                                        break;
                                    default:
                                        throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                                listInit.get(tmpNameID1),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getType(),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getRow(),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getCol()));
                                }
                                break;
                            case "FLOAT":
                                switch (listInit.get(tmpNameID1)) {
                                    case "FLOAT":
                                        break;
                                    case "FLOOAT":
                                        break;
                                    default:
                                        throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                                listInit.get(tmpNameID1),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getType(),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getRow(),
                                                ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getCol()));
                                }
                                break;
                            case "FLOOAT":
                                for (int i = 0; i < listInit.size(); i++) {
                                    switch (listInit.get(tmpNameID1)) {
                                        case "FLOAT":
                                            break;
                                        case "FLOOAT":
                                            break;
                                        default:
                                            throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                                    listInit.get(tmpNameID1),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getType(),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getRow(),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getCol()));
                                    }
                                }
                                break;
                            case "CHAAR":
                                for (int i = 0; i < listInit.size(); i++) {
                                    switch (listInit.get(tmpNameID1)) {
                                        case "CHAR":
                                            break;
                                        case "CHAAR":
                                            break;
                                        default:
                                            throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                                    listInit.get(tmpNameID1),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getType(),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getRow(),
                                                    ((ForkInitVar) tmpForkInit).getExpression().getlValue().getValue().getLocation().getCol()));
                                    }
                                }
                                break;
                        }
            }
        } else if (tmpForkInit instanceof CallFunc) {
            String tmpNameID = nodeInit.getId().getValue();

            if (!containsKey(tmpNameID)) {
                Table newTable = new Table();
                newTable.setNode(nodeInit);
                newTable.setNameTable(tmpNameID);
                newTable.setParentTable(this);
                newTable.next(tmpForkInit);
                mainTable.put(tmpNameID, newTable);

            } else {
                throw new SemanticException(String.format("%s already init in table <%s>",
                        tmpNameID,
                        nameTable));
            }
        }
    }

    private boolean checkExpression(NodeStatement statement) throws SemanticException {
        boolean result = false;
        NodeExpression expr = null;
        NodeInit node = (NodeInit) this.node;
        if (statement instanceof NodeLoop) {
            expr = ((NodeLoop) statement).getExpression();
        } else if (statement instanceof NodeConditional) {
            expr = ((NodeConditional) statement).getExpression();
        } else if (statement instanceof NodeReturn) {
            expr = ((NodeReturn) statement).getExpression();
            for(int i = 0; i < ((CallFunc)node.getForkInit()).getStatementList().size(); i++) {
                if(((CallFunc) node.getForkInit()).getStatementList().get(i) instanceof NodeInit) {
                    if(((NodeInit) ((CallFunc) node.getForkInit()).getStatementList().get(i)).getId().getValue().equals(expr.getlValue().getValue().getValue())) {
                        if (!listInit.get(((NodeInit) ((CallFunc) node.getForkInit()).getStatementList().get(i)).getId().getValue()).equals(node.getDataType().getType())) {
                            throw new SemanticException(String.format("error type return variable: expected <%s> bun <%s> found in <%d:%d>",
                                    node.getDataType().getType(),
                                    listInit.get(((NodeInit) ((CallFunc) node.getForkInit()).getStatementList().get(i)).getId().getValue()),
                                    expr.getlValue().getValue().getLocation().getRow(),
                                    expr.getlValue().getValue().getLocation().getCol()));
                        }
                    }
                }
            }
        } else if (statement instanceof NodePrintln) {
            expr = ((NodePrintln) statement).getExpression();
        }else if (statement instanceof NodeExpression) {
            expr = (NodeExpression) statement;
        }

        if (expr != null) {
            GenericValue gv;

            if ((gv = expr.getlValue()) != null) {
                result = checkGenericValue(gv);
            }
            if ((gv = expr.getrValue()) != null) {
                result = checkGenericValue(gv);
            }

            NodeExpression tmpExpr;
            if ((tmpExpr = expr.getlExpression()) != null) {
                result = checkExpression(tmpExpr);
            }
            if ((tmpExpr = expr.getrExpression()) != null) {
                result = checkExpression(tmpExpr);
            }
        }
        return result;
    }

    private boolean checkGenericValue(GenericValue gv) throws SemanticException {
        boolean result = false;

        if (!gv.getValue().getType().equals("STRING") &&
                (!gv.getValue().getType().equals("INTEGER") ||
                        !gv.getValue().getType().equals("OCTAL") ||
                        !gv.getValue().getType().equals("HEX") ||
                        !gv.getValue().getType().equals("FLOAT"))) {
            result = containsKey(gv.getValue().getValue());
        }
        if(gv instanceof Attachment && !result) {
            return false;
        }

        if (!result) {
            return true;
        }


        if(gv instanceof Attachment) {
            String nameResult = gv.getValue().getValue();

            if(((Attachment) gv).getExpression().getrValue() != null && !listInit.isEmpty()) {
                if (((Attachment) gv).getExpression().getlValue().getClass().toString().contains("GenericValue") && ((Attachment) gv).getExpression().getrValue().getClass().toString().contains("GenericValue")) {
                    switch (listInit.get(nameResult)) {
                        case "INT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "INTEGER":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {

                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOAT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOOAT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                    }
                } else if (((Attachment) gv).getExpression().getlValue().getClass().toString().contains("Number") && ((Attachment) gv).getExpression().getrValue().getClass().toString().contains("GenericValue")) {
                    switch (listInit.get(nameResult)) {
                        case "INT":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {

                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "INTEGER":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOAT":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOOAT":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            (listInit.get(((Attachment) gv).getExpression().getrValue().getValue().getValue())),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                    }
                } else if(((Attachment) gv).getExpression().getlValue().getClass().toString().contains("GenericValue") && ((Attachment) gv).getExpression().getrValue().getClass().toString().contains("Number")) {
                    switch (listInit.get(nameResult)) {
                        case "INT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getValue(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {

                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "INTEGER":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getValue(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOAT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getValue(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getValue()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOOAT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getValue(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getValue()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                    }
                } else if(((Attachment) gv).getExpression().getlValue().getClass().toString().contains("Number") && ((Attachment) gv).getExpression().getrValue().getClass().toString().contains("Number")) {
                    switch (listInit.get(nameResult)) {
                        case "INT":
                            switch (listInit.get(((Attachment) gv).getExpression().getlValue().getValue().getValue())) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getValue(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {

                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "INTEGER":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {
                                case "INTEGER":
                                    break;
                                case "INT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                            break;
                        case "FLOAT":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                        case "FLOOAT":
                            switch (((Attachment) gv).getExpression().getlValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getlValue().getValue().getLocation().getCol()));
                            }
                            switch (((Attachment) gv).getExpression().getrValue().getValue().getType()) {
                                case "FLOAT":
                                    break;
                                case "FLOOAT":
                                    break;
                                default:
                                    throw new SemanticException(String.format("Expected <%s> but <%s> found in <%d:%d>",
                                            listInit.get(nameResult),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getType(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getRow(),
                                            ((Attachment) gv).getExpression().getrValue().getValue().getLocation().getCol()));
                            }
                    }

                }
            }
        result = checkExpression(((Attachment) gv).getExpression());
    } else if (gv instanceof CallArrayMember) {
        ArrayMember am = ((CallArrayMember) gv).getArrayMember();

        if (am instanceof ArrayMemberId) {
            result = containsKey(((ArrayMemberId) am).getId().getValue());
        }
    } else if (gv instanceof FuncCall) {
        GenericUnit gu = findIdByName(gv.getValue().getValue());
        NodeStatement node = null;
        List<NodeArgsInit> argsInitList = null;

        if (gu != null) {
            if (gu instanceof Table) {
                node = ((Table) gu).getNode();
            }
        }

        if (node != null) {
            if (node instanceof NodeInit) {

                ForkInit tmpForkInit = ((NodeInit) node).getForkInit();

                if (tmpForkInit instanceof CallFunc) {
                    argsInitList = ((CallFunc) tmpForkInit).getNodeArgsInitList();
                }
            }
        }
        if (argsInitList != null) {
            if (argsInitList.size() != ((FuncCall) gv).getArgsCall().size()) {
                throw new SemanticException(String.format("invalid count args <%s> in <%d:%d>",
                        gv.getValue(),
                        gv.getValue().getLocation().getRow(),
                        gv.getValue().getLocation().getCol()));
            }
        }

        for (int i = 0; i < ((FuncCall) gv).getArgsCall().size(); i++) {
            GenericUnit dataType = findIdByName(((FuncCall) gv).getArgsCall().get(i).getValue().getValue());
            if (!argsInitList.get(i).getDataType().getValue()
                    .equals(((Id) dataType).getNode().getDataType().getValue()))
            {
                throw new SemanticException(String.format("invalid type args <%s> '%s %s' in <%d:%d>",
                        gv.getValue().getValue(),
                        argsInitList.get(i).getDataType().getValue(),
                        argsInitList.get(i).getId().getValue(),
                        gv.getValue().getLocation().getRow(),
                        gv.getValue().getLocation().getCol()));
            }

            result = checkGenericValue(((FuncCall) gv).getArgsCall().get(i));
        }
    } else if (gv != null) {
        result = true;
    }

        return result;
}

    private GenericUnit findIdByName(String name) {
        GenericUnit gu = null;
        for (String key : mainTable.keySet()) {
            if (key.equals(name)) {
                return mainTable.get(key);
            }
        }

        if (parentTable != null) {
            gu = parentTable.findIdByName(name);
        }

        return gu;
    }

    private void addArgInitNode(NodeArgsInit nodeArgsInit) throws SemanticException {
        String tmpNameID = nodeArgsInit.getId().getValue();
        if (!containsKey(tmpNameID)) {
            mainTable.put(tmpNameID, new ArgId(nodeArgsInit));
        } else {
            throw new SemanticException(String.format("%s already init in talbe <%s>",
                    tmpNameID,
                    nameTable));
        }
    }

    private boolean containsKey(String tmpNameID) {
        for (String key : mainTable.keySet()) {
            if (key.equals(tmpNameID)) {
                return true;
            }
        }

        boolean result = false;

        if (parentTable != null) {
            result = parentTable.containsKey(tmpNameID);
        }

        return result;
    }

    private void addStatement(NodeStatement statement) throws SemanticException {
        if (statement instanceof NodeInit) {
            addInitNode((NodeInit) statement);
        } else {
            if (statement instanceof NodeConditional ||
                    statement instanceof NodeLoop) {
                Table newTable = new Table();
                newTable.setNameTable(statement.toString()
                        .split("@")[0]);
                newTable.setParentTable(this);
                newTable.next(statement);
                mainTable.put(statement.toString(), newTable);
            }

            if (statement instanceof NodeScanln) {
                String id = ((NodeScanln) statement).getId().getValue();
                if (!containsKey(id)) {
                    throw new SemanticException(String.format("variable <%s> not init in table <%s>",
                            id,
                            nameTable));
                }
            } else if (!checkExpression(statement)) {
                throw new SemanticException(String.format("variable <%s> not init in table <%s>",
                        statement,
                        nameTable));
            }
        }
    }

    public void printTable() { printTable(this, "GLOBAL_TABLE"); }

    private void printTable(Table table, String nameTable) {
        System.out.println(String.format("\n\tTable %s:",
                nameTable));

        Set<String> table_keys = new TreeSet<>();

        for (String key : table.getMainTable().keySet()) {
            if (table.getMainTable().get(key) instanceof Id ||
                    table.getMainTable().get(key) instanceof ArgId) {
                System.out.println(String.format("%s %s",
                        key,
                        table.getMainTable().get(key)));
            } else if (table.getMainTable().get(key) instanceof Table) {
                System.out.println(String.format("%s %s",
                        key,
                        (table.getMainTable().get(key))));
                table_keys.add(key);
            }
        }

        for (String table_id : table_keys) {
            printTable((Table) table.getMainTable().get(table_id), table_id);
        }
    }
}