#!/bin/bash
# Exit immediately if any command fails
set -e

# Run the test suite and capture the exit code
node scripts/run-tests.js "$@"
EXIT_CODE=$?

# Propagate the exit code explicitly to the CI pipeline:
#   0 → all tests passed  (pipeline step: SUCCESS)
#   1 → one or more tests failed (pipeline step: FAILURE)
exit $EXIT_CODE
