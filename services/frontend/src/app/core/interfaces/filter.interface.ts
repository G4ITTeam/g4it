export interface Filter<T = string | TransformedDomain> {
    [key: string]: T[];
}

export interface FilterRest {
    field: string;
    filters: FilterTreeNodeRest[];
}

export interface FilterTreeNodeRest {
    filter: string;
    filters: FilterTreeNodeRest[];
}

export interface ConstantApplicationFilter extends ApplicationFilterItem {
    children?: ApplicationFilterItem[];
}
export interface ApplicationFilterItem {
    field: string;
    translated: boolean;
}

export interface TransformedDomain extends TransformedDomainItem {
    children: TransformedDomainItem[];
}

export interface TransformedDomainItem {
    field: string;
    label: string;
    key: string;
    checked: boolean;
    visible: boolean;
    collapsed?: boolean;
}
