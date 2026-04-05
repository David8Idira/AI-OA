#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI-OA POC测试框架
"""

import os
import json
import time
import random
from datetime import datetime
from pathlib import Path

class POCTestRunner:
    def __init__(self, poc_name, output_dir):
        self.poc_name = poc_name
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.results = []
        
    def log(self, message):
        print(f"[{self.poc_name}] {message}")
        
    def save_results(self):
        output_file = self.output_dir / f"{self.poc_name}_results.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump({
                'poc_name': self.poc_name,
                'timestamp': datetime.now().isoformat(),
                'results': self.results
            }, f, ensure_ascii=False, indent=2)
        self.log(f"结果已保存: {output_file}")
        
    def calculate_pass_rate(self, threshold):
        passed = sum(1 for r in self.results if r['score'] >= threshold)
        return passed / len(self.results) if self.results else 0
        
    def generate_report(self, summary):
        report_file = self.output_dir / f"{self.poc_name}_report.md"
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write(f"# {self.poc_name.upper()} POC测试报告\n\n")
            f.write(f"**测试时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
            f.write("---\n\n")
            f.write("## 测试概况\n\n")
            f.write(f"- 测试用例数: {len(self.results)}\n")
            f.write(f"- 通过数: {sum(1 for r in self.results if r['passed'])}\n")
            f.write(f"- 失败数: {sum(1 for r in self.results if not r['passed'])}\n")
            f.write(f"- 通过率: {sum(1 for r in self.results if r['passed'])/len(self.results)*100:.1f}%\n\n")
            f.write("---\n\n")
            f.write("## 详细结果\n\n")
            for i, r in enumerate(self.results, 1):
                status = "✅ 通过" if r['passed'] else "❌ 失败"
                f.write(f"### {i}. {r['name']} {status}\n\n")
                for k, v in r.items():
                    if k not in ['name', 'passed']:
                        f.write(f"- {k}: {v}\n")
                f.write("\n")
            f.write("---\n\n")
            f.write("## 结论\n\n")
            f.write(summary)
            
        self.log(f"报告已生成: {report_file}")
        return report_file

def run_all_pocs():
    """执行所有POC测试"""
    poc_base = Path('/root/workspace/AI-OA/poc')
    
    pocs = [
        ('ocr', OCRPOCTest),
        ('rag', RAGPOCTest),
        ('report', ReportPOCTest),
        ('n8n', N8nPOCTest),
        ('multimodel', MultiModelPOCTest),
        ('im', IMPOCTest),
    ]
    
    reports = []
    for poc_name, poc_class in pocs:
        print(f"\n{'='*60}")
        print(f"执行 {poc_name.upper()} POC测试")
        print('='*60)
        
        poc = poc_class(poc_base / poc_name)
        report = poc.run()
        reports.append(report)
    
    return reports

if __name__ == '__main__':
    run_all_pocs()
