package com.aioa.core.autograd;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试示例 - 展示Java版micrograd的使用
 */
public class TestExample {
    
    public static void main(String[] args) {
        System.out.println("=== Java版micrograd测试示例 ===\n");
        
        // 示例1：基本运算和梯度计算
        System.out.println("1. 基本运算测试：");
        Value a = new Value(2.0);
        Value b = new Value(3.0);
        Value c = a.mul(b);
        Value d = c.add(new Value(1.0));
        Value e = d.relu();
        
        System.out.println("   a = " + a);
        System.out.println("   b = " + b);
        System.out.println("   c = a * b = " + c);
        System.out.println("   d = c + 1 = " + d);
        System.out.println("   e = ReLU(d) = " + e);
        
        // 反向传播
        e.backward();
        System.out.println("   反向传播后：");
        System.out.println("   ∂e/∂a = " + a.getGrad());
        System.out.println("   ∂e/∂b = " + b.getGrad());
        System.out.println();
        
        // 示例2：线性回归
        System.out.println("2. 线性回归测试：");
        Value w = new Value(0.5);  // 权重
        Value x = new Value(2.0);  // 输入
        Value bias = new Value(1.0);  // 偏置
        Value y_pred = w.mul(x).add(bias);  // 预测值
        
        Value y_true = new Value(2.0);  // 真实值
        Value loss = y_pred.sub(y_true).pow(2);  // 均方误差
        
        System.out.println("   预测: y_pred = " + y_pred.getData());
        System.out.println("   真实: y_true = " + y_true.getData());
        System.out.println("   损失: loss = " + loss.getData());
        
        loss.backward();
        System.out.println("   梯度: ∂loss/∂w = " + w.getGrad());
        System.out.println("         ∂loss/∂bias = " + bias.getGrad());
        System.out.println();
        
        // 示例3：梯度下降更新
        System.out.println("3. 梯度下降更新测试：");
        double learningRate = 0.1;
        System.out.println("   更新前: w = " + w.getData() + ", bias = " + bias.getData());
        
        w.setData(w.getData() - learningRate * w.getGrad());
        bias.setData(bias.getData() - learningRate * bias.getGrad());
        
        System.out.println("   更新后: w = " + w.getData() + ", bias = " + bias.getData());
        System.out.println();
        
        // 示例4：神经元测试
        System.out.println("4. 神经元测试：");
        Neuron neuron = new Neuron(3, true);
        System.out.println("   神经元: " + neuron);
        System.out.println("   参数数量: " + neuron.parameters().size());
        System.out.println();
        
        // 示例5：多层感知机测试
        System.out.println("5. 多层感知机测试：");
        int[] layerSizes = {3, 4, 2, 1};  // 输入3 -> 隐藏4 -> 隐藏2 -> 输出1
        MLP mlp = new MLP(3, layerSizes);
        System.out.println(mlp.getArchitecture());
        System.out.println("   总参数: " + mlp.getTotalParameters());
        
        System.out.println("\n=== 测试完成 ===");
    }
}
