package compiler.CodeGenerator;

import compiler.identifierTable.SemanticExceptioin.SemanticException;
import compiler.identifierTable.Table;
import compiler.lexer.LexerList;
import compiler.lexer.Tokenizer;
import compiler.parser.AST.NodeClass;
import compiler.parser.Parser;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CodeGenTest extends TestCase {

    public void testGo() throws IOException {
        String input = readFile("src/main/resources/testCodeGen1.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG1.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

        public void testGo2 () throws IOException {
            String input = readFile("src/main/resources/testCodeGen2.java");
            Tokenizer lexer = new Tokenizer(input);
            Parser parser = new Parser(new LexerList(lexer));
            NodeClass root = parser.go();
            Table identifierTable = new Table();
            try {
                identifierTable.go(root);
            } catch (SemanticException e) {
                System.out.println(String.format("\nERROR [semantic]: %s\n",
                        e.getMessage()));
                return;
            }
            CodeGen cg = new CodeGen();
            cg.go(root, identifierTable);
            String expected = readFile("src/main/resources/expectedTestCG2.asm");
            String actual = readFile("main.s");
            Assert.assertEquals(expected, actual);
        }

    public void testGo3 () throws IOException {
        String input = readFile("src/main/resources/testCodeGen3.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG3.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

    public void testGo4 () throws IOException {
        String input = readFile("src/main/resources/testCodeGen4.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG4.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

    public void testGo5 () throws IOException {
        String input = readFile("src/main/resources/testCodeGen5.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG5.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

    public void testGo6 () throws IOException {
        String input = readFile("src/main/resources/testCodeGen6.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG6.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

    public void testGo7 () throws IOException {
        String input = readFile("src/main/resources/testCodeGen7.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
        } catch (SemanticException e) {
            System.out.println(String.format("\nERROR [semantic]: %s\n",
                    e.getMessage()));
            return;
        }
        CodeGen cg = new CodeGen();
        cg.go(root, identifierTable);
        String expected = readFile("src/main/resources/expectedTestCG7.asm");
        String actual = readFile("main.s");
        Assert.assertEquals(expected, actual);
    }

        public static String readFile (String fileDir){

            try (BufferedReader reader = new BufferedReader(new FileReader(fileDir))) {
                String result = "";
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result = result.concat(line) + "\n";
                }
                return result;
            } catch (IOException e) {
                System.out.println("Error was encountered during loading of the file: " + fileDir);
                e.printStackTrace();
            }
            return null;
        }
    }
