const fs = require('fs');
const path = require('path');

const hasUnorderedImports = (file) => {
  if (!file.endsWith('.js')) {
    return false;
  }

  let hasTextImport = false;
  for (let line of fs.readFileSync(file).toString().split('\n')) {
    if (!line.startsWith('import')) {
      continue;
    }

    if (line.indexOf('!') !== -1) {
      hasTextImport = true;
    } else if (hasTextImport) {
      return true;
    }
  }

  return false;
}

let files = [];

const walk = (dir, result) => {
    fs.readdirSync(dir)
      .map(file => path.join(dir, file))
      .forEach((file) => {
          let stat = fs.statSync(file)
          if (stat && stat.isDirectory()) {
            walk(file, result)
          } else if (hasUnorderedImports(file)) {
            result.push(file);
          }
      })
}

process.argv.slice(2).forEach(path => walk(path, files))

if (files.length) {
  console.log('Files with incorect import order were detected:');
  files.forEach(file => console.log(`\t${file}`))
  process.exit(1)
}
