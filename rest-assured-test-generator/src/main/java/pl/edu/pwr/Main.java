package pl.edu.pwr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import pl.edu.pwr.compiler.GeneratorCommandVisitor;
import pl.edu.pwr.grammar.TestGeneratorLexer;
import pl.edu.pwr.grammar.TestGeneratorParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        CharStream input = readInputFile();
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

        writeOutputFile(render);
    }

    private static CharStream readInputFile() {
        CharStream input = null;
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("input.txt")) {
            if (is != null) {
                input = CharStreams.fromStream(is);
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        }

        return input;
    }

    private static void writeOutputFile(String content) {
        File file = new File("src/main/resources/output.java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            System.out.println("File written successfully to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }
}
