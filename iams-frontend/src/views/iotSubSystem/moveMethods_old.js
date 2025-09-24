import {
    cancelSleepColumn,
    getColumnStatus,
    leftMoveColumn,
    resetColumn,
    rightMoveColumn,
    unlockColumn
} from '@/api/system/move.js'
import {ElMessage, ElMessageBox} from "element-plus";

// import { getCurrentInstance } from 'vue'
// const { proxy } = getCurrentInstance()


import { getInfo } from '@/api/system/GDIpInfo.js'


/**
 * 错误码判断
 * 如果错误码在此指令集中，返回true
 * @param MJJZTLX
 * @returns {boolean}
 * @constructor
 */
function JudgeIsErrorZt(MJJZTLX){
    let zt_data = {
        "ZT_00": "未知指令",
        "ZT_01": "禁止移动、自动保护",
        "ZT_02": "门禁保护、前入口有人进出保护",
        "ZT_03": "通道内红外线保护",
        "ZT_09": "侧列门面保护",
        "ZT_0F": "解除通道内红外线保护",
        "ZT_12": "开架限位开关故障",
        "ZT_13": "合架限位开关故障",
        "ZT_14": "电机故障",
        "ZT_1C": "移动按键禁用",
        "ZT_21": "无盲点保护",
        "ZT_24": "压力传感器保护",
        "ZT_25": "后入口有人进出保护",
        "ZT_26": "传动机构锁定",
        "ZT_29": "压力传感器脱开",
        "ZT_2A": "通道有人，移开架体",
        "ZT_2E": "烟雾输出",
        "ZT_4E": "锁定",
        "ZT_80": "架体关闭超时",
        "ZT_81": "架体打开超时，超距",
        "ZT_82": "架体关闭超时",
        "ZT_83": "邻居读取超时",
        "ZT_86": "架体关闭超距",
        "ZT_87": "架体打开超距",
        "ZT_90": "电机驱动，反馈故障",
        "ZT_91": "自动保护"
    }
    let res = zt_data[MJJZTLX]
    if(res == undefined){
        console.log("没有此错误码"+MJJZTLX)
        return false
    }else {
        console.log("系统错误码为--",MJJZTLX+':'+res)
        return res
    }
}

