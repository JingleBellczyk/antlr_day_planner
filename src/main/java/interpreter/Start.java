package interpreter;

import grammar.GrammarLexer;
import grammar.GrammarParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class Start {

    public static List<String> start(String command) {

        CharStream inp = CharStreams.fromString(command);

        GrammarLexer lex = new GrammarLexer(inp);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        GrammarParser par = new GrammarParser(tokens);

        ParseTree tree = par.prog();

        PlannerVisitor v = new PlannerVisitor(inp,tokens);
        return v.visit(tree);
    }
}
