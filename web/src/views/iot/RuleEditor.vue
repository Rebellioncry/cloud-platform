<template>
  <div class="page-container">
    <div class="editor-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <span class="rule-name">{{ ruleName }}</span>
        <el-tag v-if="ruleStatus === 1" type="success" size="small">运行中</el-tag>
        <el-tag v-else type="info" size="small">已停止</el-tag>
      </div>
      <div class="header-right">
        <el-button @click="handleUndo" :disabled="!canUndo">撤销</el-button>
        <el-button @click="handleRedo" :disabled="!canRedo">重做</el-button>
        <el-button @click="handleZoomIn">放大</el-button>
        <el-button @click="handleZoomOut">缩小</el-button>
        <el-button @click="handleZoomReset">适应</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </div>

    <div class="editor-body">
      <div class="node-panel">
        <div class="panel-title">节点面板</div>
        <div class="node-group">
          <div class="group-title">触发器</div>
          <div
            v-for="node in triggerNodes"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="onDragStart($event, node)"
          >
            <div class="node-icon" :style="{ background: node.color }">{{ node.icon }}</div>
            <span>{{ node.label }}</span>
          </div>
        </div>
        <div class="node-group">
          <div class="group-title">处理</div>
          <div
            v-for="node in transformNodes"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="onDragStart($event, node)"
          >
            <div class="node-icon" :style="{ background: node.color }">{{ node.icon }}</div>
            <span>{{ node.label }}</span>
          </div>
        </div>
        <div class="node-group">
          <div class="group-title">输出</div>
          <div
            v-for="node in sinkNodes"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="onDragStart($event, node)"
          >
            <div class="node-icon" :style="{ background: node.color }">{{ node.icon }}</div>
            <span>{{ node.label }}</span>
          </div>
        </div>
      </div>

      <div
        ref="containerRef"
        class="flow-container"
        @dragover.prevent
        @drop="onDrop"
      />

      <div class="right-panel">
        <div class="property-panel">
          <div class="panel-title">属性配置</div>
          <div class="prop-form" v-if="selectedNode">
            <div class="prop-row">
              <label>节点名称</label>
              <el-input :model-value="selectedNodeProperties.name" size="small" readonly disabled />
            </div>

            <!-- 设备触发器属性 -->
            <template v-if="selectedNode.type === 'device-event-trigger'">
              <div class="prop-row">
                <label>产品Key</label>
                <el-input v-model="selectedNodeProperties.productKey" size="small" placeholder="留空匹配全部产品" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>事件类型</label>
                <el-select v-model="selectedNodeProperties.eventType" size="small" style="width:100%" @change="onPropertyChange">
                  <el-option label="全部" value="" />
                  <el-option label="属性上报" value="property" />
                  <el-option label="事件上报" value="event" />
                </el-select>
              </div>
            </template>

            <!-- 条件节点属性 -->
            <template v-if="selectedNode.type === 'condition'">
              <div class="prop-row">
                <label>条件表达式(Aviator)</label>
                <el-input v-model="selectedNodeProperties.expression" type="textarea" :rows="3" size="small" placeholder='例如: payload.params.hr > 60' @change="onPropertyChange" />
              </div>
            </template>

            <!-- 数据转换节点属性 -->
            <template v-if="selectedNode.type === 'data-transform'">
              <div class="prop-row">
                <label>源字段</label>
                <el-input v-model="selectedNodeProperties.sourceField" size="small" placeholder="payload.params.hr" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>目标字段</label>
                <el-input v-model="selectedNodeProperties.targetField" size="small" placeholder="payload.params.tr" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>转换表达式(Aviator)</label>
                <el-input v-model="selectedNodeProperties.expression" type="textarea" :rows="3" size="small" placeholder='例如: sourceValue * 1.8 + 32' @change="onPropertyChange" />
              </div>
            </template>

            <!-- Kafka节点属性 -->
            <template v-if="selectedNode.type === 'kafka-sink'">
              <div class="prop-row">
                <label>Bootstrap Servers</label>
                <el-input v-model="selectedNodeProperties.bootstrapServers" size="small" placeholder="localhost:9092" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Topic</label>
                <el-input v-model="selectedNodeProperties.topic" size="small" placeholder="iot-data" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Group ID</label>
                <el-input v-model="selectedNodeProperties.groupId" size="small" placeholder="iot-rule-engine" @change="onPropertyChange" />
              </div>
            </template>

            <!-- MQTT节点属性 -->
            <template v-if="selectedNode.type === 'mqtt-sink'">
              <div class="prop-row">
                <label>Broker</label>
                <el-input v-model="selectedNodeProperties.broker" size="small" placeholder="localhost" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>端口</label>
                <el-input v-model.number="selectedNodeProperties.port" size="small" placeholder="1883" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Topic</label>
                <el-input v-model="selectedNodeProperties.topic" size="small" placeholder="iot/output" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>用户名</label>
                <el-input v-model="selectedNodeProperties.username" size="small" placeholder="可选" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>密码</label>
                <el-input v-model="selectedNodeProperties.password" size="small" placeholder="可选" show-password @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>QoS</label>
                <el-select v-model.number="selectedNodeProperties.qos" size="small" style="width:100%" @change="onPropertyChange">
                  <el-option label="0 - 最多一次" :value="0" />
                  <el-option label="1 - 至少一次" :value="1" />
                  <el-option label="2 - 恰好一次" :value="2" />
                </el-select>
              </div>
              <div class="prop-row">
                <label>KeepAlive(秒)</label>
                <el-input-number v-model="selectedNodeProperties.keepAlive" size="small" :min="10" :max="300" :step="10" @change="onPropertyChange" />
              </div>
            </template>

            <!-- HTTP节点属性 -->
            <template v-if="selectedNode.type === 'http-sink'">
              <div class="prop-row">
                <label>URL</label>
                <el-input v-model="selectedNodeProperties.url" size="small" placeholder="http://localhost:8080/api" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Method</label>
                <el-select v-model="selectedNodeProperties.method" size="small" style="width:100%" @change="onPropertyChange">
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                </el-select>
              </div>
              <div class="prop-row">
                <label>Content-Type</label>
                <el-input v-model="selectedNodeProperties.contentType" size="small" placeholder="application/json" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>超时(ms)</label>
                <el-input-number v-model.number="selectedNodeProperties.timeout" size="small" placeholder="5000" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Auth Token</label>
                <el-input v-model="selectedNodeProperties.authToken" size="small" placeholder="可选" show-password @change="onPropertyChange" />
              </div>
            </template>

            <!-- RocketMQ节点属性 -->
            <template v-if="selectedNode.type === 'rocketmq-sink'">
              <div class="prop-row">
                <label>Name Server</label>
                <el-input v-model="selectedNodeProperties.nameServer" size="small" placeholder="localhost:9876" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Topic</label>
                <el-input v-model="selectedNodeProperties.topic" size="small" placeholder="iot-data" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Producer Group</label>
                <el-input v-model="selectedNodeProperties.producerGroup" size="small" placeholder="iot-rule-producer" @change="onPropertyChange" />
              </div>
            </template>

            <!-- Pulsar节点属性 -->
            <template v-if="selectedNode.type === 'pulsar-sink'">
              <div class="prop-row">
                <label>Service URL</label>
                <el-input v-model="selectedNodeProperties.serviceUrl" size="small" placeholder="pulsar://localhost:6650" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Topic</label>
                <el-input v-model="selectedNodeProperties.topic" size="small" placeholder="persistent://public/default/iot" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Tenant</label>
                <el-input v-model="selectedNodeProperties.tenant" size="small" placeholder="public" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Namespace</label>
                <el-input v-model="selectedNodeProperties.namespace" size="small" placeholder="default" @change="onPropertyChange" />
              </div>
            </template>

            <!-- Redis节点属性 -->
            <template v-if="selectedNode.type === 'redis-sink'">
              <div class="prop-row">
                <label>Host</label>
                <el-input v-model="selectedNodeProperties.host" size="small" placeholder="localhost" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>端口</label>
                <el-input v-model.number="selectedNodeProperties.port" size="small" placeholder="6379" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Key</label>
                <el-input v-model="selectedNodeProperties.key" size="small" placeholder="iot:data" @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>密码</label>
                <el-input v-model="selectedNodeProperties.password" size="small" placeholder="可选" show-password @change="onPropertyChange" />
              </div>
              <div class="prop-row">
                <label>Database</label>
                <el-input v-model.number="selectedNodeProperties.database" size="small" placeholder="0" @change="onPropertyChange" />
              </div>
            </template>

            <div class="prop-row" style="margin-top: 16px">
              <el-button type="danger" size="small" @click="handleDeleteNode">删除节点</el-button>
            </div>
          </div>
          <div class="empty-tip" v-else>点击节点查看属性</div>
        </div>

        <div class="help-panel">
          <div class="panel-title">📖 使用说明</div>
          <div class="help-scroll">
            <div class="help-section">
              <h3>整体流程</h3>
              <div class="help-flow-diagram">
                <div class="flow-box trigger-box">📡 设备事件</div>
                <div class="flow-arrow">→</div>
                <div class="flow-box transform-box">⚡ 条件 / 🔄 转换</div>
                <div class="flow-arrow">→</div>
                <div class="flow-box sink-box">📡 输出</div>
              </div>
              <p class="help-desc">拖拽节点到画布，从锚点（圆点）拖出连线，保存后启动规则。</p>
            </div>

            <div class="help-section">
              <h3>节点填写指南</h3>

              <h4>📡 设备事件触发器</h4>
              <table class="help-table">
                <thead><tr><th>字段</th><th>说明</th></tr></thead>
                <tbody>
                  <tr><td>产品Key</td><td>留空 = 匹配所有产品。填入具体 productKey 则只触发该产品的数据</td></tr>
                  <tr><td>事件类型</td><td><code>全部</code> / <code>属性上报</code> / <code>事件上报</code></td></tr>
                </tbody>
              </table>

              <h4>⚡ 条件判断</h4>
              <p class="help-desc">填写 Aviator 表达式，返回 true 放行，false 停止链路。支持 <code>payload.params.字段名</code> 或 <code>payload['params']['字段名']</code>。</p>
              <div class="help-code-block"><code>
