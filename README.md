# Gherkin Lint Java
Java implementation for "linting" cucumber features.

Intended to be used to enforce QA policies across cucumber feature files.
For example:
* Scenarios must have unique id
* Scenarios must have a unique name
* Scenarios steps must follow legitimate Given, When, Then patterns
* Cucumber tags must conform a specific structure
* No arbitrary cucumber tags
* All Scenarios must have a unique id tag

This project includes rule implementations for the above but allows you to
create rules for want ever policies you want to enforce.

# Example Usage

## Step by step

1. Instantiate a linter
~~~
    GherkinLinter linter = new GherkinLinter();
~~~
2. Register lint rules
Register rules based on properties on classpath
~~~
    linter.registerRules("gherkin-lint.properties");
~~~
Register rules based on properties
~~~
    Properties properties = new Properties();
    properties.setProperty("rule.1.class", "com.example.gherkinlint.rules.UniqueNameLintRule");
    properties.setProperty("rule.2.class", "com.example.gherkinlint.rules.GivenWhenThenLintRule");
    properties.setProperty("rule.3.class", "com.example.gherkinlint.rules.AllowedTagsLintRule");
    properties.setProperty("rule.3.args", "@smoke @version_\\d+");
    linter.registerRules(properties);
~~~
Register rules explicitly
~~~
    linter.registerRule(new UniqueNameLintRule()).registerRule(new GivenWhenThenLintRule());
~~~

3. Invoke the linter
Invoke for specific file
~~~
    linter.lint(new File("src/test/features/test.feature"));
~~~
Invoke for any feature files under a directory
~~~
    linter.lint(new File("src/test/features""));
~~~
4. Get results
Get any lint results
~~~
    List<GherkinLint> results = linter.getResults();
~~~
or assert that there is no lint (this will throw an exception if there is lint)
~~~
    linter.assertNoLint();
~~~

## Invoke GherkinLinter main()
GherkinLinter has a main method that will instantiate a linter, register rules, collect lint and assert that there is
no lint found.
If no args are supplied then it will attempt to load rules from a classpath resource with the name 
"gherkin-lint.properties" and lint any feature files under src/test/resources.
~~~
  java com.example.gherkinlint.GherkinLinter
~~~
If args are supplied then any args which end in ".properties" are assumed to be rules properties. All other args are
assumed to be feature file locations.
~~~
  java com.example.gherkinlint.GherkinLinter gherkin-lint.properties src/test/resources/unit-test-features
~~~

## Run linting as part of unit tests
~~~
  @Test
  void lintFeatureFiles() {
    new GherkinLinter()
        .registerRule(new UniqueNameLintRule())
        .registerRule(new GivenWhenThenLintRule())
        .lint(new File("src/test/resources"))
        .assertNoLint();
  }
~~~