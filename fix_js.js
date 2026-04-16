const fs = require('fs');
let c = fs.readFileSync('View/app.js', 'utf8');
const idx = c.indexOf('handlePlanSatinAl');
console.log('Found at:', idx);
// Show 400 chars from that position
const seg = c.substring(idx, idx+400);
console.log(JSON.stringify(seg));
