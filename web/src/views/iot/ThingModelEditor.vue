<template>
  <div class="thing-model-editor">
    <div class="editor-header">
      <el-button-group>
        <el-button :type="!showJson ? 'primary' : ''" @click="showJson = false">可视化编辑</el-button>
        <el-button :type="showJson ? 'primary' : ''" @click="switchToJson">JSON编辑</el-button>
      </el-button-group>
    </div>

    <div v-if="!showJson" class="visual-editor">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="属性 (Properties)" name="properties">
          <div class="tab-toolbar">
            <el-button type="primary" size="small" @click="addItem('property')">+ 添加属性</el-button>
          </div>
          <el-table :data="model.properties" border size="small" empty-text="暂无属性，点击上方按钮添加">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="identifier" label="标识符" min-width="120" />
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column label="数据类型" min-width="100">
              <template #default="{ row }">{{ dataTypeLabel(row.dataType?.type) }}</template>
            </el-table-column>
            <el-table-column label="读写类型" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.accessMode === 'rw'" size="small">读写</el-tag>
                <el-tag v-else-if="row.accessMode === 'w'" type="warning" size="small">只写</el-tag>
                <el-tag v-else type="info" size="small">只读</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button link type="primary" size="small" @click="editItem('property', $index)">编辑</el-button>
                <el-button link type="danger" size="small" @click="removeItem('properties', $index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="服务 (Services)" name="services">
          <div class="tab-toolbar">
            <el-button type="primary" size="small" @click="addItem('service')">+ 添加服务</el-button>
          </div>
          <el-table :data="model.services" border size="small" empty-text="暂无服务，点击上方按钮添加">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="identifier" label="标识符" min-width="120" />
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column label="调用方式" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.callType === 'sync'" size="small">同步</el-tag>
                <el-tag v-else type="warning" size="small">异步</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="入参" width="80">
              <template #default="{ row }">{{ (row.inputData || []).length }}个</template>
            </el-table-column>
            <el-table-column label="出参" width="80">
              <template #default="{ row }">{{ (row.outputData || []).length }}个</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button link type="primary" size="small" @click="editItem('service', $index)">编辑</el-button>
                <el-button link type="danger" size="small" @click="removeItem('services', $index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="事件 (Events)" name="events">
          <div class="tab-toolbar">
            <el-button type="primary" size="small" @click="addItem('event')">+ 添加事件</el-button>
          </div>
          <el-table :data="model.events" border size="small" empty-text="暂无事件，点击上方按钮添加">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="identifier" label="标识符" min-width="120" />
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column label="事件类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.type === 'alert'" type="warning" size="small">告警</el-tag>
                <el-tag v-else-if="row.type === 'error'" type="danger" size="small">错误</el-tag>
                <el-tag v-else size="small">信息</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="输出参数" width="80">
              <template #default="{ row }">{{ (row.outputData || []).length }}个</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ $index }">
                <el-button link type="primary" size="small" @click="editItem('event', $index)">编辑</el-button>
                <el-button link type="danger" size="small" @click="removeItem('events', $index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-else class="json-editor">
      <el-input v-model="jsonText" type="textarea" :rows="22" placeholder="请输入物模型JSON（阿里云IoT TSL格式）" />
    </div>

    <!-- 属性编辑弹窗 -->
    <el-dialog v-model="propDialogVisible" :title="editingIndex >= 0 ? '编辑属性' : '添加属性'" width="600px" append-to-body>
      <el-form :model="propForm" label-width="90px" size="small">
        <el-form-item label="标识符" required>
          <el-input v-model="propForm.identifier" placeholder="英文标识符，如 temperature" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="propForm.name" placeholder="如 温度" />
        </el-form-item>
        <el-form-item label="读写类型">
          <el-select v-model="propForm.accessMode" style="width: 100%">
            <el-option label="读写 (rw) - 可读取也可下发" value="rw" />
            <el-option label="只读 (r) - 仅读取设备上报值" value="r" />
            <el-option label="只写 (w) - 仅下发给设备" value="w" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型" required>
          <el-select v-model="propForm.dataType.type" style="width: 100%" @change="onDataTypeChange">
            <el-option label="int32 (整型)" value="int32" />
            <el-option label="float (单精度浮点)" value="float" />
            <el-option label="double (双精度浮点)" value="double" />
            <el-option label="text (字符串)" value="text" />
            <el-option label="bool (布尔)" value="bool" />
            <el-option label="date (时间戳, UTC毫秒)" value="date" />
            <el-option label="enum (枚举)" value="enum" />
            <el-option label="struct (结构体)" value="struct" />
            <el-option label="array (数组)" value="array" />
          </el-select>
        </el-form-item>

        <template v-if="['int', 'float', 'double'].includes(propForm.dataType.type)">
          <el-form-item label="最小值">
            <el-input-number v-model="propForm.dataType.specs.min" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最大值">
            <el-input-number v-model="propForm.dataType.specs.max" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="步长">
            <el-input-number v-model="propForm.dataType.specs.step" :controls="false" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="propForm.dataType.specs.unit" placeholder="如 ℃、%" />
          </el-form-item>
        </template>

        <template v-if="propForm.dataType.type === 'string'">
          <el-form-item label="最大长度">
            <el-input-number v-model="propForm.dataType.specs.maxLength" :min="1" style="width: 100%" />
          </el-form-item>
        </template>

        <template v-if="propForm.dataType.type === 'enum'">
          <el-form-item label="枚举值">
            <div v-for="(val, key, i) in propForm.dataType.specs" :key="key" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-input :model-value="key" disabled style="width: 80px" placeholder="值" />
              <el-input v-model="propForm.dataType.specs[key]" placeholder="名称" style="flex: 1" />
              <el-button link type="danger" @click="delete propForm.dataType.specs[key]">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button size="small" @click="addEnumItem">+ 添加枚举项</el-button>
          </el-form-item>
        </template>

        <template v-if="propForm.dataType.type === 'struct'">
          <el-form-item label="结构体字段">
            <div v-for="(field, i) in propForm.dataType.specs.fields" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-input v-model="field.identifier" placeholder="标识符" style="width: 120px" />
              <el-input v-model="field.name" placeholder="名称" style="width: 100px" />
              <el-select v-model="field.dataType" style="width: 110px">
                <el-option label="int32" value="int32" />
                <el-option label="float" value="float" />
                <el-option label="double" value="double" />
                <el-option label="text" value="text" />
                <el-option label="bool" value="bool" />
                <el-option label="date" value="date" />
                <el-option label="enum" value="enum" />
              </el-select>
              <el-button link type="danger" @click="propForm.dataType.specs.fields.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
            </div>
            <el-button size="small" @click="propForm.dataType.specs.fields.push({ identifier: '', name: '', dataType: 'int32' })">
              + 添加字段
            </el-button>
            <div style="color: #909399; font-size: 12px; margin-top: 4px">结构体内不支持嵌套 struct</div>
          </el-form-item>
        </template>

        <template v-if="propForm.dataType.type === 'array'">
          <el-form-item label="元素类型" required>
            <el-select v-model="propForm.dataType.specs.itemType" style="width: 100%">
              <el-option label="int32" value="int32" />
              <el-option label="float" value="float" />
              <el-option label="double" value="double" />
              <el-option label="text" value="text" />
              <el-option label="struct" value="struct" />
            </el-select>
          </el-form-item>
          <el-form-item label="元素个数" required>
            <el-input-number v-model="propForm.dataType.specs.size" :min="1" :max="512" style="width: 100%" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="propDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProperty">确定</el-button>
      </template>
    </el-dialog>

    <!-- 服务编辑弹窗 -->
    <el-dialog v-model="svcDialogVisible" :title="editingIndex >= 0 ? '编辑服务' : '添加服务'" width="650px" append-to-body>
      <el-form :model="svcForm" label-width="90px" size="small">
        <el-form-item label="标识符" required>
          <el-input v-model="svcForm.identifier" placeholder="英文标识符，如 set_temperature" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="svcForm.name" placeholder="如 设置温度" />
        </el-form-item>
        <el-form-item label="调用方式">
          <el-select v-model="svcForm.callType" style="width: 100%">
            <el-option label="异步 (async)" value="async" />
            <el-option label="同步 (sync)" value="sync" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">输入参数</el-divider>
        <div v-for="(param, i) in svcForm.inputData" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
          <el-input v-model="param.identifier" placeholder="标识符" style="width: 130px" />
          <el-input v-model="param.name" placeholder="名称" style="width: 120px" />
          <el-select v-model="param.dataType.type" style="width: 120px">
            <el-option label="int" value="int" />
            <el-option label="float" value="float" />
            <el-option label="text" value="text" />
            <el-option label="bool" value="bool" />
            <el-option label="double" value="double" />
          </el-select>
          <el-button link type="danger" @click="svcForm.inputData.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button size="small" @click="svcForm.inputData.push({ identifier: '', name: '', dataType: { type: 'int32' } })">
          + 添加输入参数
        </el-button>

        <el-divider content-position="left">输出参数</el-divider>
        <div v-for="(param, i) in svcForm.outputData" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
          <el-input v-model="param.identifier" placeholder="标识符" style="width: 130px" />
          <el-input v-model="param.name" placeholder="名称" style="width: 120px" />
          <el-select v-model="param.dataType.type" style="width: 120px">
            <el-option label="int32" value="int32" />
            <el-option label="float" value="float" />
            <el-option label="double" value="double" />
            <el-option label="text" value="text" />
            <el-option label="bool" value="bool" />
            <el-option label="date" value="date" />
            <el-option label="enum" value="enum" />
            <el-option label="struct" value="struct" />
            <el-option label="array" value="array" />
          </el-select>
          <el-button link type="danger" @click="svcForm.inputData.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button size="small" @click="svcForm.inputData.push({ identifier: '', name: '', dataType: { type: 'int32' } })">
          + 添加输入参数
        </el-button>

        <el-divider content-position="left">输出参数</el-divider>
        <div v-for="(param, i) in svcForm.outputData" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
          <el-input v-model="param.identifier" placeholder="标识符" style="width: 130px" />
          <el-input v-model="param.name" placeholder="名称" style="width: 120px" />
          <el-select v-model="param.dataType.type" style="width: 120px">
            <el-option label="int32" value="int32" />
            <el-option label="float" value="float" />
            <el-option label="double" value="double" />
            <el-option label="text" value="text" />
            <el-option label="bool" value="bool" />
            <el-option label="date" value="date" />
            <el-option label="enum" value="enum" />
            <el-option label="struct" value="struct" />
            <el-option label="array" value="array" />
          </el-select>
          <el-button link type="danger" @click="svcForm.outputData.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button size="small" @click="svcForm.outputData.push({ identifier: '', name: '', dataType: { type: 'int32' } })">
          + 添加输出参数
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="svcDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveService">确定</el-button>
      </template>
    </el-dialog>

    <!-- 事件编辑弹窗 -->
    <el-dialog v-model="evtDialogVisible" :title="editingIndex >= 0 ? '编辑事件' : '添加事件'" width="600px" append-to-body>
      <el-form :model="evtForm" label-width="90px" size="small">
        <el-form-item label="标识符" required>
          <el-input v-model="evtForm.identifier" placeholder="英文标识符，如 high_temperature" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="evtForm.name" placeholder="如 高温告警" />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="evtForm.type" style="width: 100%">
            <el-option label="信息 (info)" value="info" />
            <el-option label="告警 (alert)" value="alert" />
            <el-option label="错误 (error)" value="error" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">输出参数</el-divider>
        <div v-for="(param, i) in evtForm.outputData" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
          <el-input v-model="param.identifier" placeholder="标识符" style="width: 130px" />
          <el-input v-model="param.name" placeholder="名称" style="width: 120px" />
          <el-select v-model="param.dataType.type" style="width: 120px">
            <el-option label="int" value="int" />
            <el-option label="float" value="float" />
            <el-option label="text" value="text" />
            <el-option label="bool" value="bool" />
            <el-option label="double" value="double" />
          </el-select>
          <el-button link type="danger" @click="evtForm.outputData.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button size="small" @click="evtForm.outputData.push({ identifier: '', name: '', dataType: { type: 'int32' } })">
          + 添加输出参数
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="evtDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEvent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const activeTab = ref('properties')
const showJson = ref(false)
const jsonText = ref('')
const editingIndex = ref(-1)

