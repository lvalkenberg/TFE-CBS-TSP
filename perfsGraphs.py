import os
import sys
import signal
import subprocess
import psutil
import csv
from threading import Timer

def add_data_to_csv(data, label, file_name):
    file_exists = os.path.exists(file_name)

    with open(file_name, 'a', newline='') as csvfile:
        writer = csv.writer(csvfile)

        # If the file does not exist, write the header
        if not file_exists:
            writer.writerow(['Label', 'Data'])

        # Write the data
        for value in data:
            writer.writerow([label, value])

def kill_process_tree(pid, including_parent=True):
    parent = psutil.Process(pid)
    children = parent.children(recursive=True)

    for child in children:
        child.kill()

    if including_parent:
        parent.kill()


def terminate_process_tree_unix(pid):
    os.killpg(os.getpgid(pid), signal.SIGTERM)


def run_java_program(instance, solver, time_limit=60):
    command = ["java", "-jar", 'Intellij TSP/out/artifacts/Solver_jar/Solver.jar', solver]
    command.append("TSPlib/xml files/" + instance + ".xml")

    if sys.platform == "linux" or sys.platform == "linux2":
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, preexec_fn=os.setsid)
    else:
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    if sys.platform == "linux" or sys.platform == "linux2":
        timer = Timer(time_limit, lambda: terminate_process_tree_unix(process.pid))
    else:
        timer = Timer(time_limit, lambda: kill_process_tree(process.pid))

    timer.start()

    try:
        stdout, stderr = process.communicate()
        exit_code = process.wait()
    finally:
        timer.cancel()

    if exit_code == 0:
        try:
            time_taken = int(stdout.decode("utf-8").strip())
        except:
            print("The output is not a digit :")
            print(stdout.decode("utf-8"))
            return

        print(f"{solver}-{instance}: {time_taken} ms")
        time_taken_dict[solver].append(time_taken)
        #time_taken[solver] += time_taken
    else:
        print(f"{solver}-{instance} timed out or encountered an error")
        #print("Error message:")
        #print(stderr.decode("ISO 8859-1"))


instances = ["burma14", "ulysses16", "gr17", "gr21", "ulysses22", "gr24", "fri26", "bayg29", "bays29",
            "dantzig42", "swiss42", "att48", "gr48", "hk48", "eil51", "berlin52", "brazil58", "st70",
            "eil76", "gr96", "rat99","kroA100","kroB100", "kroC100", "kroD100", "kroE100", "rd100",
            "eil101", "lin105", "pr107", "gr120", "pr124", "pr136", "pr144", "pr152", "pr152", "u159",
            "si175", "pr226", "pr264", "a280", "pr299"]

instances = ["burma14", "ulysses16", "gr17", "gr21", "ulysses22", "gr24", "fri26", "bayg29", "bays29",
            "dantzig42", "swiss42", "att48", "gr48", "hk48", "eil51", "berlin52", "brazil58", "st70",
            "eil76", "gr96", "rat99","kroA100","kroB100", "kroC100", "kroD100", "kroE100", "rd100",
            "eil101", "lin105", "pr107"]

solvers = ["stackoverflow", "no filtering", "repCost", "margCost", "margCost + repCost","force branching", "force DFS","LCS"]
solvers = ["stackoverflow", "no filtering","margCost + repCost","force branching", "incremental Lagrangian"]
solvers = ["incremental Lagrangian + LCS"]


time_taken_dict = {}

def list_instances_in_directory(directory):
    return [file.split('.')[0] for file in os.listdir(directory) if os.path.isfile(os.path.join(directory, file))]

directory_path = "TSPlib/xml files/random"
instances = [ "random/" + i for i in  list_instances_in_directory(directory_path)]
#print(instances)

for solver in solvers:
    time_taken_dict[solver] = []

for instance in instances:
    for solver in solvers:
        run_java_program(instance, solver, time_limit=60)

for solver in solvers:
    add_data_to_csv(time_taken_dict[solver], solver, "random60complete.csv")

print("Time taken list:", time_taken_dict)
