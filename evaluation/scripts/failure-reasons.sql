WITH STEP1 AS (
    SELECT R.D4J_PROJECT,
           F.TYPE                                             FEEDBACK_TYPE,
           COALESCE(R.ERROR_STACK_TRACE, F.ERROR_STACK_TRACE) ERROR
    FROM RUN R
             LEFT JOIN TARGET T ON R.ID = T.RUN_ID
             LEFT JOIN FEEDBACK_LOOP F ON F.TARGET_ID = T.ID
    WHERE (R.ERROR_STACK_TRACE IS NOT NULL OR F.ERROR_STACK_TRACE IS NOT NULL) AND R.COMPLETED_AT IS NOT NULL)
SELECT D4J_PROJECT,
       FEEDBACK_TYPE,
       COUNT(*)           FREQUENCY,
       CASE
           WHEN ERROR LIKE '%Mutation testing failed with exit code%TestEvaluator%' THEN 'Evaluation (test fail)'
           WHEN ERROR LIKE '%Ran out of reprompts%TestFixPipeline%' THEN 'Fixer (out of reprompts)'
           WHEN ERROR LIKE '%Expected exactly one top-level%TestFixPipeline%' THEN 'Fixer (parse exception)'
           WHEN ERROR LIKE '%methodNames must not be empty%TestFixPipeline%' THEN 'Fixer (no test methods)'
           WHEN ERROR LIKE '%Testing failed with exit code%TestFixPipeline%' THEN 'Fixer (test exception)'
           WHEN ERROR LIKE '%com.openai.errors.PermissionDeniedException%' THEN 'OpenAI API error (other)'
           WHEN ERROR LIKE
                '%com.openai.errors.BadRequestException: 400: Your input exceeds the context window of this model%'
               THEN 'OpenAI API error (context)'
           ELSE ERROR END ERROR
FROM STEP1
GROUP BY D4J_PROJECT, FEEDBACK_TYPE, ERROR
ORDER BY D4J_PROJECT, FEEDBACK_TYPE, FREQUENCY DESC;
