## Automatically Configure the Experimental Environment

- Set up a (virtual) machine:
  - Arch Linux (e.g., kernel 6.18.9-arch1-2)
  - Minimum RAM: 2 GB
  - Minimum Disk: 20 GB
  - Non-root user with sudo privileges
- Copy [`configure-arch`](./configure-arch) to the non-root user's home directory
- Make it executable: `chmod u+x configure-arch`
- Run it without sudo: `./configure-arch`

> [!Important]
> Do not run `./configure-arch` on your development machine as it will install packages and re-configures the system.

This script will install requirements, like java, git, and podman using `pacman`.
This requires sudo privileges.
It then downloads defects4j and this repository.
The script builds an app form this repository and installs it as a systemd user service.
When configuring the service, the script asks for an OpenAI API key to be input/pasted.
Ensure that the key has access to the Responses API.

The configuration can be changed using the file at `~/tools/iterative-prompt-testgen/.env`.
See [Environment Variables](#environment-variables) for all available options.

You can then start the service using `systemctl --user start iterative-prompt-testgen`.
Logs can be viewed using `journalctl -t iterative-prompt-testgen` and the output database is located at
`~/tools/iterative-prompt-testgen/data/runs.h2.mv.db`.

To create a backup of the database, run `~/tools/db-backup`. Backups are stored in `~/tools/db-backups`.

## Running Manually

### Requirements

- Java 25
- Compose (Docker or Podman). Example:
  - Install [podman](https://github.com/containers/podman)
  - Install [podman-compose](https://github.com/containers/podman-compose)
- Clone [defects4j](https://github.com/rjust/defects4j)
  - Checkout revision `8022adcd685ae8f591f0cb5d71282e5c93798e4d`

> [!Note]
> This app patches `Dockerfile` and `docker-compose.yaml` of Defects4J to ensure reproducibility.

### Environment Variables

| Name                                | Description                                                                                                                                                          |
|:------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `OPENAI_API_KEY`                    | The API key for OpenAI. Must have access to Responses API.                                                                                                           |
| `MAX_OUTPUT_TOKENS`                 | The maximum number output tokens set as aconstraint for the LLM (includes reasoning).                                                                                |
| `DEFECTS4J_HOME`                    | The directory into which defects4j has been cloned.                                                                                                                  |
| `DEFECTS4J_BUGS`                    | The defects4j project and bug ids. One bug per line in the format `<project-id>,<bug-id>`. See https://github.com/rjust/defects4j for the list of projects and bugs. |
| `COMPOSE_PROGRAM`                   | The compose program executable.                                                                                                                                      |
| `RUNS`                              | How many times to run per bug given in `DEFECTS4J_BUGS`. Every run starts with an initially generated test that's used for both feedback loops (coverage/mutation).  |
| `MAX_LLM_REPROMPTS_FOR_FIXING_TEST` | The maximum number of allowed reprompts for fixing a test (syntax error, compilation error, test failure).                                                           |
| `FEEDBACK_ITERATIONS`               | The number of coverage/mutation feedback loop interations per run.                                                                                                   |

You can use a `.env` file in the working directory for environment variables.\
Example:

```dotenv
OPENAI_API_KEY=sk-********************************

MAX_OUTPUT_TOKENS=30000

DEFECTS4J_HOME=/home/username/workspace/third-party/git/defects4j
DEFECTS4J_BUGS="Lang,1
Lang,3
Lang,4"

COMPOSE_PROGRAM=podman-compose

RUNS=40
MAX_LLM_REPROMPTS_FOR_FIXING_TEST=5
FEEDBACK_ITERATIONS=10
```


### Starting the App

```bash
./gradlew run # Run
# OR
./gradlew build # Create a runnable app in ./build/distributions/
```
