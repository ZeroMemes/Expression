package me.zero.expressions.expression;

import java.util.*;

/**
 * @author Brady
 * @since 6/20/2022
 */
public final class Evaluator {

    // TODO: Builtin Functions/Default globals
    private final Map<String, Double> globals;
    private final Deque<Map<String, Double>> locals;
    private final Map<String, FunctionDescriptor> functions;

    public Evaluator() {
        this.globals = new HashMap<>();
        this.locals = new ArrayDeque<>();
        this.functions = new HashMap<>();
    }

    public double variable(String identifier) {
        if (!this.locals.isEmpty()) {
            var val = this.locals.peek().get(identifier);
            if (val != null) {
                // TODO: Warn about shadowing a global variable
                return val;
            }
        }

        var val = this.globals.get(identifier);
        if (val == null) {
            throw new IllegalArgumentException("Invalid variable name provided! Unable to resolve.");
        }
        return val;
    }

    public void addFunction(String name, List<String> args, Expression body) {
        this.functions.put(name, FunctionDescriptor.of(args, body));
    }

    public FunctionDescriptor getFunction(String name) {
        var desc = this.functions.get(name);
        if (desc == null) {
            throw new IllegalArgumentException("Invalid function name provided! Unable to resolve.");
        }
        return desc;
    }

    public void pushLocals(Map<String, Double> locals) {
        this.locals.push(Map.copyOf(locals));
    }

    public void popLocals() {
        this.locals.pop();
    }

    public Map<String, Double> getGlobals() {
        return this.globals;
    }
}
