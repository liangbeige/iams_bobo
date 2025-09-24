<template>
  <div class="loading-container">
    <div class="loading-spinner"></div>
    <div class="loading-text">正在登录<span class="dotting"></span></div>
  </div>
</template>

<script setup>
// 原有脚本部分保持不变
import {ref, onMounted, computed} from "vue";
import useUserStore from '@/store/modules/user'
import {useRoute, useRouter} from "vue-router";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();

function ssoLogin() {
  // 从hash中解析access_token参数
  const hash = window.location.hash;
  const token = getTokenFromHash(hash);

  console.log(token, '=========================================');

  if (token) {
    userStore.LoginIAMSHaveToken(token).then(() => {
      router.push({path: "/",});
    }).catch(() => {
      // 处理登录失败的情况
    });
  } else {
    console.error('未找到access_token');
  }
}

// 辅助函数：从hash中提取access_token
function getTokenFromHash(hash) {
  // 移除开头的#号
  const hashWithoutSymbol = hash.startsWith('#') ? hash.substring(1) : hash;

  // 分割hash部分，处理可能的多个参数
  const params = hashWithoutSymbol.split('&');

  for (let param of params) {
    // 处理参数可能包含空格的情况
    if (param.includes('access token=') || param.includes('access_token=')) {
      // 提取token值
      const tokenValue = param.split('=')[1];
      return tokenValue;
    }
  }

  return null;
}

onMounted(() => {
  ssoLogin();
})
</script>

<style scoped lang="scss">
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: rgba(255, 255, 255, 0.9);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.loading-text {
  font-size: 18px;
  color: #606266;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;

  .dotting {
    display: inline-block;
    min-width: 2px;
    min-height: 2px;
    box-shadow: 2px 0 currentColor, 6px 0 currentColor, 10px 0 currentColor;
    animation: dot 1.5s infinite step-start both;
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dot {
  25% {
    box-shadow: none;
  }
  /* 0个点 */
  50% {
    box-shadow: 2px 0 currentColor;
  }
  /* 1个点 */
  75% {
    box-shadow: 2px 0 currentColor, 6px 0 currentColor;
  }
  /* 2个点 */
}
</style>