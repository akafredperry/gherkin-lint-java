package com.example.gherkinlint.rules;

import org.junit.jupiter.api.Test;

import static com.example.gherkinlint.rules.ScenarioTests.*;
import static org.hamcrest.MatcherAssert.assertThat;

class GivenWhenThenLintRuleTest {

  @Test
  void testDefaultConstructorAllowsScenariosWithExpectedPatterns() {
    GivenWhenThenLintRule rule = new GivenWhenThenLintRule();

    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "When", "Then")),
        containsDocumentLint());
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "Then")),
        containsDocumentLint());
    assertThat(rule.lint(null, createScenarioWithSteps(2, "When", "Then")),
        containsDocumentLint());
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Then")),
        containsDocumentLint());
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "And", "When", "And", "Then", "And")),
        containsDocumentLint());
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "When", "Then", "When", "Then")),
        containsDocumentLint());
  }

  @Test
  void testDefaultConstructorAllowsBackgroundAndScenariosWithExpectedPatterns() {
    GivenWhenThenLintRule rule = new GivenWhenThenLintRule();

    assertThat(rule.lint(createBackgroundWithSteps(2, "Given"), createScenarioWithSteps(4, "When", "Then")),
        containsDocumentLint());
    assertThat(rule.lint(createBackgroundWithSteps(2, "Given"), createScenarioWithSteps(4, "And", "When", "Then")),
        containsDocumentLint());
    assertThat(rule.lint(createBackgroundWithSteps(2, "Given"), createScenarioWithSteps(4, "Then")),
        containsDocumentLint());
    assertThat(rule.lint(createBackgroundWithSteps(2, "Given", "And"), createScenarioWithSteps(5, "And", "When", "And", "Then", "And")),
        containsDocumentLint());
  }

  @Test
  void testDefaultConstructorDisallowsScenariosWithIllegalPatterns() {
    GivenWhenThenLintRule rule = new GivenWhenThenLintRule();

    assertThat(rule.lint(null, createScenarioWithSteps(2, "And", "When", "Then")),
        containsDocumentLint("first step should NOT be And", "3"));

    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given")),
        containsDocumentLint("step sequence [G] matches illegal pattern", "3"));
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "Then", "When")),
        containsDocumentLint("step sequence [GTW] matches illegal pattern", "5"));
    assertThat(rule.lint(null, createScenarioWithSteps(2, "Given", "Then", "When", "And")),
        containsDocumentLint("step sequence [GTW] matches illegal pattern", "6"));
  }

}