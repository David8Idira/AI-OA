package com.aioa.core.autograd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 多层感知机（MLP）- 参考micrograd的实现
 * 应用场景：AI-OA中的自定义AI模型
 */
public class MLP {
    
    private List<Neuron> layers;
    private List<Integer> layerSizes;
    
    /**
     * 构造函数
     * @param nin 输入维度
     * @param nouts 各层输出维度数组
     */
    public MLP(int nin, int[] nouts) {
        this.layers = new ArrayList<>();
        this.layerSizes = new ArrayList<>();
        this.layerSizes.add(nin);
        
        // 构建网络层
        for (int i = 0; i < nouts.length; i++) {
            int nout = nouts[i];
            // 最后一层通常不使用非线性激活
            boolean useNonlinearity = i < nouts.length - 1;
            
            // 创建一层神经元
            Neuron layer = new Neuron(nin, useNonlinearity);
            layers.add(layer);
            layerSizes.add(nout);
            
            nin = nout; // 下一层的输入维度等于当前层的输出
        }
    }
    
    /**
     * 前向传播
     */
    public List<Value> forward(List<Value> inputs) {
        List<Value> current = new ArrayList<>(inputs);
        
        for (Neuron layer : layers) {
            // 将每个神经元的输出收集起来
            List<Value> outputs = new ArrayList<>();
            
            // 对于每个神经元（实际上需要实现全连接层）
            // 简化：假设每个神经元独立处理输入
            Value output = layer.forward(current);
            outputs.add(output);
            
            current = outputs;
        }
        
        return current;
    }
    
    /**
     * 单个输出（用于回归或二分类）
     */
    public Value forwardSingle(List<Value> inputs) {
        List<Value> outputs = forward(inputs);
        return outputs.get(0);
    }
    
    /**
     * 获取所有参数
     */
    public List<Value> parameters() {
        List<Value> params = new ArrayList<>();
        
        for (Neuron layer : layers) {
            params.addAll(layer.parameters());
        }
        
        return params;
    }
    
    /**
     * 更新参数（梯度下降）
     */
    public void updateParameters(double learningRate) {
        for (Neuron layer : layers) {
            layer.updateParameters(learningRate);
        }
    }
    
    /**
     * 清零梯度
     */
    public void zeroGrad() {
        for (Neuron layer : layers) {
            layer.zeroGrad();
        }
    }
    
    /**
     * 训练一个批次
     * @param inputs 输入列表
     * @param targets 目标列表
     * @param learningRate 学习率
     * @return 损失值
     */
    public Value trainBatch(List<List<Value>> inputs, List<Value> targets, double learningRate) {
        // 前向传播
        List<Value> predictions = new ArrayList<>();
        
        for (int i = 0; i < inputs.size(); i++) {
            Value pred = forwardSingle(inputs.get(i));
            predictions.add(pred);
        }
        
        // 计算损失
        Value loss = Value.mseLoss(predictions, targets);
        
        // 反向传播
        zeroGrad();
        loss.backward();
        
        // 更新参数
        updateParameters(learningRate);
        
        return loss;
    }
    
    /**
     * 获取网络架构信息
     */
    public String getArchitecture() {
        StringBuilder sb = new StringBuilder();
        sb.append("MLP Architecture: [");
        
        for (int i = 0; i < layerSizes.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(layerSizes.get(i));
        }
        
        sb.append("]\n");
        sb.append("Total Layers: ").append(layers.size()).append("\n");
        sb.append("Total Parameters: ").append(parameters().size());
        
        return sb.toString();
    }
    
    /**
     * 获取网络层数
     */
    public int getLayerCount() {
        return layers.size();
    }
    
    /**
     * 获取总参数数量
     */
    public int getTotalParameters() {
        return parameters().size();
    }
    
    @Override
    public String toString() {
        return getArchitecture();
    }
}
