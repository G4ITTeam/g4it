/**
 * Extract the filename (last part) of path
 * @param path the path with \\ or /
 * @returns the filename
 */
export function extractFileName(path: string) {
    let result = path;
    ["\\", "/"].forEach((pathDelimiter: string) => {
        if (path.includes(pathDelimiter)) {
            result = path.substring(path.lastIndexOf(pathDelimiter) + 1);
        }
    });

    return result;
}
