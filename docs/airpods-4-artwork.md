# AirPods 4 artwork

AirPods 4, AirPods 4 ANC, and both AirPods 5 variants share the AirPods 4
artwork on the main screen. Model capabilities remain independent.

Source: [SpriteOvO/AirPodsDesktop](https://github.com/SpriteOvO/AirPodsDesktop),
commit `56b04001f75044dbd15507303a9438c71e93c6e4`,
[`Source/Resource/Image/Animation/AirPods_4.png`](https://github.com/SpriteOvO/AirPodsDesktop/blob/56b04001f75044dbd15507303a9438c71e93c6e4/Source/Resource/Image/Animation/AirPods_4.png).
The upstream project is distributed under GPL-3.0; see its
[LICENSE](https://github.com/SpriteOvO/AirPodsDesktop/blob/56b04001f75044dbd15507303a9438c71e93c6e4/LICENSE).
Credit to the AirPodsDesktop contributors; Apple product designs and marks belong
to Apple. The GPL license text is also included in this repository's LICENSE.

`airpods_4.png` is the unmodified 488 x 244 transparent source image. The other
four PNGs extract parts of that image, without redrawing or changing their colors:

| Resource suffix | Source rectangle (x, y, width, height) |
| --- | --- |
| buds | 30, 55, 190, 150 |
| left | 30, 55, 108, 150 |
| right | 138, 55, 82, 150 |
| case | 260, 15, 220, 210 |

AirPods 4 connection overlays show the complete static artwork. AirPods 5
connection overlays use the existing video animation, so the connection popup
does not become a still image. The bundled animation is a shared fallback rather
than an AirPods 5-specific asset. Transparent PNG backgrounds follow the popup
theme where static artwork is used.
