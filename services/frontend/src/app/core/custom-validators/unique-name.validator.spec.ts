import { FormControl } from "@angular/forms";
import { uniqueNameValidator } from "./unique-name.validator";

describe("uniqueNameValidator", () => {
    it("should return null if the name is not in the existing names list", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl("David");

        const result = validator(control);

        expect(result).toBeNull();
    });

    it("should return an error object if the name is in the existing names list", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl("Alice");

        const result = validator(control);

        expect(result).toEqual({ uniqueName: true });
    });

    it("should return null if the control value is empty", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl("");

        const result = validator(control);

        expect(result).toBeNull();
    });

    it("should return null if the control value is null", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl(null);

        const result = validator(control);

        expect(result).toBeNull();
    });

    it("should return an error object if the name with spaces is in the existing names list", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl("A l i c e");

        const result = validator(control);

        expect(result).toEqual({ uniqueName: true });
    });

    it("should return null if the name with spaces is not in the existing names list", () => {
        const existingNames = ["Alice", "Bob", "Charlie"];
        const validator = uniqueNameValidator(existingNames);
        const control = new FormControl("D a v i d");

        const result = validator(control);

        expect(result).toBeNull();
    });
});
