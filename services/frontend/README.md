## Things to know to happily work on this project 🌈

### Getting started

To start the project, follow these steps:

```
npm run init
npm i
npm start
```

The command `npm run init` will create this environment.local.ts based on environment.ts and environment variables (uses AZURE_CLIENT_ID).

Open your browser on `http://localhost:4200/`. The app will automatically reload if you
change any of the source files.

### Backend Configuration

You have two options for working with the backend:

Keep the entire backend running.
Open another terminal and run the command `npm run start:mockserver`. This will use the
mock server defined in the file `mock-server/server`.js to simulate data.

#### Unit tests

```
npm test
```

To run unit tests for the entire application and view the code coverage percentage, use
the command: `ng test --code-coverage`.

Feel free to refer to this README file whenever you need guidance while working on the
Angular inventory frontend.

#### Full Mocked or with backend

To activate mocked mode, launch this command :

```shell
npm run start:mockserver
```

*Note:*

- *This will use the mock server defined in the file `mock-server/server.js` to simulate
  data.*

The best way to develop is to have end to end project locally.

### Debugging with Elf

For debugging purposes, you can use the Elf devTool(). Add the following code snippet
somewhere in your code and install
the [Redux DevTools extension](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd).

```typescript
import { devTools } from "@ngneat/elf-devtools";
devTools();
```

We use .repository.ts files to set up and manipulate our Elf store that are located
in `app/core/store`

To know more how to work with Elf, see
the [Elf official documentation](https://ngneat.github.io/elf/)

### Text localization

If you need to locate the text, you can find it in the file `assets/i18n/en.json`. It is
already populated with some texts for the inventories page. To use it in HTML, you can use
the following syntax: `{{ 'inventories-footprint.round-button.status' | translate }}`.
A `fr.json` file can be added to the `assets/i18n` folder to automatically translate the
text.

### Styling

To modify the styles, you can make changes in the `styles.scss` file. There is a
variables.scss file where colors and other variables are declared, which can be used in
styles.scss using the syntax `var(--primary-color)`.

To ease your life please use the [PrimeNg](https://primeng.org/) components.
The PrimeNg theme can be changed but be cautious if you modify PrimeNG components directly
in the `assets/themes/primeng-theme.scss` file.

For the charts we use the components
from [Echarts](https://echarts.apache.org/en/index.html)
The echarts theme location is `assets/themes/primeng-theme.scss`.

And finally please check the `assets/images` folder before importing already existing
icons and designs.

---

### Software requirements

List of tools to be installed:

- Node.js 20

#### End-to-end tests

Run `npm run e2e` to execute the end-to-end tests via a platform of your choice. To use
this command, you need to first add a package that implements end-to-end testing
capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out
the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.

## Recommended development workflow

See documentation website.

## How does the pipeline work

In this project, there is a CI/CD pipeline that covers code quality, unit testing,
security scans and also deployment to Openshift. We briefly introduce the role of each
stages in pipeline below,

- init: does some initial checks before going further in the pipeline.
- build-application: builds (or promotes) the application artifacts (e.g. .war, .jar,
  .exe...).
- test-application: tests the application (this stage is optional as the tests can also be
  executed during the build-application stage).
- build-image: builds (or promotes) the application image from the Dockerfile present in
  the repo and the artifacts generated previously.
- analysis: runs some static analysis on the application (both security and lint/best
  practices).
- security-check: serves as a security gate based on the results of the static analysis
  done previously.
- deploy: deploys the application on an environment (test or production).
- pre-acceptance: prepares for the acceptance tests (for instance load some test data in a
  database).
- acceptance: executes the acceptance tests that will validate the application.
- performance: executes the performance tests that will validate the application.
- release: runs a set of actions when the application is released (i.e. merged to a
  production branch).
- destroy: defines a manual job that can be run to destroy a deployed environment (see
  deploy stage).
- .post: runs some specific actions at the end of the pipeline

## Security concerns

The DEP updates the original template regularly but you can not benefit automatically from
the updates.

In all cases, the project is responsible for assessing and mitigating security issues on
their repositories.

The OCI images used in [.gitlab-ci.yml](.gitlab-ci.yml) file are built from
the [dep/library/project-oci-images](dep/library/project-oci-images) repository. You can
check periodically for potential updates of these images.

## How to improve this template

This GitLab project template has been generated dynamically
using [mustache templating](https://mustache.github.io/). If you want to help the DEP team
improve this template, please open an issue or a merge request
at [dep/library/dynamic-project-templates](dep/library/dynamic-project-templates).

## Env variable injection for urls

For production, we inject url values as env variable in the nginx container, and replace
them thanks to the [nginx/25-inject-env-var.sh](nginx/25-inject-env-var.sh) script.
This script is placed in docker-entrypoint.d folder, and is executed before container
startup.

/!\ You need to inject at least the `$BASE_HREF` env var:

```html

<base href="$BASE_HREF/">
<!-- Inject /overgreen -->
<base href="/overgreen/">
```

For instance, if you want to start the image locally:

```sh
podman run -d -p 4200:4200 --name overgreen-frontend -e BASE_HREF=http://localhost:4200
```

And for deployment on k8s:

```sh
BASE_HREF=/overgreen
```
