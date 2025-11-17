Feature: Given When Then Tests With Background

  Background:
    Given precondition
    And some other precondition

  Scenario: This is fine
    When action
    Then result

  Scenario: This is fine
    And yet another other precondition
    When action
    And some other action
    Then result

  Scenario: This is fine
    When action
    And some other action
    Then result
    And another result

  Scenario: This is fine
    When action
    Then result
    When some other action
    Then another result

  Scenario: This is NOT fine
    When action
    And some other action
    Then action
    When result
