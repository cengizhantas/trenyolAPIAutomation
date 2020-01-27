package base;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.TestSuite;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class BaseUtil extends TestSuite
{
    @BeforeClass
    public void setUp()
    {
        RestAssured.basePath = "api/books";
        Reporter.log("Set Up completed \n", true);
        // delete all books
        given().accept(ContentType.JSON).contentType(ContentType.JSON).get("/delete");
    }

    @BeforeMethod
    public void beforeTest(Method method)
    {
        Reporter.log("Test Started= " + method.getName(), true);
    }

    @AfterMethod
    public void afterTest(Method method)
    {
        Reporter.log("Test Completed= " + method.getName() + "\n", true);
    }

    @Test(priority=0)
    public void emptyStore()
    {
        try
        {
            Response response = given().accept(ContentType.JSON).contentType(ContentType.JSON).get();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            List<HashMap<String, String>> booklist = response.jsonPath().getList("$.");
            assertEquals(0, booklist.size(), "Booklist is not empty");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test(description = "Check title field")
    public void requiredFieldTitle() throws Exception
    {
        try {
            Response response=given().contentType("application/json")
                    .body("{\"title\" : \"DevOps is a lie\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "title and author are required fields", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test(description = "Check author field")
    public void requiredFieldAuthor()
    {
        try {
            Response response = given().contentType("application/json")
                    .body("{\"author\" : \"Jane Archer\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "title and author are required fields", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test(description = "Check empty author field")
    public void emptyAuthorFields()
    {
        try {
            Response response = given().contentType("application/json")
                    .body("{\"author\" : \"\" ,\"title\" : \"DevOps is a lie\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "title and author fields cannot be empty", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test(description = "Check empty title field")
    public void emptyTitleFields()
    {
        try {
            Response response = given().contentType("application/json")
                    .body("{\"author\" : \"Jane Archer\" ,\"title\" : \"\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "title and author fields cannot be empty", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test (description = "Try to send ID")
    public void readOnlyId()
    {
        try {
            Response response = given().contentType("application/json")
                    .body("{\"id\" : \"2\",\"author\" : \"Jane Archer\" ,\"title\" : \"DevOps is a lie\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "id field is readonly", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test (description = "Try to put book duplicate")
    public void putbooksDuplicate()
    {
        try {
            Response addBookSuccessfullResponse = given().contentType("application/json")
                    .body("{\"author\" : \"Jane Archer2\" ,\"title\" : \"DevOps is a lie\"}")
                    .when()
                    .put();
            String author = addBookSuccessfullResponse.jsonPath().get("author");
            String title = addBookSuccessfullResponse.jsonPath().get("title");

            Response addBookDuplicateResponse = given().contentType("application/json")
                    .body("{\"author\" : \""+author+"\" ,\"title\" : \""+title+"\"}")
                    .when()
                    .put();
            Reporter.log("Service Response:", true);
            Reporter.log(addBookDuplicateResponse.getBody().asString(), true);
            String errorMsg = addBookDuplicateResponse.jsonPath().get("error");
            assertEquals(errorMsg, "Another book with similar title and author already exists", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test (description = "get book that not exist")
    public void getBookNotFound()
    {
        try {
            //Find book that has max id
            Response getAllBooks = given().contentType("application/json")
                    .when()
                    .get();

            List<Integer> idList=getAllBooks.jsonPath().get("id");

            int max=0;
            for (int i = 0; i < idList.size(); i++) {
                if(idList.get(i)>max)
                { max=idList.get(i);}
            }

            Response response = given().contentType("application/json")
                    .when()
                    .get("/"+(max+1)); // max+1 is not exist
            Reporter.log("Service Response:", true);
            Reporter.log(response.getBody().asString(), true);
            String errorMsg = response.jsonPath().get("error");
            assertEquals(errorMsg, "the given id does not exist.", "Error message is not correct");
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }

    @Test (description = "get book that exist")
    public void getBookFound()
    {
        try {
            //Firstly Put a New book
            String author="Cengizhan Tas";
            String title="DevOps is a lie";
            Response addBookResponse = given().contentType("application/json")
                    .body("{\"author\" : \""+author+"\" ,\"title\" : \""+title+"\"}")
                    .when()
                    .put();

            addBookResponse.then().assertThat().statusCode(200);
            int id = addBookResponse.jsonPath().get("id");

            //Then Get created book
            Response getBookSuccessfullyResponse = given().contentType("application/json")
                    .when()
                    .get("/"+id);

            Reporter.log("Service Response:", true);
            Reporter.log(getBookSuccessfullyResponse.getBody().asString(), true);

            assertEquals(author, getBookSuccessfullyResponse.jsonPath().get("author"));
            assertEquals(title, getBookSuccessfullyResponse.jsonPath().get("title"));
        } catch (Exception e) {
            Reporter.log(e.getStackTrace().toString(), true);
        }
    }
}