payload.params.hr &gt; 60<br>
payload.params.br == 30 &amp;&amp; payload.params.hr &gt; 60<br>
productKey == "smart-band-01"<br>
              </code></div>

              <h4>🔄 数据转换</h4>
              <table class="help-table">
                <thead><tr><th>字段</th><th>说明</th><th>示例</th></tr></thead>
                <tbody>
                  <tr><td>源字段</td><td>用 Aviator 语法提取的字段路径</td><td><code>payload.params.hr</code></td></tr>
                  <tr><td>目标字段</td><td>转换结果写入的位置（支持嵌套）</td><td><code>payload.params.tr</code></td></tr>
                  <tr><td>转换表达式</td><td><code>sourceValue</code> 是源字段的值</td><td><code>sourceValue * 1.8 + 32</code></td></tr>
                </tbody>
              </table>
              <p class="help-desc">目标字段填 <code>payload.params.tr</code> 时，结果会写入 payload 内部，输出节点发送时会包含该字段。</p>

              <h4>📡 MQTT输出</h4>
              <table class="help-table">
                <thead><tr><th>字段</th><th>说明</th><th>示例</th></tr></thead>
                <tbody>
                  <tr><td>Broker</td><td>MQTT 服务器地址</td><td><code>lyz.iot.com</code></td></tr>
                  <tr><td>端口</td><td>MQTT 端口</td><td><code>1883</code></td></tr>
                  <tr><td>Topic</td><td>发送目标 Topic</td><td><code>iot/output</code></td></tr>
                  <tr><td>用户名/密码</td><td>认证信息，留空则无认证</td><td></td></tr>
                  <tr><td>QoS</td><td>0=最多一次, 1=至少一次, 2=恰好一次</td><td><code>1</code></td></tr>
                </tbody>
              </table>
              <p class="help-desc">发送的 payload 为经过数据转换后的完整 payload 对象（JSON 格式）。</p>
            </div>

            <div class="help-section">
              <h3>可用变量</h3>
              <div class="help-code-block"><code>
