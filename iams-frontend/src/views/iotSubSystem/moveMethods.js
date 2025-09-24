import {
    cancelSleepColumn,
    getColumnStatus,
    leftMoveColumn,
    resetColumn,
    rightMoveColumn,
    unlockColumn
} from '@/api/system/move.js'
import { ElMessage, ElMessageBox } from "element-plus";
import { getInfo } from '@/api/system/GDIpInfo.js'

// 错误码判断
function JudgeIsErrorZt(MJJZTLX) {
    const ztData = {
        "ZT_00": "未知指令", "ZT_01": "禁止移动、自动保护", "ZT_02": "门禁保护、前入口有人进出保护",
        "ZT_03": "通道内红外线保护", "ZT_09": "侧列门面保护", "ZT_0F": "解除通道内红外线保护",
        "ZT_12": "开架限位开关故障", "ZT_13": "合架限位开关故障", "ZT_14": "电机故障",
        "ZT_1C": "移动按键禁用", "ZT_21": "无盲点保护", "ZT_24": "压力传感器保护",
        "ZT_25": "后入口有人进出保护", "ZT_26": "传动机构锁定", "ZT_29": "压力传感器脱开",
        "ZT_2A": "通道有人，移开架体", "ZT_2E": "烟雾输出", "ZT_4E": "锁定",
        "ZT_80": "架体关闭超时", "ZT_81": "架体打开超时，超距", "ZT_82": "架体关闭超时",
        "ZT_83": "邻居读取超时", "ZT_86": "架体关闭超距", "ZT_87": "架体打开超距",
        "ZT_90": "电机驱动，反馈故障", "ZT_91": "自动保护"
    };
    const res = ztData[MJJZTLX];
    if (!res) {
        console.log("没有此错误码" + MJJZTLX);
        return false;
    }
    console.log("系统错误码为--", `${MJJZTLX}:${res}`);
    return res;
}

// 公共工具函数
const showMsg = (msg, type = 'success', duration = 3000) => ElMessage({ type, message: msg, duration });
const showConfirm = (msg, title = '系统提示',type = 'warning') => ElMessageBox.confirm(msg, title, { confirmButtonText: '确定', cancelButtonText: '取消', type: type });
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
const createLoading = (proxy, text) => proxy.$loading({ lock: true, text, spinner: "el-icon-loading" });

// 验证位置格式和范围
const validateLocation = (row) => {
    const { shitiLocation, exactLocation } = row;
    if (!shitiLocation || !exactLocation) {
        showMsg('档案位置信息不完整，请检查实体档案位置是否录入。', 'error');
        return false;
    }

    const locParts = shitiLocation.split('-');
    if (locParts.length !== 3) {
        showConfirm('请检查档案位置格式是否正确！正确格式参考: 第一区第二列左侧第三层第四节', '系统警告');
        return false;
    }

    const [quHao, lieHao] = locParts;
    const quHaoList = ['1', '2', '3', '4'];
    const lieHaoList = Array.from({ length: 18 }, (_, i) => (i + 1).toString());

    if (!quHaoList.includes(quHao) || !lieHaoList.includes(lieHao)) {
        showConfirm('请检查档案位置格式！区号仅限1-4，列号仅限1-18', '系统警告');
        return false;
    }

    return { quHao, lieHao, aOrB: locParts[2] };
};

