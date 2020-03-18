package ca.rollends.simuflow.blocks.python;

public interface IAbstractSyntaxTreeVisitor {
    void visitAssignStatement(AssignStatement st);
    void visitReturnStatement(ReturnStatement st);
    void visitImportStatement(ImportStatement st);
    void visitStatement(Statement st);
    void visitExpression(Expression exp);
    void visitScope(Scope scp);
    void visitFunction(Function fx);
    void visitSymbol(Symbol sym);
    void visitProgram(Program prog);
    void visitSequence(Sequence seq);
}
