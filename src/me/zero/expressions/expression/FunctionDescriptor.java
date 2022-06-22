package me.zero.expressions.expression;

import me.zero.expressions.expression.evaluate.Evaluable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brady
 * @since 6/22/2022
 */
public interface FunctionDescriptor extends Evaluable {

    List<String> getArguments();

    default Map<String, Double> bind(List<Expression> args, Evaluator ev) {
        if (this.getArguments().size() != args.size()) {
            throw new IllegalArgumentException("Specified arguments do not match expected count!");
        }

        // Evaluate the argument expressions in the current context, and provide a map of name->value
        var locals = new HashMap<String, Double>();
        for (int i = 0; i < args.size(); i++) {
            var name = this.getArguments().get(i);
            var expr = args.get(i);
            locals.put(name, expr.eval(ev));
        }
        return locals;
    }

    static FunctionDescriptor of(List<String> args, Expression body) {
        return new FunctionDescriptor() {
            @Override
            public List<String> getArguments() {
                return args;
            }

            @Override
            public double eval(Evaluator ev) {
                return body.eval(ev);
            }
        };
    }
}
