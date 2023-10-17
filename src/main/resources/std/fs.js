"use strict";
var java = this.require("java")
function readFile(fileName, callback) {
    fileName = fileName.replace('/', java.File.separator)
    java.Basin.run(function() {
        var src = null
        try {
            src = new java.FileInputStream(new java.File(fileName));
            while(true) {
                var bytes = src.readNBytes(16384)
                callback(null, bytes)
                if (bytes.length < 16384) {
                    break
                }
            }
        } catch (e) {
            callback(e, null);
        } finally {
            src.close();
        }
    });
}
function writeFile(fileName, data, whenError) {
    fileName = fileName.replace('/', java.File.separator)
    java.Basin.run(function() {
        var src = null
        try {
            var file = new java.File(fileName)
            if (!file.exists()) {
                file.mkdirs()
                file.createNewFile()
            }
            src = new java.FileOutputStream(file);
            src.write(data)
        } catch (e) {
            whenError(e, null);
        } finally {
            src.close()
        }
    })
}

function readFileSync(fileName) {
    fileName = fileName.replace('/', java.File.separator)
    var src = null
    var bytes = null
    try {
        src = new java.FileInputStream(new java.File(fileName));
        bytes = src.readAllBytes()
    } finally {
        src.close()
    }
    return bytes
}

function writeFileSync(fileName, data) {
    fileName = fileName.replace('/', java.File.separator)
    var src = null
    try {
        src = new java.FileOutputStream(new java.File(fileName));
        src.write(data)
    } finally {
        src.close()
    }
}

this.export("fs", {
    writeFile: writeFile,
    writeFileSync: writeFileSync,
    readFile: readFile,
    readFileSync: readFileSync
})