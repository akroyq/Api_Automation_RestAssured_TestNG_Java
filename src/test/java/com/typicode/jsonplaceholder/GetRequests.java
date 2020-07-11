package com.typicode.jsonplaceholder;

import static io.restassured.RestAssured.given;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpStatus;
import org.hamcrest.core.IsNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.typicode.utilities.SubSequentCalls;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by Amit Roy. Using fake API http://jsonplaceholder.typicode.com
 */

public class GetRequests {
	SubSequentCalls subSequentCalls = new SubSequentCalls();
	Map<String,String> headerProperties;
	String baseUri;
	String userPath;
	String postsPath;
	String commentsPath;
	Properties properties;
	int samanthaId;
	String token;
	
	private final String SAMANTHA_POSTS_TITLE = "ea molestias quasi exercitationem repellat qui ipsa sit aut";

	/**
	 * BeforeMethod is used to fetched the baseUri, basePath from Properties
	 * 
	 */
	@BeforeMethod
	public void preCondition() throws IOException {
		FileInputStream fis = new FileInputStream("./src/main/resources/config.properties");
		properties = new Properties();
		properties.load(fis);
		baseUri = System.getProperty("HOST");
		if (baseUri == null) {
			baseUri = properties.getProperty("HOST");
		}
		userPath = properties.getProperty("UserPath");
		postsPath = properties.getProperty("PostPath");
		commentsPath = properties.getProperty("CommentsPath");
		token = subSequentCalls.getToken();

	}

	/**
	 * This test is used to validate response schema
	 * 
	 */

	@Test
	public void verifyResponseSchemaForUsers() {
		try {
			given().baseUri(baseUri).basePath(userPath).log().all().when().get().then().assertThat().and()
					.statusCode(HttpStatus.SC_OK).log().all().and()
					.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/schemaFile.jsd"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}

	@Test
	public void verifyUsersIdIsNotBlank() {
		try {
			given().baseUri(baseUri).basePath(userPath).log().all().get().then().assertThat()
					.statusCode(HttpStatus.SC_OK).log().all().and().body("id", IsNull.notNullValue());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}

	/**
	 * This test is used to search user name Samantha in the response payload
	 * 
	 */

	@Test
	public void verifyUserNameSamantha() {
		try {
			Response response = given().baseUri(baseUri).basePath(userPath).log().all().when().get().then().assertThat()
					.statusCode(HttpStatus.SC_OK).log().all().and().body("username", hasItems("Samantha")).extract()
					.response();

			List<String> userNameList = response.path("username");
			for (int i = 0; i < userNameList.size(); i++) {
				if (response.path("username[" + i + "]").equals("Samantha")) {
					samanthaId = response.path("id[" + i + "]");
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}

	/**
	 * Use the details fetched to make a search for the posts written by the user
	 * Samantha.
	 * 
	 */
	@Test(dependsOnMethods = { "verifyUserNameSamantha" })
	public void verifySamanthaPosts() {
		try {
			given().baseUri(baseUri).basePath(postsPath).log().all().when().get().then().assertThat()
					.statusCode(HttpStatus.SC_OK).log().all().and()
					.body("[" + Integer.toString(samanthaId - 1) + "].title", equalTo(SAMANTHA_POSTS_TITLE));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}

	/**
	 * This test is for each post, fetch the comments and validate if the emails in
	 * the comment section are in the proper format.
	 * 
	 */

	@Test
	public void verifyValidateEmailsForEeachPost() {
		try {
			Response response = given().baseUri(baseUri).basePath(commentsPath).log().all().when().get().then()
					.assertThat().statusCode(HttpStatus.SC_OK).log().all().and().extract().response();
			List<String> emailList = response.path("email");
			if (emailList.size() > 0) {
				SoftAssert softAssertion = new SoftAssert();
				for (int i = 0; i < emailList.size(); i++) {
					softAssertion
							.assertTrue(response.path("email[" + i + "]").toString().matches("^[A-Za-z0-9+_.-]+@(.+)$"),
									"postId " + response.path("postId[" + i + "]").toString() + " and Id "
											+ response.path("id[" + i + "]").toString()
											+ " email address is not valid");
				}
				softAssertion.assertAll();
			}

			else {
				Assert.fail("Email list is empty. Please check.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception thrown Test case failed :" + e.getMessage(), e);
		}
	}

}
