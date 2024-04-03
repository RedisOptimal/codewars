def snail(snail_map):
    if len(snail_map[0]) == 0:
           return []
    y_axis = len(snail_map[0])
    x_axis = len(snail_map)
    coordinate = (0, 0)
    director = 0
    delta = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    visit = [[False] * y_axis for _ in range(x_axis)]
    sum = 1
    def check_range(coordinate, x_axis, y_axis):
        return 0 <= coordinate[0] < x_axis and 0 <= coordinate[1] < y_axis
    ret = [snail_map[0][0]]
    visit[0][0] = True
    while sum < y_axis * x_axis:
        n_coordinate = (coordinate[0] + delta[director][0], coordinate[1] + delta[director][1])
        while True:
            n_coordinate = (coordinate[0] + delta[director][0], coordinate[1] + delta[director][1])
            if not check_range(n_coordinate, x_axis, y_axis) or visit[n_coordinate[0]][n_coordinate[1]]:
                break
            sum += 1
            ret.append(snail_map[n_coordinate[0]][n_coordinate[1]])
            visit[n_coordinate[0]][n_coordinate[1]] = True
            coordinate = n_coordinate
        director = (director + 1) % 4
    return ret