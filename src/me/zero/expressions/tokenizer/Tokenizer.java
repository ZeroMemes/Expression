package me.zero.expressions.tokenizer;

import me.zero.expressions.provider.Operator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.zero.expressions.tokenizer.Token.Type.*;

/**
 * @author Brady
 * @since 6/20/2022
 */
public final class Tokenizer {

    private Tokenizer() {}

    public static List<Token> convertToPostfix(List<Token> infix) {
        var output = new ArrayList<Token>();
        var stack = new ArrayDeque<Token>();
        var arity = new ArrayDeque<Integer>();

        // Extremely helpful resources:
        // https://en.wikipedia.org/wiki/Shunting_yard_algorithm
        // https://wcipeg.com/wiki/Shunting_yard_algorithm#Extensions

        Token prev = null;
        for (var token : infix) {
            switch (token.type()) {
                case LITERAL, VARIABLE -> output.add(token);
                case LEFT_PARENTHESIS -> stack.push(token);
                case FUNCTION -> {
                    stack.push(token);
                    // Default argument count to 1, 0 args has a separate check
                    arity.push(1);
                }
                case OPERATOR -> {
                    // Only pop the stack if it's not a unary operator
                    if (!token.operator().isUnary()) {
                        while (!stack.isEmpty()
                            && stack.peek().type() != LEFT_PARENTHESIS
                            && (token.precedence() < stack.peek().precedence()
                            || token.precedence() == stack.peek().precedence() && token.associativity() == Operator.Associativity.LEFT)) {
                            output.add(stack.pop());
                        }
                    }
                    stack.push(token);
                }
                case RIGHT_PARENTHESIS, ARGUMENT_SEPARATOR -> {
                    // "No args" represented by subsequent right after left: ()
                    var noargs = prev.type() == LEFT_PARENTHESIS;

                    // Handle closing statement
                    while (stack.peek().type() != LEFT_PARENTHESIS) {
                        output.add(stack.pop());
                    }
                    // Only pop the right left parenthesis if we're coming from the right parenthesis
                    if (token.type() == RIGHT_PARENTHESIS) {
                        stack.pop();
                        if (stack.peek().type() == FUNCTION) {
                            output.add(stack.pop().hintArgs(noargs ? 0 : arity.pop()));
                        }
                    } else {
                        // Increment argument count
                        arity.push(arity.pop() + 1);
                    }
                }
            }
            prev = token;
        }

        // Flush the stack
        while (!stack.isEmpty()) {
            output.add(stack.pop());
        }

        return output;
    }

    public static List<Token> tokenize(final String infix) {
        // Remove all whitespace
        var str = infix.replaceAll("\s", "");

        var t = new State();

        for (var ch : str.toCharArray()) {
            Token.Type.getType(ch).ifPresentOrElse(
                type -> {
                    // Various cases to be handled before adding type token
                    switch (type) {
                        case LITERAL -> t.numBuffer.append(ch);
                        case VARIABLE -> {
                            t.pushNum();
                            t.letBuffer.append(ch);
                        }
                        case OPERATOR -> {
                            if (!t.pushNum()) {
                                t.pushVars();
                            }
                        }
                        case LEFT_PARENTHESIS -> {
                            if (!t.pushFunction()) {
                                t.pushNum();
                            }
                        }
                        case RIGHT_PARENTHESIS, ARGUMENT_SEPARATOR, EQUALS -> t.flush();
                    }

                    // Add unbuffered token immediately
                    if (!type.isBuffered()) {
                        t.push(type, ch);
                    }
                },
                () -> {
                    throw new IllegalArgumentException("Invalid token found: %s".formatted(ch));
                }
            );
        }

        // Flush buffers
        t.flush();

        // Apply various fixes
        convertToUnary(t);
        combineBuiltinVars(t);
        implicitMultiplication(t);

        // Validate, then return if successful (An exception will be thrown)
        return validate(t.tokens);
    }

    private static void convertToUnary(State t) {
        var nextUnary = true;

        for (int i = 0; i < t.tokens.size(); i++) {
            var token = t.tokens.get(i);

            Token unary;
            if (nextUnary && (unary = token.toUnary()) != null) {
                token = unary;
                t.tokens.set(i, token);
            }

            nextUnary = switch (token.type()) {
                case OPERATOR, LEFT_PARENTHESIS, ARGUMENT_SEPARATOR -> true;
                default -> false;
            };
        }
    }

    private static void combineBuiltinVars(State t) {
        var name = new StringBuilder();
        for (var token : t.tokens) {
            if (token.type() == VARIABLE) {
                name.append(token.value());
                // TODO: Merge sequential variables into single for predefined (p, i) -> pi
            } else {
                name.setLength(0);
            }
        }
    }