productKey   — 产品标识<br>
deviceName   — 设备名称<br>
eventType    — 事件类型 (property/event)<br>
method       — 上报方法<br>
payload      — 上报数据 (Map)<br>
payload.params.字段名 — 具体参数值<br>
              </code></div>
            </div>

            <div class="help-section">
              <h3>操作流程</h3>
              <ol class="help-steps">
                <li>从左侧<strong>拖拽节点</strong>到画布</li>
                <li>从锚点（圆点）<strong>拖出连线</strong>到下一个节点</li>
                <li><strong>单击节点</strong>，右侧配置属性</li>
                <li><strong>单击连线</strong>，按 Delete 键删除连线</li>
                <li>点击顶部<strong>「保存」</strong></li>
                <li>规则列表中点击<strong>「启动」</strong></li>
              </ol>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/index.css'
import { registerCustomNodes } from './ruleNodes'
import { getRule, updateRule } from '@/api/iot'

const route = useRoute()
const router = useRouter()
const ruleId = route.params.id
const ruleName = ref('')
const ruleStatus = ref(0)
const saving = ref(false)
const containerRef = ref(null)
const selectedNode = ref(null)
const selectedNodeProperties = reactive({})
let lf = null
const canUndo = ref(false)
const canRedo = ref(false)

const triggerNodes = [
  { type: 'device-event-trigger', label: '设备事件', icon: '📡', color: '#409eff', nodeType: 'trigger' }
]

