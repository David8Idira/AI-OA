package com.aioa.core.autograd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 神经元层 - 参考micrograd的神经网络实现
 * 应用场景：AI-OA中的自定义AI组件
 */
public class Neuron {
    
    private List<Value> weights;
    private Value bias;
    private boolean useNonlinearity;
    
    /**
     * 构造函数
     * @param nin 输入维度
     * @param useNonlinearity 是否使用非线性激活
     */
    public Neuron(int nin, boolean useNonlinearity) {
        this.weights = new ArrayList<>();
        this.useNonlinearity = useNonlinearity;
        
        // 初始化权重（使用Xavier初始化）
        Random random = new Random();
        double scale = Math.sqrt(2.0 / nin);
        
        for (int i = 0; i < nin; i++) {
            double weight = random.nextGaussian() * scale;
            weights.add(new Value(weight));
        }
        
        // 初始化偏置
        this.bias = new Value(0.0);
    }
    
    /**
     * 前向传播
     */
    public Value forward(List<Value> inputs) {
        if (inputs.size() != weights.size()) {
            throw new IllegalArgumentException("输入维度必须等于权重数量");
        }
        
        // 线性组合：sum(w_i * x_i) + b
        Value activation = Value.constant(0);
        
        for (int i = 0; i < inputs.size(); i++) {
            Value weighted = weights.get(i).mul(inputs.get(i));
            activation = activation.add(weighted);
        }
        
        activation = activation.add(bias);
        
        // 非线性激活（如果需要）
        if (useNonlinearity) {
            return activation.relu();
        }
        
        return activation;
    }
    
    /**
     * 获取所有参数
     */
    public List<Value> parameters() {
        List<Value> params = new ArrayList<>(weights);
        params.add(bias);
        return params;
    }
    
    /**
     * 更新参数（梯度下降）
     */
    public void updateParameters(double learningRate) {
        for (Value param : parameters()) {
            param.update(learningRate);
        }
    }
    
    /**
     * 清零梯度
     */
    public void zeroGrad() {
        for (Value param : parameters()) {
            param.zeroGrad();
        }
    }
    
    /**
     * 获取权重数量
     */
    public int getWeightCount() {
        return weights.size();
    }
    
    /**
     * 获取是否使用非线性激活
     */
    public boolean isUseNonlinearity() {
        return useNonlinearity;
    }
    
    @Override
    public String toString() {
        return String.format("Neuron(nin=%d, nonlinearity=%s)", 
            weights.size(), useNonlinearity ? "ReLU" : "None");
    }
}