// 处理休眠状态   已经现场测试  无异常
const handleSleepStatus = async (proxy, ip, port, isPower, MJJZTLX) => {
    // 输出当前时间
    console.log(`标志位置isPower`, isPower);
    if (isPower == 0) {
        let loading_handleSleepStatus = createLoading(proxy, '正在解除休眠状态...');
        await delay(1000); // 初始延迟，确保指令发送

        const res = await cancelSleepColumn(ip, port);
        if (res.data.data !== "1") {
            loading_handleSleepStatus.close();
            await showConfirm('关闭休眠失败,请手动移动至固定列打开休眠状态!', '系统警告');
            return false;
        }
        // await delay(5000) // 确保休眠解锁成功
        // 持续检测休眠状态，直到 isPower 变为 1
        // showMsg('正在关闭休眠状态，请稍候...', 'success');
        let retryCount = 0;
        const maxRetries = 60; // 最大重试次数
        const retryInterval = 500; // 重试间隔(毫秒)

        while (retryCount < maxRetries) {
            await delay(retryInterval);
            retryCount++;

            const statusRes = await getColumnStatus(ip, port);
            const currentIsPower = statusRes.data.data[0].IsPower;
            console.log('当前IsPower状态码:', currentIsPower)
            if (currentIsPower == 1) {
                loading_handleSleepStatus.setText(`成功解锁休眠状态,初始化中...`);
                await delay(7000)
                console.log('休眠状态关闭成功')
                loading_handleSleepStatus.close();
                return true;
            }

            // 更新加载提示
            loading_handleSleepStatus.setText(`正在关闭休眠状态: 请稍等 ${retryCount}/${maxRetries}`);
        }

        // 达到最大重试次数仍未成功
        loading_handleSleepStatus.close();
        showMsg('休眠状态关闭超时，请手动检查', 'error');
        return false;
    }
    return true;
};

// 检查合架状态
const checkColumnStatus = async (proxy, ip, port) => {
    const statusRes = await getColumnStatus(ip, port);
    const { MJJZTLX } = statusRes.data.data[0];
    // 列已合架
    return MJJZTLX == 'ZT_07';
};


