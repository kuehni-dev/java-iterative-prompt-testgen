SELECT R.UUID                                                        RUN_UUID,
--        R.CREATED_AT                                                  RUN_START,
--        R.COMPLETED_AT                                                RUN_COMPLETED,
       R.COMPLETED_AT IS NOT NULL                                    RUN_COMPLETED,
       DATEDIFF(MILLISECOND, R.CREATED_AT, R.COMPLETED_AT) / 60000.0 RUN_DURATION_MINUTES,
       R.SUCCESS                                                     RUN_SUCCESSFUL,
       R.D4J_PROJECT,
       R.D4J_BUG_ID,
       R.MODEL,
       R.REASONING_LEVEL,
       R.MAX_FIX_REPROMPTS,
       R.FEEDBACK_ITERATIONS,
       T.CLASS_NAME,
       F.TYPE                                                        FEEDBACK_TYPE,
--        F.CREATED_AT                                                  FEEDBACK_LOOP_START,
--        F.COMPLETED_AT                                                FEEDBACK_LOOP_COMPLETED,
       DATEDIFF(MILLISECOND, F.CREATED_AT, F.COMPLETED_AT) / 60000.0 FEEDBACK_LOOP_DURATION_MINUTES,
       F.SUCCESS                                                     FEEDBACK_LOOP_SUCCESSFUL,
       I.NUMBER                                                      ITERATION,
--        I.CREATED_AT                                                  ITERATION_STARTED,
--        I.COMPLETED_AT                                                ITERATION_COMPLETED,
       DATEDIFF(MILLISECOND, I.CREATED_AT, I.COMPLETED_AT) / 60000.0 ITERATION_DURATION_MINUTES,
       (SELECT COUNT(*)
        FROM FIXER_STAGE S
        WHERE S.ITERATION_ID = I.ID
          AND S.SUCCESS = FALSE
          AND S.COMPLETED_AT IS NOT NULL)                            FIX_REPROMPT_COUNT,
       E.REAL_BUG_RESULT,
       E.MUTANTS,
       E.COVERED_MUTANTS,
       E.KILLED_MUTANTS,
       E.LINE_COVERAGE,
       E.BRANCH_COVERAGE
FROM RUN R
         LEFT JOIN TARGET T ON R.ID = T.RUN_ID
         LEFT JOIN ITERATION I ON I.TARGET_ID = T.ID
         LEFT JOIN FEEDBACK_LOOP_ITERATION FI ON FI.ITERATION_ID = I.ID
         LEFT JOIN FEEDBACK_LOOP F ON F.TARGET_ID = T.ID AND F.ID = FI.FEEDBACK_LOOP_ID
         LEFT JOIN EVALUATION E ON E.ID = I.EVALUATION_ID
GROUP BY R.ID, T.CLASS_NAME, F.TYPE, I.NUMBER
ORDER BY R.ID, T.CLASS_NAME, F.TYPE, I.NUMBER;
