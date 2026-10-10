// Renders the SVG sources in this directory into the PNG and ICO files under src/main/resources.
//
//   corepack enable   (once; provides the Yarn version pinned in package.json)
//   yarn install
//   yarn build
//
// Each icon has three sources: <name>-16.svg and <name>-32.svg use heavier strokes for small sizes,
// <name>.svg is used from 33px upwards. Banner text is set in Quicksand Bold from fonts/
// (SIL Open Font License, see fonts/OFL.txt) and ends up as pixels only.
import fs from 'node:fs';
import path from 'node:path';
import { Resvg, type ResvgRenderOptions } from '@resvg/resvg-js';

interface IcoFrame {
    size: number;
    png: Buffer;
}

const SRC = import.meta.dirname;
const RES = path.join(SRC, '..', 'main', 'resources');
const FONT: ResvgRenderOptions['font'] = {
    fontFiles: [path.join(SRC, 'fonts', 'Quicksand-Bold.ttf')],
    loadSystemFonts: false,
    defaultFontFamily: 'Quicksand',
};

function source(name: string, size: number): string {
    const suffix = size <= 16 ? '-16' : size <= 32 ? '-32' : '';
    return fs.readFileSync(path.join(SRC, `${name}${suffix}.svg`), 'utf8');
}

function render(svg: string, size: number): Buffer {
    return new Resvg(svg, { fitTo: { mode: 'width', value: size }, font: FONT }).render().asPng();
}

// ICO with PNG-compressed frames (supported since Windows Vista)
function ico(frames: IcoFrame[]): Buffer {
    const header = Buffer.alloc(6 + 16 * frames.length);
    header.writeUInt16LE(1, 2);
    header.writeUInt16LE(frames.length, 4);
    let offset = header.length;
    frames.forEach(({ size, png }, i) => {
        const e = 6 + 16 * i;
        header[e] = size >= 256 ? 0 : size;
        header[e + 1] = size >= 256 ? 0 : size;
        header.writeUInt16LE(1, e + 4);
        header.writeUInt16LE(32, e + 6);
        header.writeUInt32LE(png.length, e + 8);
        header.writeUInt32LE(offset, e + 12);
        offset += png.length;
    });
    return Buffer.concat([header, ...frames.map(f => f.png)]);
}

function widthOf(svg: string): number {
    const match = svg.match(/width="(\d+)"/);
    if (!match) {
        throw new Error('SVG without width');
    }
    return Number(match[1]);
}

const ICO_SIZES = [16, 24, 32, 48, 64, 256];
const icon = (name: string, size: number): Buffer => render(source(name, size), size);

for (const size of [16, 32, 48, 128, 256]) {
    fs.writeFileSync(path.join(RES, 'icons', `jauswertung-${size}.png`), icon('jauswertung', size));
}
fs.writeFileSync(path.join(RES, 'icons', 'jAuswertung.ico'), ico(ICO_SIZES.map(size => ({ size, png: icon('jauswertung', size) }))));
fs.writeFileSync(path.join(RES, 'icons', 'jDatei.ico'), ico(ICO_SIZES.map(size => ({ size, png: icon('wettkampf', size) }))));

// Banners at their native size: banner.png for splash screens and the About dialog,
// banner-vertical.png for the left side of the installer wizard
for (const name of ['banner', 'banner-vertical']) {
    const svg = fs.readFileSync(path.join(SRC, `${name}.svg`), 'utf8');
    fs.writeFileSync(path.join(RES, 'images', `${name}.png`), render(svg, widthOf(svg)));
}
console.log(`Wrote icons and banners to ${RES}`);
