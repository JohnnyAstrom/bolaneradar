export function preloadImage(src?: string) {
    if (!src) return;

    const img = new Image();
    img.src = src;
}