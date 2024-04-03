#!/usr/bin/env python
# -*- coding: utf-8 -*-
from abc import abstractmethod
from enum import Enum


class SYMBOL(Enum):
    UNKNOWN = (0, None, None)
    PLUS = (1, True, '+')
    MINUS = (2, True, '-')
    MUL = (3, True, '*')
    DIV = (4, True, '/')
    POW = (5, True, '^')
    SIN = (6, False, 'sin')
    COS = (7, False, 'cos')
    TAN = (8, False, 'tan')
    EXP = (9, False, 'exp')
    LN = (10, False, 'ln')
    VARIABLE = (11, None, 'x')
    CONSTANT = (12, None, None)
    LEFT_BRACKET = (13, None, '(')
    RIGHT_BRACKET = (14, None, ')')

    def __str__(self):
        return self.value[2]


class ast:
    @abstractmethod
    def op(self):
        pass

    @abstractmethod
    def has_variable(self):
        pass


class bin_op(ast):
    def __init__(self, op, a, b):
        super().__init__()
        self._op = op
        self._a = a
        self._b = b
        self._has_variable = a.has_variable() or b.has_variable()

    def op(self):
        return self._op

    def has_variable(self):
        return self._has_variable

    def a(self):
        return self._a

    def b(self):
        return self._b

    def __str__(self):
        return "(%s %s %s)" % (self.op(), self.a(), self.b())


class un_op(ast):
    def __init__(self, op, n):
        super().__init__()
        self._op = op
        self._n = n
        self._has_variable = n.has_variable()

    def has_variable(self):
        return self._has_variable

    def op(self):
        return self._op

    def n(self):
        return self._n

    def __str__(self):
        return "(%s %s)" % (self.op(), self.n())


class immi_op(ast):
    def __init__(self, number=None, is_variable=False):
        super().__init__()
        self._number = number
        self._is_variable = is_variable

    def has_variable(self):
        return self._is_variable

    def op(self):
        return None

    def number(self):
        return self._number

    def __str__(self):
        return 'x' if self.has_variable() else str(self.number())


magic_word = {'x': SYMBOL.VARIABLE, '(': SYMBOL.LEFT_BRACKET,
              ')': SYMBOL.RIGHT_BRACKET, '+': SYMBOL.PLUS, '-': SYMBOL.MINUS, '*': SYMBOL.MUL, '/': SYMBOL.DIV,
              '^': SYMBOL.POW, 'cos': SYMBOL.COS, 'sin': SYMBOL.SIN, 'tan': SYMBOL.TAN, 'exp': SYMBOL.EXP,
              'ln': SYMBOL.LN}


def feed_symbol(expr, offset):
    origin_offset = offset

    while offset < len(expr) and expr[offset] != ' ':
        offset += 1
        if offset < len(expr) and expr[offset] == ')': break
        if expr[origin_offset:offset] in magic_word: break

    symbol = (magic_word.get(expr[origin_offset: offset]), None) if expr[origin_offset: offset] in magic_word else (
        SYMBOL.CONSTANT, int(expr[origin_offset: offset])) if expr[origin_offset: offset].isnumeric() else (
        SYMBOL.UNKNOWN, None)
    return symbol, offset + 1 if offset < len(expr) and expr[offset] == ' ' else offset


def parse_symbol(expr):
    symbols = []
    position = 0
    while position < len(expr):
        symbol, position = feed_symbol(expr, position)
        symbols.append(symbol)

    return symbols


def build_ast(symbols, offset):
    symbol, number = symbols[offset]
    if symbol == SYMBOL.LEFT_BRACKET:
        current_node, offset = build_ast(symbols, offset + 1)
        return current_node, offset + 1  # process RIGHT_BRACKET
    elif symbol == SYMBOL.VARIABLE:
        return immi_op(is_variable=True), offset + 1
    elif symbol == SYMBOL.CONSTANT:
        return immi_op(number), offset + 1
    else:
        if symbol.value[1] is None:
            raise Exception(symbol)
        elif symbol.value[1]:  # bin_op
            sub_node_a, offset = build_ast(symbols, offset + 1)
            sub_node_b, offset = build_ast(symbols, offset)
            current_node = bin_op(symbol, sub_node_a, sub_node_b)
            return current_node, offset
        else:  # un_op
            sub_node, offset = build_ast(symbols, offset + 1)
            current_node = un_op(symbol, sub_node)
            return current_node, offset


