// ====== LAUNCHER CONFIG ====== //
var config = {
    dir: ".kj-launcher", // Launcher directory
    title: "KeeperJerry's Launcher", // Window title
    icons: [ "favicon.png" ], // Window icon paths

    // Auth config
    newsURL: "https://launcher.keeperjerry.ru/", // News WebView URL
    linkText: "Забыли пароль?", // Text for link under "Auth" button
    linkURL: new java.net.URL("https://mirror.keeperjerry.ru/"), // URL for link under "Auth" button

    // Settings defaults
    settingsMagic: 0xC0DE5, // Ancient magic, don't touch
    autoEnterDefault: false, // Should autoEnter be enabled by default?
    fullScreenDefault: false, // Should fullScreen be enabled by default?
    ramDefault: 1024 // Default RAM amount (0 for auto)
};

// ====== DON'T TOUCH! ====== //
var dir = IOHelper.HOME_DIR.resolve(config.dir);
if (JVMHelper.OS_TYPE == JVMHelperOS.MUSTDIE)
{
    dir = IOHelper.HOME_DIR_WIN.resolve(config.dir);
}
if (!IOHelper.isDir(dir)) {
    java.nio.file.Files.createDirectory(dir);
}
var defaultUpdatesDir = dir.resolve("updates");
if (!IOHelper.isDir(defaultUpdatesDir)) {
    java.nio.file.Files.createDirectory(defaultUpdatesDir);
}