    private static void implicitMultiplication(State t) {
        var patterns = new Token.Type[][] {
            {VARIABLE,          VARIABLE},          // xx        => x*x
            {VARIABLE,          LEFT_PARENTHESIS},  // x(5)      => x*(5)
            {LITERAL,           VARIABLE},          // 5x        => 5*x
            {LITERAL,           FUNCTION},          // 5sin(5)   => 5*sin(5)
            {LITERAL,           LEFT_PARENTHESIS},  // 5(5)      => 5*(5)
            {RIGHT_PARENTHESIS, VARIABLE},          // (5)x      => (5)*x
            {RIGHT_PARENTHESIS, LITERAL},           // (5)5      => (5)*5
            {RIGHT_PARENTHESIS, LEFT_PARENTHESIS},  // (5)(5)    => (5)*(5)
            {RIGHT_PARENTHESIS, FUNCTION},          // (5)sin(5) => (5)*sin(5)
        };
        outer:
        while (true) {
            for (var pattern : patterns) {
                int index = findPattern(t.tokens, pattern);
                if (index >= 0) {
                    t.insertOperator(index + 1, Operator.MULTIPLY);
                    continue outer;
                }
            }
            break;
        }
    }

    private static List<Token> validate(List<Token> tokens) {
        if (tokens.get(0).type() == RIGHT_PARENTHESIS || tokens.get(0).type() == ARGUMENT_SEPARATOR) {
            throw new IllegalStateException("Expression cannot start with RIGHT_PARENTHESIS or ARGUMENT_SEPARATOR");
        }

        // Confirm matching parenthesis
        int right = 0;
        int left = 0;
        for (var token : tokens) {
            switch (token.type()) {
                case RIGHT_PARENTHESIS -> right++;
                case LEFT_PARENTHESIS -> left++;
            }
        }
        if (right != left) {
            throw new IllegalStateException("Mismatched parenthesis");
        }

        // Check for invalid token type patterns
        var invalid = new Token.Type[][] {
            {VARIABLE, LITERAL} // x5
        };
        for (var pattern : invalid) {
            int index = findPattern(tokens, pattern);
            if (index >= 0) {
                throw new IllegalStateException(
                    "Encountered invalid pattern in tokenized expression! %s".formatted(Arrays.toString(pattern))
                );
            }
        }

        // Verify LITERAL token value
        for (var token : tokens) {
            if (token.type() == LITERAL) {
                try {
                    token.doubleValue();
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Token doesn't represent a valid number", e);
                }
            }
        }
        return tokens;
    }

    private static int findPattern(List<Token> tokens, Token.Type... types) {
        outer:
        for (var i = 0; i <= tokens.size() - types.length; i++) {
            var offset = 0;
            var matched = 0;
            while (matched < types.length) {
                var index = i + offset++;
                if (index >= tokens.size()) {
                    continue outer;
                }
                var token = tokens.get(index);
                if (types[matched] != token.type()) {
                    continue outer;
                }
                matched++;
            }
            return i;
        }
        return -1;
    }

    private record State(List<Token> tokens, StringBuffer numBuffer, StringBuffer letBuffer) {

        private State() {
            this(new ArrayList<>(), new StringBuffer(), new StringBuffer());
        }

        private void insert(int index, Token.Type type, String value) {
            this.tokens.add(index, new Token(type, value));
        }

        private void insertOperator(int index, Operator op) {
            insert(index, OPERATOR, op.name());
        }

        private void push(Token.Type type, String value) {
            this.tokens.add(new Token(type, value));
        }

        private void push(Token.Type type, char value) {
            if (type == OPERATOR) {
                this.push(type, String.valueOf(Operator.getBySymbol(value).orElseThrow()));
            } else {
                this.push(type, String.valueOf(value));
            }
        }

        private void flush() {
            this.pushNum();
            this.pushVars();
        }

        private boolean pushNum() {
            if (this.numBuffer.isEmpty()) {
                return false;
            }

            this.push(Token.Type.LITERAL, this.numBuffer.toString());
            this.numBuffer.setLength(0);
            return true;
        }

        private boolean pushVars() {
            if (this.letBuffer.isEmpty()) {
                return false;
            }

            for (char var : this.letBuffer.toString().toCharArray()) {
                this.push(Token.Type.VARIABLE, var);
            }
            this.letBuffer.setLength(0);
            return true;
        }

        private boolean pushFunction() {
            if (this.letBuffer.isEmpty()) {
                return false;
            }

            this.push(Token.Type.FUNCTION, this.letBuffer.toString());
            this.letBuffer.setLength(0);
            return true;
        }
    }
}
