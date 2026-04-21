<template>
  <div class="form-builder">
    <!-- 顶部工具栏 -->
    <div class="builder-toolbar">
      <div class="toolbar-left">
        <el-button type="text" @click="undo" :disabled="!canUndo">
          ↩️ 撤销
        </el-button>
        <el-button type="text" @click="redo" :disabled="!canRedo">
          ↪️ 重做
        </el-button>
      </div>
      
      <div class="toolbar-center">
        <el-input 
          v-model="formName" 
          placeholder="表单名称"
          class="form-name-input"
        />
        <el-tag :type="formStatusType" size="small">
          {{ formStatusText }}
        </el-tag>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="preview">
          👁️ 预览
        </el-button>
        <el-button @click="saveForm" type="primary">
          💾 保存
        </el-button>
        <el-button @click="publishForm" type="success">
          🚀 发布
        </el-button>
      </div>
    </div>
    
    <!-- 主体区域 -->
    <div class="builder-main">
      <!-- 左侧组件面板 -->
      <div class="component-panel">
        <div class="panel-header">
          <h4>📦 表单组件</h4>
        </div>
        
        <div class="component-categories">
          <div 
            v-for="category in componentCategories" 
            :key="category.name"
            class="category"
          >
            <div class="category-title" @click="toggleCategory(category.name)">
              <span>{{ category.icon }} {{ category.label }}</span>
              <span>{{ category.expanded ? '▼' : '▶' }}</span>
            </div>
            
            <div v-if="category.expanded" class="component-list">
              <div 
                v-for="component in category.components" 
                :key="component.type"
                class="component-item"
                draggable="true"
                @dragstart="onDragStart($event, component)"
                @click="addComponent(component)"
              >
                <span class="component-icon">{{ component.icon }}</span>
                <span class="component-name">{{ component.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 中间表单画布 -->
      <div class="form-canvas">
        <div class="canvas-header">
          <span>表单画布</span>
          <div class="canvas-actions">
            <el-button type="text" size="small" @click="clearForm">
              🗑️ 清空
            </el-button>
            <el-button type="text" size="small" @click="toggleGrid">
              📐 网格: {{ showGrid ? '开' : '关' }}
            </el-button>
          </div>
        </div>
        
        <div 
          class="canvas-content"
          :class="{ 'show-grid': showGrid }"
          @dragover.prevent
          @drop="onDrop"
          @click="selectWidget(null)"
        >
          <div v-if="widgets.length === 0" class="empty-canvas">
            <div class="empty-icon">📝</div>
            <p>从左侧拖拽组件到此处</p>
            <p class="hint">或点击组件进行添加</p>
          </div>
          
          <div v-else class="widget-list">
            <div 
              v-for="(widget, index) in widgets" 
              :key="widget.id"
              :class="['widget-item', { selected: selectedWidget?.id === widget.id }]"
              @click.stop="selectWidget(widget)"
              @dragover.prevent
              @drop.stop="onWidgetDrop($event, index)"
            >
              <!-- 输入类组件 -->
              <template v-if="isInputType(widget.type)">
                <div class="widget-content">
                  <label class="widget-label">
                    {{ widget.props.label }}
                    <span v-if="widget.props.required" class="required">*</span>
                  </label>
                  
                  <el-input
                    v-if="widget.type === 'input'"
                    :placeholder="widget.props.placeholder"
                    :disabled="widget.props.disabled"
                    size="small"
                  />
                  
                  <el-input
                    v-else-if="widget.type === 'textarea'"
                    type="textarea"
                    :placeholder="widget.props.placeholder"
                    :rows="widget.props.rows || 3"
                    size="small"
                  />
                  
                  <el-input-number
                    v-else-if="widget.type === 'number'"
                    :min="widget.props.min"
                    :max="widget.props.max"
                    size="small"
                  />
                  
                  <el-select
                    v-else-if="widget.type === 'select'"
                    :placeholder="widget.props.placeholder"
                    size="small"
                  >
                    <el-option
                      v-for="opt in widget.props.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                  
                  <el-radio-group v-else-if="widget.type === 'radio'" size="small">
                    <el-radio-button
                      v-for="opt in widget.props.options"
                      :key="opt.value"
                      :label="opt.value"
                    >
                      {{ opt.label }}
                    </el-radio-button>
                  </el-radio-group>
                  
                  <el-checkbox-group v-else-if="widget.type === 'checkbox'" size="small">
                    <el-checkbox-button
                      v-for="opt in widget.props.options"
                      :key="opt.value"
                      :label="opt.value"
                    >
                      {{ opt.label }}
                    </el-checkbox-button>
                  </el-checkbox-group>
                  
                  <el-date-picker
                    v-else-if="widget.type === 'date'"
                    type="date"
                    placeholder="选择日期"
                    size="small"
                  />
                  
                  <el-date-picker
                    v-else-if="widget.type === 'datetime'"
                    type="datetime"
                    placeholder="选择日期时间"
                    size="small"
                  />
                  
                  <div v-else-if="widget.type === 'daterange'" size="small">
                    <el-date-picker type="daterange" range-separator="至" size="small" />
                  </div>
                  
                  <el-time-picker
                    v-else-if="widget.type === 'time'"
                    placeholder="选择时间"
                    size="small"
                  />
                  
                  <div v-else-if="widget.type === 'switch'" size="small">
                    <el-switch />
                  </div>
                  
                  <div v-else-if="widget.type === 'slider'" size="small">
                    <el-slider :min="widget.props.min" :max="widget.props.max" />
                  </div>
                  
                  <div v-else-if="widget.type === 'rate'" size="small">
                    <el-rate />
                  </div>
                  
                  <el-input
                    v-else-if="widget.type === 'email'"
                    type="email"
                    placeholder="请输入邮箱"
                    size="small"
                  />
                  
                  <el-input
                    v-else-if="widget.type === 'phone'"
                    placeholder="请输入手机号"
                    size="small"
                  />
                  
                  <el-input
                    v-else-if="widget.type === 'url'"
                    placeholder="请输入网址"
                    size="small"
                  />
                  
                  <div v-else-if="widget.type === 'color'" size="small">
                    <el-color-picker size="small" />
                  </div>
                  
                  <div v-else-if="widget.type === 'file'" size="small">
                    <el-upload action="#" :limit="widget.props.limit || 1">
                      <el-button size="small" type="primary">
                        点击上传
                      </el-button>
                    </el-upload>
                  </div>
                  
                  <div v-else-if="widget.type === 'image'" size="small">
                    <el-upload
                      action="#"
                      list-type="picture-card"
                      :limit="widget.props.limit || 1"
                    >
                      <i class="el-icon-plus"></i>
                    </el-upload>
                  </div>
                  
                  <!-- 布局组件 -->
                  <div v-else-if="widget.type === 'divider'" class="widget-divider">
                    <el-divider />
                  </div>
                  
                  <div v-else-if="widget.type === 'space'" class="widget-space">
                    <div :style="{ height: widget.props.height + 'px' }"></div>
                  </div>
                  
                  <!-- 信息展示组件 -->
                  <div v-else-if="widget.type === 'text'" class="widget-text">
                    {{ widget.props.content }}
                  </div>
                  
                  <div v-else-if="widget.type === 'html'" class="widget-html">
                    <div v-html="widget.props.content"></div>
                  </div>
                  
                  <!-- 高级组件 -->
                  <div v-else-if="widget.type === 'signature'" class="widget-signature">
                    <el-button size="small">点击签名</el-button>
                  </div>
                  
                  <div v-else-if="widget.type === 'location'" class="widget-location">
                    <el-button size="small">📍 获取位置</el-button>
                  </div>
                  
                  <div v-else-if="widget.type === 'barcode'" class="widget-barcode">
                    <el-button size="small">📊 生成条码</el-button>
                  </div>
                  
                  <!-- 未知组件 -->
                  <div v-else class="widget-unknown">
                    未知组件: {{ widget.type }}
                  </div>
                  
                  <!-- 组件ID -->
                  <div class="widget-id">{{ widget.id }}</div>
                </div>
                
                <!-- 操作按钮 -->
                <div v-if="selectedWidget?.id === widget.id" class="widget-actions">
                  <el-button 
                    type="text" 
                    size="small" 
                    @click.stop="moveUp(index)"
                    :disabled="index === 0"
                  >
                    ⬆️
                  </el-button>
                  <el-button 
                    type="text" 
                    size="small" 
                    @click.stop="moveDown(index)"
                    :disabled="index === widgets.length - 1"
                  >
                    ⬇️
                  </el-button>
                  <el-button 
                    type="text" 
                    size="small" 
                    @click.stop="copyWidget(widget)"
                  >
                    📋
                  </el-button>
                  <el-button 
                    type="text" 
                    size="small" 
                    @click.stop="deleteWidget(index)"
                    class="delete-btn"
                  >
                    🗑️
                  </el-button>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-header">
          <h4>⚙️ 组件属性</h4>
        </div>
        
        <div v-if="selectedWidget" class="property-content">
          <!-- 基础属性 -->
          <div class="property-section">
            <div class="section-title">基础属性</div>
            
            <div class="property-item">
              <label>组件ID</label>
              <el-input :value="selectedWidget.id" disabled size="small" />
            </div>
            
            <div class="property-item">
              <label>标签文本</label>
              <el-input 
                v-model="selectedWidget.props.label" 
                placeholder="标签名称"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>占位提示</label>
              <el-input 
                v-model="selectedWidget.props.placeholder" 
                placeholder="占位文本"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>默认值</label>
              <el-input 
                v-model="selectedWidget.props.defaultValue" 
                placeholder="默认值"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>必填</label>
              <el-switch 
                v-model="selectedWidget.props.required"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>禁用</label>
              <el-switch 
                v-model="selectedWidget.props.disabled"
                @change="updateWidget"
              />
            </div>
          </div>
          
          <!-- 高级属性 -->
          <div v-if="hasAdvancedProps" class="property-section">
            <div class="section-title">高级属性</div>
            
            <div v-if="selectedWidget.type === 'select' || selectedWidget.type === 'radio' || selectedWidget.type === 'checkbox'" class="property-item">
              <label>选项配置</label>
              <div class="options-editor">
                <div 
                  v-for="(opt, idx) in selectedWidget.props.options" 
                  :key="idx"
                  class="option-row"
                >
                  <el-input 
                    v-model="opt.label" 
                    placeholder="标签"
                    size="small"
                    @change="updateWidget"
                  />
                  <el-input 
                    v-model="opt.value" 
                    placeholder="值"
                    size="small"
                    @change="updateWidget"
                  />
                  <el-button 
                    type="text" 
                    size="small"
                    @click="removeOption(idx)"
                    class="delete-btn"
                  >
                    ✕
                  </el-button>
                </div>
                <el-button 
                  type="text" 
                  size="small"
                  @click="addOption"
                >
                  + 添加选项
                </el-button>
              </div>
            </div>
            
            <div v-if="selectedWidget.type === 'number' || selectedWidget.type === 'slider'" class="property-item">
              <label>最小值</label>
              <el-input-number 
                v-model="selectedWidget.props.min" 
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div v-if="selectedWidget.type === 'number' || selectedWidget.type === 'slider'" class="property-item">
              <label>最大值</label>
              <el-input-number 
                v-model="selectedWidget.props.max" 
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div v-if="selectedWidget.type === 'textarea'" class="property-item">
              <label>行数</label>
              <el-input-number 
                v-model="selectedWidget.props.rows" 
                :min="1"
                :max="20"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div v-if="selectedWidget.type === 'file'" class="property-item">
              <label>文件数量限制</label>
              <el-input-number 
                v-model="selectedWidget.props.limit" 
                :min="1"
                :max="10"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div v-if="selectedWidget.type === 'space'" class="property-item">
              <label>间距高度(px)</label>
              <el-input-number 
                v-model="selectedWidget.props.height" 
                :min="0"
                :max="200"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div v-if="selectedWidget.type === 'text' || selectedWidget.type === 'html'" class="property-item">
              <label>内容</label>
              <el-input 
                v-model="selectedWidget.props.content" 
                type="textarea"
                :rows="3"
                placeholder="文本内容"
                size="small"
                @change="updateWidget"
              />
            </div>
          </div>
          
          <!-- 校验规则 -->
          <div class="property-section">
            <div class="section-title">校验规则</div>
            
            <div class="property-item">
              <label>最小长度</label>
              <el-input-number 
                v-model="selectedWidget.props.minLength" 
                :min="0"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>最大长度</label>
              <el-input-number 
                v-model="selectedWidget.props.maxLength" 
                :min="0"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>正则校验</label>
              <el-input 
                v-model="selectedWidget.props.pattern" 
                placeholder="正则表达式"
                size="small"
                @change="updateWidget"
              />
            </div>
          </div>
          
          <!-- 样式属性 -->
          <div class="property-section">
            <div class="section-title">样式</div>
            
            <div class="property-item">
              <label>宽度</label>
              <el-input 
                v-model="selectedWidget.props.width" 
                placeholder="100% 或 200px"
                size="small"
                @change="updateWidget"
              />
            </div>
            
            <div class="property-item">
              <label>CSS类名</label>
              <el-input 
                v-model="selectedWidget.props.className" 
                placeholder="自定义类名"
                size="small"
                @change="updateWidget"
              />
            </div>
          </div>
        </div>
        
        <div v-else class="empty-property">
          <p>👈 请选择组件</p>
          <p class="hint">或在画布中点击组件</p>
        </div>
      </div>
    </div>
    
    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      title="表单预览"
      width="600px"
    >
      <div class="preview-content">
        <h3>{{ formName || '未命名表单' }}</h3>
        <el-form :model="formData" label-width="120px">
          <div v-for="widget in widgets" :key="widget.id" class="preview-widget">
            <el-form-item :label="widget.props.label" :required="widget.props.required">
              <el-input 
                v-if="widget.type === 'input'"
                v-model="formData[widget.id]"
                :placeholder="widget.props.placeholder"
              />
              <el-input 
                v-else-if="widget.type === 'textarea'"
                v-model="formData[widget.id]"
                type="textarea"
                :rows="widget.props.rows || 3"
              />
              <component 
                v-else
                :is="getComponent(widget.type)"
                v-model="formData[widget.id]"
              />
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 类型定义
interface Widget {
  id: string
  type: string
  props: Record<string, any>
}

interface Component {
  type: string
  label: string
  icon: string
  category: string
  defaultProps: Record<string, any>
}

interface Category {
  name: string
  label: string
  icon: string
  expanded: boolean
  components: Component[]
}

// 状态
const formName = ref('')
const widgets = ref<Widget[]>([])
const selectedWidget = ref<Widget | null>(null)
const showGrid = ref(true)
const previewVisible = ref(false)
const formData = reactive<Record<string, any>>({})

// 历史记录
const history = ref<Widget[][]>([])
const historyIndex = ref(-1)

// 组件分类
const componentCategories = ref<Category[]>([
  {
    name: 'input',
    label: '输入框',
    icon: '⌨️',
    expanded: true,
    components: [
      { type: 'input', label: '文本框', icon: '📝', category: 'input', defaultProps: { label: '文本框', placeholder: '请输入', required: false, disabled: false } },
      { type: 'textarea', label: '多行文本', icon: '📄', category: 'input', defaultProps: { label: '多行文本', placeholder: '请输入', rows: 3, required: false } },
      { type: 'number', label: '数字', icon: '#️⃣', category: 'input', defaultProps: { label: '数字', min: 0, max: 100, required: false } },
      { type: 'email', label: '邮箱', icon: '📧', category: 'input', defaultProps: { label: '邮箱', placeholder: '请输入邮箱', required: false } },
      { type: 'phone', label: '手机号', icon: '📱', category: 'input', defaultProps: { label: '手机号', placeholder: '请输入手机号', required: false } },
      { type: 'url', label: '网址', icon: '🔗', category: 'input', defaultProps: { label: '网址', placeholder: '请输入网址', required: false } },
      { type: 'password', label: '密码', icon: '🔒', category: 'input', defaultProps: { label: '密码', placeholder: '请输入密码', required: false } },
    ]
  },
  {
    name: 'select',
    label: '选择器',
    icon: '📋',
    expanded: false,
    components: [
      { type: 'select', label: '下拉选择', icon: '🔽', category: 'select', defaultProps: { label: '下拉选择', placeholder: '请选择', options: [{ label: '选项1', value: '1' }, { label: '选项2', value: '2' }], required: false } },
      { type: 'radio', label: '单选按钮', icon: '⭕', category: 'select', defaultProps: { label: '单选按钮', options: [{ label: '选项1', value: '1' }, { label: '选项2', value: '2' }], required: false } },
      { type: 'checkbox', label: '多选框', icon: '☑️', category: 'select', defaultProps: { label: '多选框', options: [{ label: '选项1', value: '1' }, { label: '选项2', value: '2' }], required: false } },
      { type: 'switch', label: '开关', icon: '🔘', category: 'select', defaultProps: { label: '开关', required: false } },
      { type: 'slider', label: '滑块', icon: '🎚️', category: 'select', defaultProps: { label: '滑块', min: 0, max: 100, required: false } },
      { type: 'rate', label: '评分', icon: '⭐', category: 'select', defaultProps: { label: '评分', required: false } },
    ]
  },
  {
    name: 'datetime',
    label: '日期时间',
    icon: '📅',
    expanded: false,
    components: [
      { type: 'date', label: '日期', icon: '🗓️', category: 'datetime', defaultProps: { label: '日期', required: false } },
      { type: 'datetime', label: '日期时间', icon: '🕐', category: 'datetime', defaultProps: { label: '日期时间', required: false } },
      { type: 'daterange', label: '日期范围', icon: '📆', category: 'datetime', defaultProps: { label: '日期范围', required: false } },
      { type: 'time', label: '时间', icon: '⏰', category: 'datetime', defaultProps: { label: '时间', required: false } },
    ]
  },
  {
    name: 'upload',
    label: '上传',
    icon: '📤',
    expanded: false,
    components: [
      { type: 'file', label: '文件上传', icon: '📎', category: 'upload', defaultProps: { label: '文件上传', limit: 1, required: false } },
      { type: 'image', label: '图片上传', icon: '🖼️', category: 'upload', defaultProps: { label: '图片上传', limit: 1, required: false } },
    ]
  },
  {
    name: 'layout',
    label: '布局',
    icon: '📐',
    expanded: false,
    components: [
      { type: 'divider', label: '分割线', icon: '➖', category: 'layout', defaultProps: { label: '', required: false } },
      { type: 'space', label: '间距', icon: '↕️', category: 'layout', defaultProps: { label: '', height: 20, required: false } },
    ]
  },
  {
    name: 'display',
    label: '展示',
    icon: '📊',
    expanded: false,
    components: [
      { type: 'text', label: '文本', icon: 'ℹ️', category: 'display', defaultProps: { label: '', content: '文本内容', required: false } },
      { type: 'html', label: 'HTML', icon: '🌐', category: 'display', defaultProps: { label: '', content: '<b>HTML内容</b>', required: false } },
    ]
  },
  {
    name: 'advanced',
    label: '高级',
    icon: '⚡',
    expanded: false,
    components: [
      { type: 'signature', label: '签名', icon: '✍️', category: 'advanced', defaultProps: { label: '签名', required: false } },
      { type: 'location', label: '位置', icon: '📍', category: 'advanced', defaultProps: { label: '位置', required: false } },
      { type: 'barcode', label: '条码', icon: '📊', category: 'advanced', defaultProps: { label: '条码', required: false } },
      { type: 'color', label: '颜色选择', icon: '🎨', category: 'advanced', defaultProps: { label: '颜色', required: false } },
    ]
  },
])

// 计算属性
const canUndo = computed(() => historyIndex.value > 0)
const canRedo = computed(() => historyIndex.value < history.value.length - 1)

const formStatusType = computed(() => {
  if (!formName.value) return 'info'
  if (widgets.value.length === 0) return 'warning'
  return 'success'
})

const formStatusText = computed(() => {
  if (!formName.value) return '未命名'
  if (widgets.value.length === 0) return '空白表单'
  return `已配置${widgets.value.length}个组件`
})

const hasAdvancedProps = computed(() => {
  if (!selectedWidget.value) return false
  const type = selectedWidget.value.type
  return ['select', 'radio', 'checkbox', 'number', 'slider', 'textarea', 'file', 'image', 'space', 'text', 'html'].includes(type)
})

// 方法
const toggleCategory = (name: string) => {
  const category = componentCategories.value.find(c => c.name === name)
  if (category) {
    category.expanded = !category.expanded
  }
}

const generateId = () => {
  return 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const addComponent = (component: Component) => {
  const newWidget: Widget = {
    id: generateId(),
    type: component.type,
    props: { ...component.defaultProps }
  }
  
  widgets.value.push(newWidget)
  saveHistory()
  selectedWidget.value = newWidget
  
  ElMessage.success(`已添加${component.label}`)
}

const onDragStart = (event: DragEvent, component: Component) => {
  event.dataTransfer?.setData('component', JSON.stringify(component))
}

const onDrop = (event: DragEvent) => {
  const data = event.dataTransfer?.getData('component')
  if (data) {
    const component = JSON.parse(data) as Component
    addComponent(component)
  }
}

const onWidgetDrop = (event: DragEvent, index: number) => {
  // 处理组件排序逻辑
}

const selectWidget = (widget: Widget | null) => {
  selectedWidget.value = widget
}

const updateWidget = () => {
  // 触发更新
  saveHistory()
}

const deleteWidget = (index: number) => {
  widgets.value.splice(index, 1)
  if (selectedWidget.value && !widgets.value.includes(selectedWidget.value)) {
    selectedWidget.value = null
  }
  saveHistory()
  ElMessage.success('组件已删除')
}

const copyWidget = (widget: Widget) => {
  const copiedWidget: Widget = {
    id: generateId(),
    type: widget.type,
    props: { ...widget.props }
  }
  
  const index = widgets.value.indexOf(widget)
  widgets.value.splice(index + 1, 0, copiedWidget)
  saveHistory()
  selectedWidget.value = copiedWidget
  
  ElMessage.success('组件已复制')
}

const moveUp = (index: number) => {
  if (index > 0) {
    const temp = widgets.value[index]
    widgets.value[index] = widgets.value[index - 1]
    widgets.value[index - 1] = temp
    saveHistory()
  }
}

const moveDown = (index: number) => {
  if (index < widgets.value.length - 1) {
    const temp = widgets.value[index]
    widgets.value[index] = widgets.value[index + 1]
    widgets.value[index + 1] = temp
    saveHistory()
  }
}

const toggleGrid = () => {
  showGrid.value = !showGrid.value
}

const clearForm = () => {
  widgets.value = []
  selectedWidget.value = null
  formName.value = ''
  saveHistory()
  ElMessage.success('表单已清空')
}

const saveHistory = () => {
  // 保存历史记录
  const currentState = JSON.parse(JSON.stringify(widgets.value))
  
  if (historyIndex.value < history.value.length - 1) {
    history.value = history.value.slice(0, historyIndex.value + 1)
  }
  
  history.value.push(currentState)
  historyIndex.value = history.value.length - 1
  
  // 限制历史记录数量
  if (history.value.length > 50) {
    history.value.shift()
    historyIndex.value--
  }
}

const undo = () => {
  if (canUndo.value) {
    historyIndex.value--
    widgets.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]))
    selectedWidget.value = null
    ElMessage.info('已撤销')
  }
}

