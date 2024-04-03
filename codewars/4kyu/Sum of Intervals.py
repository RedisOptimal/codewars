import functools

def cmp(x, y):
    return x-y


def cross(a, b):
    # A in B
    if a[0] <= b[0] <= b[1] <= a[1]:
        return True
    # B in A
    elif b[0] <= a[0] <= a[1] <= b[1]:
        return True
    # A CROSS B
    elif a[0] <= b[0] <= a[1] <= b[1]:
        return True
    # B CROSS A
    elif b[0] <= a[0] <= b[1] <= a[1]:
        return True
    else:
        return False


def merge_interval(a, b):
    # A in B
    if a[0] <= b[0] <= b[1] <= a[1]:
        return a
    # B in A
    elif b[0] <= a[0] <= a[1] <= b[1]:
        return b
    # A CROSS B
    elif a[0] <= b[0] <= a[1] <= b[1]:
        return (a[0], b[1])
    # B CROSS A
    elif b[0] <= a[0] <= b[1] <= a[1]:
        return (b[0], a[1])
    else:
        return None
    

def sum_of_intervals(intervals):
    stack = []
    intervals = sorted(intervals, key=functools.cmp_to_key(lambda x,y: cmp(x[1], y[1]) if x[0] == y[0] else cmp(x[0], y[0])))
    for interval in intervals:
        top_element = stack[-1] if len(stack) > 0 else None
        print(top_element)
        if top_element is None:
            stack.append(interval)
        else:
            print('%s %s %s' % (top_element, interval, cross(top_element, interval)))
            if cross(top_element, interval):
                top_element = merge_interval(top_element, interval)
                stack[-1] = top_element
            else:
                stack.append(interval)
    print(stack)
    return sum(map(lambda x: x[1] - x[0], stack))