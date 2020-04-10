package compiler.identifierTable;

import compiler.identifierTable.SemanticExceptioin.SemanticException;
import compiler.lexer.LexerList;
import compiler.lexer.Tokenizer;;
import compiler.parser.AST.NodeClass;
import compiler.parser.Parser;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class TableTest extends TestCase {


    public void testGo() {
        String input = readFile("src/main/resources/TableTest.java");
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
        identifierTable.printTable();
        Assert.assertEquals(true, identifierTable.getMainTable().containsKey("MainMethod"));
    }

    public void testGo2() {
        String input = readFile("src/main/resources/TableTest.java");
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
        identifierTable.printTable();
        Assert.assertEquals("GLOBAL_TABLE", identifierTable.getNameTable());
    }

    public void testGo3() {
        String input = readFile("src/main/resources/TableTest.java");
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
        identifierTable.printTable();
        Assert.assertEquals("[MainMethod]", identifierTable.getMainTable().keySet().toString());
    }

    public void testGo4() {
        String input = readFile("src/main/resources/TableTest.java");
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
        identifierTable.printTable();
        ArrayList<String> key;
        key = TestHelper(identifierTable, "GLOBAL_TABLE");
        int count = 0;
        for(String k : key) {
            if (k.equals("a")) {
                Assert.assertEquals("a", k);
                count++;
            } else if (k.equals("b")) {
                Assert.assertEquals("b", k);
                count++;
            }
        }
        if (count != 2)
            Assert.fail();
    }

    private ArrayList<String> TestHelper(Table table, String nameTable) {

        Set<String> table_keys = new TreeSet<>();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> key_returned = new ArrayList<>();

        for (String key : table.getMainTable().keySet()) {
            if (table.getMainTable().get(key) instanceof Id ||
                    table.getMainTable().get(key) instanceof ArgId) {
                if (key.equals("a")) {
                    keys.add(key);
                }
                if (key.equals("b")) {
                    keys.add(key);
                }
            } else if (table.getMainTable().get(key) instanceof Table) {
                table_keys.add(key);
            }
            if (keys.toArray().length == 2) {
                return keys;
            }
        }

        for (String table_id : table_keys) {
            key_returned = TestHelper((Table) table.getMainTable().get(table_id), table_id);
        }
        return key_returned;
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