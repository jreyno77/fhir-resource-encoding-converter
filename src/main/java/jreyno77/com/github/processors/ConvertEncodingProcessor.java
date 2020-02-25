package jreyno77.com.github.processors;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;
import jreyno77.com.github.parameters.ConvertEncodingParameters;
import jreyno77.com.github.utilities.HAPIIOUtils;
import jreyno77.com.github.utilities.HAPIIOUtils.Encoding;

public class ConvertEncodingProcessor {
    public enum FHIRVersion {
        FHIR3("fhir3"), FHIR4("fhir4");

        private String string;

        public String toString() {
            return this.string;
        }

        private FHIRVersion(String string) {
            this.string = string;
        }

        public static FHIRVersion parse(String value) {
            switch (value) {
            case "fhir3":
                return FHIR3;
            case "fhir4":
                return FHIR4;
            default:
                throw new RuntimeException("Unable to parse FHIR version value:" + value);
            }
        }
    }

    public static FhirContext getFhirContext(FHIRVersion fhirVersion)
        {
            switch (fhirVersion) {
                case FHIR3:
                    return FhirContext.forDstu3();
                case FHIR4:
                    return FhirContext.forR4();
                default:
                    throw new IllegalArgumentException("Unknown IG version: " + fhirVersion);
            }     
        }

    public static void convertEncoding(ConvertEncodingParameters params) {
        String resourcePath = params.resourcePath;
        FHIRVersion fhirVersion = params.fhirVersion;
        Encoding encoding = params.outputEncoding;
        //Boolean versioned = params.versioned;
        ArrayList<String> resourceDirs = params.resourceDirectoryPaths;
        FhirContext fhirContext = getFhirContext(fhirVersion);
        resourcePath = (resourcePath == null) ? null : Paths.get(resourcePath).toAbsolutePath().toString();
        for (String path : resourceDirs) {
            if(path == null) continue;
            HAPIIOUtils.resourceDirectories.add(Paths.get(path).toAbsolutePath().toString());
        }

        if (resourcePath != null) {
            convertResource(encoding, fhirContext, resourcePath);
        }

        if (!resourceDirs.isEmpty()) {
            convertResources(encoding, fhirContext, resourceDirs);
        }
    }

	private static void convertResource(Encoding encoding, FhirContext fhirContext, String resourcePath) {
        IAnyResource resource;
		try {
			resource = HAPIIOUtils.readResource(resourcePath, fhirContext);
		} catch (Exception e) {
            System.out.println(e);
            return;
		}
        HAPIIOUtils.writeResource(resource, HAPIIOUtils.getParentDirectoryPath(resourcePath), encoding, fhirContext);
    }
    
    private static void convertResources(Encoding encoding, FhirContext fhirContext, ArrayList<String> resourceDirs) {
        for (String resourcePathDirectory : resourceDirs) {
            List<String> filePaths = HAPIIOUtils.getFilePaths(resourcePathDirectory, true);
            for (String path : filePaths) {
                IAnyResource resource;
				try {
                    resource = HAPIIOUtils.readResource(path, fhirContext);
                    HAPIIOUtils.writeResource(resource, HAPIIOUtils.getParentDirectoryPath(path), encoding, fhirContext);
                    System.out.println("Successfully wrote resource " + resource.getIdElement().getIdPart() + " to " + HAPIIOUtils.getParentDirectoryPath(path));
				} catch (Exception e) {
                    System.out.println("Failed writing resource from path: " + path);
                    System.out.println(e);
                    continue;
				}
            }
        }
	}
}