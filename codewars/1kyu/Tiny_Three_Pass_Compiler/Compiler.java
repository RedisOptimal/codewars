/**
 * @author zhe
 * @createTime 5/16/16
 * @description
 */

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {

    private static class Word {
        private final String word;
        private final WordType type;

        public Word(String word, WordType type) {
            this.word = word;
            this.type = type;
        }

        public String getWord() {
            return word;
        }

        public WordType getType() {
            return type;
        }

        @Override
        public String toString() {
            return getWord();
        }
    }

    private static final Word endWord = new Word("$", WordType.END);

    private enum WordType {
        LEFT_BRACKET,
        RIGHT_BRACKET,
        CONST_NUMBER,
        VARIABLE,
        PLUS,
        MINUS,
        MULTIPLE,
        DIVISION,
        END
    }

    private static Map<String, Word> staticMap = new HashMap<>();

    static {
        staticMap.put("[", new Word("[", WordType.LEFT_BRACKET));
        staticMap.put("]", new Word("]", WordType.RIGHT_BRACKET));
        staticMap.put("(", new Word("(", WordType.LEFT_BRACKET));
        staticMap.put(")", new Word(")", WordType.RIGHT_BRACKET));
        staticMap.put("+", new Word("+", WordType.PLUS));
        staticMap.put("-", new Word("-", WordType.MINUS));
        staticMap.put("*", new Word("*", WordType.MULTIPLE));
        staticMap.put("/", new Word("/", WordType.DIVISION));
        staticMap.put("$", endWord);
    }

    private enum ProcessingStatus {
        ARG_LIST,
        EXPRESSION,
        END
    }

    private Map<String, Integer> variablePositionMap = null;

    private Ast astRoot = null;

    public Compiler() {
        variablePositionMap = new HashMap<>();
    }

    /**
     * ( < +- < /* < )
     *
     * @param expression
     * @return
     */
    private Ast buildAst(Deque<Word> expression) {
        Stack<Word> op = new Stack<>();
        Ast root = null;
        List<Word> prefixExpression = new ArrayList<>(expression.size());
        Iterator<Word> riterator = expression.descendingIterator();
        while (riterator.hasNext()) {
            Word word = riterator.next();
            if (word.getType() == WordType.VARIABLE || word.getType() == WordType.CONST_NUMBER) {
                prefixExpression.add(word);
            } else if (word.getType() == WordType.LEFT_BRACKET) {
                while (op.peek().getType() != WordType.RIGHT_BRACKET) {
                    prefixExpression.add(op.pop());
                }
                op.pop();
            } else if (word.getType() == WordType.RIGHT_BRACKET) {
                op.push(word);
            } else if (word.getType() == WordType.PLUS ||
                    word.getType() == WordType.MINUS) {
                while (!op.isEmpty() && op.peek().getType() != WordType.RIGHT_BRACKET &&
                        op.peek().getType() != WordType.MINUS &&
                        op.peek().getType() != WordType.PLUS) {
                    prefixExpression.add(op.pop());
                }
                op.push(word);
            } else if (word.getType() == WordType.MULTIPLE ||
                    word.getType() == WordType.DIVISION) {
//                while (!op.isEmpty() && op.peek().getType() != WordType.RIGHT_BRACKET
//                        && op.peek().getType() != WordType.PLUS && op.peek().getType() != WordType.MINUS) {
//                    prefixExpression.add(op.pop());
//                }
                op.push(word);
            }
        }
        while (!op.isEmpty()) {
            prefixExpression.add(op.pop());
        }
        Collections.reverse(prefixExpression);
        class AstVo {
            private Ast ast;
            private Word word;

            public AstVo(Ast ast, Word word) {
                this.ast = ast;
                this.word = word;
            }
        }
        Stack<AstVo> astStack = new Stack<>();
        for (Word word : prefixExpression) {
            if (word.getType() == WordType.VARIABLE) {
                astStack.push(new AstVo(new UnOp("arg", getVariableOffset(word.getWord())), word));
            } else if (word.getType() == WordType.CONST_NUMBER) {
                astStack.push(new AstVo(new UnOp("imm", Integer.parseInt(word.getWord())), word));
            } else {
                astStack.push(new AstVo(null, word));
            }
            while (astStack.size() > 2) {
                AstVo secondParam = astStack.pop();
                AstVo firstParam = astStack.pop();
                AstVo operation = astStack.peek();
                if (secondParam.ast != null && firstParam.ast != null) {
                    operation.ast = new BinOp(operation.word.getWord(), firstParam.ast, secondParam.ast);
                } else {
                    astStack.push(firstParam);
                    astStack.push(secondParam);
                    break;
                }
            }
        }
        root = astStack.peek().ast;
        return root;
    }

    private Integer getVariableOffset(String variable) {
        return variablePositionMap.get(variable);
    }

    private void registVariable(Word variable, Integer offset) {
        if (variable != null && variable.getWord().length() > 0) {
            variablePositionMap.putIfAbsent(variable.getWord(), offset);
        }
    }

    private void parseArgList(Deque<Word> argList) {
        AtomicInteger offset = new AtomicInteger(0);
        for (Word word : argList) {
            if (word.getType() == WordType.END) {
                break;
            } else if (word.getType() == WordType.VARIABLE) {
                registVariable(word, offset.getAndIncrement());
            }
        }
    }

    private Ast parseSyntax(Deque<Word> prog) {
        ProcessingStatus processingStatus = ProcessingStatus.ARG_LIST;
        Deque<Word> argList = new LinkedList<>();
        Deque<Word> expression = new LinkedList<>();
        for (Word word : prog) {
            if (word.getType() == WordType.END) {
                processingStatus = ProcessingStatus.END;
                break;
            }
            if (processingStatus == ProcessingStatus.ARG_LIST) {
                argList.add(word);
                if (word.getType() == WordType.RIGHT_BRACKET) {
                    processingStatus = ProcessingStatus.EXPRESSION;
                }
            } else if (processingStatus == ProcessingStatus.EXPRESSION) {
                expression.add(word);
            }
        }
        parseArgList(argList);
        astRoot = buildAst(expression);
        return astRoot;
    }

    private Ast optimizeAst(Ast astRoot) {
        Ast newAstRoot = null;
        if (astRoot == null) {
            return null;
        }
        if (astRoot instanceof UnOp) {
            return astRoot;
        } else if (astRoot instanceof BinOp) {
            Ast a = optimizeAst(((BinOp) astRoot).a());
            Ast b = optimizeAst(((BinOp) astRoot).b());
            if (a instanceof UnOp && a.op().equals("imm") &&
                    b instanceof UnOp && b.op().equals("imm")) {
                Integer number1 = ((UnOp) a).n();
                Integer number2 = ((UnOp) b).n();
                int result = 0;
                switch (astRoot.op()) {
                    case "+":
                        result = number1 + number2;
                        break;
                    case "-":
                        result = number1 - number2;
                        break;
                    case "*":
                        result = number1 * number2;
                        break;
                    case "/":
                        result = number1 / number2;
                        break;
                }
                return new UnOp("imm", result);
            } else {
                return new BinOp(astRoot.op(), a, b);
            }
        }
        return newAstRoot;
    }

    private static void saveUnOpToStack(Ast ast, List<String> asmOperationSequence) {
        if (ast instanceof UnOp) {
            if ("imm".equals(ast.op())) {
                asmOperationSequence.add("IM " + ((UnOp) ast).n());
            } else if ("arg".equals(ast.op())) {
                asmOperationSequence.add("AR " + ((UnOp) ast).n());
            }
            asmOperationSequence.add("PU");
        } else {
            assert false;
        }
    }

    private static void saveUnOpToR1(Ast ast, List<String> asmOperationSequence) {
        if (ast instanceof UnOp) {
            if ("imm".equals(ast.op())) {
                asmOperationSequence.add("IM " + ((UnOp) ast).n());
            } else if ("arg".equals(ast.op())) {
                asmOperationSequence.add("AR " + ((UnOp) ast).n());
            }
            asmOperationSequence.add("SW");
        } else {
            assert false;
        }
    }

    private static void calcAndSaveToStack(Ast ast, List<String> asmOperationSequence) {
        switch (ast.op()) {
            case "+":
                asmOperationSequence.add("AD");
                break;
            case "-":
                asmOperationSequence.add("SU");
                break;
            case "*":
                asmOperationSequence.add("MU");
                break;
            case "/":
                asmOperationSequence.add("DI");
                break;
        }
        asmOperationSequence.add("PU");
    }

    private static void popStackToR0(List<String> asmOperationSequence) {
        asmOperationSequence.add("PO");
    }

    private static void popStackToR1(List<String> asmOperationSequence) {
        asmOperationSequence.add("PO");
        asmOperationSequence.add("SW");
    }


    private void constructAsmOperationSequence(Ast ast, List<String> asmOperationSequence) {
        if (ast == null) {
            return;
        }
        if (ast instanceof UnOp) {
                saveUnOpToStack(ast, asmOperationSequence);
        } else if (ast instanceof BinOp) {
            constructAsmOperationSequence(((BinOp) ast).a(), asmOperationSequence);
            constructAsmOperationSequence(((BinOp) ast).b(), asmOperationSequence);
            popStackToR1(asmOperationSequence);
            popStackToR0(asmOperationSequence);
            calcAndSaveToStack(ast, asmOperationSequence);
        }
    }

    public List<String> compile(String prog) {
        return pass3(pass2(pass1(prog)));
    }

    /**
     * Returns an un-optimized AST
     */
    public Ast pass1(String prog) {
        Deque<Word> tokens = tokenize(prog);
        return parseSyntax(tokens);
    }

    /**
     * Returns an AST with constant expressions reduced
     */
    public Ast pass2(Ast ast) {
        return optimizeAst(ast);
    }

    /**
     * Returns assembly instructions
     */
    public List<String> pass3(Ast ast) {
        List<String> asmOperationSequence = new LinkedList<>();
        constructAsmOperationSequence(ast, asmOperationSequence);
        return asmOperationSequence;
    }

    private static Deque<Word> tokenize(String prog) {
        Deque<Word> tokens = new LinkedList<>();
        Pattern pattern = Pattern.compile("[-+*/()\\[\\]]|[a-zA-Z]+|\\d+");
        Matcher m = pattern.matcher(prog);
        while (m.find()) {
            String str = m.group();
            Word word = staticMap.get(str);
            if (word == null) {
                try {
                    Long number = Long.parseLong(str);
                    word = new Word(str, WordType.CONST_NUMBER);
                } catch (NumberFormatException e) {
                    word = new Word(str, WordType.VARIABLE);
                }
            }
            tokens.add(word);
        }
        tokens.add(endWord); // end-of-stream
        return tokens;
    }
}
