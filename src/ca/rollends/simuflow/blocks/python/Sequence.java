package ca.rollends.simuflow.blocks.python;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sequence extends AbstractSyntaxTree {
    protected final List<AbstractSyntaxTree> operations;

    public Sequence(List<AbstractSyntaxTree> ops) {
        operations = ops;
    }

    public static Sequence concat(Sequence a, Sequence b) {
        return new Sequence(Stream.concat(a.getChildren().stream(), b.getChildren().stream()).collect(Collectors.toUnmodifiableList()));
    }

    public static Sequence concat(Sequence a, Sequence b, Sequence ...others) {
        Stream<AbstractSyntaxTree> folded = Stream.concat(a.getChildren().stream(), b.getChildren().stream());
        for(Sequence other : others) {
            folded = Stream.concat(folded, other.getChildren().stream());
        }
        return new Sequence(folded.collect(Collectors.toUnmodifiableList()));
    }

    public static Sequence empty() {
        return new Sequence(List.of());
    }

    public static Sequence from(AbstractSyntaxTree tree) {
        return new Sequence(List.of(tree));
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitSequence(this);
    }

    public List<AbstractSyntaxTree> getChildren() { return operations; }
}
