package me.zero.expressions.expression.ast;

import me.zero.expressions.expression.Evaluator;
import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.SimplificationContext;

import java.util.Collection;
import java.util.List;

/**
 * @author Brady
 * @since 6/21/2022
 */
public record MultiplyDivide(Collection<Expression> multiply, Collection<Expression> divide) implements Expression {

    public MultiplyDivide(Expression multiply, Expression divide) {
        this(List.of(multiply), List.of(divide));
    }

    @Override
    public double eval(Evaluator evaluator) {
        return this.multiply.stream().mapToDouble(e -> e.eval(evaluator)).reduce(1.0d, (a, b) -> a * b)
            / this.divide.stream().mapToDouble(e -> e.eval(evaluator)).reduce(1.0d, (a, b) -> a * b);
    }

    @Override
    public Expression simplify(SimplificationContext context) {
        return this;
    }
}
