class Path:
    def __init__(self, lst):
        self.edges = []
        for i in range(len(lst)-1):
            self.edges += [(lst[i],lst[i+1])]

    def __contains__(self, edge):
        return (edge in self.edges) or ((edge[1], edge[0]) in self.edges)
    def __iter__(self):
        return self.edges.__iter__()
    def __repr__(self):
        return "Path:" + str(self.edges)
    def __len__(self):
        return len(self.edges)
    def getNodes(self):
        nodes = []
        for i in range(len(self.edges)):
            if i == 0:
                nodes.append(self.edges[i][0])
            nodes.append(self.edges[i][1])
        return nodes
