Feature: Login

  @id_LOGIN_001 @tag_example
  Scenario: LOGIN_001 User can log in
    Given the user is on the login page
    And something else
    When they enter valid credentials
    Then they should see their dashboard

  @tag_pattern1
  Scenario: LOGIN_002 User can log in only once
    Given the user is on the login page
    When they enter valid credentials
    Then they should see their dashboard

  @tag_pattern2
  Scenario Outline: <Id> User can log in <Iteration>
    Given the user is on the login page
    And something else
    When they enter valid credentials
    Then they should see their dashboard
    Examples:
      | Id        | Iteration |
      | LOGIN_003 | 1         |
      | LOGIN_004 | 2         |

  Scenario: LOGIN_005 User can log in again 5
    Given the user is on the login page
    Then they enter valid credentials

  Scenario: LOGIN_006 User can log in again 6
    Given the user is on the login page
    When they should see their dashboard
    Then they enter valid credentials

  Scenario: LOGIN_007 User can log in again 7
    Given the user is on the login page
    And something else
    Then they enter valid credentials
    When they should see their dashboard
    Then they enter valid credentials
