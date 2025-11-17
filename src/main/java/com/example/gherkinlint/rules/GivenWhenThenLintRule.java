package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class GivenWhenThenLintRule implements GherkinLintRule {

  private static final String[] DEFAULT_ILLEGAL_PATTERNS = new String[]{
      "G$", "W$", "GT[^W]"
  };

  private List<Matcher> matchers;

  public GivenWhenThenLintRule(String... patterns) {
    matchers = toMatchers(patterns.length > 1 ? patterns : DEFAULT_ILLEGAL_PATTERNS);
  }

  @Override
  public List<DocumentLint> lint(Background background, Scenario scenario) {
    List<Step> steps = collateSteps(background, scenario);
    if (steps.isEmpty()) {
      return emptyList();
    }
    if (steps.get(0).getKeyword().equals("And")) {
      return singletonList(new DocumentLint("first step should NOT be And", steps.get(0).getLocation()));
    }
    String sequence = steps.stream()
        .filter(step -> !step.getKeyword().startsWith("And"))
        .map(step -> step.getKeyword().substring(0, 1))
        .collect(Collectors.joining());
    for (Matcher matcher : matchers) {
      if (matcher.reset(sequence).find()) {
        return singletonList(new DocumentLint(
            String.format("step sequence [%s] matches illegal pattern [%s]", sequence, matcher.pattern()),
            steps.get(steps.size() - 1).getLocation()));
      }
    }
    return emptyList();
  }

  private List<Step> collateSteps(Background background, Scenario scenario) {
    List<Step> steps = new ArrayList<>();
    if (background != null) {
      steps.addAll(background.getSteps());
    }
    if (scenario != null) {
      steps.addAll(scenario.getSteps());
    }
    return steps;
  }

  private static List<Matcher> toMatchers(String[] patterns) {
    List<Matcher> matchers = new ArrayList<>();
    for (int i = 0; i < patterns.length; i++) {
      matchers.add(Pattern.compile(toAbbreviations(patterns[i])).matcher(""));
    }
    return matchers;
  }

  private static String toAbbreviations(String s) {
    return s.replaceAll("Given", "G")
        .replaceAll("When", "W")
        .replaceAll("Then", "T")
        .replaceAll("But", "B");
  }
}
