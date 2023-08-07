package org.library.filegenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.library.mapper.MapDataObjects;
import org.library.utility.FileUtility;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for generating and serving files in different formats.
 */
public class GenerateFile extends MapDataObjects {

	FileUtility fileUtility = new FileUtility();
	
	

	public static final String SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String TEXT_CSV = "text/csv";

	/**
	 * Generates and serves the appropriate file format response based on input
	 * data.
	 *
	 * @param response HttpServletResponse to send the generated file as a response.
	 * @param fileName Desired filename of the generated file.
	 * @param data     Data to be written to the file.
	 * @throws IOException              If there's an issue with file generation or
	 *                                  response.
	 * @throws IllegalArgumentException If invalid arguments are passed.
	 * @throws IllegalAccessException   If there's an issue accessing class fields.
	 */
	public void generateResponse(HttpServletResponse response, String fileName, Object data)
			throws IOException, IllegalArgumentException, IllegalAccessException {

		List<Map<String, Object>> values = dataToMapObject(data);
		String fileType = fileUtility.getFileType(fileName);
		if ("xlsx".equalsIgnoreCase(fileType)) {
			generateExcelResponse(response, fileName, values);
		} else if ("csv".equalsIgnoreCase(fileType)) {
			generateCsvResponse(response, fileName, values);
		} else {
			throw new IOException("Only xlsx/csv file format is allowed");
		}
	}

	/**
	 * Generates an Excel Workbook containing data in XLSX format.
	 *
	 * @param data List of data to be written to the workbook.
	 * @return Generated XSSFWorkbook.
	 */
	private Workbook generateXLSXWorkbook(List<Map<String, Object>> data) {
		XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
		Sheet sheet = xSSFWorkbook.createSheet("Results");
		Font headerFont = xSSFWorkbook.createFont();
		headerFont.setBold(true);
		CellStyle headerCellStyle = xSSFWorkbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		Row headerRow = sheet.createRow(0);
		int columnIndex = 0;
		for (String header : data.get(0).keySet()) {
			Cell cell = headerRow.createCell(columnIndex++);
			String formattedHeader = formatColumnHeaderFromCamelCase(header);
			cell.setCellValue(formattedHeader);
			cell.setCellStyle(headerCellStyle);
		}

		int rowIndex = 1;
		for (Map<String, Object> rowData : data) {
			Row row = sheet.createRow(rowIndex++);
			columnIndex = 0;
			for (Object value : rowData.values()) {
				Cell cell = row.createCell(columnIndex++);
				cell.setCellValue(value != null ? value.toString() : "");
			}
		}

		for (int i = 0; i < data.get(0).size(); i++) {
			sheet.autoSizeColumn(i);
		}

		return xSSFWorkbook;
	}

	/**
	 * Generates an Excel response using the provided HttpServletResponse.
	 *
	 * @param response HttpServletResponse to send the generated Excel file as a
	 *                 response.
	 * @param filename Desired filename of the generated Excel file.
	 * @param data     Data to be written to the Excel file.
	 * @throws IOException If there's an issue with file generation or response.
	 */
	private void generateExcelResponse(HttpServletResponse response, String filename, List<Map<String, Object>> data)
			throws IOException {
		if (data != null && !data.isEmpty()) {
			Workbook workbook = generateXLSXWorkbook(data);
			try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
				workbook.write(outByteStream);
				byte[] outArray = outByteStream.toByteArray();
				response.setContentType(SPREADSHEET);
				response.setContentLength(outArray.length);
				response.setHeader("Expires:", "0");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
					servletOutputStream.write(outArray);
				}
				workbook.close();
			} catch (IOException e) {
				onFileException(response, "Unable to generate excel file  Error -" + e.getMessage());
			}
		}
	}

	/**
	 * Handles file-related exceptions by sending an error response.
	 *
	 * @param response HttpServletResponse to send the error response.
	 * @param message  Error message to include in the response.
	 * @throws IOException If there's an issue sending the error response.
	 */
	private void onFileException(HttpServletResponse response, String message) throws IOException {
		response.sendError(500, message);
	}

	/**
	 * Generates a CSV response using the provided HttpServletResponse.
	 *
	 * @param response HttpServletResponse to send the generated CSV file as a
	 *                 response.
	 * @param filename Desired filename of the generated CSV file.
	 * @param data     Data to be written to the CSV file.
	 * @throws IOException If there's an issue with file generation or response.
	 */
	private void generateCsvResponse(HttpServletResponse response, String filename, List<Map<String, Object>> data)
			throws IOException {

		if (data != null && !data.isEmpty()) {
			response.setContentType(TEXT_CSV);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
			String[] header = data.get(0).keySet().toArray(new String[data.get(0).size()]);
			for (int i = 0; i < header.length; i++) {
				header[i] = formatColumnHeaderFromCamelCase(header[i]);
			}
			CSVFormat csvFormat = CSVFormat.Builder.create().setHeader(header).setAllowMissingColumnNames(true).build();
			try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), csvFormat)) {
				for (Map<String, Object> rowData : data) {
					csvPrinter.printRecord(rowData.values());
				}
			} catch (IOException e) {
				onFileException(response, "Unable to generate file Error -" + e.getMessage());
			}
		}
	}

	/**
	 * Formats a column header from CamelCase to uppercase words separated by
	 * spaces.
	 *
	 * @param columnHeader Original column header.
	 * @return Formatted column header.
	 */
	private String formatColumnHeaderFromCamelCase(String columnHeader) {
		return columnHeader.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("[_-]+", " ").toUpperCase();
	}

}
