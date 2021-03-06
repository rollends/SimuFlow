package ca.rollends.simuflow.blocks.python;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PythonGenerator implements IAbstractSyntaxTreeVisitor {
    private final Deque<Object> codeStack = new LinkedList<>();

    @Override
    public String toString() {
        Object result = codeStack.peek();

        if(result instanceof String) {
            return (String) result;
        }

        List<String> listing = (List<String>) result;

        StringBuilder builder = new StringBuilder();
        listing.forEach( (l) -> builder.append(l) );

        return builder.toString();
    }

    @Override
    public void visitProgram(Program prog) {
        // Generate code for entire program.
        prog.getImplementation().accept(this);
    }

    @Override
    public void visitSequence(Sequence scp) {
        List<String> codeLines = new LinkedList<>();

        for(AbstractSyntaxTree child : scp.getChildren()) {
            child.accept(this);

            // Inspect top of stack. Two cases.
            Object returnValue = codeStack.poll();
            if(returnValue instanceof String) {

                // Child essentially just a statement.
                codeLines.add((String) returnValue);

            } else if(returnValue instanceof List) {

                // Child returned a list of code lines. Tab them in
                codeLines.addAll((List<String>) returnValue);

            }
        }

        codeStack.push(codeLines);
    }

    @Override
    public void visitStatement(Statement st) {
        // Default. Do nothing.
    }

    @Override
    public void visitPlainStatement(PlainStatement st) {
        codeStack.push(st.toString() + "\n");
    }

    @Override
    public void visitReturnStatement(ReturnStatement st) {
        Expression rhs = st.getRHS();

        // Traverse expression.
        rhs.accept(this);

        // Top of stack has (1) symbol and (2) rhs code
        StringBuilder strStmt = new StringBuilder();
        strStmt.append("return ");
        strStmt.append((String)codeStack.poll());
        strStmt.append("\n");

        codeStack.push(strStmt.toString());
    }

    @Override
    public void visitImportStatement(ImportStatement st) {
        Expression rhs = st.getRHS();

        // Traverse expression.
        rhs.accept(this);

        // Top of stack has (1) symbol and (2) rhs code
        StringBuilder strStmt = new StringBuilder();
        strStmt.append("import ");
        strStmt.append((String)codeStack.poll());
        strStmt.append("\n");

        codeStack.push(strStmt.toString());
    }

    @Override
    public void visitAssignStatement(AssignStatement st) {
        Symbol lhs = st.getLHS();
        Expression rhs = st.getRHS();

        // Traverse expression.
        rhs.accept(this);

        // Traverse symbol.
        lhs.accept(this);

        // Top of stack has (1) symbol and (2) rhs code
        StringBuilder strStmt = new StringBuilder();
        strStmt.append((String)codeStack.poll());
        strStmt.append("=");
        strStmt.append((String)codeStack.poll());
        strStmt.append("\n");

        codeStack.push(strStmt.toString());
    }

    @Override
    public void visitScope(Scope scp) {
        String tabInFormat = "    %s";

        Sequence implementation = scp.getImplementation();
        implementation.accept(this);

        // Will always return a List of code strings
        List<String> codeListing = (List<String>) (codeStack.poll());

        // Child returned a list of code lines. Tab them in
        Stream<String> stream = codeListing.stream().map( (s) -> String.format(tabInFormat, s));

        // Push lines of code
        codeStack.push(stream.collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void visitFunction(Function fx) {
        List<String> codeLines = new LinkedList<>();

        String defFunctionFormat = "def %s(%s):\n";

        // Make Parameter List
        List<String> parameters = new LinkedList<>();
        for(Symbol parameter : fx.getParameters()) {
            parameter.accept(this);
            parameters.add((String)codeStack.poll());
        }
        if(parameters.size() > 0) {
            codeStack.push(parameters.stream().reduce((a, b) -> a + "," + b).get());
        } else {
            codeStack.push("");
        }

        // Push Function Name
        fx.getName().accept(this);

        String name = (String) codeStack.poll();
        String parameterList = (String) codeStack.poll();

        codeStack.push(String.format(defFunctionFormat, name, parameterList));

        // Visit the scope
        fx.getImplementation().accept(this);

        // Pop the resulting scope implementation and function declaration.
        List<String> impl = (List<String>) (codeStack.poll());
        String declaration = (String) codeStack.poll();

        // Push code listing for function
        codeStack.push(Stream.concat(Stream.of(declaration), impl.stream()).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void visitPlainExpression(PlainExpression exp) {
        codeStack.push(exp.getCode());
    }

    @Override
    public void visitBinaryOperatorExpression(BinaryOperatorExpression exp) {
        StringBuilder str = new StringBuilder();

        exp.getRightOperand().accept(this);
        codeStack.push(exp.getOperator());
        exp.getLeftOperand().accept(this);

        str.append('(');
        str.append(codeStack.poll());
        str.append(codeStack.poll());
        str.append(codeStack.poll());
        str.append(')');

        codeStack.push(str.toString());
    }

    @Override
    public void visitUnaryOperatorExpression(UnaryOperatorExpression exp) {
        StringBuilder str = new StringBuilder();

        exp.getOperand().accept(this);
        codeStack.push(exp.getOperator());

        str.append('(');
        str.append(codeStack.poll());
        str.append(codeStack.poll());
        str.append(')');

        codeStack.push(str.toString());
    }

    @Override
    public void visitFunctionCallExpression(FunctionCallExpression exp) {
        StringBuilder str = new StringBuilder();

        str.append(exp.getFunctionName());
        str.append('(');

        List<Expression> children = exp.getArguments();

        for(int i = 0; i < children.size(); i++) {
            children.get(i).accept(this);
            if (i > 0) {
                str.append(',');
            }
            str.append(codeStack.poll());
        }

        str.append(')');

        codeStack.push(str.toString());
    }

    @Override
    public void visitFunctionCallStatement(FunctionCallStatement exp) {
        StringBuilder str = new StringBuilder();

        str.append(exp.getFunctionName());
        str.append('(');

        List<Expression> children = exp.getArguments();

        for(int i = 0; i < children.size(); i++) {
            children.get(i).accept(this);
            if (i > 0) {
                str.append(',');
            }
            str.append(codeStack.poll());
        }

        str.append(')');
        str.append('\n');

        codeStack.push(str.toString());
    }

    @Override
    public void visitTupleExpression(TupleExpression exp) {
        StringBuilder str = new StringBuilder();
        str.append('(');

        List<Expression> children = exp.getElements();

        for(int i = 0; i < children.size(); i++) {
            children.get(i).accept(this);
            if (i > 0) {
                str.append(',');
            }
            str.append(codeStack.poll());
        }

        str.append(')');

        codeStack.push(str.toString());
    }

    @Override
    public void visitSymbol(Symbol sym) {
        codeStack.push(sym.name);
    }
}
