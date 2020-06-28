package com.typicode.jsonplaceholder;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import utils.FileReader;
import utils.SubSequentCalls;

public class PostRequests {
	
	SubSequentCalls subSequentCalls = new SubSequentCalls();
	Map<String,String> headerProperties;
	String baseUri;
	String postsPath;
	Properties properties;
	String token;
	String requestPayload;
	private static Configuration configuration = Configuration.defaultConfiguration();
	private final int USERID_MAX_RANGE = 999999999;

	/**
	 * BeforeMethod is used to fetched the baseUri, basePath from Properties
	 * 
	 */
	@BeforeMethod
	public void preCondition() throws IOException {
		FileInputStream fis = new FileInputStream("./src/main/resources/config.properties");
		headerProperties = utils.FileReader.readProperties("./src/test/resources/requests/header_jsonplaceholder.properties");
		requestPayload = new FileReader().readFile("requests/postsrequestpayload.json");
		properties = new Properties();
		properties.load(fis);
		baseUri = System.getProperty("HOST");
		if (baseUri == null) {
			baseUri = properties.getProperty("HOST");
		}
		postsPath = properties.getProperty("PostPath");
		token = subSequentCalls.getToken();
	}

	/**
	 * This test is used to validate response 201
	 * 
	 */

	@Test
	public void verifyResponse201() {
		try {
			given().baseUri(baseUri).basePath(postsPath).headers(headerProperties).log().all().when().post().then().assertThat().and()
					.statusCode(HttpStatus.SC_CREATED).log().all();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}
	
	@Test
	public void removeOptionalValuesFromReqBody() {
		try {
			DocumentContext requestBody = JsonPath.using(configuration).parse(requestPayload);
			requestBody.delete("$.userId");
			requestBody.delete("$.body");
			given().baseUri(baseUri).basePath(postsPath).headers(headerProperties).body(requestBody.jsonString()).log().all().when().post().then().assertThat().and()
					.statusCode(HttpStatus.SC_CREATED).log().all();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}
	
	@Test
	public void verifyBlankPayloady() {
		try {
			given().baseUri(baseUri).basePath(postsPath).headers(headerProperties).body("null").log().all().when().post().then().assertThat().and()
					.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).log().all();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}
	
	@Test
		public void verifyUserIdMaxRange() {
			try {
				DocumentContext requestBody = JsonPath.using(configuration).parse(requestPayload);
				requestBody.set("$.userId", USERID_MAX_RANGE);
				String response = given().baseUri(baseUri).basePath(postsPath).headers(headerProperties).body(requestBody.jsonString()).log().all().when().post().then().assertThat().and()
						.statusCode(HttpStatus.SC_CREATED).log().all().and().extract().response().asString();
				DocumentContext responseBody = JsonPath.using(configuration).parse(response);
				Assert.assertTrue(responseBody.read("$.userId").equals(USERID_MAX_RANGE),"Correct user id not recived in response");
				Assert.assertTrue(responseBody.read("$.title").equals("foo"),"Correct title not recived in response");			
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
			}
		}

}