// 移动架子的操作handleMove   likang
export async function handleMoveMeth(row,proxy) {
    // 检查必要的字段是否存在
    if (!row.shitiLocation || row.shitiLocation === '') {
        ElMessage.error('档案位置信息不完整，请检查档案的实体位置信息');
        return;
    }
    if (!row.exactLocation || row.exactLocation === '') {
        ElMessage.error('档案位置信息不完整，请检查档案的实体位置信息');
        return;
    }

    console.log('row.shitiLocation',row)
    const exactLocation = row.exactLocation

    // console.log('列数据',row.shitiLocation)
    const quHao = row.shitiLocation.split('-')[0]
    // .substring(0, 1);
    const lieHao = row.shitiLocation.split('-')[1]
    // .substring(0, 1);
    const AorB = row.shitiLocation.split('-')[2]
    // .substring(0, 1);

    const cengHao = row.exactLocation.split('-')[0]
    const jieHao = row.exactLocation.split('-')[1]

    const AorBReflect = {
        'A': '左',
        'B': '右'
    }

    // 判断row.shitiLocation安装位置格式是否正确
    if (row.shitiLocation.split('-').length !== 3) {
        ElMessageBox.confirm(
            '请检查档案位置格式是否正确！正确格式参考:  第一区第二列左侧第三层第四节;您的格式为:' + row.shitiLocation,
            '系统警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'error',
            }
        ).then(() => {

        }).catch(() => {

        });
        return;
    }

    let quHaoList = ['1', '2', '3', '4']
    let lieHaoList = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18']
    // 判断quHao是否在quHaoList中
    if (!quHaoList.includes(quHao)) {
        ElMessageBox.confirm(
            '请检查档案位置格式是否正确！正确格式参考:  第一区第二列左侧第三层第四节;' +
            '实际代表1区2列左边。' +
            '但是您的格式为:' + row.shitiLocation + ';请先修改。ps:区号只有1-4,列号只有1-18',
            '系统警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'error',
            }
        ).then(() => {

        }).catch(() => {

        });
        return;
    }
    // 判断lieHao是否在lieHaoList中
    if (!lieHaoList.includes(lieHao)) {
        ElMessageBox.confirm(
            '请检查档案位置格式是否正确！正确格式参考:  第一区第二列左侧第三层第四节;' +
            '实际代表1区2列左边。' +
            '但是您的格式为:' + row.shitiLocation + ';请先修改。ps:区号只有1-4,列号只有1-18',
            '系统警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'error',
            }
        ).then(() => {

        }).catch(() => {

        });
        return;
    }


    // 是否开架
    await proxy.$modal.confirm('是否确认打开[解锁]编号为"' + row.danghao + '"的档案所在列？具体位置为第'
        + quHao + '区第' + lieHao + '列' + AorBReflect[AorB] + '侧第' + cengHao + '层第' + jieHao + '节。', '系统提示')
        .then(async () => {
            const loadingInstance = proxy.$loading({
                lock: true,
                text: "正在发送相关指令...",
                spinner: "el-icon-loading",
            });
            // 根据 quHao查找ip和端口
            await getInfo(quHao).then(async (response) => {
                const ip = response.data.gdlIp;
                const port = response.data.gdlPort;
                // 获取列状态 判断是否断电休眠
                await getColumnStatus(ip, port)
                    .then(async (response) => {
                        // isPower为0 代表已经休眠 需要关闭休眠
                        const isPower = response['data']['data'][0]['IsPower']
                        if (isPower == 0) {
                            // 休眠中 需要先关闭休眠
                            await cancelSleepColumn(ip, port)
                                .then(async (response) => {
                                    // console.log('休眠关闭返回',response)
                                    if (response.data.data == "1") {
                                        ElMessage({
                                            message: '正在取消休眠...',
                                            type: 'success',
                                            duration: 7000
                                        })
                                        setTimeout(async function () {
                                            // 核心代码逻辑       -----------------start------------------------2025/07/09-----
                                            // 进行是否需要移动的判断，需要移动再进行解锁
                                            const loadingInstance2 = proxy.$loading({
                                                lock: true,
                                                text: "正在获取柜子状态...",
                                                spinner: "el-icon-loading",
                                            });
                                            // 延时操作 1.5s 判断是否需要移动
                                            setTimeout(async () => {
                                                // 进门左手的档案柜
                                                if (quHao == 1 || quHao == 2 || quHao == '1' || quHao == '2') {
                                                    // 看左侧
                                                    if (AorB == 'A') {
                                                        // 获取列状态
                                                        await getColumnStatus(ip, port)
                                                            .then(async (response) => {
                                                                loadingInstance2.close()
                                                                const loadingInstance3 = proxy.$loading({
                                                                    lock: true,
                                                                    text: "正在发送移动指令...",
                                                                    spinner: "el-icon-loading",
                                                                });
                                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                                // 第一列状态是0，表示两边均未到位；
                                                                // 第二列状态是2，表示只有右边到位；
                                                                // 第三列状态是3，表示两边均到位；
                                                                // 第四列状态是1表示只有左边到位。

                                                                // 新增状态获取延迟操作 800ms
                                                                setTimeout(async ()=> {
                                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                                    console.log('列状态', ColumnStatus)
                                                                    console.log('第' + lieHao + '列状态', ColumnStatus[lieHao - 1])
                                                                    // 判断ColumnStatus中第lieHao的数值
                                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 1 || ColumnStatus[lieHao - 1] == 3) {
                                                                        // 列均未到位 || 列右边未到位 || 都到位
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        await rightMoveColumn(ip, port, lieHao)
                                                                                                            .then(response => {
                                                                                                                console.log('右移1成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    console.log('右移1成功2', response)
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                    // loadingV.value = true;
                                                                                                                }
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('右移1失败', response)
                                                                                                                // ElMessage({
                                                                                                                //   message: '右移操作失败',
                                                                                                                //   type: 'error'
                                                                                                                // })
                                                                                                            });
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })
                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 2) {
                                                                        loadingInstance3.close()
                                                                        //  只有右边到位，说明左边未到位，本来就是查看左边，所以不用管
                                                                        ElMessage({
                                                                            message: '档案柜已到位',
                                                                            type: 'success',
                                                                            duration: 2500
                                                                        })
                                                                        console.log('只有右边到位，说明左边未到位，本来就是查看左边，所以不用管')
                                                                    }
                                                                },800)
                                                            })
                                                            .catch(error => {
                                                                loadingInstance2.close()
                                                                console.log('获取列状态失败')
                                                            });
                                                    }
                                                    // 看右侧
                                                    else if (AorB == 'B') {
                                                        await getColumnStatus(ip, port)
                                                            .then(async response => {
                                                                loadingInstance2.close()
                                                                const loadingInstance3 = proxy.$loading({
                                                                    lock: true,
                                                                    text: "正在发送移动指令...",
                                                                    spinner: "el-icon-loading",
                                                                });
                                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                                // 第一列状态是0，表示两边均未到位；
                                                                // 第二列状态是2，表示只有右边到位；
                                                                // 第三列状态是3，表示两边均到位；
                                                                // 第四列状态是1表示只有左边到位。

                                                                setTimeout(async ()=> {
                                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                                    // 判断ColumnStatus中第lieHao的数值
                                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 2) {
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        await leftMoveColumn(ip, port, lieHao)
                                                                                                            .then(response => {
                                                                                                                console.log('左移1成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                    // loadingV.value = true;
                                                                                                                }
                                                                                                                console.log(response);
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('左移1失败', response)
                                                                                                                // ElMessage({
                                                                                                                //   message: '左移操作失败',
                                                                                                                //   type: 'error'
                                                                                                                // })
                                                                                                            })
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })
                                                                        // 列均未到位 || 列左边不到位
                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 3) {
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        rightMoveColumn(ip, port, parseInt(lieHao) + 1)
                                                                                                            .then(response => {
                                                                                                                console.log('右移2成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                    // loadingV.value = true;
                                                                                                                }
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('右移2失败', response)
                                                                                                                // ElMessage({
                                                                                                                //   message: '右移操作失败',
                                                                                                                //   type: 'error'
                                                                                                                // })
                                                                                                            });
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })
                                                                        // 3两侧均到位      则2+1号柜子右移

                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 1) {
                                                                        // 1左侧到位 就是右侧不到位
                                                                        // 不用管
                                                                        loadingInstance3.close()
                                                                        ElMessage({
                                                                            message: '档案柜已到位',
                                                                            type: 'success',
                                                                            duration: 2500
                                                                        })
                                                                    }
                                                                },800)

                                                            })
                                                            .catch(error => {
                                                                loadingInstance2.close()
                                                                console.log('获取列状态失败')
                                                            });
                                                    }
                                                }
                                                // 进门右手的档案柜
                                                else if (quHao == 3 || quHao == 4 || quHao == '3' || quHao == '4') {
                                                    if (AorB == 'A') {
                                                        // 获取列状态
                                                        await getColumnStatus(ip, port)
                                                            .then(async (response) => {
                                                                loadingInstance2.close()
                                                                const loadingInstance3 = proxy.$loading({
                                                                    lock: true,
                                                                    text: "正在发送移动指令...",
                                                                    spinner: "el-icon-loading",
                                                                });
                                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                                // 第一列状态是0，表示两边均未到位；
                                                                // 第二列状态是2，表示只有右边到位；
                                                                // 第三列状态是3，表示两边均到位；
                                                                // 第四列状态是1表示只有左边到位。
                                                                setTimeout(async ()=> {
                                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                                    // 判断ColumnStatus中第lieHao的数值
                                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 1) {
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        await rightMoveColumn(ip, port, lieHao)
                                                                                                            .then(response => {
                                                                                                                console.log('右移3成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    loadingInstance3.close()
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                }
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('右移3失败', response)
                                                                                                            });
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })
                                                                        // 列均未到位 || 列右边未到位

                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 3) {
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        await leftMoveColumn(ip, port, parseInt(lieHao) + 1)
                                                                                                            .then(response => {
                                                                                                                console.log('左移2成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                }
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('左移2失败', response)
                                                                                                                // ElMessage({
                                                                                                                //   message: '左移操作失败',
                                                                                                                //   type: 'error'
                                                                                                                // })
                                                                                                            });
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })

                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 2) {
                                                                        // 不用管
                                                                        console.log('')
                                                                        ElMessage({
                                                                            message: '档案柜已到位',
                                                                            type: 'success',
                                                                            duration: 2500
                                                                        })
                                                                    }
                                                                },800)

                                                            })
                                                            .catch(error => {
                                                                loadingInstance2.close()
                                                                console.log('获取列状态失败')
                                                            });
                                                    }
                                                    else if (AorB == 'B') {
                                                        await getColumnStatus(ip, port)
                                                            .then(async (response) => {
                                                                loadingInstance2.close()
                                                                const loadingInstance3 = proxy.$loading({
                                                                    lock: true,
                                                                    text: "正在发送移动指令...",
                                                                    spinner: "el-icon-loading",
                                                                });
                                                                setTimeout(async ()=> {
                                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 2 || ColumnStatus[lieHao - 1] == 3) {
                                                                        await unlockColumn(ip, port).  //原
                                                                            then(async (response) => {
                                                                                // 解锁成功之后的操作
                                                                                if (response.data.data == "1") { //原
                                                                                    ElMessage({
                                                                                        message: '列解锁成功...正在检测当前状态',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await getColumnStatus(ip, port)
                                                                                            .then(async (response) => {
                                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                                // 判断错误码
                                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                                if (isErr != false) {
                                                                                                    ElMessageBox.confirm(
                                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                                        '系统警告',
                                                                                                        {
                                                                                                            confirmButtonText: '确定',
                                                                                                            cancelButtonText: '取消',
                                                                                                            type: 'error',
                                                                                                        }
                                                                                                    ).then(() => {

                                                                                                    }).catch(() => {

                                                                                                    });
                                                                                                    return
                                                                                                }
                                                                                                else {
                                                                                                    ElMessage({
                                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                                        type: 'success',
                                                                                                        duration: 1500
                                                                                                    })
                                                                                                    setTimeout(async () => {
                                                                                                        await leftMoveColumn(ip, port, lieHao)
                                                                                                            .then(response => {
                                                                                                                console.log('左移3成功', response)
                                                                                                                loadingInstance3.close()
                                                                                                                if (response.data.data == "1") {
                                                                                                                    ElMessage({
                                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                                        type: 'success',
                                                                                                                        duration: 2500
                                                                                                                    })
                                                                                                                }
                                                                                                            })
                                                                                                            .catch(error => {
                                                                                                                loadingInstance3.close()
                                                                                                                console.log('左移3失败', response)

                                                                                                                // ElMessage({
                                                                                                                //   message: '左移操作失败',
                                                                                                                //   type: 'error'
                                                                                                                // })
                                                                                                            });
                                                                                                    },1500)
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                                console.log(error)
                                                                                            })
                                                                                    },3500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                ElMessage({
                                                                                    message: '解锁失败',
                                                                                    type: 'error',
                                                                                    duration: 5000
                                                                                })
                                                                            })
                                                                    }
                                                                    else if (ColumnStatus[lieHao - 1] == 1) {
                                                                        loadingInstance3.close()
                                                                        console.log('不用管')
                                                                        ElMessage({
                                                                            message: '档案柜已到位',
                                                                            type: 'success',
                                                                            duration: 2500
                                                                        })
                                                                    }
                                                                },800)
                                                            })
                                                            .catch(error => {
                                                                loadingInstance2.close()
                                                            });
                                                    }
                                                }
                                            }, 1500);
                                            // 核心代码逻辑      -----------------end------------------------2025/07/09-----
                                        },7000)
                                    }
                                })
                        }
                        else {
                            loadingInstance.close()
                            // 核心代码逻辑       -----------------start------------------------2025/07/09-----
                            // 进行是否需要移动的判断，需要移动再进行解锁
                            const loadingInstance2 = proxy.$loading({
                                lock: true,
                                text: "正在获取柜子状态...",
                                spinner: "el-icon-loading",
                            });
                            // 延时操作 1.5s 判断是否需要移动
                            setTimeout(async () => {
                                // 进门左手的档案柜
                                if (quHao == 1 || quHao == 2 || quHao == '1' || quHao == '2') {
                                    // 看左侧
                                    if (AorB == 'A') {
                                        // 获取列状态
                                        await getColumnStatus(ip, port)
                                            .then(async (response) => {
                                                loadingInstance2.close()
                                                const loadingInstance3 = proxy.$loading({
                                                    lock: true,
                                                    text: "正在发送移动指令...",
                                                    spinner: "el-icon-loading",
                                                });
                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                // 第一列状态是0，表示两边均未到位；
                                                // 第二列状态是2，表示只有右边到位；
                                                // 第三列状态是3，表示两边均到位；
                                                // 第四列状态是1表示只有左边到位。

                                                // 新增状态获取延迟操作 800ms
                                                setTimeout(async ()=> {
                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                    console.log('列状态', ColumnStatus)
                                                    console.log('第' + lieHao + '列状态', ColumnStatus[lieHao - 1])
                                                    // 判断ColumnStatus中第lieHao的数值
                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 1 || ColumnStatus[lieHao - 1] == 3) {
                                                        // 列均未到位 || 列右边未到位 || 都到位
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await rightMoveColumn(ip, port, lieHao)
                                                                                            .then(response => {
                                                                                                console.log('右移1成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    console.log('右移1成功2', response)
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                    // loadingV.value = true;
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('右移1失败', response)
                                                                                                // ElMessage({
                                                                                                //   message: '右移操作失败',
                                                                                                //   type: 'error'
                                                                                                // })
                                                                                            });
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })
                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 2) {
                                                        loadingInstance3.close()
                                                        //  只有右边到位，说明左边未到位，本来就是查看左边，所以不用管
                                                        ElMessage({
                                                            message: '档案柜已到位',
                                                            type: 'success',
                                                            duration: 2500
                                                        })
                                                        console.log('只有右边到位，说明左边未到位，本来就是查看左边，所以不用管')
                                                    }
                                                },800)
                                            })
                                            .catch(error => {
                                                loadingInstance2.close()
                                                console.log('获取列状态失败')
                                            });
                                    }
                                    // 看右侧
                                    else if (AorB == 'B') {
                                        await getColumnStatus(ip, port)
                                            .then(async response => {
                                                loadingInstance2.close()
                                                const loadingInstance3 = proxy.$loading({
                                                    lock: true,
                                                    text: "正在发送移动指令...",
                                                    spinner: "el-icon-loading",
                                                });
                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                // 第一列状态是0，表示两边均未到位；
                                                // 第二列状态是2，表示只有右边到位；
                                                // 第三列状态是3，表示两边均到位；
                                                // 第四列状态是1表示只有左边到位。

                                                setTimeout(async ()=> {
                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                    // 判断ColumnStatus中第lieHao的数值
                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 2) {
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await leftMoveColumn(ip, port, lieHao)
                                                                                            .then(response => {
                                                                                                console.log('左移1成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                    // loadingV.value = true;
                                                                                                }
                                                                                                console.log(response);
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('左移1失败', response)
                                                                                                // ElMessage({
                                                                                                //   message: '左移操作失败',
                                                                                                //   type: 'error'
                                                                                                // })
                                                                                            })
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })
                                                        // 列均未到位 || 列左边不到位
                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 3) {
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        rightMoveColumn(ip, port, parseInt(lieHao) + 1)
                                                                                            .then(response => {
                                                                                                console.log('右移2成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                    // loadingV.value = true;
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('右移2失败', response)
                                                                                                // ElMessage({
                                                                                                //   message: '右移操作失败',
                                                                                                //   type: 'error'
                                                                                                // })
                                                                                            });
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })
                                                        // 3两侧均到位      则2+1号柜子右移

                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 1) {
                                                        // 1左侧到位 就是右侧不到位
                                                        // 不用管
                                                        loadingInstance3.close()
                                                        ElMessage({
                                                            message: '档案柜已到位',
                                                            type: 'success',
                                                            duration: 2500
                                                        })
                                                    }
                                                },800)

                                            })
                                            .catch(error => {
                                                loadingInstance2.close()
                                                console.log('获取列状态失败')
                                            });
                                    }
                                }
                                // 进门右手的档案柜
                                else if (quHao == 3 || quHao == 4 || quHao == '3' || quHao == '4') {
                                    if (AorB == 'A') {
                                        // 获取列状态
                                        await getColumnStatus(ip, port)
                                            .then(async (response) => {
                                                loadingInstance2.close()
                                                const loadingInstance3 = proxy.$loading({
                                                    lock: true,
                                                    text: "正在发送移动指令...",
                                                    spinner: "el-icon-loading",
                                                });
                                                // 列到位状态字符串：0231，说明该组密集架共有4列，
                                                // 第一列状态是0，表示两边均未到位；
                                                // 第二列状态是2，表示只有右边到位；
                                                // 第三列状态是3，表示两边均到位；
                                                // 第四列状态是1表示只有左边到位。
                                                setTimeout(async ()=> {
                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                    // 判断ColumnStatus中第lieHao的数值
                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 1) {
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await rightMoveColumn(ip, port, lieHao)
                                                                                            .then(response => {
                                                                                                console.log('右移3成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    loadingInstance3.close()
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('右移3失败', response)
                                                                                            });
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })
                                                        // 列均未到位 || 列右边未到位

                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 3) {
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await leftMoveColumn(ip, port, parseInt(lieHao) + 1)
                                                                                            .then(response => {
                                                                                                console.log('左移2成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('左移2失败', response)
                                                                                                // ElMessage({
                                                                                                //   message: '左移操作失败',
                                                                                                //   type: 'error'
                                                                                                // })
                                                                                            });
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })

                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 2) {
                                                        // 不用管
                                                        console.log('')
                                                        ElMessage({
                                                            message: '档案柜已到位',
                                                            type: 'success',
                                                            duration: 2500
                                                        })
                                                    }
                                                },800)

                                            })
                                            .catch(error => {
                                                loadingInstance2.close()
                                                console.log('获取列状态失败')
                                            });
                                    }
                                    else if (AorB == 'B') {
                                        await getColumnStatus(ip, port)
                                            .then(async (response) => {
                                                loadingInstance2.close()
                                                const loadingInstance3 = proxy.$loading({
                                                    lock: true,
                                                    text: "正在发送移动指令...",
                                                    spinner: "el-icon-loading",
                                                });
                                                setTimeout(async ()=> {
                                                    const ColumnStatus = response['data']['data'][0]['ColumnStatus']
                                                    if (ColumnStatus[lieHao - 1] == 0 || ColumnStatus[lieHao - 1] == 2 || ColumnStatus[lieHao - 1] == 3) {
                                                        await unlockColumn(ip, port).  //原
                                                            then(async (response) => {
                                                                // 解锁成功之后的操作
                                                                if (response.data.data == "1") { //原
                                                                    ElMessage({
                                                                        message: '列解锁成功...正在检测当前状态',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        await getColumnStatus(ip, port)
                                                                            .then(async (response) => {
                                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                                console.log('当前状态码:', MJJZTLX)
                                                                                console.log('当前状态为:', MJJZTLXName)
                                                                                // 判断错误码
                                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                                if (isErr != false) {
                                                                                    ElMessageBox.confirm(
                                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                                        '系统警告',
                                                                                        {
                                                                                            confirmButtonText: '确定',
                                                                                            cancelButtonText: '取消',
                                                                                            type: 'error',
                                                                                        }
                                                                                    ).then(() => {

                                                                                    }).catch(() => {

                                                                                    });
                                                                                    return
                                                                                }
                                                                                else {
                                                                                    ElMessage({
                                                                                        message: '状态正常...发送移动指令...请耐心等待',
                                                                                        type: 'success',
                                                                                        duration: 1500
                                                                                    })
                                                                                    setTimeout(async () => {
                                                                                        await leftMoveColumn(ip, port, lieHao)
                                                                                            .then(response => {
                                                                                                console.log('左移3成功', response)
                                                                                                loadingInstance3.close()
                                                                                                if (response.data.data == "1") {
                                                                                                    ElMessage({
                                                                                                        message: '正在移动柜子，请耐心等待，或观看监控状态',
                                                                                                        type: 'success',
                                                                                                        duration: 2500
                                                                                                    })
                                                                                                }
                                                                                            })
                                                                                            .catch(error => {
                                                                                                loadingInstance3.close()
                                                                                                console.log('左移3失败', response)

                                                                                                // ElMessage({
                                                                                                //   message: '左移操作失败',
                                                                                                //   type: 'error'
                                                                                                // })
                                                                                            });
                                                                                    },1500)
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log('解锁休眠之后二次判断状态(移动)', response)
                                                                                console.log(error)
                                                                            })
                                                                    },3500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                ElMessage({
                                                                    message: '解锁失败',
                                                                    type: 'error',
                                                                    duration: 5000
                                                                })
                                                            })
                                                    }
                                                    else if (ColumnStatus[lieHao - 1] == 1) {
                                                        loadingInstance3.close()
                                                        console.log('不用管')
                                                        ElMessage({
                                                            message: '档案柜已到位',
                                                            type: 'success',
                                                            duration: 2500
                                                        })
                                                    }
                                                },800)
                                            })
                                            .catch(error => {
                                                loadingInstance2.close()
                                            });
                                    }
                                }
                            }, 1500);
                            // 核心代码逻辑      -----------------end------------------------2025/07/09-----
                        }
                    })
            });
        })
        .catch(() => {
        });
}


