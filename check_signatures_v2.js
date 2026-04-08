const fs = require('fs');
const files = [
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\img_avatar.png',
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\user_avatar_premium.png',
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\ic_logo_premium.png'
];
files.forEach(file => {
    try {
        const fd = fs.openSync(file, 'r');
        const buffer = Buffer.alloc(16);
        fs.readSync(fd, buffer, 0, 16, 0);
        console.log(`File: ${file.split('\\').pop()}`);
        console.log(`Hex: ${buffer.toString('hex')}`);
        fs.closeSync(fd);
    } catch (e) {
        console.log(`Error reading ${file}: ${e.message}`);
    }
});
