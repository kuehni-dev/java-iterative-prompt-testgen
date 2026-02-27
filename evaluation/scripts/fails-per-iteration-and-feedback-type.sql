WITH STEP1 AS (
    SELECT R.UUID                                                                RUN_UUID,
           T.CLASS_NAME,
           COALESCE(CASE WHEN I.NUMBER = 0 THEN NULL ELSE F.TYPE END, 'INITIAL') FEEDBACK_TYPE,
           I.ID                                                                  ITERATION_ID,
           I.NUMBER,
           I.COMPLETED_AT                                                        ITERATION_COMPLETED_AT,
           I.EVALUATION_ID
    FROM ITERATION I
             LEFT JOIN FEEDBACK_LOOP_ITERATION FI ON I.ID = FI.ITERATION_ID
             LEFT JOIN FEEDBACK_LOOP F ON FI.FEEDBACK_LOOP_ID = F.ID
             JOIN TARGET T ON I.TARGET_ID = T.ID
             JOIN RUN R ON T.RUN_ID = R.ID
    WHERE R.COMPLETED_AT IS NOT NULL
    GROUP BY FEEDBACK_TYPE, I.ID)
SELECT FEEDBACK_TYPE,
       STEP1.NUMBER,
       COUNT(*)                  STARTED,
       COUNT(CASE
                 WHEN (SELECT NOT S.SUCCESS
                       FROM FIXER_STAGE S
                       WHERE S.ITERATION_ID = STEP1.ITERATION_ID
                       ORDER BY S.CREATED_AT DESC
                       LIMIT 1)
                     THEN 1 END) FIXER_FAILED,
       COUNT(CASE
                 WHEN (SELECT S.SUCCESS
                       FROM FIXER_STAGE S
                       WHERE S.ITERATION_ID = STEP1.ITERATION_ID
                       ORDER BY S.CREATED_AT DESC
                       LIMIT 1) AND ITERATION_COMPLETED_AT IS NULL
                     THEN 1 END) VALID_BUT_NOT_COMPLETED,
       COUNT(CASE
                 WHEN (SELECT S.SUCCESS
                       FROM FIXER_STAGE S
                       WHERE S.ITERATION_ID = STEP1.ITERATION_ID
                       ORDER BY S.CREATED_AT DESC
                       LIMIT 1) AND ITERATION_COMPLETED_AT IS NOT NULL AND EVALUATION_ID IS NULL
                     THEN 1 END) COMPLETED_BUT_NO_EVAL,
       COUNT(CASE
                 WHEN (SELECT S.SUCCESS
                       FROM FIXER_STAGE S
                       WHERE S.ITERATION_ID = STEP1.ITERATION_ID
                       ORDER BY S.CREATED_AT DESC
                       LIMIT 1) AND ITERATION_COMPLETED_AT IS NOT NULL AND EVALUATION_ID IS NOT NULL
                     THEN 1 END) EVALUATED
FROM STEP1
GROUP BY FEEDBACK_TYPE, STEP1.NUMBER;
