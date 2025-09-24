const { app, BrowserWindow, protocol, session} = require('electron')
const path = require('path')





// 创建窗口函数
function createWindow() {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        webPreferences: {
            preload: path.join(__dirname, 'preload.cjs'),
            nodeIntegration: false, // 安全设置
            contextIsolation: true,
            webSecurity: false,
            enableBlinkFeatures: 'WebRTC'
        }
    })


    win.loadFile(path.join(__dirname, '../dist/index.html'))

    win.webContents.setUserAgent(
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36'
    );

    // win.webContents.openDevTools();

}



// 应用就绪后初始化
app.whenReady().then(()=>{
    createWindow();
    session.defaultSession.setPermissionRequestHandler(
            (webContents, permission, callback) => {
                const allowedPermissions = ['media', 'webRTC'];
                if (allowedPermissions.includes(permission)) {
                    callback(true); // 允许
                } else {
                    callback(false);
                }
            });
})
app.commandLine.appendSwitch('enable-features', 'WebRTC');



// 关闭所有窗口时退出应用（macOS 除外）
app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit()
})

// macOS 点击 Dock 图标重新创建窗口
app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
})


