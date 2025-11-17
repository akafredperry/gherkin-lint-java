package com.example.gherkinlint;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.*;

import java.io.File;
import java.io.InputStream;
import java.lang.Exception;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GherkinLinter {

  private final GherkinParser parser = GherkinParser.builder().build();

  private final List<GherkinLintRule> rules = new ArrayList<>();

  private final Map<String, Location> locations = new HashMap<>();

  private final List<GherkinLint> results = new ArrayList<>();

  public static void main(String[] args) {
    GherkinLinter linter = new GherkinLinter();
    toLintProperties(args, "gherkin-lint.properties").forEach(linter::registerRules);
    toFeatureLocations(args, new File("src/test/resources")).forEach(linter::lint);
    linter.assertNoLint();
  }

  public void assertNoLint() {
    if (!results.isEmpty()) {
      throw new RuntimeException("Gherkin Lint Exception\n" +
          results.stream().map(GherkinLint::toString).collect(Collectors.joining("\n")));
    }
  }

  public GherkinLinter registerRule(GherkinLintRule rule) {
    rules.add(rule);
    return this;
  }

  public GherkinLinter registerRules(Properties properties) {
    for (String className : getRuleClassNames(properties)) {
      registerRule(createRuleInstance(className, getRuleArgs(className, properties)));
    }
    return this;
  }

  public GherkinLinter registerRules(String propertiesFilename) {
    return registerRules(createProperties(propertiesFilename));
  }

  public List<GherkinLintRule> getRules() {
    return rules;
  }

  public GherkinLinter lint(File featureFileOrDir) {
    if (!featureFileOrDir.exists()) {
      throw new IllegalArgumentException(featureFileOrDir + " does not exist");
    } else if (featureFileOrDir.isDirectory()) {
      try (Stream<Path> paths = Files.walk(featureFileOrDir.toPath())) {
        paths.filter(p -> p.toString().endsWith(".feature")).forEach(this::lintFeatureFile);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (featureFileOrDir.getName().endsWith(".feature")) {
      lintFeatureFile(featureFileOrDir.toPath());
    } else {
      throw new IllegalArgumentException("expected directory or feature file");
    }
    return this;
  }

  public GherkinLinter clear() {
    results.clear();
    rules.forEach(GherkinLintRule::clear);
    return this;
  }

  public List<GherkinLint> getResults() {
    return results;
  }

  private void lintFeatureFile(Path path) {
    try {
      parser.parse(path).forEach(envelope -> processEnvelope(envelope, path));
    } catch (Exception e) {
      results.add(new GherkinLint(path, e.getMessage()));
    }
  }

  private void processEnvelope(Envelope envelope, Path path) {
    if (envelope.getParseError().isPresent()) {
      results.add(new GherkinLint(path, envelope.getParseError().get().getMessage()));
    } else if (envelope.getGherkinDocument().isPresent()) {
      locations.clear();
      GherkinDocument document = envelope.getGherkinDocument().get();
      collateLocations(document);
      if (document.getFeature().isPresent()) {
        Feature feature = document.getFeature().get();
        lint(feature, path);
        lintScenarios(feature, path);
      }
    } else if (envelope.getPickle().isPresent()) {
      lintPickle(envelope.getPickle().get(), path);
    }
  }

  private void lint(Feature feature, Path path) {
    for (GherkinLintRule rule : rules) {
      try {
        for (GherkinLintRule.DocumentLint lint : rule.lint(feature)) {
          results.add(new GherkinLint(path, lint.location(), lint.message()));
        }
      } catch (Exception e) {
        results.add(new GherkinLint(path, feature.getLocation(), e.getMessage()));
      }
    }
  }

  private void lintScenarios(Feature feature, Path path) {
    Background background = null;
    for (FeatureChild child : feature.getChildren()) {
      if (child.getBackground().isPresent()) {
        background = child.getBackground().get();
      } else if (child.getScenario().isPresent()) {
        lintScenario(background, child.getScenario().get(), path);
      }
    }
  }

  private void lintScenario(Background background, Scenario scenario, Path path) {
    for (GherkinLintRule rule : rules) {
      try {
        for (GherkinLintRule.DocumentLint lint : rule.lint(background, scenario)) {
          results.add(new GherkinLint(path, lint.location(), lint.message()));
        }
      } catch (Exception e) {
        results.add(new GherkinLint(path, scenario.getLocation(), e.getMessage()));
      }
    }
  }

  private void lintPickle(Pickle pickle, Path path) {
    for (GherkinLintRule rule : rules) {
      try {
        for (GherkinLintRule.PickleLint lint : rule.lint(pickle)) {
          results.add(new GherkinLint(path, getLocation(lint), lint.message()));
        }
      } catch (Exception e) {
        results.add(new GherkinLint(path, getLocation(pickle), e.getMessage()));
      }
    }
  }

  private Location getLocation(GherkinLintRule.PickleLint lint) {
    return lint.nodeAstId() != null ? locations.get(lint.nodeAstId()) : null;
  }

  private Location getLocation(Pickle pickle) {
    return !pickle.getAstNodeIds().isEmpty() ? locations.get(pickle.getAstNodeIds().get(0)) : null;
  }

  private void collateLocations(GherkinDocument document) {
    if (document.getFeature().isPresent()) {
      collateLocations(document.getFeature().get());
    }
  }

  private void collateLocations(Feature feature) {
    feature.getTags().forEach(tag -> locations.put(tag.getId(), tag.getLocation()));
    feature.getChildren().forEach(this::collateLocations);
  }

  private void collateLocations(FeatureChild child) {
    if (child.getBackground().isPresent()) {
      collateLocations(child.getBackground().get());
    }
    if (child.getScenario().isPresent()) {
      collateLocations(child.getScenario().get());
    }
  }

  private void collateLocations(Background background) {
    locations.put(background.getId(), background.getLocation());
    background.getSteps().forEach(this::collateLocations);
  }

  private void collateLocations(Step step) {
    locations.put(step.getId(), step.getLocation());
    if (step.getDataTable().isPresent()) {
      collateLocations(step.getDataTable().get());
    }
  }

  private void collateLocations(DataTable dataTable) {
    dataTable.getRows().forEach(row -> locations.put(row.getId(), row.getLocation()));
  }

  private void collateLocations(Scenario scenario) {
    locations.put(scenario.getId(), scenario.getLocation());
    scenario.getTags().forEach(tag -> locations.put(tag.getId(), tag.getLocation()));
    scenario.getSteps().forEach(this::collateLocations);
    scenario.getExamples().forEach(this::collateLocations);
  }

  private void collateLocations(Examples example) {
    locations.put(example.getId(), example.getLocation());
    example.getTags().forEach(tag -> locations.put(tag.getId(), tag.getLocation()));
    example.getTableBody().forEach(row -> locations.put(row.getId(), row.getLocation()));
  }

  private static List<String> toLintProperties(String[] args, String defaultValue) {
    List<String> result = Arrays.stream(args)
        .filter(arg -> arg.endsWith(".properties"))
        .toList();
    return !result.isEmpty() ? result : Collections.singletonList(defaultValue);
  }

  private static List<File> toFeatureLocations(String[] args, File defaultValue) {
    List<File> result = Arrays.stream(args)
        .filter(arg -> !arg.endsWith(".properties"))
        .map(File::new)
        .toList();
    return !result.isEmpty() ? result : Collections.singletonList(defaultValue);
  }

  private static GherkinLintRule createRuleInstance(String className, String[] args) {
    try {
      Class<?> klass = Class.forName(className);
      try {
        return (GherkinLintRule) klass.getConstructor(String[].class).newInstance((Object) args);
      } catch (NoSuchMethodException ignored) {
      }
      return (GherkinLintRule) klass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("failed to instantiate: " + className, e);
    }
  }

  private static Properties createProperties(String propertiesFilename) {
    URL propertiesUrl = GherkinLinter.class.getClassLoader().getResource(propertiesFilename);
    if (propertiesUrl == null) {
      throw new RuntimeException("no " + propertiesFilename + " on classpath");
    }
    Properties properties = new Properties();
    try (InputStream in = propertiesUrl.openStream()) {
      properties.load(in);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    for (String key : properties.stringPropertyNames()) {
      properties.setProperty(key, properties.getProperty(key).trim());
    }
    return properties;
  }

  private static List<String> getRuleClassNames(Properties properties) {
    List<String> classes = new ArrayList<>();
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith("rule.") && key.endsWith(".class")) {
        classes.add(properties.getProperty(key));
      }
    }
    return classes;
  }

  private static String[] getRuleArgs(String className, Properties properties) {
    String prefix = null;
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith("rule.") && key.endsWith(".class") && properties.getProperty(key).equals(className)) {
        prefix = key.substring(0, key.length() - ".class".length());
      }
    }
    return prefix != null ? toArgArray(properties.getProperty(prefix + ".args")) : new String[0];
  }

  private static String[] toArgArray(String whiteSpaceSeparatedArgs) {
    if (whiteSpaceSeparatedArgs == null) {
      return new String[0];
    } else {
      return whiteSpaceSeparatedArgs.split("\\s+");
    }
  }

}
