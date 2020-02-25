package jreyno77.com.github;

import jreyno77.com.github.factories.OperationFactory;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Error: Requests must include which operation to run as a command line argument. See docs for examples on how to use this project.");
        }

        String operation = args[0];
        if (!operation.startsWith("-")) {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }

        OperationFactory.createOperation(operation.substring(1)).execute(args);
    }
}