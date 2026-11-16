const normalize = value => value.toLocaleLowerCase('vi').normalize('NFD').replace(/\p{M}/gu, '').replace(/đ/g, 'd');

export const matchesPlace = (name, query) => normalize(name).includes(normalize(query.trim()));
