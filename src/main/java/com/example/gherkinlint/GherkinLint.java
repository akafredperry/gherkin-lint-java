package com.example.gherkinlint;

import io.cucumber.messages.types.Location;

import java.nio.file.Path;

public record GherkinLint(Path featureFile, Location location, String message) {

  public GherkinLint(Path featureFile, String message) {
    this(featureFile, null, message);
  }

  @Override
  public String toString() {
    if (location == null) {
      return String.format("%s\n\tat %s(%s)", message, featureFile,
          featureFile.getName(featureFile.getNameCount() - 1));
    } else {
      return String.format("%s\n\tat %s(%s:%d)", message, featureFile,
          featureFile.getName(featureFile.getNameCount() - 1), location.getLine());
    }
  }
}
