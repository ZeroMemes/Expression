package me.zero.expressions.expression.evaluate;

import me.zero.expressions.Utils;
import me.zero.expressions.expression.Evaluator;
import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.ast.Literal;
import me.zero.expressions.expression.ast.RuntimeFunction;
import me.zero.expressions.expression.ast.Variable;
import me.zero.expressions.provider.ExpressionProvider;
import me.zero.expressions.tokenizer.Token;
import me.zero.expressions.tokenizer.Tokenizer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brady
 * @since 6/21/2022
 */
public class ExpressionEngine {

    private final Evaluator evaluator;

    public ExpressionEngine() {
        this.evaluator = new Evaluator();
    }

    public OptionalDouble eval(String expression) {
        // Tokenize the entire infix expression
        var tokenized = Tokenizer.tokenize(expression);

        // Split the tokens by EQUALS and convert each subsection to postfix
        var split = Utils.splitList(tokenized, t -> t.type() == Token.Type.EQUALS).stream()
            .map(Tokenizer::convertToPostfix).toList();

        // Handle each split length case
        switch (split.size()) {
            case 1 -> {
                return OptionalDouble.of(build(split.get(0)).eval(this.evaluator));
            }
            case 2 -> {
                var symbol = split.get(0);
                var value = build(split.get(1));

                var signature = symbol.stream().map(Token::type)
                    .map(Token.Type::name).collect(Collectors.joining());

                if (signature.equals("VARIABLE")) {
                    // TODO: Address recursive reference for redefinition
                    var val = value.eval(this.evaluator);
                    this.evaluator.getGlobals().put(symbol.get(0).value(), val);
                    return OptionalDouble.of(val);
                }
                if (signature.matches("(VARIABLE)+FUNCTION")) {
                    var name = symbol.get(symbol.size() - 1).functionName();
                    var args = symbol.stream()
                        .filter(t -> t.type() == Token.Type.VARIABLE)
                        .map(Token::value).toList();
                    this.evaluator.addFunction(name, args, value);
                    return OptionalDouble.empty();
                }
                throw new IllegalArgumentException("Invalid definition symbol");
            }
            default -> throw new IllegalArgumentException("Multiple assignments are not supported");
        }
    }

    private static Expression build(List<Token> postfix) {
        var stack = new ArrayDeque<Expression>();

        for (var token : postfix) {
            var args = Collections.asLifoQueue(new ArrayDeque<Expression>());
            for (int i = 0; i < getArgumentCount(token); i++) {
                args.add(stack.pop());
            }
            stack.push(getProvider(token).apply(args));
        }

        return stack.pop();
    }

    private static ExpressionProvider getProvider(Token token) {
        return switch (token.type()) {
            case LITERAL -> args -> new Literal(token.doubleValue());
            case VARIABLE -> args -> new Variable(token.value());
            case OPERATOR -> token.operator();
            // TODO: Differentiate between evaluation runtime and built-in functions
            // runtime -> by name lookup
            // builtin -> construct object for it
            case FUNCTION -> args -> new RuntimeFunction(token.functionName(), Arrays.asList(args));
            default -> throw new IllegalArgumentException("Invalid token type");
        };
    }

    private static int getArgumentCount(Token token) {
        return switch (token.type()) {
            case LITERAL, VARIABLE -> 0;
            case OPERATOR -> token.operator().isUnary() ? 1 : 2;
            case FUNCTION -> token.functionArgs();
            default -> throw new UnsupportedOperationException("Invalid token type");
        };
    }
}
