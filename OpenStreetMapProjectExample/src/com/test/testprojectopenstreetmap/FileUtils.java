package com.test.testprojectopenstreetmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

public class FileUtils {

	public static void deleteInsideDirectoryThread(final File dir) {
		Thread t = new Thread() {
			@Override
			public void run() {
				if (dir.isDirectory()) {
					String[] children = dir.list();
					for (int i = 0; i < children.length; i++) {
						deleteDirectory(new File(dir, children[i]));
					}
				}
			}
		};
		t.start();
	}

	public static boolean deleteInsideADirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success)
					return false;
			}
		}
		return true;
	}

	public static boolean deleteDirectory(File dir) {
		if (deleteInsideADirectory(dir) == false)
			return false;
		return dir.delete();
	}

	public static int countFilesInDirectory(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				count++;
			}
			if (file.isDirectory()) {
				count += countFilesInDirectory(file);
			}
		}
		return count;
	}

	public static boolean copyFile(InputStream src, OutputStream dest)
			throws IOException {

		byte[] buff = new byte[1024];
		int length;
		boolean result = false;

		try {
			while ((length = src.read(buff)) > 0) {
				dest.write(buff, 0, length);
			}

			result = true;
		} catch (Exception e) {
			result = false;
		} finally {
			try {
				if (dest != null) {
					try {
						dest.flush();
					} finally {
						dest.close();
					}
				}
			} finally {
				if (src != null) {
					src.close();
				}
			}
		}

		return result;
	}

	public static void extractFileToDestination(InputStream src,
			OutputStream dest) throws Exception {

		byte[] buff = new byte[8192];
		int length;

		GZIPInputStream inZip = new GZIPInputStream(src);
		BufferedInputStream in = new BufferedInputStream(inZip);
		BufferedOutputStream out = new BufferedOutputStream(dest);

		try {

			while ((length = in.read(buff)) > 0) {
				out.write(buff, 0, length);
			}

		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (src != null)
				src.close();
			if (dest != null)
				dest.close();
		}
	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

	public static String getStringFromHtmlFile(InputStream src)
			throws IOException {

		StringBuffer stringBuffer = new StringBuffer();

		try {
			int character = src.read();
			while (character > 0) {
				stringBuffer.append((char) character);
				character = src.read();
			}
		} finally {
			if (src != null) {
				src.close();
			}
		}
		return stringBuffer.toString();
	}

	public static void writeStringBufferToFile(StringBuffer stringBuffer,
			OutputStream dest) throws IOException {

		try {
			for (int i = 0; i < stringBuffer.length(); ++i) {

				dest.write(stringBuffer.charAt(i));
			}
			dest.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}
}
