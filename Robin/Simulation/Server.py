import SocketServer
from Graph import *
from Node import Node
from Path import *
SocketServer.TCPServer.allow_reuse_address=True

class MyTCPHandler(SocketServer.BaseRequestHandler):
    edges = set()
    trusts = dict()
    f = open(".trust", "a+")
    f.seek(0)
    line = f.readline()
    graph = Graph()
    nodes = set()
    try:
        trusts = eval(line)
    except SyntaxError:
        trusts = dict()
    for key in trusts:
        for value in trusts[key]:
            if key in trusts[value]:
                if (value, key) not in edges:
                    edges.add((key,value))
    def handle(self):
        [sender, friend] = self.request.recv(1024).strip().split()
        print "{} wrote:".format(self.client_address[0])
        print(str(sender) + " requested " + str(friend))
        if not self.trusts.has_key(sender):
            self.trusts[sender] = [friend]
        else:
            self.trusts[sender].append(friend)
        if self.trusts.has_key(friend):
            print("has friend")
            if sender in self.trusts[friend]:
                print("has sender")
                if (sender, friend) not in self.edges:
                    self.edges.add((friend, sender))
                    friend = Node(friend)
                    sender = Node(sender)
                    friend.trust(sender)
                    sender.trust(sender)
                    self.graph.add(friend)
                    self.graph.add(sender)
                    self.graph.drawMe()
                    
                    
        print(self.trusts)
        print(self.edges)
        print(self.nodes)
        print(self.graph.nodes)

if __name__ == "__main__":
    HOST, PORT = "localhost", 9999
    server = SocketServer.TCPServer((HOST,PORT), MyTCPHandler)
    server.serve_forever()