const model = reactive({
  properties: [],
  services: [],
  events: []
})

const propDialogVisible = ref(false)
const svcDialogVisible = ref(false)
const evtDialogVisible = ref(false)

const propForm = reactive({
  identifier: '',
  name: '',
  accessMode: 'rw',
  dataType: { type: 'int', specs: {} }
})

const svcForm = reactive({
  identifier: '',
  name: '',
  callType: 'async',
  inputData: [],
  outputData: []
})

const evtForm = reactive({
  identifier: '',
  name: '',
  type: 'info',
  outputData: []
})

const DATA_TYPES = {
  int32: '整型(int32)',
  float: '单精度浮点(float)',
  double: '双精度浮点(double)',
  text: '字符串(text)',
  bool: '布尔(bool)',
  date: '时间戳(date)',
  enum: '枚举(enum)',
  struct: '结构体(struct)',
  array: '数组(array)'
}

const dataTypeLabel = (type) => DATA_TYPES[type] || type

function onDataTypeChange(type) {
  if (['int32', 'float', 'double'].includes(type)) {
    propForm.dataType.specs = { min: 0, max: 100, step: 1, unit: '', unitName: '' }
  } else if (type === 'text') {
    propForm.dataType.specs = {}
  } else if (type === 'bool') {
    propForm.dataType.specs = { '0': '关闭', '1': '开启' }
  } else if (type === 'date') {
    propForm.dataType.specs = {}
  } else if (type === 'enum') {
    propForm.dataType.specs = { '0': '' }
  } else if (type === 'struct') {
    propForm.dataType.specs = { fields: [] }
  } else if (type === 'array') {
    propForm.dataType.specs = { itemType: 'int32', size: 1 }
  } else {
    propForm.dataType.specs = {}
  }
}

