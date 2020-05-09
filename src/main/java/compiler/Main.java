package compiler;

import compiler.CodeGenerator.CodeGen;
import compiler.identifierTable.SemanticExceptioin.SemanticException;
import compiler.identifierTable.Table;
import compiler.lexer.LexerList;
import compiler.lexer.Token;
import compiler.lexer.Tokenizer;
import compiler.parser.AST.NodeClass;
import compiler.parser.Parser;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
    private static LinkedList<Token> tokenss = new LinkedList<>();
    private static Tokenizer tokenizer = null;
    private static Parser parser = null;
    private static NodeClass root = null;
    private static Table identifierTable = null;

    static lex Lexer;
    
    public static void main(String[] args) throws IOException {
        String input = readFile("src/main/resources/alg.java");
        String outputDir = "src/main/resources/out.txt";
        boolean flag = true;

        while (flag) {
            System.out.println("\n1 - --dumb-token" + '\n' + "2 - --dumb-parser" + '\n' + "3 - --dumb-asm" + '\n' + "4 - --compile" + '\n' + "5 - exit");
            System.out.println("Choose part:");
            Scanner in = new Scanner(System.in);
            int s = Integer.parseInt(in.nextLine());
            switch (s) {
                case 1:
                    System.out.println(input);
                    tokenizer = new Tokenizer(input);
                    System.out.println(tokenizer.getTokenss());
                    writeTokensToFile(outputDir);
                    break;
                case 2:
                    parser = new Parser(new LexerList(tokenizer));
                    root = parser.go();
                    parser.printTreeToFile();
                    identifierTable = new Table();
                    try {
                        identifierTable.go(root);
                    } catch (SemanticException e) {
                        System.out.println(String.format("\nERROR [semantic]: %s\n",
                                e.getMessage()));
                        return;
                    }
                    identifierTable.printTable();
                    break;
                case 3:
                    CodeGen cg = new CodeGen();
                    cg.go(root, identifierTable);
                    break;
                case 4:
                    Lexer = new lex();
                    Thread threadLex = new Thread(Lexer);
                    threadLex.start();
                    flag = false;
                    break;
                case 5:
                    flag = false;
                    break;
                default:
                    flag = false;
                    System.out.println("Error command!");
                    break;
            }
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

    private static void writeTokensToFile(String outputDir) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputDir));
            out.write(tokenss.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("An error was encountered during writing to the file: " + outputDir);
            System.out.println(e.getStackTrace());
        }

    }
}
