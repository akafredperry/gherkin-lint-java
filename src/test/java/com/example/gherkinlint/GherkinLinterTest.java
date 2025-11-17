package com.example.gherkinlint;

import com.example.gherkinlint.rules.GivenWhenThenLintRule;
import com.example.gherkinlint.rules.UniqueNameLintRule;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.Scenario;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GherkinLinterTest {

  @Test
  void lintFeatureFiles() {
    new GherkinLinter()
        .registerRule(new UniqueNameLintRule())
        .registerRule(new GivenWhenThenLintRule())
        .lint(new File("src/test/resources"))
        .assertNoLint();
  }

  @Test
  void testMainDefaultRulesAndLocationDoesNotThrowException() {
    GherkinLinter.main(new String[0]);
  }

  @Test
  void testMainUniqueTagRuleThrowsException() {
    Exception ex = assertThrows(Exception.class,
        () -> GherkinLinter.main(new String[]{"gherkin-lint-with-id-tag-rule.properties"}));
    assertThat(ex.getMessage(), containsString("is missing an id tag"));
  }

  @Test
  void testMainOverrideLintPropertiesAndFeaturesThrowsException() {
    Exception ex = assertThrows(Exception.class,
        () -> GherkinLinter.main(new String[]{
            "gherkin-lint-gwt-only.properties",
            "src/test/unit-test-features/gwt.feature",
            "src/test/unit-test-features/gwt-with-background.feature"
        }));
    assertThat(ex.getMessage(), containsString("matches illegal pattern"));
  }

  @Test
  void testMainInvalidFeatureFileThrowsException() {
    Exception ex = assertThrows(Exception.class,
        () -> GherkinLinter.main(new String[]{
            "src/test/unit-test-features/invalid.feature"
        }));
    assertThat(ex.getMessage(), containsString("Illegal keyword"));
  }

  @Test
  void testMainLintPropertiesNotFoundThrowsException() {
    Exception ex = assertThrows(Exception.class,
        () -> GherkinLinter.main(new String[]{
            "missing.properties",
        }));
    assertThat(ex.getMessage(), containsString("no missing.properties on classpath"));
  }

  @Test
  void testMainFeatureFileNotFoundThrowsException() {
    Exception ex = assertThrows(Exception.class,
        () -> GherkinLinter.main(new String[]{
            "src/test/unit-test-features/missing.feature"
        }));
    assertThat(ex.getMessage(), containsString("missing.feature does not exist"));
  }

  @Test
  void testRegisterRulesInvokesExpectedConstructorArgs() {
    GherkinLinter linter = new GherkinLinter();
    Properties properties = new Properties();
    properties.setProperty("rule.1.class", "com.example.gherkinlint.GherkinLinterTest$TestGherkinLintRule");
    linter.registerRules(properties);

    assertThat(((TestGherkinLintRule) linter.getRules().get(0)).args.length, is(0));

    properties.setProperty("rule.1.args", "one two three");
    linter.registerRules(properties);

    assertThat(((TestGherkinLintRule) linter.getRules().get(1)).args, equalTo(new String[]{"one", "two", "three"}));
  }

  @Test
  void testLintInvokesRulesInExpectedWays() {
    GherkinLinter linter = new GherkinLinter();
    GherkinLintRule rule = mock(GherkinLintRule.class);
    linter.registerRule(rule);

    // this feature file has 6 scenarios, one of which is a scenario outline with 2 iterations, no background defined
    linter.lint(new File("src/test/resources/test.feature"));

    verify(rule).lint(Mockito.any(Feature.class));
    verify(rule, times(6)).lint(eq(null), Mockito.any(Scenario.class));
    verify(rule, times(7)).lint(Mockito.any(Pickle.class));
  }

  @Test
  void testLintCatchesLintFeatureExceptions() {
    GherkinLinter linter = new GherkinLinter();
    GherkinLintRule rule1 = mock(GherkinLintRule.class);
    doThrow(new RuntimeException("deliberate feature lint exception"))
        .when(rule1)
        .lint(Mockito.any(Feature.class));
    GherkinLintRule rule2 = mock(GherkinLintRule.class);
    linter.registerRule(rule1).registerRule(rule2);

    File feature = new File("src/test/resources/test.feature");
    List<GherkinLint> results = linter.lint(feature).getResults();
    assertThat(results.size(), equalTo(1));
    assertThat(results.get(0).message(), containsString("deliberate feature lint exception"));
    assertThat(results.get(0).location().getLine(), equalTo(1L));
    assertThat(results.get(0).featureFile(), equalTo(feature.toPath()));
    verify(rule2).lint(Mockito.any(Feature.class));
  }

  @Test
  void testLintCatchesLintScenarioExceptions() {
    GherkinLinter linter = new GherkinLinter();
    GherkinLintRule rule1 = mock(GherkinLintRule.class);
    doThrow(new RuntimeException("deliberate scenario lint exception"))
        .when(rule1)
        .lint(eq(null), Mockito.any(Scenario.class));
    GherkinLintRule rule2 = mock(GherkinLintRule.class);
    linter.registerRule(rule1).registerRule(rule2);

    // this feature file has 6 scenarios
    File feature = new File("src/test/resources/test.feature");
    List<GherkinLint> results = linter.lint(feature).getResults();
    assertThat(results.size(), equalTo(6));
    long previousLine = 0;
    for (GherkinLint result : results) {
      assertThat(result.message(), containsString("deliberate scenario lint exception"));
      assertThat(result.location().getLine(), greaterThan(previousLine));
      assertThat(result.featureFile(), equalTo(feature.toPath()));
      previousLine = result.location().getLine();
    }

    verify(rule2).lint(Mockito.any(Feature.class));
  }

  @Test
  void testLintCatchesLintPickleExceptions() {
    GherkinLinter linter = new GherkinLinter();
    GherkinLintRule rule1 = mock(GherkinLintRule.class);
    doThrow(new RuntimeException("deliberate pickle lint exception"))
        .when(rule1)
        .lint(Mockito.any(Pickle.class));
    GherkinLintRule rule2 = mock(GherkinLintRule.class);
    linter.registerRule(rule1).registerRule(rule2);

    // this feature file has 7 pickles
    File feature = new File("src/test/resources/test.feature");
    List<GherkinLint> results = linter.lint(feature).getResults();
    assertThat(results.size(), equalTo(7));
    long previousLine = 0;
    for (GherkinLint result : results) {
      assertThat(result.message(), containsString("deliberate pickle lint exception"));
      assertThat(result.location().getLine(), greaterThanOrEqualTo(previousLine));
      assertThat(result.featureFile(), equalTo(feature.toPath()));
      previousLine = result.location().getLine();
    }

    verify(rule2).lint(Mockito.any(Feature.class));
  }

  @Test
  void testClearWorksAsExpected() {
    GherkinLinter linter = new GherkinLinter();
    linter.registerRule(new UniqueNameLintRule());

    File feature = new File("src/test/resources/test.feature");
    assertThat(linter.lint(feature).getResults(), is(empty()));

    // check re-run result in duplicate scenario name lint
    assertThat(linter.lint(feature).getResults(), is(not((empty()))));

    linter.clear();
    assertThat(linter.lint(feature).getResults(), is(empty()));

  }

  public static class TestGherkinLintRule implements GherkinLintRule {

    String[] args;

    public TestGherkinLintRule(String... args) {
      this.args = args;
    }
  }
}