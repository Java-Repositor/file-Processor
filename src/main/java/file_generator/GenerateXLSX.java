package file_generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import mapper.MapClassObjects;
import javax.inject.Inject;

public class GenerateXLSX {

	@Inject
	MapClassObjects mapObject;

	public static final String SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public void generateExcel(HttpServletResponse response, String fileName, Object data) throws IOException {
		List<Map<String, Object>> values = mapObject.dataToMapObject(data);
		generateExcelResponse(response, fileName, values);
	}

	private Workbook generateXLSXWorkbook(List<Map<String, Object>> data) {
		XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
		Sheet sheet = xSSFWorkbook.createSheet("Results");
		Font headerFont = xSSFWorkbook.createFont();
		headerFont.setBold(true);
		CellStyle headerCellStyle = xSSFWorkbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		Row headerRow = sheet.createRow(0);
		for (int j = 0; j < data.get(0).size(); j++) {
			Cell cell = headerRow.createCell(j);
			String headerVal = formatColumnHeaderFromCamelCase(((Map) data.get(0)).keySet().toArray()[j].toString());
			cell.setCellValue(headerVal);
			cell.setCellStyle(headerCellStyle);
		}
		for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
			Row row = sheet.createRow(rowIndex + 1);
			for (int columnIndex = 0; columnIndex < data.get(rowIndex).size(); columnIndex++)
				row.createCell(columnIndex)
						.setCellValue(((data.get(rowIndex)).values().toArray()[columnIndex] != null)
								? (data.get(rowIndex)).values().toArray()[columnIndex].toString()
								: "null");
		}
		for (int i = 0; i < data.get(0).size(); i++)
			sheet.autoSizeColumn(i);
		return xSSFWorkbook;
	}

	public void generateExcelResponse(HttpServletResponse response, String filename, List<Map<String, Object>> data)
			throws IOException {
		try {
			if (data != null && !data.isEmpty()) {
				Workbook workbook = generateXLSXWorkbook(data);
				ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
				workbook.write(outByteStream);
				byte[] outArray = outByteStream.toByteArray();
				response.setContentType(SPREADSHEET);
				response.setContentLength(outArray.length);
				response.setHeader("Expires:", "0");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				ServletOutputStream servletOutputStream = response.getOutputStream();
				servletOutputStream.write(outArray);
				servletOutputStream.flush();
				workbook.close();
			}
		} catch (IOException e) {
			onFileException(response, "Unable to generate excel file", e);
		}
	}

	public void onFileException(HttpServletResponse response, String message, Throwable e) {
		try {
			response.sendError(500, message);
		} catch (IOException iOException) {
		}
	}

	private String formatColumnHeaderFromCamelCase(String s) {
		return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ").toUpperCase();
	}
}
