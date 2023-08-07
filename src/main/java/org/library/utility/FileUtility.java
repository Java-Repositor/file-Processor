package org.library.utility;

/**
 * A utility class for working with files.
 */
public class FileUtility {

	/**
	 * Retrieves the file type extension from a given file name.
	 *
	 * @param fileName The name of the file including its extension.
	 * @return The file type extension as a string.
	 */
	public String getFileType(String fileName) {
		String[] fileParts = fileName.split("\\.");
		return fileParts[fileParts.length - 1];
	}
}
