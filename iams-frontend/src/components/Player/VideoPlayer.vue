<template>
    <video 
      :src="src"
      :poster="poster"
      controls
      preload="auto"
      class="video-element"
    >
      您的浏览器不支持视频播放
    </video>
  </template>
  
  <script setup>
  defineProps({
    src: String,
    poster: String
  })
  </script>
  
  <style scoped>
  .video-element {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
  </style>