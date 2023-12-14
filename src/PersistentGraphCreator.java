/**
 * Author: Declan ONUNKWO
 * College: SUNY Oswego
 * CSC 365 Project 3
 * Fall 2023
 */

import HashClasses.CustomHashTable;
import HashClasses.ExtendibleHashing;

import java.io.*;
import java.util.*;

class Edge implements java.io.Serializable{
    private String from;
    private String to;
    private final double similarity;

    public Edge (String from, String to, double similarity) {
        this.from = from;
        this.to = to;
        this.similarity = similarity;
    }

    double getSimilarity(){
        return similarity;
    }

    String getTo(){
        return to;
    }

}

class Graph implements java.io.Serializable {

    private HashMap<String, ArrayList<Edge>> graph;
    private String[] urlList;

    public Graph (String[] urlList) {
        this.urlList = urlList;
        createGraph(urlList);
    }

    public Graph (String[] urlList, ExtendibleHashing verticesMap) {
        this.urlList = urlList;
        createGraph(urlList,verticesMap);
    }

    private void createGraph(String[] urlList) {
        graph = new HashMap<>();
        // I will leave this empty for now (or forever)
    }

    private void createGraph(String[] urlList, ExtendibleHashing verticesMap) {
        graph = new HashMap<>();

        for(int i=0; i<urlList.length; i++){

            ArrayList<Edge> edgeList = new ArrayList<>();
            CustomHashTable ht1 = verticesMap.find(urlList[i]);

            for(int j=0; j<urlList.length; j++){

                if(!urlList[j].equals(urlList[i])){

                    CustomHashTable ht2 = verticesMap.find(urlList[j]);
                    String[] ht2WordList = ht2.toKeyList();
                    double similarity = SimilarityAlgorithm.doCosineSimilarity(ht1,ht2,ht2WordList);
                    similarity = (similarity != 0) ? 1/similarity : 0; // avoiding Math error while trying to store the inverse

                    edgeList.add(new Edge(urlList[i], urlList[j], similarity));
                }
            }
            graph.put(urlList[i],edgeList);
        }
    }

    // Dijkstra-like algorithm to find the shortest path (using max weight :) )
    public String[] getPath(String from, String to){
        DisjointDataSet disjointSet = new DisjointDataSet(urlList.length);
        HashMap<String, Double> shortestDistances = new HashMap<>();
        HashMap<String, String> previousNodes = new HashMap<>();

        shortestDistances.put(from, 0.0);

        PriorityQueue<String> nodesToVisit = new PriorityQueue<>(Comparator.comparingDouble(shortestDistances::get));
        nodesToVisit.add(from);

        while (!nodesToVisit.isEmpty()) {
            String currentNode = nodesToVisit.poll();

            if (currentNode.equals(to)) {
                break;
            }

            for (Edge edge : graph.get(currentNode)) {
                String neighbor = edge.getTo();
                double newDistance = shortestDistances.get(currentNode) + edge.getSimilarity();
                int indexCurrentNode = Arrays.asList(urlList).indexOf(currentNode);
                int indexNeighbor = Arrays.asList(urlList).indexOf(neighbor);

                if (!shortestDistances.containsKey(neighbor) || newDistance < shortestDistances.get(neighbor)) {
                    shortestDistances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, currentNode);
                    nodesToVisit.add(neighbor);
                    disjointSet.union(indexCurrentNode, indexNeighbor);
                }
            }
        }

        if (!shortestDistances.containsKey(to)) {
            return null; // No path to our destination "to"
        }

        ArrayList<String> path = new ArrayList<>();
        for (String node = to; node != null; node = previousNodes.get(node)) {
            path.add(node);
        }
        path.add(disjointSet.getCount()+"");
        Collections.reverse(path);

        return path.toArray(new String[0]);
    }

}

public class PersistentGraphCreator {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Deserialize Extensible hashing class
        FileInputStream fis = new FileInputStream("SerializedExtensibleHashingClass");
        ObjectInputStream ois = new ObjectInputStream(fis);
        ExtendibleHashing urlsMappedToObject = (ExtendibleHashing) ois.readObject();

        // Deserialize myUrl list
        FileInputStream fis2 = new FileInputStream("SerializedUrlList");
        ObjectInputStream ois2 = new ObjectInputStream(fis2);
        CustomHashTable deserializedUrlList = (CustomHashTable) ois2.readObject();
        String[] myUrls = deserializedUrlList.toKeyList();

        Graph urlGraph = new Graph(myUrls,urlsMappedToObject);

        // Serialize graph
        FileOutputStream fos = new FileOutputStream("SerializedGraph");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(urlGraph);

    }
}
