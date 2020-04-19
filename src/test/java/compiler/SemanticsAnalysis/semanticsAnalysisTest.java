package compiler.SemanticsAnalysis;

import compiler.identifierTable.SemanticExceptioin.SemanticException;
import compiler.identifierTable.Table;
import compiler.lexer.LexerList;
import compiler.lexer.Tokenizer;
import compiler.parser.AST.NodeClass;
import compiler.parser.Parser;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class semanticsAnalysisTest{
    //Тест на ошибку инициализации
    @Test
    public void semanticsTest1() {
        String input = readFile("src/main/resources/semanticsTest1.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("Expected <FLOOAT> but <INTEGER> found in <17:21>"));
        }
    }
    //Тест на ошибку в арифметических операциях, при несовпадении типов данных
    @Test
    public void semanticsTest2() {
        String input = readFile("src/main/resources/semanticsTest2.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("Expected <INT> but <FLOOAT> found in <19:14>"));
        }
    }

    //Тест на несовпадение типов данных при вызове функции
    @Test
    public void semanticsTest3() {
        String input = readFile("src/main/resources/semanticsTest3.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("invalid type args <getMin> 'int size' in <23:25>"));
        }
    }

    //Тест на ошибочное количество аргументов при вызове функции
    @Test
    public void semanticsTest4() {
        String input = readFile("src/main/resources/semanticsTest4.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("invalid count args <loc<23:25> ID 'getMin'> in <23:25>"));
        }
    }

    //Тест на проверку переопределения переменной
    @Test
    public void semanticsTest5() {
        String input = readFile("src/main/resources/semanticsTest5.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("size already init in table <MAIN_METHOD>"));
        }
    }

    //Тест на проверку использования необьявленной переменной
    @Test
    public void semanticsTest6() {
        String input = readFile("src/main/resources/semanticsTest6.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("variable <c> not init in table <MAIN_METHOD>"));
        }
    }

    //Тест на проверку несовпадения типов функции и Return(Ошибка в обьявлении)
    @Test
    public void semanticsTest7() {
        String input = readFile("src/main/resources/semanticsTest7.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("error type return variable: expected <CHAAR> bun <INT> found in <11:19>"));
        }
    }

    //Тест на проверку несовпадения типов функции и Return(Ошибка переменной Return)
    @Test
    public void semanticsTest8() {
        String input = readFile("src/main/resources/semanticsTest8.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("error type return variable: expected <INT> bun <FLOOAT> found in <11:19>"));
        }
    }

    //Тест на проверку переменной, которой присваевается результат работы функции
    @Test
    public void semanticsTest9() {
        String input = readFile("src/main/resources/semanticsTest9.java");
        Tokenizer lexer = new Tokenizer(input);
        Parser parser = new Parser(new LexerList(lexer));
        NodeClass root = parser.go();
        Table identifierTable = new Table();
        try {
            identifierTable.go(root);
            fail();
        } catch (SemanticException e) {
            assertThat(e.getMessage(), is("error type variable: expected <INT> bun <CHAAR> found in <22:13>"));
        }
    }

    public static String readFile(String fileDir) {

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
