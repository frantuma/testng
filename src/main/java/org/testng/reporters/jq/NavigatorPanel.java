package org.testng.reporters.jq;

import static org.testng.reporters.jq.Main.C;
import static org.testng.reporters.jq.Main.D;
import static org.testng.reporters.jq.Main.S;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.reporters.XMLStringBuffer;

import java.util.List;
import java.util.Map;

public class NavigatorPanel implements IPanel {
  private Model m_model;
  public NavigatorPanel(Model model) {
    m_model = model;
  }

  @Override
  public void generate(List<ISuite> suites, XMLStringBuffer main) {
    int suiteCount = 0;
    for (ISuite suite : suites) {
      if (suite.getResults().size() == 0) {
        continue;
      }

      String suiteName = "suite-" + suiteCount;

      XMLStringBuffer header = new XMLStringBuffer(main.getCurrentIndent());

      Map<String, ISuiteResult> results = suite.getResults();
      int failed = 0;
      int skipped = 0;
      int passed = 0;
      for (ISuiteResult result : results.values()) {
        ITestContext context = result.getTestContext();
        failed += context.getFailedTests().size();
        skipped += context.getSkippedTests().size();
        passed += context.getPassedTests().size();
      }

      // Suite name in big font
      header.push(D, C, "suite");
      header.push(D, C, "suite-header rounded-window");
      // Extra div so the highlighting logic will only highlight this line and not
      // the entire container
      header.push(D, C, "light-rounded-window-top");
      header.push("a", "href", "#",
          "panel-name", suiteName,
          C, "navigator-link");
      header.addOptional(S, suite.getName(), C, "suite-name");
      header.pop("a");
      header.pop(D);

      header.push(D, C, "navigator-suite-content");

      //
      // Info
      //
      header.push(D, C, "suite-section-title");
      header.addRequired(S, "Info");
      header.pop(D);

      // Info content
      header.push(D, C, "suite-section-content");
      int total = failed + skipped + passed;
      String stats = String.format("%s, %s %s %s",
          pluralize(total, "method"),
          maybe(failed, "failed", ", "),
          maybe(skipped, "skipped", ", "),
          maybe(passed, "passed", ""));

      header.push("ul");

      // Tests
      header.push("li");
      header.push("a", "href", "#",
          "panel-name",  TestPanel.getTag(),
          C, "navigator-link ");
      header.addOptional(S, String.format("%s ", pluralize(results.values().size(), "test"),
          C, "test-stats"));
      header.pop("a");
      header.pop("li");

      // testng.xml
      header.push("li");
      header.push("a", "href", "#",
          "panel-name", TestNgXmlPanel.getTag(suiteCount),
          C, "navigator-link");
      String fqName = suite.getXmlSuite().getFileName();
      header.addOptional(S, fqName.substring(fqName.lastIndexOf("/") + 1),
          C, "testng-xml");
      header.pop("a");
      header.pop("li");

      header.pop("ul");
      header.pop(D); // suite-section-content

      //
      // Methods
      //
      header.push(D, C, "result-section");

      header.push(D, C, "suite-section-title");
      header.addRequired(S, "Results");
      header.pop(D);

      // Method stats
      header.push(D, C, "suite-section-content");
      header.push("ul");
      header.push("li");
      header.addOptional(S, stats, C, "method-stats");
      header.pop("li");

      generateMethodList("Failed methods", ITestResult.FAILURE, suite, suiteName, header);
      generateMethodList("Skipped methods", ITestResult.SKIP, suite, suiteName, header);

      header.pop("ul");

      header.pop(D); // suite-section-content
      header.pop(D); // suite-header
      header.pop(D); // suite

      header.pop(D); // result-section

      header.pop(D); // navigator-suite-content

      main.addString(header.toXML());

      suiteCount++;
    }
  }

  private static String maybe(int count, String s, String sep) {
    return count > 0 ? count + " " + s + sep: "";
  }

  private void generateMethodList(String name, int status, ISuite suite, String suiteName,
      XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());

    xsb.push("li");
    // Failed methods
    xsb.addRequired(S, name, C, "method-list-title");

    // List of failed methods
    xsb.push(D, C, "method-list-content");
    int count = 0;
    for (ITestResult tr : m_model.getTestResults(suite)) {
      if (tr.getStatus() == status) {
        String testName = Model.getTestResultName(tr);
        xsb.addRequired("a", testName, "href", "#",
            "hash-for-method", m_model.getTag(tr),
            "panel-name", suiteName,
            C, "method navigator-link");
        count++;
      }
    }
    xsb.pop(D);
    xsb.pop("li");

    if (count > 0) {
      main.addString(xsb.toXML());
    }
  }

  private String pluralize(int count, String singular) {
    return Integer.toString(count) + " "
        + (count > 1 ? (singular.endsWith("s") ? singular + "es" : singular + "s") : singular);
  }

}
