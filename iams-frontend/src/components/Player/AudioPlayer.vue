<template>
    <div class="audio-container">
      <audio 
        :src="src"
        controls
        class="audio-element"
      >
        您的浏览器不支持音频播放
      </audio>
    </div>
  </template>
  
  <script setup>
  defineProps({
    src: String
  })
  </script>
  
  <style scoped>
  .audio-container {
    width: 100%;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  }
  
  .audio-element {
    width: 100%;
  }
  </style>