package com.example.gherkinlint.rules;

import org.junit.jupiter.api.Test;

import static com.example.gherkinlint.rules.PickleTests.containsPickleLint;
import static com.example.gherkinlint.rules.PickleTests.createPickleWithName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueIdQualifiedNameLintRuleTest {

  @Test
  void testPicklesWithUniqueNamesDoNotCreateAnyLint() {
    UniqueIdQualifiedNameLintRule rule = new UniqueIdQualifiedNameLintRule();

    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-2 scenario 2")),
        containsPickleLint());
  }

  @Test
  void testPicklesWithDuplicateNamesCreateLint() {
    UniqueIdQualifiedNameLintRule rule = new UniqueIdQualifiedNameLintRule();

    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-2 scenario 2")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-3 scenario 1")),
        containsPickleLint("duplicate pickle name: scenario 1", "1"));
  }

  @Test
  void testPicklesWithDuplicateIdCreateLint() {
    UniqueIdQualifiedNameLintRule rule = new UniqueIdQualifiedNameLintRule();

    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-2 scenario 2")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 3")),
        containsPickleLint("duplicate pickle id: TEST-1", "1"));
  }

  @Test
  void testClear() {
    UniqueIdQualifiedNameLintRule rule = new UniqueIdQualifiedNameLintRule();

    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 1")),
        containsPickleLint());
    rule.clear();
    assertThat(rule.lint(createPickleWithName("TEST-1 scenario 2")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("TEST-2 scenario 1")),
        containsPickleLint());
  }

  @Test
  void testConstructorArgs() {
    UniqueIdQualifiedNameLintRule rule = new UniqueIdQualifiedNameLintRule("(ID-\\d+) (.+)");

    assertThat(rule.lint(createPickleWithName("scenario 1")),
        containsPickleLint("pickle name does not match pattern", "1"));

    assertThat(rule.lint(createPickleWithName("ID-0001 scenario 1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithName("ID-0002 scenario 1")),
        containsPickleLint("duplicate pickle name: scenario 1", "1"));
    assertThat(rule.lint(createPickleWithName("ID-0001 scenario 2")),
        containsPickleLint("duplicate pickle id: ID-0001", "1"));

    UniqueIdQualifiedNameLintRule rule2 = new UniqueIdQualifiedNameLintRule("(ID-\\d+)", "(.+)");
    assertThat(rule2.lint(createPickleWithName("ID-0001 scenario 1")),
        containsPickleLint());
    assertThat(rule2.lint(createPickleWithName("ID-0002 scenario 1")),
        containsPickleLint("duplicate pickle name: scenario 1", "1"));
    assertThat(rule2.lint(createPickleWithName("ID-0001 scenario 2")),
        containsPickleLint("duplicate pickle id: ID-0001", "1"));

    assertThrows(IllegalArgumentException.class, () -> new UniqueIdQualifiedNameLintRule("(ID-\\d+)"));
    assertThrows(IllegalArgumentException.class, () -> new UniqueIdQualifiedNameLintRule("(ID-\\d+).+"));
    assertThrows(IllegalArgumentException.class, () -> new UniqueIdQualifiedNameLintRule("(ID-\\d+)(\\s)(.+)"));

  }


}