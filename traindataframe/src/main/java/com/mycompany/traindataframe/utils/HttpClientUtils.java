package com.mycompany.traindataframe.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.input.NullInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DeflateInputStreamFactory;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.apache.http.client.entity.InputStreamFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.brotli.dec.BrotliInputStream;

public class HttpClientUtils {
	public static final class ContentStreamWithRetries extends InputStream {
		private final HttpGet request;
		private InputStream res;
		private CloseableHttpResponse rsp;
		int availableRetries;
		long position = 0;

		private ContentStreamWithRetries(int maxRetries, HttpGet request, InputStream res, CloseableHttpResponse rsp) {
			this.request = request;
			this.res = res;
			this.rsp = rsp;
			availableRetries = maxRetries;
		}
		
		HttpResponse getResponse() {
			return this.rsp;
		}
		
		/**
		 * Might return false in case of a 304 response...
		 * This imples this stream was obtained using a "last-modified" or a "if-none-match" header
		 * @see HttpClientUtils#get200(String, int, Header...)
		 */
		public boolean hasAnswer() {
			return this.res != null;
		}
		
		private void rescue(IOException x) throws IOException {

			if (availableRetries-- <= 0) throw x;
			request.reset();
			if (!isCompressed(rsp)) request.setHeader("Range", "bytes=" + this.position + "-");

			try {
				rsp.close();
				request.releaseConnection();
			} catch (Exception y) {
			}

			int retries = 3;

			while (true) {
				try {
					rsp = httpclient.execute(request, new BasicHttpContext());
					break;
				}
				catch(SocketException z) {
					if (retries-- == 0) throw x;
				}
			}
			
			switch(rsp.getStatusLine().getStatusCode()) {
			case 206:
				res = rsp.getEntity().getContent();
				break;
			case 200:
				res = rsp.getEntity().getContent();
				res.skip(this.position);
				break;
			default: throw new IOException("Error while retrying failed stream read : " + EntityUtils.toString(rsp.getEntity()), x);
			}
			
			System.err.println("Rescued URL " + request.getURI() + " to position " + this.position);
		}

		@Override
		public int read() throws IOException {
			try {
				int ret = res.read();
				if (ret != -1) position++;
				return ret;
			} catch (InterruptedIOException x) {
				this.rescue(x);

				return this.read();
			}
		}

		@Override
		public int read(byte[] b) throws IOException {
			try {
				int ret = res.read(b);
				if (ret != -1) position += ret;
				return ret;
			} catch (InterruptedIOException x) {
				this.rescue(x);

				return this.read(b);
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			try {
				int ret = res.read(b, off, len);
				if (ret != -1) position += ret;
				return ret;
			} catch (InterruptedIOException x) {
				this.rescue(x);

				return this.read(b, off, len);
			}
		}

		@Override
		public long skip(long n) throws IOException {
			try {
				long ret = res.skip(n);
				position += ret;
				return ret;
			} catch (InterruptedIOException x) {
				this.rescue(x);

				return this.skip(n);
			}
		}

		@Override
		public int available() throws IOException {
			return res.available();
		}

		@Override
		public void close() throws IOException {
			if (res == null) return;
			try {
				res.close();
			} finally {
				rsp.close();
				request.releaseConnection();
			}
		}

		@Override
		public synchronized void mark(int readlimit) {
			res.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			res.reset();
		}

		@Override
		public boolean markSupported() {
			return res.markSupported();
		}
	}

	public static class Response {
		private static Charset getCharset(HttpResponse response, Charset defaultCharset) {
			ContentType ret = ContentType.get(response.getEntity());
			return ret == null || ret.getCharset() == null ? defaultCharset : ret.getCharset();
		}
		private static String getETag(HttpResponse response) {
            Header et = response.getLastHeader("ETag");
			return et == null ? null : et.getValue();
		}

		final String url;
		final byte[] response;
		final int code;
		final String eTag;
		final Charset charset;
		private Header[] headers;

