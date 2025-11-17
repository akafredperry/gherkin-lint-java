package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Pickle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllowedTagsLintRule implements GherkinLintRule {

  private final List<Matcher> matchers = new ArrayList<>();

  public AllowedTagsLintRule(String... patterns) {
    for (String s : patterns.length > 0 ? patterns : new String[]{"@.+"}) {
      matchers.add(Pattern.compile(s).matcher(""));
    }
  }

  @Override
  public List<PickleLint> lint(Pickle pickle) {
    List<PickleLint> lint = new ArrayList<>();
    for (var tag : pickle.getTags()) {
      String name = tag.getName();
      if (!isAllowedPattern(name)) {
        lint.add(new PickleLint("invalid tag : " + name, tag.getAstNodeId()));
      }
    }
    return lint;
  }

  private boolean isAllowedPattern(String s) {
    return matchers.stream().anyMatch(matcher -> matcher.reset(s).matches());
  }
}
