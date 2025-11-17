package com.example.gherkinlint.rules;

import org.junit.jupiter.api.Test;

import static com.example.gherkinlint.rules.PickleTests.containsPickleLint;
import static com.example.gherkinlint.rules.PickleTests.createPickleWithName;
import static org.hamcrest.MatcherAssert.assertThat;

class UniqueIdNameLintRuleTest {

  @Test
  void testPicklesWithUniqueNamesDoNotCreateAnyLint() {
    UniqueNameLintRule rule = new UniqueNameLintRule();

    assertThat(rule.lint(createPickleWithName("test scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("test scenario 2")),
        containsPickleLint());
  }

  @Test
  void testPicklesWithDuplicateNamesCreateLint() {
    UniqueNameLintRule rule = new UniqueNameLintRule();

    assertThat(rule.lint(createPickleWithName("test scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("test scenario 2")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("test scenario 1")),
        containsPickleLint("duplicate pickle name: test scenario 1", "1"));
  }

  @Test
  void testClear() {
    UniqueNameLintRule rule = new UniqueNameLintRule();

    assertThat(rule.lint(createPickleWithName("test scenario 1")),
        containsPickleLint());
    rule.clear();
    assertThat(rule.lint(createPickleWithName("test scenario 1")),
        containsPickleLint());
  }

}