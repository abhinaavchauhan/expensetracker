const fs = require('fs');
const path = require('path');
const dir = 'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable';
const files = fs.readdirSync(dir).filter(f => f.endsWith('.png'));
files.forEach(f => {
    const file = path.join(dir, f);
    try {
        const fd = fs.openSync(file, 'r');
        const buffer = Buffer.alloc(4);
        fs.readSync(fd, buffer, 0, 4, 0);
        const hex = buffer.toString('hex');
        if (hex !== '89504e47') {
            console.log(`Mismatch: ${f} is NOT a PNG! Hex: ${hex}`);
        } else {
            console.log(`OK: ${f} is a PNG.`);
        }
        fs.closeSync(fd);
    } catch (e) {
        console.log(`Error reading ${f}: ${e.message}`);
    }
});
