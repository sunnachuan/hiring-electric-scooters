<template>
  <footer class="page-footer">
    <!-- 顶部渐变边框 -->
    <div class="footer-top-border"></div>
    
    <div class="footer-content">
      <div class="footer-section">
        <h4>联系我们</h4>
        <p>客服热线：400-123-4567</p>
        <p>邮箱：service@scooter-rental.com</p>
        <p>地址：XX市XX区XX街道123号</p>
      </div>
      
      <div class="footer-section">
        <h4>关于我们</h4>
        <p>电动滑板车租赁服务</p>
        <p>专业、安全、便捷</p>
        <p>让出行更轻松</p>
      </div>
      
      <div class="footer-section">
        <h4>法律声明</h4>
        <p>© 2024 电动滑板车租赁系统</p>
        <p>版权所有</p>
        <p>隐私政策 | 使用条款</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 不再需要图标导入
</script>

<style scoped>
.page-footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  margin-top: 0;
  padding: 0;
  color: #fff;
  position: relative;
}

.footer-top-border {
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #667eea 100%);
  margin: 0;
}

.footer-content {
  display: flex;
  justify-content: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 200px 20px 200px;
  gap: 60px;
  align-items: flex-start;
}

.footer-section {
  flex: 1;
  text-align: center;
  min-width: 0;
  max-width: 280px;
}

.footer-section h4 {
  color: #ecf0f1;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.footer-section p {
  color: #bdc3c7;
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  transition: color 0.2s ease;
}

.footer-section p:hover {
  color: #ecf0f1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .footer-content {
    padding: 32px 100px 24px 100px;
    gap: 40px;
  }
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: row !important;
    flex-wrap: wrap !important;
    gap: 2px !important;
    padding: 16px 8px 12px 8px !important;
    justify-content: space-between !important;
  }
  
  .footer-section {
    flex: 0 0 calc(33.333% - 2px) !important;
    max-width: calc(33.333% - 2px) !important;
    text-align: center !important;
    min-width: 100px !important;
  }
  
  .footer-section h4 {
    margin-bottom: 6px;
    font-size: 11px;
    font-weight: 500;
    letter-spacing: 0.3px;
  }
  
  .footer-section p {
    margin: 2px 0;
    font-size: 9px;
    line-height: 1.2;
  }
}

@media (max-width: 600px) {
  .footer-content {
    gap: 1px !important;
    padding: 12px 4px 8px 4px !important;
  }
  
  .footer-section {
    flex: 0 0 calc(50% - 1px) !important;
    max-width: calc(50% - 1px) !important;
    min-width: 80px !important;
  }
  
  .footer-section h4 {
    font-size: 10px;
    margin-bottom: 4px;
  }
  
  .footer-section p {
    font-size: 8px;
    margin: 1px 0;
  }
}

@media (max-width: 400px) {
  .footer-content {
    flex-direction: row !important;
    gap: 0px !important;
    padding: 10px 2px 6px 2px !important;
  }
  
  .footer-section {
    flex: 0 0 calc(33.333% - 0px) !important;
    max-width: calc(33.333% - 0px) !important;
    min-width: 70px !important;
  }
  
  .footer-section h4 {
    font-size: 9px;
    margin-bottom: 3px;
  }
  
  .footer-section p {
    font-size: 7px;
    margin: 1px 0;
    line-height: 1.1;
  }
}

@media (max-width: 320px) {
  .footer-content {
    flex-direction: column;
    gap: 8px;
    padding: 8px 4px 4px 4px;
  }
  
  .footer-section {
    flex: 1 1 100%;
    max-width: 100%;
    min-width: auto;
  }
  
  .footer-section h4 {
    font-size: 10px;
    margin-bottom: 4px;
  }
  
  .footer-section p {
    font-size: 8px;
    margin: 2px 0;
    line-height: 1.2;
  }
}
</style>