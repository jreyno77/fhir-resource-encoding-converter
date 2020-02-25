package jreyno77.com.github.processors;

import static java.util.Arrays.asList;

import jreyno77.com.github.parameters.ConvertEncodingParameters;
import java.util.ArrayList;
import java.util.List;

import jreyno77.com.github.processors.ConvertEncodingProcessor.FHIRVersion;
import jreyno77.com.github.utilities.ArgUtils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import jreyno77.com.github.utilities.HAPIIOUtils.Encoding;


public class ConvertEncodingArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"ConvertEncoding"};

    public static final String[] RESOURCE_PATH_OPTIONS = {"rp", "resourcepath"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhir-version"};
    public static final String[] OUTPUT_ENCODING = {"e", "encoding"};
    public static final String[] RESOURCE_DIRECTORY_PATH_OPTIONS = {"rdp", "resourcedirectorypath"};

    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder resourcePathBuilder = parser.acceptsAll(asList(RESOURCE_PATH_OPTIONS),"Limited to a single version of FHIR. If omitted directories must be specified");
        OptionSpecBuilder resourceDirectoryPathBuilder = parser.acceptsAll(asList(RESOURCE_DIRECTORY_PATH_OPTIONS),"Use multiple times to define multiple resource directories. If omitted resourcePath must be specified");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"As of now FHIR DSTU3 and R4 are supported");
        OptionSpecBuilder outputEncodingBuilder = parser.acceptsAll(asList(OUTPUT_ENCODING), "If omitted, output will be generated using JSON encoding.");
    
        OptionSpec<String> resourcePath = resourcePathBuilder.withOptionalArg().describedAs("path to target resource");
        OptionSpec<String> resourceDirectoryPath = resourceDirectoryPathBuilder.withOptionalArg().describedAs("directory of resources");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withRequiredArg().describedAs("fhir version");
        OptionSpec<String> outputEncoding = outputEncodingBuilder.withOptionalArg().describedAs("desired output encoding for resources"); 

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public ConvertEncodingParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String resourcePath = (String)options.valueOf(RESOURCE_PATH_OPTIONS[0]);
        List<String> resourceDirectoryPaths = (List<String>)options.valuesOf(RESOURCE_DIRECTORY_PATH_OPTIONS[0]);
        String fhirVersion = (String)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        String outputEncoding = (String)options.valueOf(OUTPUT_ENCODING[0]);
        Encoding outputEncodingEnum = Encoding.JSON;
        if (outputEncoding != null) {
            outputEncodingEnum = Encoding.parse(outputEncoding.toLowerCase());
        }
        // Boolean versioned = options.has(VERSIONED_OPTIONS[0]);

        ArrayList<String> paths = new ArrayList<String>();
        paths.addAll(resourceDirectoryPaths);
    
        ConvertEncodingParameters cep = new ConvertEncodingParameters();
        cep.resourcePath = resourcePath;
        cep.fhirVersion = FHIRVersion.parse(fhirVersion);
        cep.outputEncoding = outputEncodingEnum;
        //cep.versioned = versioned;
        cep.resourceDirectoryPaths = paths;
       
        return cep;
    }
}