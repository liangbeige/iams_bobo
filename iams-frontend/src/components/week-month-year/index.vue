<template>
    <div class="week-month-year">
        <div
            v-for="(item, index) in filteredGroupList"
            :key="index"
            :class="['item', { 'is-checked': checkedIndex === index }]"
            @click="handleChange(index)"
        >
            {{ item.label }}
        </div>
    </div>
</template>




<script setup>
import { ref, computed } from 'vue';

// 定义 props
const props = defineProps({
    hideYear: {
        type: Boolean,
        default: false
    },
    hideWeek: {
        type: Boolean,
        default: false
    },
    showDay: {
        type: Boolean,
        default: false
    }
});

// 定义事件
const emit = defineEmits(['handleChange']);

// 原始选项列表
const groupList = ref([
    { label: '日', value: 'day' },
    { label: '周', value: 'week' },
    { label: '月', value: 'month' },
    { label: '年', value: 'year' },
]);

// 计算属性过滤掉不需要的项
const filteredGroupList = computed(() => {
    return groupList.value.filter(item => {
        if (item.value === 'year' && props.hideYear) return false;
        if (item.value === 'week' && props.hideWeek) return false;
        if (item.value === 'day' && !props.showDay) return false;
        return true;
    });
});

const checkedIndex = ref(0); // 默认选中第一个

// 处理切换事件
const handleChange = (index) => {
    if (checkedIndex.value === index) return;
    checkedIndex.value = index;
    emit('handleChange', filteredGroupList.value[index].value);
};
</script>


<style lang="scss" scoped>
.week-month-year {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 129px;
  height: 34px;
  padding: 0 5px; // 增加内边距
  background: rgba(233, 243, 255, 0.37);
  border-radius: 10px;

  .item {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 25px;
    font-size: 14px;
    color: #9ca3b4;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      color: var(--color-primary);
    }
  }

  .is-checked {
    background: #ffffff;
    box-shadow: 0px 0px 4px rgba(0, 0, 0, 0.11);
    border-radius: 7px;
    font-weight: 600;
    color: var(--color-primary);
  }
}
</style>