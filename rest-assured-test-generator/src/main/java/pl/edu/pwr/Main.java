package pl.edu.pwr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import pl.edu.pwr.grammar.TestGeneratorLexer;
import pl.edu.pwr.grammar.TestGeneratorParser;

import pl.edu.pwr.compiler.GeneratorCommandVisitor;

public class Main {
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(System.in);

        // create a lexer that feeds off of input CharStream
        TestGeneratorLexer lexer = new TestGeneratorLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        TestGeneratorParser parser = new TestGeneratorParser(tokens);

        // start parsing at the program rule
        ParseTree tree = parser.program();
        // System.out.println(tree.toStringTree(parser));

        // create a visitor to traverse the parse tree
        GeneratorCommandVisitor visitor = new GeneratorCommandVisitor();
//        System.out.println(visitor.visit(tree));
    }
}