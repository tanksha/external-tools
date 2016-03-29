/*
Contains various helper functions
*/


var isNode = function (atom) {
    return atom["type"].indexOf("Node") > -1;
};

var isLink = function (atom) {
    return atom["type"].indexOf("Link") > -1;
};

var findByHandle = function(atom, atoms){
    return atoms.filter(function(a){
     return a.handle === atom.handle;
   })
}

var findById = function(nodes, id){
    return nodes.filter(function(n){
        return n.id == id;
    });
}

var removeNodes = function(graph, id, depth){ /* polymorphic recursive function.. */
      depth = depth || 1;
      var node = findById(graph.nodes, id)[0]
      /*if depth is defined it will use it or it just removes one node at a time*/
      if(node){ 
          if(node.incoming.length + node.outgoing.length != 0){
                var max = node.outgoing.length                    
                if(node.incoming.length > node.outgoing.length)
                    max = node.incoming.length
                for(var i=0; i < max; i++){ /* single loop for better performance ... */
                    if(node.outgoing[i] != undefined && node.outgoing.length != 0){  
                       if(depth > 0)
                            graph = removeNodes(graph, node.incoming[i], isNode(node) ? depth - 1 : depth) 
                        /*each copy of graph varriable will be distroyed after exiting of the function
                         * we will not consider a link as node so if the node is a link we will note do any change to depth param
                        */
                       var n = findById(graph.nodes, node.outgoing[i])[0]
                       graph.nodes.splice(graph.nodes.indexOf(n), 1)                        
                       graph.collapsedNodes.push(n)
                       if(isLink(node)){ /* links should be updated along sinde with the removal of nodes*/
                            var link = graph.links.splice(graph.links.indexOf({source:node, target:n, label:node.label}))
                            graph.collapsedLinks.push(link)
                       }                                    
                    }
                    if(node.incoming[i] != undefined && node.incoming.length != 0){
                        if(depth > 0)
                            graph = removeNodes(graph, node.incoming[i], isNode(node) ? depth - 1 : depth)
                        var n = findById(graph.nodes, node.outgoing[i])[0]
                        graph.nodes.splice(graph.nodes.indexOf(n), 1) 
                        graph.collapsedNodes.push(n) 
                        if(isLink(node)){
                            var link = graph.links.splice(graph.links.indexOf({source:node, target:n, label:node.label}))
                            graph.collapsedLinks.push(link)
                        }                              
                    }  
                                           
                }                
          }                                  
        }
        return graph; /* returns graph object for the next recursion */
}

var findLinksByHandle = function(linkHandles, atoms){
    var linksFound = [];
    for (var atom_index = 0; atom_index < atoms.length; atom_index++) {
        for(var link_i = 0; link_i < linkHandles.length; link_i++){
            if(atoms[atom_index].handle == linkHandles[link_i]){
                linksFound.push(atoms[atom_index]);
            }
        }
    }
    return linksFound;
}



var processAtoms = function(atoms){
    var graph = {nodes: [], raw_links: [], links: []};
    for (var atom_index = 0; atom_index < atoms.length; atom_index++) {
        var atom = atoms[atom_index];
        graph.nodes.push({
            id: atom["handle"],
            label: atom["name"],
            type: atom["type"],
            outgoing: atom.outgoing,
            incoming: atom.incoming,
            collapsed: (atom.outgoing.length + atom.incoming.length) - (findLinksByHandle(atom['outgoing'], atoms).length + findLinksByHandle(atom['incoming'], atoms).length)
        });

        if (isLink(atom)) {
            for (var outgoing_index = 0; outgoing_index < atom["outgoing"].length; outgoing_index++) {
                graph.raw_links.push({
                    source: atom["handle"],
                    target: atom["outgoing"][outgoing_index],
                    label: atom['name']
                });
            }
        }
    }

    graph.raw_links.forEach(function (e) {
        var sourceNode = graph.nodes.filter(function (n) {
                return n.id === e.source;
            })[0],
            targetNode = graph.nodes.filter(function (n) {
                return n.id === e.target;
            })[0];
        graph.links.push({source: sourceNode, target: targetNode, label: e.label});
    });
    graph.collapsedNodes = [];
    graph.collapsedLinks = [];

    return graph;
}