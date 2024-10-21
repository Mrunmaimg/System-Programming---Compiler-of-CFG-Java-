import java.io.*;
import java.util.*;

public class CFGCompiler {

    // Class to represent a parse tree node
    static class ParseTreeNode {
        String symbol;
        ParseTreeNode left;
        ParseTreeNode right;

        // Constructor for non-terminal node
        ParseTreeNode(String symbol) {
            this.symbol = symbol;
        }

        // Constructor for non-terminal with children
        ParseTreeNode(String symbol, ParseTreeNode left, ParseTreeNode right) {
            this.symbol = symbol;
            this.left = left;
            this.right = right;
        }

        // Function to print the parse tree with tree structure
        public void printTree(String indent, boolean isLast) {
            System.out.print(indent);
            if (isLast) {
                System.out.print("└── ");
                indent += "    ";
            } else {
                System.out.print("├── ");
                indent += "│   ";
            }
            System.out.println(symbol);
            if (left != null) {
                left.printTree(indent, right == null); // Left node
            }
            if (right != null) {
                right.printTree(indent, true); // Right node (always last)
            }
        }
    }

    // Function to parse the grammar from input.txt file
    public static Map<String, List<String>> readGrammar(String filename) {
        Map<String, List<String>> grammar = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("->");
                String left = parts[0].trim();
                String[] right = parts[1].trim().split("\\|"); // handle multiple productions
                grammar.putIfAbsent(left, new ArrayList<>());
                for (String prod : right) {
                    grammar.get(left).add(prod.trim());
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grammar;
    }

    // Function to display the grammar
    public static void displayGrammar(Map<String, List<String>> grammar) {
        System.out.println("Grammar:");
        for (Map.Entry<String, List<String>> entry : grammar.entrySet()) {
            for (String production : entry.getValue()) {
                System.out.println(entry.getKey() + " -> " + production);
            }
        }
    }

    // CYK algorithm with parse tree construction
    public static ParseTreeNode cykParse(String input, Map<String, List<String>> grammar) {
        int n = input.length();
        List<ParseTreeNode>[][] dp = new List[n][n];

        // Initialize the table
        for (int i = 0; i < n; i++) {
            dp[i][i] = new ArrayList<>();
            String symbol = Character.toString(input.charAt(i));
            for (String lhs : grammar.keySet()) {
                for (String rhs : grammar.get(lhs)) {
                    if (rhs.equals(symbol)) {
                        ParseTreeNode terminalNode = new ParseTreeNode(symbol); // Terminal node
                        dp[i][i].add(new ParseTreeNode(lhs, terminalNode, null)); // Add terminal as child
                    }
                }
            }
        }

        // Fill the table for substrings of length > 1
        for (int length = 2; length <= n; length++) {
            for (int i = 0; i <= n - length; i++) {
                int j = i + length - 1;
                dp[i][j] = new ArrayList<>();

                for (int k = i; k < j; k++) {
                    for (String lhs : grammar.keySet()) {
                        for (String rhs : grammar.get(lhs)) {
                            if (rhs.length() == 2) {
                                char B = rhs.charAt(0);
                                char C = rhs.charAt(1);
                                List<ParseTreeNode> leftNodes = dp[i][k];
                                List<ParseTreeNode> rightNodes = dp[k + 1][j];

                                for (ParseTreeNode left : leftNodes) {
                                    for (ParseTreeNode right : rightNodes) {
                                        if (left.symbol.equals(Character.toString(B))
                                                && right.symbol.equals(Character.toString(C))) {
                                            dp[i][j].add(new ParseTreeNode(lhs, left, right));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if the start symbol 'S' is in dp[0][n-1]
        for (ParseTreeNode root : dp[0][n - 1]) {
            if (root.symbol.equals("S")) {
                return root; // Return the parse tree with root 'S'
            }
        }
        return null; // No valid parse tree found
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read the grammar from input.txt
        Map<String, List<String>> grammar = readGrammar("input.txt");

        // Display the grammar
        displayGrammar(grammar);

        // Loop to allow multiple string inputs
        while (true) {
            // Get the string from user
            System.out.print("Enter the string to parse (or type 'exit' to quit): ");
            String input = scanner.nextLine();

            // Exit the loop if user types 'exit'
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the program.");
                break;
            }

            // Parse the string using CYK and get the parse tree
            ParseTreeNode parseTree = cykParse(input, grammar);
            if (parseTree != null) {
                System.out.println("The string can be parsed by the grammar. \nParse Tree:");
                parseTree.printTree("", true); // Print the parse tree with structure
            } else {
                System.out.println("The string cannot be parsed by the grammar.");
            }
        }

        scanner.close();
    }
}