const transformNodes = [
  { type: 'condition', label: '条件判断', icon: '⚡', color: '#e6a23c', nodeType: 'transform' },
  { type: 'data-transform', label: '数据转换', icon: '🔄', color: '#e6a23c', nodeType: 'transform' }
]

const sinkNodes = [
  { type: 'kafka-sink', label: 'Kafka', icon: '📨', color: '#e6a23c', nodeType: 'sink' },
  { type: 'rocketmq-sink', label: 'RocketMQ', icon: '🚀', color: '#f56c6c', nodeType: 'sink' },
  { type: 'mqtt-sink', label: 'MQTT', icon: '📡', color: '#409eff', nodeType: 'sink' },
  { type: 'http-sink', label: 'HTTP请求', icon: '🌐', color: '#67c23a', nodeType: 'sink' },
  { type: 'pulsar-sink', label: 'Pulsar', icon: '🌀', color: '#9b59b6', nodeType: 'sink' },
  { type: 'redis-sink', label: 'Redis', icon: '🔴', color: '#f56c6c', nodeType: 'sink' }
]

const allNodeTypes = [...triggerNodes, ...transformNodes, ...sinkNodes]

function getNodeDef(type) {
  return allNodeTypes.find(n => n.type === type)
}

onMounted(async () => {
  await loadRule()
  await nextTick()
  setTimeout(() => {
    initLogicFlow()
  }, 100)
  document.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('mousedown', handleCanvasMouseDown)
  if (lf) {
    lf.destroy()
    lf = null
  }
})

function initLogicFlow() {
  if (!containerRef.value) {
    console.error('LogicFlow container not found')
    return
  }

  const rect = containerRef.value.getBoundingClientRect()
  if (rect.width === 0 || rect.height === 0) {
    console.error('LogicFlow container has zero dimensions', rect)
    setTimeout(() => initLogicFlow(), 200)
    return
  }

  lf = new LogicFlow({
    container: containerRef.value,
    grid: true,
    background: { color: '#0d1b2a' },
    edgeType: 'polyline',
    snapline: true,
    history: true,
    textMode: 'disabled',
    keys: { ctrl: true, multiple: true }
  })

  registerCustomNodes(lf)

  lf.on('history:change', () => {
    canUndo.value = lf.history.undoAble()
    canRedo.value = lf.history.redoAble()
  })

  lf.on('node:click', ({ data }) => {
    selectedEdgeId = null
    selectNode(data)
  })

  lf.on('node:dblclick', () => false)

  lf.on('edge:click', ({ data }) => {
    selectedNode.value = null
    selectedEdgeId = data.id
  })

  lf.on('blank:click', () => {
    if (canvasMouseDown) {
      selectedNode.value = null
      selectedEdgeId = null
    }
    canvasMouseDown = false
  })

  lf.on('connection:not-allowed', ({ data }) => {
    ElMessage.warning('不允许的连接')
  })

  lf.render()

  document.addEventListener('mousedown', handleCanvasMouseDown)

  loadFlow()
}

async function loadRule() {
  try {
    const res = await getRule(ruleId)
    ruleName.value = res.data?.name || ''
    ruleStatus.value = res.data?.status || 0
  } catch (e) {
    console.error(e)
  }
}

async function loadFlow() {
  try {
    const res = await getRule(ruleId)
    const ruleModel = res.data?.ruleModel
    if (ruleModel && ruleModel.trim()) {
      const flowData = JSON.parse(ruleModel)
      if (flowData.nodes && flowData.nodes.length > 0) {
        lf.render(flowData)
      }
    }
  } catch (e) {
    console.error('加载流程图失败:', e)
  }
}

