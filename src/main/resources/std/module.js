const __map = new Map();

this.require = name => {
    return __map.get(name);
};

this.export = (name, value) => {
    __map.set(name, value);
};