function addEnumItem() {
  const keys = Object.keys(propForm.dataType.specs)
  const nextVal = keys.length > 0 ? Math.max(...keys.map(Number)) + 1 : 0
  propForm.dataType.specs[String(nextVal)] = ''
}

function toModel() {
  return JSON.stringify({
    properties: model.properties,
    services: model.services,
    events: model.events
  })
}

function fromJson(json) {
  try {
    const obj = typeof json === 'string' ? JSON.parse(json) : json
    model.properties = obj.properties || []
    model.services = obj.services || []
    model.events = obj.events || []
  } catch {
    model.properties = []
    model.services = []
    model.events = []
  }
}

watch(() => props.modelValue, (val) => {
  fromJson(val)
}, { immediate: true })

function switchToJson() {
  jsonText.value = toModel()
  showJson.value = true
}

function syncToParent() {
  emit('update:modelValue', toModel())
}

// ====== Property ======
function addItem(type) {
  editingIndex.value = -1
  if (type === 'property') {
    Object.assign(propForm, { identifier: '', name: '', accessMode: 'rw', dataType: { type: 'int', specs: { min: 0, max: 100, step: 1, unit: '' } } })
    propDialogVisible.value = true
  } else if (type === 'service') {
    Object.assign(svcForm, { identifier: '', name: '', callType: 'async', inputData: [], outputData: [] })
    svcDialogVisible.value = true
  } else if (type === 'event') {
    Object.assign(evtForm, { identifier: '', name: '', type: 'info', outputData: [] })
    evtDialogVisible.value = true
  }
}

