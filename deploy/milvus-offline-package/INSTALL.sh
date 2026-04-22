#!/bin/bash

# Milvus离线部署安装脚本
# 毛泽东思想指导：实事求是，自力更生

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    # 检查系统资源
    local mem_kb=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    local mem_gb=$((mem_kb / 1024 / 1024))
    
    if [ $mem_gb -lt 4 ]; then
        log_warn "系统内存不足（当前: ${mem_gb}GB，建议: 4GB+）"
    fi
    
    # 检查磁盘空间
    local disk_gb=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ $disk_gb -lt 10 ]; then
        log_warn "磁盘空间不足（当前: ${disk_gb}GB，建议: 10GB+）"
    fi
    
    log_info "依赖检查通过"
}

# 创建目录结构
create_directories() {
    log_info "创建目录结构..."
    
    mkdir -p volumes/etcd
    mkdir -p volumes/minio
    mkdir -p volumes/milvus
    mkdir -p logs
    mkdir -p backup
    
    log_info "目录创建完成"
}

# 设置权限
set_permissions() {
    log_info "设置目录权限..."
    
    chmod 755 volumes
    chmod 755 logs
    chmod 755 backup
    
    # 设置Docker需要的权限
    if [ -d "volumes/etcd" ]; then
        chmod 777 volumes/etcd
    fi
    
    if [ -d "volumes/minio" ]; then
        chmod 777 volumes/minio
    fi
    
    if [ -d "volumes/milvus" ]; then
        chmod 777 volumes/milvus
    fi
    
    log_info "权限设置完成"
}

# 加载Docker镜像
load_docker_images() {
    log_info "加载Docker镜像..."
    
    # 检查镜像文件
    if [ ! -f "milvus-standalone-docker.tar" ]; then
        log_error "未找到Docker镜像文件: milvus-standalone-docker.tar"
        exit 1
    fi
    
    # 加载镜像
    docker load -i milvus-standalone-docker.tar
    
    # 验证镜像
    if docker images | grep -q "milvusdb/milvus"; then
        log_info "Docker镜像加载成功"
    else
        log_error "Docker镜像加载失败"
        exit 1
    fi
}

# 配置环境变量
setup_environment() {
    log_info "配置环境变量..."
    
    # 创建环境变量文件
    cat > .env << EOF
# Milvus环境配置
MILVUS_HOST=localhost
MILVUS_PORT=19530
ETCD_PORT=2379
MINIO_PORT=9000

# 存储路径
VOLUME_DIR=$(pwd)/volumes
LOG_DIR=$(pwd)/logs

# 默认凭据
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# 性能配置
MILVUS_CACHE_SIZE=4GB
MILVUS_INSERT_BUFFER_SIZE=1GB
EOF
    
    # 加载环境变量
    source .env
    
    log_info "环境变量配置完成"
}

# 启动服务
start_services() {
    log_info "启动Milvus服务..."
    
    # 检查Docker Compose文件
    if [ ! -f "docker-compose.yml" ]; then
        log_error "未找到docker-compose.yml文件"
        exit 1
    fi
    
    # 启动服务
    docker-compose up -d
    
    # 等待服务启动
    log_info "等待服务启动（30秒）..."
    sleep 30
    
    # 检查服务状态
    if check_service_health; then
        log_info "Milvus服务启动成功"
    else
        log_error "Milvus服务启动失败"
        docker-compose logs
        exit 1
    fi
}

