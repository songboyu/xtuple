package common_function;

import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.io.SocketInputBuffer;

import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class httpclient {
	public httpclient() {
		// TODO Auto-generated constructor stub
	}

	public ClientConnectionManager getManager() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		PoolingClientConnectionManager pcm = new PoolingClientConnectionManager(
				schemeRegistry, 60, TimeUnit.SECONDS);
		// Increase max total connection to 500
		pcm.setMaxTotal(500);
		// Increase default max connection per route to 100
		pcm.setDefaultMaxPerRoute(100);

		return pcm;
		// cm = pcm;
		// IdleConnectionMonitorThread monitor = new
		// IdleConnectionMonitorThread(cm);
		// monitor.start();
	}

	public DefaultHttpClient getDefaultHttpClient(ClientConnectionManager cm) {
		DefaultHttpClient httpclient = new DefaultHttpClient(cm);

		httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response,
					HttpContext context) {
				return 60 * 1000;
			}
		});

		// httpclient.getParams().setParameter("http.protocol.version",
		// HttpVersion.HTTP_1_0);
		httpclient.getParams().setParameter(
				CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				60 * 1000);
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);

		return httpclient;
		// this.httpget = new HttpGet(url);
	}

	public String getResponse(DefaultHttpClient httpclient, String url,
			String encode) {
		System.out.println("Downloading ------- " + url);
		String result = null;
		final HttpGet httpget = new HttpGet(url);
		httpget.addHeader("Accept-encoding", "gzip");
		httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:28.0) Gecko/20100101 Firefox/28.0");
		httpget.addHeader("Connection", "close"); // important

		final HttpContext context = new BasicHttpContext();
		try {
			HttpResponse response = httpclient.execute(httpget, context);
			// final HttpContext context;
			HttpEntity entity = response.getEntity();


			if (entity != null
					&& response.getStatusLine().getStatusCode() == 200) {
				// boolean isChunked = entity.isChunked();
				// It seems httpclient can auto-decode chunked pages,so I can
				// just ignore it
				// Sometimes it will throws EOFException,ignore that too,the
				// program still works
				boolean isChunked = false;
				InputStream chunked = null;
				String ContentEncoding = "null";
				try {
					ContentEncoding = entity.getContentEncoding().getValue();
				} catch (NullPointerException e) {
					// do nothing
				}
				System.out.println("ContentEncoding:" + ContentEncoding);
				if (isChunked) {
					System.out.println("Chunked!!!");

				}
				// else{
				InputStream instream = entity.getContent();
				// }
				// String webEncode = entity.getContentEncoding().toString();
				System.out.println("got response");
				// System.out.println("Web encode:" + webEncode);

				// result = EntityUtils.toString(entity);
				try {
					// StringBuffer sb = new StringBuffer();
					// Scanner sc = new Scanner(instream,encode);
					// while(sc.hasNextLine())
					// {
					// sb.append(sc.nextLine());
					// System.out.println(result);
					// }
					// result = sb.toString();
					// System.out.println("read");
					StringBuffer out = new StringBuffer();
					// InputStreamReader inread = new
					// InputStreamReader(instream,encode);
					// BufferedReader in = new BufferedReader(new
					// InputStreamReader(instream,encode));

					BufferedReader in = null;
					if (ContentEncoding.equals("gzip")) {
						InputStream gzipIn = null;
						// InflaterInputStream inflate = null;
						if (isChunked) {
							chunked = new ChunkedInputStream(instream);
							gzipIn = new GZIPInputStream(chunked);
						} else {
							gzipIn = new GZIPInputStream(instream);
						}
						in = new BufferedReader(new InputStreamReader(gzipIn,
								encode));
					} else {
						// e.printStackTrace();
						System.out.println("Not in gzip format");
						in = new BufferedReader(new InputStreamReader(instream,
								encode));
					}

					char[] b = new char[8192];
					if (isChunked) {
					} else {
						try {
							for (int n; (n = in.read(b)) != -1;) {
								out.append(new String(b, 0, n));

							}
						} catch (EOFException e) {
							// do nothing
						}
					}
					// }

					result = out.toString();
					in.close();
					// gzipIn.close();
					// inread.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					instream.close();
				}
			} else if (response.getStatusLine().getStatusCode() != 200) {
				result = "HTTP Status:"
						+ response.getStatusLine().getStatusCode();
			}
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpget.abort();
			System.out.println("return");

		}
		return result;
	}

	// public abortMe()
	// {

	// }

	public String extractJsonArray(String response) {
		String result = response;

		if (result != null) {
			// String jsonText =
			// "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"id1\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"id\": null}";
			int begin = result.indexOf('=');
			if (result.endsWith(";"))
				result = result.substring(begin + 1, result.length() - 1); // get
																			// json
																			// string
			else
				result = result.substring(begin + 1);
		}
		// System.out.println(result);
		return result;
	}
}
