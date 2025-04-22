package interpreter;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.Scanner;

public class Start {
    public static void start() {

        // Odczyt jednej linii z konsoli (kończy się po Enterze)
        Scanner scanner = new Scanner(System.in);
        System.out.print("Wprowadź polecenie: ");
        String inputLine = scanner.nextLine();

        // Tworzenie strumienia wejściowego z linii
        CharStream inp = CharStreams.fromString(inputLine);

//        CharStream inp = null;
//        try {
//            inp = CharStreams.fromFileName("we.first");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        CharStream inp = CharStreams.fromString("mail list 10");
//        CharStream inp = CharStreams.fromString("((0 and 1) or (1 or 0))","wejście");
//        CharStream inp = null;
//        try {
//            inp = CharStreams.fromStream(System.in);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        GrammarLexer lex = new GrammarLexer(inp);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        GrammarParser par = new GrammarParser(tokens);

        ParseTree tree = par.prog();

        PlannerVisitor v = new PlannerVisitor(inp,tokens);
        Object res = v.visit(tree);
        System.out.printf("Wynik: %d\n", res);
    }
}
