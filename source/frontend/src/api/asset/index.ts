import request from '@/utils/request'

// 获取资产列表
export function getAssetList(params: any) {
  return request({
    url: '/asset/info/list',
    method: 'get',
    params
  })
}

// 获取资产详情
export function getAssetDetail(id: string) {
  return request({
    url: `/asset/info/${id}`,
    method: 'get'
  })
}

// 新增资产
export function addAsset(data: any) {
  return request({
    url: '/asset/info',
    method: 'post',
    data
  })
}

// 更新资产
export function updateAsset(data: any) {
  return request({
    url: '/asset/info',
    method: 'put',
    data
  })
}

// 删除资产
export function deleteAsset(id: string) {
  return request({
    url: `/asset/info/${id}`,
    method: 'delete'
  })
}

// 更新资产状态
export function updateAssetStatus(id: string, status: number) {
  return request({
    url: `/asset/info/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 资产领用
export function borrowAsset(data: any) {
  return request({
    url: '/asset/operation/borrow',
    method: 'post',
    data
  })
}

// 资产归还
export function returnAsset(data: any) {
  return request({
    url: '/asset/operation/return',
    method: 'post',
    data
  })
}

// 资产调拨
export function transferAsset(data: any) {
  return request({
    url: '/asset/operation/transfer',
    method: 'post',
    data
  })
}

// 资产报废
export function scrapAsset(data: any) {
  return request({
    url: '/asset/operation/scrap',
    method: 'post',
    data
  })
}

// 获取资产分类列表
export function getCategories() {
  return request({
    url: '/asset/category/list',
    method: 'get'
  })
}

// 获取库存记录
export function getStockRecords(params: any) {
  return request({
    url: '/asset/stock/list',
    method: 'get',
    params
  })
}

// 入库
export function stockIn(data: any) {
  return request({
    url: '/asset/stock/in',
    method: 'post',
    data
  })
}

// 出库
export function stockOut(data: any) {
  return request({
    url: '/asset/stock/out',
    method: 'post',
    data
  })
}

// 库存盘点
export function stockCheck(data: any) {
  return request({
    url: '/asset/stock/check',
    method: 'post',
    data
  })
}

// 获取办公用品列表
export function getOfficeSupplies(params: any) {
  return request({
    url: '/asset/office-supply/list',
    method: 'get',
    params
  })
}

// 申请办公用品
export function requestOfficeSupply(data: any) {
  return request({
    url: '/asset/office-supply/request',
    method: 'post',
    data
  })
}

// 审批办公用品申请
export function approveOfficeSupply(data: any) {
  return request({
    url: '/asset/office-supply/approve',
    method: 'post',
    data
  })
}

// 领用办公用品
export function claimOfficeSupply(data: any) {
  return request({
    url: '/asset/office-supply/claim',
    method: 'post',
    data
  })
}

// 生成标签
export function generateLabel(data: any) {
  return request({
    url: '/asset/label/generate',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 批量生成标签
export function batchGenerateLabel(data: any) {
  return request({
    url: '/asset/label/batch-generate',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 扫码登记
export function scanRegister(data: any) {
  return request({
    url: '/asset/stock/scan',
    method: 'post',
    data
  })
}