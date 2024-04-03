import itertools


def find_pos(arr, pos):
    n = 0
    p = 0
    while n != pos:
        while not str.isnumeric(arr[p]):
            p += 1
        p += 1
        n += 1

    while not str.isnumeric(arr[p]):
        p += 1
    return p


def find_left(arr, pos):
    pos -= 1
    while arr[pos] is None:
        pos -= 1
    return pos


def find_right(arr, pos):
    while arr[pos] is None:
        pos += 1
    return pos


op_switch = {
    '+': lambda x, y: x + y,
    '-': lambda x, y: x - y,
    '*': lambda x, y: x * y,
    '/': lambda x, y: x / y
}


def merge(arr, pos, op):
    op_pos1 = find_left(arr, pos)
    op_pos2 = find_right(arr, pos)
    op_num1 = arr[op_pos1]
    op_num2 = arr[op_pos2]
    arr[op_pos2] = None
    arr[op_pos1] = op_switch[op](op_num1, op_num2)
    return arr


def merge_expr(arr, pos, op):
    op_pos1 = find_left(arr, pos)
    op_pos2 = find_right(arr, pos)
    op_num1 = arr[op_pos1]
    op_num2 = arr[op_pos2]
    arr[op_pos2] = None
    arr[op_pos1] = "(%s %s %s)" % (op_num1, op, op_num2)
    return arr


def dfs(arr):
    for pos in itertools.permutations([1, 2, 3]):
        for i0 in ['+', '-', '*', '/']:
            for i1 in ['+', '-', '*', '/']:
                for i2 in ['+', '-', '*', '/']:
                    calc = arr[:]
                    try:
                        calc = merge(merge(merge(calc, pos[0], i0), pos[1], i1), pos[2], i2)
                    except:
                        continue
                    if calc[0] == 24:
                        expr = [str(x) for x in arr]
                        ans = merge_expr(merge_expr(merge_expr(expr, pos[0], i0), pos[1], i1), pos[2], i2)
                        return ans[0], True
    return [], False


def equal_to_24(a, b, c, d):
    arrs = []
    permu = itertools.permutations([a, b, c, d])
    for arr in permu:
        arrs.append(list(arr))

    for i in range(len(arrs)):
        for j in range(i):
            if arrs[i] == arrs[j]:
                arrs[j] = None

    new_arrs = [x for x in arrs if x is not None]
    for arr in new_arrs:
        answer, is_ok = dfs(arr)
        if is_ok:
            return answer

    return "It's not possible!"