# 检查服务健康状态
check_service_health() {
    log_info "检查服务健康状态..."
    
    # 检查Docker容器
    local container_count=$(docker-compose ps -q | wc -l)
    if [ $container_count -lt 3 ]; then
        log_error "容器数量不足，预期3个，实际${container_count}个"
        return 1
    fi
    
    # 检查Milvus服务
    local milvus_health=$(curl -s http://localhost:19530/health || echo "unhealthy")
    if [ "$milvus_health" != "healthy" ]; then
        log_warn "Milvus健康检查返回: $milvus_health"
        return 1
    fi
    
    # 检查etcd
    if ! docker-compose exec -T etcd etcdctl endpoint health; then
        log_warn "etcd健康检查失败"
        return 1
    fi
    
    # 检查minio
    if ! curl -s http://localhost:9000/minio/health/live > /dev/null; then
        log_warn "MinIO健康检查失败"
        return 1
    fi
    
    log_info "所有服务健康检查通过"
    return 0
}

# 创建验证脚本
create_verification_script() {
    log_info "创建验证脚本..."
    
    cat > verify_milvus.py << 'EOF'
#!/usr/bin/env python3
"""
Milvus服务验证脚本
"""

import sys
import time
from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType

def test_connection():
    """测试连接"""
    try:
        connections.connect("default", host="localhost", port="19530")
        print("✓ Milvus连接成功")
        return True
    except Exception as e:
        print(f"✗ Milvus连接失败: {e}")
        return False

def test_collection_operations():
    """测试集合操作"""
    try:
        # 定义字段
        fields = [
            FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
            FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=128)
        ]
        
        # 创建schema
        schema = CollectionSchema(fields, description="测试集合")
        
        # 创建集合
        collection_name = "test_collection_" + str(int(time.time()))
        collection = Collection(name=collection_name, schema=schema)
        print(f"✓ 创建集合成功: {collection_name}")
        
        # 插入数据
        import numpy as np
        vectors = np.random.random((10, 128)).tolist()
        collection.insert([vectors])
        print("✓ 插入数据成功")
        
        # 创建索引
        index_params = {
            "index_type": "IVF_FLAT",
            "metric_type": "L2", 
            "params": {"nlist": 1024}
        }
        collection.create_index("embedding", index_params)
        print("✓ 创建索引成功")
        
        # 搜索测试
        search_params = {"metric_type": "L2", "params": {"nprobe": 10}}
        results = collection.search(
            vectors[:1], "embedding", search_params, limit=3
        )
        print(f"✓ 搜索测试成功，返回 {len(results[0])} 个结果")
        
        # 清理测试集合
        collection.drop()
        print(f"✓ 清理测试集合成功")
        
        return True
        
    except Exception as e:
        print(f"✗ 集合操作测试失败: {e}")
        return False

def main():
    print("=" * 50)
    print("Milvus服务验证")
    print("=" * 50)
    
    tests = [
        ("连接测试", test_connection),
        ("集合操作测试", test_collection_operations)
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        print(f"\n▶ 执行 {test_name}...")
        if test_func():
            passed += 1
        time.sleep(1)
    
    print(f"\n" + "=" * 50)
    print(f"测试结果: {passed}/{total} 通过")
    
    if passed == total:
        print("✅ 所有测试通过，Milvus服务正常")
        return 0
    else:
        print("❌ 部分测试失败，请检查服务状态")
        return 1

if __name__ == "__main__":
    sys.exit(main())
EOF
    
    # 创建Shell验证脚本
    cat > verify_milvus.sh << 'EOF'
#!/bin/bash
# Milvus服务验证脚本

echo "开始验证Milvus服务..."
echo "========================"

# 检查端口
echo "1. 检查端口..."
if nc -z localhost 19530; then
    echo "  ✓ 端口19530已开放"
else
    echo "  ✗ 端口19530未开放"
    exit 1
fi

# 检查健康接口
echo "2. 检查健康接口..."
HEALTH=$(curl -s http://localhost:19530/health)
if [ "$HEALTH" = "healthy" ]; then
    echo "  ✓ 健康检查通过: $HEALTH"
else
    echo "  ✗ 健康检查失败: $HEALTH"
    exit 1
fi

# 检查Docker容器
echo "3. 检查Docker容器..."
CONTAINERS=$(docker-compose ps --services | wc -l)
if [ $CONTAINERS -ge 3 ]; then
    echo "  ✓ Docker容器运行正常: $CONTAINERS 个服务"
else
    echo "  ✗ Docker容器数量不足: $CONTAINERS 个"
    exit 1
fi

# 检查服务日志
echo "4. 检查服务日志..."
ERROR_COUNT=$(docker-compose logs --tail=100 | grep -i error | wc -l)
if [ $ERROR_COUNT -eq 0 ]; then
    echo "  ✓ 服务日志无错误"
else
    echo "  ✗ 发现 $ERROR_COUNT 个错误，请查看日志"
    docker-compose logs --tail=20 | grep -i error
fi

echo "========================"
echo "验证完成！"
echo "服务状态: ✅ 正常"
EOF
    
    chmod +x verify_milvus.py verify_milvus.sh
    log_info "验证脚本创建完成"
}

# 创建管理脚本
create_management_scripts() {
    log_info "创建管理脚本..."
    
    # 创建启动脚本
    cat > start_milvus.sh << 'EOF'
#!/bin/bash
# 启动Milvus服务

cd "$(dirname "$0")"
echo "启动Milvus服务..."
docker-compose up -d
echo "服务启动完成"
EOF
    
    # 创建停止脚本
    cat > stop_milvus.sh << 'EOF'
#!/bin/bash
# 停止Milvus服务

cd "$(dirname "$0")"
echo "停止Milvus服务..."
docker-compose down
echo "服务已停止"
EOF
    
    # 创建重启脚本
    cat > restart_milvus.sh << 'EOF'
#!/bin/bash
# 重启Milvus服务

cd "$(dirname "$0")"
echo "重启Milvus服务..."
docker-compose restart
echo "服务重启完成"
EOF
    
    # 创建状态检查脚本
    cat > status_milvus.sh << 'EOF'
#!/bin/bash
# 检查Milvus服务状态

cd "$(dirname "$0")"
echo "Milvus服务状态："
echo "========================"
docker-compose ps
echo "========================"
echo "服务日志（最后10行）："
docker-compose logs --tail=10
EOF
    
    # 创建备份脚本
    cat > backup_milvus.sh << 'EOF'
#!/bin/bash
# 备份Milvus数据

cd "$(dirname "$0")"
BACKUP_DIR="backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "开始备份Milvus数据..."
echo "备份目录: $BACKUP_DIR"

# 备份etcd数据
if [ -d "volumes/etcd" ]; then
    tar -czf "$BACKUP_DIR/etcd.tar.gz" volumes/etcd
    echo "✓ 备份etcd数据完成"
fi

# 备份minio数据
if [ -d "volumes/minio" ]; then
    tar -czf "$BACKUP_DIR/minio.tar.gz" volumes/minio
    echo "✓ 备份minio数据完成"
fi

# 备份milvus数据
if [ -d "volumes/milvus" ]; then
    tar -czf "$BACKUP_DIR/milvus.tar.gz" volumes/milvus
    echo "✓ 备份milvus数据完成"
fi

# 备份配置文件
tar -czf "$BACKUP_DIR/configs.tar.gz" docker-compose.yml .env config/
echo "✓ 备份配置文件完成"

echo "备份完成！"
echo "备份文件保存在: $BACKUP_DIR"
EOF
    
    # 设置执行权限
    chmod +x *.sh
    
    log_info "管理脚本创建完成"
}

# 创建SDK集成示例
create_sdk_examples() {
    log_info "创建SDK集成示例..."
    
    # Java示例
    mkdir -p examples/java
    cat > examples/java/MilvusExample.java << 'EOF'
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.grpc.DataType;

import java.util.ArrayList;
import java.util.List;

public class MilvusExample {
    public static void main(String[] args) {
        // 1. 连接Milvus
        MilvusServiceClient client = new MilvusServiceClient(
            ConnectParam.newBuilder()
                .withHost("localhost")
                .withPort(19530)
                .build()
        );
        
        System.out.println("✅ 连接Milvus成功");
        
        // 2. 检查集合是否存在
        String collectionName = "knowledge_base";
        R<Boolean> hasCollection = client.hasCollection(
            HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()
        );
        
        if (!hasCollection.getData()) {
            // 3. 创建集合
            FieldType field1 = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();
            
            FieldType field2 = FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(1536)
                .build();
            
            FieldType field3 = FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(65535)
                .build();
            
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("知识库向量存储")
                .withFieldTypes(field1, field2, field3)
                .build();
            
            client.createCollection(createParam);
            System.out.println("✅ 创建集合成功: " + collectionName);
            
            // 4. 创建索引
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType("IVF_FLAT")
                .withMetricType("L2")
                .withExtraParam("{\"nlist\":1024}")
                .build();
            
            client.createIndex(indexParam);
            System.out.println("✅ 创建索引成功");
        } else {
            System.out.println("ℹ️ 集合已存在: " + collectionName);
        }
        
        // 5. 插入示例数据
        List<InsertParam.Field> fields = new ArrayList<>();
        
        // 添加向量数据
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            vector.add((float) Math.random());
        }
        List<List<Float>> vectors = new ArrayList<>();
        vectors.add(vector);
        fields.add(new InsertParam.Field("embedding", vectors));
        
        // 添加文本内容
        List<String> contents = new ArrayList<>();
        contents.add("这是测试文档内容");
        fields.add(new InsertParam.Field("content", contents));
        
        InsertParam insertParam = InsertParam.newBuilder()
            .withCollectionName(collectionName)
            .withFields(fields)
            .build();
        
        client.insert(insertParam);
        System.out.println("✅ 插入测试数据成功");
        
        // 6. 加载集合
        client.loadCollection(
            io.milvus.param.collection.LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()
        );
        
        System.out.println("✅ 加载集合成功");
        System.out.println("🎉 Milvus集成测试完成！");
        
        client.close();
    }
}
EOF
    
    # Python示例
    cat > examples/python/milvus_example.py << 'EOF'
from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType
import numpy as np

def main():
    # 1. 连接Milvus
    connections.connect("default", host="localhost", port=19530)
    print("✅ 连接Milvus成功")
    
    # 2. 定义集合
    collection_name = "knowledge_base"
    
    # 定义字段
    fields = [
        FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
        FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=1536),
        FieldSchema(name="content", dtype=DataType.VARCHAR, max_length=65535),
        FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=255),
        FieldSchema(name="category", dtype=DataType.VARCHAR, max_length=100)
    ]
    
    schema = CollectionSchema(fields, description="知识库向量存储")
    
    # 3. 创建集合（如果不存在）
    if not connections.has_collection(collection_name):
        collection = Collection(name=collection_name, schema=schema)
        print(f"✅ 创建集合成功: {collection_name}")
        
        # 4. 创建索引
        index_params = {
            "index_type": "IVF_FLAT",
            "metric_type": "L2",
            "params": {"nlist": 1024}
        }
        
        collection.create_index("embedding", index_params)
        print("✅ 创建索引成功")
    else:
        collection = Collection(collection_name)
        print(f"ℹ️ 集合已存在: {collection_name}")
    
    # 5. 插入测试数据
    # 生成随机向量
    vectors = np.random.random((10, 1536)).tolist()
    
    # 准备数据
    data = [
        vectors,  # embedding
        ["文档内容" + str(i) for i in range(10)],  # content
        ["标题" + str(i) for i in range(10)],      # title
        ["分类" + str(i % 3) for i in range(10)]   # category
    ]
    
    # 插入数据
    mr = collection.insert(data)
    print(f"✅ 插入 {len(vectors)} 条数据成功")
    
    # 6. 加载集合
    collection.load()
    print("✅ 加载集合成功")
    
    # 7. 搜索测试
    search_params = {"metric_type": "L2", "params": {"nprobe": 10}}
    
    # 使用第一个向量进行搜索
    results = collection.search(
        [vectors[0]], 
        "embedding", 
        search_params, 
        limit=3,
        output_fields=["title", "content"]
    )
    
    print("✅ 搜索测试成功")
    print(f"搜索结果数量: {len(results[0])}")
    
    for i, hit in enumerate(results[0]):
        print(f"  结果 {i+1}: id={hit.id}, 距离={hit.distance}, 标题={hit.entity.get('title')}")
    
    print("\n🎉 Python SDK测试完成！")

