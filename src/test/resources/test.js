import * as fs from "fs";
import * as java from "java";
fs.writeFileSync('data/file', java.JString("Test").getBytes());
fs.readFile('data/file', function(err, data) {
    if(!err) {
        console.log(data)
    }
});