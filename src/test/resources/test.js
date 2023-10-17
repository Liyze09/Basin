var fs = this.require("fs");
var java = this.require("java");
fs.writeFileSync('data/file', [0]);
fs.readFile('data/file', function(err, data) {
     Java.type("java.lang.System").out.println(data)
});