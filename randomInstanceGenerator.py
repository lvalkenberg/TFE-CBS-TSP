import random
import string
from lxml import etree


def generate_random_tsp_instance(instance_name, num_vertices, min_cost, max_cost):
    root = etree.Element('travellingSalesmanProblemInstance')
    name = etree.SubElement(root, 'name')
    name.text = instance_name
    source = etree.SubElement(root, 'source')
    source.text = 'Randomly generated'
    description = etree.SubElement(root, 'description')
    description.text = f'{num_vertices}-vertex randomly generated TSP instance'
    double_precision = etree.SubElement(root, 'doublePrecision')
    double_precision.text = '15'
    ignored_digits = etree.SubElement(root, 'ignoredDigits')
    ignored_digits.text = '8'

    graph = etree.SubElement(root, 'graph')

    dist_matrix = [[min_cost-1 for i in range(num_vertices)] for i in range(num_vertices)]

    for i in range(num_vertices):
        vertex = etree.SubElement(graph, 'vertex')
        for j in range(num_vertices):
            if i != j:
                if(dist_matrix[i][j] != min_cost-1):
                    edge = etree.SubElement(vertex, 'edge', cost=f'{dist_matrix[i][j]:.15e}')
                    edge.text = str(j)
                else:
                    dist_matrix[i][j] = random.uniform(min_cost, max_cost)
                    dist_matrix[j][i] = dist_matrix[i][j]
                    edge = etree.SubElement(vertex, 'edge', cost=f'{dist_matrix[i][j]:.15e}')
                    edge.text = str(j)

    return root


if __name__ == '__main__':
    #num_vertices = 14
    min_cost = 0
    max_cost = 1
    name = "random_tsp_instance"

    for num_vertices in range(20,100):
        for i in range (1):
            name = 'rd' + str(num_vertices).zfill(3) + '_' + "".join(random.choices(string.ascii_letters + string.digits, k=5))

            instance = generate_random_tsp_instance(name, num_vertices, min_cost, max_cost)
            xml_data = etree.tostring(instance, encoding='UTF-8', pretty_print=True, xml_declaration=True)

            with open('TSPlib/xml files/random/'+name+'.xml', 'wb') as f:
                f.write(xml_data)