// 合并架子
export async function handleCombineMeth(row,proxy) {
    if (!row.shitiLocation || row.shitiLocation == '') {
        ElMessage.error('档案位置信息不完整，请检查档案的实体位置信息');
        return;
    }
    // 获取区号
    const quHao = row.shitiLocation.split('-')[0]
    // 根据区号查询ip+port
    // 根据 quHao查找ip和端口
    await getInfo(quHao).then(async (response) => {
        const ip = response.data.gdlIp;
        const port = response.data.gdlPort;
        await proxy.$modal.confirm('是否解锁柜体并执行合架操作?目前为第' + quHao + '区', '系统提示')
            .then(async () => {
            // 获取列状态 判断是否断电休眠
            await getColumnStatus(ip, port)
                .then(async (response) => {
                    // isPower为0 代表已经休眠 需要关闭休眠
                    const isPower = response['data']['data'][0]['IsPower']

                    if (isPower == 0) {
                        // 休眠中 需要先关闭休眠
                        await cancelSleepColumn(ip, port)
                            .then(async (response) => {
                                // console.log('休眠关闭返回',response)
                                if (response.data.data == "1") {
                                    ElMessage({
                                        message: '正在关闭休眠状态...  请稍等',
                                        type: 'success',
                                        duration: 7000
                                    })
                                    setTimeout(async () => {
                                        // 先进行解锁操作，后进行错误码判断
                                        await unlockColumn(ip, port).  //原
                                            then(async (response) => {
                                                // console.log('解锁返回',response)
                                                if (response.data.data == "1") { //原
                                                    ElMessage({
                                                        message: '列解锁成功...正在检测当前状态',
                                                        type: 'success',
                                                        duration: 3500
                                                    })
                                                    setTimeout(async () => {
                                                        await getColumnStatus(ip, port)
                                                            .then(async (response) => {
                                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                                console.log('当前状态码:', MJJZTLX)
                                                                console.log('当前状态为:', MJJZTLXName)
                                                                // 判断错误码
                                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                                if (isErr != false) {
                                                                    ElMessageBox.confirm(
                                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                                        '系统警告',
                                                                        {
                                                                            confirmButtonText: '确定',
                                                                            cancelButtonText: '取消',
                                                                            type: 'error',
                                                                        }
                                                                    ).then(() => {

                                                                    }).catch(() => {

                                                                    });
                                                                    return
                                                                }
                                                                else {
                                                                    ElMessage({
                                                                        message: '状态正常...发送合架指令...请耐心等待',
                                                                        type: 'success',
                                                                        duration: 1500
                                                                    })
                                                                    setTimeout(async () => {
                                                                        resetColumn(ip, port)
                                                                            .then(response => {
                                                                                let data = response.data.data
                                                                                if (data == "1") {
                                                                                    ElMessage({
                                                                                        message: '第' + quHao + '区合架指令发送成功,请耐心等待合架完成.',
                                                                                        type: 'success',
                                                                                        duration: 2000
                                                                                    })
                                                                                }
                                                                            })
                                                                            .catch(error => {
                                                                                console.log(error)
                                                                            })
                                                                    },1500)
                                                                }
                                                            })
                                                            .catch(error => {
                                                                console.log('解锁休眠之后二次判断状态', response)
                                                                console.log(error)
                                                            })
                                                    },3500)
                                                }
                                            })
                                            .catch(error => {
                                                console.log('解锁失败', response)
                                            })
                                    },7000)
                                }
                            })
                    }
                    else {
                        // 先进行解锁操作，后进行错误码判断
                        await unlockColumn(ip, port).  //原
                            then(async (response) => {
                                // console.log('解锁返回',response)
                                // 解锁成功之后的操作
                                if (response.data.data == "1") { //原
                                    ElMessage({
                                        message: '列解锁成功...正在检测当前状态',
                                        type: 'success',
                                        duration: 3500
                                    })
                                    setTimeout(async () => {
                                        await getColumnStatus(ip, port)
                                            .then(async (response) => {
                                                const MJJZTLX = response['data']['data'][0]['MJJZTLX']
                                                const MJJZTLXName = response['data']['data'][0]['MJJZTLXName']
                                                console.log('当前状态码:', MJJZTLX)
                                                console.log('当前状态为:', MJJZTLXName)
                                                // 判断错误码
                                                let isErr = JudgeIsErrorZt(MJJZTLX)
                                                if (isErr != false) {
                                                    ElMessageBox.confirm(
                                                        '列状态异常[__'+MJJZTLXName+'__],请等待5-10s之后再次重试操作。若重试仍无效，请检查柜体间是否有遮挡物,清理遮挡物之后重试,',
                                                        '系统警告',
                                                        {
                                                            confirmButtonText: '确定',
                                                            cancelButtonText: '取消',
                                                            type: 'error',
                                                        }
                                                    ).then(() => {

                                                    }).catch(() => {

                                                    });
                                                    return
                                                }
                                                else {
                                                    ElMessage({
                                                        message: '状态正常...发送合架指令...请耐心等待',
                                                        type: 'success',
                                                        duration: 3500
                                                    })
                                                    setTimeout(async () => {
                                                        resetColumn(ip, port)
                                                            .then(response => {
                                                                let data = response.data.data
                                                                if (data == "1") {
                                                                    ElMessage({
                                                                        message: '第' + quHao + '区合架指令发送成功,请耐心等待合架完成.',
                                                                        type: 'success',
                                                                        duration: 2000
                                                                    })
                                                                }
                                                            })
                                                            .catch(error => {
                                                                console.log(error)
                                                            })
                                                    },3500)
                                                }
                                            })
                                            .catch(error => {
                                                console.log('解锁休眠之后二次判断状态', response)
                                                console.log(error)
                                            })
                                    },3500)
                                }
                            })
                            .catch(error => {
                                console.log('解锁失败', response)
                            })
                    }
                })
        })
    })
}
