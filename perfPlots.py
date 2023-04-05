import csv
import matplotlib.pyplot as plt

perfs = [0, 1133, 634, 4, 15, 9, 14, 58, 91, 12942, 148, 33, 1578, 245, 1183, 834, 759]
enhenced = [2, 40, 13, 4, 397, 16, 12, 16, 64, 922, 68, 826, 2669, 385, 16, 1202, 1209, 2220, 7628, 2882, 2183, 5236]

def read_data_from_csv(file_name):
    data_dict = {}

    with open(file_name, 'r', newline='') as csvfile:
        reader = csv.reader(csvfile)

        # Skip the header
        next(reader)

        # Read the data
        for row in reader:
            solver, value = row
            if solver not in data_dict:
                data_dict[solver] = []

            data_dict[solver].append(int(value))

    return data_dict

def create_cactus_plot(data_dict, markers):
    for (label, data), i in zip(data_dict.items(), range(len(data_dict.keys()))):
        #if(label not in ["margCost + repCost", "stackoverflow", "force branching", "incremental Lagrangian", "incremental Lagrangian + LCS"]): continue
        data.sort()
        y_values = range(1, len(data) + 1)
        plt.plot(data, y_values, marker=markers[i],linestyle="--", linewidth=0.5, label=label)

    plt.xlabel('Time (ms)')
    plt.ylabel('Number of instances solved')
    plt.title('Performance comparison')

    #plt.xscale('log')
    plt.legend()

    plt.savefig("figs/timeout60limited.pdf")
    plt.show()

# Example usage:
create_cactus_plot(read_data_from_csv("timeout60complete.csv"),['o', 's', '^', 'v', 'D', 'P', '*', 'X', 'H', '8'])