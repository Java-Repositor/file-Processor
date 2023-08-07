package org.library.filereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.library.utility.FileUtility;

/**
 * Utility class for reading various file formats and converting data to a list
 * of map objects.
 */

public class ReadFile {

	private final FileUtility fileUtility = new FileUtility();

	/**
	 * Reads data from an input stream and returns it as a list of map objects.
	 *
	 * @param inputFile InputStream of the file to be read.
	 * @param fileName  Name of the file including extension.
	 * @return List of map objects containing the read data.
	 * @throws IOException If an I/O error occurs.
	 */

	public List<Map<String, Object>> readFileAsList(InputStream inputFile, String fileName) throws IOException {
		String fileType = fileUtility.getFileType(fileName);
		if ("xlsx".equalsIgnoreCase(fileType)) {
			try (XSSFWorkbook workbook = new XSSFWorkbook(inputFile)) {
				return readWorkbook(workbook);
			}
		} else if ("csv".equalsIgnoreCase(fileType)) {
			return readCsvFile(inputFile);
		} else {
			throw new IOException("Only xlsx/csv file format is allowed");
		}
	}

	/**
	 * Reads data from a file path and returns it as a list of map objects.
	 *
	 * @param path Path to the file.
	 * @return List of map objects containing the read data.
	 * @throws IOException If an I/O error occurs.
	 */

	public List<Map<String, Object>> readFileFromPath(String path) throws IOException {
		File file = new File(path);
		try (FileInputStream fis = new FileInputStream(file)) {
			String fileType = fileUtility.getFileType(file.getName());
			if ("xlsx".equalsIgnoreCase(fileType)) {
				try (XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
					return readWorkbook(workbook);
				}
			} else if ("csv".equalsIgnoreCase(fileType)) {
				return readCsvFile(fis);
			} else {
				throw new IOException("Only xlsx/csv file format is allowed");
			}
		}
	}

	/**
	 * Reads an Excel workbook and converts its data to a list of map objects.
	 *
	 * @param workbook Workbook to be read.
	 * @return List of map objects containing the read data.
	 */

	private List<Map<String, Object>> readWorkbook(Workbook workbook) {
		List<Map<String, Object>> modelMap = new LinkedList<>();
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter formatter = new DataFormatter();

		Row headerRow = sheet.getRow(0);
		List<String> header = readHeaderRow(headerRow, formatter);

		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row currentRow = sheet.getRow(rowIndex);
			Map<String, Object> rowMap = readRowData(currentRow, header, formatter);
			modelMap.add(rowMap);
		}

		return modelMap;
	}

	/**
	 * Reads the header row of a sheet and returns a list of formatted column
	 * headers.
	 *
	 * @param headerRow Header row of the sheet.
	 * @param formatter DataFormatter for formatting cell values.
	 * @return List of formatted column headers.
	 */

	private List<String> readHeaderRow(Row headerRow, DataFormatter formatter) {
		List<String> header = new ArrayList<>();
		for (Cell cell : headerRow) {
			if (cell != null) {
				String data = formatter.formatCellValue(cell);
				header.add(headerToCamelCase(data));
			}
		}
		return header;
	}

	/**
	 * Reads the data of a row and returns a map containing column names and cell
	 * values.
	 *
	 * @param currentRow Row to be read.
	 * @param header     List of column headers.
	 * @param formatter  DataFormatter for formatting cell values.
	 * @return Map containing column names and cell values.
	 */

	private Map<String, Object> readRowData(Row currentRow, List<String> header, DataFormatter formatter) {
		Map<String, Object> rowMap = new LinkedHashMap<>();
		for (int cellIndex = 0; cellIndex < header.size(); cellIndex++) {
			Cell currentCell = currentRow.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
			String columnName = header.get(cellIndex);

			if (currentCell != null) {
				Object cellValue;
				if (currentCell.getCellType() == CellType.FORMULA) {
					cellValue = getFormulaCellValue(currentCell);
				} else {
					cellValue = formatter.formatCellValue(currentCell);
				}
				rowMap.put(columnName, cellValue);
			} else {
				rowMap.put(columnName, "");
			}
		}
		return rowMap;
	}

	/**
	 * Gets the formula cell's value based on its formula result type.
	 *
	 * @param formulaCell Formula cell to be processed.
	 * @return Formula cell's value.
	 */

	private Object getFormulaCellValue(Cell formulaCell) {
		CellType formulaResultType = formulaCell.getCachedFormulaResultType();
		switch (formulaResultType) {
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(formulaCell)) {
				return formulaCell.getDateCellValue();
			}
			return formulaCell.getNumericCellValue();
		case STRING:
			return formulaCell.getStringCellValue();
		case BOOLEAN:
			return formulaCell.getBooleanCellValue();
		case ERROR:
			return ErrorEval.getText(formulaCell.getErrorCellValue());
		default:
			return "";
		}
	}

	/**
	 * Reads data from a CSV file and converts it to a list of map objects.
	 *
	 * @param inputFile InputStream of the CSV file.
	 * @return List of map objects containing the read data.
	 * @throws IOException If an I/O error occurs.
	 */

	private List<Map<String, Object>> readCsvFile(InputStream inputFile) throws IOException {
		try (CSVParser csvParser = new CSVParser(new InputStreamReader(inputFile), CSVFormat.DEFAULT)) {
			List<CSVRecord> csvRows = csvParser.getRecords();
			if (csvRows.isEmpty()) {
				return Collections.emptyList();
			}
			List<String> header = readCsvHeader(csvRows.get(0));
			return csvRows.stream().skip(1).map(row -> readCsvRecord(row, header)).toList();
		}
	}

	/**
	 * Reads the header row of a CSV file and returns a list of formatted column
	 * headers.
	 *
	 * @param headerRecord CSVRecord of the header row.
	 * @return List of formatted column headers.
	 */

	private List<String> readCsvHeader(CSVRecord headerRecord) {
		return StreamSupport.stream(headerRecord.spliterator(), false).map(this::headerToCamelCase).toList();
	}

	/**
	 * Reads a CSV record and returns a map containing column names and cell values.
	 *
	 * @param csvRow CSVRecord to be read.
	 * @param header List of column headers.
	 * @return Map containing column names and cell values.
	 */

	private Map<String, Object> readCsvRecord(CSVRecord csvRow, List<String> header) {
		Map<String, Object> rowMap = new LinkedHashMap<>();
		IntStream.range(0, header.size()).forEach(i -> {
			String columnName = header.get(i);
			String cellValue = csvRow.get(i);
			rowMap.put(columnName, cellValue);
		});
		return rowMap;
	}

	/**
	 * Converts a formatted string to camel case.
	 *
	 * @param formattedString Formatted string to be converted.
	 * @return Camel case string.
	 */

	private String headerToCamelCase(String formattedString) {
		String[] words = formattedString.toLowerCase().split("[\\s_-]");
		StringBuilder camelCaseBuilder = new StringBuilder(words[0]);
		Arrays.stream(words, 1, words.length).forEach(
				word -> camelCaseBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)));
		return camelCaseBuilder.toString();
	}
}
