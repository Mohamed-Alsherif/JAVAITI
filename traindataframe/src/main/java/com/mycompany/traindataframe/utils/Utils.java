package com.mycompany.traindataframe.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.mycompany.traindataframe.utils.HttpClientUtils.ContentStreamWithRetries;
import com.mycompany.traindataframe.utils.HttpClientUtils.Response;
import scala.Tuple2;

public class Utils {

	/**
	 * Download contents of an url as found by an HTTP GET.
	 * Note that full content resides in memory; prefer {@link #readLineByLine(URL)} to read line by line.
	 * @see #readLineByLine(URL)
	 * @see #downloadIfAbsent(URL, File)
 	 * @return the content of the represented resource
	 */
	public static String download(URL url) throws Exception {
		try {
			return downloadInt(url);
		} catch (Exception x) {
			try {
				System.out.println("Trying to recover a failed connection for " + url);
				return downloadInt(url);
			} catch (Exception y) {
				try {
					Thread.sleep(500);
					System.out.println("Trying to recover a failed connection - 2 - for " + url);
					return downloadInt(url);
				} catch (Exception z) {
					throw x;
				}
			}
		}
	}

	/**
	 * Download the content behind the URL to the given file in case file does not exist or is older than date on the server.
	 * In case destination file exists, issues a request to server with a "If-Modified-Since" header ; a 304 response does not affect the existing file.
	 * Content is directly streamed to file avoiding memory saturation in case of a large resource.
	 * @param url the URL of the resource to be downloaded
	 * @param dest the destination file
	 */
	public static void downloadIfAbsent(URL url, File dest) throws IOException {
		boolean exists = dest.exists();
		Header[] headers;
		if (exists) {
			Date lastModifiedDate = new Date(dest.lastModified());
			headers = new Header[] {new BasicHeader("If-Modified-Since", DateUtil.formatDate(lastModifiedDate))};
		} else {
			headers = new Header[0];
		}
		try (ContentStreamWithRetries in = HttpClientUtils.get200(url.toExternalForm(), 3, headers)) {
			if (exists && ! in.hasAnswer()) return; // Unchanged since last download
			File tmpFile = new File(dest.getAbsolutePath() + ".dwnld"); // Temp file in same folder so that final mv can be atomic
			tmpFile.deleteOnExit(); // this is a temp file
			try {
				Files.copy(in, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // grabbing result to temp file that might already exists
			} catch (IOException x) {
				try {
					tmpFile.delete();
				} catch (Exception y) {}
				throw x;
			}
			// Setting modification time if available
			Header lastModified = in.getResponse().getLastHeader("Last-Modified");
			if (lastModified != null) {
				try {
					Date lastModifiedDate = DateUtil.parseDate(lastModified.getValue());
					tmpFile.setLastModified(lastModifiedDate.getTime());
				} catch (DateParseException e) {
					System.err.println("Weird last-modified date sent from server for url " + url.toExternalForm() + ": " + lastModified.getValue());
				}
			}
			// Temp file to real file as atomically as possible
			try {
				Files.move(tmpFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException x) {
				Files.move(tmpFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			System.out.println(url.toExternalForm() + " downloaded to " + dest);
		}
	}

	private static String downloadInt(URL url) throws Exception {

		try {
			Response res = HttpClientUtils.get(url.toExternalForm(), null, 3);
			// System.out.println("Downloaded " + url);
			return res.getResponseAsString();
		} catch(Exception x) {
			x.printStackTrace();
			throw x;
		}
	}

	/**
	 * Iterates on the line of the textual resource as represented by its URL.
	 * Resource is found using HTTP GET.
	 * Avoids memory saturation in case of a large textual file with reasonably sized lines...
	 * @param url the url of the textual resource
	 * @return an iterator on the lines ; iterator MUST be eithetr closed or fully iterated for used resources to be freed
	 * @see #readLineByLineTupleIterator(Object, URL)
	 */
	public static CloseableIterator<String> readLineByLine(URL url) throws IOException {
		try {
			InputStream in = HttpClientUtils.get200(url.toExternalForm(), 3);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			final String first = reader.readLine();
			if (first == null) {
				in.close();
				return new CloseableIterator<String>() {
					
					@Override
					public void close() throws Exception {
						throw new NoSuchElementException();
					}
					
					@Override
					public String next() {
						return null;
					}
					
					@Override
					public boolean hasNext() {
						return false;
					}
				};
			} else {
				return new CloseableIterator<String>() {
	
					private String next = first;
	
					@Override
					public boolean hasNext() {
						return next != null;
					}
	
					@Override
					public String next() {
						if (next == null)
							throw new NoSuchElementException();
						String ret = next;
						try {
							next = reader.readLine();
						} catch (IOException e) {
							try {
								reader.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							throw new RuntimeException(e);
						}
						if (next == null) {
							try {
								reader.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						return ret;
					}

					@Override
					public void close() throws Exception {
						reader.close();
					}
				};
			}
		} catch (Exception x) {
			x.printStackTrace();
			throw x;
		}
	}

	/**
	 * Transforms a multi-line text into a list of lines.
	 * Note that all resides into memory.
	 * @see #toLineSequenceIterator(String)
	 */
	public static List<String> toLineSequence(String text) {
		StringTokenizer st = new StringTokenizer(text, "\n\r\f");
		List<String> ret = new LinkedList<>();
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			ret.add(line);
		}
		return Collections.unmodifiableList(ret);
	}

	/**
	 * Transforms a multi-line text into a list of lines.
	 * Avoids duplicating the text into memory (unlike {@link #toLineSequence(String)}.
	 * @see #toLineSequenceTupleIterator(Object, String)
	 */
	public static Iterator<String> toLineSequenceIterator(String text) {
		StringTokenizer st = new StringTokenizer(text, "\n\r\f");
		return new Iterator<String>() {

			@Override
			public boolean hasNext() {
				return st.hasMoreTokens();
			}

			@Override
			public String next() {
				return st.nextToken();
			}

		};
	}

	/**
	 * Same as {@link #readLineByLine(URL)} but returns for each line a {@link Tuple2} with key as given as parameter and value as the read line.
	 * Hence, returned tuples all have the same (given) key.
	 * @param key the (same) key for each returned tuple
	 */
	public static <K> Iterator<Tuple2<K, String>> readLineByLineTupleIterator(K key, URL url) throws IOException {
		final Iterator<String> res = readLineByLine(url);
		return new Iterator<Tuple2<K, String>>() {

			@Override
			public boolean hasNext() {
				return res.hasNext();
			}

			@Override
			public Tuple2<K, String> next() {
				return new Tuple2<K, String>(key, res.next());
			}
		};
	}

	/**
	 * Same as {@link #toLineSequenceIterator(String)} but returns for each line a {@link Tuple2} with key as given as parameter and value as the read line.
	 * Hence, returned tuples all have the same (given) key.
	 * @param key the key for each returned tuple
	 */
	public static <K> Iterator<Tuple2<K, String>> toLineSequenceTupleIterator(final K key, String text) {
		final Iterator<String> res = toLineSequenceIterator(text);
		return new Iterator<Tuple2<K, String>>() {

			@Override
			public boolean hasNext() {
				return res.hasNext();
			}

			@Override
			public Tuple2<K, String> next() {
				return new Tuple2<K, String>(key, res.next());
			}
		};
	}

	/**
	 * Transforms a text into a {@link Map} when this text represents a JSON object (that must be between '{' '}' chars).
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ?> fromJsonObject(String text)
			throws JsonParseException, JsonMappingException, IOException {
		if (null == text || "".equals(text.trim())) return new HashMap<>();
		return new ObjectMapper().readValue(text, HashMap.class);
	}

	/**
	 * Cuts a text into its different cells when this text represents a row of a <a href="https://en.wikipedia.org/wiki/Comma-separated_values">CSV</a> resource.
	 * Characters " might be used for cell content (so that the cell separator can appear within a cell value), and can be escaped by being duplicated.
	 * Note that text is duplicated into memory.
	 * @param line the <a href="https://en.wikipedia.org/wiki/Comma-separated_values">CSV</a> row
	 * @param separator the separator between cells (usually characters ,, ;, or \t)
	 */
	public static String[] fromCSV(String line, char separator) {

		if (line == null) {
			return null;
		}
		if (line.isEmpty()) {
			return new String[0];
		}

		List<String> res = new LinkedList<>();
		
		int idx = 0, start, end;
		
		while (idx < line.length()) {
			start = idx;
			
			boolean replaceDDQ = false;
			
			if ('"' == line.charAt(start)) {
				start = idx = idx+1;
				
				end = line.indexOf('"' , idx);
				
				if (end < 0) {
					end = idx = line.length();
				} else {
					while (end > 0 && end+1 < line.length() && '"' == line.charAt(end+1)) {
						end = line.indexOf('"' , end+2);
						replaceDDQ = true;
					}
					idx = line.indexOf(separator, end);
					if (idx < 0) idx = line.length();
				}
				
				
			} else {
				end = idx = line.indexOf(separator, idx);
				if (idx < 0) end = idx = line.length();
			}
			
			String cell = line.substring(start, end);
			if (replaceDDQ) cell = cell.replaceAll("\"\"", "\"");
			
			res.add(cell);
			
			idx++;
			
		}

		return res.toArray(new String[res.size()]);
	}

	public static File gzip(File inFile, boolean removeOriginal) throws IOException {
		File output = new File(inFile.getParentFile(), inFile.getName() + ".gz");
		gzip(inFile, output, removeOriginal);
		return output;
	}

	public static void gzip(File inFile, File outfile, boolean removeOriginal) throws IOException {

		try (GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(outfile))) {

			FileInputStream in = new FileInputStream(inFile);
			IOUtils.copy(in, gzos);

			if (removeOriginal) {
				inFile.delete();
			}

		}
	}

	public static byte[] unzip(byte[] inBytes) throws IOException {
		try (ByteArrayOutputStream res = new ByteArrayOutputStream()) {

			InputStream in = new GZIPInputStream(new ByteArrayInputStream(inBytes));
			IOUtils.copy(in, res);

			return res.toByteArray();
		}
	}
}
