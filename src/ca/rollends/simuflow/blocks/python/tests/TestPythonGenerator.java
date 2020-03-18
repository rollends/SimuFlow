package ca.rollends.simuflow.blocks.python.tests;

import ca.rollends.simuflow.blocks.python.*;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.plaf.nimbus.State;
import java.util.List;

public class TestPythonGenerator {

    @Test
    public void testBasicExpression() {
        PythonGenerator gen = new PythonGenerator();
        Expression ex = new Expression("print('Hello World')" );

        ex.accept(gen);

        String result = gen.toString();

        Assert.assertEquals("print('Hello World')", result);
    }

    @Test
    public void testBasicStatement() {
        PythonGenerator gen = new PythonGenerator();

        Symbol rs = new Symbol("temp");
        Expression ex = new Expression("a*b + c*d**2" );
        Statement stmt = new Statement(rs, ex);

        stmt.accept(gen);

        String result = gen.toString();

        Assert.assertEquals("temp=a*b + c*d**2\n", result);
    }

    @Test
    public void testSimpleScope() {
        PythonGenerator gen = new PythonGenerator();

        Symbol t1 = new Symbol("t1");
        Symbol t2 = new Symbol("t2");
        Statement stmt = new Statement(t1, new Expression("2" ));
        Statement stmt2 = new Statement(t2, new Expression("10*t1"));
        Scope scope = new Scope(List.of(stmt, stmt2));

        scope.accept(gen);

        String result = gen.toString();

        Assert.assertEquals("    t1=2\n    t2=10*t1\n", result);
    }

    @Test
    public void testSimpleFunction() {
        PythonGenerator gen = new PythonGenerator();

        Symbol t1 = new Symbol("t1");
        Symbol t2 = new Symbol("t2");
        Statement stmt = new Statement(t1, new Expression("2" ));
        Statement stmt2 = new Statement(t2, new Expression("10*t1"));
        Scope scope = new Scope(List.of(stmt, stmt2));

        Symbol name = new Symbol("fx");
        Function fx = new Function(name, List.of(), List.of(), scope);

        fx.accept(gen);

        String result = gen.toString();

        Assert.assertEquals("def fx():\n    t1=2\n    t2=10*t1\n", result);
    }

    @Test
    public void testNestedFunction() {
        PythonGenerator gen = new PythonGenerator();

        Symbol t1 = new Symbol("t1");
        Symbol t2 = new Symbol("t2");
        Statement stmt = new Statement(t1, new Expression("2" ));
        Statement stmt2 = new Statement(t2, new Expression("10*t1"));
        Scope scope = new Scope(List.of(stmt, stmt2));

        Symbol name = new Symbol("fx");
        Function fx = new Function(name, List.of(), List.of(), scope);

        Symbol t3 = new Symbol("t3");
        Statement stmt3 = new Statement(t3, new Expression("25" ));
        Scope scope2 = new Scope(List.of(fx, stmt3));

        Symbol name2 = new Symbol("fx2");
        Function fx2 = new Function(name2, List.of(), List.of(), scope2);

        fx2.accept(gen);

        String result = gen.toString();

        Assert.assertEquals("def fx2():\n    def fx():\n        t1=2\n        t2=10*t1\n    t3=25\n", result);
    }
}
