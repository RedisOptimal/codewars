def format_duration(seconds):
    def append_str(time, unit):
        ret = []
        if time > 0:
            ret.append(time)
            if time > 1:
                ret.append(unit+'s')
            else:
                ret.append(unit)
        return ret
    if seconds <= 0:
        return 'now'
    time = seconds
    second = time % 60
    time //= 60
    minute = time % 60
    time //= 60
    hour = time % 24
    time //= 24
    day = time % 365
    time //= 365
    year = time
#    print('%d year %d day %d hour %d minute %d second' % (year, day, hour, minute, second))
    s = []
    s += append_str(year, 'year')
    s += append_str(day, 'day')
    s += append_str(hour, 'hour')
    s += append_str(minute, 'minute')
    s += append_str(second, 'second')
    L = len(s)
    pos = 0
    r = ''
    r += '%d %s' % (s[2*pos], s[2*pos+1])
    pos += 1
    while 2 * pos + 2 < L:
        r += ', %d %s' % (s[2*pos], s[2*pos+1])
        pos += 1
    if 2 * pos < L:
        r += ' and %d %s' % (s[2*pos], s[2*pos+1])
    return r