// 解锁并检查错误码，然后循环判断合架
const unlockAndCheckErrorAndReset = async (proxy,ip, port,quHao) => {
    const unlockRes = await unlockColumn(ip, port);
    if (unlockRes.data.data !== "1") {
        showMsg('解锁失败', 'error');
        return false;
    }

    let loading_judge;
    // showMsg('列解锁中...请稍候', 'info');
    let loading_lock = createLoading(proxy, '列解锁中...请稍候');


    // 循环检测解锁状态，直到IsLock变为0-------------------------------------------   已现场测试无异常
    let retryCount = 0;
    let maxRetries = 60; // 最大重试次数
    let retryInterval = 500; // 重试间隔(毫秒)
    while (retryCount < maxRetries) {
        await delay(retryInterval);
        retryCount++;

        const statusRes = await getColumnStatus(ip, port);
        const { IsLock, MJJZTLX } = statusRes.data.data[0];

        console.log('当前IsLock状态码:', IsLock)

        // 判断是否解锁成功 且 MJJZTLX为ZT_0E(解除禁止)时，跳出循环
        if (IsLock == 0 && MJJZTLX == 'ZT_0E') {
            // showMsg('列解锁成功...正在检测当前状态', 'success');
            loading_lock.setText('列解锁成功');
            loading_lock.close();
            loading_judge = createLoading(proxy, '正在检测当前状态...');
            await delay(1500)
            // 解锁确认成功，发送合架指令
            const res = await resetColumn(ip, port);
            if (res.data.data == "1") {
                loading_judge.setText('合架指令已发送,持续检测合架状态中...');
                await delay(3000)  // 等待解锁状态 变为移动状态
            }
            break; // 解锁成功，跳出循环
        }

        // 更新提示信息
        // showMsg(`列解锁中...请稍等 ${retryCount}/${maxRetries}`, 'info');
        loading_lock.setText(`列解锁中...请稍等 ${retryCount}/${maxRetries}`);
    }
    // 如果达到最大重试次数仍未解锁成功
    if (retryCount >= maxRetries) {
        // showMsg('列解锁超时，请手动检查', 'error');
        await showConfirm('列解锁超时，请手动检查', '系统警告')
        loading_lock.close();
        return false;
    }

    // 循环检测合架是否进行或者已经成功
    retryCount = 0;
    maxRetries = 300; // 最大重试次数
    retryInterval = 500; // 重试间隔(毫秒)
    let currentMJJZTLX;
    let currentMJJZTLXArr = [];
    while (retryCount < maxRetries) {
        await delay(retryInterval);
        retryCount++;
        const statusRes = await getColumnStatus(ip, port);
        // 检测状态码是否异常
        const { MJJZTLX, MJJZTLXName } = statusRes.data.data[0];

        // 检测状态码是否为ZT_07,为ZT_07则提示成功并结束
        if(MJJZTLX=='ZT_07'){
            loading_judge.close();// 关闭加载提示
            await showConfirm('第'+quHao+'区所有架体成功归位,合架成功', '系统成功提示','success')
            loading_judge.close(); // 关闭加载提示
            return true
        }

        // 检测MJJZTLX不存在于currentMJJZTLXArr,就加入
        if (!currentMJJZTLXArr.includes(MJJZTLX)) {
            currentMJJZTLXArr.push(MJJZTLX);
        }
        currentMJJZTLX = MJJZTLX+':'+MJJZTLXName;

        // MMJZTLX在执行了移动命令之后,未立即响应,仍未ZT_0E，即解除禁止状态，这里判断状态码是否为ZT_0E，如果是 则continue
        if (MJJZTLX == 'ZT_0E' || MJJZTLXName.includes('解除禁止')) {
            loading_judge.setText(`合架中...请稍等 ${retryCount}/${maxRetries}`);
            continue;
        }

        // 当currentMJJZTLXArr中的状态码数量>3，则提醒用户是否继续重试   或者 不是移动中时候，则提醒用户是否继续重试  或者不是解除禁止  也不是未知指令
        if (currentMJJZTLXArr.length > 3 || (MJJZTLX != 'ZT_0C' && MJJZTLX != 'ZT_00')) {
            console.log('当前的不同状态码集合:currentMJJZTLXArr:', currentMJJZTLXArr)
            const confirmResult = await showConfirm('检测到柜体异常,建议人工检查架体是否异常(如过道有人、遮挡物等);若确认无异常，请点击确认继续合架', '严重警告','error')
                .then(async () => {
                    // 继续执行合架
                    const res = await resetColumn(ip, port);
                    if (res.data.data == "1") {
                        currentMJJZTLXArr = []
                        loading_judge.setText('重新发送合架指令,持续检测合架状态中...');
                    }
                })
                .catch(async () => {
                    loading_judge.close();
                    await showConfirm('用户取消合架,点击确定进行重试，点击取消进行取消操作!', '系统警告');
                    return false;
                });
        }

        loading_judge.setText(`合架中...请稍等 ${retryCount}/${maxRetries}`);
    }
    // 如果达到最大重试次数仍未解锁成功
    if (retryCount >= maxRetries) {
        await showConfirm('合架超时,当前状态为['+currentMJJZTLX+'],请手动检查柜体异常', '严重警告','error')
        loading_lock.close();
        return false;
    }
}

