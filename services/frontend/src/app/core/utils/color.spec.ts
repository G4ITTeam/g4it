import { generateColor } from "./color";

describe("UserService", () => {
    it("should return a valid hex color code for a given string", () => {
        const inputString = "example";

        const result = generateColor(inputString);

        expect(result).toMatch(/^#[0-9A-F]{6}$/i);
    });

    it("should return the same color for the same input string", () => {
        const inputString = "example";

        const result1 = generateColor(inputString);
        const result2 = generateColor(inputString);

        expect(result1).toEqual(result2);
    });
});
