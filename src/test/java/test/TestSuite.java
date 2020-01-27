package test;

import io.restassured.RestAssured;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;

public class TestSuite
{
    public String runId = String.valueOf(System.currentTimeMillis()).substring(5, 12);

    @BeforeClass
    public void beforeClass(ITestContext context)
    {
        RestAssured.baseURI = context.getCurrentXmlTest().getParameter("BaseURI");
        Reporter.log("Test Started with runID= " + runId, true);
    }
}