// 解锁并检查错误码，然后移动架子
const unlockAndCheckErrorAndMove = async (proxy,ip, port,moveParams) => {
    const unlockRes = await unlockColumn(ip, port);
    if (unlockRes.data.data !== "1") {
        showMsg('解锁失败', 'error');
        return false;
    }

    let loading_judge;
    // showMsg('列解锁中...请稍候', 'info');
    let loading_lock = createLoading(proxy, '列解锁中...请稍候');


    // 循环检测解锁状态，直到IsLock变为0-------------------------------------------
    let retryCount = 0;
    let maxRetries = 60; // 最大重试次数
    let retryInterval = 500; // 重试间隔(毫秒)
    while (retryCount < maxRetries) {
        await delay(retryInterval);
        retryCount++;

        const statusRes = await getColumnStatus(ip, port);
        const { IsLock, MJJZTLX } = statusRes.data.data[0];
        console.log('当前IsLock状态码:', IsLock)

        // 一直到解锁成功 且 MJJZTLX为ZT_0E(解除禁止)时，跳出循环
        if (IsLock == 0 && MJJZTLX == 'ZT_0E') {
            // showMsg('列解锁成功...正在检测当前状态', 'success');
            loading_lock.setText('列解锁成功');
            loading_lock.close();
            loading_judge = createLoading(proxy, '正在检测当前状态...');
            await delay(2000) // 等待解锁状态 变为移动状态
            // 解锁确认成功，发送移动指令
            await executeMove(proxy,ip, port, moveParams.column, moveParams.direction);
            loading_judge.close();
            break; // 解锁成功，跳出循环
        }

        // 更新提示信息
        loading_lock.setText(`列解锁中...请稍等 ${retryCount}/${maxRetries}`);
    }
    // 如果达到最大重试次数仍未解锁成功
    if (retryCount >= maxRetries) {
        // showMsg('列解锁超时，请手动检查', 'error');
        await showConfirm('列解锁超时，请手动检查', '系统警告')
        loading_lock.close();
        return false;
    }
}
// 执行移动指令
const executeMove = async (proxy,ip, port, column, direction) => {

    const moveFn = direction == 'left' ? leftMoveColumn : rightMoveColumn;
    const res = await moveFn(ip, port, column);
    await delay(3000) // 确保进入移动状态

    let load_move;
    if (res.data.data == "1") {
        // showMsg('正在移动柜子，请耐心等待，或观看监控状态', 'success', 2500);
        load_move = createLoading(proxy, '正在移动柜子，请耐心等待');
    }

    // 循环检测移动是否进行或者已经成功
    let retryCount = 0;
    let maxRetries = 300; // 最大重试次数
    let retryInterval = 500; // 重试间隔(毫秒)
    let currentMJJZTLX;
    // let currentMJJZTLXArr = [];
    while (retryCount < maxRetries) {
        await delay(retryInterval);
        retryCount++;
        const statusRes = await getColumnStatus(ip, port);
        // 检测状态码是否异常
        const { MJJZTLX, MJJZTLXName } = statusRes.data.data[0];
        console.log('当前状态码:', MJJZTLX)
        console.log('当前状态码名称:', MJJZTLXName)
        // 检测状态名称是否含有到位
        if(MJJZTLXName.includes('打开到位')){
            load_move.close();
            await showConfirm(MJJZTLXName, '系统成功提示','success')
            load_move.close();
            return true
        }

        currentMJJZTLX = MJJZTLX+':'+MJJZTLXName;

        // MMJZTLX在执行了移动命令之后,未立即响应,仍未ZT_0E，即解除禁止状态，这里判断状态码是否为ZT_0E，如果是 则continue
        if (MJJZTLX == 'ZT_0E' || MJJZTLXName.includes('解除禁止')) {
            load_move.setText(`移动中...请稍等 ${retryCount}/${maxRetries}`);
            continue;
        }
        // 不含有移动中就说明 没在移动   且忽略未知指令
        if (!MJJZTLXName.includes('移动中') && MJJZTLX!='ZT_00') {
            // 跳过未知指令之后的合架提示
            if(!MJJZTLXName.includes('关闭到位')){
                // console.log('当前的不同状态码集合:currentMJJZTLXArr:', currentMJJZTLXArr)
                const confirmResult = await showConfirm('检测到柜体异常,建议人工检查架体是否异常(如过道有人、遮挡物等);若确认无异常，请点击确认继续尝试移动', '严重警告','error')
                    .then(async () => {
                        // 继续执行移动
                        const res = await moveFn(ip, port, column);
                        if (res.data.data == "1") {
                            // currentMJJZTLXArr = []
                            load_move.setText('重新发送移动指令,持续检测状态中...');
                        }
                    })
                    .catch(async () => {
                        load_move.close();
                        await showConfirm('用户取消移动,点击确定进行重试，点击取消进行取消操作!', '系统警告');
                        return false;
                    });
            }
        }

        load_move.setText(`移动中...请稍等 ${retryCount}/${maxRetries}`);
    }
    // 如果达到最大重试次数仍未解锁成功
    if (retryCount >= maxRetries) {
        await showConfirm('移动超时,当前状态为['+currentMJJZTLX+'],请手动检查柜体异常', '严重警告','error')
        load_move.close();
        return false;
    }

    return res;
};

