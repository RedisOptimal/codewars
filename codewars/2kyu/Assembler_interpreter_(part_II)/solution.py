import re
msg_re = re.compile('\'[^\']*\',|[a-z],')
two_param_operator = set(['mov', 'div', 'mul', 'add', 'sub', 'cmp'])
one_param_operator = set(['inc', 'dec', 'call', 'msg', 'jmp', 'jne', 'je', 'jge', 'jg', 'jle', 'jl'])


def filter_comment(line):
    return line[:line.find(';')].strip() if line.find(';') > -1 else line


def process_instruct(line_number, line, instruct):
    if len(line) == 0:
        return True
    line = filter_comment(line)
    if len(line) == 0:
        return False
    if line == 'end' or line == 'ret':
        instruct.append((line_number, line))
        return True

    operator = line.split(' ')[0]
    params = line[len(operator) + 1:].strip()

    if operator in two_param_operator:
        param_a, param_b = params.split(',')
        instruct.append((line_number, operator, param_a.strip(), param_b.strip()))
    elif operator in one_param_operator:
        param_a = params
        instruct.append((line_number, operator, param_a))
    else:
        raise ValueError("Unsupport : " + str(line_number) + "\t" + line)
    return False


def process_main(program, ip, instruct):
    while True:
        line = program[ip]
        ip += 1
        if process_instruct(ip - 1, line, instruct):
            return ip


def read_function_name(program, ip):
    while ip < len(program):
        line = program[ip].strip()
        line = filter_comment(line)
        ip += 1
        if line.endswith(':'):
            return line[:-1], ip
    return None, ip


def process_function(program, ip, functions, instruct):
    function_name, ip = read_function_name(program, ip)
    if function_name is None:
        return ip
    functions[function_name] = ip

    while True:
        line = program[ip].strip()
        ip += 1
        if process_instruct(ip - 1, line, instruct):
            break

    return ip


def run(main_instruct, functions, ip, regs):
    ret = ""
    cmp_reg = None
    while True:
        if ip == len(main_instruct):  # missing end
            raise ValueError("missing end")
        instruct = main_instruct[ip]
        ip += 1
        operator = instruct[1]
        if operator in two_param_operator:
            param_a, param_b = instruct[2], instruct[3]
        elif operator in one_param_operator:
            param_a = instruct[2]

        if operator == 'end' or operator == 'ret':  # end or ret
            break
        elif operator == 'mov':  # mov
            if param_a.isdigit() and param_b.isdigit():
                raise ValueError("")
            elif param_a.isdigit() and not param_b.isdigit():
                raise ValueError("")
            elif not param_a.isdigit() and param_b.isdigit():
                regs[param_a] = int(param_b)
            else:
                regs[param_a] = regs[param_b]
        elif operator == 'inc':  # inc
            if param_a.isdigit():
                raise ValueError("")
            else:
                regs[param_a] += 1
        elif operator == 'dec':  # dec
            if param_a.isdigit():
                raise ValueError("")
            else:
                regs[param_a] -= 1
        elif operator == 'call':  # call
            next_ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
            _, tmp_ret = run(main_instruct, functions, next_ip, regs)
            ret += tmp_ret
        elif operator == 'msg':  # msg
            params = msg_re.findall(param_a + ",")
            params = map(lambda x: x.strip()[:-1], params)
            for param in params:
                if param.startswith('\''):
                    ret += param[1:-1]
                else:
                    ret += str(int(regs[param]))
        elif operator == 'div':  # div
            if param_a.isdigit() and param_b.isdigit():
                raise ValueError("")
            elif param_a.isdigit() and not param_b.isdigit():
                raise ValueError("")
            elif not param_a.isdigit() and param_b.isdigit():
                regs[param_a] //= int(param_b)
            else:
                regs[param_a] //= regs[param_b]
        elif operator == 'mul':  # mul
            if param_a.isdigit() and param_b.isdigit():
                raise ValueError("")
            elif param_a.isdigit() and not param_b.isdigit():
                raise ValueError("")
            elif not param_a.isdigit() and param_b.isdigit():
                regs[param_a] *= int(param_b)
            else:
                regs[param_a] *= regs[param_b]
        elif operator == 'add':  # add
            if param_a.isdigit() and param_b.isdigit():
                raise ValueError("")
            elif param_a.isdigit() and not param_b.isdigit():
                raise ValueError("")
            elif not param_a.isdigit() and param_b.isdigit():
                regs[param_a] += int(param_b)
            else:
                regs[param_a] += regs[param_b]
        elif operator == 'sub':  # sub
            if param_a.isdigit() and param_b.isdigit():
                raise ValueError("")
            elif param_a.isdigit() and not param_b.isdigit():
                raise ValueError("")
            elif not param_a.isdigit() and param_b.isdigit():
                regs[param_a] -= int(param_b)
            else:
                regs[param_a] -= regs[param_b]
        elif operator == 'cmp':  # cmp
            if param_a.isdigit() and param_b.isdigit():
                cmp_reg = (int(param_a), int(param_b))
            elif param_a.isdigit() and not param_b.isdigit():
                cmp_reg = (int(param_a), regs[param_b])
            elif not param_a.isdigit() and param_b.isdigit():
                cmp_reg = (regs[param_a], int(param_b))
            else:
                cmp_reg = (regs[param_a], regs[param_b])
        elif operator == 'jmp':  # jmp
            ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'jne':  # jne
            if cmp_reg[0] != cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'jl':  # jl
            if cmp_reg[0] < cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'jle':  # jle
            if cmp_reg[0] <= cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'jge':  # jge
            if cmp_reg[0] >= cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'jg':  # jg
            if cmp_reg[0] > cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        elif operator == 'je':  # je
            if cmp_reg[0] == cmp_reg[1]:
                ip = [i for i, p in enumerate(main_instruct) if functions[param_a] == p[0]][0]
        else:
            raise ValueError("Unsupport : " + str(instruct))
    return ip, ret


def assembler_interpreter(program):
    # ret = "(5+1)/2 = 3"
    program = program.split('\n')
    main_instruct = []
    functions = dict()

    ip = 1
    ip = process_main(program, ip, main_instruct)

    while ip < len(program):
        ip = process_function(program, ip, functions, main_instruct)

    # print(main_instruct, functions)
    # print(functions)
    try:
        _, tmp_ret = run(main_instruct, functions, 0, {chr(ord('a') + i): 0 for i in range(26)})
        return tmp_ret  # output
    except ValueError:
        return -1