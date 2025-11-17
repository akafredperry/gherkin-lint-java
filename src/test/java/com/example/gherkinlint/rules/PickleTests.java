package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleTag;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PickleTests {

  public static Matcher<List<GherkinLintRule.PickleLint>> containsPickleLint(String... expectedMessagesAndNodeIds) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(List<GherkinLintRule.PickleLint> lint) {
        if (lint.size() != expectedMessagesAndNodeIds.length / 2) {
          return false;
        }
        for (int i = 0; i < lint.size(); i++) {
          if (!lint.get(i).message().contains(expectedMessagesAndNodeIds[i * 2])) {
            return false;
          }
          if (!lint.get(i).nodeAstId().equals(expectedMessagesAndNodeIds[(i * 2) + 1])) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        if (expectedMessagesAndNodeIds.length == 2) {
          description.appendText("lint message [" + expectedMessagesAndNodeIds[0] + "][" + expectedMessagesAndNodeIds[1] + "]");
        } else {
          description.appendText("lint messages");
          for (int i = 0; i < expectedMessagesAndNodeIds.length; i++) {
            description.appendText(" [" + expectedMessagesAndNodeIds[i] + "][" + expectedMessagesAndNodeIds[++i] + "]");
          }
        }
      }

      @Override
      protected void describeMismatchSafely(List<GherkinLintRule.PickleLint> lint, Description mismatch) {
        if (lint.isEmpty()) {
          mismatch.appendText("no lint");
          return;
        }
        if (lint.size() > 1) {
          mismatch.appendText("multiple lint");
          return;
        }
        mismatch.appendText("lint messages");
        for (GherkinLintRule.PickleLint pickleLint : lint) {
          mismatch.appendText(" [" + pickleLint.message() + "][" + pickleLint.nodeAstId() + "]");
        }
      }
    };
  }

  public static Pickle createPickleWithTags(String... tags) {
    AtomicInteger counter = new AtomicInteger(1);
    return new Pickle(
        UUID.randomUUID().toString(),
        "http://test.uri",
        "Test Pickle",
        "en",
        List.of(),
        Arrays.stream(tags).map(s -> new PickleTag(s, String.valueOf(counter.incrementAndGet()))).toList(),
        List.of("1")
    );
  }

  public static Pickle createPickleWithName(String name) {
    return new Pickle(
        UUID.randomUUID().toString(),
        "http://test.uri",
        name,
        "en",
        List.of(),
        List.of(),
        List.of("1")
    );
  }
}
