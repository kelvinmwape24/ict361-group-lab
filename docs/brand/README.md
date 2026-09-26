# Brand assets

| File | Use |
|---|---|
| `mu_crest_source.png` | The crest as supplied, 430×538, white background. The archival original — do not edit. |
| `mu_crest_transparent.png` | Background flood-filled to transparency from the edges in, then trimmed. 390×500. This is the working master. |
| `mu_crest_500.png` | 500px-tall transparent version for documents and slides. |

The background was removed by flooding inward from the border rather than by
keying out every white pixel — the crest has white *inside* it (the graduates'
robes, the gaps in the gear, the banner), and a global key punches holes
through all of it.

## Where it is used in the app

| Asset | Generated from | Notes |
|---|---|---|
| `res/drawable-*dpi/mu_crest.png` | the master | Five densities, sized for a 120dp-tall splash; the 46dp auth header reuses it scaled down |
| `res/drawable-*dpi/ic_launcher_foreground.png` | the master | Adaptive-icon foreground, crest inset into the 66dp safe circle of the 108dp canvas so no launcher mask clips the shield |
| `res/mipmap-*dpi/ic_launcher.png`, `ic_launcher_round.png` | the master | Legacy PNG launcher icons for API 24–25, crest on a white field |
| `res/drawable/splash_icon.xml` | — | Insets `mu_crest` for the Android 12+ splash, which masks to a circle |

## Colours

Sampled from the crest, not picked by eye:

| | Hex | On white |
|---|---|---|
| Shield blue | `#4E52B3` | 6.60 : 1 |
| Shield blue, dark | `#3A3E8F` | 9.27 : 1 |
| Shield red | `#D23B30` | 4.72 : 1 |

`res/values/colors.xml` is the single source of truth. Do not hardcode a hex
in a layout.
