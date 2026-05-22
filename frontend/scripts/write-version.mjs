import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const frontendDir = path.resolve(__dirname, '..');
const repoRoot = path.resolve(frontendDir, '..');
const versionConfigPath = path.join(repoRoot, 'app-version.json');
const outputPath = path.join(frontendDir, 'src', 'assets', 'version.json');

function normalizeBaseVersion(value) {
  const baseVersion = String(value || '').trim();
  if (!/^\d+\.\d+\.\d+$/.test(baseVersion)) {
    throw new Error(`baseVersion invalida: "${baseVersion}". Usa formato mayor.menor.parche, por ejemplo 1.0.0`);
  }
  return baseVersion;
}

function resolvePrNumber() {
  const envCandidates = [
    process.env.PR_NUMBER,
    process.env.GITHUB_PR_NUMBER,
    process.env.ghprbPullId
  ];

  for (const candidate of envCandidates) {
    const parsed = Number(candidate);
    if (Number.isInteger(parsed) && parsed > 0) {
      return parsed;
    }
  }

  const refName = process.env.GITHUB_REF_NAME || '';
  if (/^\d+\/merge$/.test(refName)) {
    return Number(refName.split('/')[0]);
  }

  return 0;
}

const config = JSON.parse(await readFile(versionConfigPath, 'utf8'));
const baseVersion = normalizeBaseVersion(config.baseVersion);
const prNumber = resolvePrNumber();
const fullVersion = `${baseVersion}.${prNumber}`;

const payload = {
  baseVersion,
  prNumber,
  fullVersion,
  generatedAt: new Date().toISOString(),
  source: prNumber > 0 ? 'pull_request' : 'local'
};

await mkdir(path.dirname(outputPath), { recursive: true });
await writeFile(outputPath, `${JSON.stringify(payload, null, 2)}\n`, 'utf8');

process.stdout.write(`${fullVersion}\n`);
