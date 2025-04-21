//package compiler;
//import grammar.GrammarLexer;
//import grammar.GrammarParser;
//import org.antlr.v4.runtime.*;
//import org.antlr.v4.runtime.tree.*;
//
//public class Main {
//	public static void main(String[] args) throws Exception {
//		// create a CharStream that reads from standard input
//		CharStream input = CharStreams.fromStream(System.in);
//
//		// create a lexer that feeds off of input CharStream
//		GrammarLexer lexer = new GrammarLexer(input);
//
//		// create a buffer of tokens pulled from the lexer
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//		// create a parser that feeds off the tokens buffer
//		GrammarParser parser = new GrammarParser(tokens);
//
//		// start parsing at the prog rule
//		ParseTree tree = parser.prog();
//		// System.out.println(tree.toStringTree(parser));
//
//		// create a visitor to traverse the parse tree
//		PlannerVisitor visitor = new PlannerVisitor();
////		System.out.println(visitor.visit(tree));
//	}
//}