if __name__ == "__main__":
    main()
EOF
    
    # Node.js示例
    cat > examples/nodejs/milvus_example.js << 'EOF'
const { MilvusClient, DataType } = require('@zilliz/milvus2-sdk-node');

async function main() {
    // 1. 连接Milvus
    const milvusClient = new MilvusClient({
        address: 'localhost:19530'
    });
    
    console.log('✅ 连接Milvus成功');
    
    // 2. 检查集合是否存在
    const collectionName = 'knowledge_base';
    const hasCollection = await milvusClient.hasCollection({
        collection_name: collectionName
    });
    
    if (!hasCollection.value) {
        // 3. 创建集合
        await milvusClient.createCollection({
            collection_name: collectionName,
            fields: [
                {
                    name: 'id',
                    description: '文档ID',
                    data_type: DataType.Int64,
                    is_primary_key: true,
                    autoID: true
                },
                {
                    name: 'embedding',
                    description: '向量嵌入',
                    data_type: DataType.FloatVector,
                    dim: 1536
                },
                {
                    name: 'content',
                    description: '文档内容',
                    data_type: DataType.VarChar,
                    max_length: 65535
                },
                {
                    name: 'title',
                    description: '文档标题',
                    data_type: DataType.VarChar,
                    max_length: 255
                }
            ]
        });
        
        console.log(`✅ 创建集合成功: ${collectionName}`);
        
        // 4. 创建索引
        await milvusClient.createIndex({
            collection_name: collectionName,
            field_name: 'embedding',
            index_name: 'embedding_index',
            index_type: 'IVF_FLAT',
            metric_type: 'L2',
            params: { nlist: 1024 }
        });
        
        console.log('✅ 创建索引成功');
    } else {
        console.log(`ℹ️ 集合已存在: ${collectionName}`);
    }
    
    // 5. 插入测试数据
    // 生成随机向量
    const generateVectors = (count, dim) => {
        const vectors = [];
        for (let i = 0; i < count; i++) {
            const vector = [];
            for (let j = 0; j < dim; j++) {
                vector.push(Math.random());
            }
            vectors.push(vector);
        }
        return vectors;
    };
    
    const vectors = generateVectors(5, 1536);
    const contents = vectors.map((_, i) => `测试文档内容 ${i + 1}`);
    const titles = vectors.map((_, i) => `测试标题 ${i + 1}`);
    
    const insertResult = await milvusClient.insert({
        collection_name: collectionName,
        fields_data: [
            { embedding: vectors },
            { content: contents },
            { title: titles }
        ]
    });
    
    console.log(`✅ 插入 ${vectors.length} 条数据成功`);
    
    // 6. 加载集合
    await milvusClient.loadCollection({
        collection_name: collectionName
    });
    
    console.log('✅ 加载集合成功');
    
    // 7. 搜索测试
    const searchResult = await milvusClient.search({
        collection_name: collectionName,
        vectors: [vectors[0]],
        search_params: {
            anns_field: 'embedding',
            topk: 3,
            metric_type: 'L2',
            params: JSON.stringify({ nprobe: 10 })
        },
        output_fields: ['title', 'content']
    });
    
    console.log('✅ 搜索测试成功');
    console.log(`搜索结果数量: ${searchResult.results.length}`);
    
    searchResult.results.forEach((result, i) => {
        console.log(`  结果 ${i + 1}: id=${result.id}, 距离=${result.distance}`);
    });
    
    console.log('\n🎉 Node.js SDK测试完成！');
    
    // 关闭连接
    await milvusClient.closeConnection();
}

