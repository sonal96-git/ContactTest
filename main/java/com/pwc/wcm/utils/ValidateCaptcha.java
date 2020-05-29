package com.pwc.wcm.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class ValidateCaptcha {

	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	//public static final String secret = "6LdyKSkTAAAAAKeoiewLTI1BU35E9C8lzo0Ba9Hj";
	private final static String USER_AGENT = "Mozilla/5.0";
	public static final Logger log  = LoggerFactory.getLogger(ValidateCaptcha.class);
	
	static GoogleRecaptcha recaptcha =new GoogleRecaptcha();
	public  String secret =recaptcha.getPrivateKey();
	public String publicKey=recaptcha.getPublicKey();

	public String getPublicKey() {
		return publicKey;
	}

	public  String  validateCaptcha(String gRecaptchaResponse) {
		String valid="false";
		//String gRecaptchaResponse="03AHJ_VutBjzV8iIAebVKv3_gK1UglJjlft2an59MjkgDw8OGLe4fh7sXNf_IKMYEI3UKZCGhkIdGpXFrqTp82hUpNUworUWAamOQb1IcHRFYKIWV9c7jOEpC0OArrAcoEDMMT1mE1WbPQzsxBMv66ryf1YS3Mx6X2p_2asF7M_2rRProstvO_5n6geKeY-yXICn08EFShyjWGWuQGGz3T9n51rHg7jUtJit_SISAL3MEeT3yuFVzqClyWhZUiLjM6SQTRbWTJzm6s6HJ9cFT21HJO_4lWfypGIjslwGzhxZDNhdDcmOsp0PLhuWfJbetHyWZWUaROlNL3tCFARA1KINxlv6w6lucQ7l-oyXoJeCvbte9uTzq7Dnr65jmnlzb56vqP9TISW5FaN2OxBMMfKutL6feYaAZgWf9x4ErgJQ-7yWijZCeN5m2IWPHkVIucC6N_LOHNh8JTTbwvjSlj2Qip8cGcQhuTYeCjp6nFyowydaMEMa5bMjtkliYixyYHXUrZ61EhEL72YEHNzg0aWF3UW-fIuiR-XL60sybCJzFVa2SBcLGI_TGzTO52M_ccLjGuqmIi24-3lok6OdQTxe81FfcCJbLJRfWpwwg-HJN4-YKSOpNFQbid5HfVn3oHsaNwFiQZSIaRf-XtIppDXTaFzwQoqXuqziRzH4BGOCUT8V-8Q6bvE7o3hi1yORDNXuR01yH-QiWrF40ZN7DxqEgbWYeJGX36oeSioqwHnKjsS28xG2VhwNmekRz3gNeiYnyrr_B1akkebDDnuKSxlvBZrShlw0t0qC-meF7AkF8td_97aYsyPG58enucIy-ekjke1eetTFi9N1ipV1m-GiMn-SPwbbuAGWjMTntqA6U66rbmDePoO2Og1P2Clcy7wKGVm8999SIAioLcO1wMcOkg9K9ke14wmfBwe_FD6Qaqf9As8lvmcw0";
		if (gRecaptchaResponse == null) {
			return "false";
		}
		
		if ( "".equals(gRecaptchaResponse)) {
			return "false";
		}
		
		try{
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		String postParams = "secret=" + secret + "&response="
				+ gRecaptchaResponse;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		log.info("\nSending 'POST' request to URL : " + url);
		log.info("Post parameters : " + postParams);
		log.info("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		//System.out.println(response.toString());
		
		//parse JSON response and return 'success' value
		//JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
		//JsonObject jsonObject = jsonReader.readObject();
		//jsonReader.close();
         JSONObject json = new JSONObject(response.toString());
         if(json.has("success")) {
	     	 valid = json.get("success").toString();
         }
		
		//return jsonObject.getBoolean("success");
		}catch(Exception exception){
			log.error(exception.getMessage(),exception);
			//return false;
		}
		return valid;
	}
}