function getCanvasPoint(clientX, clientY) {
  const rect = containerRef.value.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  if (lf && lf.getTransform) {
    const { SCALE_X, TRANSLATE_X, TRANSLATE_Y } = lf.getTransform()
    return {
      x: (x - TRANSLATE_X) / SCALE_X,
      y: (y - TRANSLATE_Y) / SCALE_X
    }
  }
  return { x, y }
}

function handleCanvasMouseDown(e) {
  canvasMouseDown = !!(containerRef.value && containerRef.value.contains(e.target))
}

function handleKeyDown(e) {
  if (e.key === 'Delete' || e.key === 'Backspace') {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
    if (!lf) return

    if (selectedNode.value) {
      const nodeId = selectedNode.value.id
      try { lf.deleteNode(nodeId) } catch { try { lf.removeNode(nodeId) } catch {} }
      selectedNode.value = null
      e.preventDefault()
    } else if (selectedEdgeId) {
      try { lf.deleteEdge(selectedEdgeId) } catch { try { lf.removeEdge(selectedEdgeId) } catch {} }
      selectedEdgeId = null
      e.preventDefault()
    }
  }
}

let canvasMouseDown = false
let draggedNode = null
let selectedEdgeId = null

function onDragStart(event, node) {
  draggedNode = node
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', node.type)
}

function onDrop(event) {
  event.preventDefault()
  if (!lf || !draggedNode) return

  const point = getCanvasPoint(event.clientX, event.clientY)
  addNodeAtPoint(draggedNode, point.x, point.y)
  draggedNode = null
}

function addNodeToCanvas(node) {
  if (!lf) return
  const rect = containerRef.value.getBoundingClientRect()
  const point = getCanvasPoint(
    rect.left + rect.width / 2,
    rect.top + rect.height / 2
  )
  addNodeAtPoint(node, point.x, point.y)
}

function addNodeAtPoint(node, x, y) {
  const defaultProps = {
    name: node.label,
    ...(node.nodeType === 'trigger' ? { productKey: '', eventType: '' } : {}),
    ...(node.type === 'condition' ? { expression: '' } : {}),
    ...(node.type === 'data-transform' ? { sourceField: '', targetField: '', expression: '' } : {}),
    ...(node.type === 'kafka-sink' ? { bootstrapServers: 'localhost:9092', topic: '', groupId: 'iot-rule-engine' } : {}),
    ...(node.type === 'rocketmq-sink' ? { nameServer: 'localhost:9876', topic: '', producerGroup: 'iot-rule-producer' } : {}),
    ...(node.type === 'mqtt-sink' ? { broker: 'localhost', port: 1883, topic: '', username: '', password: '', qos: 1, keepAlive: 60 } : {}),
    ...(node.type === 'http-sink' ? { url: '', method: 'POST', contentType: 'application/json', timeout: 5000, authToken: '' } : {}),
    ...(node.type === 'pulsar-sink' ? { serviceUrl: 'pulsar://localhost:6650', topic: '', tenant: 'public', namespace: 'default' } : {}),
    ...(node.type === 'redis-sink' ? { host: 'localhost', port: 6379, key: '', password: '', database: 0 } : {})
  }

  try {
    const newNode = lf.addNode({
      type: node.type,
      x,
      y,
      properties: defaultProps
    })

    if (newNode) {
      selectNode({ id: newNode.id, type: node.type, properties: defaultProps })
    }
  } catch (e) {
    console.error('添加节点失败:', e)
    ElMessage.error('添加节点失败: ' + e.message)
  }
}

function selectNode(data) {
  if (!lf) return
  const nodeModel = lf.getNodeModelById(data.id)
  if (!nodeModel) return

  selectedNode.value = {
    id: data.id,
    type: data.type
  }

  Object.keys(selectedNodeProperties).forEach(k => delete selectedNodeProperties[k])
  const props = nodeModel.getProperties ? nodeModel.getProperties() : (data.properties || {})
  Object.keys(props).forEach(k => {
    selectedNodeProperties[k] = props[k]
  })
}

function onPropertyChange() {
  if (!selectedNode.value || !lf) return
  const nodeModel = lf.getNodeModelById(selectedNode.value.id)
  if (!nodeModel) return

  const newProps = { ...selectedNodeProperties }
  if (nodeModel.setProperties) {
    nodeModel.setProperties(newProps)
  }
}

function handleDeleteNode() {
  if (!selectedNode.value || !lf) return
  lf.deleteNode(selectedNode.value.id)
  selectedNode.value = null
}

