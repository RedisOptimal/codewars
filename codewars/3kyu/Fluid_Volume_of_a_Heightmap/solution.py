from queue import PriorityQueue

# Test helper functions
def pretty_print(heightmap):
    size = max(len(str(v)) for r in heightmap for v in r)
    return '\n'.join(' '.join(f'{v: >{size}}' for v in r) for r in heightmap)


def check_range(coordinate, height):
    x_axis = len(height)
    y_axis = len(height[0])
    return 0 <= coordinate[0] < x_axis and 0 <= coordinate[1] < y_axis


def flood(coordinate, gauging_line: int, height, heightmap, wall_mark, wall_mark_map):
    delta_coordinate = [(-1, 0), (1, 0), (0, -1), (0, 1)]
    bfs_queue = [coordinate]
    while len(bfs_queue) > 0:
        curr_coordinate = bfs_queue.pop(0)
        for delta in delta_coordinate:
            n_coordinate = (curr_coordinate[0] + delta[0], curr_coordinate[1] + delta[1])
            if not check_range(n_coordinate, height):
                continue
            if height[n_coordinate[0]][n_coordinate[1]] == 'O':
                if heightmap[n_coordinate[0]][n_coordinate[1]] <= gauging_line:
                    height[n_coordinate[0]][n_coordinate[1]] = gauging_line
                    bfs_queue.append(n_coordinate)
                else:
                    height[n_coordinate[0]][n_coordinate[1]] = 'W'
                    if not wall_mark_map[n_coordinate[0]][n_coordinate[1]]:
                        wall_mark_map[n_coordinate[0]][n_coordinate[1]] = True
                        wall_mark.append(n_coordinate)


def volume(heightmap):
    # print(pretty_print(heightmap))
    from time import time
    start_time = time()
    x_axis = len(heightmap)
    y_axis = len(heightmap[0])
    height = [(['O'] * y_axis) for _ in range(x_axis)]
    wall_mark_map = [[False] * y_axis for _ in range(x_axis)]
    wall_mark = []
    for x in range(x_axis):
        height[x][0] = 'W'
        height[x][y_axis - 1] = 'W'
        wall_mark.append((x, 0))
        wall_mark.append((x, y_axis - 1))
        wall_mark_map[x][0] = wall_mark_map[x][y_axis - 1] = True
    for y in range(1, y_axis - 1):
        height[0][y] = 'W'
        height[x_axis - 1][y] = 'W'
        wall_mark.append((0, y))
        wall_mark.append((x_axis - 1, y))
        wall_mark_map[0][y] = wall_mark_map[x_axis - 1][y] = True

    epoch = 0
    flood_seed = PriorityQueue()
    while True:
        # if epoch % 1 == 0:
        #     print('EPOCH = %d time = %d' % (epoch, time() - start_time))
        epoch += 1

        while len(wall_mark) > 0:
            coordinate = wall_mark.pop(0)
            height[coordinate[0]][coordinate[1]] = heightmap[coordinate[0]][coordinate[1]]
            flood_seed.put((heightmap[coordinate[0]][coordinate[1]], coordinate))
            # flood_seed.append(coordinate + (heightmap[coordinate[0]][coordinate[1]], ))
        # for x in range(x_axis):
        #     for y in range(y_axis):
        #         if height[x][y] == 'W':
        #             height[x][y] = heightmap[x][y]
        #             flood_seed.append((x, y, heightmap[x][y]))
        #         print(pretty_print(height))
        flood_point = flood_seed.get()
        # flood_seed = sorted(flood_seed, key=lambda x: x[2])
        # print(flood_seed)
        # flood_point = flood_seed.pop(0)
        # print(flood_point)
        flood(flood_point[1], flood_point[0], height, heightmap, wall_mark, wall_mark_map)
        # print(pretty_print(height))
        if flood_seed.empty():
            break

    total = 0
    for x in range(x_axis):
        total += sum(map(lambda tmp: tmp[0] - tmp[1], zip(height[x], heightmap[x])))
    return total