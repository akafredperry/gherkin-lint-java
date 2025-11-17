package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleTag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class UniqueIdTagLintRule implements GherkinLintRule {

  private final Pattern idTagPattern;

  private final Set<String> ids = new HashSet<>();

  public UniqueIdTagLintRule(String... args) {
    this.idTagPattern = toPattern(args);
  }

  @Override
  public void clear() {
    ids.clear();
  }

  @Override
  public List<PickleLint> lint(Pickle pickle) {
    Matcher matcher = idTagPattern.matcher("");
    List<PickleTag> idTags = pickle.getTags().stream()
        .filter(tag -> matcher.reset(tag.getName()).matches())
        .toList();
    if (idTags.isEmpty()) {
      return singletonList(new PickleLint("is missing an id tag", pickle.getAstNodeIds().get(0)));
    } else if (idTags.size() > 1) {
      return singletonList(new PickleLint("multiple id tags", idTags.get(0).getAstNodeId()));
    } else {
      PickleTag tag = idTags.get(0);
      if (!ids.add(tag.getName())) {
        return singletonList(new PickleLint("duplicate id tag '" + tag.getName() + "' found.", tag.getAstNodeId()));
      }
    }
    return emptyList();
  }

  private static Pattern toPattern(String... args) {
    if (args.length == 0) {
      return Pattern.compile("@id_(.+)");
    } else if (args.length == 1) {
      return Pattern.compile(args[0]);
    } else {
      throw new IllegalArgumentException("expected single pattern as arg");
    }
  }

}
