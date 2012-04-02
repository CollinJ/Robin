from Path import *
from Graph import *
import random

class Node:
    id = 0
    blacklist = {}
    def __init__(self, *args):
        self.id = args[0]
        self.blacklist = set()
        self.trusts = dict()
        self.parent = None
        for arg in args[1:]:
            self.trust(arg)
        
    def trust(self, node):
        if node.id not in self.trusts:
            self.trusts[node.id] = node
            node.trust(self)
        
    def __repr__(self):
        return "Node:" +str(self.id)
    
    def __str__(self):
        return str(self.id) + ": " + str(list(self.trusts.values()))
    
    
    def shortestPathTo(self, node):
        queue = []
        queue.append(self)
        explored = set()
        explored.add(self.id)
        #print("New bfs call: "+str(node.id))
        while True:
            #print(queue)
            #print(explored)
            if len(queue) == 0:
                return None
            curr = queue.pop(0)
            if curr.id == node.id:
                #print("Path found.")
                return curr.getPath()
            for trust in curr.trusts:
                if (trust in explored) or (curr.trusts[trust] in queue):
                    continue
                curr.trusts[trust].parent = curr
                queue.append(curr.trusts[trust])
            explored.add(curr.id)
    
    def bfs(self, node):
        queue = []
        queue.append(self)
        explored = set()
        explored.add(self.id)
        #print("New bfs call: "+str(node.id))
        while True:
            #print(queue)
            #print(explored)
            if len(queue) == 0:
                return None
            curr = queue.pop(0)
            if curr.id == node.id:
                #print("Path found.")
                return curr.getPath()
            for trust in curr.trusts:
                if (trust in explored) or (curr.trusts[trust] in queue):
                    continue
                if curr.id in Node.blacklist:
                    if trust in Node.blacklist[curr.id]:
                        continue
                curr.trusts[trust].parent = curr
                queue.append(curr.trusts[trust])
            explored.add(curr.id)

    def getPath(self):
        path = []
        curr = self
        while curr is not None:
            path.append(curr)
            if curr.parent is not None:
                if curr.id in Node.blacklist:
                    Node.blacklist[curr.id].add(curr.parent.id)
                else:
                    Node.blacklist[curr.id] = set()
                    Node.blacklist[curr.id].add(curr.parent.id)

                if curr.parent.id in Node.blacklist:
                    Node.blacklist[curr.parent.id].add(curr.id)
                else:
                    Node.blacklist[curr.parent.id] = set()
                    Node.blacklist[curr.parent.id].add(curr.id) 
            curr = curr.parent
        path.reverse()
        return Path(path)

    def trustFactor(self, node, graph):
        trust = graph.summary(self)
        return trust[node.id]
    
    def pathsTo(self, node):
        paths = []
        while True:
            path = self.bfs(node)
            if path == None:
                Node.blacklist = {}
                return paths
            paths.append(path)


#graph = Graph()
#
#g1 = [Node() for i in range(50)]
#g2 = [Node() for i in range(50)]
#
#for node in g1:
#    for other_node in g1[g1.index(node)+1:]:
#        if node != other_node and random.randint(0,10)+random.randint(0,10) < 4:
#            node.trust(other_node)
#    graph.add(node)
#for node in g2:
#    for other_node in g2[g2.index(node)+1:]:
#        if node != other_node and random.randint(0,10)+random.randint(0,10) < 4:
#            node.trust(other_node)
#    graph.add(node)
##for node in g2:
##    g1[48].trust(node)
##    graph.add(node)
#g1[15].trust(g2[5])
#g1[5].trust(g2[25])
#
#graph.drawMe(g1[1])
#





        
        
