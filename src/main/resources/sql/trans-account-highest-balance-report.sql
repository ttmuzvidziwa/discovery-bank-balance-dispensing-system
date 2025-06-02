SELECT c.CLIENT_ID                  AS "Client Id",
       c.SURNAME                    AS "Client Surname",
       ca.CLIENT_ACCOUNT_NUMBER     AS "Client Account Number",
       act.DESCRIPTION              AS "Account Description",
       ROUND(ca.DISPLAY_BALANCE, 2) AS "Display Balance"
FROM CLIENT c
         JOIN
     CLIENT_ACCOUNT ca ON ca.CLIENT_ID = c.CLIENT_ID
         JOIN
     ACCOUNT_TYPE act ON ca.ACCOUNT_TYPE_CODE = act.ACCOUNT_TYPE_CODE
WHERE ca.DISPLAY_BALANCE = (
    SELECT MAX(ca2.DISPLAY_BALANCE)
    FROM CLIENT_ACCOUNT ca2
    WHERE ca2.CLIENT_ID = c.CLIENT_ID
    )
ORDER BY c.CLIENT_ID