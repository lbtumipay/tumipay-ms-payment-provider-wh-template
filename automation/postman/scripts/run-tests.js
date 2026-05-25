'use strict';

const fs = require('fs');
const os = require('os');
const path = require('path');
const newman = require('newman');

const BASE_PATH = path.resolve(__dirname, '..');

function runCollection(name, collection, environmentFile, opts = {}) {
    return new Promise((resolve, reject) => {
        console.log(`\n▶ Ejecutando: ${name}`);

        try {
            const collectionJson = require(collection);
            const environmentJson = JSON.parse(fs.readFileSync(environmentFile, 'utf8'));

            console.log("✔ Collection cargada");
            console.log("✔ Environment cargado");

            const reporters = opts.ci ? ['cli', 'json']
                            : (opts.html || opts.verbose) ? ['cli', 'htmlextra']
                            : ['cli'];
            const reporterOptions = {};
            if (opts.ci) {
                reporterOptions.json = {
                    export: path.join(BASE_PATH, `reports/ci-report-${name.replace(/\s+/g, '-').toLowerCase()}.json`)
                };
            }
            if (opts.html) {
                reporterOptions.htmlextra = {
                    export: path.join(BASE_PATH, `reports/report-${opts.env}-${name.replace(/\s+/g, '-').toLowerCase()}.html`)
                };
            } else if (opts.verbose) {
                reporterOptions.htmlextra = {
                    export: path.join(BASE_PATH, `reports/verbose-report-${name.replace(/\s+/g, '-').toLowerCase()}.html`)
                };
            }

            newman.run({
                collection: collectionJson,
                environment: environmentJson,
                exportEnvironment: environmentFile,
                reporters,
                reporter: reporterOptions,
                bail: !!opts.ci
            }, (err, summary) => {

                console.log("👉 Callback ejecutado");

                if (err) {
                    console.error(`\n❌ Error técnico en ${name}:`);
                    console.error(err);
                    return reject(err);
                }

                if (!summary) {
                    console.error("❌ Summary undefined");
                    return reject(new Error("Summary undefined"));
                }

                if (summary.run.failures.length > 0) {
                    console.error(`\n❌ Fallos en ${name}:`);

                    summary.run.failures.forEach((failure, index) => {
                        console.error(`\n[${index + 1}] ${failure.error.test || 'N/A'}`);
                        console.error(failure.error.message);
                    });

                    return reject(summary.run.failures);
                }

                console.log(`✅ OK: ${name}`);
                resolve();
            });

        } catch (error) {
            console.error(`\n💥 Error antes de ejecutar Newman (${name}):`);
            console.error(error);
            reject(error);
        }
    });
}

async function main() {
    // Detectar entorno con --env=dev|local (por defecto local)
    const envArg = process.argv.find(a => a.startsWith('--env='));
    const env = envArg ? envArg.split('=')[1] : 'local';
    const isCi      = process.argv.includes('--ci');
    const isVerbose = process.argv.includes('--verbose');
    const isHtml    = process.argv.includes('--html');

    const opts = { env, ci: isCi, verbose: isVerbose, html: isHtml };

    const sourceEnvironmentFile = `${BASE_PATH}/enviroments/${env}_environment.json`;
    const environmentFile = path.join(os.tmpdir(), `newman-env-${env}-${Date.now()}.json`);

    fs.copyFileSync(sourceEnvironmentFile, environmentFile);

    const modeTag = isCi ? ' [CI]' : isVerbose ? ' [VERBOSE]' : isHtml ? ' [HTML]' : '';
    console.log(`\n🌍 Entorno: ${env}${modeTag}`);
    console.log(`📁 Environment source: ${sourceEnvironmentFile}`);
    console.log(`🧪 Environment temporal: ${environmentFile}`);

    const collections = [
        { name: 'PayIn Tests',   file: `${BASE_PATH}/collections/payin_collection.json` },
        { name: 'PayOut Tests',  file: `${BASE_PATH}/collections/payout_collection.json` },
        { name: 'Query Tests',   file: `${BASE_PATH}/collections/query_collection.json` },
        { name: 'Webhook Tests', file: `${BASE_PATH}/collections/webhook_collection.json` },
    ];

    let allPassed = true;

    try {
        for (const col of collections) {
            try {
                await runCollection(col.name, col.file, environmentFile, opts);
            } catch (err) {
                console.error(`\n❌ La colección "${col.name}" falló. Continuando con las siguientes...`);
                allPassed = false;
            }
        }
    } finally {
        if (fs.existsSync(environmentFile)) {
            fs.unlinkSync(environmentFile);
        }
    }

    if (!allPassed) {
        console.error('\n❌ Una o más colecciones fallaron.');
        process.exit(1); // exit code 1 → CI pipeline marks the step as FAILED
    }

    console.log('\n✅ Todas las colecciones ejecutadas correctamente.');
    process.exit(0); // exit code 0 → CI pipeline marks the step as PASSED
}

main();
