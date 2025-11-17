package com.example.gherkinlint;

import io.cucumber.messages.types.*;

import java.util.Collections;
import java.util.List;

public interface GherkinLintRule {

  record DocumentLint(String message, Location location) {
  }

  record PickleLint(String message, String nodeAstId) {
  }

  default List<DocumentLint> lint(Feature feature) {
    return Collections.emptyList();
  }

  default List<DocumentLint> lint(Background background, Scenario scenario) {
    return Collections.emptyList();
  }

  default List<PickleLint> lint(Pickle pickle) {
    return Collections.emptyList();
  }

  default void clear() {
  }
}
