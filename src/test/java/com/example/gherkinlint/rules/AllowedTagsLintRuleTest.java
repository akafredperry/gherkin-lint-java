package com.example.gherkinlint.rules;

import org.junit.jupiter.api.Test;

import static com.example.gherkinlint.rules.PickleTests.containsPickleLint;
import static com.example.gherkinlint.rules.PickleTests.createPickleWithTags;
import static org.hamcrest.MatcherAssert.assertThat;

class AllowedTagsLintRuleTest {

  @Test
  void testDefaultConstructorAllowsAllTags() {
    AllowedTagsLintRule rule = new AllowedTagsLintRule();

    assertThat(rule.lint(createPickleWithTags("@any_tag")), containsPickleLint());
  }

  @Test
  void testSinglePatternWithSingleTag() {
    AllowedTagsLintRule rule = new AllowedTagsLintRule("@test.*");

    assertThat(rule.lint(createPickleWithTags("@test123")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@tag123")),
        containsPickleLint("invalid tag : @tag123", "2"));
  }

  @Test
  void testSinglePatternWithMultipleTags() {
    AllowedTagsLintRule rule = new AllowedTagsLintRule("@test.*");

    assertThat(rule.lint(createPickleWithTags("@test1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@test1", "@test2")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@xxx")),
        containsPickleLint("invalid tag : @xxx", "2"));
    assertThat(rule.lint(createPickleWithTags("@xxx", "@yyy")),
        containsPickleLint("invalid tag : @xxx", "2", "invalid tag : @yyy", "3"));
    assertThat(rule.lint(createPickleWithTags("@test1", "@xxx")),
        containsPickleLint("invalid tag : @xxx", "3"));
  }

  @Test
  void testMultiplePatternsWithSingleTag() {
    AllowedTagsLintRule rule = new AllowedTagsLintRule("@id_.+", "@priority_.+", "@exclude");

    assertThat(rule.lint(createPickleWithTags("@id_12345")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@priority_1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@exclude")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@test1")),
        containsPickleLint("invalid tag : @test1", "2"));
  }


  @Test
  void testMultiplePatternsWithMultipleTags() {
    AllowedTagsLintRule rule = new AllowedTagsLintRule("@id_.+", "@priority_.+", "@exclude");

    assertThat(rule.lint(createPickleWithTags("@id_12345", "@priority_1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@id_12345", "@exclude")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@exclude", "@priority_1")),
        containsPickleLint());
    assertThat(rule.lint(createPickleWithTags("@test1", "@exclude")),
        containsPickleLint("invalid tag : @test1", "2"));
    assertThat(rule.lint(createPickleWithTags("@exclude", "@test1")),
        containsPickleLint("invalid tag : @test1", "3"));
    assertThat(rule.lint(createPickleWithTags("@id_12345", "@priority_1", "@random-tag")),
        containsPickleLint("invalid tag : @random-tag", "4"));
  }

}