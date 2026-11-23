// Renders the listing artwork in marketing/images/ from real in-game screenshots (marketing/images/screenshots/), renders
// of the mod's own textures (marketing/source/renders/, made by renders.py), the mod logo and the bundled OFL fonts.
// Usage (from the repository root):
//   NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available. Set CHROMIUM to a Chromium executable to use
// that one instead of Playwright's own download.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility tables of curseforge/description.md and modrinth/description.md).
const RELEASE = { version: '1.0' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const renders = path.join(__dirname, 'renders');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// The five biomes, their colours (from the biome's own grass, water and sky colours) and the README's highlights.
const BIOMES = [
  ['blossom-valley', 'Blossom Valley', '#E27BA6'],
  ['lavender-fields', 'Lavender Fields', '#9B7FD4'],
  ['strawberry-meadow', 'Strawberry Meadow', '#E5606F'],
  ['moonlight-forest-night', 'Moonlight Forest', '#5C6BC8'],
  ['fairy-glade', 'Fairy Glade', '#4FB28F'],
];

const css = `
@font-face { font-family: FgDisplay; src: url('${url(path.join(fonts, 'fredoka.ttf'))}'); }
@font-face { font-family: FgText; src: url('${url(path.join(fonts, 'nunito.ttf'))}'); font-weight: 400; }
@font-face { font-family: FgText; src: url('${url(path.join(fonts, 'nunito-extrabold.ttf'))}'); font-weight: 800; }
:root { --text:#4b3f5c; --dim:#8d82a0; --plum:#55406e; --pink:#E27BA6; --pink-deep:#c9567f; --lav:#9B7FD4; --lav-deep:#7a5fbf;
        --mint:#4FB28F; --sky:#7FA8E8; --moon:#3E4A8C; --cream:#fff8fc; --line:rgba(155,127,212,.3); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--cream); color: var(--text); font-family: FgText, sans-serif; }
.stage { position: relative; overflow: hidden; background: linear-gradient(160deg, #fff8fc 0%, #f6eefc 55%, #e9f6f1 100%); }
.display { font-family: FgDisplay, sans-serif; }
.fairy { background: linear-gradient(100deg, #d9578a 0%, #e27ba6 32%, #9b7fd4 68%, #5fa9d9 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.soft { filter: drop-shadow(0 3px 0 rgba(255,255,255,.95)) drop-shadow(0 10px 24px rgba(122,95,191,.25)); }
.kicker { font-family: FgDisplay, sans-serif; color: var(--lav-deep); letter-spacing: .14em; font-size: 24px; text-transform: uppercase; }
.spark { display: inline-block; vertical-align: -4px; margin-right: 12px; }
.sparkles { background-color: rgba(255,255,255,.85); background-image:
  radial-gradient(circle at 25% 50%, rgba(226,123,166,.75) 0 2.5px, transparent 3px),
  radial-gradient(circle at 75% 50%, rgba(155,127,212,.75) 0 2.5px, transparent 3px);
  background-size: 22px 14px; }
.rule { height: 6px; border-radius: 3px; background: repeating-linear-gradient(90deg, var(--pink) 0 10px, transparent 10px 16px, var(--lav) 16px 26px, transparent 26px 32px, var(--mint) 32px 42px, transparent 42px 48px); opacity: .8; }
.frame { position: relative; border: 7px solid #fff; border-radius: 22px; overflow: hidden; background: #fff;
         box-shadow: 0 0 0 2px rgba(155,127,212,.55), 0 26px 60px rgba(85,64,110,.25), 0 6px 14px rgba(85,64,110,.12); }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.wash { position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 45%, rgba(255,255,255,0) 30%, rgba(255,248,252,.6) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(18px) brightness(1.12) saturate(1.05); }
.pill { display: inline-block; border-radius: 999px; padding: 9px 22px 7px; margin-right: 12px; font-family: FgDisplay, sans-serif; font-size: 22px;
        color: #fff; background: linear-gradient(100deg, #e27ba6, #9b7fd4); box-shadow: 0 6px 16px rgba(122,95,191,.3); }
.pill.ghost { color: var(--lav-deep); background: rgba(255,255,255,.9); box-shadow: 0 0 0 2px rgba(155,127,212,.6), 0 6px 16px rgba(85,64,110,.12); }
.label { position: absolute; font-family: FgDisplay, sans-serif; font-size: 23px; color: var(--plum); background: rgba(255,255,255,.95);
         border-radius: 999px; padding: 8px 20px 6px; white-space: nowrap; box-shadow: 0 0 0 2px rgba(155,127,212,.7), 0 10px 26px rgba(85,64,110,.25); z-index: 2; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 38px; margin: 0 0 16px; font-size: 27px; line-height: 1.3; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 6px; width: 24px; height: 24px; background: var(--pink);
  -webkit-mask: var(--star) center / contain no-repeat; mask: var(--star) center / contain no-repeat; }
ul.points li:nth-child(3n+2)::before { background: var(--lav); }
ul.points li:nth-child(3n)::before { background: var(--mint); }
:root { --star: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'><path d='M12 1 C13 8 16 11 23 12 C16 13 13 16 12 23 C11 16 8 13 1 12 C8 11 11 8 12 1 Z'/></svg>"); }
b.hi { color: var(--plum); font-weight: 800; }
.stats { display: flex; gap: 14px; margin-top: 22px; }
.stats > div { flex: 1; padding: 14px 20px 12px; border-radius: 18px; background: rgba(255,255,255,.93);
               box-shadow: 0 0 0 2px rgba(155,127,212,.45), 0 14px 34px rgba(85,64,110,.16); text-align: center; }
.stats .t { font-family: FgText, sans-serif; font-weight: 800; font-size: 16px; letter-spacing: .1em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: FgDisplay, sans-serif; font-size: 34px; color: var(--plum); margin-top: 2px; white-space: nowrap; }
.tile { position: relative; border-radius: 18px; background: rgba(255,255,255,.93); text-align: center;
        box-shadow: 0 0 0 2px rgba(155,127,212,.45), 0 14px 30px rgba(85,64,110,.16); }
.tile.night { background: linear-gradient(180deg, #2c3570 0%, #3e4a8c 100%); box-shadow: 0 0 0 2px rgba(127,168,232,.6), 0 14px 30px rgba(40,40,90,.3); }
.tile.night .n { color: #eef0ff; } .tile.night .s { color: #b9c3ef; }
.tile .art { display: flex; align-items: center; justify-content: center; }
.tile .art img { image-rendering: pixelated; max-width: 100%; max-height: 100%; }
.tile .n { font-family: FgDisplay, sans-serif; color: var(--plum); line-height: 1.1; }
.tile .s { font-family: FgText, sans-serif; font-weight: 800; color: var(--dim); font-size: 15px; letter-spacing: .04em; }
.arrow { font-family: FgDisplay, sans-serif; color: var(--lav); font-size: 34px; align-self: center; }
pre.code { margin: 0; font-family: 'DejaVu Sans Mono', monospace; font-size: 22px; line-height: 1.5; color: var(--text); }
`;

// A four-pointed sparkle, the fairy counterpart of the sibling kits' hearts, stars and daisies.
const sparkSvg = (fill, size) => `<svg class="spark" width="${size}" height="${size}" viewBox="0 0 24 24" style="display:inline-block">
  <path d="M12 1 C13 8 16 11 23 12 C16 13 13 16 12 23 C11 16 8 13 1 12 C8 11 11 8 12 1 Z" fill="${fill}" stroke="#fff" stroke-width="1.3"/></svg>`;

// Drifting petals and fairy sparkles. Deterministic so renders are reproducible.
const motes = (count = 40, seed = 7, night = false) => `<canvas class="motes"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  const spark = (x, y, z, a, tint) => { g.save(); g.translate(x, y); g.fillStyle = 'rgba(' + tint + ',' + a + ')'; g.beginPath();
    const k = z * 3.2; g.moveTo(0, -k); g.quadraticCurveTo(k * .12, -k * .12, k, 0); g.quadraticCurveTo(k * .12, k * .12, 0, k);
    g.quadraticCurveTo(-k * .12, k * .12, -k, 0); g.quadraticCurveTo(-k * .12, -k * .12, 0, -k); g.fill(); g.restore(); };
  const petal = (x, y, z, a, rot, tint) => { g.save(); g.translate(x, y); g.rotate(rot);
    const h = g.createLinearGradient(-z * 4, 0, z * 4, 0); h.addColorStop(0, 'rgba(' + tint + ',' + a + ')'); h.addColorStop(1, 'rgba(255,246,250,' + a + ')');
    g.fillStyle = h; g.beginPath(); g.ellipse(0, 0, z * 4.2, z * 2.4, 0, 0, Math.PI * 2); g.fill(); g.restore(); };
  const tints = ${night ? "['200,215,255', '235,225,255', '190,240,225']" : "['226,123,166', '155,127,212', '120,200,170']"};
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .4 + rnd() * .45, z = 1.4 + rnd() * 2.2;
    const t = tints[Math.floor(rnd() * tints.length)];
    if (${night ? 'true' : 'rnd() < .45'}) spark(x, y, z, a, t); else petal(x, y, z, a, rnd() * Math.PI, t); } })();
</script>`;

// Renders of the mod's own textures (renders.py), drawn pixel-sharp. Waits for every image to load.
const loadScript = `<script>
(() => { const imgs = [...document.images]; let left = imgs.length;
  const done = () => { if (--left <= 0) document.body.dataset.items = 'ready'; };
  if (!left) document.body.dataset.items = 'ready';
  for (const i of imgs) { if (i.complete) done(); else { i.onload = done; i.onerror = done; } } })();
</script>`;
const model = (name, h) => `<img src="${url(path.join(renders, `${name}.png`))}" style="height:${h}px;image-rendering:pixelated">`;
// A tile with a render and a caption.
const tile = (name, title, { h = 110, art = 130, size = 20, sub = '', night = false } = {}) => `<div class="tile${night ? ' night' : ''}" style="padding:12px 8px 12px">
    <div class="art" style="height:${art}px">${model(name, h)}</div>
    <div class="n" style="font-size:${size}px;margin-top:6px">${title}</div>${sub ? `<div class="s">${sub}</div>` : ''}</div>`;
const grid = (cols, items, gap = 14) => `<div style="display:grid;grid-template-columns:repeat(${cols},1fr);gap:${gap}px">${items.join('')}</div>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div>${loadScript}</body></html>`;

// A section heading: a ribbon with the title, readable on CurseForge's and Modrinth's light and dark themes.
const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
    ${sparkSvg('#9B7FD4', 36)}
    <div style="position:relative;padding:14px 54px 12px;border-radius:999px;background:linear-gradient(100deg,#e27ba6 0%,#c98bd0 50%,#9b7fd4 100%);
                box-shadow:0 0 0 4px #fff,0 0 0 6px rgba(79,178,143,.7),0 12px 28px rgba(85,64,110,.28)">
      <div class="display" style="font-size:46px;line-height:1;color:#fff;white-space:nowrap;text-shadow:0 3px 0 rgba(110,60,130,.4)">${title}</div></div>
    ${sparkSvg('#4FB28F', 36)}
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980, backdrop = 'backdrop.jpg' }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source(backdrop)}');opacity:.45"></div>
  <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,248,252,.95) 0%,rgba(255,248,252,.88) 34%,rgba(246,240,252,.6) 62%,rgba(236,246,242,.42) 100%)"></div>
  <div class="wash"></div>${motes()}
  <div class="sparkles" style="position:absolute;left:0;right:0;top:0;height:14px"></div>
  <div class="sparkles" style="position:absolute;left:0;right:0;bottom:0;height:14px"></div>
  <div style="position:absolute;left:76px;top:0;bottom:0;width:${width - artWidth - 176}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 22 ? ' style="font-size:20px;letter-spacing:.1em"' : ''}>${sparkSvg('#E27BA6', 26)}${kicker}</div>
    <div class="display fairy soft" style="font-size:66px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:56px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in source pixels of a 1600x900 screenshot) scaled to W pixels wide.
