import axios from 'axios';

// mock适配器
import MockAdapter from 'axios-mock-adapter';

// const mock = new MockAdapter(axios);
// mock.onGet('/api/endpoint').reply(200, { data: '1' });
//
// export function testAxios() {
//   return axios({
//     url: '/api/endpoint',
//     method: 'get'
//   });
// }
// 锁定
export function lockColumn(ip, port) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=Lock`,
    method: 'get'
  });
}

// 解锁
export function unlockColumn(ip, port) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=UnLock`,
    method: 'get'
  });
}

// 停止移动
export function stopMoveColumn(ip, port) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=StopMove`,
    method: 'get'
  });
}

// 复位
export function resetColumn(ip, port) {
  console.log(`http://${ip}:${port}/MjjWebApi?Op=Reset`)
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=Reset`,
    method: 'get'
  });
}

// 左移（需要返回 ColumnNo）
export function leftMoveColumn(ip, port, columnNumber) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=LeftMove&ColumnNo=${columnNumber}`,
    method: 'get'
  })
  //     .then(res => {
  //   return {
  //     response: res.data,
  //     columnNo: columnNumber
  //   };
  // });
}

// 右移（需要返回 ColumnNo）
export function rightMoveColumn(ip, port, columnNumber) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=RightMove&ColumnNo=${columnNumber}`,
    method: 'get'
  })

  //     .then(res => {
  //   return {
  //     response: res.data,
  //     columnNo: columnNumber
  //   };
  // });
}

// 获取密集架的详细状态
export function getColumnStatus(ip, port) {
  return axios({
    // http://固定列Ip地址:端口/ MjjWebApi?Op= GetMjjStatus
    url: `http://${ip}:${port}/MjjWebApi?Op=GetMjjStatus`,
    // url: `http://${ip}:${port}/MjjWebApi?Op=GetStatus`,
    method: 'get'
  });
}


// 取消休眠
export function cancelSleepColumn(ip, port) {
  return axios({
    url: `http://${ip}:${port}/MjjWebApi?Op=PowerOn_YDL`,
    method: 'get'
  });
}



// [{
//   "QUNO":"区号",
//   "Temp":"温度",
//   "Hum":"湿度",
//   "PM2_5":"PM2.5数据",
//   "PM10":"PM10数据",
//   "TVOC":"TVOC数据",
//   "CO2":"二氧化碳数据",
//   "COLNO":"操作列号",
//   "MJJZTLX":"状态码",
//   "MJJZTLXName":"状态码中文文本",
//   "COLUMNDWZT_CHANGED":"列到位状态是否改变,1:是，0:否",
//   "DATA":"数据，如查询时移动列输入的数据",
//   "IsBJ":"是否报警",
//   "IsLock":"是否锁定",
//   "IsVent":"是否通风中",
//   "IsPower":"是否断电",
//   "IsZDKJ":"是否启用自动开架功能",
//   "ColumnStatus":"列到位状态"
// }]
