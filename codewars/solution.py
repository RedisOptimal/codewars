#!/usr/bin/env python
# -*- coding: utf-8 -*-

# N const Normal a-z0-9A-Z
# A const Any .
# W op ZeroOrMore *
# L op Left Bracket (
# R op Right Bracket )
# O op Or |
# S Start <Start>
# E End <End>
from preloaded import Any, Normal, Or, Str, ZeroOrMore


class AST:
    left = None
    right = None
    kind = None
    expr = None

    def __init__(self, kind=None, left=None, right=None, expr=None):
        self.left = left
        self.right = right
        self.kind = kind
        self.expr = expr


def build_ast(symbol):
    L = len(symbol)
    pos = 1
    op_stack = [symbol[0]]
    num_stack = []
    while pos < L:
        current = symbol[pos]
        if current[3] == 'CONST':
            num_stack.append(AST(current, expr=Normal(current[2]) if current[0] == 'N' else Any()))
        else:
            if current[0] == 'L':
                op_stack.append(current)
            elif current[0] == 'R':
                op_pop = op_stack.pop()
                while op_pop[0] != 'L':
                    ast_right = num_stack.pop()
                    ast_left = num_stack.pop()
                    ast_tmp = AST(op_pop, ast_left, ast_right)
                    num_stack.append(ast_tmp)
                    op_pop = op_stack.pop()
            elif current[1] > op_stack[-1][1]:  # priority current > stack top, push to stack
                op_stack.append(current)
            else:
                if current[0] == 'O' or current[0] == 'W':
                    if current[1] == op_stack[-1][1]:  # a|b|c, a**
                        raise Exception('a|b|c, a**')
                while current[1] <= op_stack[-1][1]:
                    op_pop = op_stack.pop()
                    ast_right = num_stack.pop()
                    ast_left = num_stack.pop()
                    ast_tmp = AST(op_pop, ast_left, ast_right)
                    num_stack.append(ast_tmp)
                op_stack.append(current)
        pos += 1
    return num_stack[0]


def parse_symbol(indata, pos, symbol):
    L = len(indata)
    while pos < L:
        if indata[pos] == '.':
            symbol[pos] = ('A', None, '.', 'CONST')
        elif indata[pos] == '*':
            symbol[pos] = ('W', 90, '*', 'OPER')
        elif indata[pos] == '|':
            symbol[pos] = ('O', 30, '|', 'OPER')
        elif indata[pos] == '(':
            symbol[pos] = ('L', 20, '(', 'OPER')
        elif indata[pos] == ')':
            symbol[pos] = ('R', 10, ')', 'OPER')
        else:
            symbol[pos] = ('N', None, indata[pos], 'CONST')
        pos += 1


def optimize_ast(ast):
    return ast
    # if ast.kind[3] == 'CONST':
    #     return ast
    # else:  # OP
    #     if ast.kind[0] == '<shadow+>':
    #         left = optimize_ast(ast.left)
    #         right = optimize_ast(ast.right)
    #         if type(left.expr) == Normal and type(right.expr) == Normal:
    #             new_ast = AST(ast.kind, expr=Str([left.expr, right.expr]))
    #             return new_ast
    #         elif type(left.expr) == Str and type(right.expr) == Normal:
    #             new_ast = AST(ast.kind, expr=Str(left.expr.args[0] + [right.expr, ]))
    #             return new_ast
    #         elif type(left.expr) == Normal and type(right.expr) == Str:
    #             new_ast = AST(ast.kind, expr=Str([left.expr, ] + right.expr.args[0]))
    #             return new_ast
    #         elif type(left.expr) == Str and type(right.expr) == Str:
    #             new_ast = AST(ast.kind, expr=Str([left.expr.args[0] + right.expr.args[0]]))
    #             return new_ast
    #         else:
    #             return ast
    #     else:
    #         return ast


def MLR_walk(ast):
    if ast.expr:
        return ast.expr
    if ast.kind[3] == 'CONST':
        if ast.kind[0] == 'N':
            return ast.expr
        elif ast.kind[0] == 'A':
            return ast.expr
        elif ast.kind[0] == '<blank>':
            return None
        else:
            raise Exception('UNKNOWN CONST')
    else:  # OP
        left = MLR_walk(ast.left)
        right = MLR_walk(ast.right)
        if ast.kind[0] == 'W':
            return ZeroOrMore(left)
        elif ast.kind[0] == 'O':
            return Or(left, right)
        elif ast.kind[0] == '<shadow+>':
            if type(left) == Str and type(right) == Str:
                return Str(left.args[0] + right.args[0])
            elif type(left) == Str:
                return Str(left.args[0] + [right, ])
            elif type(right) == Str:
                return Str([left, ] + right.args[0])
            else:
                return Str([left, right])


