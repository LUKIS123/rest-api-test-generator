package pl.edu.pwr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import pl.edu.pwr.compiler.GeneratorCommandVisitor;
import pl.edu.pwr.grammar.TestGeneratorLexer;
import pl.edu.pwr.grammar.TestGeneratorParser;
import pl.edu.pwr.utility.CommandLineParser;
import pl.edu.pwr.utility.FileUtils;
import pl.edu.pwr.utility.ToolArguments;

public class Main {
    public static void main(String[] args) {
        ToolArguments toolArguments = CommandLineParser.parseCommandLine(args);
        CharStream input = FileUtils.readInputFile(toolArguments.inputFilePath());
        if (input == null) {
            System.err.println("Input stream is null");
            return;
        }
        // create a lexer that feeds off of input CharStream
        TestGeneratorLexer lexer = new TestGeneratorLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        TestGeneratorParser parser = new TestGeneratorParser(tokens);

        // start parsing at the program rule
        ParseTree tree = parser.program();
        // System.out.println(tree.toStringTree(parser));

        STGroup group = new STGroupFile("src/main/java/pl/edu/pwr/templates/GeneratorCommands.stg");
        // create a visitor to traverse the parse tree
        GeneratorCommandVisitor visitor = new GeneratorCommandVisitor(group);
        ST res = visitor.visit(tree);
        if (res == null) {
            System.err.println("Resulting template is null");
            return;
        }

        String render = res.render();

        System.out.println("===================================\n");
        System.out.println(render);
        System.out.println("===================================");

        String outputFile = toolArguments.outputFilePath() != null
                ? toolArguments.outputFilePath()
                : "./out.java";
        FileUtils.saveToFile(outputFile, render);
    }
}