main().catch(console.error);
EOF
    
    log_info "SDK示例创建完成"
}

# 主安装流程
main() {
    echo "=========================================="
    echo "    Milvus离线部署包安装程序"
    echo "=========================================="
    
    # 检查依赖
    check_dependencies
    
    # 创建目录
    create_directories
    
    # 设置权限
    set_permissions
    
    # 加载镜像
    load_docker_images
    
    # 配置环境
    setup_environment
    
    # 创建脚本
    create_verification_script
    create_management_scripts
    create_sdk_examples
    
    # 启动服务
    start_services
    
    echo ""
    echo "=========================================="
    echo "      Milvus安装完成！"
    echo "=========================================="
    echo ""
    echo "服务信息："
    echo "  - Milvus地址: http://localhost:19530"
    echo "  - 健康检查: curl http://localhost:19530/health"
    echo "  - MinIO管理: http://localhost:9000"
    echo "  - 默认账号: minioadmin / minioadmin"
    echo ""
    echo "管理命令："
    echo "  - 启动服务: ./start_milvus.sh"
    echo "  - 停止服务: ./stop_milvus.sh"
    echo "  - 查看状态: ./status_milvus.sh"
    echo "  - 验证服务: ./verify_milvus.sh"
    echo ""
    echo "SDK集成示例："
    echo "  - Java: examples/java/MilvusExample.java"
    echo "  - Python: examples/python/milvus_example.py"
    echo "  - Node.js: examples/nodejs/milvus_example.js"
    echo ""
    echo "如需帮助，请查看 README.md 文件"
    echo "=========================================="
}

# 执行主函数
main "$@"