package ca.rollends.simuflow.blocks.python;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Program {
    private final List<AbstractSyntaxTree> children;

    private Program(List<AbstractSyntaxTree> childs) {
        children = childs;
    }

    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitProgram(this);
    }

    public static Program newProgram() {
        return new Program(List.of());
    }

    public static Program newProgram(Program p, AbstractSyntaxTree tree) {
        return new Program(Stream.concat(p.children.stream(), Stream.of(tree)).collect(Collectors.toUnmodifiableList()));
    }

    public List<AbstractSyntaxTree> getChildren() { return children; }
}
