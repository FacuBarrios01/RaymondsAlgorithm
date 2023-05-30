import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class Node {
    private int id;
    private int numNodes;
    // If parent node value is -1, it means the node is the token holder
    private int parentNode;
    // We use a LinkedHashSet to ensure uniquenes in the Queue, while keeping
    // insertion order
    private LinkedHashSet<Integer> requestQueue;
    private boolean csInUse;

    private static Node allNodes[];

    public Node(int id, int numNodes, int parentNodeId) {
        this.id = id;
        this.numNodes = numNodes;
        this.parentNode = parentNodeId;
        this.requestQueue = new LinkedHashSet<>();
        this.csInUse = false;
    }

    public void requestToken() {
        // Add it self id to request queue
        if (parentNode != -1) {
            requestQueue.add(this.id);
            getNode(parentNode).receiveRequest(this.id);
        }
    }

    public void receiveRequest(int senderId) {
        requestQueue.add(senderId);
        if (parentNode != -1) {
            getNode(this.parentNode).receiveRequest(this.id);
        }
    }

    public void receiveReply(int senderId) {
        if (!this.requestQueue.isEmpty())
            this.requestQueue.add(senderId);
        Integer[] requests = new Integer[requestQueue.size()];
        requests = requestQueue.toArray(requests);
        // If the node is the requester it removes its parent node and adquires the
        // token
        if (requests[0] == this.id) {
            this.parentNode = -1;
            requestQueue.remove(requests[0]);
        } else {
            // If the node is not the token requester we propagate the request
            this.parentNode = requests[0];
            this.requestQueue.remove(requests[0]);
            getNode(parentNode).receiveReply(this.id);
        }

        // Print state of the graph after every reply propagation
        for (int i = 0; i < allNodes.length; i++) {
            String queueStatus = allNodes[i].requestQueue.toString();
            if (allNodes[i].parentNode == -1)
                System.out.println("*" + (i + 1) + " : " + queueStatus);
            else
                System.out.println(" " + (i + 1) + " : " + queueStatus);
        }
        System.out.println("\n");
    }

    // Simulate CS computation
    public void executeCS() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // We look for the parent node in our system and release the token
    public static void releaseToken() {
        boolean tokenFound = false;
        for (int i = 0; i < allNodes.length; i++) {
            if (allNodes[i].parentNode == -1 && !tokenFound) {
                Node pTokenNode = getNode(allNodes[i].id);
                Integer[] requests = new Integer[pTokenNode.requestQueue.size()];
                requests = allNodes[i].requestQueue.toArray(requests);

                pTokenNode.parentNode = requests[0];
                pTokenNode.requestQueue.remove(requests[0]);
                Node newTokenNode = getNode(pTokenNode.parentNode);
                newTokenNode.receiveReply(pTokenNode.id);
                tokenFound = true;
            }
        }
    }

    public static Node getNode(int nodeId) {
        return allNodes[nodeId - 1];
    }

    public static void main(String args[]) throws IOException {
        // Check for correct arguments
        if (args.length < 1) {
            System.out.println("No dataset provided");
            System.exit(0);
        } else if (args.length > 1) {
            System.out.println("Too many arguments");
            System.exit(0);
        }

        File inputFile = null;
        Scanner reader = null;
        inputFile = new File(args[0]);
        if (inputFile != null)
            reader = new Scanner(inputFile);
        String line = reader.nextLine();
        if (!line.contains("Number of nodes:")) {
            System.out.println(
                    "First line must contain \"Number of nodes:\" immediately followed by a integer value ex. \"Number of nodes:7\"");
            System.exit(0);
        }

        // Extract tree structure of the dataset
        int numNodes = Integer.parseInt(line.split(":")[1]);
        allNodes = new Node[numNodes];
        int nodeId;
        int parentsId;

        // Create nodes
        for (int i = 0; i < numNodes; i++) {
            try {
                line = reader.nextLine();
                nodeId = Integer.parseInt((line.split(",")[0]));
                parentsId = Integer.parseInt((line.split(",")[1]));
                allNodes[i] = new Node(nodeId, numNodes, parentsId);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(
                        "The first value must be the node id, followed by a \',\' and the id of the node's parent. ex: \"4:1\"");
                System.out.println(
                        "Node id MUST NOT exceed the value of the number of nodes. See example dataset in the documentation.");
                System.exit(0);
            }
        }

        line = reader.nextLine();
        if (!line.contains("Token requests:")) {
            System.out.println(
                    "To introduce requests in the dataset you must start with a single line with \"Token requests:\".");
            System.out.println(
                    "After this line we will input our requests in cronological order by adding the requesting node's id in a single line. See example dataset in the documentation.");
            System.exit(0);
        }

        // Here the execution of the program begins
        int requestingNodeId;
        int requestCounter = 0;

        // We will place all of the request at the same time to demonstrate how the
        // algorithm works
        while (reader.hasNextLine()) {
            line = reader.nextLine();
            requestingNodeId = Integer.parseInt(line);
            getNode(requestingNodeId).requestToken();
            requestCounter++;
        }

        // We print the initial state of the graph
        System.out.println("Initial state: ");
        for (int j = 0; j < allNodes.length; j++) {
            String queueStatus = allNodes[j].requestQueue.toString();
            if (allNodes[j].parentNode == -1)
                System.out.println("*" + (j + 1) + " : " + queueStatus);
            else
                System.out.println(" " + (j + 1) + " : " + queueStatus);
        }
        System.out.println("\n");

        // And propagate the token one by one
        for (int i = 0; i < requestCounter; i++) {
            System.out.println( (i+1) + "th. request fulfiled");
            releaseToken();

            for (int j = 0; j < allNodes.length; j++) {
                String queueStatus = allNodes[j].requestQueue.toString();
                if (allNodes[j].parentNode == -1)
                    System.out.println("*" + (j + 1) + " : " + queueStatus);
                else
                    System.out.println(" " + (j + 1) + " : " + queueStatus);
            }
            System.out.println("\n");
        }

        // Print graph topology and request queues state
        System.out.println("Final topology: ");
        for (int i = 0; i < allNodes.length; i++) {
            System.out.println(allNodes[i].id + " -> " + allNodes[i].parentNode);
        }

        System.out.println("Final Queues state: ");
        for (int j = 0; j < allNodes.length; j++) {
            String queueStatus = allNodes[j].requestQueue.toString();
            if (allNodes[j].parentNode == -1)
                System.out.println("*" + (j + 1) + " : " + queueStatus);
            else
                System.out.println(" " + (j + 1) + " : " + queueStatus);
        }

        // Close readers and exit the program
        reader.close();
        System.exit(1);
    }
}