package me.zero.expressions.expression.ast;

import me.zero.expressions.expression.Evaluator;
import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.SimplificationContext;

/**
 * @author Brady
 * @since 6/21/2022
 */
public record Exponent(Expression base, Expression power) implements Expression {

    @Override
    public double eval(Evaluator ev) {
        return Math.pow(this.base.eval(ev), this.power.eval(ev));
    }

    @Override
    public Expression simplify(SimplificationContext context) {
        return this;
    }
}
