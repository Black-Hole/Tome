package generator.paths

import java.util.*

suspend fun <TNode> aStar(
    start: TNode,
    isEnd: (TNode) -> Boolean,
    neighbors: (TNode) -> List<TNode>,
    cost: (ULong, TNode) -> ULong,
    heuristic: (TNode) -> ULong,
    exploreNodeCallback: suspend (TNode) -> Unit
): TNode? where TNode : Any {
    
    data class Node<T>(
        val cost: ULong,
        val heuristic: ULong,
        val state: T
    )
    
    val comparator = Comparator<Node<TNode>> { a, b ->
        (a.cost + a.heuristic).compareTo(b.cost + b.heuristic)
    }
    
    val openSet = PriorityQueue(comparator)
    val closedSet = mutableSetOf<TNode>()
    
    openSet.add(Node(
        cost = 0u,
        heuristic = heuristic(start),
        state = start
    ))
    
    while (openSet.isNotEmpty()) {
        val currentNode = openSet.poll()
        
        exploreNodeCallback(currentNode.state)
        
        if (isEnd(currentNode.state)) {
            return currentNode.state
        }
        
        closedSet.add(currentNode.state)
        
        for (neighbor in neighbors(currentNode.state)) {
            if (neighbor in closedSet) {
                continue
            }
            
            val newCost = cost(currentNode.cost, neighbor)
            val newHeuristic = heuristic(neighbor)
            
            openSet.add(Node(
                cost = newCost,
                heuristic = newHeuristic,
                state = neighbor
            ))
        }
    }
    
    return null
}
