BIKE METER APP  (Android)
=========================

What it is
  A small Android app that opens your speedometer page (http://192.168.4.1/) full screen,
  keeps the screen on, and sends its traffic over the BikeMeter Wi-Fi even when Android
  says "no internet". It retries by itself until the speedometer answers.

HOW TO GET THE APK (free, no coding, about 10 minutes, done on a computer)
  1. Create a free account at github.com and press  New repository.
     Name it  bike-meter-app  and choose "Private" or "Public". Press Create.
  2. On the new repository page press  "uploading an existing file".
  3. Unzip this folder on your computer. Select EVERYTHING inside it
     (the folders  app  and  .github  and the other files) and drag them into the page.
     The folder called .github must be included: it holds the build recipe.
     (If your computer hides folders starting with a dot, turn on "show hidden files".)
  4. Press  Commit changes.
  5. Open the  Actions  tab. A run called "Build APK" starts by itself
     (or press Build APK, then Run workflow). Wait 3 to 5 minutes for the green tick.
  6. Open that run and download  BikeMeter-apk  at the bottom (Artifacts). Unzip it:
     inside is  app-debug.apk.
  7. Send app-debug.apk to your phone and open it. Android asks to allow installing from
     this source: allow it, then Install.

USING IT
  - Join the BikeMeter Wi-Fi, then open the "Bike Meter" app.
  - If Android asks "Stay connected to BikeMeter? No internet", choose Yes / Keep.

CHANGING THE ADDRESS
  If you change the speedometer's address, edit  app/src/main/res/values/strings.xml
  (app_url) and upload it again; the APK rebuilds.
