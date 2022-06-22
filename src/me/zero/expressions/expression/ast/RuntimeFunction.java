package me.zero.expressions.expression.ast;

import me.zero.expressions.expression.Evaluator;
import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.SimplificationContext;

import java.util.List;

/**
 * @author Brady
 * @since 6/21/2022
 */
public record RuntimeFunction(String name, List<Expression> args) implements Expression {

    @Override
    public double eval(Evaluator ev) {
        var func = ev.getFunction(this.name);
        ev.pushLocals(func.bind(this.args, ev));
        var ret = func.eval(ev);
        ev.popLocals();
        return ret;
    }

    @Override
    public Expression simplify(SimplificationContext context) {
        return this;
    }
}
