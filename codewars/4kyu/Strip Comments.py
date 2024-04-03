def strip_comments(string, markers):
    lines = string.split('\n')
    ret = []
    for line in lines:
        for marker in markers:
            line = line.split(marker)[0].rstrip('\t ')
        ret.append(line)
    return '\n'.join(ret)