import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'

// export function getToken() {
//   return Cookies.get(TokenKey)
// }
//
// export function setToken(token) {
//   return Cookies.set(TokenKey, token)
// }
// 修改 setToken 和 getToken 函数
export function setToken(token) {
  localStorage.setItem(TokenKey, token);
}

export function getToken() {
  return localStorage.getItem(TokenKey);
}

export function removeToken() {
  return localStorage.removeItem(TokenKey); // 使用 localStorage 删除 token
}
