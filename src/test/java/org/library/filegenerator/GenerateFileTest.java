package org.library.filegenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.utility.FileUtility;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

class GenerateFileTest {

	@Mock
	FileUtility fileUtility;
	GenerateFile generateFile;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		generateFile = new GenerateFile();
	}

	@Test
	void testGenerateResponseInvalidFileType() throws IOException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		String fileName = "test.txt";
		List<String> data = new ArrayList<>();
		data.add(getData());

		when(fileUtility.getFileType(fileName)).thenReturn("txt");

		assertThrows(IOException.class, () -> generateFile.generateResponse(response, fileName, data));

		verify(response, never()).setContentType(anyString());
		verify(response, never()).setContentLength(anyInt());
		verify(response, never()).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
		verify(response, never()).getOutputStream();
		verify(response, never()).getWriter();
	}

	@Test
	void testGenerateResponseNullData() throws IOException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		String fileName = "test.xlsx";

		when(fileUtility.getFileType(fileName)).thenReturn("xlsx");

		List<String> data = null;
		NullPointerException exception = assertThrows(NullPointerException.class, () -> {
			generateFile.generateResponse(response, fileName, data);
		});

		assertEquals("Data cannot be null or empty", exception.getMessage());

		verify(response, never()).setContentType(anyString());
		verify(response, never()).setContentLength(anyInt());
		verify(response, never()).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
		verify(response, never()).getOutputStream();
		verify(response, never()).getWriter();
	}

	@Test
	void testGenerateExcelResponseIOException() throws IOException {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
		String fileName = "test.xlsx";
		List<String> data = new ArrayList<>();
		data.add(getData());

		when(fileUtility.getFileType(fileName)).thenReturn("xlsx");
		doThrow(new IOException("Test Exception")).when(servletOutputStream).write(any(byte[].class));

		when(response.getOutputStream()).thenReturn(servletOutputStream);
		doThrow(new IOException("Test Exception")).when(response).sendError(eq(500), anyString());

		assertThrows(IOException.class, () -> generateFile.generateResponse(response, fileName, data));

		verify(response, times(1)).sendError(eq(500), anyString());
	}

	String getData() {
		return "{\r\n" + "        \"data\": \"Click Here\",\r\n" + "        \"size\": 36,\r\n"
				+ "        \"style\": \"bold\",\r\n" + "        \"name\": \"text1\",\r\n"
				+ "        \"hOffset\": 250,\r\n" + "        \"vOffset\": 100,\r\n"
				+ "        \"alignment\": \"center\",\r\n"
				+ "        \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\"\r\n" + "    }";
	}

}
