package jreyno77.com.github.factories;

import jreyno77.com.github.operations.ConvertEncodingOperation;
import jreyno77.com.github.operations.Operation;

public class OperationFactory {

    public static Operation createOperation(String operationName) {
        switch (operationName) {
            case "ConvertEncoding":
                return new ConvertEncodingOperation();
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
