/**
 * Asset Data Models
 */
export interface AssetInfo {
  id: string;
  assetCode: string;
  assetName: string;
  categoryId: string;
  categoryName: string;
  model: string;
  currentQuantity: number;
  warningQuantity: number;
  purchasePrice: number;
  responsiblePerson: string;
  location: string;
  assetStatus: string;
  status: number;
}

export interface AssetCategory {
  id: string;
  categoryName: string;
  parentId: string;
  level: number;
}

export interface AssetQueryParams {
  pageNum: number;
  pageSize: number;
  assetName?: string;
  categoryId?: string;
  assetStatus?: string;
  status?: string;
}