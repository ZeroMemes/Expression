package me.zero.expressions.provider;

import me.zero.expressions.expression.Expression;

import java.util.Collection;

/**
 * @author Brady
 * @since 6/21/2022
 */
@FunctionalInterface
public interface ExpressionProvider {

    /**
     * @param args The arguments to the underlying expression
     * @return The produced expression
     */
    Expression apply(Expression... args);

    default Expression apply(Collection<Expression> args) {
        return this.apply(args.toArray(Expression[]::new));
    }
}
