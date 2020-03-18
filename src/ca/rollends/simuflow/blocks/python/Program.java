package ca.rollends.simuflow.blocks.python;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Program {
    private final Sequence code;

    private Program(Sequence childs) {
        code = childs;
    }

    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitProgram(this);
    }

    public static Program newProgram() {
        return new Program(Sequence.empty());
    }

    public static Program newProgram(Program p, AbstractSyntaxTree tree) {
        return new Program(Sequence.concat(p.code, Sequence.from(tree)));
    }

    public Sequence getImplementation() { return code; }
}
