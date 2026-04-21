package com.aioa.core.autograd;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Java版自动微分引擎 - 参考Andrej Karpathy的micrograd
 * 核心思想：通过计算图实现反向传播
 * 应用场景：AI-OA中的自定义优化算法
 */
public class Value {
    
    private double data;
    private double grad;
    private List<Value> children;
    private Runnable backward;
    private String op;
    
    /**
     * 构造函数
     * @param data 数据值
     * @param children 子节点
     * @param op 操作符
     */
    public Value(double data, List<Value> children, String op) {
        this.data = data;
        this.grad = 0.0;
        this.children = children != null ? children : new LinkedList<>();
        this.op = op != null ? op : "";
        this.backward = () -> {};
    }
    
    public Value(double data) {
        this(data, null, "");
    }
    
    // ==================== 基本运算 ====================
    
    /**
     * 加法运算
     */
    public Value add(Value other) {
        Value out = new Value(this.data + other.data, List.of(this, other), "+");
        
        out.backward = () -> {
            this.grad += out.grad;
            other.grad += out.grad;
        };
        
        return out;
    }
    
    /**
     * 乘法运算
     */
    public Value mul(Value other) {
        Value out = new Value(this.data * other.data, List.of(this, other), "*");
        
        out.backward = () -> {
            this.grad += other.data * out.grad;
            other.grad += this.data * out.grad;
        };
        
        return out;
    }
    
    /**
     * 幂运算
     */
    public Value pow(double exponent) {
        Value out = new Value(Math.pow(this.data, exponent), List.of(this), "**" + exponent);
        
        out.backward = () -> {
            this.grad += (exponent * Math.pow(this.data, exponent - 1)) * out.grad;
        };
        
        return out;
    }
    
    /**
     * ReLU激活函数
     */
    public Value relu() {
        double output = this.data > 0 ? this.data : 0;
        Value out = new Value(output, List.of(this), "ReLU");
        
        out.backward = () -> {
            this.grad += (out.data > 0 ? 1 : 0) * out.grad;
        };
        
        return out;
    }
    
    /**
     * 减法运算
     */
    public Value sub(Value other) {
        return this.add(other.neg());
    }
    
    /**
     * 除法运算
     */
    public Value div(Value other) {
        return this.mul(other.pow(-1));
    }
    
    /**
     * 取负
     */
    public Value neg() {
        return this.mul(new Value(-1));
    }
    
    // ==================== 反向传播 ====================
    
    /**
     * 执行反向传播（计算梯度）
     */
    public void backward() {
        // 拓扑排序
        List<Value> topo = new LinkedList<>();
        Set<Value> visited = new HashSet<>();
        buildTopo(this, topo, visited);
        
        // 设置输出梯度为1
        this.grad = 1.0;
        
        // 反向传播计算梯度
        for (int i = topo.size() - 1; i >= 0; i--) {
            Value v = topo.get(i);
            v.backward.run();
        }
    }
    
    /**
     * 拓扑排序辅助函数
     */
    private void buildTopo(Value v, List<Value> topo, Set<Value> visited) {
        if (!visited.contains(v)) {
            visited.add(v);
            for (Value child : v.children) {
                buildTopo(child, topo, visited);
            }
            topo.add(v);
        }
    }
    
    // ==================== 静态方法 ====================
    
    /**
     * 创建常量值
     */
    public static Value constant(double value) {
        return new Value(value);
    }
    
    /**
     * 线性组合：y = wx + b
     */
    public static Value linear(Value w, Value x, Value b) {
        return w.mul(x).add(b);
    }
    
    /**
     * 均方误差损失
     */
    public static Value mseLoss(List<Value> predictions, List<Value> targets) {
        if (predictions.size() != targets.size()) {
            throw new IllegalArgumentException("预测和目标数量必须相同");
        }
        
        Value sum = constant(0);
        int n = predictions.size();
        
        for (int i = 0; i < n; i++) {
            Value diff = predictions.get(i).sub(targets.get(i));
            sum = sum.add(diff.pow(2));
        }
        
        return sum.div(constant(n));
    }
    
    // ==================== Getter/Setter ====================
    
    public double getData() {
        return data;
    }
    
    public void setData(double data) {
        this.data = data;
    }
    
    public double getGrad() {
        return grad;
    }
    
    public void setGrad(double grad) {
        this.grad = grad;
    }
    
    public List<Value> getChildren() {
        return children;
    }
    
    public String getOp() {
        return op;
    }
    
    /**
     * 清零梯度（用于批量训练）
     */
    public void zeroGrad() {
        this.grad = 0.0;
        for (Value child : children) {
            child.zeroGrad();
        }
    }
    
    /**
     * 参数更新：data = data - learningRate * grad
     */
    public void update(double learningRate) {
        this.data -= learningRate * this.grad;
        for (Value child : children) {
            child.update(learningRate);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Value(data=%.4f, grad=%.4f, op=%s)", data, grad, op);
    }
}
