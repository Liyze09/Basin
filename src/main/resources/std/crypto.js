var java = this.require("java")

function createHash(algorithm) {
    return java.MessageDigest.getInstance(algorithm);
}