package ca.rollends.simuflow.blocks.python;

public interface IAbstractSyntaxTreeVisitor {
    void visitAssignStatement(AssignStatement st);
    void visitReturnStatement(ReturnStatement st);
    void visitImportStatement(ImportStatement st);
    void visitPlainStatement(PlainStatement st);
    void visitFunctionCallStatement(FunctionCallStatement exp);
    void visitStatement(Statement st);
    void visitPlainExpression(PlainExpression exp);
    void visitBinaryOperatorExpression(BinaryOperatorExpression exp);
    void visitUnaryOperatorExpression(UnaryOperatorExpression exp);
    void visitFunctionCallExpression(FunctionCallExpression exp);
    void visitTupleExpression(TupleExpression exp);
    void visitScope(Scope scp);
    void visitFunction(Function fx);
    void visitSymbol(Symbol sym);
    void visitProgram(Program prog);
    void visitSequence(Sequence seq);
}