function editItem(type, index) {
  editingIndex.value = index
  if (type === 'property') {
    const item = model.properties[index]
    Object.assign(propForm, {
      identifier: item.identifier,
      name: item.name,
      accessMode: item.accessMode || 'rw',
      dataType: JSON.parse(JSON.stringify(item.dataType || { type: 'int', specs: {} }))
    })
    propDialogVisible.value = true
  } else if (type === 'service') {
    const item = model.services[index]
    Object.assign(svcForm, {
      identifier: item.identifier,
      name: item.name,
      callType: item.callType || 'async',
      inputData: JSON.parse(JSON.stringify(item.inputData || [])),
      outputData: JSON.parse(JSON.stringify(item.outputData || []))
    })
    svcDialogVisible.value = true
  } else if (type === 'event') {
    const item = model.events[index]
    Object.assign(evtForm, {
      identifier: item.identifier,
      name: item.name,
      type: item.type || 'info',
      outputData: JSON.parse(JSON.stringify(item.outputData || []))
    })
    evtDialogVisible.value = true
  }
}

function removeItem(list, index) {
  model[list].splice(index, 1)
  syncToParent()
}

function saveProperty() {
  if (!propForm.identifier || !propForm.name) {
    ElMessage.warning('请填写标识符和名称')
    return
  }
  const item = { identifier: propForm.identifier, name: propForm.name, accessMode: propForm.accessMode, dataType: JSON.parse(JSON.stringify(propForm.dataType)) }
  if (editingIndex.value >= 0) {
    model.properties[editingIndex.value] = item
  } else {
    model.properties.push(item)
  }
  propDialogVisible.value = false
  syncToParent()
}

function saveService() {
  if (!svcForm.identifier || !svcForm.name) {
    ElMessage.warning('请填写标识符和名称')
    return
  }
  const item = {
    identifier: svcForm.identifier,
    name: svcForm.name,
    callType: svcForm.callType,
    inputData: svcForm.inputData.filter(p => p.identifier && p.name),
    outputData: svcForm.outputData.filter(p => p.identifier && p.name)
  }
  if (editingIndex.value >= 0) {
    model.services[editingIndex.value] = item
  } else {
    model.services.push(item)
  }
  svcDialogVisible.value = false
  syncToParent()
}

function saveEvent() {
  if (!evtForm.identifier || !evtForm.name) {
    ElMessage.warning('请填写标识符和名称')
    return
  }
  const item = {
    identifier: evtForm.identifier,
    name: evtForm.name,
    type: evtForm.type,
    outputData: evtForm.outputData.filter(p => p.identifier && p.name)
  }
  if (editingIndex.value >= 0) {
    model.events[editingIndex.value] = item
  } else {
    model.events.push(item)
  }
  evtDialogVisible.value = false
  syncToParent()
}

defineExpose({ toModel, fromJson })
</script>

<style scoped>
.thing-model-editor { width: 100%; }
.editor-header { margin-bottom: 12px; }
.tab-toolbar { margin-bottom: 10px; }
.json-editor { min-height: 400px; }
</style>
