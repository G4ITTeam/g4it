import { init, report } from "../utilsCypress";

describe("Report", () => {
    before(() => {
        init("report", Cypress.env("mode"));
    });
    after(() => {
        report();
    });
    it("Generate report", () => {});
});