def derivatives_ast(root):
    current_node = None
    if root.has_variable():
        if type(root) is bin_op:
            if root.op() in {SYMBOL.PLUS, SYMBOL.MINUS, SYMBOL.MUL, SYMBOL.DIV}:
                det_a = derivatives_ast(root.a()) if root.a().has_variable() else immi_op(0)
                det_b = derivatives_ast(root.b()) if root.b().has_variable() else immi_op(0)
            if root.op() in {SYMBOL.PLUS, SYMBOL.MINUS}:
                current_node = bin_op(root.op(), det_a, det_b)
            elif root.op() is SYMBOL.MUL:
                a = bin_op(SYMBOL.MUL, det_a, root.b())
                b = bin_op(SYMBOL.MUL, root.a(), det_b)
                current_node = bin_op(SYMBOL.PLUS, a, b)
            elif root.op() is SYMBOL.DIV:
                a = bin_op(SYMBOL.MUL, det_a, root.b())
                b = bin_op(SYMBOL.MUL, root.a(), det_b)
                numerator = bin_op(SYMBOL.MINUS, a, b)
                denominator = bin_op(SYMBOL.POW, root.b(), immi_op(2))
                current_node = bin_op(SYMBOL.DIV, numerator, denominator)
            elif root.op() is SYMBOL.POW:
                a = bin_op(SYMBOL.MUL, root.b(), bin_op(SYMBOL.POW, root.a(), immi_op(root.b().number() - 1)))
                current_node = bin_op(SYMBOL.MUL, a, derivatives_ast(root.a()))
        elif type(root) is un_op:
            if root.op() is SYMBOL.SIN:
                current_node = bin_op(SYMBOL.MUL, derivatives_ast(root.n()), un_op(SYMBOL.COS, root.n()))
            elif root.op() is SYMBOL.COS:
                current_node = bin_op(SYMBOL.MUL, derivatives_ast(root.n()),
                                      bin_op(SYMBOL.MUL, immi_op(-1), un_op(SYMBOL.SIN, root.n())))
            elif root.op() is SYMBOL.TAN:
                current_node = bin_op(SYMBOL.MUL, derivatives_ast(root.n()),
                                      bin_op(SYMBOL.POW, un_op(SYMBOL.COS, root.n()), immi_op(-2)))
            elif root.op() is SYMBOL.EXP:
                current_node = bin_op(SYMBOL.MUL, derivatives_ast(root.n()), root)
            elif root.op() is SYMBOL.LN:
                current_node = bin_op(SYMBOL.MUL, derivatives_ast(root.n()), bin_op(SYMBOL.DIV, immi_op(1), root.n()))
        elif type(root) is immi_op:
            return immi_op(1)
    else:
        return immi_op(0)
    return current_node


def optimal_ast(root):
    current_node = root
    if type(root) is bin_op:
        optimal_a = optimal_ast(root.a())
        optimal_b = optimal_ast(root.b())

        if not optimal_a.has_variable() and not optimal_b.has_variable():
            if root.op() in {SYMBOL.PLUS, SYMBOL.MINUS, SYMBOL.MUL, SYMBOL.DIV, SYMBOL.POW}:
                r_map = {SYMBOL.PLUS: lambda x, y: x + y, SYMBOL.MINUS: lambda x, y: x - y,
                         SYMBOL.MUL: lambda x, y: x * y, SYMBOL.DIV: lambda x, y: x / y,
                         SYMBOL.POW: lambda x, y: x ** y}
                return immi_op(r_map[root.op()](optimal_a.number(), optimal_b.number()))

        if root.op() is SYMBOL.MUL:
            if type(optimal_a) is immi_op and optimal_a.number() == 0:
                return immi_op(0)
            if type(optimal_a) is immi_op and optimal_a.number() == 1:
                return optimal_b
            if type(optimal_b) is immi_op and optimal_b.number() == 0:
                return immi_op(0)
            if type(optimal_b) is immi_op and optimal_b.number() == 1:
                return optimal_a
        elif root.op() is SYMBOL.DIV:
            if type(optimal_a) is immi_op and optimal_a.number() == 0:
                return immi_op(0)
        elif root.op() is SYMBOL.POW:
            if type(optimal_a) is immi_op and optimal_a.number() == 0:
                return immi_op(0)
            if type(optimal_b) is immi_op and optimal_b.number() == 0:
                return immi_op(1)
            if type(optimal_b) is immi_op and optimal_b.number() == 1:
                return optimal_a
        elif root.op() is SYMBOL.PLUS:
            if type(optimal_a) is immi_op and optimal_a.number() == 0:
                return optimal_b
            if type(optimal_b) is immi_op and optimal_b.number() == 0:
                return optimal_a
        elif root.op() is SYMBOL.MINUS:
            pass
        current_node = bin_op(root.op(), optimal_a, optimal_b)
    elif type(root) is un_op:
        pass
    return current_node


def diff(expr):
    symbols = parse_symbol(expr)
    # print(expr + ' ' + str(symbols))
    root, _ = build_ast(symbols, 0)
    root = derivatives_ast(root)
    root = optimal_ast(root)
    return str(root)