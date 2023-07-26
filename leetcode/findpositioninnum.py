# -*- coding: utf-8 -*-
# Author : Zhe Yuan
# Date :
import sys

def length_smaller_than_n(n: int):
    # 543 = 1~9 + 10~99 + 100~543
    # 543 = (10^1-10^0)*1 + (10^2-10^1)*2 + (n-10^l)*l
    # l = math.floor(log10(n))
    if n <= 1:
        return 0
    import math
    L = math.ceil(math.log10(n))
    length = 0
    for bit in range(1, L):
        length += (10 ** bit - 10 ** (bit - 1)) * bit
    length += (n - 10 ** (L - 1)) * L
    return length

def find_position(string):
    L = len(string)
    position = sys.maxsize
    # print(position)
    n = int(string)
    for bit in range(1, L+1):
        for missing_bit in range(bit):
            num_arr = []
            first_num = bit - missing_bit
            num_arr.append(string[:first_num])
            while first_num + bit < L:
                num_arr.append(string[first_num:first_num+bit])
                first_num += bit
            num_arr.append(string[first_num:])

            print('bit = %d miss = %d' % (bit, missing_bit))
            print(num_arr)
            # fix missing bit
            # fix first number
            first_num = num_arr[0]
            second_num = num_arr[1]
            first_num_expect = int(first_num) + 1
            s = ''
            for i in range(missing_bit):
                s = s + second_num[i] if i < len(second_num) else '0'
            first_num = s + first_num
            num_arr[0] = first_num
            # fix last number
            first_num = num_arr[-2]
            second_num = num_arr[-1]
            second_num_expect = int(first_num) + 1
            if second_num != str(second_num_expect)[:len(second_num)]:
                print('SUCC NOT MATCH BIT = %d MISSING BIT = %d FIRST NUM = %s SECOND NUM = %s' % (bit, missing_bit, first_num, second_num))
                continue
            num_arr[-1] = str(second_num_expect)
            print('FIXED num_arr = %s' % num_arr)
            # check num
            first_num = int(num_arr[0])
            for i in range(len(num_arr)):
                if int(num_arr[i]) != first_num + i:
                    print('Sequence check failed. %s' % num_arr)
                    continue
            n_position = length_smaller_than_n(first_num) + missing_bit
            print('n_position = %d position = %d' % (n_position, position))
            position = n_position if n_position < position else position
    return position


import codewars_test as test
if __name__ == '__main__':
    test.describe("Example tests")
    test.it("Should pass fixed tests")
    test.assert_equals(find_position("456"), 3, "...3456...")
    test.assert_equals(find_position("454"), 79, "...444546...")
    test.assert_equals(find_position("455"), 98, "...545556...")
    test.assert_equals(find_position("910"), 8, "...7891011...")
    test.assert_equals(find_position("9100"), 188, "...9899100...")
    test.assert_equals(find_position("99100"), 187, "...9899100...")
    test.assert_equals(find_position("00101"), 190, "...9899100...")
    test.assert_equals(find_position("001"), 190, "...9899100...")
    test.assert_equals(find_position("00"), 190, "...9899100...")
    test.assert_equals(find_position("123456789"), 0)
    test.assert_equals(find_position("1234567891"), 0)
    test.assert_equals(find_position("123456798"), 1000000071)
    test.assert_equals(find_position("10"), 9)
    test.assert_equals(find_position("53635"), 13034)
    test.assert_equals(find_position("040"), 1091)
    test.assert_equals(find_position("11"), 11)
    test.assert_equals(find_position("99"), 168)
    test.assert_equals(find_position("667"), 122)
    test.assert_equals(find_position("0404"), 15050)
    test.assert_equals(find_position("949225100"), 382689688)
    test.assert_equals(find_position("58257860625"), 24674951477)
    test.assert_equals(find_position("3999589058124"), 6957586376885)
    test.assert_equals(find_position("555899959741198"), 1686722738828503)
    test.assert_equals(find_position("01"), 10)
    test.assert_equals(find_position("091"), 170)
    test.assert_equals(find_position("0910"), 2927)
    test.assert_equals(find_position("0991"), 2617)
    test.assert_equals(find_position("09910"), 2617)
    test.assert_equals(find_position("09991"), 35286)
    print("<COMPLETEDIN::>")
    print("<COMPLETEDIN::>")