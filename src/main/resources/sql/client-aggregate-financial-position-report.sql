SELECT CONCAT_WS(' ', c.TITLE, c.NAME, c.SURNAME) AS "Client",

       ROUND(COALESCE(SUM(
                              CASE
                                  WHEN ca.ACCOUNT_TYPE_CODE IN ('PLOAN', 'HLOAN')
                                      THEN ca.DISPLAY_BALANCE
                                  ELSE 0
                                  END
                      ), 0), 2)                   AS "Loan Balance",

       ROUND(COALESCE(SUM(
                              CASE
                                  WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'CHQ'
                                      THEN ca.DISPLAY_BALANCE + 10000
                                  WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'SVGS'
                                      THEN ca.DISPLAY_BALANCE
                                  WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'CCRD'
                                      THEN ca.DISPLAY_BALANCE - COALESCE(ccl.ACCOUNT_LIMIT, 0)
                                  ELSE 0
                                  END
                      ), 0), 2)                   AS "Transactional Balance",

       ROUND(
               COALESCE(SUM(
                                CASE
                                    WHEN ca.ACCOUNT_TYPE_CODE IN ('PLOAN', 'HLOAN')
                                        THEN ca.DISPLAY_BALANCE
                                    ELSE 0
                                    END
                        ), 0)
                   +
               COALESCE(SUM(
                                CASE
                                    WHEN ca.ACCOUNT_TYPE_CODE = 'CFCA'
                                        THEN CASE
                                                 WHEN ccr.CONVERSION_INDICATOR = '*' THEN ca.DISPLAY_BALANCE * ccr.RATE
                                                 WHEN ccr.CONVERSION_INDICATOR = '/' THEN ca.DISPLAY_BALANCE / ccr.RATE
                                                 ELSE ca.DISPLAY_BALANCE
                                        END
                                    ELSE 0
                                    END
                        ), 0)
                   +
               COALESCE(SUM(
                                CASE
                                    WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'CHQ'
                                        THEN ca.DISPLAY_BALANCE + 10000
                                    WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'SVGS'
                                        THEN ca.DISPLAY_BALANCE
                                    WHEN at.TRANSACTIONAL = TRUE AND ca.ACCOUNT_TYPE_CODE = 'CCRD'
                                        THEN ca.DISPLAY_BALANCE - COALESCE(ccl.ACCOUNT_LIMIT, 0)
                                    ELSE 0
                                    END
                        ), 0)
           , 2)                                   AS "Net Position"

FROM CLIENT c
         LEFT JOIN CLIENT_ACCOUNT ca ON ca.CLIENT_ID = c.CLIENT_ID
         LEFT JOIN ACCOUNT_TYPE at ON ca.ACCOUNT_TYPE_CODE = at.ACCOUNT_TYPE_CODE
         LEFT JOIN CURRENCY_CONVERSION_RATE ccr ON ca.CURRENCY_CODE = ccr.CURRENCY_CODE
         LEFT JOIN CREDIT_CARD_LIMIT ccl ON ca.CLIENT_ACCOUNT_NUMBER = ccl.CLIENT_ACCOUNT_NUMBER
GROUP BY c.CLIENT_ID, c.TITLE, c.NAME, c.SURNAME
ORDER BY c.CLIENT_ID