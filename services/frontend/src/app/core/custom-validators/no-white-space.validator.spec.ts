import { FormControl } from "@angular/forms";
import { noWhitespaceValidator } from "./no-white-space.validator";

describe("noWhitespaceValidator", () => {
    it("should return null if control value is not whitespace", () => {
        const control = new FormControl("validValue");
        const result = noWhitespaceValidator()(control);
        expect(result).toBeNull();
    });

    it("should return { required: true } if control value is whitespace", () => {
        const control = new FormControl("   ");
        const result = noWhitespaceValidator()(control);
        expect(result).toEqual({ required: true });
    });

    it("should return { required: true } if control value is empty", () => {
        const control = new FormControl("");
        const result = noWhitespaceValidator()(control);
        expect(result).toEqual({ required: true });
    });

    it("should return null if control value is a non-whitespace string", () => {
        const control = new FormControl("nonWhitespace");
        const result = noWhitespaceValidator()(control);
        expect(result).toBeNull();
    });

    it("should return null if control value is a number", () => {
        const control = new FormControl(123);
        const result = noWhitespaceValidator()(control);
        expect(result).toBeNull();
    });

    it("should return { required: true } if control value is null", () => {
        const control = new FormControl(null);
        const result = noWhitespaceValidator()(control);
        expect(result).toEqual({ required: true });
    });
});
