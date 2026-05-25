const fs = require('fs');
const { execSync } = require('child_process');

function checkCommand(cmd) {
    try {
        execSync(cmd, { stdio: 'ignore' });
        return true;
    } catch {
        return false;
    }
}

function checkFile(path, required = false) {
    if (fs.existsSync(path)) {
        console.log(`[OK] ${path}`);
        return true;
    } else {
        if (required) {
            console.error(`[ERROR] Missing: ${path}`);
            process.exit(1);
        } else {
            console.warn(`[WARN] Missing: ${path}`);
        }
    }
}

function verifySetup(basePath) {
    console.log('====================================');
    console.log('Verificando entorno...');
    console.log('====================================\n');

    if (!checkCommand('node --version')) {
        console.error('[ERROR] Node.js no instalado');
        process.exit(1);
    }

    if (!checkCommand('npm --version')) {
        console.error('[ERROR] npm no instalado');
        process.exit(1);
    }

    if (!checkCommand('npx newman --version')) {
        console.error('[ERROR] Newman no disponible');
        process.exit(1);
    }

    checkFile(`${basePath}/collections/query_collection.json`, true);
    checkFile(`${basePath}/collections/payin_collection.json`);
    checkFile(`${basePath}/collections/payout_collection.json`);
    checkFile(`${basePath}/collections/webhook_collection.json`);

    console.log('\n[OK] Setup validado\n');
}

module.exports = verifySetup;