import { RectNode, RectNodeModel } from '@logicflow/core'
import { h } from '@logicflow/core'

const NODE_WIDTH = 180
const NODE_HEIGHT = 50

const nodeThemeMap = {
  'device-event-trigger': { fill: '#1a3a5c', stroke: '#409eff', textColor: '#409eff', icon: '📡', shape: 'rect' },
  'condition':            { fill: '#3d2b1a', stroke: '#e6a23c', textColor: '#e6a23c', icon: '⚡', shape: 'diamond' },
  'data-transform':       { fill: '#3d2b1a', stroke: '#e6a23c', textColor: '#e6a23c', icon: '🔄', shape: 'rect' },
  'kafka-sink':           { fill: '#3d2b1a', stroke: '#e6a23c', textColor: '#e6a23c', icon: '📨', shape: 'roundRect' },
  'rocketmq-sink':        { fill: '#3d1a1a', stroke: '#f56c6c', textColor: '#f56c6c', icon: '🚀', shape: 'roundRect' },
  'mqtt-sink':            { fill: '#1a3a5c', stroke: '#409eff', textColor: '#409eff', icon: '📡', shape: 'roundRect' },
  'http-sink':            { fill: '#1a3d2b', stroke: '#67c23a', textColor: '#67c23a', icon: '🌐', shape: 'roundRect' },
  'pulsar-sink':          { fill: '#2b1a3d', stroke: '#9b59b6', textColor: '#9b59b6', icon: '🌀', shape: 'roundRect' },
  'redis-sink':           { fill: '#3d1a1a', stroke: '#f56c6c', textColor: '#f56c6c', icon: '🔴', shape: 'roundRect' }
}

class IotRectNode extends RectNode {
  getShape() {
    const { model } = this.props
    const { x, y, width, height } = model
    const theme = nodeThemeMap[model.type] || nodeThemeMap['log']

    if (theme.shape === 'diamond') {
      const hw = width / 2
      const hh = height / 2
      const points = [
        [x, y - hh],
        [x + hw, y],
        [x, y + hh],
        [x - hw, y]
      ]
      return h('g', {},
        h('polygon', {
          points: points.map(p => p.join(',')).join(' '),
          fill: theme.fill,
          stroke: theme.stroke,
          strokeWidth: 2
        }),
        h('text', {
          x: x - hw + 14,
          y: y + 5,
          fill: theme.textColor,
          fontSize: 13,
          fontWeight: '600'
        }, `${theme.icon} ${model.properties?.name || model.type}`),
        h('circle', { cx: x, cy: y - hh, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
        h('circle', { cx: x, cy: y + hh, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
        h('circle', { cx: x + hw, cy: y, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
        h('circle', { cx: x - hw, cy: y, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 })
      )
    }

    const rx = theme.shape === 'roundRect' ? 12 : 4
    return h('g', {},
      h('rect', {
        x: x - width / 2,
        y: y - height / 2,
        width,
        height,
        rx,
        ry: rx,
        fill: theme.fill,
        stroke: theme.stroke,
        strokeWidth: 2
      }),
      h('text', {
        x: x - width / 2 + 14,
        y: y + 5,
        fill: theme.textColor,
        fontSize: 13,
        fontWeight: '600'
      }, `${theme.icon} ${model.properties?.name || model.type}`),
      h('circle', { cx: x, cy: y - height / 2, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
      h('circle', { cx: x, cy: y + height / 2, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
      h('circle', { cx: x - width / 2, cy: y, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 }),
      h('circle', { cx: x + width / 2, cy: y, r: 5, fill: '#0d1b2a', stroke: theme.stroke, strokeWidth: 2 })
    )
  }
}

class IotNodeModel extends RectNodeModel {
  constructor(data, graphModel) {
    super(data, graphModel)
    this.width = NODE_WIDTH
    this.height = NODE_HEIGHT
  }

  setAttributes() {
    this.width = NODE_WIDTH
    this.height = NODE_HEIGHT
  }

  getTextStyle() {
    return { fontSize: 0 }
  }

  isShowCustomAnchor() {
    return true
  }

  getNodeStyle() {
    const theme = nodeThemeMap[this.type] || nodeThemeMap['log']
    return {
      fill: theme.fill,
      stroke: theme.stroke,
      strokeWidth: 2
    }
  }

  getDefaultAnchor() {
    const { x, y, width, height } = this
    return [
      { x, y: y - height / 2, id: 'top' },
      { x, y: y + height / 2, id: 'bottom' },
      { x: x - width / 2, y, id: 'left' },
      { x: x + width / 2, y, id: 'right' }
    ]
  }
}

export function registerCustomNodes(lf) {
  const allTypes = Object.keys(nodeThemeMap)
  allTypes.forEach(type => {
    lf.register({
      type,
      view: IotRectNode,
      model: IotNodeModel
    })
  })
}

export { nodeThemeMap, IotRectNode, IotNodeModel }