		private transient String responseStr = null;

		public Response(String url, byte[] content, HttpResponse response) {
			this(url, content, response, getCharset(response, UTF8));
		}

		public Response(String url, byte[] content, HttpResponse response, Charset charset) {
			this(url, content, response.getStatusLine().getStatusCode(),
					getETag(response), getCharset(response, charset), response.getAllHeaders());
		}

		public Response(String url, byte[] response, int code, String eTag, Header... headers) {
			this(url, response, code, eTag, UTF8, headers);
		}

		public Response(String url, byte[] response, int code, String eTag, Charset charset, Header... headers) {
			super();
			this.url = url;
			this.response = response;
			this.code = code;
			this.eTag = eTag;
			this.charset = charset;
			this.headers = headers;
		}

		public int getCode() {
			return code;
		}
		public String geteTag() {
			return eTag;
		}
		public String getResponseAsString() throws IOException {
			if (this.responseStr == null) {
				if (this.response == null || this.response.length == 0) {
					this.responseStr = "";
				}
				else {
					this.responseStr = new String(this.response, this.charset);
				}
			}

			return this.responseStr;
		}
		public Header[] getResponseHeaders() throws IOException {
			return this.headers;
		}
		public byte[] getResponseAsByteArray(){
			return response;
		}
		public Header getFirstHeader(String header) {
			for (Header h : this.headers) {
				if (h.getName().equalsIgnoreCase(header)) return h;
			}
			return null;
		}
	}

	static Logger logger = Logger.getAnonymousLogger();
	public static final Charset UTF8 = Charset.forName("UTF-8");
	private static final byte[] EMPTY_BYTES = new byte[0];
	public static final CloseableHttpClient httpclient;
	private static final Map<String, InputStreamFactory> decoders;
	public static final int REQUEST_TIMEOUT, SO_TIMEOUT, VALIDATE_TIMEOUT;

	static {
		REQUEST_TIMEOUT = 30 * 1000;
		SO_TIMEOUT = 10 * 1000;
		VALIDATE_TIMEOUT = 30 * 1000;

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(100);
		cm.setDefaultMaxPerRoute(20);
		cm.setValidateAfterInactivity(VALIDATE_TIMEOUT);

		Map<String, InputStreamFactory> _decoders = new HashMap<String, InputStreamFactory>();
		_decoders.put("gzip", GZIPInputStreamFactory.getInstance());
		_decoders.put("deflate", DeflateInputStreamFactory.getInstance());
		_decoders.put("br", new InputStreamFactory() {
			
			@Override
			public InputStream create(InputStream instream) throws IOException {
				return new BrotliInputStream(instream);
			}
		});
		decoders = Collections.unmodifiableMap(_decoders);
		
		httpclient = HttpClients
							 .custom()
							 .setConnectionManager(cm)
							 .setRetryHandler(new StandardHttpRequestRetryHandler(5, true))
							 .setDefaultRequestConfig(
									 RequestConfig
											 .custom()
											 .setSocketTimeout(SO_TIMEOUT)
											 .setConnectTimeout(REQUEST_TIMEOUT)
											 .setConnectionRequestTimeout(REQUEST_TIMEOUT)
											 .build())
							 .setContentDecoderRegistry(decoders)
							 .build();
	}
	
	private static boolean isCompressed(HttpResponse response) {
		Header ce = response.getLastHeader("Content-Encoding");
		return (ce != null) && decoders.keySet().contains(ce.getValue());
	}

	public static Response post(String url, int maxRetries, Charset charset, List<BasicNameValuePair> postParams, Header... headers)
			throws Exception {

		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(postParams));

		return grab(post, url, null, maxRetries, charset, headers);
	}

	public static Response post(String url, int maxRetries, Charset charset, String data, Header... headers)
			throws Exception {

		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(data, charset));

