/**
 * Asset Service - 资产管理服务
 */
import { AssetInfo, AssetCategory, AssetQueryParams } from '../model/asset/AssetModel';

export class AssetService {
  private static instance: AssetService = new AssetService();

  static getInstance(): AssetService {
    return this.instance;
  }

  /**
   * 获取资产列表
   */
  getAssetList(params: AssetQueryParams): Promise<{ records: AssetInfo[], total: number }> {
    return new Promise((resolve) => {
      // 模拟数据
      setTimeout(() => {
        const mockData: AssetInfo[] = [
          {
            id: '1',
            assetCode: 'AST001',
            assetName: '联想ThinkPad笔记本',
            categoryId: 'C001',
            categoryName: '办公设备',
            model: 'T490',
            currentQuantity: 50,
            warningQuantity: 10,
            purchasePrice: 7999.00,
            responsiblePerson: '张三',
            location: '3号楼201',
            assetStatus: '1',
            status: 1
          },
          {
            id: '2',
            assetCode: 'AST002',
            assetName: 'Dell显示器27寸',
            categoryId: 'C002',
            categoryName: '办公设备',
            model: 'U2720Q',
            currentQuantity: 30,
            warningQuantity: 5,
            purchasePrice: 3500.00,
            responsiblePerson: '李四',
            location: '3号楼202',
            assetStatus: '1',
            status: 1
          },
          {
            id: '3',
            assetCode: 'AST003',
            assetName: '罗技无线鼠标',
            categoryId: 'C003',
            categoryName: '配件',
            model: 'MX Master 3',
            currentQuantity: 100,
            warningQuantity: 20,
            purchasePrice: 799.00,
            responsiblePerson: '王五',
            location: '仓库A',
            assetStatus: '2',
            status: 1
          }
        ];

        // 简单的过滤
        let filtered = mockData;
        if (params.assetName) {
          filtered = filtered.filter(a => a.assetName.includes(params.assetName));
        }
        if (params.categoryId) {
          filtered = filtered.filter(a => a.categoryId === params.categoryId);
        }
        if (params.assetStatus) {
          filtered = filtered.filter(a => a.assetStatus === params.assetStatus);
        }

        resolve({
          records: filtered,
          total: filtered.length
        });
      }, 800);
    });
  }

  /**
   * 获取分类列表
   */
  getCategories(): Promise<AssetCategory[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { id: 'C001', categoryName: '办公设备', parentId: '', level: 1 },
          { id: 'C002', categoryName: '电子设备', parentId: '', level: 1 },
          { id: 'C003', categoryName: '配件', parentId: '', level: 1 },
          { id: 'C004', categoryName: '家具', parentId: '', level: 1 }
        ]);
      }, 500);
    });
  }

  /**
   * 获取资产详情
   */
  getAssetDetail(id: string): Promise<AssetInfo> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const mockData: AssetInfo = {
          id: id,
          assetCode: 'AST001',
          assetName: '联想ThinkPad笔记本',
          categoryId: 'C001',
          categoryName: '办公设备',
          model: 'T490',
          currentQuantity: 50,
          warningQuantity: 10,
          purchasePrice: 7999.00,
          responsiblePerson: '张三',
          location: '3号楼201',
          assetStatus: '1',
          status: 1
        };
        resolve(mockData);
      }, 500);
    });
  }

  /**
   * 领用资产
   */
  borrowAsset(id: string): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve();
      }, 800);
    });
  }

  /**
   * 归还资产
   */
  returnAsset(id: string): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve();
      }, 800);
    });
  }
}

export const assetService = AssetService.getInstance();