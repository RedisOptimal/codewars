# -*- coding: utf-8 -*-
# Author : Zhe Yuan
# Date :

def strip_comments(string, markers):
    lines = string.split('\n')
    ret = []
    for line in lines:
        for marker in markers:
            line = line.split(marker)[0].strip('\t ')
        ret.append(line)
    return '\n'.join(ret)

if __name__ == '__main__':
    import codewars_test as test
    test.assert_equals(strip_comments('apples, pears # and bananas\ngrapes\nbananas !apples', ['#', '!']),
                       'apples, pears\ngrapes\nbananas')
    test.assert_equals(strip_comments('a #b\nc\nd $e f g', ['#', '$']), 'a\nc\nd')
    test.assert_equals(strip_comments(' a #b\nc\nd $e f g', ['#', '$']), ' a\nc\nd')
    test.assert_equals(strip_comments('\t# avocados avocados\nbananas ! apples @\n= .', ['#', ',', '-', "'", '.']), '\nbananas ! apples @\n=')