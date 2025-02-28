import { defineConfig } from "cypress";
import * as fs from "fs";

export default defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            on("task", {
                readFileMaybe(filename) {
                    if (fs.existsSync(filename)) {
                        return fs.readFileSync(filename, "utf8");
                    }

                    return null;
                },
            });
        },
        baseUrl: "http://localhost:4200",
        env: {
            mode: "one-report-by-test",
        },
        testIsolation: false,
        experimentalRunAllSpecs: true,
        specPattern: [
            "cypress/e2e/accessibility-test/*.cy.ts",
            "cypress/e2e/report/*.cy.ts",
        ],
        defaultCommandTimeout: 10000,
    },
    viewportWidth: 1920,
    viewportHeight: 1080,
});
