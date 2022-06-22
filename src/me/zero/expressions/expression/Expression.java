package me.zero.expressions.expression;

import me.zero.expressions.expression.evaluate.Evaluable;

/**
 * @author Brady
 * @since 6/20/2022
 */
public interface Expression extends Evaluable {

    Expression simplify(SimplificationContext context);
}
