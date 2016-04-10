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

var removeNodes = function(graph, idc, idp, depth){ /* polymorphic recursive function.. */
      if(depth == undefined)
        depth = 1
      idp = idp || undefined
      var nodec = findById(graph.nodes, idc)[0]
      var nodep = findById(graph.nodes, idp)[0]
      /*if depth is defined it will use it or it just removes one node at a time*/
      if(nodec){ 
          if(nodec.incoming.length + nodec.outgoing.length != 0){
            if(depth > 0){
                var max = nodec.outgoing.length
                if(nodec.incoming.length > nodec.outgoing.length)
                    max = nodec.incoming.length
                for(var i=0; i < max; i++){ /* single loop for better performance ... */
                    if(nodec.outgoing.length != 0 && nodec.outgoing[i] != undefined){
                        if((idp != undefined && idp != nodec.outgoing[i]) || idp == undefined){
                            var nodenx = findById(graph.nodes, nodec.outgoing[i])[0]
                            console.log("depth of next node is ", isNode(nodenx) ? depth - 1 : depth)
                            console.log("depth ", depth)
                            var z = isNode(nodenx) ? depth - 1 : depth
                            graph = removeNodes(graph, nodec.outgoing[i], nodec.id, z)
                            
                        }
                        
                    }   
                    if(nodec.incoming.length != 0 && nodec.incoming[i] != undefined ){
                        if((idp != undefined && idp != nodec.incoming[i]) || idp == undefined){
                            var nodenx = findById(graph.nodes, nodec.incoming[i])[0]
                            console.log("depth of next node is ", isNode(nodenx) ? depth -1 : depth)
                            console.log("depth ", depth)
                            var z = isNode(nodenx) ? depth - 1 : depth
                            graph = removeNodes(graph, nodec.incoming[i], nodec.id, z)
                           
                        }
                        
                    }                                                                             
                }
                if(nodep){
                    graph.nodes.splice(graph.nodes.indexOf(nodec), 1)
                    if(nodep.outgoing.indexOf(nodec.id)){
                         graph.links.splice(graph.links.indexOf({source:nodep, target:nodec, label:nodep.label}), 1)
                    }else{
                         graph.links.splice(graph.links.indexOf({source:nodec, target:nodep, label:nodec.label}), 1)
                    }
                }

            }else if(depth == 0){
                if(nodec.incoming.length + nodec.outgoing.length > 1){
                    if(nodep){
                        graph.nodes.splice(graph.nodes.indexOf(nodec), 1)
                             graph.links.splice(graph.links.indexOf({source:nodep, target:nodec, label:nodep.label}), 1)
                        
                             graph.links.splice(graph.links.indexOf({source:nodec, target:nodep, label:nodec.label}), 1)
                        
                    }
                }else{
                    
                     graph.links.splice(graph.links.indexOf({source:nodep, target:nodec, label:nodep.label}), 1)
                
                     graph.links.splice(graph.links.indexOf({source:nodec, target:nodep, label:nodec.label}), 1)
                    
                }
            }
                            
          }                                  
      }
      console.log(graph)
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