// 处理移动核心逻辑
const handleMoveLogic = async (proxy,ip, port, quHao, lieHao, aOrB, loading3) => {
    const statusRes = await getColumnStatus(ip, port);
    await delay(1500); // 状态获取延迟

    // 统一转为字符串
    quHao = quHao.toString()
    lieHao = lieHao.toString()
    aOrB = aOrB.toString()

    let columnStatus = statusRes.data.data[0].ColumnStatus;
    // 将字符串columnStatus的首位改为3      因为机器第一位列是固定列，所以需要将columnStatus的0位置改为3
    if(quHao.toString() == '1' || quHao.toString() == '2'){
        // 1-2区域是从左到右,首位固定类默认为 1(右到位)
        columnStatus = '3' + columnStatus.substring(1);
    }
    else if(quHao.toString() == '3' || quHao.toString() == '4'){
        // 3-4区域是从右到左,首位固定类默认为 2(左到位)     // 现场检查
        // columnStatus反转
        columnStatus = columnStatus.split('').reverse().join('');
    }
    let colState = columnStatus[parseInt(lieHao) - 1];
    colState = parseInt(colState)
    const isLeftArea = ['1', '2'].includes(quHao.toString());

    let moveParams;


    //------------------------------------------------------
    if (isLeftArea) {
        if (aOrB == 'A') { // 左区左侧
            if(lieHao==1 || lieHao=='1'){
                loading3.close();
                await showConfirm('此列左侧已开放,且此列为固定列,不可移动')
                loading3.close();
                console.log("此列左侧已开放,且此列为固定列,不可移动")
                return false;  // 直接不进行后续的移动操作
            }
            moveParams = [0, 1, 3].includes(colState) ? { column: parseInt(lieHao), direction: 'right' } : null;
        }
        else { // 左区右侧
            if(lieHao==18 || lieHao=='18' || (lieHao==13 && quHao=='2')|| (lieHao=='13' && quHao=='2')){
                console.log("此面在最外侧,请将此区合架之后去取档案.")
                let isHeJia = await checkColumnStatus(proxy,ip, port)
                if(!isHeJia){
                    loading3.close();
                    // showMsg("开始合架", 'warning')
                    await showConfirm("此面在最外侧,请将此区合架之后去取档案。", '系统提示')
                    loading3.close();
                    return false;  // 直接不进行后续的移动操作
                }
                else {
                    loading3.close();
                    await showConfirm("此面在最外侧,且已经合架,无需移动,请直接去取档案!", '系统提示')
                    loading3.close();
                    return false;  // 直接不进行后续的移动操作
                }
            }
            if ([0, 2].includes(colState)) moveParams = { column: parseInt(lieHao), direction: 'left' };
            else if (colState == 3) moveParams = { column: parseInt(lieHao) + 1, direction: 'right' };
            else if (colState == 1) moveParams = null;
        }
    }
    else { // 右区
        if (aOrB == 'A') { // 右区左侧
            if(lieHao==18 || lieHao=='18' || (lieHao==13 && quHao=='2')|| (lieHao=='13' && quHao=='2')){
                console.log("此面在最外侧,请将此区合架之后去取档案.")
                let isHeJia = await checkColumnStatus(proxy,ip, port)
                if(!isHeJia){
                    // showMsg("开始合架", 'warning')
                    loading3.close();
                    await showConfirm("此面在最外侧,请将此区合架之后去取档案。", '系统提示')
                    loading3.close();
                    return false;  // 直接不进行后续的移动操作

                }else {
                    loading3.close();
                    await showConfirm("此面在最外侧,且已经合架,无需移动,请直接去取档案!", '系统提示')
                    loading3.close();
                    return false;  // 直接不进行后续的移动操作
                }
            }
            if ([0, 1].includes(colState)) moveParams = { column: parseInt(lieHao), direction: 'right' };
            else if (colState == 3) moveParams = { column: parseInt(lieHao) + 1, direction: 'left' };
            else moveParams = null;
        }
        else { // 需要开右区右侧的柜子,如果是第1列，则提醒为固定列，请勿移动
            if(lieHao==1 || lieHao=='1'){
                loading3.close();
                await showConfirm('此列右侧已开放,且此列为固定列,不可移动')
                loading3.close();
                console.log("此列右侧已开放,且此列为固定列,不可移动")
                return false;  // 直接不进行后续的移动操作
            }
            moveParams = [0, 2, 3].includes(colState) ? { column: parseInt(lieHao), direction: 'left' } : null;
        }
    }
    console.log('判断移动之后的moveParams:', moveParams,'----------------------------------------')
    // -----------------------------------------------------
    if (!moveParams) {
        // showMsg('档案柜已到位', 'success', 2500);
        await showConfirm('此档案柜已到位,无需移动', '系统成功提示','success')
        loading3.close();
        return;
    }

    loading3.close();
    if (await unlockAndCheckErrorAndMove(proxy,ip, port, moveParams)) {}
};

