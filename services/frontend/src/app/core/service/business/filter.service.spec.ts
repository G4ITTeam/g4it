import { Constants } from "src/constants";
import { FilterService } from "./filter.service";

describe("DigitalServiceBusinessService", () => {
    let filterService: FilterService;
    beforeEach(() => {
        filterService = new FilterService();
    });

    it("should be create", () => {
        expect(filterService).toBeTruthy();
    });

    it("getUpdateSelectedValues() should return empty array", () => {
        expect(filterService.getUpdateSelectedValues([], [], "selection")).toEqual([]);
    });

    it("getUpdateSelectedValues() should return all items", () => {
        expect(
            filterService.getUpdateSelectedValues(
                [Constants.ALL, "a", "b"],
                [Constants.ALL, "a", "b", "c"],
                Constants.ALL,
            ),
        ).toEqual([Constants.ALL, "a", "b", "c"]);
    });

    it("getUpdateSelectedValues() should return selected item without All", () => {
        expect(
            filterService.getUpdateSelectedValues(
                [Constants.ALL, "b"],
                [Constants.ALL, "a", "b", "c"],
                "a",
            ),
        ).toEqual(["b"]);
    });

    it("getUpdateSelectedValues() should return selected item without All", () => {
        expect(
            filterService.getUpdateSelectedValues(
                ["a", "b", "c"],
                [Constants.ALL, "a", "b", "c"],
                "c",
            ),
        ).toEqual([Constants.ALL, "a", "b", "c"]);
    });
});