const redo = () => {
  if (canRedo.value) {
    historyIndex.value++
    widgets.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]))
    selectedWidget.value = null
    ElMessage.info('已重做')
  }
}

const addOption = () => {
  if (selectedWidget.value) {
    if (!selectedWidget.value.props.options) {
      selectedWidget.value.props.options = []
    }
    selectedWidget.value.props.options.push({
      label: '新选项',
      value: String(selectedWidget.value.props.options.length + 1)
    })
    updateWidget()
  }
}

const removeOption = (index: number) => {
  if (selectedWidget.value && selectedWidget.value.props.options) {
    selectedWidget.value.props.options.splice(index, 1)
    updateWidget()
  }
}

const isInputType = (type: string) => {
  return true // 所有类型都是可接受的
}

const getComponent = (type: string) => {
  // 返回对应的Element Plus组件
  return 'el-input'
}

const preview = () => {
  if (widgets.value.length === 0) {
    ElMessage.warning('请先添加组件')
    return
  }
  previewVisible.value = true
}

const saveForm = () => {
  if (!formName.value) {
    ElMessage.warning('请输入表单名称')
    return
  }
  
  if (widgets.value.length === 0) {
    ElMessage.warning('请先添加组件')
    return
  }
  
  // 保存表单配置
  const formConfig = {
    name: formName.value,
    widgets: widgets.value,
    createdAt: new Date().toISOString()
  }
  
  localStorage.setItem('form_config_' + formName.value, JSON.stringify(formConfig))
  
  ElMessage.success('表单已保存')
}

