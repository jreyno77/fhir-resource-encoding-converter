package jreyno77.com.github.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class HAPIIOUtils 
{        
    public enum Encoding 
    { 
        CQL("cql"), JSON("json"), XML("xml"), UNKNOWN(""); 
  
        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private Encoding(String string) 
        { 
            this.string = string; 
        }

        public static Encoding parse(String value) {
            switch (value) {
                case "cql":
                    return CQL;
                case "json": 
                    return JSON;
                case "xml":
                    return XML;
                default: 
                    return UNKNOWN;
            }
        }
    } 

    public static ArrayList<String> resourceDirectories = new ArrayList<String>();

    public static String getIdFromFileName(String fileName) {
        return fileName.replaceAll("_", "-");
    }

    public static byte[] parseResource(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        if (encoding == Encoding.UNKNOWN) {
            return new byte[] { };
        }
        IParser parser = getParser(encoding, fhirContext);    
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static String parseResourceAsString(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        if (encoding == Encoding.UNKNOWN) {
            return "";
        }
        IParser parser = getParser(encoding, fhirContext);  
        return parser.setPrettyPrint(true).encodeResourceToString(resource).toString();
    }

    //users should protect against Encoding.UNKNOWN or Enconding.CQL
    private static IParser getParser(Encoding encoding, FhirContext fhirContext) 
    {
        switch (encoding) {
            case XML: 
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default: 
                throw new RuntimeException("Unknown encoding type: " + encoding.toString());
        }
    }

    public static <T extends IAnyResource> void writeResource(T resource, String path, Encoding encoding, FhirContext fhirContext) 
    {        
        try (FileOutputStream writer = new FileOutputStream(FilenameUtils.concat(path, formatFileName(resource.getIdElement().getIdPart(), encoding, fhirContext))))
        {
            writer.write(parseResource(resource, encoding, fhirContext));
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error writing Resource to file: " + e.getMessage());
        }
    }

    public static <T extends IAnyResource> void writeResources(Map<String, T> resources, String path, Encoding encoding, FhirContext fhirContext)
    {        
        for (Map.Entry<String, T> set : resources.entrySet())
        {
            writeResource(set.getValue(), path, encoding, fhirContext);
        }
    }

    //There's a special operation to write a bundle because I can't find a type that will reference both dstu3 and r4.
    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                writeResource(((org.hl7.fhir.dstu3.model.Bundle)bundle), path, encoding, fhirContext);
                break;
            case R4:
                writeResource(((org.hl7.fhir.r4.model.Bundle)bundle), path, encoding, fhirContext);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void copyFile(String inputPath, String outputPath) {
        try  {
            Path src = Paths.get(inputPath);
            Path dest = Paths.get(outputPath);
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error copying file: " + e.getMessage());
        }
    }

    public static IAnyResource readResource(String path, FhirContext fhirContext) {
        return readResource(path, fhirContext, false);
    }
    
    public static Encoding getEncoding(String path)
    {
        return Encoding.parse(FilenameUtils.getExtension(path));
    }

    //users should always check for null
    private static Map<String, IAnyResource> cachedResources = new HashMap<String, IAnyResource>();
    public static IAnyResource readResource(String path, FhirContext fhirContext, Boolean safeRead) 
    {        
        Encoding encoding = getEncoding(path);
        if (encoding == Encoding.UNKNOWN) {
            return null;
        }

        IAnyResource resource = cachedResources.get(path);     
        if (resource != null) {
            return resource;
        } 

        try
        {
            IParser parser = getParser(encoding, fhirContext);
            File file = new File(path);
            if (safeRead) {
                if (!file.exists()) {
                    return null;
                }
            }
            resource = (IAnyResource)parser.parseResource(new FileReader(file));
            cachedResources.put(path, resource);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
        return resource;
    }

    public static List<IAnyResource> readResources(List<String> paths, FhirContext fhirContext) 
    {
        List<IAnyResource> resources = new ArrayList<>();
        for (String path : paths)
        {
            IAnyResource resource = readResource(path, fhirContext);
            if (resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public static List<String> getFilePaths(String directoryPath, Boolean recursive)
    {
        List<String> filePaths = new ArrayList<String>();
        File inputDir = new File(directoryPath);
        ArrayList<File> files = inputDir.isDirectory() ? new ArrayList<File>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).orElseThrow())) : new ArrayList<File>();
       
        for (File file : files) {
            if (file.isDirectory()) {
                //note: this is not the same as anding recursive to isDirectory as that would result in directories being added to the list if the request is not recursive.
                if (recursive) {
                    filePaths.addAll(getFilePaths(file.getPath(), recursive));
                }
            }
            else {
               filePaths.add(file.getPath());
            }
        }
        return filePaths;
    }

    public static String getParentDirectoryPath(String path) {
        File file = new File(path);
        return file.getParent().toString();
    }

    public static String getFileExtension(Encoding encoding) {
        return "." + encoding.toString();
    }

    public static String formatFileName(String baseName, Encoding encoding, FhirContext fhirContext) {
        //I think this should really just be the version name i.e. DSTU3 or R4
        String igVersionToken;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                igVersionToken = "FHIR3";
                break;
            case R4:
                igVersionToken = "FHIR4";
                break;
            default:
                igVersionToken = "";
        }
        String result = baseName + getFileExtension(encoding); 
        result = result.replace("-" + igVersionToken, "_" + igVersionToken);
        return result;
    }    
}