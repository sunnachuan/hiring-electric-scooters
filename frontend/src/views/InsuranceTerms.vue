<template>
  <div class="insurance-terms-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" type="primary" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回个人中心
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1>保险与条款</h1>
      <el-button type="primary" @click="downloadTerms">
        <el-icon><Download /></el-icon>
        下载条款文档
      </el-button>
    </div>

    <!-- 保险与条款内容 -->
    <div class="content-section">
      <!-- 保险状态 -->
      <el-card class="insurance-card">
        <template #header>
          <div class="card-header">
            <el-icon><Medal /></el-icon>
            <span>当前保险状态</span>
          </div>
        </template>
        <div class="insurance-status">
          <div class="status-item">
            <span class="label">保险类型：</span>
            <span class="value">基础交通意外险</span>
          </div>
          <div class="status-item">
            <span class="label">生效时间：</span>
            <span class="value">{{ userInfo.createTime || '注册时生效' }}</span>
          </div>
          <div class="status-item">
            <span class="label">保障范围：</span>
            <span class="value">第三方人身伤害、财产损失、用户意外伤害</span>
          </div>
          <div class="status-item">
            <span class="label">保险状态：</span>
            <el-tag type="success">有效</el-tag>
          </div>
        </div>
      </el-card>

      <!-- 免责条款 -->
      <el-card class="terms-card">
        <template #header>
          <div class="card-header">
            <el-icon><Warning /></el-icon>
            <span>重要免责条款</span>
          </div>
        </template>
        <div class="terms-list">
          <div class="term-item" v-for="(term, index) in importantTerms" :key="index">
            <el-icon class="term-icon"><InfoFilled /></el-icon>
            <span class="term-text">{{ term }}</span>
          </div>
        </div>
      </el-card>

      <!-- 超时处理说明 -->
      <el-card class="overtime-card">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>超时未还车处理方案</span>
          </div>
        </template>
        <div class="overtime-steps">
          <div class="step-item" v-for="(step, index) in overtimeSteps" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-title">{{ step.title }}</div>
              <div class="step-desc">{{ step.description }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 安全使用指南 -->
      <el-card class="safety-guide-card">
        <template #header>
          <div class="card-header">
            <el-icon><Lock /></el-icon>
            <span>安全使用指南</span>
          </div>
        </template>
        <div class="safety-guide">
          <div class="guide-section" v-for="(guide, index) in safetyGuides" :key="index">
            <h3>{{ guide.title }}</h3>
            <ul>
              <li v-for="(item, itemIndex) in guide.items" :key="itemIndex">{{ item }}</li>
            </ul>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Download, Medal, Warning, Clock, Lock, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 用户信息
const userInfo = reactive({
  createTime: '2024-01-01'
})

// 重要免责条款
const importantTerms = [
  '用户需遵守交通规则，违规使用造成的损失由用户自行承担',
  '酒后驾驶、无证驾驶等违法行为不在保险保障范围内',
  '用户需妥善保管车辆，丢失或被盗需承担相应赔偿责任',
  '故意损坏车辆或违反使用说明造成的损失由用户承担',
  '在禁止骑行区域使用车辆造成的后果由用户自行负责',
  '未成年人使用车辆需有监护人陪同，否则后果自负'
]

// 超时处理步骤
const overtimeSteps = [
  {
    title: '超时提醒',
    description: '用车时间超过预订时间30分钟后，系统将发送超时提醒通知'
  },
  {
    title: '额外计费',
    description: '超时部分将按照正常费率的1.5倍进行计费'
  },
  {
    title: '系统锁定',
    description: '超时2小时后，车辆将自动锁定，无法继续使用'
  },
  {
    title: '客服联系',
    description: '超时4小时后，客服将主动联系用户确认情况'
  },
  {
    title: '信用影响',
    description: '严重超时行为将影响用户的信用评级'
  }
]

// 安全使用指南
const safetyGuides = [
  {
    title: '骑行前检查',
    items: [
      '检查车辆刹车系统是否正常',
      '确认轮胎气压充足',
      '检查车灯和转向灯是否工作',
      '确认电池电量充足'
    ]
  },
  {
    title: '安全骑行',
    items: [
      '佩戴安全头盔和护具',
      '遵守交通信号灯和标志',
      '保持安全车速，避免急刹车',
      '注意观察路况，避让行人'
    ]
  },
  {
    title: '停放规范',
    items: [
      '在指定停车区域停放车辆',
      '避免阻碍交通和行人通行',
      '确保车辆停放稳固',
      '锁好车辆并确认上锁'
    ]
  },
  {
    title: '紧急情况',
    items: [
      '发生事故立即拨打客服电话',
      '保留现场证据和照片',
      '及时联系保险公司',
      '配合相关部门调查处理'
    ]
  }
]

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}

// 下载条款文档
const downloadTerms = () => {
  ElMessage.info('条款文档下载功能开发中')
}

// 初始化数据
onMounted(() => {
  // 可以在这里获取真实的用户信息和保险状态
})
</script>

<style scoped>
.insurance-terms-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.content-section {
  display: grid;
  gap: 24px;
}

.insurance-card,
.terms-card,
.overtime-card,
.safety-guide-card {
  transition: transform 0.2s ease;
}

.insurance-card:hover,
.terms-card:hover,
.overtime-card:hover,
.safety-guide-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.insurance-status {
  display: grid;
  gap: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
}

.status-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #606266;
}

.value {
  color: #303133;
}

.terms-list {
  display: grid;
  gap: 12px;
}

.term-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #e6a23c;
}

.term-icon {
  color: #e6a23c;
  margin-top: 2px;
  flex-shrink: 0;
}

.term-text {
  color: #606266;
  line-height: 1.5;
}

.overtime-steps {
  display: grid;
  gap: 20px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.step-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.step-desc {
  color: #606266;
  line-height: 1.5;
}

.safety-guide {
  display: grid;
  gap: 24px;
}

.guide-section h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.guide-section ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.guide-section li {
  margin-bottom: 8px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .insurance-terms-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .step-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .step-number {
    align-self: flex-start;
  }
}
</style>