		return grab(post, url, null, maxRetries, charset, headers);
	}

	public static Response get(String url, String eTag, int maxRetries)
			throws Exception {

		return get(url, eTag, maxRetries, UTF8);
	}

	public static Response get(String url, String eTag, int maxRetries, Charset charset, Header... headers)
			throws Exception {

		return grab(new HttpGet(url), url, eTag, maxRetries, charset, headers);
	}

	public static ContentStreamWithRetries get200(String url, int maxRetries, Header... headers)
			throws IOException {

		HttpGet request = new HttpGet(url);

		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				request.addHeader(header);
			}
		}

		CloseableHttpResponse response;
		int retries = 3;

		while (true) {
			try {
				response = httpclient.execute(request, new BasicHttpContext());
				break;
			}
			catch(SocketException x) {
				if (retries-- == 0) throw x;
			}
		}

		boolean close = true;
		try {
			switch(response.getStatusLine().getStatusCode()) {
				case 200: {
					HttpEntity entity = response.getEntity();
					final InputStream res = entity.getContent();
					final CloseableHttpResponse rsp = response;
					close = false;
					return new ContentStreamWithRetries(maxRetries, request, res, rsp);
				}
				case 304: {
					final CloseableHttpResponse rsp = response;
					return new ContentStreamWithRetries(maxRetries, request, null, rsp);
				}
				default:
					String msg = null;
					try {
						HttpEntity entity = response.getEntity();
						byte[] ret = entity != null ? EntityUtils.toByteArray(entity) : EMPTY_BYTES;
						Response rsp = new Response(url, ret, response, UTF8);
						msg = rsp.getResponseAsString();
					} catch (Exception x) {
						x.printStackTrace();
					}
					throw new IllegalStateException("Got response " + response.getStatusLine().getStatusCode() +
															(msg == null ? "" : " (" + msg + ')') +
															" while requesting " + url);
			}
		} catch (Exception x) {
			if (maxRetries > 0) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return get200(url, maxRetries-1, headers);
			} else {
				throw x;
			}
		} finally {
			if (close)
				try {
					EntityUtils.consume(response.getEntity());
				} finally {
					response.close();
					request.releaseConnection();
				}
		}
	}

	static Response grab(HttpRequestBase request, String url, String eTag, int maxRetries, Charset charset, Header... headers)
			throws Exception {

		if (eTag != null) {
			request.addHeader("If-None-Match", eTag);
		}

		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				request.addHeader(header);
			}
		}

		CloseableHttpResponse response;
		int retries = 3;

		while (true) {
			try {
				response = httpclient.execute(request, new BasicHttpContext());
				break;
			}
			catch(SocketException x) {
				if (retries-- == 0) throw x;
			}
		}

		try {
			switch(response.getStatusLine().getStatusCode()) {
				case 200: {
					HttpEntity entity = response.getEntity();
					byte[] ret = entity != null ? EntityUtils.toByteArray(entity) : EMPTY_BYTES;
					return new Response(url, ret, response, charset);
				} case 304: {
					return new Response(url, null, response);
				} case 404: {
					return new Response(url, EMPTY_BYTES, 404, null);
				} case 204: {
					return new Response(url, EMPTY_BYTES, response);
				}
				default:
					String msg = null;
					try {
						HttpEntity entity = response.getEntity();
						byte[] ret = entity != null ? EntityUtils.toByteArray(entity) : EMPTY_BYTES;
						Response rsp = new Response(url, ret, response, charset);
						msg = rsp.getResponseAsString();
					} catch (Exception x) {
						x.printStackTrace();
					}
					throw new IllegalStateException("Got response " + response.getStatusLine().getStatusCode() +
															(msg == null ? "" : " (" + msg + ')') +
															" while requesting " + url);
			}
		} catch (Exception x) {
			if (maxRetries > 0) {
				Thread.sleep(3000);
				return get(url, eTag, maxRetries-1);
			} else {
				throw x;
			}
		} finally {
			try {
				EntityUtils.consume(response.getEntity());
			} finally {
				response.close();
				request.releaseConnection();
			}
		}
	}
}