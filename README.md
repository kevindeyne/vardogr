# bclone
CLI that can clone production data to test environments without PII. No actual data is directly copied, instead a valueDistribution model is
generate from the production data. Test data can then be generated in any other database along the same patterns as the model.

This also allows for projecting into the future. For example, you can test with how your production data would look like with 2x growth. 
It can also simulate user activity by continuously projecting into the future.
