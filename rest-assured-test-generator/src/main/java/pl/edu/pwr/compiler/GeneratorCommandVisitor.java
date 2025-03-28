package pl.edu.pwr.compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import pl.edu.pwr.grammar.TestGeneratorBaseVisitor;

public class GeneratorCommandVisitor extends TestGeneratorBaseVisitor<ST> {
    private final STGroup stGroup;

    public GeneratorCommandVisitor(STGroup stGroup) {
        super();
        this.stGroup = stGroup;
    }

    @Override
    protected ST defaultResult() {
        return stGroup.getInstanceOf("deflt");
    }

    @Override
    protected ST aggregateResult(ST aggregate, ST nextResult) {
        if (nextResult != null) {
            aggregate.add("elem", nextResult);
        }

        return aggregate;
    }

    @Override
    public ST visitTerminal(TerminalNode node) {
        return new ST("Terminal node:<n>").add("n", node.getText());
    }
}