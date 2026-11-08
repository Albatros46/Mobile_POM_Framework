# Mobile POM Framework · one set of page objects for Android and iOS

[![Mobile Tests](https://github.com/Albatros46/Mobile_POM_Framework/actions/workflows/mobile-tests.yml/badge.svg)](https://github.com/Albatros46/Mobile_POM_Framework/actions/workflows/mobile-tests.yml)
![Java](https://img.shields.io/badge/Java-21-orange) ![Appium](https://img.shields.io/badge/Appium-3.9-662D91) ![UiAutomator2](https://img.shields.io/badge/UiAutomator2-8.7-3DDC84) ![XCUITest](https://img.shields.io/badge/XCUITest-12.22-0D96F6) ![TestNG](https://img.shields.io/badge/TestNG-7.12-CD6E00)

A Page Object framework for Appium that runs the same tests on Android (UiAutomator2) and iOS (XCUITest). The apps under test are Sauce Labs' **[My Demo App](https://github.com/saucelabs/my-demo-app-android)** 2.3.0 for Android and iOS, a shop app made for test automation.

## The idea

Tests call page methods only. Each element field carries an Android and an iOS locator side by side:

```java
@AndroidFindBy(id = AndroidIds.PREFIX + "cartBt")
@iOSXCUITFindBy(accessibility = "AddToCart")
private WebElement addToCartButton;
```

Where the two apps behave differently, the difference stays inside the page. For example, going back to the catalog uses the Android app menu and the iOS tab bar (`ProductPage.backToCatalog()`), and the cart badge is read in a different way on each platform. `-Dplatform=android|ios` picks the driver; nothing else changes in the tests.

## Framework parts

| Part | File | Role |
|---|---|---|
| `Platform` | `framework/config/Platform.java` | enum `ANDROID`, `IOS`; read from `-Dplatform`, case-insensitive, `android` when not set |
| `Settings` | `framework/config/Settings.java` | run settings from `-D` properties, each with a default (see below) |
| `DriverFactory` | `framework/driver/DriverFactory.java` | builds `UiAutomator2Options` or `XCUITestOptions`; fails early with the download hint when the app file is missing; WebDriverAgent startup retries (2, every 20 s) |
| `Session` | `framework/driver/Session.java` | the Appium driver of the current thread (`ThreadLocal`) |
| `BasePage` | `framework/pages/BasePage.java` | waits: `visible`, `tap`, `atLeast` for lists; while it waits it clears Android's "isn't responding" dialog |
| `OnlyOn` | `framework/pages/OnlyOn.java` | marks a field that has a locator for one platform only; the `reason` is required |
| `FailureEvidence` | `framework/evidence/FailureEvidence.java` | TestNG listener: on failure saves a screenshot and the page source to `target/evidence/<platform>/`; with `-Devidence.print=true` the page source also goes into the log |
| `BaseMobileTest` | `framework/testing/BaseMobileTest.java` | starts a new session before and ends it after each test |

### Settings

| Property | Default | Meaning |
|---|---|---|
| `platform` | `android` | `android` or `ios` |
| `appium.url` | `http://127.0.0.1:4723` | Appium server |
| `wait.seconds` | `30` | explicit wait timeout |
| `android.app` | `apps/mda-2.3.0-27.apk` | Android app under test |
| `ios.app` | `apps/SauceLabs-Demo-App.app` | iOS simulator app under test |
| `ios.device` | `iPhone 16` | simulator name |
| `ios.version` | empty | empty: Appium picks the newest runtime |
| `evidence.print` | `false` | print the page source of a failed test into the log |

## Guard rails without a device

Group `unit`, 10 tests. They need no emulator, simulator or Appium server, and run in CI before the device jobs.

| Class | Tests | Checks |
|---|---|---|
| `PlatformTest` | 3 | default is Android · `ios` in any case and with spaces · an unknown value fails and names the allowed values |
| `SettingsTest` | 3 | defaults · `-D` overrides · a blank `ios.version` is empty |
| `DriverFactoryTest` | 3 | Android capabilities · iOS capabilities including the WebDriverAgent retries · a missing app file gives the download hint |
| `PageLocatorCoverageTest` | 1 | every `WebElement` or `List<WebElement>` field of `CatalogPage`, `ProductPage` and `CartPage` has an Android and an iOS locator, unless `@OnlyOn` says why |

## Tests

| Class | Tests | Checks |
|---|---|---|
| `CatalogTest` | 4 (2 known-bug, one with 2 data rows) | `smoke`: the catalog shows more than one product · the first product starts with "Sauce Labs Backpack" · known bug MDA-03 (Android, 2 cards) · known bug MDA-04 (iOS price label) |
| `ProductTest` | 3 | `smoke`: detail page name equals the catalog name · `smoke`: detail price equals the catalog price (skipped on iOS, MDA-04) · quantity starts at 1 and changes by +1 and -1 |
| `CartTest` | 3 | `smoke`: an added product is in the cart with "1 Items" · two of one product give "2 Items" · removing the only product empties the cart |

Groups: `smoke` runs 4 tests (1 in `CatalogTest`, 2 in `ProductTest`, 1 in `CartTest`); `known-bug` runs the 2 known-bug methods (3 invocations, because MDA-03 has a data provider with 2 rows). On Android the MDA-04 invocation is skipped; on iOS the MDA-03 invocations and the price test are skipped.

Each test starts with a fresh session (`noReset=false`), so no test sees the cart or the state of another.

### Defects found

| ID | Platform | Finding | Test |
|---|---|---|---|
| **MDA-03** | Android, crash | From a fresh start, tapping any catalog card other than the first crashes My Demo App 2.3.0 at `ProductCatalogFragment.java:156`: a `NullPointerException` (`Integer.intValue()` on null) for "Sauce Labs Backpack (green)", and an `ArrayIndexOutOfBoundsException` (length=6, index=6) for "Sauce Labs Bike Light". Found in the device crash log after the tests showed the home screen. | `known_bug_mda_03_…`, data provider with 2 cards |
| **MDA-04** | iOS, accessibility | The catalog price element exposes only "Product Price" (text, value, label and name). The amount is not available to VoiceOver or to automation, because the storyboard sets the accessibility label "Product Price" on the price label. | `known_bug_mda_04_…`; the price comparison test is skipped on iOS with this reason |

A known-bug test describes the current behaviour and passes while the defect is there. It turns red once the app is fixed, and then the assertion has to change. CI runs the known-bug tests after the regular tests, each in its own Maven call.

### Platform differences

| Area | Android | iOS |
|---|---|---|
| First product name | "Sauce Labs Backpack" | "Sauce Labs Backpack - Black" (tests check the prefix) |
| Product detail name | `productTV` | no id: the first static text that is neither "Products" nor a price |
| Opening a product from the catalog | tap on the picture (`productIV`) | tap on the whole cell (`ProductItem`) |
| Scrolling to a product | `UiScrollable.scrollIntoView` | `mobile: scroll` with a predicate on the label |
| Cart badge | text of `cartTV` | the badge is not inside the cart tab button and has no id: it is the static text next to the `RoundView Icons` image (`GrayRoundView` or `GreenRoundView`), read from its name |
| Cart item names | `titleTV` | no ids: the name is the static text right before a "$ " price |
| Cart item count | `itemsTV` | `label MATCHES '[0-9]+ Items'` (`ENDSWITH` also matched the hidden "No Items") |
| Add to cart | button `cartBt` | button `AddToCart` |
| Back to catalog | app menu, then "Catalog" | tab bar, `Catalog-tab-item` |

## What went wrong, and what it taught me

| Problem | Cause | Fix |
|---|---|---|
| Appium could not start any Android session | the server did not see `ANDROID_HOME` | start Appium with `ANDROID_HOME` set (see the run steps) |
| Tests from the catalog found an empty product list | page-factory lists are looked up once, without waiting (`Duration.ZERO`) | `BasePage.atLeast` waits until the list has enough elements |
| "Back" from the product page closed the app | on Android the system back key leaves the app from this screen | the app menu on Android, the tab bar on iOS |
| Two products in the cart, but only one arrived | the colour variants crash the app (MDA-03); the product page also returned before its screen was shown | every page waits for its own screen; `addToCart` waits until the badge shows the expected count; the cart test adds quantity 2 of one product |
| A count-based wait passed without a real add | the badge is missing while the screen loads, so 0 to 1 looked like an add | the wait is for "before + quantity", not for "more than before" |
| The iOS item count never became visible | the predicate `ENDSWITH ' Items'` also matched the hidden "No Items" text | `MATCHES '[0-9]+ Items'` |
| iOS locators were guesses at first | no Mac available locally | locators taken from the app's own XCUITests and storyboard, then corrected from the page source printed into the CI log (`-Devidence.print=true`) |
| The Android run lost the emulator after the crash test | after MDA-03 crashes the app, adb sometimes reports the device offline | CI runs the known-bug tests after the regular tests, in their own `mvn` call |
| One iOS session waited 10 minutes | WebDriverAgent did not start once | `wdaStartupRetries` 2, `wdaStartupRetryInterval` 20 s |
| Unit tests leaked a system property into the next class | with `-Dgroups`, TestNG skips `@BeforeMethod`/`@AfterMethod` methods without `alwaysRun` | `alwaysRun = true` on the restore methods |

### Control run

Removing the add-to-cart tap from `ProductPage` turned all 3 `CartTest` tests red on the emulator. The tap was restored afterwards.

## Measured

| | Local Android emulator (my laptop) | GitHub Actions |
|---|---|---|
| Unit tests (`-Dgroups=unit`) | not measured | about 15 s |
| Android job | 8 regular tests: 2 runs in a row, green, about 4 min per full run | about 3 to 4 min |
| iOS simulator job | not measured | about 15 to 17 min (WebDriverAgent build included) |

## Status

| Platform | Where | Result |
|---|---|---|
| Android 15 emulator | locally (2 runs in a row) and GitHub Actions | all regular tests green, known-bug tests reproduce MDA-03 |
| iOS simulator (iPhone 16) | GitHub Actions, macOS runner | catalog and cart tests passed, the product name check passed; the iOS job is **not green yet**: in the last run one session could not start (WebDriverAgent did not come up), so the product tests were skipped |

The iOS job is kept in CI on purpose: its failures print the page source, which is how every iOS locator here was corrected.

## Run it

### Android

Needs JDK 21+, Maven, Node.js 22+, and the Android SDK (`ANDROID_HOME`) with the image `system-images;android-35;default;x86_64`.

```powershell
npm ci                                                  # Appium server and drivers, pinned in package.json
.\scripts\download-apps.ps1                             # My Demo App, SHA-256 checked (bash scripts/download-apps.sh on Linux and macOS)
.\scripts\create-emulator.ps1 -AvdHome E:\Android\avd   # once; creates the AVD "qa_api35"
.\scripts\start-emulator.ps1 -AvdHome E:\Android\avd    # waits until Android has booted
npm run doctor:android                                  # checks the UiAutomator2 driver setup
npm run appium                                          # same as npx appium --port 4723; keep this terminal open
```

In a second terminal:

```powershell
mvn test -Dplatform=android                 # all tests, known-bug included
mvn test -Dgroups=smoke                     # the 4 smoke tests
mvn test -Dgroups=known-bug                 # the known-bug tests only
mvn test -Dgroups=unit                      # no device and no Appium server needed
```

`-AvdHome` is optional; it keeps the virtual disk off a drive that is short of space. `ANDROID_HOME` must be set in the shell that starts `npm run appium`.

### iOS

Needs macOS with Xcode and an installed iOS simulator runtime.

```bash
npm ci
bash scripts/download-apps.sh
npm run doctor:ios
npm run appium
mvn test -Dplatform=ios -Dios.device="iPhone 16"
```

The iOS CI job uses `-Dwait.seconds=60`, because WebDriverAgent is slower to respond than the Android emulator.

### CI

Workflow `.github/workflows/mobile-tests.yml`, triggered on pushes to `main`, pull requests and manual runs. Cancels a running workflow when a newer run starts on the same ref.

| Job | Runner | Steps |
|---|---|---|
| `unit` | Ubuntu | `mvn test -Dgroups=unit` (10 tests) |
| `android` (needs `unit`) | Ubuntu, KVM enabled | Android 15 emulator (API 35, `pixel_6`, x86_64, no window, swiftshader); Appium started and waited for on port 4723; `mvn test -Dplatform=android -DexcludedGroups=unit,known-bug`, then `mvn test -Dplatform=android -Dgroups=known-bug` |
| `ios` (needs `unit`) | macOS 15 | `npm ci`, app download, list of simulators; Appium started; `mvn test -Dplatform=ios -Dios.device="iPhone 16" -Dwait.seconds=60 -Devidence.print=true -DexcludedGroups=unit,known-bug`, then the known-bug tests in a separate step |

The `android` and `ios` jobs upload `target/surefire-reports`, `target/evidence` and the Appium log as artifacts, kept for 14 days, also when a test fails.

## Structure

```
apps/                                       downloaded My Demo App (APK and iOS simulator app), not committed
scripts/                                    download-apps.ps1 / .sh, create-emulator.ps1, start-emulator.ps1
package.json                                Appium 3.9.0 server, UiAutomator2 driver 8.7.3, XCUITest driver 12.22.8
pom.xml                                     Java 21, java-client 11.0.0, Selenium 4.52.0, TestNG 7.12.0, AssertJ 3.27.7
src/main/java/com/servet/mobile/
├── framework/
│   ├── config/        Platform, Settings
│   ├── driver/        DriverFactory, Session
│   ├── evidence/      FailureEvidence
│   ├── pages/         BasePage, OnlyOn
│   └── testing/       BaseMobileTest
└── mydemoapp/
    ├── MyDemoApp                           package names
    └── pages/         AndroidIds, CatalogPage, ProductPage, CartPage
src/test/java/com/servet/mobile/
├── framework/         PageLocatorCoverageTest
│   ├── config/        PlatformTest, SettingsTest
│   └── driver/        DriverFactoryTest
└── mydemoapp/tests/   CatalogTest, ProductTest, CartTest
```

## Next steps

Hybrid apps and WebViews, parallel devices (the session is already kept per thread), and a device cloud.
