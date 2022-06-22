package me.zero.expressions.expression.ast;

import me.zero.expressions.expression.Evaluator;
import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.SimplificationContext;

/**
 * @author Brady
 * @since 6/21/2022
 */
public record Variable(String variable) implements Expression {

    @Override
    public double eval(Evaluator ev) {
        return ev.variable(this.variable);
    }

    @Override
    public Expression simplify(SimplificationContext context) {
        return this;
    }
}
