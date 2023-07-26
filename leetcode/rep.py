#!/bin/usr/env python3
# -*- coding: utf-8 -*-
# Author : Zhe Yuan
# Date: 2023/7/25
# Description:

if __name__ == '__main__':
    from codewars.preloaded import Any, Normal, Or, Str, ZeroOrMore
    from codewars.solution import parse_regexp
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

        @test.describe("precedence examples")
        def _():
            runtest("ab*", Str([Normal("a"), ZeroOrMore(Normal("b"))]))
            runtest("(ab)*", ZeroOrMore(Str([Normal("a"), Normal("b")])))
            runtest("ab|a", Or(Str([Normal("a"), Normal("b")]), Normal("a")))
            runtest("a(b|a)", Str([Normal("a"), Or(Normal("b"), Normal("a"))]))
            runtest("a|b*", Or(Normal("a"), ZeroOrMore(Normal("b"))))
            runtest("(a|b)*", ZeroOrMore(Or(Normal("a"), Normal("b"))))

        @test.describe("the other examples")
        def _():
            runtest("a", Normal("a"))
            runtest("ab", Str([Normal("a"), Normal("b")]))
            runtest("a.*", Str([Normal("a"), ZeroOrMore(Any())]))
            runtest("(a.*)|(bb)", Or(Str([Normal("a"), ZeroOrMore(Any())]), Str([Normal("b"), Normal("b")])))

        @test.describe("invalid examples")
        def _():
            runtest("", None)
            runtest("(", None)
            runtest("(hi!", None)
            runtest(")(", None)
            runtest("a|t|y", None)
            runtest("a**", None)

        @test.describe("complex examples")
        def _():
            runtest("((aa)|ab)*|a",
                    Or(ZeroOrMore(Or(Str([Normal("a"), Normal("a")]), Str([Normal("a"), Normal("b")]))), Normal("a")))
            runtest("((a.)|.b)*|a",
                    Or(ZeroOrMore(Or(Str([Normal("a"), Any()]), Str([Any(), Normal("b")]))), Normal("a")))