const region = (file, x, y, w, h, W, full = 1600) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * full / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const whole = (file, W) => region(file, 0, 0, 1600, 900, W);
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style) => `<div class="label" style="${style}">${text}</div>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="v">${v}</div><div class="t">${t}</div></div>`).join('')}</div>`;
const code = (file, body) => `<div class="frame" style="background:rgba(255,255,255,.96);padding:24px 34px">
  <div style="font-family:FgText;font-weight:800;font-size:18px;letter-spacing:.08em;color:var(--dim);margin-bottom:10px">${file}</div><pre class="code">${body}</pre></div>`;
const k = (t) => `<span style="color:#c9567f">${t}</span>`;
const v = (t) => `<span style="color:#4FB28F">${t}</span>`;
const cm = (t) => `<span style="color:#ab9fbf">${t}</span>`;
// A framed screenshot with a caption label in its bottom-left corner.
const captioned = (file, text, W, style = '') => `<div style="position:relative;${style}">${framed(whole(file, W - 14))}${label(text, 'left:18px;bottom:16px;font-size:20px;padding:6px 16px 4px')}</div>`;

const versionText = VERSIONS.map((ver) => ver.minecraft).join(' · ');

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('blossom-valley.jpg')}" style="position:absolute;right:-40px;top:-20px;width:1180px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,248,252,.98) 0%,rgba(255,248,252,.96) 42%,rgba(255,248,252,.5) 60%,rgba(255,248,252,0) 76%)"></div>
    ${motes(46, 11)}
    <div class="sparkles" style="position:absolute;left:0;right:0;top:0;height:16px"></div>
    <div class="sparkles" style="position:absolute;left:0;right:0;bottom:0;height:16px"></div>
    <div style="position:absolute;left:86px;top:56px;width:1000px">
      <div class="kicker">${sparkSvg('#E27BA6', 26)}Version ${RELEASE.version} · Sugarplum Studios</div>
      <div class="display fairy soft" style="font-size:112px;line-height:1;white-space:nowrap;margin:16px 0 4px">Fairy Garden</div>
      <div class="rule" style="width:620px;margin:18px 0 24px"></div>
      <div style="font-family:FgDisplay;font-size:42px;color:var(--plum);line-height:1.2">A softer, more magical world.</div>
      <div style="font-size:27px;color:var(--text);line-height:1.35;margin-top:8px">Five pastel fantasy biomes, glowing moonlit plants,<br>hidden structures and tiny magical creatures.</div>
      <div style="margin-top:28px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}<span class="pill ghost">Requires TerraBlender</span></div>
      <div style="margin-top:16px;font-family:FgDisplay;font-size:26px;color:var(--dim)">Minecraft ${versionText}</div>
    </div>
`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 40%,#fff8fc 0%,#f3e9fb 55%,#e3f3ec 100%)"></div>${motes(22, 5)}
    <div style="position:absolute;left:46px;top:46px;width:420px;height:420px;border-radius:50%;
                background:radial-gradient(circle at 50% 35%,#4a58a6 0%,#2f3878 70%,#262d63 100%);
                box-shadow:0 0 0 8px #fff,0 0 0 13px #c9a7ef,0 24px 50px rgba(85,64,110,.35)"></div>
    <div style="position:absolute;left:46px;top:46px;width:420px;height:420px;border-radius:50%;overflow:hidden">${motes(26, 19, true)}</div>
    <div style="position:absolute;left:46px;top:46px;width:420px;height:420px;display:flex;align-items:center;justify-content:center">
      <div style="filter:drop-shadow(0 0 3px rgba(20,24,70,.9)) drop-shadow(0 0 26px rgba(170,200,255,.55))">${model('moonflower@open', 280)}</div></div>
    ${[[256, 30], [482, 256], [256, 482], [30, 256]].map(([x, y], i) => `<div style="position:absolute;left:${x - 22}px;top:${y - 22}px">${sparkSvg(['#E27BA6', '#9B7FD4', '#4FB28F', '#7FA8E8'][i], 44)}</div>`).join('')}`),

  'card-biomes': () => card({
    kicker: 'Five pastel biomes', title: 'Explore a<br>fairy world',
    points: ['Blossom Valley and Lavender Fields', 'Strawberry Meadow and Moonlight Forest', 'The Fairy Glade, with giant flowers and crystals',
      'Each with its own grass, water, fog and sky', 'Fitted into vanilla terrain by TerraBlender'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      ${captioned('blossom-valley.jpg', 'Blossom Valley', 482)}${captioned('lavender-fields.jpg', 'Lavender Fields', 482)}
      ${captioned('strawberry-meadow.jpg', 'Strawberry Meadow', 482)}${captioned('fairy-glade.jpg', 'Fairy Glade', 482)}</div>` +
      stats([['Biomes', '5'], ['Structures', '4'], ['Blocks', '90'], ['Creatures', '2']]) }),

  'card-night': () => card({
    kicker: 'Day and night', title: 'When the<br>moon rises',
    points: ['Moonflowers open one by one after dusk', 'Moonwood bark, moonbells and glowcaps glow', 'An open moonflower gives a Moon Petal',
      'Rare Moon Wisps drift between the trees', 'Butterflies come out by day'],
    art: framed(whole('moonlight-forest-night.jpg', 966)) + `<div style="margin-top:18px">${grid(6, [
      ['moonflower@closed', 'Moonflower', 'By day'], ['moonflower@open', 'Moonflower', 'At night'], ['moonbell@block', 'Moonbell', 'Glows'],
      ['glowcap', 'Glowcap', 'Glows'], ['moon_lantern', 'Moon Lantern', 'Light 14'], ['moon_petal', 'Moon Petal', 'From open flowers']]
      .map(([n, t, s]) => tile(n, t, { h: 70, art: 80, size: 17, sub: s, night: true })), 10)}</div>` }),

  'card-plants': () => card({
    kicker: 'Magical plants', title: 'Grown by<br>moonlight',
    points: ['Starbloom grows only at night, under open sky', 'Twice as fast under a full moon', 'Ripe starbloom gives Fairy Dust',
      'Fairy Dust ripens starbloom and opens moonflowers', 'Strawberry bushes: pick, and they regrow'],
    art: `<div class="tile" style="padding:18px 22px 16px;text-align:left">
        <div class="display" style="font-size:28px;color:var(--lav-deep);margin-bottom:8px">Starbloom</div>
        <div style="display:flex;align-items:flex-end;justify-content:space-between">
          ${[['starbloom@0', 'Sprout'], ['starbloom@1', 'Bud'], ['starbloom@2', 'Closed star'], ['starbloom@3', 'Starbloom']].map(([n, t], i) =>
            `${i ? '<div class="arrow">›</div>' : ''}<div style="text-align:center">${model(n, 128)}<div class="s">${t}</div></div>`).join('')}
          <div class="arrow">=</div><div style="text-align:center">${model('fairy_dust', 128)}<div class="s">Fairy Dust</div></div></div></div>
      <div class="tile" style="margin-top:16px;padding:18px 22px 16px;text-align:left">
        <div class="display" style="font-size:28px;color:#E5606F;margin-bottom:8px">Strawberry Bush</div>
        <div style="display:flex;align-items:flex-end;justify-content:space-between">
          ${[['strawberry_bush@0', 'Sprout'], ['strawberry_bush@1', 'Bush'], ['strawberry_bush@2', 'Berries'], ['strawberry_bush@3', 'Ripe']].map(([n, t], i) =>
            `${i ? '<div class="arrow">›</div>' : ''}<div style="text-align:center">${model(n, 128)}<div class="s">${t}</div></div>`).join('')}
          <div class="arrow">=</div><div style="text-align:center">${model('strawberry', 128)}<div class="s">Strawberries</div></div></div></div>` }),

  'card-flowers': () => card({
    kicker: 'A flower collection', title: 'Ten fairy<br>flowers',
    points: ['All ten make dye, and the small ones go in pots', 'Hydrangea bushes in pink, blue and lavender', 'Flower carpets, giant petals and flower crowns',
      'Giant petals soften falls to a fifth', 'Pick all ten for the Flower Collector advancement'],
    art: grid(5, [['rosy_daisy', 'Rosy Daisy'], ['lavender', 'Lavender'], ['cream_buttercup', 'Cream Buttercup'], ['mint_posy', 'Mint Posy'],
      ['starflower', 'Starflower'], ['bluebell', 'Bluebell'], ['moonflower@open', 'Moonflower'], ['crystal_flower', 'Crystal Flower'],
      ['tall_lavender', 'Tall Lavender'], ['fairy_foxglove', 'Fairy Foxglove']].map(([n, t]) => tile(n, t, { h: 100, art: 116, size: 19 })), 12) +
      `<div style="margin-top:16px">${grid(5, [['pink_giant_petals', 'Pink'], ['lavender_giant_petals', 'Lavender'], ['blue_giant_petals', 'Blue'],
        ['cream_giant_petals', 'Cream'], ['flower_crown', 'Flower Crown']].map(([n, t]) => tile(n, t, { h: 96, art: 110, size: 19, sub: n === 'flower_crown' ? 'A helmet' : 'Giant Petals' })), 12)}</div>` }),

  'card-woods': () => card({
    kicker: 'Two full wood sets', title: 'Blossom and<br>Moonwood',
    points: ['Pink Blossom and periwinkle Moonwood', 'Moonwood bark has glowing veins', 'Doors, signs, hanging signs, boats and chest boats',
      'Logs strip with an axe, and the wood burns', 'In the vanilla wood tags, so recipes just work'],
    art: ['blossom', 'moonwood'].map((wood) => `<div style="margin-bottom:16px">${grid(6, [
      [`${wood}_log`, 'Log'], [`${wood}_planks`, 'Planks'], [`${wood}_leaves`, 'Leaves'], [`${wood}_door`, 'Door'],
      [`${wood}_hanging_sign`, 'Hanging Sign'], [`${wood}_chest_boat`, 'Chest Boat']]
      .map(([n, t]) => tile(n, t, { h: 104, art: 120, size: 18, sub: wood === 'blossom' ? 'Blossom' : 'Moonwood', night: wood === 'moonwood' })), 12)}</div>`).join('') +
      grid(3, [['snow_blossom_leaves', 'Snow Blossom Leaves', 'White petals fall'], ['stripped_moonwood_log', 'Stripped Moonwood', 'Axe it'],
        ['blossom_petals', 'Blossom Petals', '1 to 4 per block']].map(([n, t, s]) => tile(n, t, { h: 96, art: 110, size: 18, sub: s })), 12) }),

  'card-structures': () => card({
    kicker: 'Exploration', title: 'Hidden<br>treasure',
    points: ['Flower Shrines in the meadows', 'A Hidden Fairy Garden behind its hedges', 'A Moonstone Circle with a buried chest',
      'A Mushroom Cottage with a pantry', 'Fairy caches buried under crystal flowers'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      ${captioned('flower-shrine.jpg', 'Flower Shrine', 482)}${captioned('hidden-fairy-garden.jpg', 'Hidden Fairy Garden', 482)}
      ${captioned('moonstone-circle.jpg', 'Moonstone Circle', 482)}${captioned('mushroom-cottage.jpg', 'Mushroom Cottage', 482)}</div>` +
      stats([['Structures', '4'], ['Treasure tables', '5'], ['Advancements', '13']]) }),

  'card-creatures': () => card({
    kicker: 'Creatures and ambience', title: 'Alive with<br>magic',
    points: ['Butterflies in six colourways, by biome', 'Moon Wisps glow and leave fairy dust', 'Falling petals, pollen, sparkles and motes',
      'Birdsong, bees, night sounds and fairy chimes', 'Their own music for the fairy biomes'],
    art: framed(whole('fairy-glade-night.jpg', 966)) + `<div style="margin-top:18px">${grid(6, [
      ['butterfly@pink', 'Pink'], ['butterfly@lavender', 'Lavender'], ['butterfly@cream', 'Cream'], ['butterfly@mint', 'Mint'],
      ['butterfly@sky', 'Sky'], ['butterfly@white', 'White']].map(([n, t]) => tile(n, t, { h: 66, art: 76, size: 17, sub: 'Butterfly' })), 10)}</div>` }),

  'card-crafting': () => card({
    kicker: 'Fairy crafts', title: 'Made with<br>fairy dust',
    points: ['Fairy and Moon Lanterns, light 15 and 14', 'Lavender Tea: regeneration, cures poison', 'Strawberry Shortcake gives absorption',
      'Glimmer Lilies, crystal blocks and lavender bales', 'Flower crowns from any fairy flowers'],
    art: grid(4, [['fairy_lantern', 'Fairy Lantern', 'Gold nuggets + fairy dust'], ['moon_lantern', 'Moon Lantern', 'Iron nuggets + moon petal'],
      ['lavender_tea', 'Lavender Tea', 'Bottle + lavender + sugar'], ['strawberry_shortcake', 'Shortcake', 'Strawberries, wheat, sugar, egg'],
      ['glimmer_lily', 'Glimmer Lily', 'Lily pad + fairy dust'], ['fairy_crystal_block', 'Crystal Block', '4 crystal shards'],
      ['lavender_bale', 'Lavender Bale', '9 lavender'], ['fairy_dust', 'Fairy Dust ×2', 'Shard + glowstone + sugar']]
      .map(([n, t, s]) => tile(n, t, { h: 132, art: 150, size: 22, sub: s })), 16) }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Set the<br>magic',
    points: ['Make fairy biomes rarer or more common', 'Turn any of the five biomes off', 'Structure spacing by data pack',
      'Ambient particles and density per player', 'Changes apply to new chunks'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">
      ${code('config/fairygarden-common.toml', `${k('[worldgen]')}
  ${cm('# Vanilla\'s own region weight is 10')}
  ${k('regionWeight')} = ${v('8')}   ${cm('# 0 turns the biomes off')}
  ${k('blossomValley')} = ${v('true')}
  ${k('lavenderFields')} = ${v('true')}
  ${k('strawberryMeadow')} = ${v('true')}
  ${k('moonlightForest')} = ${v('true')}
  ${k('fairyGlade')} = ${v('true')}`)}
      ${code('config/fairygarden-client.toml', `${k('[ambience]')}
  ${k('ambientParticles')} = ${v('true')}
  ${k('ambientParticleDensity')} = ${v('1.0')}   ${cm('# 0 to 3')}`)}</div>` }),
};

const headers = { 'header-biomes': 'The Biomes', 'header-night': 'Day and Night', 'header-plants': 'Magical Plants',
  'header-flowers': 'Flowers', 'header-woods': 'Two Wood Sets', 'header-exploration': 'Exploration',
  'header-creatures': 'Creatures & Ambience', 'header-crafting': 'Crafting', 'header-config': 'Configuration',
  'header-everything': 'Everything Else', 'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions',
  'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:14px">
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
  ${sparkSvg('#9B7FD4', 22)}${sparkSvg('#E27BA6', 30)}${sparkSvg('#4FB28F', 22)}
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch(process.env.CHROMIUM
    ? { executablePath: process.env.CHROMIUM, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] }
    : {});
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.waitForFunction(() => document.body.dataset.items === 'ready');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
