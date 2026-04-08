const fs = require('fs');

const files = [
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\img_avatar.png',
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\user_avatar_premium.png',
    'd:\\Projects\\Expence Tracker\\app\\src\\main\\res\\drawable\\ic_logo_premium.png'
];

files.forEach(file => {
    try {
        const buffer = fs.readFileSync(file, { length: 8 });
        console.log(`${file}: ${buffer.toString('hex')}`);
    } catch (e) {
        console.log(`${file}: Error: ${e.message}`);
    }
});
