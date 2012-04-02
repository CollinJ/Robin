import pygraphviz as pgv
from ucb import interact

class Graph:
    def __init__(self, *nodes):
        self.nodes = list(nodes)
        self.G = pgv.AGraph(resolution="75", overlap='scale', splines="True")

    def __iter__(self):
        return self.nodes.__iter__()

    def __repr__(self):
        string = ""
        for node in self:
            string += str(node) + '\n'
        return string
        
    def summary2(self, this_node):
        max_edges = 0
        shortest_path_dict = dict() #Key = Node.id : Value = len(shortest path from HOST to KEY)
        out_edges_dict = dict() #Key = Node.id : Value = list of nodes that are outward from it
        votes_dict = dict() #Key = Node.id : Value = number of votes they have.
        
        for node in self:    # This builds the shortest_path_dict by the length each node is away from the HOST node
            if this_node.id != node.id:
                path = this_node.shortestPathTo(node)
                
                if path is not None:
                    shortest_path_dict[node.id] = len(path)
                else:
                    shortest_path_dict[node.id] = 0
        for node in self:   #Builds the out_edges_dict by calculating how many connecting nodes are at least as far away from host as this node
            if this_node.id != node.id: #Don't look at host node
                for edge_node_id in node.trusts: #check each node connected to this one
                    if edge_node_id != this_node.id:
                        if shortest_path_dict[edge_node_id] > shortest_path_dict[node.id]: #if they are farther away from host than we are, we give them some votes
                            if node.id in out_edges_dict: #add ourself to the list of nodes that are outward from the node we are looking at
                                out_edges_dict[node.id] += [edge_node_id]
                            else:
                                out_edges_dict[node.id] = [edge_node_id]
        max_edges = len(max(out_edges_dict.values(), key=len)) #unused now
        const_vote_percent = .1 #percent of votes it gives to each ouward edge <------THIS IS SOMETHING YOU CAN CHANGE
        queue = [] #BF distribution of votes
        for edge_id in this_node.trusts: #Populate the immediate friends vote counts
            votes_dict[edge_id] = 100/len(this_node.trusts)
            queue.append(edge_id)
        while len(queue) != 0:
            const_vote_percent = .1
            node_id = queue.pop(0)
            if node_id in out_edges_dict:
                num_nodes = len(out_edges_dict[node_id])
                if num_nodes * const_vote_percent > .7: # <----- CHANGE THIS .5
                    const_vote_percent = .7/num_nodes # <------ AND THIS .5 TO THE SAME NUMBER
                for edge_node_id in out_edges_dict[node_id]:
                    if edge_node_id == this_node.id:
                        continue
                    votes_dict[edge_node_id] = const_vote_percent * votes_dict[node_id]
                    queue.append(edge_node_id)
                votes_dict[node_id] -= const_vote_percent * votes_dict[node_id]*len(out_edges_dict[node_id])
        print(str(out_edges_dict))
        return votes_dict
        
        
        
    def summary(self, this_node):
        total_unique_paths = 0
        unique_paths_by_node = {}  # <- Collin can't name things. This is
                                   # node.id: list of path lengths
        real_unique_paths_by_node = {}
        for node in self:
            if node != this_node:
                paths = this_node.pathsTo(node)
                real_unique_paths_by_node[node.id] = paths
                unique_paths_by_node[node.id] = [len(path) for path in paths]
                total_unique_paths += sum(unique_paths_by_node[node.id])
##        print(unique_paths_by_node)
##        print(total_unique_paths)
        summary = dict((node, sum([(total_unique_paths*1.0)/i for i in unique_paths_by_node[node]])) for (node) in unique_paths_by_node.keys())
        max_trust = max(summary.values())
        summary = dict((node, (summary[node]*1.0)/max_trust) for (node) in unique_paths_by_node.keys())
        newSum = {}
        for nodeId in real_unique_paths_by_node.keys():
            Paths = real_unique_paths_by_node[nodeId]
            print("Paths: ", Paths)
            if Paths == []:
                continue
            pathNodes = real_unique_paths_by_node[nodeId][0].getNodes()
            print("This node: ", this_node.id)
            print("Path Nodes: ", pathNodes)
            print("Summary Keys: ", summary.keys())
            invertedPathNodePhase1TrustValues = []
            for path_node in pathNodes:
                if (path_node.id != this_node.id):
                    invertedPathNodePhase1TrustValues.append(summary[path_node.id]**(-1))
            newSum[nodeId] = sum(invertedPathNodePhase1TrustValues)
##        for node in summary.keys():
##            print(str(node) + " : " + str(summary[node]))
        return newSum
    
    def drawMe(self, this_node=None):
        if this_node is not None:
            summary = self.summary2(this_node)
            for node in self.nodes:
                for trust in node.trusts:
                    if this_node == None:
                        self.G.add_edge(node.id, trust)
                    elif node.id == this_node.id:
                        self.G.add_edge(this_node.id, str(trust) + ":" + str(round(summary[trust], 6)))
                    elif this_node.id == trust:
                        self.G.add_edge(this_node.id, str(node.id) + ":" + str(round(summary[node.id], 6)))
                    else:
                        self.G.add_edge(str(node.id) + ":" + str(round(summary[node.id], 6)), str(trust) + ":" + str(round(summary[trust], 6)))
        else:
            for node in self.nodes:
                for trust in node.trusts:
                    if trust is not node.id:
                        self.G.add_edge(node.id, trust)
        print("Creating graf")
        self.G.layout(prog='dot')
        self.G.draw('graph.png')

    def printSummary(self, node):
        summary = self.summary(node)
        for node in summary.keys():
            print(str(node) + " : " + str(summary[node]))

    def add(self, node):
        self.nodes.append(node)
