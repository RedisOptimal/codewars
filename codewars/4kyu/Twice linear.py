from queue import PriorityQueue

def dbl_linear(n):
    if n == 0:
        return 1
    q = PriorityQueue()
    q.put(1)
    s = set()
    for _ in range(n):
        t = q.get()
        if 2*t+1 not in s:
            s.add(2*t+1)
            q.put(2*t+1)
        if 3*t+1 not in s:
            s.add(3*t+1)
            q.put(3*t+1)
    return q.get()