package com.example.gherkinlint.rules;

import com.example.gherkinlint.GherkinLintRule;
import io.cucumber.messages.types.Pickle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class UniqueNameLintRule implements GherkinLintRule {

  private final Set<String> names = new HashSet<>();

  @Override
  public void clear() {
    names.clear();
  }

  @Override
  public List<PickleLint> lint(Pickle pickle) {
    if (!names.add(pickle.getName())) {
      return singletonList(new PickleLint("duplicate pickle name: " + pickle.getName(), pickle.getAstNodeIds().get(0)));
    }
    return emptyList();
  }
}
