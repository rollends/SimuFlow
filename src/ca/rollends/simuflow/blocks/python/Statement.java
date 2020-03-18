package ca.rollends.simuflow.blocks.python;

public class Statement extends AbstractSyntaxTree {

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitStatement(this);
    }

}
