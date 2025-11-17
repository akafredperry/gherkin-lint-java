Feature: Given When Then Tests

  Scenario: This is fine
    Given precondition
    When action
    Then result

  Scenario: This is fine
    Given precondition
    And some other precondition
    When action
    And some other action
    Then result

  Scenario: This is fine
    Given precondition
    And some other precondition
    When action
    And some other action
    Then result
    And another result

  Scenario: This is fine
    Given precondition
    When action
    Then result
    When some other action
    Then another result

  Scenario: This is fine
    When action
    Then result

  Scenario: This is fine
    Given precondition
    Then result
    And another result

  Scenario: This is fine
    Then result

  Scenario: This is NOT fine
    Given result

  Scenario: This is NOT fine
    Given precondition
    When result

  Scenario: This is NOT fine
    When result

  Scenario: This is NOT fine
    When action
    Given result

  Scenario: This is NOT fine
    Given precondition
    And some other precondition
    When action
    And some other action
    Then action
    When result
