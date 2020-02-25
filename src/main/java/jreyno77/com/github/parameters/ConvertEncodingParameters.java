package jreyno77.com.github.parameters;

import java.util.ArrayList;
import jreyno77.com.github.processors.ConvertEncodingProcessor.FHIRVersion;
import jreyno77.com.github.utilities.HAPIIOUtils;

public class ConvertEncodingParameters {  
    public String resourcePath;
    public ArrayList<String> resourceDirectoryPaths;
    public FHIRVersion fhirVersion;
    public HAPIIOUtils.Encoding outputEncoding;
}