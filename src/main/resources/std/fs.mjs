"use strict";
import * as java from './java.mjs'
export function readFile(fileName, callback) {
    fileName = fileName.replace('/', java.File.separator)
    java.Thread.ofVirtual().start(function() {
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

export function writeFile(fileName, data, whenError) {
    fileName = fileName.replace('/', java.File.separator)
    java.Thread.ofVirtual().start(function() {
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

export function readFileSync(fileName) {
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

export function writeFileSync(fileName, data) {
    fileName = fileName.replace('/', java.File.separator)
    var src = null
    try {
        src = new java.FileOutputStream(new java.File(fileName));
        src.write(data)
    } finally {
        src.close()
    }
}