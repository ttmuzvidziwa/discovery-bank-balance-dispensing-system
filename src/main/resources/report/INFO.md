# Reporting Functionality

- **NB:** Monthly reports will be automatically generated and stored in this directory (resources/report).

## Aggregate Financial Position Calculation
- **Loan Balance**: Adds up the balances of all personal and home loan accounts for each client.
- **Transactional Balance**: Adds up the balances of all transactional accounts (like cheque, savings, and credit card accounts), with some adjustments (e.g., adding 10,000 for cheque accounts, subtracting the amount spent on a credit card (display_balance -account limit) as it will need to be paid back).
- **Net Position**: Combines the total loan balances, foreign currency account balances (converted to ZAR balances), and transactional balances to show the client’s overall financial position.

## Transactional Accounts With Highest Balance
- This query creates a report that lists, for each client, the account they have with the highest balance. For every client, it shows their ID, surname, the account number, a description of the account, and the balance (rounded to two decimal places). The report only includes the account with the highest balance for each client and sorts the results by client ID.