/**
 * It passed most test cases, but still have some bugs. Only for study.
 * The python source is ok.
 */




import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Whitespace
 * Whitespace is an esoteric programming language that uses only three characters:
 * <p>
 * [space] or " " (ASCII 32)
 * [tab] or "\t" (ASCII 9)
 * [line-feed] or "\n" (ASCII 10)
 * All other characters may be used for comments. The interpreter ignores them.
 * <p>
 * Whitespace is an imperative, stack-based programming language, including features such as subroutines.
 * <p>
 * Each command in whitespace begins with an Instruction Modification Parameter (IMP).
 * <p>
 * IMPs
 * [space]: Stack Manipulation
 * [tab][space]: Arithmetic
 * [tab][tab]: Heap Access
 * [tab][line-feed]: Input/Output
 * [line-feed]: Flow Control
 * There are two types of data a command may be passed: numbers and labels.
 * <p>
 * Parsing Numbers
 * Numbers begin with a [sign] symbol. The sign symbol is either [tab] -> negative, or [space] -> positive.
 * <p>
 * Numbers end with a [terminal] symbol: [line-feed].
 * <p>
 * Between the sign symbol and the terminal symbol are binary digits [space] -> binary-0, or [tab] -> binary-1.
 * <p>
 * A number expression [sign][terminal] will be treated as zero.
 * <p>
 * The expression of just [terminal] should throw an error. (The Haskell implementation is inconsistent about this.)
 * <p>
 * Parsing Labels
 * Labels begin with any number of [tab] and [space] characters.
 * <p>
 * Labels end with a terminal symbol: [line-feed].
 * <p>
 * Unlike with numbers, the expression of just [terminal] is valid.
 * <p>
 * Labels must be unique.
 * <p>
 * A label may be declared either before or after a command that refers to it.
 * <p>
 * Input/Output
 * As stated earlier, there commands may read data from input or write to output.
 * <p>
 * Parsing Input
 * Whitespace will accept input either characters or integers. Due to the lack of an input stream mechanism, the input will be passed as a string to the interpreter function.
 * <p>
 * Reading a character involves simply taking a character from the input stream.
 * <p>
 * Reading an integer involves parsing a decimal or hexadecimal number from the current position of the input stream, up to and terminated by a line-feed character.
 * <p>
 * The original implementation being in Haskell has stricter requirements for parsing an integer.
 * <p>
 * The Javascript and Coffeescript implementations will accept any number that can be parsed by the parseInt function as a single parameter.
 * <p>
 * The Python implementations will accept any number that can be parsed by the int function as a single parameter.
 * <p>
 * The Java implementations will use an InputStream instance for input. For InputStream use readLine if the program requests a number and read if the program expects a character.
 * <p>
 * An error should be thrown if the input ends before parsing is complete. (This is a non-issue for the Haskell implementation, as it expects user input)
 * <p>
 * Writing Output
 * For a number, append the output string with the number's string value.
 * <p>
 * For a character, simply append the output string with the character.
 * <p>
 * The Java implementations will support an optional OutputStream for output. If an OutputStream is provided, it should be flushed before and after code execution and filled as code is executed. The output string should be returned in any case.
 * <p>
 * Commands
 * Notation: n specifies the parameter, [number] or [label].
 * <p>
 * Errors should be thrown for invalid numbers, labels, and heap addresses, or if there are not enough items on the stack to complete an operation (unless otherwise specified). In addition, an error should be thrown for unclean termination.
 * <p>
 * IMP [space] - Stack Manipulation
 * [space] (number): Push n onto the stack.
 * [tab][space] (number): Duplicate the nth value from the top of the stack.
 * [tab][line-feed] (number): Discard the top n values below the top of the stack from the stack. (For n<0 or n>=stack.length, remove everything but the top value.)
 * [line-feed][space]: Duplicate the top value on the stack.
 * [line-feed][tab]: Swap the top two value on the stack.
 * [line-feed][line-feed]: Discard the top value on the stack.
 * IMP [tab][space] - Arithmetic
 * [space][space]: Pop a and b, then push b+a.
 * [space][tab]: Pop a and b, then push b-a.
 * [space][line-feed]: Pop a and b, then push b*a.
 * [tab][space]: Pop a and b, then push b/a*. If a is zero, throw an error.
 * *Note that the result is defined as the floor of the quotient.
 * [tab][tab]: Pop a and b, then push b%a*. If a is zero, throw an error.
 * *Note that the result is defined as the remainder after division and sign (+/-) of the divisor (a).
 * IMP [tab][tab] - Heap Access
 * [space]: Pop a and b, then store a at heap address b.
 * [tab]: Pop a and then push the value at heap address a onto the stack.
 * IMP [tab][line-feed] - Input/Output
 * [space][space]: Pop a value off the stack and output it as a character.
 * [space][tab]: Pop a value off the stack and output it as a number.
 * [tab][space]: Read a character from input, a, Pop a value off the stack, b, then store the ASCII value of a at heap address b.
 * [tab][tab]: Read a number from input, a, Pop a value off the stack, b, then store a at heap address b.
 * IMP [line-feed] - Flow Control
 * [space][space] (label): Mark a location in the program with label n.
 * [space][tab] (label): Call a subroutine with the location specified by label n.
 * [space][line-feed] (label): Jump unconditionally to the position specified by label n.
 * [tab][space] (label): Pop a value off the stack and jump to the label specified by n if the value is zero.
 * [tab][tab] (label): Pop a value off the stack and jump to the label specified by n if the value is less than zero.
 * [tab][line-feed]: Exit a subroutine and return control to the location from which the subroutine was called.
 * [line-feed][line-feed]: Exit the program.
 * Notes
 * Division and modulo
 * Whitespace expects floored division and modulo
 * <p>
 * In Javascript and Coffeescript, the modulus operator is implemented differently than it was in the original Whitespace interpreter. Whitespace was influenced by having been originally implemented in Haskell. Javascript and Coffeescript also lack integer division operations. You need to pay a little extra attention in regard to the implementation of integer division and the modulus operator (See: floored division in the Wikipedia article "Modulo operation"
 * Java defines methods for floor division and modulo in Math class. The methods differ from the traditional / and % operators.
 * There is no difference between Whitespace and Python in regard to the standard implementation of integer division and modulo operations.
 */
class Pair<K, V> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

public class WhitespaceInterpreter {

    /**
     * Parsing Numbers
     * Numbers begin with a [sign] symbol. The sign symbol is either [tab] -> negative, or [space] -> positive.
     * Numbers end with a [terminal] symbol: [line-feed].
     * Between the sign symbol and the terminal symbol are binary digits [space] -> binary-0, or [tab] -> binary-1.
     * A number expression [sign][terminal] will be treated as zero.
     * The expression of just [terminal] should throw an error. (The Haskell implementation is inconsistent about this.)
     *
     * @param codeSegment
     * @param offset
     * @return
     */
    private static Pair<Integer, Integer> feedNumber(String codeSegment, int offset) {
        int number = 0;
        if (codeSegment.charAt(offset) == 'n')
            throw new RuntimeException("The expression of just [terminal] should throw an error.");

        char ch = codeSegment.charAt(offset++);
        int sign = ch == 't' ? -1 : 1;


        while (offset < codeSegment.length()) {
            ch = codeSegment.charAt(offset++);
            if (ch == 'n') break;
            number = number * 2 + (ch == 't' ? 1 : 0);
        }
        return new Pair<>(sign * number, offset);
    }

    /**
     * Parsing Labels
     * Labels begin with any number of [tab] and [space] characters.
     * Labels end with a terminal symbol: [line-feed].
     * Unlike with numbers, the expression of just [terminal] is valid.
     * Labels must be unique.
     * A label may be declared either before or after a command that refers to it.
     *
     * @param codeSegment
     * @param offset
     * @return
     */
    private static Pair<String, Integer> feedLabel(String codeSegment, int offset) {
        String label = "x";
        char ch;
        while (offset < codeSegment.length()) {
            ch = codeSegment.charAt(offset++);
            if (ch == 'n') break;
            label = label + ch;
        }
        return new Pair<>(label, offset);
    }

    // transforms space characters to ['s','t','n'] chars;
    private static String unbleach(String code) {
        code = code != null ? code.chars().filter(r -> r == ' ' || r == '\t' || r == '\n').collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString() : null;
        return code != null ? code.replace(' ', 's').replace('\t', 't').replace('\n', 'n') : null;
    }

    private static int readLine(InputStream inputStream) throws IOException {
        int ret = 0;
        int ch = 0;

        while (true) {
            ch = inputStream.read();
            if (ch == -1) throw new RuntimeException("Exception end of input.");
            if (ch == 10) break;
            ret = ret * 10 + ch - '0';
        }
        return ret;
    }

    /**
     * * IMP [space] - Stack Manipulation
     * * [space] (number): Push n onto the stack.
     * * [tab][space] (number): Duplicate the nth value from the top of the stack.
     * * [tab][line-feed] (number): Discard the top n values below the top of the stack from the stack. (For n<0 or n>=stack.length, remove everything but the top value.)
     * * [line-feed][space]: Duplicate the top value on the stack.
     * * [line-feed][tab]: Swap the top two value on the stack.
     * * [line-feed][line-feed]: Discard the top value on the stack.
     */
    enum StackOP {
        PUSH, DUPLICATE_TOP_OF_N, DISCARD_TOP_N, DUPLICATE_TOP, SWAP, DISCARD_TOP
    }

    private static void StackEngine(StackOP stackOP, Integer param, Stack<Integer> stack) {
        switch (stackOP) {
            case PUSH:
                stack.push(param);
                break;
            case SWAP:
                int tmpa = stack.pop();
                int tmpb = stack.pop();
                stack.push(tmpa);
                stack.push(tmpb);
                break;
            case DISCARD_TOP:
                stack.pop();
                break;
            case DISCARD_TOP_N:
                tmpa = stack.pop();
                if (param < 0 || param >= stack.size()) {
                    stack.clear();
                } else {
                    while (param-- > 0) stack.pop();
                }
                stack.push(tmpa);
                break;
            case DUPLICATE_TOP:
                stack.push(stack.peek());
                break;
            case DUPLICATE_TOP_OF_N:
                stack.push(stack.get(stack.size() - 1 - param));
                break;
        }
    }

    private static int StackProcess(String codeSegment, int offset, List<String> explainCode) {
        char ch = codeSegment.charAt(offset++);
        Pair<Integer, Integer> pair;
        if (ch == 's') {
            pair = feedNumber(codeSegment, offset);
            offset = pair.value;
            explainCode.add(StackOP.PUSH.toString());
            explainCode.add(pair.key.toString());
//            StackEngine(StackOP.PUSH, pair.key, stack);
        } else if (ch == 't') {
            ch = codeSegment.charAt(offset++);
            pair = feedNumber(codeSegment, offset);
            offset = pair.value;
            if (ch == 's') {
                explainCode.add(StackOP.DUPLICATE_TOP_OF_N.toString());
                explainCode.add(pair.key.toString());
//                StackEngine(StackOP.DUPLICATE_TOP_OF_N, pair.key, stack);
            } else if (ch == 'n') {
                explainCode.add(StackOP.DISCARD_TOP_N.toString());
                explainCode.add(pair.key.toString());
//                StackEngine(StackOP.DISCARD_TOP_N, pair.key, stack);
            } else {
                throw new RuntimeException("UNKONWN");
            }
        } else if (ch == 'n') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                explainCode.add(StackOP.DUPLICATE_TOP.toString());
//                StackEngine(StackOP.DUPLICATE_TOP, null, stack);
            } else if (ch == 'n') {
                explainCode.add(StackOP.DISCARD_TOP.toString());
//                StackEngine(StackOP.DISCARD_TOP, null, stack);
            } else if (ch == 't') {
                explainCode.add(StackOP.SWAP.toString());
//                StackEngine(StackOP.SWAP, null, stack);
            } else {
                throw new RuntimeException("UNKONWN");
            }
        } else {
            throw new RuntimeException("UNKONWN");
        }
        return offset;
    }

    /**
     * * IMP [tab][tab] - Heap Access
     * * [space]: Pop a and b, then store a at heap address b.
     * * [tab]: Pop a and then push the value at heap address a onto the stack.
     */
    enum HeapOP {
        STORE_TO_HEAP, STORE_TO_STACK
    }

    private static void HeapEngine(HeapOP heapOP, Stack<Integer> stack, Map<Integer, Integer> heap) {
        switch (heapOP) {
            case STORE_TO_HEAP:
                int tmpa = stack.pop();
                int tmpb = stack.pop();
                heap.put(tmpb, tmpa);
                break;
            case STORE_TO_STACK:
                tmpa = stack.pop();
                tmpb = heap.get(tmpa);
                stack.push(tmpb);
                break;
        }
    }

    private static int HeapProcess(String codeSegment, int offset, List<String> explainCode) {
        char ch = codeSegment.charAt(offset++);
        if (ch == 's') {
            explainCode.add(HeapOP.STORE_TO_HEAP.toString());
//            HeapEngine(HeapOP.STORE_TO_HEAP, stack, heap);
        } else if (ch == 't') {
            explainCode.add(HeapOP.STORE_TO_STACK.toString());
//            HeapEngine(HeapOP.STORE_TO_STACK, stack, heap);
        } else {
            throw new RuntimeException("UNKONWN");
        }
        return offset;
    }

    /**
     * * IMP [tab][space] - Arithmetic
     * * [space][space]: Pop a and b, then push b+a.
     * * [space][tab]: Pop a and b, then push b-a.
     * * [space][line-feed]: Pop a and b, then push b*a.
     * * [tab][space]: Pop a and b, then push b/a*. If a is zero, throw an error.
     * * *Note that the result is defined as the floor of the quotient.
     * * [tab][tab]: Pop a and b, then push b%a*. If a is zero, throw an error.
     * * *Note that the result is defined as the remainder after division and sign (+/-) of the divisor (a).
     */
    enum ArithmeticOP {
        ADD, MINUS, MUL, DIV, MOD
    }

    private static void ArithmeticEngine(ArithmeticOP arithmeticOP, Stack<Integer> stack) {
        int tmpa = stack.pop();
        int tmpb = stack.pop();
        switch (arithmeticOP) {
            case ADD:
                stack.push(tmpa + tmpb);
                break;
            case DIV:
                stack.push(Math.floorDiv(tmpb, tmpa));
                break;
            case MOD:
                stack.push(Math.floorMod(tmpb, tmpa));
                break;
            case MUL:
                stack.push(tmpa * tmpb);
                break;
            case MINUS:
                stack.push(tmpb - tmpa);
                break;
        }
    }

    private static int ArithmeticProcess(String codeSegment, int offset, List<String> explainCode) {
        char ch = codeSegment.charAt(offset++);
        if (ch == 's') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                explainCode.add(ArithmeticOP.ADD.toString());
//                ArithmeticEngine(ArithmeticOP.ADD, stack);
            } else if (ch == 't') {
                explainCode.add(ArithmeticOP.MINUS.toString());
//                ArithmeticEngine(ArithmeticOP.MINUS, stack);
            } else if (ch == 'n') {
                explainCode.add(ArithmeticOP.MUL.toString());
//                ArithmeticEngine(ArithmeticOP.MUL, stack);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else if (ch == 't') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                explainCode.add(ArithmeticOP.DIV.toString());
//                ArithmeticEngine(ArithmeticOP.DIV, stack);
            } else if (ch == 't') {
                explainCode.add(ArithmeticOP.MOD.toString());
//                ArithmeticEngine(ArithmeticOP.MOD, stack);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else {
            throw new RuntimeException("UNKNOWN");
        }
        return offset;
    }

    /**
     * * IMP [tab][line-feed] - Input/Output
     * * [space][space]: Pop a value off the stack and output it as a character.
     * * [space][tab]: Pop a value off the stack and output it as a number.
     * * [tab][space]: Read a character from input, a, Pop a value off the stack, b, then store the ASCII value of a at heap address b.
     * * [tab][tab]: Read a number from input, a, Pop a value off the stack, b, then store a at heap address b.
     */
    enum IOOP {
        OUTPUT_CHAR, OUTPUT_NUMBER, INPUT_CHAR, INPUT_NUMBER
    }

    private static String IOEngine(IOOP ioop, Stack<Integer> stack, Map<Integer, Integer> heap, InputStream inputStream) throws IOException {
        switch (ioop) {
            case INPUT_NUMBER:
                int ch = readLine(inputStream);
                System.out.println("InputStream number " + ch);
                int tmpa = stack.pop();
                heap.put(tmpa, ch);
                break;
            case INPUT_CHAR:
                ch = inputStream.read();
                System.out.println("InputStream char " + ch);
                if (ch < 0 || ch > 127) throw new RuntimeException("Char boom");
                tmpa = stack.pop();
                heap.put(tmpa, ch);
                break;
            case OUTPUT_CHAR:
                tmpa = stack.pop();
                return String.valueOf(((char) tmpa));
            case OUTPUT_NUMBER:
                tmpa = stack.pop();
                return String.valueOf(tmpa);
        }
        return null;
    }

    private static int IOProcess(String codeSegment, int offset, List<String> explainCode) {
        char ch = codeSegment.charAt(offset++);
        if (ch == 's') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                explainCode.add(IOOP.OUTPUT_CHAR.toString());
//                output += IOEngine(IOOP.OUTPUT_CHAR, stack, heap, inputStream);
            } else if (ch == 't') {
                explainCode.add(IOOP.OUTPUT_NUMBER.toString());
//                output += IOEngine(IOOP.OUTPUT_NUMBER, stack, heap, inputStream);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else if (ch == 't') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                explainCode.add(IOOP.INPUT_CHAR.toString());
//                IOEngine(IOOP.INPUT_CHAR, stack, heap, inputStream);
            } else if (ch == 't') {
                explainCode.add(IOOP.INPUT_NUMBER.toString());
//                IOEngine(IOOP.INPUT_NUMBER, stack, heap, inputStream);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else {
            throw new RuntimeException("UNKNOWN");
        }
        return offset;
    }

    /**
     * * IMP [line-feed] - Flow Control
     * * [space][space] (label): Mark a location in the program with label n.
     * * [space][tab] (label): Call a subroutine with the location specified by label n.
     * * [space][line-feed] (label): Jump unconditionally to the position specified by label n.
     * * [tab][space] (label): Pop a value off the stack and jump to the label specified by n if the value is zero.
     * * [tab][tab] (label): Pop a value off the stack and jump to the label specified by n if the value is less than zero.
     * * [tab][line-feed]: Exit a subroutine and return control to the location from which the subroutine was called.
     * * [line-feed][line-feed]: Exit the program.
     */
    enum FlowControOP {
        MARK_LABEL, CALL_LABEL_N, JMP_LABEL_N, JMP_N_IF_STACK_EQ_ZERO, JMP_N_IF_STACK_LE_ZERO, EXIT_SUBROUTINE, EXIT
    }

    private static int FlowControlEngine(FlowControOP fcop, String label, int offset, Map<String, Integer> labels, Stack<Integer> stack, Stack<Integer> codeStack) {
        int tmp;
        switch (fcop) {
            case MARK_LABEL:
                labels.put(label, offset);
                break;
            case CALL_LABEL_N:
                codeStack.push(offset);
                offset = labels.get(label);
                break;
            case JMP_LABEL_N:
                offset = labels.get(label);
                break;
            case JMP_N_IF_STACK_EQ_ZERO:
                tmp = stack.pop();
                if (tmp == 0) offset = labels.get(label);
                break;
            case JMP_N_IF_STACK_LE_ZERO:
                tmp = stack.pop();
                if (tmp < 0) offset = labels.get(label);
                break;
            case EXIT_SUBROUTINE:
                if (codeStack.empty()) throw new RuntimeException("Code stack empty.");
                offset = codeStack.pop();
                break;
            case EXIT:
                offset = -1;
                break;
        }
        return offset;
    }

    private static int FlowControlProcess(String codeSegment, int offset, List<String> explainCode) {
        char ch = codeSegment.charAt(offset++);
        Pair<String, Integer> pair;

        if (ch == 's') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                pair = feedLabel(codeSegment, offset);
                offset = pair.value;
                explainCode.add(FlowControOP.MARK_LABEL.toString());
                explainCode.add(pair.key);
//                offset = FlowControlEngine(FlowControOP.MARK_LABEL, pair.key, pair.value, labels, null, null);
            } else if (ch == 't') {
                pair = feedLabel(codeSegment, offset);
                offset = pair.value;
                explainCode.add(FlowControOP.CALL_LABEL_N.toString());
                explainCode.add(pair.key);
//                offset = FlowControlEngine(FlowControOP.CALL_LABEL_N, pair.key, pair.value, labels, null, codeStack);
            } else if (ch == 'n') {
                pair = feedLabel(codeSegment, offset);
                offset = pair.value;
                explainCode.add(FlowControOP.JMP_LABEL_N.toString());
                explainCode.add(pair.key);
//                offset = FlowControlEngine(FlowControOP.JMP_LABEL_N, pair.key, pair.value, labels, null, null);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else if (ch == 't') {
            ch = codeSegment.charAt(offset++);
            if (ch == 's') {
                pair = feedLabel(codeSegment, offset);
                offset = pair.value;
                explainCode.add(FlowControOP.JMP_N_IF_STACK_EQ_ZERO.toString());
                explainCode.add(pair.key);
//                offset = FlowControlEngine(FlowControOP.JMP_N_IF_STACK_EQ_ZERO, pair.key, pair.value, labels, stack, null);
            } else if (ch == 't') {
                pair = feedLabel(codeSegment, offset);
                offset = pair.value;
                explainCode.add(FlowControOP.JMP_N_IF_STACK_LE_ZERO.toString());
                explainCode.add(pair.key);
//                offset = FlowControlEngine(FlowControOP.JMP_N_IF_STACK_LE_ZERO, pair.key, pair.value, labels, stack, null);
            } else if (ch == 'n') {
                explainCode.add(FlowControOP.EXIT_SUBROUTINE.toString());
//                offset = FlowControlEngine(FlowControOP.EXIT_SUBROUTINE, "", offset, null, null, codeStack);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else if (ch == 'n') {
            ch = codeSegment.charAt(offset++);
            if (ch == 'n') {
                explainCode.add(FlowControOP.EXIT.toString());
//                offset = -1;
//                offset = FlowControlEngine(FlowControOP.EXIT, "", -1, null, null, null);
            } else {
                throw new RuntimeException("UNKNOWN");
            }
        } else {
            throw new RuntimeException("UNKNOWN");
        }
        return offset;
    }

    private static Pair<String, Integer> run(List<String> explainCode, Map<String, Integer> labels, InputStream inputStream) throws IOException {
        String output = "";
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> heap = new HashMap<>();
        Stack<Integer> codeStack = new Stack<>();
        int position = 0;
        while (position < explainCode.size()) {
            String op = explainCode.get(position++);
            if ("PUSH".equals(op)) {
                int param = Integer.valueOf(explainCode.get(position++));
                StackEngine(StackOP.PUSH, param, stack);
            } else if ("DUPLICATE_TOP_OF_N".equals(op)) {
                int param = Integer.valueOf(explainCode.get(position++));
                StackEngine(StackOP.DUPLICATE_TOP_OF_N, param, stack);
            } else if ("DISCARD_TOP_N".equals(op)) {
                int param = Integer.valueOf(explainCode.get(position++));
                StackEngine(StackOP.DISCARD_TOP_N, param, stack);
            } else if ("DUPLICATE_TOP".equals(op)) {
                StackEngine(StackOP.DUPLICATE_TOP, null, stack);
            } else if ("SWAP".equals(op)) {
                StackEngine(StackOP.SWAP, null, stack);
            } else if ("DISCARD_TOP".equals(op)) {
                StackEngine(StackOP.DISCARD_TOP, null, stack);
            } else if ("STORE_TO_HEAP".equals(op)) {
                HeapEngine(HeapOP.STORE_TO_HEAP, stack, heap);
            } else if ("STORE_TO_STACK".equals(op)) {
                HeapEngine(HeapOP.STORE_TO_STACK, stack, heap);
            } else if ("ADD".equals(op)) {
                ArithmeticEngine(ArithmeticOP.ADD, stack);
            } else if ("MINUS".equals(op)) {
                ArithmeticEngine(ArithmeticOP.MINUS, stack);
            } else if ("MUL".equals(op)) {
                ArithmeticEngine(ArithmeticOP.MUL, stack);
            } else if ("DIV".equals(op)) {
                ArithmeticEngine(ArithmeticOP.DIV, stack);
            } else if ("MOD".equals(op)) {
                ArithmeticEngine(ArithmeticOP.MOD, stack);
            } else if ("OUTPUT_CHAR".equals(op)) {
                output += IOEngine(IOOP.OUTPUT_CHAR, stack, heap, inputStream);
            } else if ("OUTPUT_NUMBER".equals(op)) {
                output += IOEngine(IOOP.OUTPUT_NUMBER, stack, heap, inputStream);
            } else if ("INPUT_CHAR".equals(op)) {
                IOEngine(IOOP.INPUT_CHAR, stack, heap, inputStream);
            } else if ("INPUT_NUMBER".equals(op)) {
                IOEngine(IOOP.INPUT_NUMBER, stack, heap, inputStream);
            } else if ("MARK_LABEL".equals(op)) { // Ignore
                position++;
            } else if ("CALL_LABEL_N".equals(op)) {
                String label = explainCode.get(position++);
                position = FlowControlEngine(FlowControOP.CALL_LABEL_N, label, position, labels, null, codeStack);
            } else if ("JMP_LABEL_N".equals(op)) {
                String label = explainCode.get(position++);
                position = FlowControlEngine(FlowControOP.JMP_LABEL_N, label, position, labels, null, null);
            } else if ("JMP_N_IF_STACK_EQ_ZERO".equals(op)) {
                String label = explainCode.get(position++);
                position = FlowControlEngine(FlowControOP.JMP_N_IF_STACK_EQ_ZERO, label, position, labels, stack, null);
            } else if ("JMP_N_IF_STACK_LE_ZERO".equals(op)) {
                String label = explainCode.get(position++);
                position = FlowControlEngine(FlowControOP.JMP_N_IF_STACK_LE_ZERO, label, position, labels, stack, null);
            } else if ("EXIT_SUBROUTINE".equals(op)) {
                position = FlowControlEngine(FlowControOP.EXIT_SUBROUTINE, "", position, null, null, codeStack);
            } else if ("EXIT".equals(op)) {
                return new Pair<>(output, 0);
            } else {
                throw new RuntimeException("UNKNOWN STATUS");
            }
        }
        return new Pair<>(output, -1);
    }

    // solution
    public static String execute(String code, InputStream input, OutputStream output) {
        String ret = execute(code, input);
        return ret;
    }

    public static String execute(String code, InputStream input) {
        String codeSegment = unbleach(code);
        System.out.println(codeSegment);

        List<String> explainCode = new ArrayList<>(codeSegment.length() / 2);

        int position = 0;
        while (position < codeSegment.length()) {
            char ch = codeSegment.charAt(position++);
            if (ch == 's') {
                position = StackProcess(codeSegment, position, explainCode);
            } else if (ch == 't') {
                ch = codeSegment.charAt(position++);
                if (ch == 't') {
                    position = HeapProcess(codeSegment, position, explainCode);
                } else if (ch == 's') {
                    position = ArithmeticProcess(codeSegment, position, explainCode);
                } else if (ch == 'n') {
                    position = IOProcess(codeSegment, position, explainCode);
                }
            } else if (ch == 'n') {
                position = FlowControlProcess(codeSegment, position, explainCode);
                if (position == -1) {
                    break;
                }
            }
        }

//        System.out.println(explainCode);

//        if (position != -1) {
//            throw new RuntimeException("Program unterminated.");
//        }

        Map<String, Integer> labels = new HashMap<>();

        for (int i = 0; i < explainCode.size(); ++i) {
            if (explainCode.get(i).equals("MARK_LABEL")) {
                if (labels.containsKey(explainCode.get(i + 1))) throw new RuntimeException("Repeated labels");
                labels.put(explainCode.get(i + 1), i + 2);
            }
        }

//        System.out.println(labels);

        String output = null;
        try {
            Pair<String, Integer> pair = run(explainCode, labels, input);
            if (pair.value != 0) {
                throw new RuntimeException("Exception @ Exit");
            }
            output = pair.key;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

}
