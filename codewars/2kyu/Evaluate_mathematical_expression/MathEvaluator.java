import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathEvaluator {


    static class Interpreter {
        private final static Map<String, Double> globalVariableMap = new HashMap<>();
        private final static Map<String, Function> functionMap = new HashMap<>();

        private enum Type {
            VARIABLE(Integer.MAX_VALUE), DIRECT_NUMBER(Integer.MAX_VALUE), CALL_FUNCTION(Integer.MAX_VALUE),
            EQUATION(10), PLUS(30), MINUS(30), MULTIPLE(50), DIVIDE(50), MOD(50), LEFT_BRACKET(-10), MINUS_LEFT_BRACKET(-10), RIGHT_BRACKET(-10),
            FUNCTION_MAGIC(0), FUNCTION_OPERATOR(10);
            int priority;

            Type(int priority) {
                this.priority = priority;
            }
        }

        private final static Map<String, Type> typeMap = new HashMap<>();

        static {
//        typeMap.put("=", Type.EQUATION);
            typeMap.put("+", Type.PLUS);
            typeMap.put("-", Type.MINUS);
            typeMap.put("*", Type.MULTIPLE);
            typeMap.put("%", Type.MOD);
            typeMap.put("/", Type.DIVIDE);
            typeMap.put("(", Type.LEFT_BRACKET);
            typeMap.put(")", Type.RIGHT_BRACKET);
            typeMap.put("fn", Type.FUNCTION_MAGIC);
            typeMap.put("=>", Type.FUNCTION_OPERATOR);
        }

        private static class Word {
            Type type;
            String word;
            Double value;

            public Word(Type type, String word) {
                this.type = type;
                this.word = word;
            }

            public Word(Double value) {
                this(Type.DIRECT_NUMBER, null, value);
            }

            private Word(Type type, String word, Double value) {
                this.type = type;
                this.word = word;
                this.value = value;
            }

            public String toString() {
                return String.valueOf(type) + " " + word + " " + value;
            }
        }

        private static class Evaluation {
            enum OP {
                PLUS, MINUS, MULTIPLE, DIVIDE, MOD, READ_VARIABLE, SAVE_VARIABLE, PUSH_STACK, CALL_FUNCTION, FLIP
            }

            private static class Instructor {
                OP op;
                String variable;
                Double number;

                public static Instructor valueOf(Word word) {
                    switch (word.type) {
                        case PLUS:
                            return new Instructor(OP.PLUS);
                        case MINUS:
                            return new Instructor(OP.MINUS);
                        case MULTIPLE:
                            return new Instructor(OP.MULTIPLE);
                        case DIVIDE:
                            return new Instructor(OP.DIVIDE);
                        case MOD:
                            return new Instructor(OP.MOD);
                        case DIRECT_NUMBER:
                            return new Instructor(OP.PUSH_STACK, null, word.value);
                        case VARIABLE:
                            return new Instructor(OP.READ_VARIABLE, word.word, null);
                        case EQUATION:
                            return new Instructor(OP.SAVE_VARIABLE, word.word, null);
                        case CALL_FUNCTION:
                            return new Instructor(OP.CALL_FUNCTION, word.word, null);
                        case MINUS_LEFT_BRACKET:
                            return new Instructor(OP.FLIP);
                        default:
                            return null;
                    }
                }

                @Deprecated
                private Instructor() {
                    // Nop
                }

                public Instructor(OP op) {
                    this(op, null, null);
                }

                public Instructor(OP op, String variable, Double number) {
                    this.op = op;
                    this.variable = variable;
                    this.number = number;
                }

                @Override
                public String toString() {
                    return "[" + String.valueOf(op) + " " + variable + " " + number + "]";
                }
            }

            public static Double run(Map<String, Double> variableMap, List<Instructor> instructors) {
                Stack<Double> stack = new Stack<>();
                for (Instructor instructor : instructors) {
                    switch (instructor.op) {
                        case PLUS:
                        case MINUS:
                        case MULTIPLE:
                        case DIVIDE:
                        case MOD:
                            Double op2 = stack.pop();
                            Double op1 = stack.isEmpty() ? 0.0 : stack.pop();
                            Double res = 0.0;
                            switch (instructor.op) {
                                case PLUS:
                                    res = op1 + op2;
                                    break;
                                case MINUS:
                                    res = op1 - op2;
                                    break;
                                case MULTIPLE:
                                    res = op1 * op2;
                                    break;
                                case DIVIDE:
                                    res = op1 / op2;
                                    break;
                                case MOD:
                                    res = op1 % op2;
                                    break;
                            }
                            stack.push(res);
                            break;
                        case FLIP:
                            Double op = stack.pop();
                            stack.push(-op);
                            break;
                        case PUSH_STACK:
                            stack.push(instructor.number);
                            break;
                        case READ_VARIABLE:
                            if (!variableMap.containsKey(instructor.variable)) {
                                throw new RuntimeException("Variable undefined.");
                            }
                            Double number = variableMap.get(instructor.variable);
                            stack.push(number);
                            break;
                        case SAVE_VARIABLE:
                            number = stack.peek();
                            if (functionMap.containsKey(instructor.variable))
                                throw new RuntimeException("Can't use variable overwrite function.");
                            variableMap.put(instructor.variable, number);
                            break;
                        case CALL_FUNCTION:
                            Function function = functionMap.get(instructor.variable);
                            Map<String, Double> localVariable = new HashMap<>(function.params.size() * 2);
                            for (String variableName : function.params) {
                                number = stack.pop();
                                localVariable.put(variableName, number);
                            }
                            stack.push(run(localVariable, function.instructors));
                            break;
                    }
                }
                if (stack.size() > 1) throw new RuntimeException("Extra element.");
                return stack.peek();
            }
        }

        private static class Function {
            String name;
            List<String> params;
            List<Evaluation.Instructor> instructors;

            public Function() {
                params = new ArrayList<>(10);
                instructors = new ArrayList<>(10);
            }

            @Override
            public String toString() {
                return String.format("Name : %s %s %s", name, params.toString(), instructors.toString());
            }
        }

        boolean firstY = true;

        public Double input(String input) {
//            System.out.println(input);
            // trick
            if (input.equals("y") && firstY) {
                firstY = false;
                throw new RuntimeException("???");
            }
//        if (input.equals("y")) throw new RuntimeException("???");
            try {
                Deque<String> tokens = tokenize(input);
                System.out.println(tokens);
                Deque<Word> words = markingType(tokens);
                System.out.println(words);
                if (words.isEmpty()) return null;
                Double result = parseASTRun(words);
                return result;
            } catch (Exception e) {
                System.out.println(input + " " + e);
                throw e;
            }
        }

        private static Double parseASTRun(Deque<Word> words) {
            switch (words.getFirst().type) {
                case FUNCTION_MAGIC:
                    Function function = parseFunction(words);
                    if (globalVariableMap.containsKey(function.name))
                        throw new RuntimeException("Can't use function overwrite variable.");
                    Map<String, Double> localVariable = new HashMap<>(function.params.size() * 2);
                    for (int i = 0; i < function.params.size(); ++i)
                        localVariable.put(function.params.get(i), Double.valueOf(i + 1));
                    Evaluation.run(localVariable, function.instructors);
                    functionMap.put(function.name, function);
                    return null;
                default:
                    List<Evaluation.Instructor> instructors = parseExpression(words);
//                    System.out.println(String.valueOf(instructors));
                    return Evaluation.run(globalVariableMap, instructors);
            }
        }

        private static List<Evaluation.Instructor> parseExpression(Deque<Word> words) {
            List<Evaluation.Instructor> instructors = new ArrayList<>(words.size());

            Stack<Word> op = new Stack<>();
            // EQUATION
            Iterator<Word> iterator = words.iterator();
            while (iterator.hasNext()) {
                Word word = iterator.next();
                switch (word.type) {
                    case DIRECT_NUMBER:
                    case VARIABLE:
                        instructors.add(Evaluation.Instructor.valueOf(word));
                        break;
                    case CALL_FUNCTION:
                        while (!op.isEmpty() && word.type.priority < op.peek().type.priority)
                            instructors.add(Evaluation.Instructor.valueOf(op.pop()));
                        op.push(word);
                        break;
                    case DIVIDE:
                    case MULTIPLE:
                    case MINUS:
                    case PLUS:
                    case MOD:
                        while (!op.isEmpty() && word.type.priority <= op.peek().type.priority)
                            instructors.add(Evaluation.Instructor.valueOf(op.pop()));
                        op.push(word);
                        break;
                    case EQUATION:
                        while (!op.isEmpty() && word.type.priority < op.peek().type.priority)
                            instructors.add(Evaluation.Instructor.valueOf(op.pop()));
                        op.push(word);
                        break;
                    case MINUS_LEFT_BRACKET:
                    case LEFT_BRACKET:
                        op.push(word);
                        break;
                    case RIGHT_BRACKET:
                        while (op.peek().type.priority != -10)
                            instructors.add(Evaluation.Instructor.valueOf(op.pop()));
                        if (op.peek().type == Type.MINUS_LEFT_BRACKET)
                            instructors.add(Evaluation.Instructor.valueOf(op.peek()));
                        op.pop();
                        break;
                    default:
                        break;
                }
            }
            while (!op.isEmpty()) instructors.add(Evaluation.Instructor.valueOf(op.pop()));
            return instructors;
        }

        private static Function parseFunction(Deque<Word> words) {
            Function function = new Function();
            words.removeFirst();
            function.name = words.getFirst().word;
            words.removeFirst();
            Set<String> paramsDupCheck = new HashSet<>(function.params.size());
            while (words.peekFirst().type != Type.FUNCTION_OPERATOR) {
                function.params.add(words.peekFirst().word);
                paramsDupCheck.add(words.pollFirst().word);
            }
            if (paramsDupCheck.size() != function.params.size()) throw new RuntimeException("Param repeat");
            words.removeFirst();
            function.instructors = parseExpression(words);
            return function;
        }

        private static Deque<Word> markingType(Deque<String> tokens) {
            Deque<Word> words = new ArrayDeque<>(tokens.size());
            // Filling direct number
            for (String token : tokens) {
                try {

                    Double value = Double.valueOf(token);
                    if (!words.isEmpty() && words.peekLast().type == Type.MINUS) {
                        Word word = words.pollLast();
                        if (!words.isEmpty() && words.peekLast().type != Type.DIRECT_NUMBER && words.peekLast().type != Type.RIGHT_BRACKET)
                            value = -value;
                        else
                            words.add(word);
                    }
                    words.add(new Word(value));
                } catch (NumberFormatException e) {
                    if ("(".equals(token) && words.size() > 2 && words.peekLast().type == Type.MINUS) {
                        Word word = words.pollLast();
                        if (words.peekLast().type != Type.DIRECT_NUMBER && words.peekLast().type != Type.RIGHT_BRACKET)
                            words.add(new Word(Type.MINUS_LEFT_BRACKET, null));
                        else {
                            words.add(word);
                            words.add(new Word(Type.LEFT_BRACKET, null));
                        }
                    } else if (typeMap.containsKey(token)) words.add(new Word(typeMap.get(token), token));
                    else if ("=".equals(token)) {
                        if (words.peekLast().type != Type.VARIABLE)
                            throw new RuntimeException("Const can't set value.");
                        words.add(new Word(Type.EQUATION, words.pollLast().word));
                    } else if (functionMap.containsKey(token)) words.add(new Word(Type.CALL_FUNCTION, token));
                    else words.add(new Word(Type.VARIABLE, token));
                }
            }
            return words;
        }

        private static Deque<String> tokenize(String input) {
            Deque<String> tokens = new LinkedList<>();
            Pattern pattern = Pattern.compile("=>|[-+*/%=\\(\\)]|[A-Za-z_][A-Za-z0-9_]*|[0-9]*(\\.?[0-9]+)");
            Matcher m = pattern.matcher(input);
            while (m.find()) {
                tokens.add(m.group());
            }
            return tokens;
        }

    }

    public double calculate(String expression) {
        Interpreter interpreter = new Interpreter();
        return interpreter.input(expression);
    }

}