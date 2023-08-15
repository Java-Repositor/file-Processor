# File Generation and Reading Library

> ***This library provides utility classes for generating and reading files in different formats (Excel and CSV).***

## Classes Overview

### GenerateFile.class

> ***Utility class for generating and serving files in XLSX & CSV formats.***

### Method:

I. **generateResponse**(HttpServletResponse response, String fileName, Object data)

### Descr: 
> ***Generates and serves the appropriate file format response based on input data***

### Inputs:

1. HttpServletResponse - import jakarta.servlet.http.HttpServletResponse

2. fileName - String value (ex. sample.xlsx | sample.csv)

3. Object data - data should be either List<classObject> | List <JsonString> | Map<>

### Output: 

file (sample.xlsx | sample.csv)

### ReadFile.class

> ***Utility class for reading various file formats and converting data to a list of map objects.***

### Method:

I. **readFileAsList**(InputStream inputFile, String fileName)

### Descr:  

> ***Reads data from an input stream and returns it as a list of map objects.***

### Inputs:

1.  InputStream - Send the file as inputStream ex. Get the input stream from multipart body request using MulitpartBody.getInputStream()

2. fileName - String value (ex. sample.xlsx | sample.csv) ex. Get the filename from the multipart body using MulitpartBody.getOriginalFilename()

II. **readFileFromPath**(String path)

### Descr: 

> ***Reads data from the file from the local drive and returns it as a list of map objects.***

### Inputs:

1. Path - String path (ex: "path_to_generated_excel/sample.xlsx" |"path_to_generated_excel/sample.xlsx")

 ### NOTE

>  ***Generated data from the file will have camel case key (ex: if the file contains one of the column names as **SAMPLE_HEADER**, header key will sampleHeader) same goes for file generate (ex: Key  sampleHeader => SAMPLE_HEADER)***


## Example Programm

import org.library.filegenerator.GenerateFile;

import org.library.filereader.ReadFile;

import org.library.mapper.MapDataObjects;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

public class FileProcessingExample {

    public static void main(String[] args) {
        GenerateFile generateFile = new GenerateFile();
        ReadFile readFile = new ReadFile();
        MapDataObjects mapDataObjects = new MapDataObjects();

        try {
            // Generate and serve an Excel file
            HttpServletResponse excelResponse = getMockHttpServletResponse();
            String excelFileName = "sample.xlsx";
            List<Map<String, Object>> excelData = getDataForExcel();
            generateFile.generateResponse(excelResponse, excelFileName, excelData);

            // Generate and serve a CSV file
            HttpServletResponse csvResponse = getMockHttpServletResponse();
            String csvFileName = "sample.csv";
            List<Map<String, Object>> csvData = getDataForCsv();
            generateFile.generateResponse(csvResponse, csvFileName, csvData);

            // Read and convert the generated Excel file
            String excelFilePath = "path_to_generated_files/sample.xlsx"; // Replace with actual file path
            List<Map<String, Object>> convertedExcelData = readFile.readFileFromPath(excelFilePath);
            List<Map<String, Object>> excelMapObjects = mapDataObjects.dataToMapObject(convertedExcelData);

            // Read and convert the generated CSV file
            String csvFilePath = "path_to_generated_files/sample.csv"; // Replace with actual file path
            List<Map<String, Object>> convertedCsvData = readFile.readFileFromPath(csvFilePath);
            List<Map<String, Object>> csvMapObjects =       mapDataObjects.dataToMapObject(convertedCsvData);

            // Print converted data from Excel
            System.out.println("Converted Excel Data:");
            excelMapObjects.forEach(System.out::println);

            // Print converted data from CSV
            System.out.println("Converted CSV Data:");
            csvMapObjects.forEach(System.out::println);
        } catch (IOException | IllegalArgumentException | IllegalAccessException e) {
            System.err.println("Error: " + e.getMessage());
        }
}

    // Simulate generating sample data for Excel
    private static List<Map<String, Object>> getDataForExcel() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("Name", "John Doe");
        row1.put("Age", 30);
        row1.put("City", "New York");
        data.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("Name", "Jane Smith");
        row2.put("Age", 25);
        row2.put("City", "Los Angeles");
        data.add(row2);

        return data;
    }

    // Simulate generating sample data for CSV
    private static List<Map<String, Object>> getDataForCsv() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("Product", "Laptop");
        row1.put("Price", 999.99);
        row1.put("Stock", 10);
        data.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("Product", "Smartphone");
        row2.put("Price", 599.99);
        row2.put("Stock", 20);
        data.add(row2);

        return data;
    }}

