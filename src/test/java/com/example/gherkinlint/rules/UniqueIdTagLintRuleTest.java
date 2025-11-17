package com.example.gherkinlint.rules;

import org.junit.jupiter.api.Test;

import static com.example.gherkinlint.rules.PickleTests.containsPickleLint;
import static com.example.gherkinlint.rules.PickleTests.createPickleWithTags;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueIdTagLintRuleTest {

  @Test
  void testMissingIdTagReturnsNoLintIfPickleHasIdTag() {
    UniqueIdTagLintRule rule = new UniqueIdTagLintRule();

    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@id_12346", "@other_tag")),
        containsPickleLint());
  }

  @Test
  void testPickleWithMultipleIdTagsReturnsExpectedLint() {
    UniqueIdTagLintRule rule = new UniqueIdTagLintRule();

    assertThat(rule.lint(createPickleWithTags("@id_12345", "@id_12346")),
        containsPickleLint("multiple id tags", "2"));
  }

  @Test
  void testPickleWithSameIdTagAsAnotherPickleReturnsExpectedLint() {
    UniqueIdTagLintRule rule = new UniqueIdTagLintRule();

    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint("duplicate id tag '@id_12345' found", "2"));

    assertThat(rule.lint(createPickleWithTags("@id_12346")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@other_tag", "@id_12346")),
        containsPickleLint("duplicate id tag '@id_12346' found", "3"));
  }

  @Test
  void testClear() {
    UniqueIdTagLintRule rule = new UniqueIdTagLintRule();

    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint());
    rule.clear();
    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint());
  }

  @Test
  void testConstructorArgs() {
    UniqueIdTagLintRule rule = new UniqueIdTagLintRule("@ID-(\\d+)");

    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint("is missing an id tag", "1"));
    assertThat(rule.lint(createPickleWithTags("@ID-12345")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@ID-12345")),
        containsPickleLint("duplicate id tag '@ID-12345' found", "2"));

    assertThrows(IllegalArgumentException.class, () -> new UniqueIdTagLintRule("@id-.+", "@ID-.+"));
  }

}