def parse_regexp(indata):
    if len(indata) == 0:
        return None
    symbol = [None] * len(indata)
    parse_symbol(indata, 0, symbol)
    symbol2 = [symbol[0]]
    for pos in range(1, len(symbol)):
        if (symbol2[-1][3] == 'CONST' and symbol[pos][3] == 'CONST') \
                or (symbol2[-1][0] == 'N' and symbol[pos][0] == 'L') \
                or (symbol2[-1][0] == 'R' and symbol[pos][0] == 'N'):
            symbol2.append(('<shadow+>', 40, None, 'OPER'))
            symbol2.append(symbol[pos])
        elif symbol[pos][0] == 'W':
            symbol2.append(symbol[pos])
            symbol2.append(('<blank>', None, None, 'CONST'))
        else:
            symbol2.append(symbol[pos])
    symbol = [('S', 1, 'START', 'OPER')] + symbol2 + [('E', 5, 'END', 'OPER')]
    # print(symbol)
    ast = None
    try:
        ast = build_ast(symbol)
        ast = optimize_ast(ast)
    except:
        return None
    return MLR_walk(ast)


if __name__ == '__main__':
    from preloaded import Any, Normal, Or, Str, ZeroOrMore
    from solution import parse_regexp
    import codewars_test as test


    def runtest(indata, expected):
        @test.it(f"{indata!r}")
        def _():
            user_reply = parse_regexp(indata)
            if user_reply == expected:
                test.expect(True)
            else:
                test.fail("\n".join([
                    "Input: " + repr(indata),
                    "",
                    "Expected: " + repr(expected),
                    "",
                    "But got:  " + repr(user_reply),
                ]))


    @test.describe("Regular expression parser - Sample tests")
    def _():
        @test.describe("basic tests")
        def _():
            runtest('(kz(v*zs*)*)', Str([Normal('k'), Normal('z'), ZeroOrMore(Str([ZeroOrMore(Normal('v')), Normal('z'), ZeroOrMore(Normal('s'))]))]))
            runtest('(f|(.i(v|(k|i))eqvi*e(l.ixd*gx*dqkra.yoz)u*))', Or(Normal('f'), Str([Any(), Normal('i'), Or(Normal('v'), Or(Normal('k'), Normal('i'))), Normal('e'), Normal('q'), Normal('v'), ZeroOrMore(Normal('i')), Normal('e'), Str([Normal('l'), Any(), Normal('i'), Normal('x'), ZeroOrMore(Normal('d')), Normal('g'), ZeroOrMore(Normal('x')), Normal('d'), Normal('q'), Normal('k'), Normal('r'), Normal('a'), Any(), Normal('y'), Normal('o'), Normal('z')]), ZeroOrMore(Normal('u'))])))
            runtest(".", Any())
            runtest("a", Normal("a"))
            runtest("a|b", Or(Normal("a"), Normal("b")))
            runtest("a*", ZeroOrMore(Normal("a")))
            runtest("(a)", Normal("a"))
            runtest("(a)*", ZeroOrMore(Normal("a")))
            runtest("(a|b)*", ZeroOrMore(Or(Normal("a"), Normal("b"))))
            runtest("a|b*", Or(Normal("a"), ZeroOrMore(Normal("b"))))
            runtest("abcd", Str([Normal("a"), Normal("b"), Normal("c"), Normal("d")]))
            runtest("ab|cd", Or(Str([Normal("a"), Normal("b")]), Str([Normal("c"), Normal("d")])))

        # @test.describe("precedence examples")
        def _():
            runtest("ab*", Str([Normal("a"), ZeroOrMore(Normal("b"))]))
            runtest("(ab)*", ZeroOrMore(Str([Normal("a"), Normal("b")])))
            runtest("ab|a", Or(Str([Normal("a"), Normal("b")]), Normal("a")))
            runtest("a(b|a)", Str([Normal("a"), Or(Normal("b"), Normal("a"))]))
            runtest("a|b*", Or(Normal("a"), ZeroOrMore(Normal("b"))))
            runtest("(a|b)*", ZeroOrMore(Or(Normal("a"), Normal("b"))))

        # @test.describe("the other examples")
        def _():
            runtest("a", Normal("a"))
            runtest("ab", Str([Normal("a"), Normal("b")]))
            runtest("a.*", Str([Normal("a"), ZeroOrMore(Any())]))
            runtest("(a.*)|(bb)", Or(Str([Normal("a"), ZeroOrMore(Any())]), Str([Normal("b"), Normal("b")])))

        # @test.describe("invalid examples")
        def _():
            runtest("", None)
            runtest("(", None)
            runtest("(hi!", None)
            runtest(")(", None)
            runtest("a|t|y", None)
            runtest("a**", None)

        # @test.describe("complex examples")
        def _():
            runtest("((aa)|ab)*|a",
                    Or(ZeroOrMore(Or(Str([Normal("a"), Normal("a")]), Str([Normal("a"), Normal("b")]))), Normal("a")))
            runtest("((a.)|.b)*|a",
                    Or(ZeroOrMore(Or(Str([Normal("a"), Any()]), Str([Any(), Normal("b")]))), Normal("a")))
