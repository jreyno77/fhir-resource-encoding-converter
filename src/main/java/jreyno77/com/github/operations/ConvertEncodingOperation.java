package jreyno77.com.github.operations;

import jreyno77.com.github.parameters.ConvertEncodingParameters;
import jreyno77.com.github.processors.ConvertEncodingArgumentProcessor;
import jreyno77.com.github.processors.ConvertEncodingProcessor;

public class ConvertEncodingOperation extends Operation {

    public ConvertEncodingOperation() {    
    } 

    @Override
    public void execute(String[] args) {
        ConvertEncodingParameters params = null;
        try {
            params = new ConvertEncodingArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        ConvertEncodingProcessor.convertEncoding(params);
    }   
}