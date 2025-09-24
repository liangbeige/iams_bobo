// src/utils/cryptoUtils.js

// 加密函数
export const encryptFile = async ({ file, password, iv }) => {
  // 1. 导入密码生成密钥材料
  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(password),
    "PBKDF2",
    false,
    ["deriveKey"]
  );

  // 2. 派生密钥（关键参数与后端一致）
  const key = await crypto.subtle.deriveKey(
    {
      name: "PBKDF2",
      salt: iv,
      iterations: 100000,    // 迭代次数
      hash: "SHA-256"        // 哈希算法
    },
    keyMaterial,
    { name: "AES-GCM", length: 256 }, // AES-256
    true,
    ["encrypt"]
  );

  // 3. 加密文件内容
  const fileBuffer = await file.arrayBuffer();
  const encrypted = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv },
    key,
    fileBuffer
  );

  return new Blob([encrypted]);
};

// 生成随机IV（16字节）
export const generateIV = () => {
  const iv = new Uint8Array(16)
  window.crypto.getRandomValues(iv)
  return iv
};