function handleUndo() { if (lf) lf.history.undo() }
function handleRedo() { if (lf) lf.history.redo() }
function handleZoomIn() { if (lf) lf.zoom(true) }
function handleZoomOut() { if (lf) lf.zoom(false) }
function handleZoomReset() { if (lf) lf.fitView() }

async function handleSave() {
  if (!lf) return
  saving.value = true
  try {
    const flowData = lf.getGraphData()
    await updateRule(ruleId, { ruleModel: JSON.stringify(flowData) })
    ElMessage.success('规则保存成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/iot/rule')
}
</script>

<style scoped>
.page-container { padding: 0; height: calc(100vh - 60px); display: flex; flex-direction: column; }

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #132238;
  border-bottom: 1px solid #1e3350;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.rule-name { font-size: 16px; font-weight: 600; color: #e2e8f0; }

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.node-panel {
  width: 200px;
  background: #0d1b2a;
  border-right: 1px solid #1e3350;
  overflow-y: auto;
  padding: 12px;
  flex-shrink: 0;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #1e3350;
}
.node-group { margin-bottom: 16px; }
.group-title {
  font-size: 12px;
  color: #5a6d80;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.node-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: grab;
  transition: background 0.2s;
  color: #c8d6e5;
  font-size: 13px;
  user-select: none;
}
.node-item:hover { background: rgba(64, 158, 255, 0.15); }
.node-item:active { background: rgba(64, 158, 255, 0.25); cursor: grabbing; }
.node-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.flow-container {
  flex: 1;
  background: #0d1b2a;
  min-width: 0;
}

.right-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #1e3350;
  flex-shrink: 0;
  overflow: hidden;
}

.property-panel {
  background: #0d1b2a;
  padding: 12px;
  max-height: 50%;
  overflow-y: auto;
  flex-shrink: 0;
  border-bottom: 1px solid #1e3350;
}
.empty-tip {
  color: #5a6d80;
  text-align: center;
  padding: 40px 0;
  font-size: 13px;
}

.help-panel {
  background: #0a1528;
  padding: 12px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.help-scroll {
  flex: 1;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.6;
}
.help-scroll h3 { color: #409eff; font-size: 13px; margin: 10px 0 6px; }
.help-scroll h4 { color: #e6a23c; font-size: 12px; margin: 8px 0 4px; }
.help-scroll .help-desc { color: #8899aa; margin: 4px 0; font-size: 12px; }
.help-scroll .help-desc code, .help-scroll .help-code-block code { color: #67c23a; font-family: 'Consolas', monospace; font-size: 11px; }
.help-scroll .help-code-block { background: #0a1628; border: 1px solid #1e3350; border-radius: 4px; padding: 8px 10px; margin: 6px 0; font-size: 11px; line-height: 1.7; }
.help-scroll .help-table { width: 100%; border-collapse: collapse; margin: 6px 0; }
.help-scroll .help-table th { background: #132238; color: #409eff; text-align: left; padding: 5px 8px; border: 1px solid #1e3350; font-size: 11px; }
.help-scroll .help-table td { padding: 5px 8px; border: 1px solid #1e3350; font-size: 11px; }
.help-scroll .help-flow-diagram { display: flex; align-items: center; gap: 6px; justify-content: center; margin: 8px 0; flex-wrap: wrap; }
.help-scroll .flow-box { padding: 6px 12px; border-radius: 6px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.help-scroll .flow-arrow { color: #5a6d80; font-size: 16px; font-weight: bold; }
.help-scroll .help-steps { padding-left: 16px; margin: 4px 0; }
.help-scroll .help-steps li { margin-bottom: 4px; font-size: 12px; }
.help-scroll .help-steps strong { color: #409eff; }
.help-scroll .help-section { margin-bottom: 12px; }

.prop-form { padding: 0; }
.prop-row { margin-bottom: 12px; }
.prop-row label {
  display: block;
  font-size: 12px;
  color: #8899aa;
  margin-bottom: 4px;
}

.trigger-box { background: rgba(64, 158, 255, 0.15); border: 1px solid #409eff; color: #409eff; }
.transform-box { background: rgba(230, 162, 60, 0.15); border: 1px solid #e6a23c; color: #e6a23c; }
.sink-box { background: rgba(103, 194, 58, 0.15); border: 1px solid #67c23a; color: #67c23a; }
</style>

