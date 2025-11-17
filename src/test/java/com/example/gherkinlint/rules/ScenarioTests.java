package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScenarioTests {

  public static Matcher<List<GherkinLintRule.DocumentLint>> containsDocumentLint(String... expectedMessagesLocations) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(List<GherkinLintRule.DocumentLint> lint) {
        if (lint.size() != expectedMessagesLocations.length / 2) {
          return false;
        }
        for (int i = 0; i < lint.size(); i++) {
          if (!lint.get(i).message().contains(expectedMessagesLocations[i * 2])) {
            return false;
          }
          if (!String.valueOf(lint.get(i).location().getLine()).equals(expectedMessagesLocations[(i * 2) + 1])) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        if (expectedMessagesLocations.length == 2) {
          description.appendText("lint message [" + expectedMessagesLocations[0] + "][" + expectedMessagesLocations[1] + "]");
        } else {
          description.appendText("lint messages");
          for (int i = 0; i < expectedMessagesLocations.length; i++) {
            description.appendText(" [" + expectedMessagesLocations[i] + "][" + expectedMessagesLocations[++i] + "]");
          }
        }
      }

      @Override
      protected void describeMismatchSafely(List<GherkinLintRule.DocumentLint> lint, Description mismatch) {
        if (lint.isEmpty()) {
          mismatch.appendText("no lint");
          return;
        }
        if (lint.size() > 1) {
          mismatch.appendText("multiple lint");
          return;
        }
        mismatch.appendText("lint messages");
        for (GherkinLintRule.DocumentLint pickleLint : lint) {
          mismatch.appendText(" [" + pickleLint.message() + "][" + pickleLint.location().getLine() + "]");
        }
      }
    };
  }

  public static Scenario createScenarioWithSteps(long lineNumber, String... keywords) {
    List<Step> steps = new ArrayList<>();
    for (int i = 0; i < keywords.length; i++) {
      steps.add(createStep(keywords[i], new Location((long) i + lineNumber + 1, 1L)));
    }
    return new Scenario(
        new Location(lineNumber, 1L),
        List.of(),
        "Scenario",
        "name",
        "description",
        steps,
        List.of(),
        UUID.randomUUID().toString()
    );
  }

  public static Background createBackgroundWithSteps(long lineNumber, String... keywords) {
    List<Step> steps = new ArrayList<>();
    for (int i = 0; i < keywords.length; i++) {
      steps.add(createStep(keywords[i], new Location((long) i + lineNumber + 1, 1L)));
    }
    return new Background(
        new Location(lineNumber, 1L),
        "Background",
        "test background",
        "",
        steps,
        UUID.randomUUID().toString()
    );
  }

  public static Step createStep(String keyword, Location location) {
    return new Step(
        location,
        keyword,
        toStepKeywordType(keyword),
        "step text",
        null,
        null,
        "step-id"
    );
  }

  private static StepKeywordType toStepKeywordType(String keyword) {
    if (keyword.equalsIgnoreCase("Given")) {
      return StepKeywordType.CONTEXT;
    } else if (keyword.equalsIgnoreCase("Then")) {
      return StepKeywordType.OUTCOME;
    } else if (keyword.equalsIgnoreCase("When")) {
      return StepKeywordType.ACTION;
    } else if (keyword.equalsIgnoreCase("But")) {
      return StepKeywordType.CONJUNCTION;
    } else if (keyword.equalsIgnoreCase("And")) {
      return StepKeywordType.CONJUNCTION;
    } else {
      return StepKeywordType.UNKNOWN;
    }
  }

  public static Step stepWithLocation(String keyword, Location location) {
    return createStep(keyword, location);
  }
}
