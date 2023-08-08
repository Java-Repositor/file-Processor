package org.library.filereader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.utility.FileUtility;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReadFileTest {

	@InjectMocks
	private ReadFile readFile;

	@Mock
	private FileUtility fileUtility;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		readFile = new ReadFile();
		readFile.fileUtility = fileUtility;
	}

	@Test
	void testReadFileAsListWithUnsupportedFormat() {
		InputStream mockInputStream = mock(InputStream.class);

		when(fileUtility.getFileType(anyString())).thenReturn("txt");

		assertThrows(IOException.class, () -> readFile.readFileAsList(mockInputStream, "test.txt"));
	}

	@Test
	void testReadFileFromPathWithInvalidFile() {
		when(fileUtility.getFileType(anyString())).thenReturn("xlsx");

		assertThrows(IOException.class, () -> readFile.readFileFromPath("nonexistent.xlsx"));
	}

	public void testReadFileAsListIOException() throws IOException {
		InputStream mockInputStream = mock(InputStream.class);

		when(fileUtility.getFileType(anyString())).thenReturn("xlsx");
		when(mockInputStream.available()).thenThrow(new IOException("Test Exception"));

		assertThrows(IOException.class, () -> readFile.readFileAsList(mockInputStream, "test.xlsx"));
	}

	@Test
	void testReadFileFromPathIOException() throws IOException {
		when(fileUtility.getFileType(anyString())).thenReturn("csv");

		assertThrows(IOException.class, () -> readFile.readFileFromPath("test.csv"));
	}

	@Test
	void testReadFileAsListWithEmptyFile() throws IOException {
		InputStream mockInputStream = mock(InputStream.class);

		when(fileUtility.getFileType("empty.xlsx")).thenReturn("xlsx");
		when(mockInputStream.available()).thenReturn(0);

		assertTrue(readFile.readFileFromPath("src/test/resources/testfiles/empty.xlsx").isEmpty());
	}

	@Test
	void testReadFileFromPathWithEmptyFile() throws IOException {
		when(fileUtility.getFileType("empty.csv")).thenReturn("csv");

		assertTrue(readFile.readFileFromPath("src/test/resources/testfiles/empty.csv").isEmpty());
	}

	@Test
	void testReadFileAsListWithCsv() throws IOException {
		InputStream mockInputStream = getClass().getResourceAsStream("/testfiles/test.csv");

		when(fileUtility.getFileType(anyString())).thenReturn("csv");

		assertFalse(readFile.readFileAsList(mockInputStream, "test.csv").isEmpty());
	}

	@Test
	void testReadFileFromPathWithXlsx() throws IOException {
		when(fileUtility.getFileType("test.xlsx")).thenReturn("xlsx");

		assertFalse(readFile.readFileFromPath("src/test/resources/testfiles/test.xlsx").isEmpty());
	}

}
