# BYDcontrol

Application Android pour BYD Seal U DMI (Dilink 3 / Android 9) qui ajoute des
fonctions absentes d'usine. **Fonction 1 livrée : jouer un son par les
haut-parleurs quand le véhicule est verrouillé.**

## Comment ça marche

`LockSoundService` est un *foreground service* qui :
1. récupère `BYDAutoDoorLockDevice` du framework BYD et enregistre un listener ;
2. à chaque `onDoorLockStatusChanged(area, state)`, si `state == DOOR_LOCK_STATE_LOCK`, joue `res/raw/lock_chirp.wav` ;
3. garde un `PARTIAL_WAKE_LOCK` pour que le callback parte même quand le système commence à s'endormir, et prend brièvement le focus audio pour réveiller l'ampli.

Le SDK BYD étant **lecture seule** côté serrure (aucune commande lock/unlock
n'existe), on ne fait qu'**observer** l'événement — c'est suffisant pour ce besoin.

## Architecture

- `:bydsdk` — stubs `compileOnly` du framework (`android.hardware.bydauto.*`).
  Jamais inclus dans l'APK ; les vraies classes viennent du système de la voiture.
- `:app` — l'app (service, receiver de boot, UI minimale).

### Constantes : pourquoi `Integer.parseInt("2")` dans les stubs ?
Les valeurs réelles de `DOOR_LOCK_STATE_*` ne sont pas publiées dans la Javadoc.
Si on écrivait `= 2`, le compilateur **inlinerait** ce littéral dans le service
et on comparerait à une mauvaise valeur. En initialisant par une expression non
constante, le champ n'a pas d'attribut `ConstantValue` → le compilateur émet une
vraie lecture (`getstatic`) résolue **à l'exécution** vers la valeur réelle de la
voiture. Les placeholders ne servent donc jamais en vrai.

## Build

### Via GitHub Actions (recommandé, vu tes limites Termux/AAPT2)
Push sur `main` → workflow `Build BYDcontrol APK` → APK dans les *artifacts*.

### En local
```bash
gradle wrapper      # génère gradlew une fois
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
```

## Installation via ADB (port déjà activé)

```bash
# 1. Installer
adb install -r app-debug.apk

# 2. Sortir l'app du Doze / optimisation batterie (clé pour la survie en veille)
adb shell dumpsys deviceidle whitelist +com.giino.bydcontrol

# 3. Démarrer le service tout de suite (sinon : après un reboot via BootReceiver)
adb shell am start-foreground-service -n com.giino.bydcontrol/.LockSoundService
#   (sur Android 9 un simple start-service marche aussi)
adb shell am startservice -n com.giino.bydcontrol/.LockSoundService

# 4. Suivre les logs pour valider
adb logcat -s BYDcontrol:*
```

Remplace `lock_chirp.wav` par ton propre son (même nom dans `res/raw/`).

## ⚠️ Deux risques réels à valider sur la voiture

1. **Privilèges BYD.** `registerListener()` peut lever une `SecurityException`
   si le framework exige une permission BYD/système. Le code log proprement
   l'exception (`adb logcat -s BYDcontrol:*`). Si c'est le cas :
   - une permission `pm grant` (normale/dangerous) peut suffire — à identifier dans le message ;
   - une permission *signature* nécessiterait de signer avec la clé plateforme ou d'installer en app système (`adb root` + remount), donc accès ROM.

2. **Veille de l'ampli audio.** L'événement de verrouillage arrive pendant que
   le système est encore éveillé, donc le chirp part normalement avant la mise
   en veille. Mais si l'ampli coupe trop vite, le son peut ne pas sortir. À
   tester ; au besoin, allonger le maintien du focus audio ou tester
   `STREAM_MUSIC` au lieu de `STREAM_NOTIFICATION`.

## Diagnostic rapide
- Aucun log `doorlock changed` au verrouillage → le listener n'est pas reçu (privilèges ou mauvaise instance device).
- Log `Could not register door-lock listener` → voir risque n°1.
- Log `doorlock changed` mais pas de son → voir risque n°2.
