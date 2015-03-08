/*
 *  实际连接目标url的类
 */
package linkcrawler.connectors;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
public class HTTPConnector {
	private String urlToConnect;	
	public HTTPConnector(String urlToConnect) {
		this.urlToConnect = urlToConnect;
	}	
	@SuppressWarnings("unused")
	public HttpURLConnection getConnection() throws IOException, MalformedURLException
	{
		URL resourceURL = new URL(urlToConnect);      
		if(resourceURL.getProtocol().trim().toLowerCase().equals("https"))  //安全https处理
		{
			SSLContext sc = null;
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
				}
			};		
			try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}catch (KeyManagementException e) {
				e.printStackTrace();
			}
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};			
			HttpsURLConnection connectionToResource = (HttpsURLConnection) resourceURL.openConnection();
			connectionToResource.setRequestMethod("GET");
			connectionToResource.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
			connectionToResource.setRequestProperty("Host", resourceURL.getHost());
			connectionToResource.setRequestProperty("Path", resourceURL.getPath());
			connectionToResource.setRequestProperty("Connection", "keep-alive");
			connectionToResource.setRequestProperty("Accept-Language", "en-US,en;q=0.8,es;q=0.6");
			connectionToResource.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			connectionToResource.setRequestProperty("Accept", "*/*");
			connectionToResource.connect();
			return connectionToResource;	        
		}
		else
		{
			HttpURLConnection connectionToResource = (HttpURLConnection) resourceURL.openConnection();
			connectionToResource.setRequestMethod("GET");
			connectionToResource.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
	        connectionToResource.setRequestProperty("Host", resourceURL.getHost());
	        connectionToResource.setRequestProperty("Path", resourceURL.getPath());
	        connectionToResource.setRequestProperty("Connection", "keep-alive");
	        connectionToResource.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
	        connectionToResource.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
	        connectionToResource.setRequestProperty("Accept", "*/*");
	        connectionToResource.setReadTimeout(5000);//读取数据超时
	        connectionToResource.setConnectTimeout(5000) ;// socket连接超时	        
	        connectionToResource.connect();
	        return connectionToResource;
		}
	}
}
