package com.popularvid.user;

import com.popularvid.youtube.YouTubeMsg;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An integrated test of the entire user module.
 *
 * @author Otar Magaldadze
 */
@SuppressWarnings("ConstantConditions")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserModuleTests {

	String url = "http://localhost";
	String api = "/api/user";

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private final String DB_DIR = "dbase";
	private final String DB_TABLE = "users";

	private String TOKEN;

	/**
	 * Backup database before tests.
	 *
	 * @throws IOException database error
	 */
	@BeforeAll
	void stashDatabase() throws IOException {
		Files.move(Paths.get(DB_DIR, DB_TABLE), Paths.get(DB_DIR, DB_TABLE + ".bkp"),
				StandardCopyOption.REPLACE_EXISTING);
		Files.createFile(Paths.get(DB_DIR, DB_TABLE));
		Files.writeString(Paths.get(DB_DIR, DB_TABLE), "username,password,time_interval,country");
	}

	/**
	 * Restore backed up database.
	 *
	 * @throws IOException database error
	 */
	@AfterAll
	void restoreDatabase() throws IOException {
		Files.move(Paths.get(DB_DIR, DB_TABLE + ".bkp"), Paths.get(DB_DIR, DB_TABLE),
				StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Add 10 users and check success status.
	 */
	@Test
	@Order(1)
	void addUsers() {
		String endPoint = url + ":" + port + api + "/add-user/";

		for (int i = 1; i <= 10; i++) {
			UserDbo userDbo = new UserDbo("user" + i,"password" + i,
					10 + i*2, "ge");

			ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.getBody().contains("Successfully created!"));
		}
	}

	/**
	 * Test add user without providing a username.
	 */
	@Test
	@Order(2)
	void addUserNoUsername() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("","password",6, "ge");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Username not provided."));
	}

	/**
	 * Test add user without providing a password.
	 */
	@Test
	@Order(3)
	void addUserNoPassword() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("johny","",6, "ge");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Password not provided."));
	}

	/**
	 * Test add user without providing a country (a mandatory field).
	 */
	@Test
	@Order(4)
	void addUserNoCountry() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("johny","password",6, "");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Country not provided."));
	}

	/**
	 * Test add user without providing a time interval (a mandatory field).
	 */
	@Test
	@Order(5)
	void addUserNoIntervalTime() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("johny","password",0, "ge");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test add user with negative time interval (must be between 1-60).
	 */
	@Test
	@Order(6)
	void addUserNegativeIntervalTime() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("johny","password",-1, "ge");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test add user with big time interval (must be between 1-60).
	 */
	@Test
	@Order(7)
	void addUserBigIntervalTime() {
		String endPoint = url + ":" + port + api + "/add-user/";

		UserDbo userDbo = new UserDbo("johny","password",100, "ge");

		ResponseEntity<String> response = restTemplate.postForEntity(endPoint, userDbo, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test if the first added user exists.
	 */
	@Test
	@Order(8)
	void userExistsFirst() {
		String endPoint = url + ":" + port + api + "/user-exists/user1";

		var response = restTemplate.getForEntity(endPoint, Boolean.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	/**
	 * Test if the last added user exists.
	 */
	@Test
	@Order(9)
	void userExistsLast() {
		String endPoint = url + ":" + port + api + "/user-exists/user1";

		var response = restTemplate.getForEntity(endPoint, Boolean.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	/**
	 * Search for a non-existent user.
	 */
	@Test
	@Order(10)
	void userExistsNot() {
		String endPoint = url + ":" + port + api + "/user-exists/nonexistent";

		var response = restTemplate.getForEntity(endPoint, Boolean.class);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	/**
	 * Search for a null user.
	 */
	@Test
	@Order(11)
	void userExistsNull() {
		String endPoint = url + ":" + port + api + "/user-exists/";

		var response = restTemplate.getForEntity(endPoint, null);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	/**
	 * Test a successful login.
	 */
	@Test
	@Order(12)
	void loginSuccess() {
		String endPoint = url + ":" + port + api + "/login";

		var requestBody = new LoginDto("user1", "password1");

		var response = restTemplate.postForEntity(endPoint, requestBody, UserDto.class);

		var body = response.getBody();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(body.getCountry());
		assertTrue(body.getTimeInterval() >= 1);
		assertTrue(body.getTimeInterval() <= 60);
		assertNotNull(body.getToken());
		assertFalse(body.getToken().isEmpty());

		TOKEN = body.getToken();
	}

	/**
	 * Test login with wrong username.
	 */
	@Test
	@Order(13)
	void loginWrongUsername() {
		String endPoint = url + ":" + port + api + "/login";

		var requestBody = new LoginDto("wrong_user", "password1");

		var response = restTemplate.postForEntity(endPoint, requestBody, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Wrong username or password!"));
	}

	/**
	 * Test login with wrong password.
	 */
	@Test
	@Order(14)
	void loginWrongPassword() {
		String endPoint = url + ":" + port + api + "/login?username=user1&password=wrong_password";

		var requestBody = new LoginDto("user1", "wrong_password");

		var response = restTemplate.postForEntity(endPoint, requestBody,
				String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Wrong username or password!"));
	}

	/**
	 * Test login with empty username.
	 */
	@Test
	@Order(15)
	void loginEmptyUsername() {
		String endPoint = url + ":" + port + api + "/login";

		var requestBody = new LoginDto("", "password1");

		var response = restTemplate.postForEntity(endPoint, requestBody, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Wrong username or password!"));
	}

	/**
	 * Test login with empty password.
	 */
	@Test
	@Order(16)
	void loginEmptyPassword() {
		String endPoint = url + ":" + port + api + "/login";

		var requestBody = new LoginDto("user1", "");

		var response = restTemplate.postForEntity(endPoint, requestBody, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Wrong username or password!"));
	}

	/**
	 * Test correctly updating a time interval.
	 */
	@Test
	@Order(17)
	void updateTimeInterval() {
		String endPoint = url + ":" + port + api + "/update-time-interval?timeInterval=43";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.postForEntity(endPoint, request, UserDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(43, response.getBody().getTimeInterval());
	}

	/**
	 * Test wrongly updating a time interval.
	 */
	@Test
	@Order(18)
	void updateTimeIntervalSmall() {
		String endPoint = url + ":" + port + api + "/update-time-interval?timeInterval=0";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.postForEntity(endPoint, request,
				String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test wrongly updating a time interval.
	 */
	@Test
	@Order(19)
	void updateTimeIntervalNegative() {
		String endPoint = url + ":" + port + api + "/update-time-interval?timeInterval=-12";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.postForEntity(endPoint, request,
				String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test wrongly updating a time interval.
	 */
	@Test
	@Order(20)
	void updateTimeIntervalBig() {
		String endPoint = url + ":" + port + api + "/update-time-interval?timeInterval=70";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.postForEntity(endPoint, request,
				String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Time interval should be between 1-60!"));
	}

	/**
	 * Test updating a country.
	 */
	@Test
	@Order(21)
	void updateCountry() {
		String endPoint = url + ":" + port + api + "/update-country?country=Ireland";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.postForEntity(endPoint, request, UserDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Ireland", response.getBody().getCountry());
	}

	/**
	 * Test login case-insensitively.
	 */
	@Test
	@Order(22)
	void loginCaseInsensitive() {
		String endPoint = url + ":" + port + api + "/login";

		var requestBody = new LoginDto("User1", "password1");

		var response = restTemplate.postForEntity(endPoint, requestBody, UserDto.class);

		var body = response.getBody();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(body.getCountry());
		assertTrue(body.getTimeInterval() >= 1);
		assertTrue(body.getTimeInterval() <= 60);
		assertNotNull(body.getToken());
		assertFalse(body.getToken().isEmpty());

		TOKEN = body.getToken();
	}

	/**
	 * Test logout.
	 */
	@Test
	@Order(23)
	void logout() {
		String endPoint = url + ":" + port + api + "/logout";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.exchange(endPoint, HttpMethod.GET, request, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Logged out successfully!", response.getBody());
	}

	/**
	 * Test get popular video and comment.
	 */
	@Test
	@Order(24)
	void getYouTubeMsg() {
		String endPoint = url + ":" + port + api + "/get-youtube-msg";

		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + TOKEN);
		HttpEntity<Object> request = new HttpEntity<>(null, headers);

		var response = restTemplate.exchange(endPoint, HttpMethod.GET, request,
				YouTubeMsg.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	/**
	 * Test get popular video and comment with unauthorized user.
	 */
	@Test
	@Order(25)
	void getYouTubeMsgUnauthorized() {
		String endPoint = url + ":" + port + api + "/get-youtube-msg";

		var response = restTemplate.getForEntity(endPoint, YouTubeMsg.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
}
