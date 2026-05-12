import { AssetInfo, AssetCategory, AssetQueryParams } from '../model/asset/AssetModel';
import ApiClient from '../data/api/ApiClient';

export class AssetService {
  private static instance: AssetService = new AssetService();

  static getInstance(): AssetService {
    return this.instance;
  }

  async getAssetList(params: AssetQueryParams): Promise<{ records: AssetInfo[], total: number }> {
    const res = await ApiClient.get<{ records: AssetInfo[], total: number }>('/api/v1/assets', params as Record<string, any>);
    return res.data;
  }

  async getCategories(): Promise<AssetCategory[]> {
    const res = await ApiClient.get<{ categories: AssetCategory[] }>('/api/v1/assets/categories');
    return res.data.categories;
  }

  async getAssetDetail(id: string): Promise<AssetInfo> {
    const res = await ApiClient.get<AssetInfo>(`/api/v1/assets/${id}`);
    return res.data;
  }

  async borrowAsset(id: string): Promise<void> {
    await ApiClient.post<void>(`/api/v1/assets/${id}/borrow`, {});
  }

  async returnAsset(id: string): Promise<void> {
    await ApiClient.post<void>(`/api/v1/assets/${id}/return`, {});
  }
}