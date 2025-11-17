package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Pickle;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class UniqueIdQualifiedNameLintRule implements GherkinLintRule {

  private final Pattern pattern;

  private final Set<String> names = new HashSet<>();

  private final Set<String> ids = new HashSet<>();

  public UniqueIdQualifiedNameLintRule(String... args) {
    this(toPattern(args));
  }

  private static @NotNull Pattern toPattern(String[] args) {
    if (args.length == 0) {
      return Pattern.compile("(\\S+)\\s+(.+)");
    } else {
      return Pattern.compile(String.join(" ", args));
    }
  }

  public UniqueIdQualifiedNameLintRule(Pattern pattern) {
    if (pattern.matcher("").groupCount() != 2) {
      throw new IllegalArgumentException("expected pattern with 2 groups (id followed by name)");
    }
    this.pattern = pattern;
  }

  @Override
  public void clear() {
    ids.clear();
    names.clear();
  }

  @Override
  public List<PickleLint> lint(Pickle pickle) {
    Matcher matcher = pattern.matcher(pickle.getName());
    String nodeId = pickle.getAstNodeIds().get(0);
    if (!matcher.matches()) {
      return singletonList(new PickleLint(format("pickle name does not match pattern [%s]: %s", pattern.pattern(),
          pickle.getName()), nodeId));
    }
    if (!ids.add(matcher.group(1))) {
      return singletonList(new PickleLint("duplicate pickle id: " + matcher.group(1), nodeId));
    }
    if (!names.add(matcher.group(2))) {
      return singletonList(new PickleLint("duplicate pickle name: " + matcher.group(2), nodeId));
    }
    return emptyList();
  }
}
