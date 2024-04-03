#!/usr/bin/env python
# -*- coding: utf-8 -*-

from enum import Enum, unique


# import Test

def warp_input_stream(inp):
    while inp:
        yield inp[0]
        inp = inp[1:]

    raise Exception("Exception end of input.")


def next_char(input_stream):
    ch = ord(next(input_stream))
    # if ch < 0 or ch > 127: raise Exception("Char boom")
    return ch


def next_int(input_stream):
    ret = ''
    ch = next(input_stream)
    while ch != '\n':
        ret += ch
        ch = next(input_stream)

    return int(ret)


def decode(n):
    return n.replace('s', ' ').replace('t', '\t').replace('n', '\n')


def unbleach(n):
    n = ''.join(filter(lambda x: x in {' ', '\t', '\n'}, n))
    return n.replace(' ', 's').replace('\t', 't').replace('\n', 'n')


def feed_number(codeSegment, offset):
    number = 0
    if codeSegment[offset] == 'n':
        raise Exception("The expression of just [terminal] should throw an error.")

    ch = codeSegment[offset]
    offset += 1
    sign = -1 if ch == 't' else 1

    while offset < len(codeSegment):
        ch = codeSegment[offset]
        offset += 1
        if ch == 'n': break
        number = number * 2 + (1 if ch == 't' else 0)

    return (sign * number, offset)


def feed_label(codeSegment, offset):
    label = "x"
    while offset < len(codeSegment):
        ch = codeSegment[offset]
        offset += 1
        if ch == 'n': break
        label = label + ch
    return (label, offset)


@unique
class STACK_OP(Enum):
    PUSH = 0
    DUPLICATE_TOP_OF_N = 1
    DISCARD_TOP_N = 2
    DUPLICATE_TOP = 3
    SWAP = 4
    DISCARD_TOP = 5


def stack_engine(op, param, stack):
    if op == STACK_OP.PUSH:
        stack.append(param)
    elif op == STACK_OP.SWAP:
        tmpa = stack.pop()
        tmpb = stack.pop()
        stack.append(tmpa)
        stack.append(tmpb)
    elif op == STACK_OP.DISCARD_TOP:
        stack.pop()
    elif op == STACK_OP.DISCARD_TOP_N:
        tmpa = stack.pop()
        if param < 0 or param >= len(stack):
            stack.clear()
        else:
            while param > 0:
                param -= 1
                stack.pop()
        stack.append(tmpa)
    elif op == STACK_OP.DUPLICATE_TOP:
        tmp = stack.pop()
        stack.append(tmp)
        stack.append(tmp)
    elif op == STACK_OP.DUPLICATE_TOP_OF_N:
        if len(stack) - 1 - param < 0: raise Exception("Out bound of stack")
        stack.append(stack[len(stack) - 1 - param])


def stack_process(code_segment, offset, explain_code):
    ch = code_segment[offset]
    offset += 1
    if ch == 's':
        number, offset = feed_number(code_segment, offset)
        explain_code.append(STACK_OP.PUSH)
        explain_code.append(number)
    elif ch == 't':
        ch = code_segment[offset]
        offset += 1
        number, offset = feed_number(code_segment, offset)
        if ch == 's':
            explain_code.append(STACK_OP.DUPLICATE_TOP_OF_N)
        elif ch == 'n':
            explain_code.append(STACK_OP.DISCARD_TOP_N)
        else:
            raise Exception("UNKNOWN")
        explain_code.append(number)
    elif ch == 'n':
        ch = code_segment[offset]
        offset += 1
        if ch == 's':
            explain_code.append(STACK_OP.DUPLICATE_TOP)
        elif ch == 'n':
            explain_code.append(STACK_OP.DISCARD_TOP)
        elif ch == 't':
            explain_code.append(STACK_OP.SWAP)
        else:
            raise Exception("UNKNOWN")
    else:
        raise Exception("UNKNOWN")

    return offset


@unique
class HEAP_OP(Enum):
    STORE_TO_HEAP = 10
    STORE_TO_STACK = 11


def heap_engine(op, stack, heap):
    if op == HEAP_OP.STORE_TO_HEAP:
        tmpa = stack.pop()
        tmpb = stack.pop()
        heap[tmpb] = tmpa
    elif op == HEAP_OP.STORE_TO_STACK:
        stack.append(heap[stack.pop()])
    else:
        raise Exception("UNKNOWN")


def heap_process(code_segment, offset, explain_code):
    ch = code_segment[offset]
    offset += 1
    if ch == 's':
        explain_code.append(HEAP_OP.STORE_TO_HEAP)
    elif ch == 't':
        explain_code.append(HEAP_OP.STORE_TO_STACK)
    else:
        raise Exception("UNKNOWN")
    return offset


