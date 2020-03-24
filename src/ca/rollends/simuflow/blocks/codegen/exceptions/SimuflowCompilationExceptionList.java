package ca.rollends.simuflow.blocks.codegen.exceptions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimuflowCompilationExceptionList extends SimuflowCompilationException {

    private final List<SimuflowCompilationException> exceptions;

    private SimuflowCompilationExceptionList(List<SimuflowCompilationException> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isEmpty() {
        return exceptions.isEmpty();
    }

    public SimuflowCompilationExceptionList concat(SimuflowCompilationExceptionList others) {
        return new SimuflowCompilationExceptionList(Stream.concat(exceptions.stream(), others.exceptions.stream()).collect(Collectors.toUnmodifiableList()));
    }

    public static SimuflowCompilationExceptionList empty() {
        return new SimuflowCompilationExceptionList(List.of());
    }

    public static SimuflowCompilationExceptionList from(SimuflowCompilationException ex) {
        return new SimuflowCompilationExceptionList(List.of(ex));
    }

    public static SimuflowCompilationExceptionList from(SimuflowCompilationException ex, SimuflowCompilationException ex2, SimuflowCompilationException ... others) {
        SimuflowCompilationExceptionList list = SimuflowCompilationExceptionList.from(ex);
        list = list.concat(SimuflowCompilationExceptionList.from(ex2));
        for(SimuflowCompilationException other : others) {
            list = list.concat(SimuflowCompilationExceptionList.from(other));
        }
        return list;
    }
}
