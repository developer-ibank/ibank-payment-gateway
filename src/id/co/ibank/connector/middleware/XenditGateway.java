package id.co.ibank.connector.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class XenditGateway {
	DecimalFormat df = new DecimalFormat("#,##0.00");
	DecimalFormat df2 = new DecimalFormat("#0");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	SimpleDateFormat sdfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String endpoin = "https://api.xendit.co/ewallets/charges";
	public static String apiKey = "xnd_development_zoI8LfvfAZi8HnsObxtwqcoQXW21sEAFJJn1as1e6yFXs4GshxtMPr08LL8Ls7";
	public static String password = "";
	public static String checkoutMethod = "ONE_TIME_PAYMENT";
	public static String successRedirectURL = "https://sandbox.ibankpos.id/portal/responPaymentDigital.do?tid=${tid}";
	public static String callbackURL = "https://sandbox.ibankpos.id/xenditCallback.do";
	public static Integer maxLengthName = 100;
	
	
	public static void main(String[] args) {
		try {
			XenditGateway xendit = new XenditGateway();
			Calendar cal = Calendar.getInstance();
			String trxCode = String.valueOf(cal.getTimeInMillis());
			double amount = 204550;
			String channelCode = "ALFAMART";
			String mobileNo = "082297403279";
			String custName = "dadang";
			
			//create ewallet charge
			//JSONObject obj = xendit.getEwalletChargeStatus("ewc_03364332-e931-41e4-93b3-cdb71dffc569", "579076");
			//JSONObject obj = xendit.getFixPaymentCodeStatus("619c7905f8b7ad110a1106ef");
			//JSONObject obj = xendit.getInvoiceDetail("619768d1d7e5590650f22dae");
//			JSONObject obj = xendit.simulatePayRetailOutlet("227722374253", amount, "579076");
			
			List items = new LinkedList();
			JSONObject jsItems = new JSONObject();
			jsItems.put("name", "	");
			jsItems.put("quantity", 1);
			jsItems.put("price", 220890);
			jsItems.put("reference_id", "PAKET-25M");
			jsItems.put("category", "Internet Service");
			jsItems.put("currency", "IDR");
			jsItems.put("type", "PRODUCT ");
			items.add(jsItems);
			
			
//			JSONObject obj = xendit.createInvoice(trxCode, amount, channelCode, "dudunng", items, new LinkedList(), "579076");
//			JSONObject obj = xendit.createEwalletCharge(trxCode, amount, channelCode, mobileNo, "web", "579076", true, items);
			
//			JSONObject obj = xendit.createVirtualAccount(trxCode, amount, "BSI", custName, true, null, true, "579076");
			
			amount = 333895;
//			xendit.simulatePaymentVirtualAccount("FP_579076_0922_0028", amount, "579076");
//			xendit.simulatePaymentVAByAccountNumber("9999126916", amount, "MANDIRI", "579076");
			
//			JSONObject obj = xendit.checkDetailVirtualAccount("ccc3d956-e8fa-440d-b432-21c8605fa9bf", "579076");
			
//			JSONObject obj = xendit.createFixPaymentCode(trxCode, amount, channelCode, custName, "579076");
////			
//			System.out.println("obj : "+obj);
			
			Map m =  xendit.readConfigFile("011006");
			System.out.println("Map ---------- "+m);
			
			double adminCharge = xendit.getAdminCharge(m, "ALFAMART", 100000);
			System.out.println("adminCharge ---------- "+adminCharge);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public JSONObject receiveCallback(String tipe, String data) {
		JSONObject js = new JSONObject();
		js.put("trx_status", JSONObject.NULL);
		js.put("trx_source", JSONObject.NULL);
		js.put("trx_amount", JSONObject.NULL);
		js.put("channel_code", JSONObject.NULL);
		js.put("reff_no", JSONObject.NULL);
		js.put("payment_date", JSONObject.NULL);
		try {
			if(!isNullOrEmpty(tipe)) {
				if(!isNullOrEmpty(data)) {
					Calendar cal = Calendar.getInstance();
					
					JSONObject objData = new JSONObject(data);
					if("ewallet".equalsIgnoreCase(tipe)) {
						JSONObject ob = objData.getJSONObject("data");
						String status = ob.getString("status");
						String reference_id = ob.getString("reference_id");
						Double capture_amount = ob.getDouble("capture_amount");
						String channel_code = ob.getString("channel_code");
						String reffNumber = ob.getString("id");
						String updated = ob.getString("updated");
						cal.setTime(sdfISO8601.parse(updated));
						cal.add(Calendar.HOUR_OF_DAY, 7);
						
						js.put("trx_status", status);
						js.put("trx_source", reference_id);
						js.put("trx_amount", capture_amount);
						js.put("channel_code", channel_code);
						js.put("reff_no", reffNumber);
						js.put("payment_date", sdf2.format(cal.getTime()));
						
					}else if("virtual_account".equalsIgnoreCase(tipe)) {
						if(!objData.isNull("external_id")) {
							String reference_id = objData.getString("external_id");
							if(reference_id.indexOf("_") > -1) {
								reference_id = reference_id.replaceAll("_", "/");
							}
							
							String payment_id = objData.getString("payment_id");
							String va_resp = objData.getString("account_number");
							String merchant_code = objData.getString("merchant_code");
							if(!isNullOrEmpty(merchant_code)) va_resp = merchant_code+va_resp;
							String bank_code_resp = objData.getString("bank_code");
							Double amount_resp = objData.getDouble("amount");
							
							js.put("trx_status", va_resp);
							js.put("trx_source", reference_id);
							js.put("trx_amount", amount_resp);
							js.put("channel_code", bank_code_resp);
							js.put("reff_no", payment_id);
							js.put("payment_date", sdf2.format(cal.getTime()));
						}
						
					}else if("invoice".equalsIgnoreCase(tipe)) {
						String status = objData.getString("status");
						String reference_id = objData.getString("external_id");
						Double capture_amount = objData.getDouble("paid_amount");
						String channel_code = objData.getString("payment_channel");
						String reffNumber = objData.getString("id");
						String paid_at = objData.getString("paid_at");
						cal.setTime(sdfISO8601.parse(paid_at));
						cal.add(Calendar.HOUR_OF_DAY, 7);
						
						js.put("trx_status", status);
						js.put("trx_source", reference_id);
						js.put("trx_amount", capture_amount);
						js.put("channel_code", channel_code);
						js.put("reff_no", reffNumber);
						js.put("payment_date", sdf2.format(cal.getTime()));
						
					}else if("retail".equalsIgnoreCase(tipe)) {
						String status = objData.getString("status");
						String reference_id = objData.getString("external_id");
						Double amount = objData.getDouble("amount");
						String retail_outlet_name = objData.getString("retail_outlet_name");
						String reffNumber = objData.getString("payment_id");
						String paid_at = objData.getString("transaction_timestamp");
						cal.setTime(sdfISO8601.parse(paid_at));
						cal.add(Calendar.HOUR_OF_DAY, 7);
						
						js.put("trx_status", status);
						js.put("trx_source", reference_id);
						js.put("trx_amount", amount);
						js.put("channel_code", retail_outlet_name);
						js.put("reff_no", reffNumber);
						js.put("payment_date", sdf2.format(cal.getTime()));
					
					}else if("qris".equalsIgnoreCase(tipe)) {
						String status = objData.getString("status");
						String reference_id = objData.getString("external_id");
						String type = objData.getString("type");
						Double amount = new Double(0);
						if(!objData.isNull("nominal")) {
							amount = objData.getDouble("nominal");
						}
						String retail_outlet_name = objData.getString("event");
						String reffNumber = objData.getString("id");
						if(!isNullOrEmpty(type) && "DYNAMIC".equalsIgnoreCase(type)) {
							if(!objData.isNull("payment_detail")) {
								JSONObject details = objData.getJSONObject("payment_detail");
								reffNumber = details.getString("receipt_id");
								retail_outlet_name = details.getString("source");
							}
						}
						String paid_at = objData.getString("created");
						cal.setTime(sdfISO8601.parse(paid_at));
						cal.add(Calendar.HOUR_OF_DAY, 7);
						
						js.put("trx_status", status);
						js.put("trx_source", reference_id);
						js.put("trx_amount", amount);
						js.put("channel_code", retail_outlet_name);
						js.put("reff_no", reffNumber);
						js.put("payment_date", sdf2.format(cal.getTime()));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Get billing status by payment method
	 * @param trxId
	 * @param paymentType
	 * @return
	 */
	public JSONObject getBillingStatusByPaymentType(String trxId, String paymentType, String orgId) {
		JSONObject js = new JSONObject();
		js.put("billing_status", "UNPAID");
		try {
			if(!isNullOrEmpty(paymentType)) {
				if("ewallet".equalsIgnoreCase(paymentType)) {
					JSONObject rs = getEwalletChargeStatus(trxId, orgId);
					js.put("resp_log", rs);
					if(!rs.isNull("status")) {
						String status = rs.getString("status");
						if("SUCCEEDED".equalsIgnoreCase(status)) {
							double payment_amount = rs.getDouble("capture_amount");
							String ref_id = rs.getString("id");
							String channel_code = rs.getString("channel_code");
							
							js.put("billing_status", "PAID");
							js.put("payment_amount", payment_amount);
							js.put("payment_date", sdf2.format(new Date()));
							js.put("payment_ref_id", ref_id);
							js.put("payment_channel", channel_code);
						}
					}
				}else if("invoice".equalsIgnoreCase(paymentType)) {
					JSONObject rs = getInvoiceDetail(trxId, orgId);
					js.put("resp_log", rs);
					if(!rs.isNull("status")) {
						String status = rs.getString("status");
						if("PAID".equalsIgnoreCase(status) || "SETTLED".equalsIgnoreCase(status)) {
							double payment_amount = rs.getDouble("amount");
							String ref_id = rs.getString("id");
							String payment_channel = rs.getString("payment_channel");
							String paid_at = rs.getString("paid_at");
							Calendar cal = Calendar.getInstance();;
							cal.setTime(sdfISO8601.parse(paid_at));
							cal.add(Calendar.HOUR_OF_DAY, 7);
							
							js.put("billing_status", "PAID");
							js.put("payment_amount", payment_amount);
							js.put("payment_date", sdf2.format(cal.getTime()));
							js.put("payment_ref_id", ref_id);
							js.put("payment_channel", payment_channel);
						}
					}
				}else if("retail_outlet".equalsIgnoreCase(paymentType)) {
					JSONObject rs = getFixPaymentCodeStatus(trxId, orgId);
					js.put("resp_log", rs);
					if(!rs.isNull("status")) {
						String status = rs.getString("status");
						if("SETTLING".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status) || "INACTIVE".equalsIgnoreCase(status)) {
							double payment_amount = rs.getDouble("expected_amount");
							String ref_id = rs.getString("id");
							String retail_outlet_name = rs.getString("retail_outlet_name");
							
							js.put("billing_status", "PAID");
							js.put("payment_amount", payment_amount);
							js.put("payment_date", sdf2.format(new Date()));
							js.put("payment_ref_id", ref_id);
							js.put("payment_channel", retail_outlet_name);
						}
					}
				}else if("virtual_account".equalsIgnoreCase(paymentType)) {
					JSONObject rs = checkDetailVirtualAccount(trxId, orgId);
					js.put("resp_log", rs);
					if(!rs.isNull("status")) {
						String status = rs.getString("status");
						if("INACTIVE".equalsIgnoreCase(status)) {
							double payment_amount = 0;
							String ref_id = rs.getString("id");
							String bank_code = rs.getString("bank_code");
							String expiration_date = rs.getString("expiration_date");
							Date exp = sdfISO8601.parse(expiration_date);
							Calendar cal = Calendar.getInstance();
							if(exp.after(cal.getTime())) {
								if(!rs.isNull("expected_amount")) {
									payment_amount = rs.getDouble("expected_amount");
								}
								
								js.put("billing_status", "PAID");
								js.put("payment_amount", payment_amount);
								js.put("payment_date", sdf2.format(cal.getTime()));
								js.put("payment_ref_id", ref_id);
								js.put("payment_channel", bank_code);
							}
						}
					}
				}else if("qris".equalsIgnoreCase(paymentType)) {
					JSONObject rs = getQRDetail(trxId, orgId);
					js.put("resp_log", rs);
					if(!rs.isNull("status")) {
						String status = rs.getString("status");
						if("INACTIVE".equalsIgnoreCase(status)) {
							double payment_amount = 0;
							if(!rs.isNull("nominal")) {
								payment_amount = rs.getDouble("nominal");
							}
							String ref_id = rs.getString("id");
							Calendar cal = Calendar.getInstance();
							
							js.put("billing_status", "PAID");
							js.put("payment_amount", payment_amount);
							js.put("payment_date", sdf2.format(cal.getTime()));
							js.put("payment_ref_id", ref_id);
							js.put("payment_channel", "QRIS");
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	public void simulateQRPayment(String extId, double amount, String orgId) {
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_qr");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_qris");
					password = (String)map.get("password");
				}
			}
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+extId+"/payments/simulate");
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
			conn.setRequestProperty("Content-Type", "application/json"); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			JSONObject data = new JSONObject();
			data.put("amount", amount);
			
			OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONArray getQRPaymentHistory(String extId, String startDate, String endDate, int limit, String orgId) {
		JSONArray rs = new JSONArray();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_qr");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_qris");
					password = (String)map.get("password");
				}
			}
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/payments?external_id="+extId+"&from="+startDate+"&to="+endDate+"&limit="+limit);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			//System.out.println("sb.toString() : "+sb.toString());
			rs = new JSONArray(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public JSONObject getQRDetail(String extId, String orgId) {
		JSONObject rs = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_qr");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_qris");
					password = (String)map.get("password");
				}
			}
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+extId);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			rs = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public JSONObject createQRCode(String extId, double amount, String qrType, String orgId) {
		JSONObject rs = new JSONObject();
		try {
			boolean autoPay = false;
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_qr");
					password = prop.getProperty("password");
					checkoutMethod = prop.getProperty("checkout_method");
					callbackURL = prop.getProperty("callback_url");
					String auto_pay_qris = prop.getProperty("auto_pay_qris");
					if(!isNullOrEmpty(auto_pay_qris) && "1".equalsIgnoreCase(auto_pay_qris)) {
						autoPay = true;
					}
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_qris");
					password = (String)map.get("password");
					checkoutMethod = (String)map.get("checkout_method");
					callbackURL = (String)map.get("callback_url");
					String auto_pay_qris = (String)map.get("auto_pay_qris");
					if(!isNullOrEmpty(auto_pay_qris) && "1".equalsIgnoreCase(auto_pay_qris)) {
						autoPay = true;
					}
				}
			}
			
			JSONObject data = new JSONObject();
			data.put("external_id", extId);
			data.put("amount", amount);
			data.put("type", qrType);
			data.put("callback_url", callbackURL);
	    	
			System.out.println("REQ QR Xendit : "+data);
	    	rs.put("req", data);
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json"); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			System.out.println("RESP QR Xendit : "+sb.toString());
			JSONObject resp = new JSONObject(sb.toString()); 
			rs.put("resp", resp);
			
			if(autoPay) {
				Thread.sleep(1*1000);
				if(!resp.isNull("status")) {
					String status = resp.getString("status");
					if("ACTIVE".equalsIgnoreCase(status)) {
						double rsAmount = resp.getDouble("amount");
						simulateQRPayment(extId, rsAmount, orgId);
					}
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public JSONObject checkDetailVirtualAccount(String idVa,  String orgId) {
		JSONObject rs = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_virtual_account");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_virtual_account");
					password = (String)map.get("password");
				}
			}
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+idVa);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic "+baseAuth); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			
			rs = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public void simulatePaymentVAByAccountNumber(String accountNo, double amount, String bankCode, String orgId) {
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_virtual_account");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_virtual_account");
					password = (String)map.get("password");
				}
			}
			
			JSONObject data = new JSONObject();
			data.put("transfer_amount", amount);
			data.put("bank_account_number", accountNo);
			data.put("bank_code", bankCode);
			System.out.println("REQ simulatePaymentVAByAccountNumber : "+data);
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			if(endpoin.indexOf("callback") > -1) {
				endpoin = endpoin.substring(0, endpoin.indexOf("callback")-1);
			}
			URL url = new URL(endpoin+"/pool_virtual_accounts/simulate_payment");
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json"); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			
			boolean repeat = false;
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
				repeat = true;
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			System.out.println("RESP simulatePaymentVAByAccountNumber : "+sb.toString());
			
			//repeat process if not succeed
//			if(repeat) {
//				simulatePaymentVAByAccountNumber(accountNo, amount, bankCode, orgId);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void simulatePaymentVirtualAccount(String extId, double amount, String orgId) {
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_virtual_account");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_virtual_account");
					password = (String)map.get("password");
				}
			}
			
			JSONObject data = new JSONObject();
			data.put("amount", amount);
			System.out.println("REQ simulatePaymentVirtualAccount : "+data);
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/external_id="+extId+"/simulate_payment");
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json"); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			
			boolean repeat = false;
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
				repeat = true;
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			System.out.println("RESP simulatePaymentVirtualAccount : "+sb.toString());
			
			if(repeat) {
				simulatePaymentVirtualAccount(extId, amount, orgId);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject createVirtualAccount(String trxCode, double amount, String bankCode, String customerName, boolean isClose, String vaNumber, boolean isSingleUse, String orgId) {
		JSONObject js = new JSONObject();
		String result = "000000";
		try {
			int exp_in_minutes = 60;
			int exp_in_second = 0;
			boolean auto_pay = false;
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_virtual_account");
					password = prop.getProperty("password");
					String invoice_duration = prop.getProperty("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_virtual_account");
					password = (String)map.get("password");
					String invoice_duration = (String)map.get("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
					String auto_pay_virtual_account = (String)map.get("auto_pay_virtual_account");
					if(!isNullOrEmpty(auto_pay_virtual_account) && "1".equalsIgnoreCase(auto_pay_virtual_account)) {
						auto_pay = true;
					}
					
					JSONObject jo = getChannelName(map, bankCode);
					String active_link_duration = jo.getString("active_link_duration");
					if(!isNullOrEmpty(active_link_duration)) {
						double duration = Double.parseDouble(active_link_duration);
						if(duration < 1) {
							exp_in_second = (int)Math.floor(duration*60);
						}else {
							exp_in_minutes = Integer.parseInt(active_link_duration);
						}
					}
					String max_length_name = jo.getString("max_length_name");
					if(!isNullOrEmpty(max_length_name)) maxLengthName = Integer.parseInt(max_length_name);
					if(maxLengthName > 0) {
						if(customerName.indexOf("-") > -1) {
							customerName = customerName.replaceAll("-", "").replaceAll("[0-9]", "");
							customerName = customerName.trim();
						}
						if(customerName.length() > maxLengthName) {
							customerName = customerName.substring(0, maxLengthName);
						}
					}
				}
			}
			
			if(trxCode.indexOf("/") > -1) {
				trxCode = trxCode.replaceAll("/", "_");
			}
			
			JSONObject data = new JSONObject();
		    data.put("external_id", trxCode);
		    data.put("bank_code", bankCode);
		    data.put("name", customerName);
		    if(isClose) {
		    	data.put("is_closed", true);
		    	data.put("expected_amount", df2.format(amount));
		    	data.put("is_single_use", isSingleUse);
		    }
		    if(!isNullOrEmpty(vaNumber)) {
		    	data.put("virtual_account_number", vaNumber);
		    }
		    
		    Calendar cal = Calendar.getInstance();
			if(exp_in_second > 0) {
				cal.add(Calendar.SECOND, exp_in_second);
			}else {
				cal.add(Calendar.MINUTE, exp_in_minutes);
			}
			data.put("expiration_date", sdfISO8601.format(cal.getTime()));
		    
		    System.out.println("REQ to Xendit----- "+data);
		    js.put("req", data);
		   
			URL url = new URL(endpoin);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			String a = new String(apiKey+":"+password);
			
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty ("Authorization", "Basic " + encoding);
	        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
	        
	        OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			InputStream is = null;
			int rc = conn.getResponseCode();
			System.out.println("rc ----- "+rc);
			if(rc==200 || rc==202) {
				is = conn.getInputStream();
			}else {
				is = conn.getErrorStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
	        System.out.println("RESP from Xendit----- "+sb);
	        js.put("resp", sb.toString());
			
			if(!isNullOrEmpty(sb.toString())) {
				JSONObject rs = new JSONObject(sb.toString());
				js.put("resp", rs);
				if(!rs.isNull("status")) {
					String status = rs.getString("status");
					if("PENDING".equalsIgnoreCase(status)) {
						result = rs.getString("account_number");
						
						if(auto_pay) {
							if(!rs.isNull("account_number")) {
								double trx_amount = rs.getDouble("expected_amount");
								Thread.sleep(4*1000);
								simulatePaymentVirtualAccount(trxCode, trx_amount, orgId);
							}
						}
						
					}
				}
			}
			System.out.println("---result ::: "+result);
			js.put("result", result);
			js.put("exp_date_rs", sdf2.format(cal.getTime()));
			js.put("is_redirect", "0");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	public JSONObject simulatePayRetailOutlet(String payCode, double amount, String orgId) {
		JSONObject js = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_retail_outlets");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_retail_outlet");
					password = (String)map.get("password");
				}
			}
			
			
			JSONObject data = new JSONObject();
			data.put("retail_outlet_name", "ALFAMART");
			data.put("payment_code", payCode);
			data.put("transfer_amount", amount);
			System.out.println("REQ : "+data);
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/simulate_payment");
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty ("Authorization", "Basic " + baseAuth);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json"); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			js = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Get Detail Fixed Payment Code
	 * @param trxId : ID fix payment code to retrieve detail
	 * @return JSONObject
	 */
	public JSONObject getFixPaymentCodeStatus(String trxId, String orgId) {
		JSONObject js = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_retail_outlets");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_retail_outlet");
					password = (String)map.get("password");
				}
			}
			
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+trxId);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic "+baseAuth); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			
			js = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Create Payment Code Retail Outlets (ALFAMART, INDOMARET)
	 * @param String trxCode
	 * @param double amount
	 * @param String channelCode
	 * @param String customerName
	 * @return JSONObject
	 */
	public JSONObject createFixPaymentCode(String trxCode, double amount, String channelCode, String customerName, String orgId) {
		JSONObject js = new JSONObject();
		String result = "000000";
		try {
			boolean autoPay = false;
			int exp_in_minutes = 60;
			int exp_in_second = 0;
			
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_retail_outlets");
					password = prop.getProperty("password");
					checkoutMethod = prop.getProperty("checkout_method");
					successRedirectURL = prop.getProperty("success_redirect_url");
					String invoice_duration = prop.getProperty("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
					String auto_pay_retail_outlet = prop.getProperty("auto_pay_retail_outlet");
					if(!isNullOrEmpty(auto_pay_retail_outlet) && "1".equalsIgnoreCase(auto_pay_retail_outlet)) {
						autoPay = true;
					}
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_retail_outlet");
					password = (String)map.get("password");
					checkoutMethod = (String)map.get("checkout_method");
					successRedirectURL = (String)map.get("success_redirect_url");
					String invoice_duration = (String)map.get("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
					String auto_pay_retail_outlet = (String)map.get("auto_pay_retail");
					if(!isNullOrEmpty(auto_pay_retail_outlet) && "1".equalsIgnoreCase(auto_pay_retail_outlet)) {
						autoPay = true;
					}
					
					JSONObject jo = getChannelName(map, channelCode);
					String active_link_duration = jo.getString("active_link_duration");
					if(!isNullOrEmpty(active_link_duration)) {
						double duration = Double.parseDouble(active_link_duration);
						if(duration < 1) {
							exp_in_second = (int)Math.floor(duration*60);
						}else {
							exp_in_minutes = Integer.parseInt(active_link_duration);
						}
					}
					
					System.out.println("customerName 1 ----------- "+customerName);
					String max_length_name = jo.getString("max_length_name");
					System.out.println("max_length_name --------- "+max_length_name);
					if(!isNullOrEmpty(max_length_name)) maxLengthName = Integer.parseInt(max_length_name);
					if(maxLengthName > 0) {
						System.out.println("maxLengthName : "+maxLengthName+" ------------ customerName.length() : "+customerName.length());
						if(customerName.length() > maxLengthName) {
							customerName = customerName.substring(0, maxLengthName);
						}
					}
				}
			}
			
			System.out.println("customerName 2 ----------- "+customerName);
			JSONObject data = new JSONObject();
		    data.put("external_id", trxCode);
		    data.put("retail_outlet_name", channelCode);
		    data.put("name", customerName);
		    data.put("expected_amount", amount);
		    
		    Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			//cal.add(Calendar.MINUTE, exp_in_minutes);
			if(exp_in_second > 0) {
				cal.add(Calendar.SECOND, exp_in_second);
			}else {
				cal.add(Calendar.MINUTE, exp_in_minutes);
			}
			sdfISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
		    data.put("expiration_date", sdfISO8601.format(cal.getTime()));
		    data.put("is_single_use", true);
		    
		    System.out.println("REQ Xendit ----- "+data);
		    js.put("req", data);
			
			URL url = new URL(endpoin);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			String a = new String(apiKey+":"+password);
			
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty ("Authorization", "Basic " + encoding);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
	        
	        OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			InputStream is = null;
			int rc = conn.getResponseCode();
			System.out.println("RC Xendit ----- "+rc);
			if(rc==200 || rc==202) {
				is = conn.getInputStream();
			}else {
				is = conn.getErrorStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
	        System.out.println("RESP Xendit----- "+sb);
	        js.put("resp", sb.toString());
			
			if(!isNullOrEmpty(sb.toString())) {
				JSONObject rs = new JSONObject(sb.toString());
				js.put("resp", rs);
				if(!rs.has("error_code")) {
					result = (String)rs.get("payment_code");
					js.put("result", result);
					js.put("exp_date_rs", sdf2.format(cal.getTime()));
					js.put("is_redirect", "0");
					
					if(autoPay) {
						if(!rs.isNull("payment_code")) {
							double trx_amount = rs.getDouble("expected_amount");
							Thread.sleep(2*1000);
							simulatePayRetailOutlet(result, trx_amount, orgId);
						}
					}
				}
			}
			System.out.println("---result ::: "+result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Get Invoice Detail
	 * @param invoice_id : invoice id to retrieve detail
	 * @return JSONObject
	 */
	public JSONObject getInvoiceDetail(String invoice_id, String orgId) {
		JSONObject js = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_invoice");
					password = prop.getProperty("password");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_invoice");
					password = (String)map.get("password");
				}
			}
			
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+invoice_id);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic "+baseAuth); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			
			js = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Create Invoice
	 * @param String trxCode
	 * @param double amount
	 * @param String channelCode
	 * @param String customerName
	 * @param List items
	 * @param List fees
	 * @return JSONObject
	 */
	public JSONObject createInvoice(String trxCode, double amount, String channelCode, String customerName, List items, List fees, String orgId, boolean addCharge, JSONObject dataCustomer, String billingDescription) {
		JSONObject js = new JSONObject();
		String result = "000000";
		try {
			double admin_charge = 0;
			int exp_in_minutes = 60;
			int exp_in_second = 0;
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_invoice");
					password = prop.getProperty("password");
					checkoutMethod = prop.getProperty("checkout_method");
					successRedirectURL = prop.getProperty("success_redirect_url");
					String invoice_duration = prop.getProperty("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_invoice");
					password = (String)map.get("password");
					checkoutMethod = (String)map.get("checkout_method");
					successRedirectURL = (String)map.get("success_redirect_url");
					String invoice_duration = (String)map.get("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
					if(!"credit_card".equalsIgnoreCase(channelCode)) {
						JSONObject jo  = getChannelName(map, channelCode);
						String active_link_duration = jo.getString("active_link_duration");
						if(!isNullOrEmpty(active_link_duration)) {
							double duration = Double.parseDouble(active_link_duration);
							if(duration < 1) {
								exp_in_second = (int)Math.floor(duration*60);
							}else {
								exp_in_minutes = Integer.parseInt(active_link_duration);
							}
						}
					}
					
					if(addCharge) {
						admin_charge = getAdminCharge(map, channelCode, amount);
						amount += admin_charge;
						
						JSONObject jsItems = new JSONObject();
						jsItems.put("name", "Biaya Admin");
						jsItems.put("quantity", 1);
						jsItems.put("price", df2.format(admin_charge));
						items.add(jsItems);
					}
				}
			}
			
			JSONObject data = new JSONObject();
		    data.put("external_id", trxCode);
		    data.put("currency", "IDR");
		    data.put("amount", amount);
		   
		    List payments = new ArrayList(); 
		    String channel = channelCode;
			if("credit_card".equalsIgnoreCase(channelCode)) {
				channel = channelCode;
			}else {
				String[] sp = channelCode.split("_");
				channel = sp[1];
			}
			payments.add(channel);
		    data.put("payment_methods", payments);
		    if(!isNullOrEmpty(billingDescription)) {
		    	data.put("description", billingDescription);
		    }else {
		    	data.put("description", "Invoice "+trxCode+" a/n. "+customerName);
		    }
		    data.put("invoice_duration", exp_in_minutes*60);
		    
		    Calendar cal = Calendar.getInstance();
		    if(exp_in_second > 0) {
				cal.add(Calendar.SECOND, exp_in_second);
			}else {
				cal.add(Calendar.MINUTE, exp_in_minutes);
			}
		    
		    //items
		    if(items.size() > 0) {
		    	data.put("items", items);
		    }
		    
		    //fee
		    if(fees.size() > 0) {
		    	data.put("fees", fees);
		    }
		    
		    //Data Customer
		    if(dataCustomer!=null &&  dataCustomer.length() > 1) {
		    	data.put("customer", dataCustomer);
		    }
		    
		    System.out.println("REQ xendit ----- "+data);
		    js.put("req", data);
		    
		    if(successRedirectURL.indexOf("${tid}") > -1) {
				String tid = new String(Base64.encodeBase64(trxCode.getBytes()));
				successRedirectURL = successRedirectURL.replace("${tid}", tid);
			}
	    	data.put("success_redirect_url", successRedirectURL);
	    	
	    	System.out.println("endpoin ----- "+endpoin);
			URL url = new URL(endpoin);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			String a = new String(apiKey+":"+password);
			
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty ("Authorization", "Basic " + encoding);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
	        
	        OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			InputStream is = null;
			int rc = conn.getResponseCode();
			System.out.println("RC Xendit ----- "+rc);
			if(rc==200 || rc==202) {
				is = conn.getInputStream();
			}else {
				is = conn.getErrorStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
	        System.out.println("RESP xendit ----- "+sb);
	        js.put("resp", sb.toString());
			
			if(!isNullOrEmpty(sb.toString())) {
				JSONObject rs = new JSONObject(sb.toString());
				js.put("resp", rs);
				result = (String)rs.get("invoice_url");
				js.put("exp_date_rs", sdf2.format(cal.getTime()));
				js.put("is_redirect", "1");
			}
			System.out.println("---result ::: "+result);
			js.put("result", result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Check eWallet Charge Status
	 * @param String chargeId : parameter "id" from create ewallet charge response
	 * @return JSONObject
	 */
	public JSONObject getEwalletChargeStatus(String charge_id, String orgId) {
		JSONObject js = new JSONObject();
		try {
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_ewallet");
					password = prop.getProperty("password");
					checkoutMethod = prop.getProperty("checkout_method");
					successRedirectURL = prop.getProperty("success_redirect_url");
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_ewallet");
					password = (String)map.get("password");
					checkoutMethod = (String)map.get("checkout_method");
					successRedirectURL = (String)map.get("success_redirect_url");
				}
			}
			
			String baseAuth = new String(Base64.encodeBase64((apiKey+":").getBytes()));
			URL url = new URL(endpoin+"/"+charge_id);
			System.out.println("endpoin : "+url);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic "+baseAuth); 
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			int status_code = conn.getResponseCode();
			System.out.println("status_code : "+status_code);
			InputStream is = null;
			if(status_code > 201) {
				is = (InputStream)conn.getErrorStream();
			}else {
				is = (InputStream)conn.getInputStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			//System.out.println(" RESP : "+sb.toString());
			
			js = new JSONObject(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return js;
	}
	
	/**
	 * Xendit Payment Gateway -- Create eWallet Charge (DANA, SHOPEEPAY, OVO)
	 * 
	 * 
	 * @param String trxCode
	 * @param double amount
	 * @param String channelCode
	 * @param String mobileNo
	 * @param String platform
	 * @return JSONObject
	 */
	public JSONObject createEwalletCharge(String trxCode, double amount, String channelCode, String mobileNo, String platform, String orgId, boolean addCharge, List basket) {	
		JSONObject response = new JSONObject();
		String result = "000000";
		try {
			double admin_charge = 0;
			int exp_in_minutes = 60;
			int exp_in_second = 0;
			if(isNullOrEmpty(orgId)) {
				Properties prop = getPropertiesFile();
				if(prop!=null) {
					apiKey = prop.getProperty("api_key");
					endpoin = prop.getProperty("url_create_trx_ewallet");
					password = prop.getProperty("password");
					checkoutMethod = prop.getProperty("checkout_method");
					successRedirectURL = prop.getProperty("success_redirect_url");
					String invoice_duration = prop.getProperty("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
				}
			}else {
				Map map = readConfigFile(orgId);
				if(map.size() > 0) {
					apiKey = (String)map.get("api_key");
					endpoin = (String)map.get("url_create_trx_ewallet");
					password = (String)map.get("password");
					checkoutMethod = (String)map.get("checkout_method");
					successRedirectURL = (String)map.get("success_redirect_url");
					String invoice_duration = (String)map.get("invoice_duration_in_minutes");
					if(!isNullOrEmpty(invoice_duration)) exp_in_minutes = Integer.parseInt(invoice_duration);
					
					if(addCharge) {
						admin_charge = getAdminCharge(map, channelCode, amount);
						amount += admin_charge;
					}
					
					JSONObject jo  = getChannelName(map, channelCode);
					String active_link_duration = jo.getString("active_link_duration");
					if(!isNullOrEmpty(active_link_duration)) {
						double duration = Double.parseDouble(active_link_duration);
						if(duration < 1) {
							exp_in_second = (int)Math.floor(duration*60);
						}else {
							exp_in_minutes = Integer.parseInt(active_link_duration);
						}
					}
				}
			}
			
			if(successRedirectURL.indexOf("${tid}") > -1) {
				String tid = new String(Base64.encodeBase64(trxCode.getBytes()));
				successRedirectURL = successRedirectURL.replace("${tid}", tid);
			}
		    
			
	    	JSONObject data = new JSONObject();
		    data.put("reference_id", trxCode);
		    data.put("currency", "IDR");
		    data.put("amount", Math.ceil(amount));
		    data.put("checkout_method", checkoutMethod);
		    data.put("channel_code", channelCode);
		    
		    JSONObject cp = new JSONObject();
		    if(channelCode.indexOf("OVO")>-1) {
		    	if(!isNullOrEmpty(mobileNo)) {
		    		if(mobileNo.length() > 14) {
			    		mobileNo = mobileNo.substring(0, 14);
			    	}
		    		String number = mobileNo;
		    		String str = mobileNo.substring(0,1);
		    		if("0".equalsIgnoreCase(str)) number = "+62"+number.substring(1);
		    		mobileNo = number;
		    		cp.put("mobile_number", mobileNo);
		    	}
		    }
		   
		    if(channelCode.indexOf("SHOPEEPAY")>-1 || channelCode.indexOf("DANA")>-1 || channelCode.indexOf("LINKAJA")>-1) {
		    	cp.put("success_redirect_url", successRedirectURL);
		    }
		    data.put("channel_properties", cp);
		    
		    //basket
		    if(basket.size() > 0) {
		    	data.put("basket", basket);
		    }
		    
		    System.out.println("REQ xendit----- "+data);
		    response.put("req", data);
			
			URL url = new URL(endpoin);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			String a = new String(apiKey+":"+password);
			
			String encoding = new String(Base64.encodeBase64(a.getBytes())); 
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty ("Authorization", "Basic " + encoding);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);
	        
	        OutputStream os = conn.getOutputStream();
			os.write(data.toString().getBytes());
			InputStream is = null;
			int rc = conn.getResponseCode();
			System.out.println("RC xendit ----- "+rc);
			if(rc==200 || rc==202) {
				is = conn.getInputStream();
			}else {
				is = conn.getErrorStream();
			}
			
			int read = -1;
			StringBuffer sb = new StringBuffer();
			while((read = is.read()) > -1) {
				sb.append((char)read);
			}
			is.close();
			os.close();
	        System.out.println("RESP xendit----- "+sb);
	        response.put("resp", sb.toString());
			
			if(!isNullOrEmpty(sb.toString())) {
				JSONObject rs = new JSONObject(sb.toString());
				response.put("resp", rs);
				boolean is_redirect = (Boolean)rs.get("is_redirect_required");
				if(!is_redirect) {
					result = "1";
				}else {
					Object action = (Object)rs.get("actions");
					if(action!=null) {
						JSONObject action_rs = rs.getJSONObject("actions");
						if(action_rs!=null) {
							Object objAction1 = (Object)action_rs.get("desktop_web_checkout_url");
							Object objAction2 = (Object)action_rs.get("mobile_web_checkout_url");
							Object objAction3 = (Object)action_rs.get("mobile_deeplink_checkout_url");
							if(!isNullOrEmpty(platform)) {
								if("mobile".equalsIgnoreCase(platform)) {
									if(!isNullOrEmpty(objAction3.toString()) && !"null".equalsIgnoreCase(objAction3.toString())) {
										result = objAction3.toString();
									}else {
										if(!isNullOrEmpty(objAction2.toString()) && !"null".equalsIgnoreCase(objAction2.toString())) {
											result = objAction2.toString();
										}else {
											result = objAction1.toString();
										}
									}
								}else {
									if(!isNullOrEmpty(objAction1.toString()) && !"null".equalsIgnoreCase(objAction1.toString())) {
										result = objAction1.toString();
									}else {
										if(!isNullOrEmpty(objAction2.toString()) && !"null".equalsIgnoreCase(objAction2.toString())) {
											result = objAction2.toString();
										}else {
											result = objAction3.toString();
										}
									}
								}
							}else {
								result = objAction1.toString();
							}	
						}
					}
				}
			}
			
			response.put("result", result);
			if("1".equalsIgnoreCase(result)) {
				response.put("is_redirect", "0");
			}else {
				response.put("is_redirect", "1");
			}
			
			Calendar cal = Calendar.getInstance();
			if(exp_in_second > 0) {
				cal.add(Calendar.SECOND, exp_in_second);
			}else {
				cal.add(Calendar.MINUTE, exp_in_minutes);
			}
		    
		    response.put("exp_date_rs", sdf2.format(cal.getTime()));
		    
		    System.out.println("RESP create ewallet charge -------- "+response);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static boolean isNullOrEmpty(String value) {
		return (value == null || value.trim().length() == 0 ? true : false);
	}
	
	public String getURLServer() {
		String url = "https://app.sandbox.midtrans.com/snap/v1/transactions";
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
	
	public Properties getPropertiesFile() throws URISyntaxException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = IbankGateway.class.getClassLoader().getResourceAsStream("xendit-gateway.properties");
			prop.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	
	
	public Map readConfigFile(String orgId) throws IOException {
		Map map = new HashMap();
		InputStream is = null;
		try {
			is = IbankGateway.class.getClassLoader().getResourceAsStream("xendit-gateway-config.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			if (doc != null) {
				String url_create_ewallet = XPathAPI.eval(doc, "xendit/config/url-create-trx-ewallet").toString();
				String url_create_invoice = XPathAPI.eval(doc, "xendit/config/url-create-trx-invoice").toString();
				String url_create_retail_outlet = XPathAPI.eval(doc, "xendit/config/url-create-trx-retail-outlet").toString();
				String url_create_qris = XPathAPI.eval(doc, "xendit/config/url-create-qris").toString();
				String url_create_virtual_account = XPathAPI.eval(doc, "xendit/config/url-create-virtual-account").toString();
				String callback_url = XPathAPI.eval(doc, "xendit/config/callback-url").toString();
				
				String api_key = XPathAPI.eval(doc, "xendit/config/api-key").toString();
				String password = new String();
				Object pwd_obj =  XPathAPI.eval(doc, "xendit/config/password");
				if(pwd_obj!=null) {
					password = XPathAPI.eval(doc, "xendit/config/password").toString();
				}
				String checkout_method = XPathAPI.eval(doc, "xendit/config/checkout-method").toString();
				String success_redirect_url = XPathAPI.eval(doc, "xendit/config/success-redirect-url").toString();
				String callback_token = XPathAPI.eval(doc, "xendit/config/callback-token").toString();
				String invoice_duration = XPathAPI.eval(doc, "xendit/config/invoice-duration-in-minutes").toString();
				String auto_pay_retail = XPathAPI.eval(doc, "xendit/config/auto-pay-retail-outlet").toString();
				String auto_pay_va = XPathAPI.eval(doc, "xendit/config/auto-pay-virtual-account").toString();
				String auto_pay_qris = XPathAPI.eval(doc, "xendit/config/auto-pay-qris").toString();
				String fee_tax_value = XPathAPI.eval(doc, "xendit/config/fee-tax-value").toString();
				String max_length_name = new String();
				
				map.put("url_create_trx_ewallet", url_create_ewallet);
				map.put("url_create_trx_invoice", url_create_invoice);
				map.put("url_create_trx_retail_outlet", url_create_retail_outlet);
				map.put("url_create_qris", url_create_qris);
				map.put("url_create_virtual_account", url_create_virtual_account);
				map.put("callback_url", callback_url);
				
				map.put("api_key", api_key);
				map.put("password", password);
				map.put("checkout_method", checkout_method);
				map.put("success_redirect_url", success_redirect_url);
				map.put("callback_token", callback_token);
				map.put("invoice_duration_in_minutes", invoice_duration);
				map.put("auto_pay_retail", auto_pay_retail);
				map.put("auto_pay_virtual_account", auto_pay_va);
				map.put("auto_pay_qris", auto_pay_qris);
				map.put("fee_tax_value", fee_tax_value);
				
				if(isNullOrEmpty(orgId)) {
					List paymentList = new LinkedList();
					NodeIterator nitr2 = XPathAPI.selectNodeIterator(doc, "xendit//config//payment-methods//channel-groups");
					Node node2 = null;
					while((node2 = nitr2.nextNode())!=null){
						String groupId = XPathAPI.eval(node2, "@group-id").toString();
						String groupName = XPathAPI.eval(node2, "@group-name").toString();
						String groupStatus = XPathAPI.eval(node2, "@group-status").toString();
						
						Map groupMap = new HashMap();
						groupMap.put("group_id", groupId);
						groupMap.put("group_name", groupName);
						
						List channelList = new LinkedList();
						NodeIterator nitr3 = XPathAPI.selectNodeIterator(node2, "channel");
						Node node3 = null;
						while((node3 = nitr3.nextNode())!=null){
							String channelId = XPathAPI.eval(node3, "@id").toString();
							String channelName = XPathAPI.eval(node3, "@name").toString();
							String channelPriority = XPathAPI.eval(node3, "@priority").toString();
							String channelStatus = XPathAPI.eval(node3, "@status").toString();
							String chargeType = XPathAPI.eval(node3, "@charge-type").toString();
							String activeLinkDuration = invoice_duration;
							String eval = XPathAPI.eval(node3, "@active-link-duration").str();
							if(!isNullOrEmpty(eval)) {
								activeLinkDuration = XPathAPI.eval(node3, "@active-link-duration").toString();
							}
							String maxLength = XPathAPI.eval(node3, "@max-length-name").str();
							if(!isNullOrEmpty(maxLength)) {
								max_length_name = XPathAPI.eval(node3, "@max-length-name").toString();
							}
							String channelImg = new String();
							String evalImg = XPathAPI.eval(node3, "@img").str();
							if(!isNullOrEmpty(evalImg)) {
								channelImg = XPathAPI.eval(node3, "@img").toString();
							}
							
							String percentage_charge = "0";
							String value_charge = "0";
							if("percentage".equalsIgnoreCase(chargeType)) {
								percentage_charge = XPathAPI.selectSingleNode(node3, "charge-percentage-value").getFirstChild().getTextContent();
							}else if("fixValue".equalsIgnoreCase(chargeType)) {
								value_charge = XPathAPI.selectSingleNode(node3, "charge-fix-value").getFirstChild().getTextContent();
							}else if("fixValueAndPercentage".equalsIgnoreCase(chargeType)) {
								percentage_charge = XPathAPI.selectSingleNode(node3, "charge-percentage-value").getFirstChild().getTextContent();
								value_charge = XPathAPI.selectSingleNode(node3, "charge-fix-value").getFirstChild().getTextContent();
							}
							
							Map channelMap = new HashMap();
							channelMap.put("channel_id", channelId);
							channelMap.put("channel_name", channelName);
							channelMap.put("channel_img", channelImg);
							channelMap.put("channel_priority", channelPriority);
							channelMap.put("channel_status", channelStatus);
							channelMap.put("percentage_charge", percentage_charge);
							channelMap.put("value_charge", value_charge);
							channelMap.put("active_link_duration", activeLinkDuration);
							channelMap.put("max_length_name", max_length_name);
							if("active".equalsIgnoreCase(channelStatus)) {
								channelList.add(channelMap);
							}
						}
						groupMap.put("channel_list_"+groupId, channelList);
						if("active".equalsIgnoreCase(groupStatus)) {
							paymentList.add(groupMap);
						}
					}
					map.put("available_payments", paymentList);
					
				}else {
					NodeIterator nitr = XPathAPI.selectNodeIterator(doc, "xendit//accounts//account");
					Node node = null;
					while((node = nitr.nextNode())!=null){
						String id = XPathAPI.eval(node, "@id").toString();
						System.out.println("param orgId : "+orgId+" id : "+id);
						if(orgId.equalsIgnoreCase(id)) {
							map.put("account_id", id);
							String org_name = XPathAPI.selectSingleNode(node, "org-name").getFirstChild().getTextContent();
							System.out.println("<--------- Use ["+org_name+"] Config --------->");
							String org_email = XPathAPI.selectSingleNode(node, "org-email").getFirstChild().getTextContent();
							api_key = XPathAPI.selectSingleNode(node, "api-key").getFirstChild().getTextContent();
							
							Object pw = XPathAPI.selectSingleNode(node, "password").getFirstChild();
							if(pw!=null) {
								password = XPathAPI.selectSingleNode(node, "password").getFirstChild().getTextContent();
							}
							checkout_method = XPathAPI.selectSingleNode(node, "checkout-method").getFirstChild().getTextContent();
							success_redirect_url = XPathAPI.selectSingleNode(node, "success-redirect-url").getFirstChild().getTextContent();
							callback_token = XPathAPI.selectSingleNode(node, "callback-token").getFirstChild().getTextContent();
							invoice_duration = XPathAPI.selectSingleNode(node, "invoice-duration-in-minutes").getFirstChild().getTextContent();
							auto_pay_retail = XPathAPI.selectSingleNode(node, "auto-pay-retail-outlet").getFirstChild().getTextContent();
							auto_pay_va = XPathAPI.selectSingleNode(node, "auto-pay-virtual-account").getFirstChild().getTextContent();
							auto_pay_qris = XPathAPI.selectSingleNode(node, "auto-pay-qris").getFirstChild().getTextContent();
							callback_url = XPathAPI.selectSingleNode(node, "callback-url").getFirstChild().getTextContent();
							fee_tax_value = XPathAPI.selectSingleNode(node, "fee-tax-value").getFirstChild().getTextContent();
							
							map.put("org_name", org_name);
							map.put("org_email", org_email);
							map.put("api_key", api_key);
							map.put("password", password);
							map.put("checkout_method", checkout_method);
							map.put("success_redirect_url", success_redirect_url);
							map.put("callback_token", callback_token);
							map.put("invoice_duration_in_minutes", invoice_duration);
							map.put("invoice_duration_in_minutes", invoice_duration);
							map.put("auto_pay_retail", auto_pay_retail);
							map.put("auto_pay_virtual_account", auto_pay_va);
							map.put("auto_pay_qris", auto_pay_qris);
							map.put("callback_url", callback_url);
							map.put("fee_tax_value", fee_tax_value);
							
							List paymentListChild = new LinkedList();
							NodeIterator nitr2Child = XPathAPI.selectNodeIterator(node, "payment-methods//channel-groups");
							Node node2Child = null;
							while((node2Child = nitr2Child.nextNode())!=null){
								String groupId = XPathAPI.eval(node2Child, "@group-id").toString();
								String groupName = XPathAPI.eval(node2Child, "@group-name").toString();
								String groupStatus = XPathAPI.eval(node2Child, "@group-status").toString();
								//System.out.println("groupName >>>>>>>>>>> "+groupName);
								Map groupMap = new HashMap();
								groupMap.put("group_id", groupId);
								groupMap.put("group_name", groupName);
								groupMap.put("group_status", groupStatus);
								
								List channelList = new LinkedList();
								NodeIterator nitr3 = XPathAPI.selectNodeIterator(node2Child, "channel");
								Node node3 = null;
								while((node3 = nitr3.nextNode())!=null){
									String channelId = XPathAPI.eval(node3, "@id").toString();
									String channelName = XPathAPI.eval(node3, "@name").toString();
									String channelPriority = XPathAPI.eval(node3, "@priority").toString();
									String channelStatus = XPathAPI.eval(node3, "@status").toString();
									String chargeType = XPathAPI.eval(node3, "@charge-type").toString();
									
									String activeLinkDuration = invoice_duration;
									String eval = XPathAPI.eval(node3, "@active-link-duration").str();
									if(!isNullOrEmpty(eval)) {
										activeLinkDuration = XPathAPI.eval(node3, "@active-link-duration").toString();
									}
									String maxLength = XPathAPI.eval(node3, "@max-length-name").str();
									if(!isNullOrEmpty(maxLength)) {
										max_length_name = XPathAPI.eval(node3, "@max-length-name").toString();
									}
									String channelImg = new String();
									String evalImg = XPathAPI.eval(node3, "@img").str();
									if(!isNullOrEmpty(evalImg)) {
										channelImg = XPathAPI.eval(node3, "@img").toString();
									}
									
									String percentage_charge = "0";
									String value_charge = "0";
									if("percentage".equalsIgnoreCase(chargeType)) {
										percentage_charge = XPathAPI.selectSingleNode(node3, "charge-percentage-value").getFirstChild().getTextContent();
									}else if("fixValue".equalsIgnoreCase(chargeType)) {
										value_charge = XPathAPI.selectSingleNode(node3, "charge-fix-value").getFirstChild().getTextContent();
									}else if("fixValueAndPercentage".equalsIgnoreCase(chargeType)) {
										percentage_charge = XPathAPI.selectSingleNode(node3, "charge-percentage-value").getFirstChild().getTextContent();
										value_charge = XPathAPI.selectSingleNode(node3, "charge-fix-value").getFirstChild().getTextContent();
									}
									
									Map channelMap = new HashMap();
									channelMap.put("channel_id", channelId);
									channelMap.put("channel_name", channelName);
									channelMap.put("channel_img", channelImg);
									channelMap.put("channel_priority", channelPriority);
									channelMap.put("channel_status", channelStatus);
									channelMap.put("percentage_charge", percentage_charge);
									channelMap.put("value_charge", value_charge);
									channelMap.put("active_link_duration", activeLinkDuration);
									channelMap.put("max_length_name", max_length_name);
									if("active".equalsIgnoreCase(channelStatus)) {
										channelList.add(channelMap);
									}
								}
								groupMap.put("channel_list_"+groupId, channelList);
								if("active".equalsIgnoreCase(groupStatus)) {
									paymentListChild.add(groupMap);
								}
								
								//System.out.println("channel_list "+groupName+" >>>>>>>>>>> "+groupMap);
							}
							map.put("available_payments", paymentListChild);
							
							break;
						}else {
							map.put("available_payments", new LinkedList());
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return map;
	}
	
	public Double getAdminCharge(Map map, String channelCode, double trxValue) {
		double charge = new Double(0);
		try {
			if(trxValue > 0) {
				String fee_tax = (String)map.get("fee_tax_value");
				List<Map> payments = (List)map.get("available_payments");
				if(payments.size() > 0) {
					Iterator itr = payments.iterator();
					while(itr.hasNext()) {
						Map group = (Map)itr.next();
						String id = (String)group.get("group_id");
						List<Map> channels = (List)group.get("channel_list_"+id);
						Iterator itrChnl = channels.iterator();
						while(itrChnl.hasNext()) {
							Map channelMap = (Map)itrChnl.next();
							String chId = (String)channelMap.get("channel_id");
							if(chId.equalsIgnoreCase(channelCode)) {
								String channel_name = (String)channelMap.get("channel_name");
								String channel_status = (String)channelMap.get("channel_status");
								String percentage_charge = (String)channelMap.get("percentage_charge");
								String value_charge = (String)channelMap.get("value_charge");
								System.out.println("channel_name :::::  "+channel_name);
								
								double percentage = 0;
								if(!isNullOrEmpty(percentage_charge)) {
									try {
										percentage = Double.parseDouble(percentage_charge);
									} catch (NumberFormatException e) {
										// TODO: handle exception
									}
								}
								
								double fixValue = 0;
								if(!isNullOrEmpty(value_charge)) {
									try {
										fixValue = Double.parseDouble(value_charge);
									} catch (NumberFormatException e) {
										// TODO: handle exception
									}
								}
								
								if(percentage > 0) {
									charge = trxValue*(percentage/100);
								}
								if(fixValue > 0) {
									charge += fixValue;
								}
								break;
							}
						}
					}
				}
				System.out.println("trxValue  :::::  "+trxValue);
			
				if(!isNullOrEmpty(fee_tax)) {
					double fee_tax_value = Double.parseDouble(fee_tax);
					if(fee_tax_value > 0) {
						double tax = charge*(fee_tax_value/100);
						System.out.println("fee tax  :::::  "+tax);
						charge += tax;
					}
				}
			
			}
			System.out.println("charge value  :::::  "+charge);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charge;
	}
	
	public JSONObject getChannelName(Map map, String channelCode) {
		JSONObject obj = new JSONObject();
		obj.put("group_name", "");
		obj.put("channel_name", "");
		try {
			List<Map> payments = (List)map.get("available_payments");
			if(payments.size() > 0) {
				Iterator itr = payments.iterator();
				while(itr.hasNext()) {
					Map group = (Map)itr.next();
					String id = (String)group.get("group_id");
					String group_name = (String)group.get("group_name");
					List<Map> channels = (List)group.get("channel_list_"+id);
					Iterator itrChnl = channels.iterator();
					while(itrChnl.hasNext()) {
						Map channelMap = (Map)itrChnl.next();
						String chId = (String)channelMap.get("channel_id");
						if(chId.equalsIgnoreCase(channelCode)) {
							String channel_name = (String)channelMap.get("channel_name");
							String active_link_duration = (String)channelMap.get("active_link_duration");
							String max_length_name = (String)channelMap.get("max_length_name");
							
							obj.put("group_name", group_name);
							obj.put("channel_name", channel_name);
							obj.put("active_link_duration", active_link_duration);
							if(!isNullOrEmpty(max_length_name)) {
								obj.put("max_length_name", max_length_name);
							}else {
								obj.put("max_length_name", maxLengthName);
							}
							break;
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	
	
	
}