// 移动架子主函数
export async function handleMoveMeth(row, proxy) {
    const locInfo = validateLocation(row);
    if (!locInfo) return;

    let { quHao, lieHao, aOrB } = locInfo;
    // 这里判断如果是第二区 aOrB无论是多少都改为'B',以为只有右侧，但是用户查看到的是A   13列真实是左
    if (quHao == '2' || quHao == 2){
        // 非13列是B  13列是A
        if(lieHao!='13' || lieHao!=13){
            aOrB = 'B';
        }
    }

    const { cengHao, jieHao } = row.exactLocation.split('-').reduce((acc, val, i) =>
        ({ ...acc, [i == 0 ? 'cengHao' : 'jieHao']: val }), {});

    const aOrBMap = { 'A': '左', 'B': '右' };
    try {
        await showConfirm(`是否确认打开[解锁]编号为"${row.danghao}"的档案所在列？位置：第${quHao}区第${lieHao}列${aOrBMap[aOrB]}侧第${cengHao}层第${jieHao}节`);

        const loading = createLoading(proxy, "正在发送相关指令...");
        const { data } = await getInfo(quHao);
        const { gdlIp: ip, gdlPort: port } = data;

        const statusRes = await getColumnStatus(ip, port);
        const {IsPower, MJJZTLX} = statusRes.data.data[0];

        // 检测是否处于休眠状态
        if (!await handleSleepStatus(proxy,ip, port, IsPower,MJJZTLX)) {
            loading.close();
            return;
        }

        const loading2 = createLoading(proxy, "正在获取柜子状态...");
        await delay(1500);
        loading2.close();

        const loading3 = createLoading(proxy, "正在发送移动指令...");
        await handleMoveLogic(proxy,ip, port, quHao, lieHao, aOrB, loading3);
        loading.close();
    }
    catch (error) {
        console.error('移动操作失败', error);
    }
}

// 合并架子
export async function handleCombineMeth(row, proxy) {
    if (!row.shitiLocation) {
        await showConfirm('档案位置信息不完整，请检查实体档案位置是否录入。', '系统警告')
        return;
    }

    const quHao = row.shitiLocation.split('-')[0];
    try {
        const { data } = await getInfo(quHao);
        const { gdlIp: ip, gdlPort: port } = data;

        await showConfirm(`是否解锁柜体并执行合架操作?目前为第${quHao}区`);
        const statusRes = await getColumnStatus(ip, port);
        const {IsPower,MJJZTLX} = statusRes.data.data[0];

        if (!await handleSleepStatus(proxy,ip, port, IsPower,MJJZTLX)) return;
        if(MJJZTLX=='ZT_07'){
            await showConfirm('第'+quHao+'区所有架体成功归位,合架成功', '系统成功提示','success')
            return false
        }
        await unlockAndCheckErrorAndReset(proxy,ip, port,quHao)
    }
    catch (error) {
        console.error('合架操作失败', error);
    }
}
