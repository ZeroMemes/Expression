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
public record AddSubtract(Collection<Expression> add, Collection<Expression> subtract) implements Expression {

    public AddSubtract(Expression add, Expression subtract) {
        this(List.of(add), List.of(subtract));
    }

    @Override
    public double eval(Evaluator ev) {
        return this.add.stream().mapToDouble(exp -> exp.eval(ev)).sum()
            - this.subtract.stream().mapToDouble(exp -> exp.eval(ev)).sum();
    }

    @Override
    public Expression simplify(SimplificationContext context) {
        return this;
    }
}
