package id.co.ibank.connector.middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

public class IbankGateway {
	
	public static void main(String[] args) {
		try {
			IbankGateway gw = new IbankGateway();
			
			//get token
//			JSONObject o = gw.getToken("223@rhino.co.id", "ibank12345");
//			String token = (String)o.get("token_id");
//			System.out.println("token --------------> "+token);
			
			//generate VA
//			JSONObject js = new JSONObject();
//			js.put("invoice_no", "");
//			js.put("name", "Rahul Punjab");
//			js.put("email", "ee.dumb@gmail.com");
//			js.put("bill_amount", "1345000.00");
//			js.put("description", "BAYAR EVENT MIDNITE PARTY");
//			
//			String va = gw.getVA("1f032c5cde57cdd430f955d293a86991", js.toString());
//			System.out.println("VA iBank --------------> "+va);
			
			//check payment status
			String token = "1f032c5cde57cdd430f955d293a86991";
			String status = gw.getPaymentStatus(token, "9880015920011102");
			System.out.println("Status VA --------------> "+status);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public String getURLServer() {
		String url = "https://sandbox.ibank.co.id";
		try {
			Properties prop = getPropertiesFile();
			if(prop!=null) {
				url = prop.getProperty("url");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public JSONObject getTokenNew(String uid, String pass) {
		String access_token = "invalid_token";
		JSONObject js = new JSONObject();
		try {
			String url_server = getURLServer();
			String api = "/ibank/apiIbank/v2.do?act=getTokenPaymentGateway";
			
			Properties prop = getPropertiesFile();
			if(prop!=null) {
				url_server = prop.getProperty("url");
			}else {
				js.put("token_id", access_token);
			}
			
			
			String a = new String(uid+";"+pass);
			//System.out.println("user+pass :: "+a);
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
			URL url = new URL(url_server+api);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			System.out.println("url : "+url);
			
			String postData = new String(encoding);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/html"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString( postData.length() ));
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			OutputStream os = connection.getOutputStream();
			os.write(postData.getBytes());
			
			int status_code = connection.getResponseCode();
			//System.out.println("status_code : "+status_code);
			
			InputStream content = null;
			if(status_code==200) {
				content = (InputStream)connection.getInputStream();
			}else {
				content = (InputStream)connection.getErrorStream();
			}
			
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader (new InputStreamReader (content));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}	
			content.close();
			
			String res = sb.toString();
			//System.out.println("res : "+res);
//			JSONObject o = new JSONObject(res);
//			String status = (String)o.getString("status");
//			System.out.println("status :: "+status);
//			if("success".equalsIgnoreCase(status)){
//				access_token = o.getString("token_id");
//			}else {
//				access_token = (String)o.getString("respon_msg");
//			}
			
			if(res!=null && res.length() > 1) {
				js = new JSONObject(res);
			}else {
				js.put("token_id", access_token);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	public JSONObject getToken(String uid, String pass) {
		String access_token = "invalid_token";
		JSONObject js = new JSONObject();
		try {
			String url_server = getURLServer();
			String api = "/ibank/apiMobile.do?mod=getIbankToken";
			
			Properties prop = getPropertiesFile();
			if(prop!=null) {
				url_server = prop.getProperty("url");
			}else {
				js.put("token_id", access_token);
			}
			
			
			String a = new String(uid+";"+pass);
			//System.out.println("user+pass :: "+a);
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
			URL url = new URL(url_server+api);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			System.out.println("url : "+url);
			
			String postData = new String(encoding);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/html"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString( postData.length() ));
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			OutputStream os = connection.getOutputStream();
			os.write(postData.getBytes());
			
			int status_code = connection.getResponseCode();
			//System.out.println("status_code : "+status_code);
			
			InputStream content = null;
			if(status_code==200) {
				content = (InputStream)connection.getInputStream();
			}else {
				content = (InputStream)connection.getErrorStream();
			}
			
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader (new InputStreamReader (content));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}	
			content.close();
			
			String res = sb.toString();
			//System.out.println("res : "+res);
//			JSONObject o = new JSONObject(res);
//			String status = (String)o.getString("status");
//			System.out.println("status :: "+status);
//			if("success".equalsIgnoreCase(status)){
//				access_token = o.getString("token_id");
//			}else {
//				access_token = (String)o.getString("respon_msg");
//			}
			
			if(res!=null && res.length() > 1) {
				js = new JSONObject(res);
			}else {
				js.put("token_id", access_token);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	
	public String getVA(String token, String data) {
		String no_va = "";
		try {
			//System.out.println("data : "+data);
			String data_client = new String(Base64.encodeBase64(data.getBytes()));
			String api = "/ibank/apiMobile.do?mod=generateVABilling&token="+token;
			String url_server = getURLServer();
			URL url = new URL(url_server+api);
			URLConnection conn = url.openConnection();
			String postData = new String(data_client);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty( "Content-Type", "text/html"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postData.length() ));
			
			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			
			InputStream is = conn.getInputStream();
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
			
			JSONObject js = new JSONObject(sb.toString());
			String success = (String)js.get("success");
			String resp_msg = (String)js.get("resp_msg");
			if("1".equalsIgnoreCase(success)) {
				JSONObject jo = (JSONObject)js.get("resp_data");
				String va_rs = (String)jo.get("no_va");
				no_va = va_rs;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return no_va;
	}
	
	public JSONObject getJsonVAResult(String token, String data) {
		JSONObject obj = new JSONObject();
		try {
			//System.out.println("token : "+token);
			//System.out.println("data : "+data);
			String data_client = new String(Base64.encodeBase64(data.getBytes()));
			String api = "/ibank/apiMobile.do?mod=generateVABilling&token="+token;
			String url_server = getURLServer();
			URL url = new URL(url_server+api);
			URLConnection conn = url.openConnection();
			String postData = new String(data_client);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty( "Content-Type", "text/html"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postData.length() ));
			
			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			
			InputStream is = conn.getInputStream();
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
			
			obj = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public String getPaymentStatus(String token, String va) {
		String status = "unpaid";
		try {
			String data_client = new String(Base64.encodeBase64(va.getBytes()));
			String url_server = getURLServer();
			String api = "/ibank/apiMobile.do?mod=checkStatusVA&token="+token;
			URL url = new URL(url_server+api);
			URLConnection conn = url.openConnection();
			String postData = new String(data_client);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty( "Content-Type", "text/html"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postData.length() ));
			
			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			
			InputStream is = conn.getInputStream();
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
			
			JSONObject js = new JSONObject(sb.toString());
			String success = (String)js.get("success");
			String resp_msg = (String)js.get("resp_msg");
			if("1".equalsIgnoreCase(success)) {
				status = (String)js.get("payment_status");
			}
//			else {
//				status = resp_msg;
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
	
	public JSONObject getJSONPaymentStatus(String token, String va) {
		JSONObject obj = new JSONObject();
		try {
			String data_client = new String(Base64.encodeBase64(va.getBytes()));
			String url_server = getURLServer();
			String api = "/ibank/apiMobile.do?mod=checkStatusVA&token="+token;
			URL url = new URL(url_server+api);
			URLConnection conn = url.openConnection();
			String postData = new String(data_client);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty( "Content-Type", "text/html"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postData.length() ));
			
			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			
			InputStream is = conn.getInputStream();
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
			
			obj = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public Properties getPropertiesFile() throws URISyntaxException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = IbankGateway.class.getClassLoader().getResourceAsStream("ibank-connector.properties");
			prop.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
}
