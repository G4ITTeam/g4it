export const groupByCriterion = (arr: any[]) => {
    return arr.reduce((acc: any, obj: any) => {
        const key = transformCriterion(obj.criterion);
        if (!acc[key]) {
            acc[key] = [];
        }
        acc[key].push(obj);
        return acc;
    }, {});
};

export const transformCriterion = (criterion: string) => {
    return criterion.toLocaleLowerCase().replaceAll("_", "-");
};

export const groupByField = (arr: any[], field: string) => {
    return arr.reduce((acc: any, obj: any) => {
        const key = obj[field];
        if (!acc[key]) {
            acc[key] = [];
        }
        acc[key].push(obj);
        return acc;
    }, {});
};

export const sumByProperty = (arr: any[], property: string): number =>
    arr.reduce((n: number, obj: any) => n + obj[property], 0);