class ARITHMETIC_OP(Enum):
    ADD = 20
    MINUS = 21
    MUL = 22
    DIV = 23
    MOD = 24


def arithmetic_engine(op, stack):
    tmpa = stack.pop()
    tmpb = stack.pop()
    if op == ARITHMETIC_OP.ADD:
        stack.append(tmpa + tmpb)
    elif op == ARITHMETIC_OP.MINUS:
        stack.append(tmpb - tmpa)
    elif op == ARITHMETIC_OP.MUL:
        stack.append(tmpa * tmpb)
    elif op == ARITHMETIC_OP.DIV:
        stack.append(tmpb // tmpa)
    elif op == ARITHMETIC_OP.MOD:
        stack.append(tmpb % tmpa)
    else:
        raise Exception("UNKNOWN")


def arithmetic_process(code_segment, offset, explain_code):
    ch = code_segment[offset:offset + 2]
    offset += 2
    if ch == "ss":
        explain_code.append(ARITHMETIC_OP.ADD)
    elif ch == "st":
        explain_code.append(ARITHMETIC_OP.MINUS)
    elif ch == "sn":
        explain_code.append(ARITHMETIC_OP.MUL)
    elif ch == "ts":
        explain_code.append(ARITHMETIC_OP.DIV)
    elif ch == "tt":
        explain_code.append(ARITHMETIC_OP.MOD)
    else:
        raise Exception("UNKNOWN")
    return offset


@unique
class IO_OP(Enum):
    OUTPUT_CHAR = 30
    OUTPUT_NUMBER = 31
    INPUT_CHAR = 32
    INPUT_NUMBER = 33


def io_engine(op, stack, heap, input_stream):
    if op == IO_OP.INPUT_NUMBER or op == IO_OP.INPUT_CHAR:
        ch = next_char(input_stream) if op == IO_OP.INPUT_CHAR else next_int(input_stream)
        print(ch)
        tmpa = stack.pop()
        heap[tmpa] = ch
    elif op == IO_OP.OUTPUT_CHAR:
        return chr(stack.pop())
    elif op == IO_OP.OUTPUT_NUMBER:
        return str(stack.pop())
    return None


def io_process(code_segment, offset, explain_code):
    ch = code_segment[offset:offset + 2]
    offset += 2
    if ch == "ss":
        explain_code.append(IO_OP.OUTPUT_CHAR)
    elif ch == "st":
        explain_code.append(IO_OP.OUTPUT_NUMBER)
    elif ch == "ts":
        explain_code.append(IO_OP.INPUT_CHAR)
    elif ch == "tt":
        explain_code.append(IO_OP.INPUT_NUMBER)
    else:
        raise Exception("UNKNOWN")
    return offset


@unique
class FLOW_CONTROL_OP(Enum):
    MARK_LABEL = 40
    CALL_LABEL_N = 41
    JMP_LABEL_N = 42
    JMP_N_IF_STACK_EQ_ZERO = 43
    JMP_N_IF_STACK_LT_ZERO = 44
    EXIT_SUBROUTINE = 45
    EXIT = 46


def flow_control_engine(op, label, offset, labels, stack, runtime_stack):
    if op == FLOW_CONTROL_OP.MARK_LABEL:
        labels[label] = offset
    elif op == FLOW_CONTROL_OP.CALL_LABEL_N:
        runtime_stack.append(offset)
        offset = labels[label]
    elif op == FLOW_CONTROL_OP.JMP_LABEL_N:
        offset = labels[label]
    elif op == FLOW_CONTROL_OP.JMP_N_IF_STACK_EQ_ZERO:
        tmp = stack.pop()
        if tmp == 0: offset = labels[label]
    elif op == FLOW_CONTROL_OP.JMP_N_IF_STACK_LT_ZERO:
        tmp = stack.pop()
        if tmp < 0: offset = labels[label]
    elif op == FLOW_CONTROL_OP.EXIT_SUBROUTINE:
        if len(runtime_stack) == 0: raise Exception("Runtime stack is empty.")
        offset = runtime_stack.pop()
    elif op == FLOW_CONTROL_OP.EXIT:
        offset = -1
    return offset


def flow_control_process(code_segment, offset, explain_code):
    ch = code_segment[offset: offset + 2]
    offset += 2
    if ch == "ss":
        label, offset = feed_label(code_segment, offset)
        explain_code.append(FLOW_CONTROL_OP.MARK_LABEL)
        explain_code.append(label)
    elif ch == "st":
        label, offset = feed_label(code_segment, offset)
        explain_code.append(FLOW_CONTROL_OP.CALL_LABEL_N)
        explain_code.append(label)
    elif ch == "sn":
        label, offset = feed_label(code_segment, offset)
        explain_code.append(FLOW_CONTROL_OP.JMP_LABEL_N)
        explain_code.append(label)
    elif ch == "ts":
        label, offset = feed_label(code_segment, offset)
        explain_code.append(FLOW_CONTROL_OP.JMP_N_IF_STACK_EQ_ZERO)
        explain_code.append(label)
    elif ch == "tt":
        label, offset = feed_label(code_segment, offset)
        explain_code.append(FLOW_CONTROL_OP.JMP_N_IF_STACK_LT_ZERO)
        explain_code.append(label)
    elif ch == "tn":
        explain_code.append(FLOW_CONTROL_OP.EXIT_SUBROUTINE)
    elif ch == "nn":
        explain_code.append(FLOW_CONTROL_OP.EXIT)
    else:
        raise Exception("UNKNOWN")
    return offset


def run(explain_code, labels, input_stream):
    output = ''
    stack = []
    heap = {}
    runtime_stack = []

    position = 0
    while position < len(explain_code):
        op = explain_code[position]
        position += 1
        if op in {STACK_OP.PUSH, STACK_OP.DUPLICATE_TOP_OF_N, STACK_OP.DISCARD_TOP_N}:
            param = explain_code[position]
            position += 1
            stack_engine(op, param, stack)
        elif op in {STACK_OP.DISCARD_TOP, STACK_OP.SWAP, STACK_OP.DUPLICATE_TOP}:
            stack_engine(op, None, stack)
        elif type(op) == HEAP_OP:
            heap_engine(op, stack, heap)
        elif type(op) == ARITHMETIC_OP:
            arithmetic_engine(op, stack)
        elif op in {IO_OP.INPUT_NUMBER, IO_OP.INPUT_CHAR}:
            io_engine(op, stack, heap, input_stream)
        elif op in {IO_OP.OUTPUT_NUMBER, IO_OP.OUTPUT_CHAR}:
            output += io_engine(op, stack, heap, input_stream)
        elif op == FLOW_CONTROL_OP.MARK_LABEL:
            pass
        elif op in {FLOW_CONTROL_OP.CALL_LABEL_N, FLOW_CONTROL_OP.JMP_LABEL_N, FLOW_CONTROL_OP.JMP_N_IF_STACK_LT_ZERO,
                    FLOW_CONTROL_OP.JMP_N_IF_STACK_EQ_ZERO}:
            label = explain_code[position]
            position += 1
            position = flow_control_engine(op, label, position, labels, stack, runtime_stack)
        elif op == FLOW_CONTROL_OP.EXIT_SUBROUTINE:
            position = flow_control_engine(op, None, position, None, None, runtime_stack)
        elif op == FLOW_CONTROL_OP.EXIT:
            return (0, output)
    return (-1, output)


# solution
def whitespace(code, inp=''):
    input_stream = warp_input_stream(inp)
    code = unbleach(code)
    print(code)

    explain_code = []
    position = 0
    while position < len(code):
        ch = code[position]
        position += 1

        if ch == 's':
            position = stack_process(code, position, explain_code)
        elif ch == 't':
            ch = code[position]
            position += 1
            if ch == 't':
                position = heap_process(code, position, explain_code)
            elif ch == 's':
                position = arithmetic_process(code, position, explain_code)
            elif ch == 'n':
                position = io_process(code, position, explain_code)
        elif ch == 'n':
            position = flow_control_process(code, position, explain_code)
            if position == -1:
                break

    print(explain_code)

    lables = {}

    for i in range(len(explain_code)):
        if explain_code[i] == FLOW_CONTROL_OP.MARK_LABEL:
            if explain_code[i + 1] in lables: raise Exception()
            lables[explain_code[i + 1]] = i + 2

    EXIT_CODE, output = run(explain_code, lables, input_stream)

    if EXIT_CODE != 0: raise Exception("Exception @ Exit")

    return output


def assert_equals(actual_value, except_value):
    if except_value != actual_value:
        raise Exception("Except : %s Actual : %s" % (except_value, actual_value))


# assert_equals(whitespace(decode("ssstnsssttnsssnssstsnsssnssstnnssntnstntsnnnn"), ''), "123")

# assert_equals(whitespace(decode("ssstntntsssstsntntssssttntntsssstssntntsssststntntsssststntttssstssntttsssttntttssstsntttssstnttttnsstnsstnsstnsstnssnnn"),[87, 111, 114, 108, 100]), "World")

#
# output1 = "   \t\n\t\n \t\n\n\n"
# output2 = "   \t \n\t\n \t\n\n\n"
# output3 = "   \t\t\n\t\n \t\n\n\n"
# output0 = "    \n\t\n \t\n\n\n"
# Test.assert_equals(whitespace(output1), "1")
# Test.assert_equals(whitespace(output2), "2")
# Test.assert_equals(whitespace(output3), "3")
# Test.assert_equals(whitespace(output0), "0")