const publishForm = () => {
  if (!formName.value) {
    ElMessage.warning('请输入表单名称')
    return
  }
  
  if (widgets.value.length === 0) {
    ElMessage.warning('请先添加组件')
    return
  }
  
  // 发布逻辑
  ElMessage.success('表单已发布')
}

const submitForm = () => {
  ElMessage.success('表单提交成功')
  previewVisible.value = false
}

// 初始化
saveHistory()
</script>

<style scoped>
.form-builder {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f0f2f5;
}

/* 工具栏 */
.builder-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-name-input {
  width: 200px;
}

/* 主体区域 */
.builder-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 组件面板 */
.component-panel {
  width: 240px;
  background: white;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.panel-header h4 {
  margin: 0;
  font-size: 14px;
}

.component-categories {
  padding: 8px;
}

.category {
  margin-bottom: 8px;
}

.category-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.category-title:hover {
  background: #e8e8e8;
}

.component-list {
  padding: 8px 0;
}

.component-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin: 4px 0;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.component-item:hover {
  background: #e6f7ff;
  border-color: #1890ff;
}

.component-icon {
  margin-right: 8px;
  font-size: 14px;
}

/* 表单画布 */
.form-canvas {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  font-size: 13px;
  font-weight: 500;
}

.canvas-actions {
  display: flex;
  gap: 8px;
}

.canvas-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.canvas-content.show-grid {
  background-image: 
    linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}

.empty-canvas {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 14px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-canvas .hint {
  font-size: 12px;
  color: #ccc;
}

.widget-list {
  max-width: 800px;
  margin: 0 auto;
}

.widget-item {
  position: relative;
  padding: 16px;
  margin-bottom: 12px;
  background: white;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.widget-item:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.widget-item.selected {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.widget-content {
  position: relative;
}

.widget-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.widget-label .required {
  color: #ff4d4f;
  margin-left: 4px;
}

.widget-id {
  position: absolute;
  top: 0;
  right: 0;
  font-size: 10px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.widget-actions {
  position: absolute;
  top: -12px;
  right: 8px;
  display: flex;
  gap: 4px;
  background: white;
  padding: 4px 8px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.widget-actions .delete-btn {
  color: #ff4d4f;
}

/* 属性面板 */
.property-panel {
  width: 280px;
  background: white;
  border-left: 1px solid #e8e8e8;
  overflow-y: auto;
}

.property-content {
  padding: 16px;
}

.property-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.property-item {
  margin-bottom: 12px;
}

.property-item label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.options-editor .option-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.empty-property {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  font-size: 14px;
}

.empty-property .hint {
  font-size: 12px;
  color: #ccc;
}

/* 预览内容 */
.preview-content {
  padding: 16px;
}

.preview-content h3 {
  margin-bottom: 20px;
  text-align: center;
}

.preview-widget {
  margin-bottom: 16px;
}
</style>
