def solution(args):
    if len(args) == 0:
        return ''
    ret = []
    L = len(args)
    pos = 0
    while pos < L:
        next_pos = pos + 1
        while next_pos < L and args[next_pos - 1] + 1 == args[next_pos]:
#             print('%d %d %d %d' % (pos, args[pos], next_pos, args[next_pos]))
            next_pos += 1
        if next_pos - pos > 2:
            ret.append('%d-%d' % (args[pos], args[next_pos-1]))
            pos = next_pos
        else:
            ret.append(str(args[pos]))
            pos += 1
    return ','.join(ret)