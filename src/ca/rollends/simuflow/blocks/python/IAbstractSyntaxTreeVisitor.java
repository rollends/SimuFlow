package ca.rollends.simuflow.blocks.python;

public interface IAbstractSyntaxTreeVisitor {
    void visitStatement(Statement st);
    void visitExpression(Expression exp);
    void visitScope(Scope scp);
    void visitFunction(Function fx);
    void visitSymbol(Symbol sym);
    void visitProgram(Program prog);
    void visitSequence(Sequence seq);
}
